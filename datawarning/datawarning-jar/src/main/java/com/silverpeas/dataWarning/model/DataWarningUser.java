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
package com.silverpeas.dataWarning.model;

import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;

public class DataWarningUser extends SilverpeasBean {

  private static final long serialVersionUID = 3139321140753518379L;
  private String instanceId;
  private int userId;

  public DataWarningUser() {
    super();
  }

  public DataWarningUser(String instanceId, int userId) {
    this.instanceId = instanceId;
    this.userId = userId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public int getUserId() {
    return userId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  @Override
  public String _getTableName() {
    return "SC_DataWarning_Rel_User";
  }

  @Override
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }
}
