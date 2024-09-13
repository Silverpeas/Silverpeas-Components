/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.kmelia.model;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * TopicSearch represents a kmelia specific topic search entity
 * @author ebonnet
 */
@Entity
@Table(name = "sc_kmelia_search")
@NamedQuery(name = "topicSearch.findByInstanceId",
    query = "SELECT ts FROM TopicSearch ts WHERE ts.instanceId = :instanceId")
public class TopicSearch extends BasicJpaEntity<TopicSearch, UniqueLongIdentifier>
    implements Serializable {

  private static final long serialVersionUID = 2162863596852109037L;

  private String instanceId;
  private Integer topicId;
  private Integer userId;
  private String language;
  private String query;
  private Date searchDate;

  /**
   * @return the instanceId
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * @param instanceId the instanceId to set
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * @return the topicId
   */
  @SuppressWarnings("unused")
  public String getTopicId() {
    return topicId.toString();
  }

  /**
   * @param topicId the topicId to set
   */
  @SuppressWarnings("unused")
  public void setTopicId(String topicId) {
    this.topicId = Integer.valueOf(topicId);
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * @return the query
   */
  @SuppressWarnings("unused")
  public String getQuery() {
    return query;
  }

  /**
   * @param query the query to set
   */
  @SuppressWarnings("unused")
  public void setQuery(String query) {
    this.query = query;
  }

  /**
   * @return the date
   */
  @SuppressWarnings("unused")
  public Date getSearchDate() {
    return searchDate;
  }

  /**
   * @param searchDate the date to set
   */
  @SuppressWarnings("unused")
  public void setSearchDate(Date searchDate) {
    this.searchDate = searchDate;
  }

  /**
   * @return the userId
   */
  public String getUserId() {
    return userId.toString();
  }

  /**
   * @param userId the userId to set
   */
  public void setUserId(String userId) {
    this.userId = Integer.valueOf(userId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TopicSearch other = (TopicSearch) obj;
    return this.getId().equals(other.getId()) ||
        (this.getId() != null && this.getId().equals(other.getId()));
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (this.getId() != null ? getId().hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "TopicSearch[ id=" + this.getId() + " ] ";
  }
}
