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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.silverpeas.resourcemanager.model.Resource;

/**
 * @author Yohann Chastagnier
 */
public class ResourceEntityMatcher extends BaseMatcher<ResourceEntity> {

  private final Resource expected;

  protected ResourceEntityMatcher(final Resource expected) {
    this.expected = expected;
  }

  @Override
  public void describeTo(final Description description) {
    description.appendValue(expected);
  }

  /*
   * (non-Javadoc)
   * @see org.hamcrest.Matcher#matches(java.lang.Object)
   */
  @Override
  public boolean matches(final Object item) {
    boolean match = false;
    if (item instanceof ResourceEntity) {
      final ResourceEntity actual = (ResourceEntity) item;
      final EqualsBuilder matcher = new EqualsBuilder();
      matcher.appendSuper(actual.getURI().toString()
          .endsWith("/resourceManager/componentName5/resources/" + expected.getId()));
      matcher.appendSuper(actual.getCategoryURI().toString().endsWith(
          "/resourceManager/componentName5/resources/categories/" +
              expected.getCategory().getId()));
      matcher.append(expected.getId(), actual.getId());
      matcher.append(expected.getName(), actual.getName());
      matcher.append(expected.getDescription(), actual.getDescription());
      match = matcher.isEquals();
    }
    return match;
  }

  public static ResourceEntityMatcher matches(final Resource expected) {
    return new ResourceEntityMatcher(expected);
  }
}
