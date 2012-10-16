/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
// *** Rajoute par F. Rageade pour compilation du SessionController
package com.stratelia.silverpeas.infoLetter.control;

// ***

import java.io.*;
import javax.activation.*;

/**
 * A simple DataSource for demonstration purposes. This class implements a DataSource from: an
 * InputStream a byte array a String
 * @author John Mani
 * @author Bill Shannon
 * @author Max Spivak
 */
public class ByteArrayDataSource implements DataSource {
  private byte[] data; // data
  private String type; // content-type

  /* Create a DataSource from an input stream */
  public ByteArrayDataSource(InputStream is, String type) {
    this.type = type;
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      int ch;

      while ((ch = is.read()) != -1)
        // XXX - must be made more efficient by
        // doing buffered reads, rather than one byte reads
        os.write(ch);
      data = os.toByteArray();

    } catch (IOException ioex) {
    }
  }

  /* Create a DataSource from a byte array */
  public ByteArrayDataSource(byte[] data, String type) {
    this.data = data;
    this.type = type;
  }

  /* Create a DataSource from a String */
  public ByteArrayDataSource(String data, String type) {
    try {
      // Assumption that the string contains only ASCII
      // characters! Otherwise just pass a charset into this
      // constructor and use it in getBytes()
      this.data = data.getBytes("UTF-8");
    } catch (UnsupportedEncodingException uex) {
    }
    this.type = type;
  }

  /**
   * Return an InputStream for the data. Note - a new stream must be returned each time.
   */
  public InputStream getInputStream() throws IOException {
    if (data == null) {
      throw new IOException("no data");
    }
    return new ByteArrayInputStream(data);
  }

  public OutputStream getOutputStream() throws IOException {
    throw new IOException("cannot do this");
  }

  public String getContentType() {
    return type;
  }

  public String getName() {
    return "dummy";
  }
}
