package org.silverpeas.components.websites.model;

import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import java.text.ParseException;
import java.util.Date;

public class SiteDetailBuilder {
  private String idSite;
  private String applicationId;
  private String name;
  private String description;
  private String page;
  private int type;
  private String creatorId;
  private String date;
  private int state;
  private int popup;

  public SiteDetailBuilder setSiteId(final String siteId) {
    this.idSite = siteId;
    return this;
  }

  public SiteDetailBuilder setApplicationId(final String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  public SiteDetailBuilder setName(final String name) {
    this.name = name;
    return this;
  }

  public SiteDetailBuilder setDescription(final String description) {
    this.description = description;
    return this;
  }

  public SiteDetailBuilder setPage(final String page) {
    this.page = page;
    return this;
  }

  public SiteDetailBuilder setType(final int type) {
    this.type = type;
    return this;
  }

  public SiteDetailBuilder setCreatorId(final String creatorId) {
    this.creatorId = creatorId;
    return this;
  }

  public SiteDetailBuilder setDate(final String date) {
    this.date = date;
    return this;
  }

  public SiteDetailBuilder setState(final int state) {
    this.state = state;
    return this;
  }

  public SiteDetailBuilder setPopup(final int popup) {
    this.popup = popup;
    return this;
  }

  public SiteDetail createSiteDetail() {
    SiteDetail site =
        new SiteDetail(idSite, applicationId, name, description, page, type, creatorId);
    if (date != null) {
      Date theCreationDate = null;
      try {
        theCreationDate = DateUtil.parse(date);
      } catch (ParseException e) {
        SilverLogger.getLogger(this).error(e);
      }
      site.setCreationDate(theCreationDate);
    }
    site.setSiteType(type);
    site.setState(state);
    site.setPopup(popup);
    return site;
  }
}