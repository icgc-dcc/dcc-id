package org.icgc.dcc.id.client.http;

import com.github.tomakehurst.wiremock.http.Fault;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.id.client.core.IdClient;
import org.icgc.dcc.id.client.http.webclient.WebClientConfig;
import org.icgc.dcc.id.core.ExhaustedRetryException;
import org.icgc.dcc.id.core.IdentifierException;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.id.client.http.HttpIdClient.DONOR_ID_PATH;
import static org.icgc.dcc.id.client.http.HttpIdClient.getResponse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

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
            .withFault(Fault.MALFORMED_RESPONSE_CHUNK)
            .withStatus(NOT_FOUND.getStatusCode()))
    );
    stubFor(get(urlEqualTo(requestUrl))
        .willReturn(aResponse()
            .withStatus(SERVICE_UNAVAILABLE.getStatusCode()) )
    );
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

  /**
   * Verifies overture-stack/SONG#322 (https://github.com/overture-stack/SONG/issues/322) is becuase
   * IdClient wasnt configured with a long enough retry.
   */
  @Test
  public void testRetryClientHandlerException(){
    val numberOfRetries = 3;

    // Setup HttpIDClient to use a WebResource Spy
    val client = new HttpIdClient(WebClientConfig.builder()
        .release("")
        .maxRetries(numberOfRetries)
        .serviceUrl("https://www.google.com")
        .authToken("sdfsdf")
        .build());
    val resource = (WebResource)Whitebox.getInternalState(client, "resource");
    val spyWebResource = spy(resource);
    when(spyWebResource.path(anyString())).thenReturn(spyWebResource);
    when(spyWebResource.queryParam(anyString(), anyString())).thenReturn(spyWebResource);
    Whitebox.setInternalState(client, "resource", spyWebResource);

    //WebResource throws a registered Retryable exception wrapped by a ClientHandlerException,
    // and HttpIdClient throws ExhaustedRetryException
    val protocolException = new ProtocolException("something");
    val clientHandlerException = new ClientHandlerException(protocolException);
    when(spyWebResource.get(ClientResponse.class)).thenThrow(clientHandlerException);
    val throwable = catchThrowable(() -> client.getObjectId("somethign", "domss"));
    assertThat(throwable).isInstanceOf(ExhaustedRetryException.class);
    // mockito magically adds another call. Print statements confirmed
    verify(spyWebResource, times((numberOfRetries+1)+1)).get(ClientResponse.class);

    // WebResource throws any exception wrapped by ClientHandlerException but not a registered Retryable exception,
    // and HttpIdClient throws ClientHandlerException
    val randomException = new Exception("Something random");
    val clientHandlerException2 = new ClientHandlerException(randomException);
    reset(spyWebResource);
    when(spyWebResource.path(anyString())).thenReturn(spyWebResource);
    when(spyWebResource.queryParam(anyString(), anyString())).thenReturn(spyWebResource);
    when(spyWebResource.get(ClientResponse.class)).thenThrow(clientHandlerException2);
    val throwable2 = catchThrowable(() -> client.getObjectId("somethign", "domss"));
    assertThat(throwable2).isInstanceOf(ClientHandlerException.class);
    // mockito magically adds another call. Print statements confirmed
    verify(spyWebResource, times(2)).get(ClientResponse.class);

    //WebResource throws a registered Retryable IOException wrapped by a ClientHandlerException,
    // and HttpIdClient throws ExhaustedRetryException
    val randomIOException = new IOException("Something random IO");
    val clientHandlerExceptionIO = new ClientHandlerException(randomIOException);
    reset(spyWebResource);
    when(spyWebResource.path(anyString())).thenReturn(spyWebResource);
    when(spyWebResource.queryParam(anyString(), anyString())).thenReturn(spyWebResource);
    when(spyWebResource.get(ClientResponse.class)).thenThrow(clientHandlerExceptionIO);
    val throwableIO = catchThrowable(() -> client.getObjectId("somethign", "domss"));
    assertThat(throwableIO).isInstanceOf(ExhaustedRetryException.class);
    // mockito magically adds another call. Print statements confirmed
    verify(spyWebResource, times((numberOfRetries+1)+1)).get(ClientResponse.class);

    // WebResource throws any other Exception other than ClientHandlerException or custom exceptions,
    // and HttpIdClient throws IdentifierException
    val randomException2 = new RuntimeException("Something random");
    reset(spyWebResource);
    when(spyWebResource.path(anyString())).thenReturn(spyWebResource);
    when(spyWebResource.queryParam(anyString(), anyString())).thenReturn(spyWebResource);
    when(spyWebResource.get(ClientResponse.class)).thenThrow(randomException2);
    val throwable3 = catchThrowable(() -> client.getObjectId("somethign", "domss"));
    assertThat(throwable3).isInstanceOf(IdentifierException.class);
    // mockito magically adds another call. Print statements confirmed
    verify(spyWebResource, times(2)).get(ClientResponse.class);
  }

  @Test
  public void testRetryServiceUnavailable(){
    val scenarioName = "serviceUnavailableScenario";
    val endpoint = "/something";
    val expectedResponse = "someId123";
    val numberOfRetries = 3;

    // Model Stateful Behaviour
    wireMockRule.resetMappings();
    wireMockRule.resetRequests();
    wireMockRule.stubFor(get(urlEqualTo(endpoint))
        .inScenario(scenarioName)
        .whenScenarioStateIs(STARTED)
        .willReturn(aResponse()
            .withStatus(SERVICE_UNAVAILABLE.getStatusCode()))
    );

    wireMockRule.stubFor(get(urlEqualTo(endpoint))
        .inScenario(scenarioName)
        .whenScenarioStateIs(STARTED)
        .willReturn(aResponse()
            .withStatus(SERVICE_UNAVAILABLE.getStatusCode()))
        .willSetStateTo("error_1")
    );

    wireMockRule.stubFor(get(urlEqualTo(endpoint))
        .inScenario(scenarioName)
        .whenScenarioStateIs("error_1")
        .willReturn(aResponse()
            .withStatus(SERVICE_UNAVAILABLE.getStatusCode()))
        .willSetStateTo("error_2")
    );

    wireMockRule.stubFor(get(urlEqualTo(endpoint))
        .inScenario(scenarioName)
        .whenScenarioStateIs("error_2")
        .willReturn(aResponse()
            .withBody(expectedResponse)
            .withStatus(OK.getStatusCode()))
        .willSetStateTo("success")
    );

    // Setup HttpIDClient to use a WebResource Spy
    val config = WebClientConfig.builder()
        .release("")
        .maxRetries(numberOfRetries)
        .serviceUrl("http://localhost:"+wireMockRule.port())
        .authToken("sdfsdf")
        .build();
    val client = new HttpIdClient(config);
    val resource = (WebResource)Whitebox.getInternalState(client, "resource");

    // Initialize and setup spy
    val retryContext = RetryContext.create(config);
    val request = resource.path(endpoint);
    val spyRequest = spy(request);

    // Run
    val response = getResponse( spyRequest, retryContext);

    // Verify
    verify(spyRequest, times(numberOfRetries)).get(ClientResponse.class);
    assertThat(response).isNotEmpty();
    assertThat(response.get()).isEqualTo(expectedResponse);
  }

}
