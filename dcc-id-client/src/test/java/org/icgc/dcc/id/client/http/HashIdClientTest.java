package org.icgc.dcc.id.client.http;

import lombok.val;
import org.icgc.dcc.id.client.core.IdClient;
import org.icgc.dcc.id.client.util.HashIdClient;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class HashIdClientTest {

  private static IdClient createIdClient(){
    return new HashIdClient(true);
  }

  private static final String SUBMITTED_ANALYSIS_ID = UUID.randomUUID().toString();


  @Test
  public void testGetAnalysisId() {
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
    assertThat(client.getAnalysisId(SUBMITTED_ANALYSIS_ID).isPresent()).isFalse();
    val id1 = client.createAnalysisId(SUBMITTED_ANALYSIS_ID);
    val id2 = client.createAnalysisId(SUBMITTED_ANALYSIS_ID);
    assertThat(id1).isEqualTo(id2);
    assertThat(client.getAnalysisId(id2).isPresent()).isTrue();
  }

}
