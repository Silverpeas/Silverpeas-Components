/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.mailinglist;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ehugonnet
 */
public class PathTestUtil {

  public static final String SEPARATOR = File.separator;
  private final static Properties TESTS_PROPS = new Properties();
  public static String BUILD_PATH = "";
  static {
    try {
      TESTS_PROPS.load(PathTestUtil.class.getClassLoader().
          getResourceAsStream("maven.properties"));
      BUILD_PATH = TESTS_PROPS.getProperty("build.dir").replace('/', SEPARATOR.charAt(0));
    } catch (IOException ex) {
      Logger.getLogger(PathTestUtil.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
