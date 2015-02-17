/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.rssAgregator.model;

public enum RSSViewType {
  
  /**
   * The AGREGATED RSS view.
   */
  AGREGATED("agregated"),
  /**
   * The SEPARATED RSS view
   */
  SEPARATED("separated");

  /**
   * Constructs a view type with the specified view mode.
   * @param viewMode the view mode as defined in the underlying calendar renderer.
   */
  private RSSViewType(final String viewMode) {
    this.rssView = viewMode;
  }
  private String rssView;
  
  /**
   * Converts this view type in a string representation.
   * The value of the string depends on the RSS view rendering engine. It should be a value
   * that matches the view mode supported by the underlying RSS renderer.
   * @return
   */
  @Override
  public String toString() {
    return this.rssView;
  }
}
