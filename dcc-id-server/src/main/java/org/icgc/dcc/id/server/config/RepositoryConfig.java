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
package org.icgc.dcc.id.server.config;

import javax.sql.DataSource;

import org.icgc.dcc.id.server.repository.DonorRepository;
import org.icgc.dcc.id.server.repository.FileRepository;
import org.icgc.dcc.id.server.repository.MutationRepository;
import org.icgc.dcc.id.server.repository.ProjectRepository;
import org.icgc.dcc.id.server.repository.SampleRepository;
import org.icgc.dcc.id.server.repository.SpecimenRepository;
import org.skife.jdbi.v2.DBI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Data access layer configuration.
 */
@Configuration
public class RepositoryConfig {

  @Autowired
  private DataSource dataSource;

  @Bean
  public DBI dbi() {
    return new DBI(dataSource);
  }

  @Bean
  public ProjectRepository projectRepository() {
    return dbi().open(ProjectRepository.class);
  }

  @Bean
  public DonorRepository donorRepository() {
    return dbi().open(DonorRepository.class);
  }

  @Bean
  public SpecimenRepository specimenRepository() {
    return dbi().open(SpecimenRepository.class);
  }

  @Bean
  public SampleRepository sampleRepository() {
    return dbi().open(SampleRepository.class);
  }

  @Bean
  public MutationRepository mutationRepository() {
    return dbi().open(MutationRepository.class);
  }

  @Bean
  public FileRepository fileRepository() {
    return dbi().open(FileRepository.class);
  }

}
