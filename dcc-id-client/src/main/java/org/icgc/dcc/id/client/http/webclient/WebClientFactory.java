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
package org.icgc.dcc.id.client.http.webclient;

import static com.sun.jersey.api.json.JSONConfiguration.FEATURE_POJO_MAPPING;
import static java.lang.Boolean.TRUE;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.icgc.dcc.common.core.security.DumbX509TrustManager;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.ApacheHttpClientHandler;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

@Slf4j
@NoArgsConstructor
public final class WebClientFactory {

  @SneakyThrows
  public static Client createClient(WebClientConfig config) {
    val connectionManager = new SimpleHttpConnectionManager();

    connectionManager.getParams().setConnectionTimeout(30000);
    connectionManager.getParams().setSoTimeout(60000);
    connectionManager.getParams().setDefaultMaxConnectionsPerHost(10);
    connectionManager.getParams().setStaleCheckingEnabled(false);

    val httpClient = new HttpClient(connectionManager);
    val clientHandler = new ApacheHttpClientHandler(httpClient);
    val root = new ApacheHttpClient(clientHandler);
    val clientConfig = new DefaultApacheHttpClientConfig();

    if (!config.isStrictSSLCertificates()) {
      log.debug("Setting up SSL context");
      val context = SSLContext.getInstance("TLS");
      context.init(null, new TrustManager[] { new DumbX509TrustManager() }, null);
      SSLContext.setDefault(context);
    }

    clientConfig.getFeatures().put(FEATURE_POJO_MAPPING, TRUE);
    clientConfig.getClasses().add(JacksonJsonProvider.class);

    val client = new Client(root, clientConfig);

    if (config.getAuthToken() != null) {
      client.addFilter(oauth2Filter(config));
    }

    if (config.isRequestLoggingEnabled()) {
      client.addFilter(new LoggingFilter());
    }

    return client;
  }

  private static ClientFilter oauth2Filter(WebClientConfig config) {
    val value = "Bearer " + config.getAuthToken();
    return new ClientFilter() {

      @Override
      public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        val headers = request.getHeaders();
        headers.putSingle(AUTHORIZATION, value);

        return getNext().handle(request);
      }

    };
  }

}
