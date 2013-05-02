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

import com.silverpeas.web.Exposable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.resourcemanager.model.Category;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * Web entity abstraction which provides category informations of the entity
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ResourceCategoryEntity implements Exposable {
  private static final long serialVersionUID = 2119850706264066092L;

  @XmlElement(defaultValue = "")
  private URI uri;

  @XmlElement
  private final Long id;

  @XmlElement(required = true)
  @NotNull
  @Size(min = 1)
  private final String name;

  @XmlElement(defaultValue = "")
  private final String description;

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public ResourceCategoryEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Creates a new category entity from the specified category.
   * @param category
   * @return the entity representing the specified category.
   */
  public static ResourceCategoryEntity createFrom(final Category category) {
    return new ResourceCategoryEntity(category);
  }

  protected ResourceCategoryEntity(final Category category) {
    this.id = category.getId();
    this.name = category.getName();
    this.description = category.getDescription();
  }

  protected ResourceCategoryEntity() {
    id = null;
    name = "";
    description = "";
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.Exposable#getURI()
   */
  @Override
  public URI getURI() {
    return uri;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(id).append(id).append(name).append(description)
        .toHashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }
    final ResourceCategoryEntity other = (ResourceCategoryEntity) obj;
    return new EqualsBuilder().append(id, other.id).append(name, other.name)
        .append(description, other.description).isEquals();
  }
}
