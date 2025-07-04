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

import java.util.HashSet;
import java.util.Set;

/**
 * A field of the classified form used by the search and on which also a user can subscribe for a
 * given value to be notified about the classifieds having such value for the field.
 *
 * @author mmoquillon
 */
public class SubscriptionField {
  private final String key;
  private final String label;
  private final Set<SubscriptionFieldValue> values = new HashSet<>();

  public SubscriptionField(String key, String label) {
    this.key = key;
    this.label = label;
  }

  public SubscriptionField valuedBy(String valueKey, String valueLabel) {
    values.add(new SubscriptionFieldValue(valueKey, valueLabel));
    return this;
  }

  public String getKey() {
    return key;
  }

  public String getLabel() {
    return label;
  }

  public Set<SubscriptionFieldValue> getValues() {
    return values;
  }
}
  