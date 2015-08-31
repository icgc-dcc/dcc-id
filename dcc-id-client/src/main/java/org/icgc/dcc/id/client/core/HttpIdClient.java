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

import static org.springframework.http.HttpMethod.GET;

import java.util.Optional;

import org.icgc.dcc.id.client.config.ClientProperties;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.NonNull;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpIdClient implements IdClient {

  /**
   * Constants.
   */
  public final static String DONOR_ID_PATH = "/api/donor/id";
  public final static String SPECIMEN_ID_PATH = "/api/specimen/id";
  public final static String SAMPLE_ID_PATH = "/api/sample/id";
  public final static String MUTATION_ID_PATH = "/api/mutation/id";

  /**
   * Configuration.
   */
  private ClientProperties properties;
  private RestTemplate restTemplate;
  private RetryTemplate retryTemplate;

  public HttpIdClient(@NonNull ClientProperties properties) {
    this.properties = properties;
  }

  /**
   * Required for reflection in Loader
   */
  public HttpIdClient(@NonNull String serviceUri, String release) {
    this(ClientProperties.builder()
        .serviceUrl(serviceUri)
        .release(release)
        .build());
  }

  private Optional<String> getResponse(UriComponentsBuilder request) {
    try {
      val response = retryTemplate
          .execute(context -> restTemplate.exchange(request.build().encode().toUri(), GET, null, String.class));
      if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }

      if (response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
        log.warn("Could not get {}", request);
        throw new RuntimeException();
      }

      val entity = response.getBody();

      return Optional.of(entity);
    } catch (Exception e) {
      log.info("Error requesting {}: {}", request, e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<String> getDonorId(@NonNull String submittedDonorId, @NonNull String submittedProjectId) {
    return getDonorId(submittedDonorId, submittedProjectId, false);
  }

  @Override
  @NonNull
  public String createDonorId(String submittedDonorId, String submittedProjectId) {
    return getDonorId(submittedDonorId, submittedProjectId, true).get();
  }

  private Optional<String> getDonorId(@NonNull String submittedDonorId, @NonNull String submittedProjectId,
      boolean create) {
    val request = resource()
        .path(DONOR_ID_PATH)
        .queryParam("submittedDonorId", submittedDonorId)
        .queryParam("submittedProjectId", submittedProjectId)
        .queryParam("create", String.valueOf(create));

    return getResponse(request);
  }

  @Override
  public Optional<String> getSpecimenId(@NonNull String submittedSpecimenId, @NonNull String submittedProjectId) {
    return getSpecimenId(submittedSpecimenId, submittedProjectId, false);
  }

  @Override
  public String createSpecimenId(@NonNull String submittedSpecimenId, @NonNull String submittedProjectId) {
    return getSpecimenId(submittedSpecimenId, submittedProjectId, true).get();
  }

  private Optional<String> getSpecimenId(String submittedSpecimenId, String submittedProjectId, boolean create) {
    val request = resource()
        .path(SPECIMEN_ID_PATH)
        .queryParam("submittedSpecimenId", submittedSpecimenId)
        .queryParam("submittedProjectId", submittedProjectId)
        .queryParam("create", String.valueOf(create));

    return getResponse(request);
  }

  @Override
  public Optional<String> getSampleId(@NonNull String submittedSampleId, @NonNull String submittedProjectId) {
    return getSampleId(submittedSampleId, submittedProjectId, false);
  }

  @Override
  public String createSampleId(@NonNull String submittedSampleId, @NonNull String submittedProjectId) {
    return getSampleId(submittedSampleId, submittedProjectId, true).get();
  }

  private Optional<String> getSampleId(String submittedSampleId, String submittedProjectId, boolean create) {
    val request = resource()
        .path(SAMPLE_ID_PATH)
        .queryParam("submittedSampleId", submittedSampleId)
        .queryParam("submittedProjectId", submittedProjectId)
        .queryParam("create", String.valueOf(create));

    return getResponse(request);
  }

  @Override
  public Optional<String> getMutationId(@NonNull String chromosome, @NonNull String chromosomeStart,
      @NonNull String chromosomeEnd,
      @NonNull String mutation, @NonNull String mutationType, @NonNull String assemblyVersion) {
    return getMutationId(chromosome, chromosomeStart, chromosomeEnd, mutation, mutationType, assemblyVersion, false);
  }

  @Override
  public String createMutationId(@NonNull String chromosome, @NonNull String chromosomeStart,
      @NonNull String chromosomeEnd, @NonNull String mutation,
      @NonNull String mutationType, @NonNull String assemblyVersion) {
    return getMutationId(chromosome, chromosomeStart, chromosomeEnd, mutation, mutationType, assemblyVersion, true)
        .get();
  }

  private Optional<String> getMutationId(String chromosome, String chromosomeStart, String chromosomeEnd,
      String mutation, String mutationType, String assemblyVersion, boolean create) {
    val request = resource()
        .path(MUTATION_ID_PATH)
        .queryParam("chromosome", chromosome)
        .queryParam("chromosomeStart", chromosomeStart)
        .queryParam("chromosomeEnd", chromosomeEnd)
        .queryParam("mutation", mutation)
        .queryParam("mutationType", mutationType)
        .queryParam("assemblyVersion", assemblyVersion)
        .queryParam("create", String.valueOf(create));

    return getResponse(request);
  }

  private UriComponentsBuilder resource() {
    return UriComponentsBuilder
        .fromHttpUrl(properties.getServiceUrl())
        .queryParam("release", properties.getRelease());
  }

}
