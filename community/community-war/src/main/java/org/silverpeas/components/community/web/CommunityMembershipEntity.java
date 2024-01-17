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
import org.silverpeas.components.community.model.CommunityMembership;
import org.silverpeas.components.community.model.MembershipStatus;
import org.silverpeas.core.web.rs.WebEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.Objects;

/**
 * Web representation of a membership as transmitted by the REST web resources.
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityMembershipEntity implements WebEntity {

  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(required = true)
  private URI community;
  @XmlElement(required = true)
  private UserEntity user;
  @XmlElement(required = true)
  private String status;

  static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unused")
  protected CommunityMembershipEntity() {
    // required by the JSON/XML unmarshaller
  }

  private CommunityMembershipEntity(final CommunityMembership membership) {
    status = membership.getStatus().toString();
  }

  @Override
  public URI getURI() {
    return this.uri;
  }

  /**
   * Gets the user related by this membership to a community.
   * @return a web entity representing a user in Silverpeas.
   */
  public UserEntity getUser() {
    return user;
  }

  /**
   * Gets the status of this membership.
   * @return the String value of one of the {@link MembershipStatus} enumeration value.
   */
  @SuppressWarnings("unused")
  public String getStatus() {
    return status;
  }

  /**
   * Gets the URI of the community of user to which this membership is related.
   * @return the URI of the community of users.
   */
  public URI getCommunity() {
    return community;
  }

  static class Builder {

    private CommunityMembership membership;
    private CommunityWebResourceURIBuilder uriBuilder;

    Builder with(final CommunityMembership membership) {
      this.membership = membership;
      return this;
    }

    Builder with(final CommunityWebResourceURIBuilder uriBuilder) {
      this.uriBuilder = uriBuilder;
      return this;
    }

    CommunityMembershipEntity build() {
      Objects.requireNonNull(this.membership);
      CommunityMembershipEntity entity = new CommunityMembershipEntity(this.membership);
      entity.user = UserEntity.builder()
          .with(this.membership.getUser())
          .with(uriBuilder)
          .build();
      if (uriBuilder != null) {
        entity.uri = uriBuilder.getMembershipURI(this.membership);
        entity.community = uriBuilder.getCommunityURI(this.membership.getCommunity());
      }
      return entity;
    }
  }
}
