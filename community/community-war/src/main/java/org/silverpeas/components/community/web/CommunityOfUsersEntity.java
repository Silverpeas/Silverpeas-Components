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
 */
package org.silverpeas.components.community.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.core.admin.space.SpaceHomePageType;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.web.rs.WebEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

/**
 * It represents the Web entity of a {@link CommunityOfUsers} state from which a representation
 * (usually in JSON) can be generated.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityOfUsersEntity implements WebEntity {

  private static final long serialVersionUID = 1L;

  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(required = true)
  private String id;
  @XmlElement(required = true)
  private String spaceId;
  @XmlElement(defaultValue = "")
  private URI memberships;
  @XmlElement
  private String homePage;
  @XmlElement
  private Integer homePageType;
  @XmlElement
  private URL charterURL;

  /**
   * Gets a builder of a web entity representing a community of users.
   * @return a builder of a {@link CommunityOfUsersEntity} object.
   */
  static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unused")
  protected CommunityOfUsersEntity() {
    // required by the JSON/XML unmarshaller
  }

  private CommunityOfUsersEntity(final CommunityOfUsers community) {
    this.id = community.getComponentInstanceId();
    this.spaceId = community.getSpaceId();
    this.charterURL = community.getCharterURL();
    Pair<String, SpaceHomePageType> theHomePage = community.getHomePage();
    this.homePage = theHomePage.getFirst();
    this.homePageType = theHomePage.getSecond().ordinal();
  }

  @Override
  public URI getURI() {
    return this.uri;
  }

  /**
   * Gets the unique identifier of this community of users.
   * @return the unique identifier of the community application instance dedicated to manage the
   * community of users for a collaborative space.
   */
  public String getId() {
    return this.id;
  }

  /**
   * Gets the unique identifier of the collaborative space for which this community of users is. The
   * space for which the community has been defined is permanent, meaning it cannot be changed.
   * @return the unique identifier of the community space.
   */
  public String getSpaceId() {
    return this.spaceId;
  }

  /**
   * Gets the URI at which the memberships to this community can be requested.
   * @return the URI at which the memberships to this community can be accessed.
   */
  public URI getMemberships() {
    return this.memberships;
  }

  /**
   * Gets the home page of this community of users for the members.
   * @return the home page and its type.
   */
  public Pair<String, SpaceHomePageType> getHomePage() {
    return Pair.of(homePage, SpaceHomePageType.values()[homePageType]);
  }

  public URL getCharterURL() {
    return charterURL;
  }

  void setUri(final URI uri) {
    this.uri = uri;
  }

  void setMemberships(final URI memberships) {
    this.memberships = memberships;
  }

  static class Builder {
    private CommunityOfUsers community;
    private CommunityWebResourceURIBuilder uriBuilder;

    Builder with(CommunityOfUsers community) {
      this.community = community;
      return this;
    }

    Builder with(CommunityWebResourceURIBuilder uriBuilder) {
      this.uriBuilder = uriBuilder;
      return this;
    }

    CommunityOfUsersEntity build() {
      Objects.requireNonNull(this.community);
      CommunityOfUsersEntity entity = new CommunityOfUsersEntity(this.community);
      if (uriBuilder != null) {
        entity.setUri(uriBuilder.getCommunityURI(community));
        entity.setMemberships(uriBuilder.getCommunityMembershipsURI(community));
      }
      return entity;
    }
  }
}