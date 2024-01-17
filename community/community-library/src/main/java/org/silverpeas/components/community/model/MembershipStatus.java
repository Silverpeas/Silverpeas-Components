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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.community.model;

/**
 * <p>
 * It defines the status of the membership of a user to a community of users. By its values, it
 * defines the different steps a membership can pass:
 * </p>
 * <ul>
 *   <li>first, a user asks to join a community of users. If the membership requires validation,
 *   the membership of the user is pending.</li>
 *   <li>If no validation is required for a membership application or if a pending membership is
 *   validated, the membership of the user to the community of users is then committed.</li>
 *   <li>In the case of a validation, the application for membership can be refused.</li>
 *   <li>After a time, the user can remove himself from the community of users (or he can be retired
 *   by an administrator), in this case his membership is removed.</li>
 * </ul>
 * @author mmoquillon
 */
public enum MembershipStatus {

  /**
   * The user asked to join the community of users and his membership is pending, waiting for
   * validation by a validator.
   */
  PENDING,
  /**
   * The user is currently member of the community of users.
   */
  COMMITTED,
  /**
   * The asking of a user to join the community of users has been refused by a validator. The
   * membership of the user to the community is stated as refused.
   */
  REFUSED,
  /**
   * The user isn't anymore a member of the community of users. He either retired himself or he has
   * been removed from the community. His membership isn't deleted, just the status is updated.
   */
  REMOVED;

  /**
   * Is the membership to the community of user pending for validation by a validator?
   * @return true if the membership of the user to the community is waiting for validation. False
   * otherwise.
   */
  public boolean isPending() {
    return this == PENDING;
  }

  /**
   * Is the membership to the community of users is committed?
   * @return true if the user is an actual member of the community. False otherwise.
   */
  public boolean isMember() {
    return this == COMMITTED;
  }

  /**
   * Is the membership to the community of users has been refused?
   * @return true if the membership asking of the user has been refused by a validator. False
   * otherwise.
   */
  public boolean isRefused() {
    return this == REFUSED;
  }

  /**
   * Is the membership to the community of users has been removed?
   * @return true if the user isn't anymore a member of the community. False otherwise.
   */
  public boolean isNoMoreMember() {
    return this == REMOVED;
  }
}
