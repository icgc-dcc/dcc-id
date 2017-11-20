package org.icgc.dcc.id.client.http;

import org.icgc.dcc.id.client.core.IdClient;

public class HttpIdClientTest extends  AbstractIdClientTest{

  private final IdClient client = new HttpIdClient(createClientConfig(3));

  @Override
  protected IdClient getIdClient() {
    return client;
  }

}
