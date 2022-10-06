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
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.rs.WebEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A collection of web entities representing each a membership to a community. As the collection can
 * be just a window over a part of some memberships, the total number of memberships answering the
 * request is indicated whereas the collection provides just a part of them.
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityMembershipEntities implements WebEntity {

  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(required = true)
  private long realSize;
  @XmlElement(required = true)
  private long size;
  @SuppressWarnings("FieldMayBeFinal")
  @XmlElement
  private List<CommunityMembershipEntity> memberships = new ArrayList<>();

  static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unused")
  protected CommunityMembershipEntities() {
    // required by the JSON/XML unmarshaller
  }

  protected CommunityMembershipEntities(final SilverpeasList<CommunityMembershipEntity> memberships) {
    this.realSize = memberships.originalListSize();
    this.size = memberships.size();
    this.memberships.addAll(memberships);
  }

  @Override
  public URI getURI() {
    return this.uri;
  }

  /**
   * Gets the total real count of memberships matching the request.
   * @return the real size of the collection, that is the size of the collection whether all the
   * members matching the request have been contained by it.
   */
  @SuppressWarnings("unused")
  public long getRealSize() {
    return this.realSize;
  }

  /**
   * Gets the actual count of memberships contained in this collection.
   * @return the actual size of this collection. If this collection contains all the memberships
   * matching the request, then his actual size is equal to his real size.
   */
  @SuppressWarnings("unused")
  public long getSize() {
    return size;
  }

  /**
   * Gets all the memberships contained in this collection.
   * @return the memberships (or a port of them) matching the request.
   */
  @SuppressWarnings("unused")
  public List<CommunityMembershipEntity> getMemberships() {
    return memberships;
  }

  static class Builder {

    private SilverpeasList<CommunityMembershipEntity> memberships;
    private CommunityWebResourceURIBuilder uriBuilder;

    Builder with(final SilverpeasList<CommunityMembershipEntity> memberships) {
      this.memberships = memberships;
      return this;
    }

    Builder with(final CommunityWebResourceURIBuilder uriBuilder) {
      this.uriBuilder = uriBuilder;
      return this;
    }

    CommunityMembershipEntities build() {
      Objects.requireNonNull(this.memberships);
      CommunityMembershipEntities entities = new CommunityMembershipEntities(this.memberships);
      if (uriBuilder != null) {
        entities.uri = uriBuilder.getCurrentResourceURI();
      }
      return entities;
    }
  }
}
