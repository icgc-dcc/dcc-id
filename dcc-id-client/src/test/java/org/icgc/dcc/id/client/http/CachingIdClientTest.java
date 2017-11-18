package org.icgc.dcc.id.client.http;

import org.icgc.dcc.id.client.core.IdClient;
import org.icgc.dcc.id.client.util.CachingIdClient;
import org.junit.Ignore;
import org.junit.Test;

public class CachingIdClientTest extends  AbstractIdClientTest{

  private final IdClient client = new HttpIdClient(createClientConfig(3));

  @Override
  protected IdClient getIdClient() {
    return new CachingIdClient(client);
  }

  @Test
  @Ignore
  public void test_503(){
    super.test_503();
  }

  @Test
  @Ignore
  public void test_500(){
    super.test_503();
  }

  @Test
  @Ignore
  public void testGetAllDonorIds(){
    super.testGetAllDonorIds();
  }

  @Test
  @Ignore
  public void testGetAllSampleIds(){
    super.testGetAllSampleIds();
  }

  @Test
  @Ignore
  public void testGetAllSpecimenIds(){
    super.testGetAllSpecimenIds();
  }

  @Test
  @Ignore
  public void testGetAllMutationIds(){
    super.testGetAllMutationIds();
  }

}
