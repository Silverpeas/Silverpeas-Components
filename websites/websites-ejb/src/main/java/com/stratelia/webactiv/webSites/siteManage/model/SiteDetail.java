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
package com.stratelia.webactiv.webSites.siteManage.model;


import java.text.ParseException;
import java.util.Date;

import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.ContentManagerFactory;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

//CBO : UPDATE
//public class SiteDetail extends AbstractI18NBean implements Serializable, SilverContentInterface
public class SiteDetail extends PublicationDetail {
  private static final long serialVersionUID = 1435448496246944796L;
  /*-------------- Attributs ------------------*/
  private SitePK sitePk = new SitePK("", "");
  private int type; /*
                     * page interne creee (0) ou externe (1) ou page interne uploadee (2)
                     */
  private int state; /* site non publie (0) ou publie (1) */
  private int popup = 1;

  private String silverObjectId;
  public static final String SITETYPE = "Website";
  private String positions;

  /**
   * SiteDetail default constructor
   * @param idSite
   * @param applicationId TODO
   * @param name
   * @param description
   * @param page
   * @param type
   * @param creatorId
   * @param date
   * @param state
   * @param popup
   */
  public SiteDetail(String idSite, String applicationId, String name, String description, String page,
      int type, String creatorId, String date, int state, int popup) {
    super("X", name, description, null, null, null, creatorId, Integer.toString(type), idSite, "",
        page);
    if (date != null) {
      Date theCreationDate = null;
      try {
        theCreationDate = DateUtil.parse(date);
      } catch (ParseException e) {
        SilverTrace.error(SITETYPE, "SiteDetail constructor", "Problem to parse date", e);
      }
      this.setCreationDate(theCreationDate);
    }
    SitePK sitePK = new SitePK(idSite, applicationId);
    init(sitePK, type, state, popup);
  }

  /*-------------- Methodes des attributs ------------------*/
  // sitePk
  public SitePK getSitePK() {
    return sitePk;
  }

  public void setSitePK(SitePK val) {
    sitePk = new SitePK(val.getId(), val.getComponentName());
  }

  public int getType() {
    return type;
  }

  public void setType(int val) {
    type = val;
  }

  public int getState() {
    return state;
  }

  public void setState(int val) {
    state = val;
  }

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
   * init
   */
  private void init(SitePK sitePK, int type, int state, int popup) {
    this.setSitePK(sitePK);
    this.type = type;
    this.state = state;
    this.popup = popup;
  }

  /**
   * toString
   */
  @Override
  public String toString() {
    return sitePk.getId() + "|" + this.getName() + "|" + this.getDescription()
        + "|" + this.getContent() + "|" + "|" + type + "|"
        + this.getCreatorId() + "|" + this.getCreationDate() + "|" + state;
  }

  public int getPopup() {
    return popup;
  }

  public void setPopup(int popup) {
    this.popup = popup;
  }

  @Override
  public String getComponentInstanceId() {
    return this.getInstanceId();
  }

  @Override
  public String getContributionType() {
    return SITETYPE;
  }

  @Override
  public String getSilverpeasContentId() {
    if (this.silverObjectId == null) {
      ContentManager contentManager = ContentManagerFactory.getFactory().getContentManager();
      try {
        int objectId = contentManager.getSilverContentId(this.getId(), this.getInstanceId());
        if (objectId >= 0) {
          this.silverObjectId = String.valueOf(objectId);
        }
      } catch (ContentManagerException ex) {
        this.silverObjectId = null;
      }
    }
    return this.silverObjectId;
  }

}