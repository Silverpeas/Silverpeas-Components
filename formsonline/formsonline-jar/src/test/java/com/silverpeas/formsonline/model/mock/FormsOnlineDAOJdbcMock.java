package com.silverpeas.formsonline.model.mock;

import java.sql.Connection;

import com.silverpeas.formsonline.model.FormsOnlineDAOJdbc;
import com.silverpeas.formsonline.model.FormsOnlineDatabaseException;

public class FormsOnlineDAOJdbcMock extends FormsOnlineDAOJdbc {
  
  private Connection con = null;
  
  public FormsOnlineDAOJdbcMock(Connection con) {
    this.con = con;
  }
  
  @Override
  protected Connection getConnection() throws FormsOnlineDatabaseException {
    return con;
  }
}
