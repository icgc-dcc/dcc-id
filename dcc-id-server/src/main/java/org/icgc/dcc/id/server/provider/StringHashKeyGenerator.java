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
package org.icgc.dcc.id.server.provider;

import java.lang.reflect.Method;

import lombok.val;

import org.icgc.dcc.id.server.controller.DonorController;
import org.icgc.dcc.id.server.controller.MutationController;
import org.icgc.dcc.id.server.controller.ProjectController;
import org.icgc.dcc.id.server.controller.SampleController;
import org.icgc.dcc.id.server.controller.SpecimenController;
import org.icgc.dcc.id.server.oauth.RetryTokenServices;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;

public class StringHashKeyGenerator implements KeyGenerator {

  private final KeyGenerator delegate = new SimpleKeyGenerator();

  @Override
  public Object generate(Object target, Method method, Object... params) {
    // Tokens is the most frequently 'hit' cache
    if (target instanceof RetryTokenServices) {
      return delegate.generate(target, method, params);
    }

    if (target instanceof MutationController) {
      return calclulateMutationHash(params);
    }

    if (target instanceof DonorController) {
      return calclulateDonorHash(params);
    }

    if (target instanceof SpecimenController) {
      return calclulateSpecimenHash(params);
    }

    if (target instanceof SampleController) {
      return calclulateSampleHash(params);
    }

    if (target instanceof SampleController) {
      return calclulateSampleHash(params);
    }

    if (target instanceof ProjectController) {
      return calclulateProjectHash(params);
    }

    return delegate.generate(target, method, params);
  }

  private static Object calclulateProjectHash(Object[] params) {
    val projectId = (String) params[0];

    return projectId.hashCode();
  }

  private static Object calclulateSampleHash(Object[] params) {
    val sampleId = (String) params[0];
    val projectId = (String) params[1];

    return (sampleId + projectId).hashCode();
  }

  private static Object calclulateSpecimenHash(Object[] params) {
    val specimenId = (String) params[0];
    val projectId = (String) params[1];

    return (specimenId + projectId).hashCode();
  }

  private static Object calclulateDonorHash(Object[] params) {
    val donorId = (String) params[0];
    val projectId = (String) params[1];

    return (donorId + projectId).hashCode();
  }

  private static Object calclulateMutationHash(Object[] params) {
    val chromosome = (String) params[0];
    val chromosomeStart = (String) params[1];
    val chromosomeEnd = (String) params[2];
    val mutation = (String) params[3];
    val mutationType = (String) params[4];
    val assemblyVersion = (String) params[5];

    return (chromosome + chromosomeStart + chromosomeEnd + mutation + mutationType + assemblyVersion).hashCode();
  }

}
