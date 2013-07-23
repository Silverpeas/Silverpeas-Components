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
package org.silverpeas.components.forum.subscription;

import com.silverpeas.subscribe.constant.SubscriptionResourceType;
import com.silverpeas.subscribe.service.PKSubscriptionResource;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.forums.models.MessagePK;

/**
 * User: Yohann Chastagnier
 * Date: 07/06/13
 */
public class ForumMessageSubscriptionResource extends PKSubscriptionResource {

  /**
   * A way to get an instance of a forum message subscription resource.
   * @param pk
   * @return
   */
  public static ForumMessageSubscriptionResource from(MessagePK pk) {
    return new ForumMessageSubscriptionResource(pk);
  }

  /**
   * Default constructor
   * @param pk
   */
  protected ForumMessageSubscriptionResource(final MessagePK pk) {
    super(pk, SubscriptionResourceType.FORUM_MESSAGE);
  }
}
