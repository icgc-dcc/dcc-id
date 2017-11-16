package org.icgc.dcc.id.server.repository;

import lombok.NonNull;
import lombok.val;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

public abstract class AnalysisRepository {

  private static final int MAX_ATTEMPTS = 5;

  @SqlUpdate("INSERT INTO analysis_ids VALUES (:id)")
  abstract int insertId(@Bind("id") String id);

  @SqlQuery("SELECT id FROM analysis_ids WHERE  id=:id")
  abstract String getId(@Bind("id") String id);

  public String findId(boolean create, @NonNull String id) {
    // Try to find the existing key
    String foundId = getId(id);
    if(!create){
      return foundId;
    }

    int attempts = 0;
    while (true) {
      boolean exists = !isNull(foundId);
      if (exists) {
        break;
      }

      // Bound the number of attempts
      attempts++;
      checkState(attempts < MAX_ATTEMPTS,
          "Could not create Id '%s' in %s attempts. Aborting.",
          id, attempts);

      try {
        // Newly discovered key, so CREATE
        val numRowChanged = insertId(id);
        checkState(numRowChanged == 1, "Only 1 row should be changed when doing an insert, but %s rows changes",
            numRowChanged);
        foundId = id;
      } catch (UnableToExecuteStatementException e) {
        // Most likely a race condition due to concurrent inserts, probably caused by a duplicate
        // key exception. However, there is no definitive way of determining if this is true so we
        // assume it. The thinking is that we will eventually resolve the value within a bounded
        // number of attempts, and if not we throw after crossing the threshold.
        foundId = getId(id);
      }
    }
    return foundId;
  }

}
