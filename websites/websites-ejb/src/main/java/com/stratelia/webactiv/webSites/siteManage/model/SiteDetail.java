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
/*
 * SiteDetail.java
 *
 * Created on 9 Avril 2001, 18:00
 */

package com.stratelia.webactiv.webSites.siteManage.model;

/** 
 * 
 * @author  cbonin
 * @version 
 */
import java.text.ParseException;
import java.util.Date;

import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

//CBO : UPDATE
//public class SiteDetail extends AbstractI18NBean implements Serializable, SilverContentInterface
public class SiteDetail extends PublicationDetail {
  /*-------------- Attributs ------------------*/
  private SitePK sitePk = new SitePK("", "", "");
  // CBO : REMOVE private String name;
  // CBO : REMOVE private String description;
  // CBO : REMOVE private String page;
  private int type; /*
                     * page interne creee (0) ou externe (1) ou page interne
                     * uploadee (2)
                     */
  // CBO : REMOVE private String author;
  // CBO : REMOVE private String date;
  private int state; /* site non publie (0) ou publie (1) */
  // CBO : REMOVE private String silverObjectId; //added for the components -
  // PDC integration
  // CBO : REMOVE private String iconUrl; //added for the components - PDC
  // integration
  private int popup = 1;

  /*-------------- Methodes des attributs ------------------*/
  // sitePk
  public SitePK getSitePK() {
    return sitePk;
  }

  public void setSitePK(SitePK val) {
    sitePk = new SitePK(val.getId(), val.getSpace(), val.getComponentName());
  }

  // CBO : REMOVE
  // name
  /*
   * public String getName() { return name; } public void setName(String val) {
   * name = val; }
   * 
   * //description public String getDescription() { return description; } public
   * void setDescription(String val) { description = val; }
   * 
   * //page public String getPage() { return page; } public void setPage(String
   * val) { page = val; }
   */
  // CBO : FIN REMOVE

  // type
  public int getType() {
    return type;
  }

  public void setType(int val) {
    type = val;
  }

  // CBO : REMOVE
  // author
  /*
   * public String getAuthor() { return author; } public void setAuthor(String
   * val) { author = val; }
   * 
   * //date public String getDate() { return date; } public void setDate(String
   * val) { date = val; }
   */
  // CBO : FIN REMOVE

  // state
  public int getState() {
    return state;
  }

  public void setState(int val) {
    state = val;
  }

  // CBO : REMOVE
  // PDC integration
  /*
   * public void setSilverObjectId(String silverObjectId) { this.silverObjectId
   * = silverObjectId; } public void setSilverObjectId(int silverObjectId) {
   * this.silverObjectId = new Integer(silverObjectId).toString(); } public
   * String getSilverObjectId() { return this.silverObjectId; }
   */

  public String getURL() {
    return "searchResult?Type=Site&Id=" + getId();
  }

  public String getId() {
    return getSitePK().getId();
  }

  public String getInstanceId() {
    return getSitePK().getComponentName();
  }

  /*
   * public String getTitle() { return getName(); }
   * 
   * public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
   * 
   * public String getIconUrl() { return this.iconUrl; }
   * 
   * public String getCreatorId(){ return getAuthor(); }
   * 
   * public String getSilverCreationDate() { return date; }
   * 
   * public String getDescription(String language) { return getDescription(); }
   * 
   * public String getName(String language) { return getName(); }
   * 
   * public Iterator getLanguages() { return null; }
   */
  // CBO : FIN REMOVE

  /*-------------- Methodes ------------------*/

  /**
   * SiteDetail
   */
  // CBO : REMOVE
  /*
   * public SiteDetail() { init("","","","",0,"","",0,0); }
   */

  /**
   * SiteDetail
   * 
   * @throws ParseException
   */
  public SiteDetail(String idSite, String name, String description,
      String page, int type, String creatorId, String date, int state, int popup) {
    // CBO : ADD
    super("X", name, description, null, null, null, creatorId,
        new Integer(type).toString(), idSite, "", page);

    if (date != null) {
      Date theCreationDate = null;
      try {
        theCreationDate = DateUtil.parse(date);
      } catch (ParseException e) {

      }
      this.setCreationDate(theCreationDate);
    }
    // CBO : FIN ADD

    // CBO : UPDATE
    // init(idSite, name, description, page, type, author, date, state, popup);
    init(idSite, type, state, popup);
  }

  /**
   * init
   */
  // CBO : UPDATE
  /*
   * public void init(String idSite, String name, String description, String
   * page, int type, String author, String date, int state, int popup){
   * this.sitePk.setId(idSite); this.name = name; this.description =
   * description; this.page = page; this.type = type; this.author = author;
   * this.date = date; this.state = state; this.popup = popup;
   * 
   * }
   */
  public void init(String idSite, int type, int state, int popup) {
    this.sitePk.setId(idSite);
    this.type = type;
    this.state = state;
    this.popup = popup;
  }

  // CBO : FIN UPDATE

  /**
   * toString
   */
  public String toString() {
    // CBO : UPDATE
    // return
    // sitePk+"|"+name+"|"+description+"|"+page+"|"+type+"|"+author+"|"+date+"|"+state;
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
}