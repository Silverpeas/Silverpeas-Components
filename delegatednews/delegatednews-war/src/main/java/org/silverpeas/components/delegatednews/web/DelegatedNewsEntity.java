/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.components.delegatednews.web;

import org.silverpeas.components.delegatednews.model.DelegatedNews;
import org.silverpeas.core.webapi.profile.UserProfileEntity;
import org.silverpeas.core.webapi.base.WebEntity;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.Date;

/**
 * The delegated news entity is a delegated news object that is exposed in the web as an entity (web
 * entity).
 * As such, it publishes only some of its attributes
 * It represents a delegated news in Silverpeas plus some additional information such as the URI
 * for accessing it.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DelegatedNewsEntity implements WebEntity {

  private static final long serialVersionUID = 8023645204584179638L;
  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(required = true)
  private int pubId;
  @XmlElement(required = true)
  private String pubTitle;
  @XmlElement(required = true)
  private String instanceId;
  @XmlElement(required = true)
  private String status;
  @XmlElement(required = true)
  private UserProfileEntity contributor;
  @XmlElement(defaultValue = "")
  private UserProfileEntity validator;
  @XmlElement(defaultValue = "")
  private Date validationDate;
  @XmlElement(defaultValue = "")
  private Date beginDate;
  @XmlElement(defaultValue = "")
  private Date endDate;
  @XmlElement(defaultValue = "0")
  private int newsOrder;

  @SuppressWarnings("unused")
  private DelegatedNewsEntity(final DelegatedNews delegatednews) {
    this.pubId = delegatednews.getPubId();
    this.instanceId = delegatednews.getInstanceId();
    this.status = delegatednews.getStatus();
    OrganizationController organizationController =
        OrganizationControllerProvider.getOrganisationController();
    UserDetail user = organizationController.getUserDetail(delegatednews.getContributorId());
    this.contributor = UserProfileEntity.fromUser(user);
    if (delegatednews.getValidatorId() != null) {
      user = organizationController.getUserDetail(delegatednews.getValidatorId());
      if (user != null) {
        this.validator = UserProfileEntity.fromUser(user);
      }
    }
    this.validationDate = delegatednews.getValidationDate();
    this.beginDate = delegatednews.getBeginDate();
    this.endDate = delegatednews.getEndDate();
    this.newsOrder = delegatednews.getNewsOrder();
  }

  protected DelegatedNewsEntity() {
  }

  /**
   * Gets the URI of this comment entity.
   * @return the URI with which this entity can be access through the Web.
   */
  @Override
  public URI getURI() {
    return this.uri;
  }


  /**
   * Gets the identifier of the delegated news.
   * @return the delegated news identifier.
   */
  public int getPubId() {
    return this.pubId;
  }

  /**
   * Gets the title of the delegated news.
   * @return the pubTitle.
   */
  public String getPubTitle() {
    return this.pubTitle;
  }

  /**
   * Gets the identifier of the Silverpeas instance identifier.
   * @return the silverpeas instance identifier.
   */
  public String getInstanceId() {
    return this.instanceId;
  }

  /**
   * Gets the status of the delegated news.
   * @return the status.
   */
  public String getStatus() {
    return this.status;
  }

  /**
   * Gets the id of the contributor.
   * @return the contributor id.
   */
  public UserProfileEntity getContributor() {
    return this.contributor;
  }

  /**
   * Gets the id of the validator.
   * @return the contributor id.
   */
  public UserProfileEntity getValidator() {
    return this.validator;
  }

  /**
   * Gets the date at which the delegated news was validated.
   * @return the validator date of the delegated news.
   */
  public Date getValidationDate() {
    return validationDate;
  }

  /**
   * Gets the begin date of visibility of the delegated news.
   * @return the begin date.
   */
  public Date getBeginDate() {
    return beginDate;
  }

  /**
   * Gets the end date of visibility of the delegated news.
   * @return the end date.
   */
  public Date getEndDate() {
    return endDate;
  }

  /**
   * Gets the order of the delegated news.
   * @return the order of the delegated news.
   */
  public int getNewsOrder() {
    return this.newsOrder;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final DelegatedNewsEntity other = (DelegatedNewsEntity) obj;
    if (this.pubId != -1 && other.getPubId() != -1) {
      return this.pubId == other.getPubId();
    } else {
      if (!this.instanceId.equals(other.getInstanceId()) ||
          !this.status.equals(other.getStatus()) ||
          !this.contributor.equals(other.getContributor())) {
        return false;
      }
      if (!this.validator.equals(other.getValidator()) ||
          !this.validationDate.equals(other.getValidationDate()) ||
          this.newsOrder != other.getNewsOrder()) {
        return false;
      }
      return this.beginDate.equals(other.getBeginDate()) && this.endDate.equals(other.getEndDate());
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    if (pubId != -1) {
      hash = 17 * hash + Integer.toString(this.pubId).hashCode();
    } else {
      hash = 17 * hash + (this.instanceId != null ? this.instanceId.hashCode() : 0);
      hash = 17 * hash + (this.contributor != null ? this.contributor.hashCode() : 0);
      hash = 17 * hash + (this.validator != null ? this.validator.hashCode() : 0);
      hash = 17 * hash + (this.validationDate != null ? this.validationDate.hashCode() : 0);
      hash = 17 * hash + (this.beginDate != null ? this.beginDate.hashCode() : 0);
      hash = 17 * hash + (this.endDate != null ? this.endDate.hashCode() : 0);
      hash = 17 * hash + (this.newsOrder != -1 ? Integer.toString(this.newsOrder).hashCode() : 0);
    }
    return hash;
  }

  /**
   * Gets the delegated news business objet that this entity represents.
   * @return a delegated news instance.
   */
  public DelegatedNews toDelegatedNews() {
    return
        new DelegatedNews(this.pubId, this.instanceId, this.contributor.getId(),
            this.validationDate, this.beginDate, this.endDate);
  }

  /**
   * Creates a new delegated news entity from the specified delegated news.
   * @param delegatedNews the delegated news to entitify.
   * @return the entity representing the specified delegated news.
   */
  public static DelegatedNewsEntity fromDelegatedNews(final DelegatedNews delegatedNews) {
    return new DelegatedNewsEntity(delegatedNews);
  }
}
