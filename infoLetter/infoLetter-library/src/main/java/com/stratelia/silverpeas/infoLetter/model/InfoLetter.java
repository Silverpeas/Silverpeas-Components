/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.infoLetter.model;

import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import org.silverpeas.util.WAPrimaryKey;

public class InfoLetter extends SilverpeasBean implements Comparable<InfoLetter> {

  private static final long serialVersionUID = -4798869204934629386L;

  /** InfoLetter instance identifier */
  private String instanceId;

  /** InfoLetter name */
  private String name;

  /** Info Letter description */
  private String description;

  /** InfoLetter frequency */
  private String periode;

  /**
   * Default constructor
   */
  public InfoLetter() {
    super();
    instanceId = "";
    name = "";
    description = "";
    periode = "";
  }

  /**
   * @param pk the info letter identifier
   * @param name the name
   * @param instanceId the component instance identifier
   * @param description the description
   * @param periode the frequency
   */
  public InfoLetter(WAPrimaryKey pk, String instanceId, String name, String description,
      String periode) {
    super();
    setPK(pk);
    this.instanceId = instanceId;
    this.name = name;
    this.description = description;
    this.periode = periode;
  }

  // Assesseurs

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getName() {
    return name;
  }

  public void setName(String n) {
    name = n;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPeriode() {
    return periode;
  }

  public void setPeriode(String periode) {
    this.periode = periode;
  }

  // Methodes

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  public int compareTo(InfoLetter obj) {
    if (obj == null) {
      return 0;
    }
    return (String.valueOf(getPK().getId())).compareTo(String.valueOf(obj.getPK().getId()));
  }

  public String _getTableName() {
    return "SC_IL_Letter";
  }

}
