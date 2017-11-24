package org.icgc.dcc.id.server.repository;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public abstract class AnalysisRepository {


  @SqlUpdate("INSERT INTO analysis_ids VALUES (:id)")
  public abstract int insertId(@Bind("id") String id);

  @SqlQuery("SELECT id FROM analysis_ids WHERE  id=:id")
  public abstract String getId(@Bind("id") String id);

}
