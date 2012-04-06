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
 * "http://www.silverpeas.org/legal/licensing"
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

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * TopicSearch represents a kmelia specific topic search entity
 * @author ebonnet
 */
@Entity
@Table(name = "sc_kmelia_search")
public class TopicSearch extends AbstractPersistable<Long> {

  private static final long serialVersionUID = 2162863596852109037L;

  private String instanceId;
  private Integer topicId;
  private Integer userId;
  private String language;
  private String query;
  private Date date;

  public TopicSearch() {
    this(null);
  }

  /**
   * Creates a new TopicSearch instance
   * @param id topic search identifier
   */
  public TopicSearch(Long id) {
    this.setId(id);
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
    this(null);
    this.instanceId = instanceId;
    this.topicId = topicId;
    this.userId = userId;
    this.language = language;
    this.query = query;
    this.date = date;
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
  public Date getDate() {
    return date;
  }

  /**
   * @param date the date to set
   */
  public void setDate(Date date) {
    this.date = date;
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

}
