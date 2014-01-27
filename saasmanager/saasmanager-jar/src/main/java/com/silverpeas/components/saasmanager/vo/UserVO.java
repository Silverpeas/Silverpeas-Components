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

package com.silverpeas.components.saasmanager.vo;

/**
 * User visual object
 * @author ahedin
 *
 */
public class UserVO {

  public static final String ROLE_ADMIN = "admin";
  public static final String ROLE_PUBLISHER = "publisher";
  public static final String ROLE_WRITER = "writer";
  public static final String ROLE_READER = "reader";
  public static final String[] ROLES = { ROLE_ADMIN, ROLE_PUBLISHER, ROLE_WRITER, ROLE_READER };

  private String id;
  private String lastName;
  private String firstName;
  private String login;
  private String email;
  private String role;
  private String company;
  private String phone;

  public UserVO() {
    id = "-1";
    role = ROLE_READER;
  }

  public UserVO(String id, String lastName, String firstName, String login, String email,
      String company, String phone) {
    this.id = id;
    this.lastName = lastName;
    this.firstName = firstName;
    this.login = login;
    this.email = email;
    role = ROLE_READER;
    this.company = company;
    this.phone = phone;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public String getLastName() {
    return lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLogin() {
    return login;
  }

  public String getEmail() {
    return email;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getRole() {
    return role;
  }

  public String getCompany() {
    return company;
  }

  public String getPhone() {
    return phone;
  }

}
