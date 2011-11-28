/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.blog.servlets;

import com.silverpeas.blog.control.BlogService;
import com.silverpeas.blog.control.BlogServiceFactory;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;

public class GoToPost extends GoTo {
  @Override
  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    BlogService service = BlogServiceFactory.getFactory().getBlogService();
    PostDetail post = service.getContentById(objectId);

    String gotoURL = URLManager.getURL(null, post.getPublication()
        .getInstanceId())
        + post.getPublication().getURL();

    return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
  }
}