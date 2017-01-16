/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.id.client.http;

import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.common.core.util.Formats.formatCount;
import static org.icgc.dcc.common.core.util.Joiners.TAB;

import java.io.File;
import java.util.Map;

import org.icgc.dcc.id.client.util.FileMutex;
import org.icgc.dcc.id.core.Prefixes;
import org.mapdb.DB;
import org.mapdb.DBException;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import com.google.common.base.Stopwatch;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Memory efficient, disk based cache for all entity ids.
 * <p>
 * Works with the dcc-id-server's {@code /export} APIs to download the entire DB. Can be used with and {@code IdClient}
 * to significantly improve lookup times.'
 */
@Slf4j
@RequiredArgsConstructor
public class ExportIdCache implements AutoCloseable {

  /**
   * Constants.
   */
  private static final File DEFAULT_CACHE_DIR = new File("/tmp");

  /**
   * Configuration.
   */
  private final File cacheDir;

  private final ExportClient exportClient;

  /**
   * State.
   */
  @Getter(lazy = true, value = PRIVATE)
  private final DB donorDB = loadEntity("donor");
  @Getter(lazy = true, value = PRIVATE)
  private final Map<String, Long> donorIds = createEntityMap(getDonorDB(), "donor");

  @Getter(lazy = true, value = PRIVATE)
  private final DB specimenDB = loadEntity("specimen");
  @Getter(lazy = true, value = PRIVATE)
  private final Map<String, Long> specimenIds = createEntityMap(getSpecimenDB(), "specimen");

  @Getter(lazy = true, value = PRIVATE)
  private final DB sampleDB = loadEntity("sample");
  @Getter(lazy = true, value = PRIVATE)
  private final Map<String, Long> sampleIds = createEntityMap(getSampleDB(), "sample");

  @Getter(lazy = true, value = PRIVATE)
  private final DB mutationDB = loadEntity("mutation");
  @Getter(lazy = true, value = PRIVATE)
  private final Map<String, Long> mutationIds = createEntityMap(getMutationDB(), "mutation");

  @Getter(lazy = true, value = PRIVATE)
  private final DB fileDB = loadEntity("file");
  @Getter(lazy = true, value = PRIVATE)
  private final Map<String, Long> fileIds = createEntityMap(getFileDB(), "file");

  private volatile boolean closed;

  public ExportIdCache() {
    this(DEFAULT_CACHE_DIR, new ExportClient());
  }

  public String getDonorId(@NonNull String projectCode, @NonNull String submittedDonorId) {
    val key = TAB.join(submittedDonorId, projectCode);
    val id = getDonorIds().get(key);
    if (id == null) return null;

    return Prefixes.DONOR_ID_PREFIX + id;
  }

  public String getSpecimenId(@NonNull String projectCode, @NonNull String submittedSpecimenId) {
    val key = TAB.join(submittedSpecimenId, projectCode);
    val id = getSpecimenIds().get(key);
    if (id == null) return null;

    return Prefixes.SPECIMEN_ID_PREFIX + id;
  }

  public String getSampleId(@NonNull String projectCode, @NonNull String submittedSampleId) {
    val key = TAB.join(submittedSampleId, projectCode);
    val id = getSampleIds().get(key);
    if (id == null) return null;

    return Prefixes.SAMPLE_ID_PREFIX + id;
  }

  public String getMutationId(@NonNull String chromosome, @NonNull String chromosomeStart,
      @NonNull String chromosomeEnd, @NonNull String mutation, @NonNull String mutationType,
      @NonNull String assemblyVersion) {
    val key = TAB.join(chromosome, chromosomeStart, chromosomeEnd, mutation, mutationType, assemblyVersion);
    val id = getMutationIds().get(key);
    if (id == null) return null;

    return Prefixes.MUTATION_ID_PREFIX + id;
  }

  public String getFileId(@NonNull String submittedFileId) {
    val id = getFileIds().get(submittedFileId);
    if (id == null) return null;

    return Prefixes.FILE_ID_PREFIX + id;
  }

  @Override
  public void close() {
    log.info("Closing dbs...");
    closed = true;

    if (getDonorDB() != null) getDonorDB().close();
    if (getSpecimenDB() != null) getSpecimenDB().close();
    if (getSampleDB() != null) getSampleDB().close();
    if (getMutationDB() != null) getMutationDB().close();
    if (getFileDB() != null) getFileDB().close();
    log.info("Closed dbs");
  }

  @SneakyThrows
  private synchronized DB loadEntity(String entity) {
    // TODO: Implement a refresh policy for the file
    {
      if (closed) {
        // Avoid loading on close() access.
        return null;
      }

      val dbFile = getEntityDBFile(entity);
      val lockFile = getEntityDBLockFile(entity);

      // Only one process should be in this section at a time
      log.info("Acquiring lock file: {}...", lockFile);
      new FileMutex(lockFile) {

        @Override
        public void withLock() {
          if (dbFile.exists()) {
            log.info("{} ids available, skipping download.", entity);
            try {
              log.info("Verifying {} db...", entity);

              @Cleanup
              DB db = createEntityDB(entity, true);
              log.info("{} db appears valid", entity);
            } catch (DBException e) {
              log.error("Error verifying existing {} db file {}: {}", entity, dbFile, e.getMessage());

              log.warn("Deleting {}", dbFile);
              dbFile.delete();

              log.warn("Refreshing {}...", dbFile);
              readEntity(entity);
              log.warn("Finished refreshing {}", dbFile);
            }
          } else {
            readEntity(entity);
          }
        }

      };
    }

    return createEntityDB(entity, true);
  }

  @SneakyThrows
  private void readEntity(String entity) {
    log.info("Reading {} ids...", entity);
    val watch = Stopwatch.createStarted();

    @Cleanup
    val db = createEntityDB(entity, false);
    val map = createEntityMap(db, entity);

    @Cleanup
    val export = exportClient.exportEntity(entity);

    String line;
    int count = 0;
    while ((line = export.readLine()) != null) {
      val id = parseId(line);
      val key = parseKey(line);
      map.put(key, id);

      if (++count % 1_000_000 == 0) {
        log.info("Processed {} {}s", formatCount(count), entity);
      }
    }

    log.info("Finished reading {} {} ids in {}", formatCount(count), entity, watch);
  }

  private DB createEntityDB(String entity, boolean readOnly) {
    val file = getEntityDBFile(entity);
    if (readOnly) {
      return DBMaker
          .fileDB(file)
          .concurrencyDisable()
          .fileMmapEnable()
          .readOnly()
          .closeOnJvmShutdown()
          .make();
    } else {
      return DBMaker
          .fileDB(file)
          .concurrencyDisable()
          .fileMmapEnable()
          .make();
    }
  }

  private File getEntityDBFile(String entity) {
    return new File(cacheDir, entity + ".db");
  }

  private File getEntityDBLockFile(String entity) {
    val dbFile = getEntityDBFile(entity);
    return new File(dbFile.getParentFile(), dbFile.getName() + ".lock");
  }

  private static Map<String, Long> createEntityMap(DB db, String name) {
    return db
        .hashMap(name, Serializer.STRING_ASCII, Serializer.LONG)
        .createOrOpen();
  }

  private static Long parseId(String line) {
    val idTab = line.indexOf('\t');
    return Long.valueOf(line.substring(0, idTab));
  }

  private static String parseKey(String line) {
    val idTab = line.indexOf('\t');
    val releaseTab = line.lastIndexOf('\t');
    return releaseTab > idTab ? line.substring(idTab + 1, releaseTab) : line.substring(idTab + 1);
  }

}
