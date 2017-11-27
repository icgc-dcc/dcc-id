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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.id.core.Prefixes;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.id.core.IdentifierException.checkIdentifier;
import static org.icgc.dcc.id.core.IdentifierException.tryIdentifier;

@NoArgsConstructor(access = PRIVATE)
public final class Ids { // NOPMD
  private static final int MAX_ANALYSIS_ID_CHARS = 512;
  private static final Pattern ANALYSIS_ID_PATTERN =
      compile("^[a-zA-Z0-9]{1}[a-zA-Z0-9-_]{2,"+(MAX_ANALYSIS_ID_CHARS-1)+"}$");

  private static final Map<String, Pattern> PREFIX_PATTERN = ImmutableMap.<String, Pattern> builder()
      .put(compilePattern(Prefixes.DONOR_ID_PREFIX))
      .put(compilePattern(Prefixes.FILE_ID_PREFIX))
      .put(compilePattern(Prefixes.MUTATION_ID_PREFIX))
      .put(compilePattern(Prefixes.PROJECT_ID_PREFIX))
      .put(compilePattern(Prefixes.SAMPLE_ID_PREFIX))
      .put(compilePattern(Prefixes.SPECIMEN_ID_PREFIX))
      .build();

  public static void validateId(@NonNull Optional<String> idOptional, @NonNull String prefix) {
    if (idOptional.isPresent()) {
      val id = idOptional.get(); // NOPMD
      checkIdentifier(PREFIX_PATTERN.containsKey(prefix),"ID Prefix '%s' does not exist", prefix);
      val pattern = PREFIX_PATTERN.get(prefix);
      checkIdentifier(pattern.matcher(id).matches(),"ID '%s' does not match pattern %s", id, pattern);
    }
  }

  public static void validateAnalysisId(@NonNull String id){
    checkIdentifier(ANALYSIS_ID_PATTERN.matcher(id).matches(),
        "ID '%s' does not conform to the analysisId string format: %s",
          id, ANALYSIS_ID_PATTERN.pattern());
  }
  public static void validateAnalysisId(@NonNull Optional<String> idOptional){
    if(idOptional.isPresent()){
      val id = idOptional.get();
      validateAnalysisId(id);
    }
  }

  public static void validateUuid(@NonNull String id){
    tryIdentifier(() -> UUID.fromString(id),IllegalArgumentException.class,
    "ID '%s' does not conform to the string representation of UUID", id );
  }

  public static void validateUuid(@NonNull Optional<String> idOptional){
    if(idOptional.isPresent()){
      val id = idOptional.get();
      validateUuid(id);
    }
  }

  public static String formatId(@NonNull String prefix, @NonNull Long id) { // NOPMD
    return format("%s%s", prefix, id);
  }

  private static Entry<String, Pattern> compilePattern(String prefix) {
    val pattern = compile(format("^%s\\d+$", prefix));
    return Maps.immutableEntry(prefix, pattern);
  }

}
