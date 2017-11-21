package org.icgc.dcc.id.server.service;

import org.icgc.dcc.id.server.repository.AnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.fasterxml.uuid.Generators.timeBasedGenerator;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Objects.isNull;

@Service
public class AnalysisService {

  private static final int RETRY_LIMIT = 100;

  @Autowired
  private AnalysisRepository analysisRepository;

  /**
   * If the analysisId is null or empty, an unique analysisId is created,
   * otherwise, used.
   * @param analysisId
   * @return analysisId
   */
  public String analysisId(boolean create, String analysisId){
    String id = analysisId;
    if (isNullOrEmpty(id)) {
      checkState(create, "Cannot retrieve an analysisId when create = false and analysisId is null/empty");
      id = createUniqueId();
    }
    return analysisRepository.findId(create, id);
  }


  private String createUniqueId(){
    String id = generateRandomUuid();
    int retryCount = 0;
    while (retryCount < RETRY_LIMIT){
      boolean doesIdExist = !isNull(analysisRepository.findId(false,id ));
      if (!doesIdExist){
        return id;
      }
      retryCount++;
      id = generateRandomUuid();
    }
    throw new IllegalStateException(format("Exceeded max retry count of %s for finding unique analysis id", RETRY_LIMIT));
  }

  private String generateRandomUuid(){
    return timeBasedGenerator().generate().toString();
  }

}
