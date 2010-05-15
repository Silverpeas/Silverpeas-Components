/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    assertEquals("fc8820e7b46497bb444e0155c5ce631d", hash);
  }

}
