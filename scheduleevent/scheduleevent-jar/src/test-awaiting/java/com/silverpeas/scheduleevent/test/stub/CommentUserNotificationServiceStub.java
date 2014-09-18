/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.scheduleevent.test.stub;

import com.silverpeas.SilverpeasComponentService;
import com.silverpeas.SilverpeasContent;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.service.CommentUserNotificationService;
import javax.inject.Named;

/**
 * Extends the CommentUserNotificationService class to play the role of a such object in tests.
 */
@Named("commentUserNotificationService")
public class CommentUserNotificationServiceStub implements CommentUserNotificationService {

  @Override
  public void register(String component,
      SilverpeasComponentService<? extends SilverpeasContent> service) {
  }

  @Override
  public void unregister(String component) {
  }

  @Override
  public void commentAdded(Comment newComment) {
  }

  @Override
  public void commentRemoved(Comment removedComment) {
  }
}
