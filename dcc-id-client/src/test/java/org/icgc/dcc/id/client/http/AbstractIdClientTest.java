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
package org.icgc.dcc.id.client.http;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import lombok.Cleanup;
import lombok.val;
import org.icgc.dcc.id.client.core.IdClient;
import org.icgc.dcc.id.client.http.webclient.WebClientConfig;
import org.icgc.dcc.id.core.ExhaustedRetryException;
import org.icgc.dcc.id.core.IdentifierException;
import org.icgc.dcc.id.util.Ids;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.id.client.http.HttpIdClient.ANALYSIS_ID_PATH;
import static org.icgc.dcc.id.client.http.HttpIdClient.DONOR_ID_PATH;
import static org.icgc.dcc.id.client.http.HttpIdClient.MUTATION_ID_PATH;
import static org.icgc.dcc.id.client.http.HttpIdClient.SAMPLE_ID_PATH;
import static org.icgc.dcc.id.client.http.HttpIdClient.SPECIMEN_ID_PATH;
import static org.icgc.dcc.id.core.Prefixes.DONOR_ID_PREFIX;
import static org.icgc.dcc.id.core.Prefixes.MUTATION_ID_PREFIX;
import static org.icgc.dcc.id.core.Prefixes.SAMPLE_ID_PREFIX;
import static org.icgc.dcc.id.core.Prefixes.SPECIMEN_ID_PREFIX;

public abstract class AbstractIdClientTest {

  private static final int SERVER_PORT = 22223;
  private static final Long RESPONSE_ID = 1000L;
  private static final String SUBMITTED_ANALYSIS_ID = UUID.randomUUID().toString();

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(SERVER_PORT);

  protected abstract IdClient getIdClient();

  @Test
  public void testGetAnalysisId() {
    val requestUrl =
        format("%s?submittedAnalysisId=%s&create=false", ANALYSIS_ID_PATH, SUBMITTED_ANALYSIS_ID);
    configureSuccessfulAnalysisResponse(requestUrl, SUBMITTED_ANALYSIS_ID);

    val response = getIdClient().getAnalysisId(SUBMITTED_ANALYSIS_ID);
    assertThat(response.get()).isEqualTo(SUBMITTED_ANALYSIS_ID);
  }

  @Test
  public void testCreateAnalysisId() {
    val requestUrl =
        format("%s?submittedAnalysisId=%s&create=true", ANALYSIS_ID_PATH, SUBMITTED_ANALYSIS_ID );
    configureSuccessfulAnalysisResponse(requestUrl, SUBMITTED_ANALYSIS_ID);

    val response = getIdClient().createAnalysisId(SUBMITTED_ANALYSIS_ID);
    assertThat(response).isEqualTo(SUBMITTED_ANALYSIS_ID);
  }

  @Test
  public void testGetDonorId() {
    val requestUrl =
        format("%s?submittedDonorId=%s&submittedProjectId=%s&release=ICGC19&create=false", DONOR_ID_PATH, "s1",
            "p1");
    configureSuccessfulResponse(requestUrl, DONOR_ID_PREFIX);

    val response = getIdClient().getDonorId("s1", "p1");
    assertThat(response.get()).isEqualTo(createId(DONOR_ID_PREFIX));
  }

  @Test
  public void testCreateDonorId() {
    val requestUrl =
        format("%s?submittedDonorId=%s&submittedProjectId=%s&release=ICGC19&create=true", DONOR_ID_PATH, "s1",
            "p1");
    configureSuccessfulResponse(requestUrl, DONOR_ID_PREFIX);

    val response = getIdClient().createDonorId("s1", "p1");
    assertThat(response).isEqualTo(createId(DONOR_ID_PREFIX));
  }

  @Test
  public void testGetMutationId() {
    val requestUrl = format("%s?chromosome=%s&chromosomeStart=%s&chromosomeEnd=%s&mutation=%s&mutationType=%s&"
        + "assemblyVersion=%s&release=ICGC19&create=false", MUTATION_ID_PATH, "x", "1", "2", "a_b", "ssm", "1");
    configureSuccessfulResponse(requestUrl, MUTATION_ID_PREFIX);

    val response = getIdClient().getMutationId("x", "1", "2", "a_b", "ssm", "1");
    assertThat(response.get()).isEqualTo(createId(MUTATION_ID_PREFIX));
  }

  @Test
  public void testCreateMutationId() {
    val requestUrl = format("%s?chromosome=%s&chromosomeStart=%s&chromosomeEnd=%s&mutation=%s&mutationType=%s&"
        + "assemblyVersion=%s&release=ICGC19&create=true", MUTATION_ID_PATH, "x", "1", "2", "a_b", "ssm", "1");
    configureSuccessfulResponse(requestUrl, MUTATION_ID_PREFIX);

    val response = getIdClient().createMutationId("x", "1", "2", "a_b", "ssm", "1");
    assertThat(response).isEqualTo(createId(MUTATION_ID_PREFIX));
  }

  @Test
  public void testGetSampleId() {
    val requestUrl =
        format("%s?submittedSampleId=%s&submittedProjectId=%s&release=ICGC19&create=false", SAMPLE_ID_PATH, "s1",
            "p1");
    configureSuccessfulResponse(requestUrl, SAMPLE_ID_PREFIX);

    val response = getIdClient().getSampleId("s1", "p1");
    assertThat(response.get()).isEqualTo(createId(SAMPLE_ID_PREFIX));
  }

  @Test
  public void testCreateSampleId() {
    val requestUrl =
        format("%s?submittedSampleId=%s&submittedProjectId=%s&release=ICGC19&create=true", SAMPLE_ID_PATH, "s1",
            "p1");
    configureSuccessfulResponse(requestUrl, SAMPLE_ID_PREFIX);

    val response = getIdClient().createSampleId("s1", "p1");
    assertThat(response).isEqualTo(createId(SAMPLE_ID_PREFIX));
  }

  @Test
  public void testGetSpecimenId() {
    val requestUrl =
        format("%s?submittedSpecimenId=%s&submittedProjectId=%s&release=ICGC19&create=false", SPECIMEN_ID_PATH, "s1",
            "p1");
    configureSuccessfulResponse(requestUrl, SPECIMEN_ID_PREFIX);

    val response = getIdClient().getSpecimenId("s1", "p1");
    assertThat(response.get()).isEqualTo(createId(SPECIMEN_ID_PREFIX));
  }

  @Test
  public void testCreateSpecimenId() {
    val requestUrl =
        format("%s?submittedSpecimenId=%s&submittedProjectId=%s&release=ICGC19&create=true", SPECIMEN_ID_PATH, "s1",
            "p1");
    configureSuccessfulResponse(requestUrl, SPECIMEN_ID_PREFIX);

    val response = getIdClient().createSpecimenId("s1", "p1");
    assertThat(response).isEqualTo(createId(SPECIMEN_ID_PREFIX));
  }

  @Test
  public void test_404() {
    val response = getIdClient().getDonorId("123", "ALL-US");
    assertThat(response.isPresent()).isFalse();
  }

  @Test
  public void testSocketTimeOut() {

    val requestUrl =
        format("%s?submittedDonorId=%s&submittedProjectId=%s&release=ICGC19&create=false", DONOR_ID_PATH, "s2", "p2");
    stubFor(get(urlEqualTo(requestUrl))
        .willReturn(aResponse()
            .withFixedDelay(70000)
            .withHeader("Content-Type", "text/plain")
            .withBody(createId(""))
            .withStatus(200)));

    try {
      @Cleanup
      val client = new HttpIdClient(createClientConfig(1));
      client.getDonorId("s2", "p2");
    } catch (Exception e) {
    }

    verify(2, getRequestedFor(urlEqualTo(requestUrl)));
  }

  @Test
  public void test_503() {
    val requestUrl =
        format("%s?submittedDonorId=%s&submittedProjectId=%s&release=ICGC19&create=false", DONOR_ID_PATH, "s2", "p2");
    stubFor(get(urlEqualTo(requestUrl))
        .willReturn(aResponse()
            .withStatus(503)));

    try {
      getIdClient().getDonorId("s2", "p2");
    } catch (ExhaustedRetryException e) {
      verify(4, getRequestedFor(urlEqualTo(requestUrl)));
    }
  }

  @Test(expected = IdentifierException.class)
  public void test_500() {
    val requestUrl =
        format("%s?submittedDonorId=%s&submittedProjectId=%s&release=ICGC19&create=false", DONOR_ID_PATH, "s2", "p2");
    stubFor(get(urlEqualTo(requestUrl))
        .willReturn(aResponse()
            .withStatus(500)));

    getIdClient().getDonorId("s2", "p2");
  }

  protected static WebClientConfig createClientConfig(int maxRetries) {
    return WebClientConfig.builder()
        .serviceUrl("http://localhost:" + SERVER_PORT)
        .release("ICGC19")
        .requestLoggingEnabled(true)
        .maxRetries(maxRetries)
        .retryMultiplier(1f)
        .waitBeforeRetrySeconds(1)
        .build();
  }

  private static void configureSuccessfulAnalysisResponse(String requestUrl, String expectedId) {
    val responseModel = aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "text/plain");

    if (!isNullOrEmpty(expectedId)){
      responseModel.withBody(expectedId);
    }
    stubFor(get(urlEqualTo(requestUrl))
        .willReturn(responseModel));
  }
  private static void configureSuccessfulResponse(String requestUrl, String prefix) {
    stubFor(get(urlEqualTo(requestUrl))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/plain")
            .withBody(createId(prefix))));
  }

  private static String createId(String prefix) {
    return Ids.formatId(prefix, RESPONSE_ID);
  }


  @Test
  public void testGetAllDonorIds(){
    val requestUrl = "/donor/export";
    configureSuccessfulResponse4ExportEndpoints(requestUrl, "donor");

    val response = getIdClient().getAllDonorIds();

    String data = response.get();

    Assert.assertEquals(10, Splitter.on('\n').splitToList(data).size());

  }

  @Test
  public void testGetAllSampleIds(){
    val requestUrl = "/sample/export";
    configureSuccessfulResponse4ExportEndpoints(requestUrl, "sample");

    val response = getIdClient().getAllSampleIds();

    String data = response.get();

    Assert.assertEquals(10, Splitter.on('\n').splitToList(data).size());

  }

  @Test
  public void testGetAllSpecimenIds(){
    val requestUrl = "/specimen/export";
    configureSuccessfulResponse4ExportEndpoints(requestUrl, "specimen");

    val response = getIdClient().getAllSpecimenIds();

    String data = response.get();

    Assert.assertEquals(10, Splitter.on('\n').splitToList(data).size());

  }

  @Test
  public void testGetAllMutationIds(){
    val requestUrl = "/mutation/export";
    configureSuccessfulResponse4ExportEndpoints(requestUrl, "mutation");

    val response = getIdClient().getAllMutationIds();

    String data = response.get();

    Assert.assertEquals(10, Splitter.on('\n').splitToList(data).size());

  }


  private static void configureSuccessfulResponse4ExportEndpoints(String requestUrl, String type) {
    stubFor(
        get(urlEqualTo(requestUrl)).willReturn(
            aResponse().withStatus(200).withHeader("Content-Type", "text/plain").withBody(createExportData(type))
        )
    );
  }


  private static String createExportData(String type){

    return
      Joiner.on('\n').join(
          IntStream.range(0, 10).boxed().map(value -> Joiner.on('\t').join(type + "_id_" + value, "project_" + value)).collect(Collectors.toList())
      );

  }

}
