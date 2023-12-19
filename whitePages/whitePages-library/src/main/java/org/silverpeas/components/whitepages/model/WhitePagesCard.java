/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.whitepages.model;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;

public class WhitePagesCard implements Comparable<WhitePagesCard> {

  private long userCardId = 0;
  private String instanceLabel;
  private String instanceId;

  public WhitePagesCard(String label) {
    this.instanceLabel = label;
  }

  public WhitePagesCard(long userCardId, String instanceId) {
    setInstanceId(instanceId);
    this.userCardId = userCardId;
  }

  public long getUserCardId() {
    return this.userCardId;
  }

  public String getInstanceId() {
    return this.instanceId;
  }

  public String getInstanceLabel() {
    return this.instanceLabel;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
    OrganizationController organizationController =
        OrganizationControllerProvider.getOrganisationController();
    ComponentInstLight app = organizationController.getComponentInstLight(instanceId);
    if (app != null) {
      this.instanceLabel = app.getLabel();
    } else {
      this.instanceLabel = "";
    }
  }

  public void setUserCardId(long userCardId) {
    this.userCardId = userCardId;
  }

  @Override
  public boolean equals(Object theOther) {
    if (!(theOther instanceof WhitePagesCard)) {
      return false;
    }
    if (theOther.getClass() != getClass()) {
      return false;
    }
    return (getInstanceLabel().equals(((WhitePagesCard) theOther).getInstanceLabel()));
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 29 * hash + (int) (this.userCardId ^ (this.userCardId >>> 32));
    hash = 29 * hash + (this.instanceLabel != null ? this.instanceLabel.hashCode() : 0);
    hash = 29 * hash + (this.instanceId != null ? this.instanceId.hashCode() : 0);
    return hash;
  }

  @Override
  public int compareTo(WhitePagesCard theOther) {
    return (getInstanceLabel().compareTo((theOther).getInstanceLabel()));
  }
}
