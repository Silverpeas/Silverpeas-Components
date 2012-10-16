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

package com.silverpeas.kmelia.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * TopicSearch represents a kmelia specific topic search entity
 * @author ebonnet
 */
@Entity
@Table(name = "sc_kmelia_search")
public class TopicSearch implements Serializable{

  private static final long serialVersionUID = 2162863596852109037L;

  @Id
  @TableGenerator(name = "UNIQUE_ID_GEN", table = "uniqueId", pkColumnName = "tablename",
  valueColumnName = "maxId", pkColumnValue = "sc_kmelia_search", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "UNIQUE_ID_GEN")
  private Long id;
  
  private String instanceId;
  private Integer topicId;
  private Integer userId;
  private String language;
  private String query;
  private Date searchDate;

  protected TopicSearch() {
  }

  /**
   * Creates a new TopicSearch instance
   * @param id topic search identifier
   */
  public TopicSearch(Long id) {
    this.id = id;
  }

  /**
   * @param instanceId
   * @param topicId
   * @param userId
   * @param language
   * @param query
   * @param date
   */
  public TopicSearch(String instanceId, Integer topicId, Integer userId, String language,
      String query, Date date) {
    this.instanceId = instanceId;
    this.topicId = topicId;
    this.userId = userId;
    this.language = language;
    this.query = query;
    this.searchDate = date;
  }

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
  public int getTopicId() {
    return topicId;
  }

  /**
   * @param topicId the topicId to set
   */
  public void setTopicId(Integer topicId) {
    this.topicId = topicId;
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
  public String getQuery() {
    return query;
  }

  /**
   * @param query the query to set
   */
  public void setQuery(String query) {
    this.query = query;
  }

  /**
   * @return the date
   */
  public Date getSearchDate() {
    return searchDate;
  }

  /**
   * @param searchDate the date to set
   */
  public void setSearchDate(Date searchDate) {
    this.searchDate = searchDate;
  }

  /**
   * @return the userId
   */
  public Integer getUserId() {
    return userId;
  }

  /**
   * @param userId the userId to set
   */
  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
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
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (id != null ? id.hashCode() : 0);
    return hash;
  }
  
  @Override
  public String toString() {
    return "com.silverpeas.kmelia.domain.TopicSearch[ id=" + id + " ] ";
  }
}
