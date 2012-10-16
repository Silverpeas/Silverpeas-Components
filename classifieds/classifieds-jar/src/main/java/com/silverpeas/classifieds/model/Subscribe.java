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
package com.silverpeas.classifieds.model;

import java.io.Serializable;

public class Subscribe implements Serializable {
  private static final long serialVersionUID = -2746485759326084199L;
  private String subscribeId;
  private String userId;
  private String field1;
  private String fieldName1;
  private String field2;
  private String fieldName2;
  private String instanceId;

  public Subscribe() {
  }

  public Subscribe(String field1, String field2) {
    this.field1 = field1;
    this.field2 = field2;
  }

  public String getSubscribeId() {
    return subscribeId;
  }

  public void setSubscribeId(String subscribeId) {
    this.subscribeId = subscribeId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getField1() {
    return field1;
  }

  public void setField1(String field1) {
    this.field1 = field1;
  }

  public String getField2() {
    return field2;
  }

  public void setField2(String field2) {
    this.field2 = field2;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getFieldName1() {
    return fieldName1;
  }

  public void setFieldName1(String fieldName1) {
    this.fieldName1 = fieldName1;
  }

  public String getFieldName2() {
    return fieldName2;
  }

  public void setFieldName2(String fieldName2) {
    this.fieldName2 = fieldName2;
  }

}
