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

import org.icgc.dcc.id.client.http.webclient.WebClientConfig;

import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
@Builder
public final class RetryContext {

  int attempts;
  int sleepSeconds;
  float multiplier;
  boolean retry;

  public static RetryContext create(WebClientConfig clientConfig) {
    return RetryContext.builder()
        .attempts(clientConfig.getMaxRetries())
        .sleepSeconds(clientConfig.getWaitBeforeRetrySeconds())
        .multiplier(clientConfig.getRetryMultiplier())
        .retry(clientConfig.getMaxRetries() > 0)
        .build();
  }

  public static RetryContext next(RetryContext previousContext) {
    return RetryContext.builder()
        .retry(previousContext.attempts > 1)
        .attempts(previousContext.attempts - 1)
        .sleepSeconds((int) (previousContext.sleepSeconds * previousContext.multiplier))
        .multiplier(previousContext.multiplier)
        .build();
  }

  @SneakyThrows
  public static RetryContext waitBeforeRetry(RetryContext retryContext) {
    log.info("Service Unavailable. Waiting for {} seconds before retry...", retryContext.getSleepSeconds());
    log.info("{}", retryContext);
    Thread.sleep(retryContext.getSleepSeconds() * 1000);

    return next(retryContext);
  }

}