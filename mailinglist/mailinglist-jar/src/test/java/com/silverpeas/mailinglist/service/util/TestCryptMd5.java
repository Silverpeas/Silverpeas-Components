package com.silverpeas.mailinglist.service.util;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import com.silverpeas.mailinglist.service.job.TestMessageChecker;
import com.silverpeas.util.cryptage.CryptMD5;
import com.stratelia.webactiv.util.exception.UtilException;

public class TestCryptMd5 extends TestCase {

  public void testCrypt() throws UtilException {
    String hash = CryptMD5.crypt("Hello World");
    assertNotNull(hash);
    assertEquals("b10a8db164e0754105b7a99be72e3fe5", hash);
  }

  public void testHash() throws UtilException {
    URL url = TestMessageChecker.class.getResource("lemonde.html");
    assertNotNull(url);
    String copyPath = url.getPath();
    assertNotNull(copyPath);
    File file = new File(copyPath);
    assertNotNull(file);
    assertTrue(file.exists());
    assertTrue(file.isFile());
    String hash = CryptMD5.hash(file);
    assertNotNull(hash);
    assertEquals("5de77d572dc46a7c82fdd658abedffb0", hash);
  }

}
