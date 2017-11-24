package org.icgc.dcc.id.server;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.id.server.service.AnalysisService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"test", "development"})
public class AnalysisServiceTest {

  @Autowired
  private AnalysisService service;

  @Test
  public void testA(){
    val result = service.isExist("sdomweihg");
    log.info("Exist? = {}",result);
  }

}
