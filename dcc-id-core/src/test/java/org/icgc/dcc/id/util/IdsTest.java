package org.icgc.dcc.id.util;

import static java.lang.String.format;
import static org.icgc.dcc.id.util.Ids.validateId;

import java.util.Optional;

import org.icgc.dcc.id.core.IdentifierException;
import org.icgc.dcc.id.core.Prefixes;
import org.junit.Test;

public class IdsTest {

  @Test
  public void testValidateId() throws Exception {
    validateId(Optional.of(format("%s123", Prefixes.DONOR_ID_PREFIX)), Prefixes.DONOR_ID_PREFIX);
    validateId(Optional.empty(), Prefixes.DONOR_ID_PREFIX);
  }

  @Test(expected = IdentifierException.class)
  public void testValidateId_failure() throws Exception {
    validateId(Optional.of("123"), Prefixes.DONOR_ID_PREFIX);
  }

}
