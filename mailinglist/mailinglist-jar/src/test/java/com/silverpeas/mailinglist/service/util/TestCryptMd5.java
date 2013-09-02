/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.mailinglist.service.util;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import com.silverpeas.mailinglist.service.job.TestMessageChecker;
import org.silverpeas.util.crypto.CryptMD5;

import com.stratelia.webactiv.util.exception.UtilException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TestCryptMd5 {

  @Test
  public void testCrypt() throws UtilException {
    String hash = CryptMD5.encrypt("Hello World");
    assertThat(hash, is(notNullValue()));
    assertThat(hash, is("b10a8db164e0754105b7a99be72e3fe5"));
  }

  @Test
  public void testHash() throws UtilException {
    URL url = TestMessageChecker.class.getResource("lemonde.html");
    assertThat(url, is(notNullValue()));
    String copyPath = url.getPath();
    assertThat(copyPath, is(notNullValue()));
    File file = new File(copyPath);
    assertThat(file, is(notNullValue()));
    assertThat(file.exists(), is(true));
    assertThat(file.isFile(), is(true));
    String hash = CryptMD5.encrypt(file);
    assertThat(hash, is(notNullValue()));
    assertThat(hash, is("7d0d6464f2bcfd92cfc16c8a4fd62306"));
  }
}
