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
package org.icgc.dcc.id.server.service;

import java.io.OutputStream;

import javax.sql.DataSource;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExportService {

  /**
   * Dependencies.
   */
  @Autowired
  DataSource dataSource;

  public void exportProjectIds(OutputStream out) {
    export("project_ids", out);
  }

  public void exportDonorIds(OutputStream out) {
    export("donor_ids", out);
  }

  public void exportSpecimenIds(OutputStream out) {
    export("specimen_ids", out);
  }

  public void exportSampleIds(OutputStream out) {
    export("sample_ids", out);
  }

  public void exportMutationIds(OutputStream out) {
    export("mutation_ids", out);
  }

  public void exportFileIds(OutputStream out) {
    export("file_ids", out);
  }

  @SneakyThrows
  private void export(String tableName, OutputStream out) {
    @Cleanup
    val connection = dataSource.getConnection();

    log.info("Exporting table '{}'", tableName);
    val rows = copy(tableName, connection.unwrap(BaseConnection.class), out);
    out.flush();
    log.info("Finished exporting {} '{}' rows.", tableName, rows);

  }

  @SneakyThrows
  private static long copy(String tableName, BaseConnection connection, OutputStream out) {
    try {
      val copy = new CopyManager(connection);
      return copy.copyOut("COPY " + tableName + " TO STDOUT", out);
    } finally {
      out.flush();
    }
  }

}
