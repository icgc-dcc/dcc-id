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

import java.util.Arrays;
import java.util.List;

import lombok.Cleanup;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.icgc.dcc.id.client.core.IdClient;
import org.icgc.dcc.id.client.core.IdClientFactory;
import org.icgc.dcc.id.client.http.webclient.WebClientConfig;
import org.junit.Ignore;
import org.junit.Test;

@Slf4j
@Ignore
public class HttpIdClientIntegrationTest {

  private static final List<String> DONOR_IDS = Arrays.asList(
      "4174884",
      "4119279",
      "4120403",
      "4133263",
      "4166503",
      "4110378",
      "4115001",
      "4145177",
      "4149246",
      "4157186",
      "4188879",
      "4134434",
      "4160100",
      "4136702",
      "4142605",
      "4178655",
      "4189035",
      "4150895",
      "4113825",
      "4177406",
      "4178310",
      "4124188",
      "4138527",
      "4147968",
      "4103141",
      "4108992",
      "4111326",
      "4178345",
      "4111337",
      "4113191",
      "4124542",
      "4131095",
      "4144633",
      "4120157",
      "4144951",
      "4145528",
      "4110996",
      "4138885",
      "4147081",
      "4161781",
      "4165379",
      "4128477",
      "4166151",
      "4177844",
      "4193638",
      "4196670",
      "4199714",
      "4184011",
      "4181460",
      "4199996",
      "4107559",
      "4190784",
      "4145056",
      "4188398",
      "4140531",
      "4187640",
      "4130194",
      "4122063",
      "4128852",
      "4158268",
      "4103141",
      "4108992",
      "4111326",
      "4178345",
      "4111337",
      "4113191",
      "4124542",
      "4131095",
      "4144633",
      "4120157",
      "4144951",
      "4145528",
      "4110996",
      "4138885",
      "4147081",
      "4161781",
      "4165379",
      "4128477",
      "4166151",
      "4177844",
      "4193638",
      "4196670",
      "4199714",
      "4184011",
      "4181460",
      "4199996",
      "4107559",
      "4190784",
      "4145056",
      "4188398",
      "4140531",
      "4187640",
      "4130194",
      "4122063",
      "4128852",
      "4158268",
      "4109956"
      );

  private static final String PROJECT_ID = "***REMOVED***";

  @Test
  public void testClient() throws Exception {
    @Cleanup
    val client = new HttpIdClient(WebClientConfig.builder()
        .release("1")
        .serviceUrl("https://localhost:8443")
        .authToken(System.getProperty("authToken"))
        .strictSSLCertificates(false)
        .build());

    val submittedFileId = "1";
    val fileId1 = client.createFileId(submittedFileId);
    log.info("fileId1: {}", fileId1);
    val fileId2 = client.createFileId(submittedFileId);
    log.info("fileId2: {}", fileId2);
  }

  @Test
  public void testClientPerformance() {
    val url = "***REMOVED***";
    val release = "ICGC19";
    val authToken = "zzz";
    val client = new IdClientFactory(url, release, authToken).create();

    try {
      for (int i = 0; i < 10; i++) {
        query(client);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void query(IdClient idClient) {
    for (val donorId : DONOR_IDS) {
      idClient.getDonorId(donorId, PROJECT_ID);
    }
  }

}
