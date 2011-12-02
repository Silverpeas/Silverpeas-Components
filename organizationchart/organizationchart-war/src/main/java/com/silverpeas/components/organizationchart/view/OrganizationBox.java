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

package com.silverpeas.components.organizationchart.view;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class OrganizationBox {
  private String name = null;
  private String dn = null;
  private String parentDn = null;
  private List<UserVO> mainActors = null;

  private boolean detailLinkActive = false;
  private boolean centerLinkActive = false;
  
  private String specificCSSClass = "";
  private Map<String, String> details = null;

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the dn
   * @throws UnsupportedEncodingException
   */
  public String getUrl() throws UnsupportedEncodingException {
    return "Main?baseOu=" + URLEncoder.encode(dn, "UTF-8");
  }

  /**
   * @param dn the dn to set
   */
  public void setDn(String dn) {
    this.dn = dn;
  }

  /**
   * @return the mainActors
   */
  public List<UserVO> getMainActors() {
    return mainActors;
  }

  /**
   * @param mainActors the mainActors to set
   */
  public void setMainActors(List<UserVO> mainActors) {
    this.mainActors = mainActors;
  }

  /**
   * @return the detailLinkActive
   */
  public boolean isDetailLinkActive() {
    return detailLinkActive;
  }

  /**
   * @param detailLinkActive the detailLinkActive to set
   */
  public void setDetailLinkActive(boolean detailLinkActive) {
    this.detailLinkActive = detailLinkActive;
  }

  /**
   * @return the centerLinkActive
   */
  public boolean isCenterLinkActive() {
    return centerLinkActive;
  }

  /**
   * @param centerLinkActive the centerLinkActive to set
   */
  public void setCenterLinkActive(boolean centerLinkActive) {
    this.centerLinkActive = centerLinkActive;
  }

  /**
   * @param parentDn the parentDn to set
   */
  public void setParentDn(String parentDn) {
    this.parentDn = parentDn;
  }

  /**
   * @return the parentUrk
   * @throws UnsupportedEncodingException
   */
  public String getParentUrl() throws UnsupportedEncodingException {
    if (parentDn == null)
      return "";
    else
      return "Main?baseOu=" + URLEncoder.encode(parentDn, "UTF-8");
  }

  public void setSpecificCSSClass(String specificCSSClass) {
    this.specificCSSClass = specificCSSClass;
  }

  public String getSpecificCSSClass() {
    return specificCSSClass;
  }
  
  public Map<String, String> getDetails() {
    return details;
  }

  public void setDetails(Map<String, String> details) {
    this.details = details;
  }

}