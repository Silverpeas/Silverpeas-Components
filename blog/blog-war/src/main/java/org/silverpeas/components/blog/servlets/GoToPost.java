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
package org.silverpeas.components.blog.servlets;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.silverpeas.components.blog.model.PostDetail;
import org.silverpeas.components.blog.service.BlogService;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.util.servlet.GoTo;
import org.silverpeas.kernel.exception.NotFoundException;

import java.net.URLEncoder;

public class GoToPost extends GoTo {

  private static final long serialVersionUID = 4824194822323955033L;

  @Inject
  private BlogService blogService;

  @Override
  public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res) {
    PostDetail post = blogService.getContributionById(
            ContributionIdentifier.from("", objectId, PostDetail.getResourceType()))
        .orElseThrow(() -> new NotFoundException("No post with id " + objectId));
    String componentId = post.getPublication().getInstanceId();
    String gotoURL = URLUtil.getURL(null, componentId) + post.getPublication().getURL();

    // force context of GraphicElementFactory
    setGefSpaceId(req, componentId);

    return "goto=" + URLEncoder.encode(gotoURL, Charsets.UTF_8);
  }
}