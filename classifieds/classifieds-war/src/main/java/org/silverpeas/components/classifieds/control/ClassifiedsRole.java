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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.classifieds.control;

public enum ClassifiedsRole implements Comparable<ClassifiedsRole> {
  ANONYMOUS("anonymous"), READER("reader"), PUBLISHER("publisher"), MANAGER("admin");

  private String name = null;

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  ClassifiedsRole(String name) {
    this.name = name;
  }

  public static ClassifiedsRole getRole(String roleName) {
    for (ClassifiedsRole role : values()) {
      if (role.getName().equals(roleName)) {
        return role;
      }
    }

    return READER;
  }

  public static ClassifiedsRole getRole(String[] profiles) {
    ClassifiedsRole highestRole = READER;
    for (String profile : profiles) {
      ClassifiedsRole role = ClassifiedsRole.getRole(profile);
      if (role.compareTo(highestRole) > 0) {
        highestRole = role;
      }
    }

    return highestRole;
  }

}
