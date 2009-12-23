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
  private String fonction; // =title ou ou si responsable
  private String description;// =tooltip
  private String service;// ou
  private String tel;// telephone
  private boolean responsable;
  private boolean firstLevel;
  private boolean detailed; // si true, on peut cliquer dessus pour voir toutes ses info
  private Map<String, String> detail;

  public OrganizationalPerson(int id, int parentId, String name, String fonction, String tel,
      String description, String service, boolean responsable) {
    this.id = id;
    this.parentId = parentId;
    this.name = name;
    this.fonction = fonction;
    this.tel = tel;
    this.description = description;
    this.service = service;
    this.responsable = responsable;
    this.detailed = true;
    this.parentId = -1;// root par défaut
    this.firstLevel = false;// children normal par défaut

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

  public boolean isResponsable() {
    return responsable;
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

  public String getTel() {
    return tel;
  }

  public String getStyle() {
    if (responsable)
      return "resp";
    if (firstLevel)
      return "assist";
    return "empl";
  }

  public void setFirstLevel(boolean first) {
    this.firstLevel = first;
  }

  public boolean isFirstLevel() {
    return firstLevel;
  }

  public String getDescription() {
    return description;
  }

  public boolean isDetailed() {
    return detailed;
  }

  public void setDetailed(boolean detailed) {
    this.detailed = detailed;
  }

  public Map<String, String> getDetail() {
    return detail;
  }

  public void setDetail(Map<String, String> detail) {
    this.detail = detail;
  }
}
