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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.processmanager;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A named value pair.
 */
public final class NamedValue implements Serializable {

  private static final long serialVersionUID = 95974461450918272L;

  private final String name;
  private final String value;
  private final boolean creationOne;

  static Comparator<NamedValue> ascendingValues = (o1, o2) -> {
    if (o1.value == null) {
      return (o2.value == null) ? 0 : (-1);
    }
    return o1.value.compareTo(o2.value);
  };

  NamedValue(String name, String value, final boolean creationOne) {
    this.name = name;
    this.value = value;
    this.creationOne = creationOne;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public boolean isCreationOne() {
    return creationOne;
  }

  public boolean equals(Object o) {
    return o instanceof NamedValue && name.equals(((NamedValue) o).name);
  }

  public int hashCode() {
    return name.hashCode();
  }
}
