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
package org.silverpeas.components.forums.subscription;

import org.silverpeas.components.forums.model.ForumPK;
import org.silverpeas.core.subscription.service.PKSubscriptionResource;

/**
 * User: Yohann Chastagnier
 * Date: 07/06/13
 */
public class ForumSubscriptionResource extends PKSubscriptionResource {

  /**
   * A way to get an instance of a forum subscription resource.
   * @param pk
   * @return
   */
  public static ForumSubscriptionResource from(ForumPK pk) {
    return new ForumSubscriptionResource(pk);
  }

  /**
   * Default constructor
   * @param pk
   */
  protected ForumSubscriptionResource(final ForumPK pk) {
    super(pk, ForumSubscriptionConstants.FORUM);
  }
}
