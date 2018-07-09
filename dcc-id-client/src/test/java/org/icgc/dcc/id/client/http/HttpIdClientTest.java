package org.icgc.dcc.id.client.http;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.id.client.core.IdClient;
import org.icgc.dcc.id.core.ExhaustedRetryException;
import org.icgc.dcc.id.core.IdentifierException;
import org.junit.Test;

import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.id.client.http.HttpIdClient.DONOR_ID_PATH;

@Slf4j
public class HttpIdClientTest extends  AbstractIdClientTest{
  private final IdClient client = new HttpIdClient(createClientConfig(3));

  @Override
  protected IdClient getIdClient() {
    return client;
  }

  @Test
  public void testClientServerErrors() {
    val submitterDonorId = "d1";
    val submitterProjectId = "p1";
    val requestUrl =
        format("%s?submittedDonorId=%s&submittedProjectId=%s&release=ICGC19&create=false", DONOR_ID_PATH,
            submitterDonorId, submitterProjectId);

    val errorCodes = IntStream.range(400, 512)
        .filter(x -> x < 452 || x >= 500)
        .filter(x -> x != NOT_FOUND.getStatusCode())
        .filter(x -> x != SERVICE_UNAVAILABLE.getStatusCode())
        .boxed()
        .collect(toImmutableSet());

    // Test not found 404
    stubFor(get(urlEqualTo(requestUrl))
        .willReturn(aResponse()
            .withStatus(NOT_FOUND.getStatusCode())));
    val result = getIdClient().getDonorId(submitterDonorId, submitterProjectId);
    assertThat(result).isEmpty();

    // Test Service Unavailabel 503
    stubFor(get(urlEqualTo(requestUrl))
        .willReturn(aResponse()
            .withStatus(SERVICE_UNAVAILABLE.getStatusCode())));
    val throwable = catchThrowable(() ->
        getIdClient().getDonorId(submitterDonorId, submitterProjectId));
    assertThat(throwable).isInstanceOf(ExhaustedRetryException.class);

    // Test all other 4xx and 5xx errors that should return an IdentifierException
    for (val errorCode : errorCodes){
      stubFor(get(urlEqualTo(requestUrl))
          .willReturn(aResponse()
              .withStatus(errorCode)));
      val throwable2 = catchThrowable(() ->
          getIdClient().getDonorId(submitterDonorId, submitterProjectId));
      assertThat(throwable2).isInstanceOf(IdentifierException.class);
    }
  }

}
