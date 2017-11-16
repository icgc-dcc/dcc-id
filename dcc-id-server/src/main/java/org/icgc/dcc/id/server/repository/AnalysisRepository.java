package org.icgc.dcc.id.server.repository;

import lombok.NonNull;
import lombok.val;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import static java.util.Objects.isNull;

public abstract class AnalysisRepository extends BaseRepository {


  public String findId(boolean create, @NonNull String submitterAnalysisId){
    return super.findId(create, submitterAnalysisId);
  }


  public boolean doesAnalysisIdExist(@NonNull String submitterAnalysisId){
    val result = getAnalysisId(submitterAnalysisId);
    return !isNull(result) && result > -1;
  }

  /**
   * JDBI instrumented.
   */
  @SqlQuery("SELECT id FROM analysis_ids WHERE analysis_id=:analysis_id")
  abstract Long getAnalysisId( @Bind("analysis_id") String analysis_id);

  /**
   * JDBI instrumented.
   */
  @SqlUpdate("INSERT INTO analysis_ids (analysis_id) VALUES (:analysis_id)")
  abstract Long createAnalysisId(@Bind("analysis_id") String analysis_id);

  @Override Long getId(String... keys) {
    return getAnalysisId(
        keys[0] /* analysis_id */
    );
  }

  @Override Long insertId(String... keys) {
    return createAnalysisId(
        keys[0] /* analysis_id */
    );
  }

  @Override String getPrefix() {
    return "";
  }

}
