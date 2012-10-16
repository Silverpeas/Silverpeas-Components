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

package com.silverpeas.components.organizationchart.view;

import java.util.List;

public class ChartUnitVO extends ChartVO {
  private CategoryBox leftRole = null;
  private CategoryBox rightRole = null;
  private List<OrganizationBox> subOrganizations = null;

  /**
   * @return the leftRole
   */
  public CategoryBox getLeftRole() {
    return leftRole;
  }

  /**
   * @param leftRole the leftRole to set
   */
  public void setLeftRole(CategoryBox leftRole) {
    this.leftRole = leftRole;
  }

  /**
   * @return the rightRole
   */
  public CategoryBox getRightRole() {
    return rightRole;
  }

  /**
   * @param rightRole the rightRole to set
   */
  public void setRightRole(CategoryBox rightRole) {
    this.rightRole = rightRole;
  }

  /**
   * @return the subOrganizations
   */
  public List<OrganizationBox> getSubOrganizations() {
    return subOrganizations;
  }

  /**
   * @param subOrganizations the subOrganizations to set
   */
  public void setSubOrganizations(List<OrganizationBox> subOrganizations) {
    this.subOrganizations = subOrganizations;
  }

  @Override
  public int getChartType() {
    return 0;
  }

}
