/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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

package com.stratelia.webactiv.almanach.control;

/**
 * A DTO on an almanach instance.
 * It is used to transfer almanach instance data to the view page.
 * By default, the almanach instance isn't an agregated one, so don't forget to set this option in the case
 * the DTO is about an agregated almanach.
 */
public class AlmanachDTO {

  private String spaceId = "";
  private String instanceId = "";
  private String url = "";
  private String label = "";
  private String color = "";
  private boolean aggregated = false;

  /**
   * Gets the color with which the events of this almanach should be displayed in a calendar.
   * @return the color definition.
   */
  public String getColor() {
    return color;
  }

  /**
   * Sets the color with which the events of this almanach should be displayed in a calendar.
   * @param color the color of this almanach's events.
   * @return the almanach DTO itself.
   */
  public AlmanachDTO setColor(String color) {
    this.color = color;
    return this;
  }

  /**
   * Gets the unique identifier of the almanach instance.
   * @return the almanach instance identifier.
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * Sets the unique identifier of this almanach instance.
   * @param instanceId the almanach instance identifier.
   * @return the almanach DTO itself.
   */
  public AlmanachDTO setInstanceId(String instanceId) {
    this.instanceId = instanceId;
    return this;
  }

  /**
   * Gets the identifier of the workspace into which the almanach is defined.
   * @return the workspace identifier.
   */
  public String getSpaceId() {
    return spaceId;
  }

  /**
   * Sets the workspace into which this almanach is defined.
   * @param spaceId the identifier of the workspace.
   * @return itself;
   */
  public AlmanachDTO setSpaceId(final String spaceId) {
    this.spaceId = spaceId;
    return this;
  }

  /**
   * Is this almanach an agregated one?
   * @return true if this almanach is an agregated one, false otherwise.
   */
  public boolean isAgregated() {
    return aggregated;
  }

  /**
   * Indicates whether this almanach is an aggregated one.
   * @param isAggregated true is this almanach an agregated one, false otherwise.
   * @return the almanach DTO itself.
   */
  public AlmanachDTO setAggregated(boolean isAggregated) {
    this.aggregated = isAggregated;
    return this;
  }

  /**
   * Gets the label of the almanach.
   * @return almanach label.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets a label to this almanach.
   * @param label the almanach label.
   * @return the almanach DTO itself.
   */
  public AlmanachDTO setLabel(String label) {
    this.label = label;
    return this;
  }

  /**
   * Gets the URL of this almanach instance.
   * @return the URL of the almanach instance.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the URL of the almanach instance.
   * @param url the almanach instance URL.
   * @return itself.
   */
  public AlmanachDTO setUrl(final String url) {
    this.url = url;
    return this;
  }

}
