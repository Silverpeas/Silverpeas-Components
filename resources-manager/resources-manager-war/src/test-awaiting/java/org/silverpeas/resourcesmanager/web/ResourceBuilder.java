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
import org.silverpeas.resourcemanager.model.ResourceStatus;

/**
 * @author Yohann Chastagnier
 */
public class ResourceBuilder {

  public static ResourceBuilder getResourceBuilder() {
    return new ResourceBuilder();
  }

  public Resource buildResource(final Long resourceId) {
    return new ResourceMock(resourceId);
  }

  private ResourceBuilder() {
    // Nothing to do
  }

  protected class ResourceMock extends Resource {

    public ResourceMock(final Long resourceId) {
      super();
      setId(resourceId);
      setName("resource-name" + resourceId);
      setDescription("resource-description" + resourceId);
      setStatus(ResourceStatus.STATUS_FOR_VALIDATION);
      setCategory(CategoryBuilder.getCategoryBuilder().buildCategory(10 * resourceId));
    }
  }
}
