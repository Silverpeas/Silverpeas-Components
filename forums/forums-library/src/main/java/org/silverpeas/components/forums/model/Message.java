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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.forums.model;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.WithAttachment;
import org.silverpeas.core.contribution.rating.model.ContributionRating;
import org.silverpeas.core.contribution.rating.model.ContributionRatingPK;
import org.silverpeas.core.contribution.rating.model.Rateable;
import org.silverpeas.core.contribution.rating.service.RatingService;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Message implements Contribution, Rateable, Serializable, WithAttachment {
  private static final String TYPE = "forum_message";

  public static final String STATUS_VALIDATE = "V";
  public static final String STATUS_FOR_VALIDATION = "A";
  public static final String STATUS_REFUSED = "R";

  public static final String RESOURCE_TYPE = "ForumMessage";

  private static final long serialVersionUID = 705520417746270396L;
  private int id;
  private String title;
  private String author;
  private Date date;
  private int forumId;
  private int parentId;
  private String text;
  private String instanceId;
  private MessagePK pk;
  private String status;
  private ContributionRating contributionRating;

  public Message(int id, String instanceId, String title, String author, Date date, int forumId,
      int parentId) {
    this.id = id;
    this.title = title;
    this.author = author;
    this.date = date;
    this.forumId = forumId;
    this.parentId = parentId;
    this.instanceId = instanceId;
  }

  public int getId() {
    return id;
  }

  public String getIdAsString() {
    return String.valueOf(id);
  }

  public void setId(int id) {
    this.id = id;
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return ContributionIdentifier.from(getInstanceId(), getIdAsString(), getContributionType());
  }

  @Override
  public User getCreator() {
    return User.getById(getAuthor());
  }

  @Override
  public Date getCreationDate() {
    return getDate();
  }

  @Override
  public User getLastUpdater() {
    return getCreator();
  }

  @Override
  public Date getLastUpdateDate() {
    return getDate();
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return false;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public int getForumId() {
    return forumId;
  }

  public String getForumIdAsString() {
    return String.valueOf(forumId);
  }

  public void setForumId(int forumId) {
    this.forumId = forumId;
  }

  public int getParentId() {
    return parentId;
  }

  public String getParentIdAsString() {
    return String.valueOf(parentId);
  }

  public void setParentId(int parentId) {
    this.parentId = parentId;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public MessagePK getPk() {
    return pk;
  }

  public void setPk(MessagePK pk) {
    this.pk = pk;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Indicates if the message is a subject (first message of a forum) or a message.
   * @return true if this message is the first one, false otherwise.
   */
  public boolean isSubject() {
    return (parentId == 0);
  }

  public boolean isValid() {
    return STATUS_VALIDATE.equals(status);
  }

  public boolean isToBeValidated() {
    return STATUS_FOR_VALIDATION.equals(status);
  }

   public boolean isRefused() {
    return STATUS_REFUSED.equals(status);
  }

  /**
   * The type of this resource
   * @return the same value returned by getContributionType()
   */
  public static String getResourceType() {
    return TYPE;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Message message = (Message) o;

    if (forumId != message.forumId) {
      return false;
    }
    if (id != message.id) {
      return false;
    }
    if (parentId != message.parentId) {
      return false;
    }
    if (!Objects.equals(author, message.author)) {
      return false;
    }
    if (!Objects.equals(date, message.date)) {
      return false;
    }
    if (!Objects.equals(instanceId, message.instanceId)) {
      return false;
    }
    if (!Objects.equals(pk, message.pk)) {
      return false;
    }
    if (!Objects.equals(status, message.status)) {
      return false;
    }
    if (!Objects.equals(text, message.text)) {
      return false;
    }
    return Objects.equals(title, message.title);
  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (author != null ? author.hashCode() : 0);
    result = 31 * result + (date != null ? date.hashCode() : 0);
    result = 31 * result + forumId;
    result = 31 * result + parentId;
    result = 31 * result + (text != null ? text.hashCode() : 0);
    result = 31 * result + (instanceId != null ? instanceId.hashCode() : 0);
    result = 31 * result + (pk != null ? pk.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Message{" + "id=" + id + ", title=" + title + ", author=" + author + ", date=" + date +
        ", forumId=" + forumId + ", parentId=" + parentId + ", text=" + text + ", instanceId=" +
        instanceId + ", pk=" + pk + ", status=" + status + '}';
  }

  @Override
  public ContributionRating getRating() {
    if (contributionRating == null) {
      contributionRating = RatingService.get()
          .getRating(
              new ContributionRatingPK(String.valueOf(getId()), getInstanceId(), RESOURCE_TYPE));
    }
    return contributionRating;
  }
}