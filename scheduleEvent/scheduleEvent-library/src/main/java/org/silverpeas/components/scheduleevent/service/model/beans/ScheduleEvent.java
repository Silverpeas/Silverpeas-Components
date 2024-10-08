/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.scheduleevent.service.model.beans;

import org.silverpeas.components.scheduleevent.constant.ScheduleEventConstant;
import org.silverpeas.components.scheduleevent.service.model.ScheduleEventBean;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.SilverpeasToolContent;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Entity
@Table(name = "sc_scheduleevent_list")
@NamedQuery(name = "findByAuthor", query =
    "select e from ScheduleEvent e where e.author = :authorId")
@NamedQuery(name = "findByContributor", query =
    "select e from Contributor c join c.scheduleEvent e where c.userId = :contributorId")
public class ScheduleEvent implements SilverpeasToolContent, ScheduleEventBean, Serializable {

  private static final long serialVersionUID = 1L;
  public static final String TYPE = "ScheduleEvent";
  @Id
  private String id;
  private String title;
  private String description;
  @Temporal(javax.persistence.TemporalType.TIMESTAMP)
  private Date creationDate;
  @Column(name = "creatorid")
  private int author;
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "scheduleeventid", nullable = false)
  @OrderBy("day, hour ASC")
  private final Set<DateOption> dates = new TreeSet<>();
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy
      = "scheduleEvent")
  private final Set<Contributor> contributors = new HashSet<>();
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy
      = "scheduleEvent")
  private final Set<Response> responses = new HashSet<>();
  private int status;

  @PrePersist
  protected void setUpId() {
    id = UUID.randomUUID().toString();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  @Override
  public User getLastUpdater() {
    return getCreator();
  }

  @Override
  public Date getLastUpdateDate() {
    return getCreationDate();
  }

  @Override
  public int getAuthor() {
    return author;
  }

  @Override
  public void setAuthor(int author) {
    this.author = author;
  }

  @Override
  public Set<DateOption> getDates() {
    return dates;
  }

  @Override
  public Set<Contributor> getContributors() {
    return contributors;
  }

  @Override
  public Set<Response> getResponses() {
    return responses;
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public void setStatus(int status) {
    this.status = status;
  }

  @Override
  public String getComponentInstanceId() {
    return ScheduleEventConstant.TOOL_ID;
  }

  @Override
  public User getCreator() {
    return User.getById(Integer.toString(getAuthor()));
  }

  @Override
  public String getContributionType() {
    return TYPE;
  }

  /**
   * Is the specified user can access this content?
   * <p/>
   * The user can access this event if it is either the author or a contributor of this event.
   *
   * @param user a user in Silverpeas.
   * @return true if the tool belongs to the personal workspace of the specified user, false
   * otherwise.
   */
  @Override
  public boolean canBeAccessedBy(final User user) {
    boolean isContributor = user.getId().equals(String.valueOf(getAuthor()));
    for (Iterator<Contributor> it = getContributors().iterator(); it.hasNext() && !isContributor;) {
      isContributor = user.getId().equals(String.valueOf(it.next().getUserId()));
    }
    return isContributor;
  }

  /**
   * The type of this resource
   *
   * @return the same value returned by getContributionType()
   */
  public static String getResourceType() {
    return TYPE;
  }

  @Override
  public String getURL() {
    return "/Rscheduleevent/jsp/Detail?scheduleEventId=" + getId();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ScheduleEvent other = (ScheduleEvent) obj;
    if (id == null) {
      return other.id == null;
    } else return id.equals(other.id);
  }
}
