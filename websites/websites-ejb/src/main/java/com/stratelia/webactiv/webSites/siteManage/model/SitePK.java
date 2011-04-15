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

/*
 * SitePK.java
 *
 * Created on 9 Avril 2001, 16:40
 */

package com.stratelia.webactiv.webSites.siteManage.model;

/** 
 *
 * @author  cbonin
 * @version 
 */

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

public class SitePK extends WAPrimaryKey implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public SitePK(String id) {
    super(id);
  }

  public SitePK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  public SitePK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  public String getRootTableName() {
    return "site";
  }

  public boolean equals(Object other) {
    if (!(other instanceof SitePK))
      return false;
    return (id.equals(((SitePK) other).getId()))
        && (space.equals(((SitePK) other).getSpace()))
        && (componentName.equals(((SitePK) other).getComponentName()));
  }

}