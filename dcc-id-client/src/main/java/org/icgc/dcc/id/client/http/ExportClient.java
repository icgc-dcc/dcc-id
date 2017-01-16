/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.                             
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

import com.google.common.net.HttpHeaders;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@RequiredArgsConstructor
public class ExportClient {

  /**
   * Constants.
   */
  private static final String DEFAULT_SERVICE_URL = "https://id.icgc.org/";

  /**
   * Configuration.
   */
  private final String serviceUrl;

  public ExportClient() {
    this(DEFAULT_SERVICE_URL);
  }

  @SneakyThrows
  public BufferedReader exportEntity(@NonNull String entity) {
    return new BufferedReader(new InputStreamReader(new GZIPInputStream(readEntityExport(entity))));
  }

  @SneakyThrows
  public void downloadEntityExport(@NonNull String entity, File exportFile) {
    @Cleanup
    val in = readEntityExport(entity);

    Files.copy(in, exportFile.toPath());
  }

  @SneakyThrows
  private InputStream readEntityExport(String entity) {
    val url = new URL(serviceUrl + "/" + entity + "/export");
    val connection = (HttpURLConnection) url.openConnection();
    connection.setRequestProperty(HttpHeaders.ACCEPT_ENCODING, "gzip");

    return connection.getInputStream();
  }

}
