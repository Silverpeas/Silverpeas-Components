/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.resourcesmanager.web;

import org.silverpeas.resourcemanager.model.Resource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * Web entity abstraction which provides common event informations of the entity
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ReservedResourceEntity extends ResourceEntity {
  private static final long serialVersionUID = -8207430778577137434L;

  @XmlElement(defaultValue = "")
  private URI resourceURI;

  @XmlElement(defaultValue = "")
  private URI reservationURI;

  @XmlElement(defaultValue = "")
  private final String status;

  @Override
  public ReservedResourceEntity withURI(final URI uri) {
    resourceURI = uri;
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T extends ResourceEntity> T withReservationURI(final URI reservationURI) {
    this.reservationURI = reservationURI;
    return (T) this;
  }

  /**
   * Creates a new resource entity from the specified resource.
   * @param resource
   * @return the entity representing the specified resource.
   */
  public static ReservedResourceEntity createFrom(final Resource resource) {
    return new ReservedResourceEntity(resource);
  }

  protected ReservedResourceEntity(final Resource resource) {
    super(resource);
    status = resource.getStatus();
  }

  protected ReservedResourceEntity() {
    super();
    status = "";
  }

  public URI getResourceURI() {
    return resourceURI;
  }

  public URI getReservationURI() {
    return reservationURI;
  }

  public String getStatus() {
    return status;
  }
}
