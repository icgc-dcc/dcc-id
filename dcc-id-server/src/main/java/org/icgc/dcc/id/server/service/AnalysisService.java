package org.icgc.dcc.id.server.service;

import org.icgc.dcc.id.server.repository.AnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

@Service
public class AnalysisService {

  private static final int RETRY_LIMIT = 1000;

  @Autowired
  private AnalysisRepository repository;

  /**
   * If the submitterAnalysisId is null or empty, an unique submitterAnalysisId is created,
   * otherwise, used.
   * @param create
   * @param submitterAnalysisId
   * @return analysisId
   */
  public String analysisId(boolean create, String submitterAnalysisId){
    String id = submitterAnalysisId;
    if (isNullOrEmpty(id)) {
      id = createUniqueUuid().toString();
    }
    return repository.findId(create, id);
  }


  private UUID createUniqueUuid(){
    UUID uuid = UUID.randomUUID();
    int retryCount = 0;
    while (retryCount < RETRY_LIMIT){
      if (!repository.doesAnalysisIdExist(uuid.toString())){
        return uuid;
      }
      retryCount++;
    }
    throw new IllegalStateException(format("Exceeded max retry count of %s for finding unique analysis id", RETRY_LIMIT));
  }

}
