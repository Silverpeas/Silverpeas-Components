package com.silverpeas.mailinglist.service.notification;

/**
 * SMTP configuration
 * 
 * @author Emmanuel Hugonnet
 * @version $revision$
 */
public class SmtpConfiguration {

  private String username;

  private String password;

  private String server;

  private int port;

  private boolean authenticate;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public boolean isAuthenticate() {
    return authenticate;
  }

  public void setAuthenticate(boolean authenticate) {
    this.authenticate = authenticate;
  }
}
