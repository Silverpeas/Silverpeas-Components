/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.silverpeas.components.organizationchart.model;

import java.util.Map;

public class OrganizationalPerson {

  private int id;
  private int parentId;
  private String name;// =cn
  private String fonction; // =titre
  private String description;// =tooltip
  private String service;// ou
  private Map<String, String> detail;
  
  private boolean visibleOnCenter = false; // visible on the top central unit
  private String visibleCenterLabel = null;
  private boolean visibleOnRight = false;  // visible on the right unit (units organizational chart only)
  private String visibleRightLabel = null; 
  private boolean visibleOnLeft = false;   // visible on the right unit (units organizational chart only)
  private String visibleLeftLabel = null;
  private String visibleCategory = null;  // visible category name (detailled organizational chart only)
  
  public OrganizationalPerson(int id, int parentId, String name, String fonction, String description, String service) {
    this.id = id;
    this.parentId = parentId;
    this.name = name;
    this.fonction = fonction;
    this.description = description;
    this.service = service;
    this.parentId = -1;// root par défaut
  }

  public String toString() {// pour debug
    return "cn = " + this.name + ", service = " + this.service + ", fonction = " + this.fonction;
  }

  public void setParentId(int parentId) {
    this.parentId = parentId;
  }

  public String getService() {
    return service;
  }

  public int getId() {
    return id;
  }

  public int getParentId() {
    return parentId;
  }

  public String getName() {
    return name;
  }

  public String getFonction() {
    return fonction;
  }

  public String getDescription() {
    return description;
  }
 
  public void setDetail(Map<String, String> detail) {
    this.detail = detail;
  }
  
  public Map<String, String> getDetail() {
	return detail;
  }
  
  public boolean isVisibleOnCenter() {
		return visibleOnCenter;
	}

	public void setVisibleOnCenter(boolean visibleOnCenter) {
		this.visibleOnCenter = visibleOnCenter;
	}

	public boolean isVisibleOnRight() {
		return visibleOnRight;
	}

	public void setVisibleOnRight(boolean visibleOnRight) {
		this.visibleOnRight = visibleOnRight;
	}

	public boolean isVisibleOnLeft() {
		return visibleOnLeft;
	}

	public void setVisibleOnLeft(boolean visibleOnLeft) {
		this.visibleOnLeft = visibleOnLeft;
	}

	public String getVisibleCategory() {
		return visibleCategory;
	}

	public void setVisibleCategory(String visibleCategory) {
		this.visibleCategory = visibleCategory;
	}
	
    public String getVisibleCenterLabel() {
		return visibleCenterLabel;
	}

	public void setVisibleCenterLabel(String visibleCenterlabel) {
		this.visibleCenterLabel = visibleCenterlabel;
	}

	public String getVisibleRightLabel() {
		return visibleRightLabel;
	}

	public void setVisibleRightLabel(String visibleRightLabel) {
		this.visibleRightLabel = visibleRightLabel;
	}

	public String getVisibleLeftLabel() {
		return visibleLeftLabel;
	}

	public void setVisibleLeftLabel(String visibleLeftLabel) {
		this.visibleLeftLabel = visibleLeftLabel;
	}

}
