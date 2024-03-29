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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.forums.model;

import org.silverpeas.core.WAPrimaryKey;

/**
 * Clé primaire associée à un message.
 * @author frageade
 * @since November 2000
 */
public class MessagePK extends WAPrimaryKey {
  /**
   * Generated serial version identifier
   */
  private static final long serialVersionUID = 1454596772173738407L;

  public MessagePK(String component, String id) {
    super(id, component);
  }

  public MessagePK(String component) {
    this(component, "0");
  }

  @Override
  public boolean equals(Object other) {
    return ((other instanceof MessagePK)
        && (getInstanceId().equals(((MessagePK) other).getInstanceId()))
        && (getId().equals(((MessagePK) other).getId())));
  }

  @Override
  public int hashCode() {
    int hash = 21;
    return hash * super.hashCode();
  }

}