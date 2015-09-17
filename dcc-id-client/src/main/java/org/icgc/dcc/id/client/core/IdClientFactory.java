/*
 * Copyright (c) 2015 The Ontario Institute for Cancer Research. All rights reserved.                             
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
import org.icgc.dcc.id.client.util.HashIdClient;

public class IdClientFactory implements Serializable {

  private static final String HTTP_ID_CLIENT_CLASSNAME = HttpIdClient.class.getName();
  private static final String HASH_ID_CLIENT_CLASSNAME = HashIdClient.class.getName();

  @NonNull
  private final String idClassName;
  @NonNull
  private final String serviceUri;
  @NonNull
  private final String releaseName;
  private final String authToken;

  /**
   * Creates {@link HashIdClient}
   */
  public IdClientFactory(String serviceUri, String releaseName) {
    this.idClassName = HASH_ID_CLIENT_CLASSNAME;
    this.serviceUri = serviceUri;
    this.releaseName = releaseName;
    this.authToken = null;
  }

  /**
   * Creates {@link HttpIdClient}
   */
  public IdClientFactory(String serviceUri, String releaseName, String authToken) {
    this.idClassName = HTTP_ID_CLIENT_CLASSNAME;
    this.serviceUri = serviceUri;
    this.releaseName = releaseName;
    this.authToken = authToken;
  }

  public IdClientFactory(String idClassName, String serviceUri, String releaseName, String authToken) {
    this.idClassName = idClassName;
    this.serviceUri = serviceUri;
    this.releaseName = releaseName;
    this.authToken = authToken;
  }

  public IdClient create() {
    if (HTTP_ID_CLIENT_CLASSNAME.equals(idClassName)) {
      return new HttpIdClient(serviceUri, releaseName, authToken);
    } else if (HASH_ID_CLIENT_CLASSNAME.equals(idClassName)) {
      return new HashIdClient(serviceUri, releaseName);
    } else {
      throw new IllegalArgumentException(format("%s client is not supported", idClassName));
    }
  }

}
