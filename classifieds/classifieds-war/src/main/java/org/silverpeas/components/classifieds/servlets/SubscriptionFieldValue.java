/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.classifieds.servlets;

/**
 * Value of a field of the classified form on which a user can subscribe to be notified about the
 * classifieds having such value for the field.
 *
 * @author mmoquillon
 */
public class SubscriptionFieldValue implements Comparable<SubscriptionFieldValue> {

  private final String key;
  private final String value;

  /**
   * Constructs a new {@link SubscriptionFieldValue} instance.
   *
   * @param key the key identified uniquely of the field value.
   * @param value a value of the field.
   */
  SubscriptionFieldValue(String key, String value) {
    this.key = key;
    this.value = value;
  }

  /**
   * Gets the unique identifier of the field value.
   * @return the unique identifier of the field value.
   */
  public String getKey() {
    return key;
  }

  /**
   * Gets the value of the field.
   * @return the value of the field.
   */
  public String getValue() {
    return value;
  }

  @Override
  public int compareTo(SubscriptionFieldValue o) {
    return getKey().compareToIgnoreCase(o.getKey());
  }
}
  