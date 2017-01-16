package org.icgc.dcc.id.client.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.util.Formats.formatCount;
import static org.icgc.dcc.common.core.util.Formats.formatRate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.icgc.dcc.id.core.Prefixes;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Stopwatch;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Ignore("For development only. This takes a while first time through since it downloads a lot of data")
public class ExportIdCacheTest {

  @Test
  public void testGetIds() {
    @Cleanup
    val cache = new ExportIdCache();

    val donorId = cache.getDonorId("THCA-SA", "PTC_279");
    assertThat(donorId).isEqualTo("DO227967");
    System.out.println(donorId);

    val specimenId = cache.getSpecimenId("LUSC-KR", "J30_T");
    assertThat(specimenId).isEqualTo("SP133670");
    System.out.println(specimenId);

    val sampleId = cache.getSampleId("THCA-SA", "PTC_279");
    assertThat(sampleId).isEqualTo("SA594536");
    System.out.println(sampleId);

    val fileId = cache.getFileId("3779df53-48bc-5340-9cf7-663cb5d48065");
    assertThat(fileId).isEqualTo("FI672813");
    System.out.println(fileId);

    val mutationId = cache.getMutationId("6", "87067050", "87067050", "C>G", "single base substitution", "GRCh37");
    assertThat(mutationId).isEqualTo("MU483749");
    System.out.println(mutationId);
  }

  @Test
  @SneakyThrows
  public void testPerformance() {
    val entity = "mutation";

    @Cleanup
    val cache = new ExportIdCache();

    val exportFile = new File("/tmp/mutation.tsv.gz");
    if (!exportFile.exists()) {
      log.info("Downloading {}...", exportFile);
      val exportClient = new ExportClient();
      exportClient.downloadEntityExport(entity, exportFile);
    }

    @Cleanup
    val export = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(exportFile))));

    log.info("Testing...");
    val watch = Stopwatch.createStarted();
    String line;
    int count = 0;
    while ((line = export.readLine()) != null) {
      String[] values = line.split("\\t");

      int i = 0;
      val expectedId = Prefixes.MUTATION_ID_PREFIX + values[i++];
      val actualId = cache.getMutationId(values[i++], values[i++], values[i++], values[i++], values[i++], values[i++]);

      assertThat(actualId).isEqualTo(expectedId);

      if (++count % 100_000 == 0) {
        log.info("Looked up {} {}s ({} ids/s)", formatCount(count), entity, formatRate(count, watch));
      }
    }

    log.info("Finished looking up {} mutations in {}", formatCount(count), watch);
  }

  @Test
  @SneakyThrows
  public void testBaselinePerformance() {
    val entity = "mutation";

    @Cleanup
    val client = new HttpIdClient("http://<internal-ip-of-varnish>:5391", "", null);

    val exportFile = new File("/tmp/mutation.tsv.gz");
    if (!exportFile.exists()) {
      log.info("Downloading {}...", exportFile);
      val exportClient = new ExportClient();
      exportClient.downloadEntityExport(entity, exportFile);
    }

    @Cleanup
    val export = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(exportFile))));

    log.info("Testing...");
    val watch = Stopwatch.createStarted();
    String line;
    int count = 0;
    while ((line = export.readLine()) != null) {
      String[] values = line.split("\\t");

      int i = 0;
      val expectedId = Prefixes.MUTATION_ID_PREFIX + values[i++];
      val actualId =
          client.getMutationId(values[i++], values[i++], values[i++], values[i++], values[i++], values[i++]).get();

      assertThat(actualId).isEqualTo(expectedId);

      if (++count % 1_000 == 0) {
        log.info("Looked up {} {}s ({} ids/s)", formatCount(count), entity, formatRate(count, watch));
      }
    }

    log.info("Finished looking up {} mutations in {}", formatCount(count), watch);
  }

}
