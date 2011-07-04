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

package com.silverpeas.crm.model;

import java.io.Serializable;

import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * Class declaration
 * @author
 */
public class CrmPK extends WAPrimaryKey implements Serializable {

  private static final long serialVersionUID = 135542061633931139L;

  protected static OrganizationController organizationController = new OrganizationController();

  public CrmPK(String id, String componentId) {
    super(id);
    ComponentInst component = organizationController.getComponentInst(componentId);
    setComponentName(componentId);
    setSpace(component.getDomainFatherId());
  }

  /**
   * Method declaration
   * @param other
   * @return
   * @see
   */
  public boolean equals(Object other) {
    if (!(other instanceof CrmPK)) {
      return false;
    }
    return id.equals(((CrmPK) other).getId());
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String toString() {
    return "(id = " + getId() + " )";
  }

}
