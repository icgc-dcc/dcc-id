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
package org.icgc.dcc.id.client.core;

import static java.lang.String.format;

import java.io.Serializable;

import lombok.NonNull;

import org.icgc.dcc.id.client.http.HttpIdClient;
import org.icgc.dcc.id.client.http.HttpIdClient.Config;
import org.icgc.dcc.id.client.util.HashIdClient;

public class IdClientFactory implements Serializable {

  private static final String HTTP_ID_CLIENT_CLASSNAME = HttpIdClient.class.getName();
  private static final String HASH_ID_CLIENT_CLASSNAME = HashIdClient.class.getName();

  @NonNull
  private final String idClassName;
  @NonNull
  private final Config config;

  /**
   * Creates {@link HashIdClient}
   */
  public IdClientFactory(@NonNull String serviceUri, @NonNull String releaseName) {
    this.idClassName = HASH_ID_CLIENT_CLASSNAME;
    this.config = Config.builder()
        .serviceUrl(serviceUri)
        .release(releaseName)
        .build();
  }

  /**
   * Creates {@link HttpIdClient}
   */
  public IdClientFactory(@NonNull String serviceUri, @NonNull String releaseName, String authToken) {
    this.idClassName = HTTP_ID_CLIENT_CLASSNAME;
    this.config = Config.builder()
        .serviceUrl(serviceUri)
        .release(releaseName)
        .authToken(authToken)
        .build();
  }

  public IdClientFactory(@NonNull String idClassName, @NonNull String serviceUri, @NonNull String releaseName,
      String authToken) {
    this.idClassName = idClassName;
    this.config = Config.builder()
        .serviceUrl(serviceUri)
        .release(releaseName)
        .authToken(authToken)
        .build();
  }

  public IdClientFactory(@NonNull String idClassName, @NonNull Config config) {
    this.idClassName = idClassName;
    this.config = config;
  }

  public IdClient create() {
    if (HTTP_ID_CLIENT_CLASSNAME.equals(idClassName)) {
      return new HttpIdClient(config);
    } else if (HASH_ID_CLIENT_CLASSNAME.equals(idClassName)) {
      return new HashIdClient(config.getServiceUrl(), config.getRelease());
    } else {
      throw new IllegalArgumentException(format("%s client is not supported", idClassName));
    }
  }

}
