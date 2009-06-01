package com.stratelia.silverpeas.connecteurJDBC.model;

import com.stratelia.webactiv.persistence.SilverpeasBean;

public class ConnecteurJDBCConnectionInfoDetail extends SilverpeasBean {

  private String JDBCdriverName = "";
  private String JDBCurl = "";
  private String login = "";
  private String password = "";
  private String SQLreq = "";
  private int rowLimit;

  // getters
  public String getJDBCdriverName() {
	return JDBCdriverName;
  }
  public String getJDBCurl() {
	return JDBCurl;
  }
  public String getLogin() {
	return login;
  }
  public String getPassword() {
	return password;
  }
  public String getSQLreq() {
	return SQLreq;
  }
  public int getRowLimit() {
	return rowLimit;
  }
  public String getInstanceId() {
		return getPK().getComponentName();
  }

  //setters
  public void setJDBCdriverName(String JDBCdriverName) {
	this.JDBCdriverName = JDBCdriverName;
  }
  public void setJDBCurl(String JDBCurl) {
	this.JDBCurl = JDBCurl;
  }
  public void setLogin(String login) {
	this.login = login;
  }
  public void setPassword(String password) {
	this.password = password;
  }
  public void setSQLreq(String SQLreq) {
	this.SQLreq = SQLreq;
  }
  public void setRowLimit(int rowLimit) {
	this.rowLimit = rowLimit;
  }
}