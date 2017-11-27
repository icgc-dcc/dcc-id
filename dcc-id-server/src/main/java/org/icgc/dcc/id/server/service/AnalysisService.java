package org.icgc.dcc.id.server.service;

import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.id.core.IdentifierException;
import org.icgc.dcc.id.server.repository.AnalysisRepository;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.fasterxml.uuid.Generators.timeBasedGenerator;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.icgc.dcc.id.core.IdentifierException.checkIdentifier;
import static org.icgc.dcc.id.server.repository.NotFoundException.checkExistence;
import static org.icgc.dcc.id.util.Ids.validateAnalysisId;
import static org.icgc.dcc.id.util.Ids.validateUuid;

@Service
public class AnalysisService {

  private static final int RETRY_LIMIT = 100;
  private static final int MAX_ATTEMPTS = 1000;

  @Autowired
  private AnalysisRepository analysisRepository;

  /**
   * When create=true, if the submittedAnalysisId is null or empty, an unique submittedAnalysisId is created,
   * otherwise, reused. When create=false and submittedAnalysisId doesnt exist, an IdentifierException is thrown
   * @param submittedAnalysisId
   * @return analysisId
   */
  public String analysisId(boolean create, String submittedAnalysisId){
    String id = submittedAnalysisId;
    if (isNullOrEmpty(id)) {
      checkIdentifier(create, "Cannot retrieve an submittedAnalysisId when create = false and "
          + "submittedAnalysisId is null/empty");
      id = createUniqueId();
    }
    validateAnalysisId(id);
    return findId(create, id);
  }

  /**
   * Check if the input submittedAnalysisId exists. If input submittedAnalysisId is null/empty, or does not exist,
   * returns false. Otherwise returns true
   * @param submittedAnalysisId
   * @return  boolean
   */
  public boolean isExist(@NonNull String submittedAnalysisId){
    validateAnalysisId(submittedAnalysisId);
    return isValidId(submittedAnalysisId) && isValidId(analysisRepository.getId(submittedAnalysisId));
  }

  /**
   * Creates unique random UUID, with collision protection. Ensures within a bounded amount of tries, a unique random
   * UUID is generated
   * @return String representation of a UUID
   */
  private String createUniqueId(){
    String id = generateRandomUuid();
    int retryCount = 0;
    while (retryCount < RETRY_LIMIT){
      if (!isExist(id)){
        validateUuid(id);
        return id;
      }
      retryCount++;
      id = generateRandomUuid();
    }
    throw new IdentifierException(format("Exceeded max retry count of %s for finding unique analysis id", RETRY_LIMIT));
  }

  /**
   * If create=false, returns the requested Id if it exists, other wise returns a 404. If create=true, returns the
   * requestedId if it exists, otherwise creates the id and returns it
   * @param create
   * @param id
   * @return analysisId
   */
  private String findId(boolean create, @NonNull String id) {

    // Try to find the existing key
    String foundId = analysisRepository.getId(id);
    boolean exists = isValidId(foundId);

    if(!create){
      checkExistence(exists, "No id found for business key: %s", id);
      return foundId;
    }

    int attempts = 0;
    while (!exists) {
      // Bound the number of attempts
      attempts++;
      checkState(attempts < MAX_ATTEMPTS,
          "Could not create Id '%s' in %s attempts. Aborting.",
          id, attempts);

      try {
        // Newly discovered key, so CREATE
        val numRowChanged = analysisRepository.insertId(id);
        checkState(numRowChanged == 1, "Only 1 row should be changed when doing an insert, but %s rows changes",
            numRowChanged);
        foundId = id;
      } catch (UnableToExecuteStatementException e) {
        // Most likely a race condition due to concurrent inserts, probably caused by a duplicate
        // key exception. However, there is no definitive way of determining if this is true so we
        // assume it. The thinking is that we will eventually resolve the value within a bounded
        // number of attempts, and if not we throw after crossing the threshold.
        foundId = analysisRepository.getId(id);
      }
      exists = isValidId(foundId);
    }

    return foundId;
  }

  private String generateRandomUuid(){
    return timeBasedGenerator().generate().toString();
  }

  private static boolean isValidId(String id){
    return !isNullOrEmpty(id);
  }

}
