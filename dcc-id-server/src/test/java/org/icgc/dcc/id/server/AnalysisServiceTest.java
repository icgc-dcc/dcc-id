package org.icgc.dcc.id.server;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.id.core.IdentifierException;
import org.icgc.dcc.id.server.repository.NotFoundException;
import org.icgc.dcc.id.server.service.AnalysisService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static com.fasterxml.uuid.Generators.timeBasedGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.util.Sets.newHashSet;
import static org.icgc.dcc.id.util.Ids.validateUuid;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"test", "development"})
public class AnalysisServiceTest {

  @Autowired
  private AnalysisService service;
  private final Set<String> testIds = newHashSet();

  @Test
  public void testNonExistingId(){
    val id = getUniqueRandomId();
    assertThat(service.isExist(id)).isFalse();
  }

  @Test
  public void testCreateId(){
    val submittedAnalysisId = getUniqueRandomId();
    val analysisId =  service.analysisId(true, submittedAnalysisId);
    assertThat(analysisId).isEqualTo(submittedAnalysisId);
    val analysisId2 = service.analysisId(false, submittedAnalysisId);
    assertThat(analysisId2).isEqualTo(submittedAnalysisId);
    assertThat(service.isExist(submittedAnalysisId)).isTrue();
  }

  @Test
  public void testGetNonExistentId(){
    val submittedAnalysisId = getUniqueRandomId();
    assertThatExceptionOfType(NotFoundException.class).isThrownBy(
        () -> service.analysisId(false, submittedAnalysisId)
    );
  }

  @Test
  public void testGetEmptyId(){
    val submittedAnalysisId = "";
    assertThatExceptionOfType(IdentifierException.class).isThrownBy(
        () -> service.analysisId(false, submittedAnalysisId)
    );
  }

  @Test
  public void testGetNullId(){
    String submittedAnalysisId = null;
    assertThatExceptionOfType(IdentifierException.class).isThrownBy(
        () -> service.analysisId(false, submittedAnalysisId)
    );
  }

  @Test
  public void testCreateRandomOnNullId(){
    String submittedAnalysisId = null;
    val analysisId = service.analysisId(true, submittedAnalysisId);
    assertThat(testIds).doesNotContain(analysisId);
    validateUuid(analysisId);
  }

  @Test
  public void testCreateRandomOnEmptyId(){
    val submittedAnalysisId = "";
    val analysisId = service.analysisId(true, submittedAnalysisId);
    assertThat(testIds).doesNotContain(analysisId);
    validateUuid(analysisId);
  }

  private String getUniqueRandomId(){
    while(true){
      String id = timeBasedGenerator().generate().toString();
      if(!testIds.contains(id)){
        testIds.add(id);
        return id;
      }
    }
  }

}
