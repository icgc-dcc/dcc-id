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
package org.icgc.dcc.id.util;

import static java.lang.String.format;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;

import lombok.NoArgsConstructor;
import lombok.val;

import org.icgc.dcc.id.core.IdentifierException;
import org.icgc.dcc.id.core.Prefixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@NoArgsConstructor
public final class Ids {

  private static final Map<String, Pattern> PREFIX_PATTERN = ImmutableMap.<String, Pattern> builder()
      .put(compilePattern(Prefixes.DONOR_ID_PREFIX))
      .put(compilePattern(Prefixes.FILE_ID_PREFIX))
      .put(compilePattern(Prefixes.MUTATION_ID_PREFIX))
      .put(compilePattern(Prefixes.PROJECT_ID_PREFIX))
      .put(compilePattern(Prefixes.SAMPLE_ID_PREFIX))
      .put(compilePattern(Prefixes.SPECIMEN_ID_PREFIX))
      .build();

  public static void validateId(Optional<String> idOptional, String prefix) {
    if (idOptional.isPresent()) {
      val id = idOptional.get();
      val pattern = PREFIX_PATTERN.get(prefix);
      if (!pattern.matcher(id).matches()) {
        throw new IdentifierException(format("ID '%s' does not match pattern %s", id, pattern));
      }
    }
  }

  public static String formatId(String prefix, Long id) {
    return format("%s%s", prefix, id);
  }

  private static Entry<String, Pattern> compilePattern(String prefix) {
    val pattern = Pattern.compile(format("^%s\\d+$", prefix));

    return Maps.immutableEntry(prefix, pattern);
  }

}
