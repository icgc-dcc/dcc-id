/*
 * Copyright 2017(c) The Ontario Institute for Cancer Research. All rights reserved.
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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.id.server.config.SecurityConfig.IdCreatable;
import org.icgc.dcc.id.server.service.AnalysisService;
import org.icgc.dcc.id.server.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AnalysisController {

  /**
   * This is to establish a contract for providing analysis/bundle ids in the future
   * by other implementations.
   */
  @NonNull private final AnalysisService analysisService;
  @NonNull private final ExportService exportService;

  @IdCreatable
  @RequestMapping(value = "/id", method = GET)
  public String analysisId(
      // Optional
      @RequestParam(value = "submittedAnalysisId", defaultValue = "") String submittedAnalysisId,
      @RequestParam(value = "create", defaultValue = "true") boolean create) {
    return analysisService.analysisId(create, submittedAnalysisId);
  }

  @RequestMapping(value = "/export", method = GET)
  public void export(HttpServletResponse response) throws IOException {
    response.setContentType("text/tsv");
    exportService.exportAnalysisIds(response.getOutputStream());
  }

}
