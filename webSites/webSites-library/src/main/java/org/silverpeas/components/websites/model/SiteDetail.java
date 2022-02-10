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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.websites.model;

import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngine;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngineProvider;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;

public class SiteDetail extends PublicationDetail {
  private static final long serialVersionUID = 1435448496246944796L;
  private SitePK sitePk = new SitePK("", "");

  /**
   * page interne creee (0) ou externe (1) ou page interne uploadee (2)
   */
  private int siteType;
  /**
   * site non publie (0) ou publie (1)
   */
  private int state;
  private int popup = 1;
  private String silverObjectId;
  private String positions;

  public static final String SITE_TYPE = "Website";

  public SiteDetail(String siteId, String applicationId, String name, String description,
      String page, int type, String creatorId) {
    setName(name);
    setDescription(description);
    setCreatorId(creatorId);
    setImportance(type);
    setVersion(siteId);
    setKeywords("");
    setContentPagePath(page);

    SitePK sitePK = new SitePK(siteId, applicationId);
    setSitePK(sitePK);
  }

  // sitePk
  public SitePK getSitePK() {
    return sitePk;
  }

  public void setSitePK(SitePK val) {
    sitePk = new SitePK(val.getId(), val.getComponentName());
  }

  public int getSiteType() {
    return siteType;
  }

  public void setSiteType(int val) {
    siteType = val;
  }

  public int getState() {
    return state;
  }

  public void setState(int val) {
    state = val;
  }

  @Override
  public String getURL() {
    return "searchResult?Type=Site&Id=" + getId();
  }

  @Override
  public String getId() {
    return getSitePK().getId();
  }

  @Override
  public String getInstanceId() {
    return getSitePK().getComponentName();
  }

  /**
   * @return the positions
   */
  public String getPositions() {
    return positions;
  }

  /**
   * @param positions the positions to set
   */
  public void setPositions(String positions) {
    this.positions = positions;
  }

  /**
   * toString
   */
  @Override
  public String toString() {
    return sitePk.getId() + "|" + this.getName() + "|" + this.getDescription() + "|" +
        this.getContentPagePath() + "|" + "|" + siteType + "|" + this.getCreatorId() + "|" +
        this.getCreationDate() + "|" + state;
  }

  public int getPopup() {
    return popup;
  }

  public void setPopup(int popup) {
    this.popup = popup;
  }

  @Override
  public String getContributionType() {
    return SITE_TYPE;
  }

  @Override
  public String getSilverpeasContentId() {
    if (this.silverObjectId == null) {
      ContentManagementEngine contentMgtEngine =
          ContentManagementEngineProvider.getContentManagementEngine();
      try {
        int objectId = contentMgtEngine.getSilverContentId(this.getId(), this.getInstanceId());
        if (objectId >= 0) {
          this.silverObjectId = String.valueOf(objectId);
        }
      } catch (ContentManagerException ex) {
        this.silverObjectId = null;
      }
    }
    return this.silverObjectId;
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}