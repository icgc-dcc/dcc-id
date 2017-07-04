/*
 * Copyright (c) 2016 The Ontario Institute for Cancer Research. All rights reserved.
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
package org.icgc.dcc.id.client.util;

import java.util.Optional;
import java.util.UUID;

import com.google.common.base.Joiner;
import org.icgc.dcc.common.core.util.UUID5;
import org.icgc.dcc.id.client.core.IdClient;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;

public class CachingIdClient extends ForwardingIdClient {

  /**
   * Caches.
   */
  @NonNull
  private final LoadingCache<Key, Optional<String>> donorIdCache;
  private final LoadingCache<Key, Optional<String>> specimenIdCache;
  private final LoadingCache<Key, Optional<String>> sampleIdCache;
  private final LoadingCache<Key, Optional<String>> fileIdCache;

  public CachingIdClient(IdClient delegate) {
    super(delegate);

    this.donorIdCache =
        createCache(key -> key.isCreate() ? Optional
            .of(delegate.createDonorId(key.getSubmittedId(), key.getSubmittedProjectId())) : delegate
                .getDonorId(key.getSubmittedId(), key.getSubmittedProjectId()));
    this.specimenIdCache =
        createCache(key -> key.isCreate() ? Optional
            .of(delegate.createSpecimenId(key.getSubmittedId(), key.getSubmittedProjectId())) : delegate
                .getSpecimenId(key.getSubmittedId(), key.getSubmittedProjectId()));
    this.sampleIdCache =
        createCache(key -> key.isCreate() ? Optional
            .of(delegate.createSampleId(key.getSubmittedId(), key.getSubmittedProjectId())) : delegate
                .getSampleId(key.getSubmittedId(), key.getSubmittedProjectId()));
    this.fileIdCache =
        createCache(key -> key.isCreate() ? Optional
            .of(delegate.createFileId(key.getSubmittedId())) : delegate
                .getFileId(key.getSubmittedId()));
  }

  //
  // Read-only
  //

  @Override
  @SneakyThrows
  public Optional<String> getDonorId(String submittedDonorId, String submittedProjectId) {
    return donorIdCache.get(new Key(submittedDonorId, submittedProjectId, false));
  }

  @Override
  @SneakyThrows
  public Optional<String> getSpecimenId(String submittedSpecimenId, String submittedProjectId) {
    return specimenIdCache.get(new Key(submittedSpecimenId, submittedProjectId, false));
  }

  @Override
  @SneakyThrows
  public Optional<String> getSampleId(String submittedSampleId, String submittedProjectId) {
    return sampleIdCache.get(new Key(submittedSampleId, submittedProjectId, false));
  }

  @Override
  @SneakyThrows
  public Optional<String> getFileId(String submittedFileId) {
    return fileIdCache.get(new Key(submittedFileId, null, false));
  }

  @Override
  public Optional<String> getObjectId(String analysisId, String fileName) {
    return Optional.of(UUID5.fromUTF8(UUID5.getNamespace(), Joiner.on('/').join(analysisId, fileName)).toString());
  }

  @Override
  public Optional<String> getAnalysisId() {
    return Optional.of(UUID.randomUUID().toString());
  }

  //
  // Read-write
  //

  @Override
  @SneakyThrows
  public String createDonorId(String submittedDonorId, String submittedProjectId) {
    return donorIdCache.get(new Key(submittedDonorId, submittedProjectId, true)).get();
  }

  @Override
  @SneakyThrows
  public String createSpecimenId(String submittedSpecimenId, String submittedProjectId) {
    return specimenIdCache.get(new Key(submittedSpecimenId, submittedProjectId, true)).get();
  }

  @Override
  @SneakyThrows
  public String createSampleId(String submittedSampleId, String submittedProjectId) {
    return sampleIdCache.get(new Key(submittedSampleId, submittedProjectId, true)).get();
  }

  @Override
  @SneakyThrows
  public String createFileId(String submittedFileId) {
    return fileIdCache.get(new Key(submittedFileId, null, true)).get();
  }

  //
  // Helpers
  //

  private static LoadingCache<Key, Optional<String>> createCache(Function<Key, Optional<String>> loader) {
    return CacheBuilder.newBuilder().build(CacheLoader.from(loader));
  }

  /**
   * Cache key
   */
  @Value
  private static class Key {

    String submittedId;
    String submittedProjectId;
    boolean create;

  }

}
