package org.icgc.dcc.id.client.http;

import lombok.val;
import org.icgc.dcc.id.client.core.IdClient;
import org.icgc.dcc.id.client.util.HashIdClient;
import org.junit.Test;

import static com.fasterxml.uuid.Generators.timeBasedGenerator;
import static org.assertj.core.api.Assertions.assertThat;

public class HashIdClientTest {

  private static final String SUBMITTED_ANALYSIS_ID_1 = timeBasedGenerator().generate().toString();
  private static final String SUBMITTED_ANALYSIS_ID_2 = timeBasedGenerator().generate().toString();

  @Test
  public void testCreateRandomAnalysisId() {
    val client = createIdClient();
    val id1 = client.createRandomAnalysisId();
    val id2 = client.createRandomAnalysisId();
    assertThat(id1).isNotEqualTo(id2);
    assertThat(client.getAnalysisId(id1).isPresent()).isTrue();
    assertThat(client.getAnalysisId(id2).isPresent()).isTrue();
  }

  @Test
  public void testCreateAnalysisId() {
    val client = createIdClient();
    assertThat(client.getAnalysisId(SUBMITTED_ANALYSIS_ID_1).isPresent()).isFalse();
    assertThat(client.getAnalysisId(SUBMITTED_ANALYSIS_ID_2).isPresent()).isFalse();
    val id1 = client.createAnalysisId(SUBMITTED_ANALYSIS_ID_1);
    val id2 = client.createAnalysisId(SUBMITTED_ANALYSIS_ID_1);
    val id3 = client.createAnalysisId(SUBMITTED_ANALYSIS_ID_2);
    val id4 = client.createAnalysisId(SUBMITTED_ANALYSIS_ID_1);
    assertThat(client.getAnalysisId(SUBMITTED_ANALYSIS_ID_1).isPresent()).isTrue();
    assertThat(client.getAnalysisId(SUBMITTED_ANALYSIS_ID_2).isPresent()).isTrue();
    assertThat(id1).isEqualTo(SUBMITTED_ANALYSIS_ID_1);
    assertThat(id2).isEqualTo(SUBMITTED_ANALYSIS_ID_1);
    assertThat(id3).isEqualTo(SUBMITTED_ANALYSIS_ID_2);
    assertThat(id4).isEqualTo(SUBMITTED_ANALYSIS_ID_1);
  }

  @Test
  public void testGetNonExistingAnalysisId() {
    val client = createIdClient();
    assertThat(client.getAnalysisId("anything").isPresent()).isFalse();
  }

  private static IdClient createIdClient(){
    return new HashIdClient(true);
  }

}
