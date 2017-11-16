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

import org.icgc.dcc.id.client.exception.ExportDataNotSupportedException;
import java.io.Closeable;
import java.util.Optional;

public interface IdClient extends Closeable {

  /**
   * Read-only
   */

  Optional<String> getDonorId(String submittedDonorId, String submittedProjectId);

  Optional<String> getSampleId(String submittedSampleId, String submittedProjectId);

  Optional<String> getSpecimenId(String submittedSpecimenId, String submittedProjectId);

  Optional<String> getMutationId(String chromosome, String chromosomeStart, String chromosomeEnd,
      String mutation, String mutationType, String assemblyVersion);

  Optional<String> getFileId(String submittedFileId);

  Optional<String> getObjectId(String analysisId, String fileName);

  Optional<String> getAnalysisId(String submittedAnalysisId);

  /**
   * Create if it doesn't exist
   */

  String createDonorId(String submittedDonorId, String submittedProjectId);

  String createSpecimenId(String submittedSpecimenId, String submittedProjectId);

  String createSampleId(String submittedSampleId, String submittedProjectId);

  String createMutationId(String chromosome, String chromosomeStart, String chromosomeEnd,
      String mutation, String mutationType, String assemblyVersion);

  String createFileId(String submittedFileId);

  String createAnalysisId(String submittedAnalysisId);

  /**
   *  export the whole data from the db table as a string
   */

  default Optional<String> getAllDonorIds(){
    throw new ExportDataNotSupportedException("donor");
  }

  default Optional<String> getAllSampleIds(){
    throw new ExportDataNotSupportedException("sample");
  }

  default Optional<String> getAllSpecimenIds(){
    throw new ExportDataNotSupportedException("specimen");
  }

  default Optional<String> getAllMutationIds() {
    throw new ExportDataNotSupportedException("mutation");
  }

  default Optional<String> getAllFileIds(){
    throw new ExportDataNotSupportedException("file");
  }

  default Optional<String> getAllAnalysisIds() {
    throw new ExportDataNotSupportedException("analysis");
  }

}
