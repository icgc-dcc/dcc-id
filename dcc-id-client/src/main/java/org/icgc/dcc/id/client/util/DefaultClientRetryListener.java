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
package org.icgc.dcc.id.client.util;

import static com.google.common.base.Throwables.getStackTraceAsString;
import static com.google.common.base.Throwables.propagate;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.net.HttpRetryException;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.icgc.dcc.metadata.core.retry.ClientRetryListener;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@Component
public class DefaultClientRetryListener extends ClientRetryListener {

  @Override
  public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
      Throwable throwable) {
    if (throwable instanceof ResourceAccessException && throwable.getCause() instanceof HttpRetryException) {
      this.retry = false;
      val exception = (HttpRetryException) throwable.getCause();
      if (exception.responseCode() == UNAUTHORIZED.value()) {
        log.error(getStackTraceAsString(throwable));
        throwUnauthorizedException();
      }

      propagate(throwable);
    }
  }

  private void throwUnauthorizedException() {
    throw new HttpClientErrorException(UNAUTHORIZED, "(UNAUTHORIZED). Verify if the access token is valid.");
  }

}
