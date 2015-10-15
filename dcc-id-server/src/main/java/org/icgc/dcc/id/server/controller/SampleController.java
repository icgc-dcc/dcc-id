/*
 * Copyright 2013(c) The Ontario Institute for Cancer Research. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public
 * License v3.0. You should have received a copy of the GNU General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.id.server.controller;

import static org.icgc.dcc.id.server.config.SecurityConfig.AUTHORIZATION_EXPRESSION;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.icgc.dcc.id.server.repository.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sample")
@RequiredArgsConstructor(onConstructor = @__(@Autowired) )
public class SampleController {

  /**
   * Dependencies
   */
  @NonNull
  private final SampleRepository repository;

  @PreAuthorize(AUTHORIZATION_EXPRESSION)
  @Cacheable(value = "sampleIds", keyGenerator = "keyGenerator")
  @RequestMapping(value = "/id", method = GET)
  public String sampleId(
      // Required
      @RequestParam("submittedSampleId") String submittedSampleId,
      @RequestParam("submittedProjectId") String submittedProjectId,
      // Optional
      @RequestParam(value = "release", defaultValue = "unknown") String release,
      @RequestParam(value = "create", defaultValue = "false") boolean create) {
    return repository.findId(create, submittedSampleId, submittedProjectId, release);
  }

}
