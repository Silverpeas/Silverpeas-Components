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

package com.silverpeas.mailinglist.service.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.type.ImmutableType;

public class StringClobType extends ImmutableType {

  public Object fromStringValue(String value) throws HibernateException {
    return value;
  }

  public Object get(ResultSet rs, String name) throws HibernateException,
      SQLException {
    Reader reader = rs.getCharacterStream(name);
    if (reader == null) {
      return null;
    }
    StringBuffer sb = new StringBuffer();
    try {
      char[] charbuf = new char[4096];
      for (int i = reader.read(charbuf); i > 0; i = reader.read(charbuf)) {
        sb.append(charbuf, 0, i);
      }
    } catch (IOException e) {
      throw new SQLException(e.getMessage());
    }
    return sb.toString();
  }

  public void set(PreparedStatement pstmt, Object value, int index)
      throws HibernateException, SQLException {
    StringReader r = new StringReader((String) value);
    pstmt.setCharacterStream(index, r, ((String) value).length());

  }

  public int sqlType() {
    return Types.CLOB;
  }

  public String toString(Object value) throws HibernateException {
    return (String) value;
  }

  public String getName() {
    return "string";
  }

  @SuppressWarnings("unchecked")
  public Class getReturnedClass() {
    return String.class;
  }

}
