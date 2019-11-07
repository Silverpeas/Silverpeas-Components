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

package org.silverpeas.components.delegatednews.model;

import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.PublicationRuntimeException;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.persistence.datasource.model.identifier.ExternalIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "sc_delegatednews_news")
@AttributeOverride(name = "id", column = @Column(name = "pubId", columnDefinition = "int"))
@NamedQueries({
    @NamedQuery(name = "delegatednews.findByStatus", query = "SELECT dn FROM DelegatedNews dn " +
        "WHERE dn.status = :status " +
        "AND (" + "(dn.beginDate is null AND dn.endDate is null) " +
        "OR " +
        "(dn.beginDate is null AND dn.endDate is not null AND CURRENT_TIMESTAMP <= dn.endDate) " +
        "OR " +
        "(dn.beginDate is not null AND dn.endDate is null AND CURRENT_TIMESTAMP >= dn.beginDate) " +
        "OR " +
        "(dn.beginDate is not null AND dn.endDate is not null AND CURRENT_TIMESTAMP >= dn" +
        ".beginDate AND CURRENT_TIMESTAMP <= dn.endDate) " + ")" + "ORDER BY dn.newsOrder ASC"),
    @NamedQuery(name = "delegatednews.findAllOrderedNews", query =
        "SELECT dn FROM DelegatedNews dn " +
            "ORDER BY dn.newsOrder ASC, dn.beginDate ASC, dn.id.id ASC")})
public class DelegatedNews extends BasicJpaEntity<DelegatedNews, ExternalIntegerIdentifier>
    implements Serializable {
  //TODO replace UniqueIntegerIdentifier with ExternalIntegerIdentifier (to create)
  // demander demain ce qu'il en est de l'identifiant de la table sc_delegatednews_news.

  private static final long serialVersionUID = 9192830552642027995L;

  @Column(name = "instanceId")
  private String instanceId;
  @Column(name = "status")
  private String status;
  @Column(name = "contributorId")
  private String contributorId;
  @Column(name = "validatorId")
  private String validatorId;
  @Column(name = "validationDate", columnDefinition = "TIMESTAMP")
  private Date validationDate;
  @Column(name = "beginDate", columnDefinition = "TIMESTAMP")
  private Date beginDate;
  @Column(name = "endDate", columnDefinition = "TIMESTAMP")
  private Date endDate;
  @Column(name = "newsOrder")
  private int newsOrder = 0;

  public static final String NEWS_TO_VALIDATE = "ToValidate";
  public static final String NEWS_VALID = "Valid";
  public static final String NEWS_REFUSED = "Refused";

  public DelegatedNews() {

  }

  public DelegatedNews(int pubId, String instanceId, String contributorId, Date validationDate,
      Date beginDate, Date endDate) {
    super();
    setId(Integer.toString(pubId));
    this.instanceId = instanceId;
    this.status = NEWS_TO_VALIDATE;
    this.contributorId = contributorId;
    if (validationDate != null) {
      this.validationDate = new Date(validationDate.getTime());
    }
    if (beginDate != null) {
      this.beginDate = new Date(beginDate.getTime());
    }
    if (endDate != null) {
      this.endDate = new Date(endDate.getTime());
    }
  }

  public int getPubId() {
    return Integer.parseInt(getId());
  }

  public void setPubId(int pubId) {
    setId(Integer.toString(pubId));
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getContributorId() {
    return contributorId;
  }

  public void setContributorId(String contributorId) {
    this.contributorId = contributorId;
  }

  public String getValidatorId() {
    return validatorId;
  }

  public void setValidatorId(String validatorId) {
    this.validatorId = validatorId;
  }

  public Date getValidationDate() {
    return validationDate;
  }

  public void setValidationDate(Date validationDate) {
    this.validationDate = validationDate;
  }

  public Date getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(Date beginDate) {
    this.beginDate = beginDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public int getNewsOrder() {
    return newsOrder;
  }

  public void setNewsOrder(int newsOrder) {
    this.newsOrder = newsOrder;
  }

  public boolean isValidated() {
    return DelegatedNews.NEWS_VALID.equals(getStatus());
  }

  public boolean isDenied() {
    return DelegatedNews.NEWS_REFUSED.equals(getStatus());
  }

  public boolean isWaitingForValidation() {
    return DelegatedNews.NEWS_TO_VALIDATE.equals(getStatus());
  }

  public PublicationDetail getPublicationDetail() {
    try {
      PublicationService publicationService = PublicationService.get();
      PublicationPK pubPk = new PublicationPK(getId(), this.instanceId);
      return publicationService.getDetail(pubPk);
    } catch (Exception e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DelegatedNews other = (DelegatedNews) obj;
    if (this.getPubId() != other.getPubId()) {
      return false;
    }
    if (!Objects.equals(this.instanceId, other.instanceId)) {
      return false;
    }
    if (!Objects.equals(this.status, other.status)) {
      return false;
    }
    if (!Objects.equals(this.contributorId, other.contributorId)) {
      return false;
    }
    if (!Objects.equals(this.validatorId, other.validatorId)) {
      return false;
    }
    if (!Objects.equals(this.validationDate, other.validationDate)) {
      return false;
    }
    if (!Objects.equals(this.beginDate, other.beginDate)) {
      return false;
    }
    if (!Objects.equals(this.endDate, other.endDate)) {
      return false;
    }
    return this.newsOrder == -1 ? other.newsOrder == -1 : this.newsOrder == other.newsOrder;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getPubId();
    result = prime * result + ((instanceId == null) ? 0 : instanceId.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result = prime * result + ((contributorId == null) ? 0 : contributorId.hashCode());
    result = prime * result + ((validatorId == null) ? 0 : validatorId.hashCode());
    result = prime * result + ((validationDate == null) ? 0 : validationDate.hashCode());
    result = prime * result + ((beginDate == null) ? 0 : beginDate.hashCode());
    result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
    result = prime * result + newsOrder;
    return result;
  }


  @Override
  public String toString() {
    return "DelegatedNews {" + "pubId=" + getPubId() + ", instanceId=" + instanceId + ", status=" +
        status + ", contributorId=" + contributorId + ", validatorId=" + validatorId +
        ", validationDate=" + validationDate + ", beginDate=" + beginDate +
        ", endDate=" + endDate + ", newsOrder=" + newsOrder + '}';
  }

}
