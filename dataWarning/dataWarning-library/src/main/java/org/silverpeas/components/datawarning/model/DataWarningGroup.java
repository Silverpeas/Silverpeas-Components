/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.datawarning.model;

import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;
import org.silverpeas.kernel.annotation.NonNull;

@SuppressWarnings({"deprecation", "unused"})
public class DataWarningGroup extends SilverpeasBean {

  private static final long serialVersionUID = -7331971484764201589L;
  private String instanceId;
  private int groupId;

  public DataWarningGroup() {
    // for the persistence layer
  }

  public DataWarningGroup(String instanceId, int groupId) {
    this.instanceId = instanceId;
    this.groupId = groupId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public int getGroupId() {
    return groupId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public void setGroupId(int groupId) {
    this.groupId = groupId;
  }

  @Override
  @NonNull
  protected String getTableName() {
    return "SC_DataWarning_Rel_Group";
  }

}
