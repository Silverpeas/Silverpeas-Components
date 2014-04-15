/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.components.suggestionbox.web;

import com.silverpeas.web.Exposable;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.validation.web.ContributionValidationEntity;
import org.springframework.util.ReflectionUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Date;

/**
 * It represents the state of a suggestion in a suggestion box as transmitted within the body of
 * an HTTP response or an HTTP request.
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class SuggestionEntity implements Exposable {

  private static final long serialVersionUID = 4234619816264612213L;

  public static SuggestionEntity fromSuggestion(final Suggestion suggestion) {
    return new SuggestionEntity().decorate(suggestion);
  }

  private URI uri;
  private Suggestion suggestion;
  private ContributionValidationEntity validationEntity;

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public SuggestionEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  @Override
  @XmlElement(defaultValue = "")
  public URI getURI() {
    return uri;
  }

  @XmlElement
  public String getId() {
    return suggestion.getId();
  }

  @XmlElement
  public String getTitle() {
    return suggestion.getTitle();
  }

  @XmlElement
  public String getContent() {
    return suggestion.getContent();
  }

  @XmlElement
  public String getAuthorName() {
    return suggestion.getCreator().getDisplayedName();
  }

  @XmlElement
  public int getCommentCount() {
    return suggestion.getCommentCount();
  }

  @XmlElement
  public Date getCreateDate() {
    return suggestion.getCreateDate();
  }

  @XmlElement
  public Date getLastUpdateDate() {
    return suggestion.getLastUpdateDate();
  }

  @XmlElement(nillable = true)
  public String getContributionType() {
    return suggestion.getContributionType();
  }

  @XmlTransient
  public boolean isPublishableBy(UserDetail user) {
    return suggestion.isPublishableBy(user);
  }

  @XmlElement
  public ContributionValidationEntity getValidation() {
    if (validationEntity == null) {
      validationEntity = ContributionValidationEntity.fromContributionValidation(suggestion.
          getValidation());
    }
    return validationEntity;
  }

  @XmlTransient
  public UserDetail getAuthor() {
    return suggestion.getCreator();
  }

  protected void setValidation(ContributionValidationEntity validation) {
    validationEntity = validation;
  }

  protected void setURI(final URI uri) {
    withURI(uri);
  }

  protected void setTitle(String title) {
    suggestion.setTitle(title);
  }

  protected void setContent(String content) {
    suggestion.setContent(content);
  }

  protected void setCommentCount(int count) {
  }

  protected void setContributionType(String type) {

  }

  protected void setAuthorName(String author) {

  }

  protected void setCreateDate(Date validationDate) {

  }

  protected void setApprobationDate(final String dateInISO8601) {

  }

  protected void setId(String id) {
    try {
      Field idField = ReflectionUtils.findField(Suggestion.class, "id");
      idField.setAccessible(true);
      ReflectionUtils.setField(idField, suggestion, new UuidIdentifier().fromString(id));
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  protected SuggestionEntity decorate(final Suggestion suggestion) {
    this.suggestion = suggestion;
    return this;
  }

  protected SuggestionEntity() {
    this.suggestion = new Suggestion("");
  }

  @Override
  public String toString() {
    return "SuggestionEntity{" + "uri=" + uri + ", id=" + getId() + ", title=" + getTitle()
        + ", content=" + getContent() + ", validation=" + getValidation() + '}';
  }
}
