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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.components.community.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.web.rs.WebEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.Objects;

/**
 * The user related by a membership to a community.
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntity implements WebEntity {

  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(required = true)
  private String firstName;
  @XmlElement(required = true)
  private String lastName;

  static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unused")
  protected UserEntity() {
    // required by the JSON/XML unmarshaller
  }

  private UserEntity(User user) {
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
  }

  /**
   * Gets the URI at which additional information about this user can be got.
   * @return the URI of the profile of this user in Silverpeas.
   */
  @XmlElement(name = "uri")
  @Override
  public URI getURI() {
    return this.uri;
  }

  /**
   * Gets the first name of this user.
   * @return the user firstname
   */
  @SuppressWarnings("unused")
  public String getFirstName() {
    return firstName;
  }

  /**
   * Gets the last name of this user.
   * @return the user lastname.
   */
  @SuppressWarnings("unused")
  public String getLastName() {
    return lastName;
  }

  static class Builder {

    User user;
    CommunityWebResourceURIBuilder uriBuilder;

    Builder with(final User user) {
      this.user = user;
      return this;
    }

    Builder with(final CommunityWebResourceURIBuilder uriBuilder) {
      this.uriBuilder = uriBuilder;
      return this;
    }

    UserEntity build() {
      Objects.requireNonNull(this.user);
      UserEntity entity = new UserEntity(this.user);
      if (uriBuilder != null) {
        entity.uri = uriBuilder.getUserURI(this.user);
      }
      return entity;
    }
  }
}
