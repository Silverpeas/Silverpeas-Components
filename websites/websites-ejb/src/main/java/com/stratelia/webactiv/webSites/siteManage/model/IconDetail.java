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

/*
 * IconDetail.java
 *
 * Created on 19 avril 2001, 09:12
 */

package com.stratelia.webactiv.webSites.siteManage.model;

/** 
 *
 * @author  cbonin
 * @version 
 */

import java.io.Serializable;

public class IconDetail implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /*-------------- Attributs ------------------*/
  private SitePK iconPk = new SitePK("", "", "");
  private String name;
  private String description;
  private String address;

  /*-------------- Methodes des attributs ------------------*/
  // iconPk
  public SitePK getIconPK() {
    return iconPk;
  }

  public void setIconPK(SitePK val) {
    iconPk = new SitePK(val.getId(), val.getSpace(), val.getComponentName());
  }

  // name
  public String getName() {
    return name;
  }

  public void setName(String val) {
    name = val;
  }

  // description
  public String getDescription() {
    return description;
  }

  public void setDescription(String val) {
    description = val;
  }

  // address
  public String getAddress() {
    return address;
  }

  public void setAddress(String val) {
    address = val;
  }

  /*-------------- Methodes ------------------*/

  /**
   * SiteDetail
   */
  public IconDetail() {
    init("", "", "", "");
  }

  /**
   * IconDetail
   */
  public IconDetail(String idIcon, String name, String description,
      String address) {
    init(idIcon, name, description, address);
  }

  /**
   * init
   */
  public void init(String idIcon, String name, String description,
      String address) {
    this.iconPk.setId(idIcon);
    this.name = name;
    this.description = description;
    this.address = address;
  }

  /**
   * toString
   */
  public String toString() {
    return iconPk + "|" + name + "|" + description + "|" + address;
  }
}