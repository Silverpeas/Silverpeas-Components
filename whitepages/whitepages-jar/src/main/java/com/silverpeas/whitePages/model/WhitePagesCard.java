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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.whitePages.model;

import com.stratelia.webactiv.beans.admin.*;
import org.silverpeas.core.admin.OrganisationController;

public class WhitePagesCard implements Comparable {
  private long userCardId = 0;
  private String instanceLabel;
  private String instanceId;
  private static OrganisationController organizationController = new OrganizationController();

  public WhitePagesCard() {
  }

  public WhitePagesCard(String label) {
    this.instanceLabel = label;
  }

  public WhitePagesCard(long userCardId, String instanceId) {
    setInstanceId(instanceId);
    setUserCardId(userCardId);
  }

  public long getUserCardId() {
    return this.userCardId;
  }

  public String getInstanceId() {
    return this.instanceId;
  }

  public String readInstanceLabel() {
    return this.instanceLabel;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
    this.instanceLabel = organizationController.getComponentInst(instanceId)
        .getLabel();
  }

  public void setUserCardId(long userCardId) {
    this.userCardId = userCardId;
  }

  public boolean equals(Object theOther) {
    return (readInstanceLabel().equals(((WhitePagesCard) theOther)
        .readInstanceLabel()));
  }

  public int compareTo(Object theOther) {
    return (readInstanceLabel().compareTo(((WhitePagesCard) theOther)
        .readInstanceLabel()));
  }

}
