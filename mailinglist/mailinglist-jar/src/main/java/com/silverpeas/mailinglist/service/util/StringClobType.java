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

public class StringClobType extends ImmutableType{


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
