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

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.util.servlet.RssServlet;
import org.silverpeas.components.blog.model.PostDetail;
import org.silverpeas.components.blog.service.BlogService;
import org.silverpeas.components.blog.service.BlogServiceFactory;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @see RssServlet
 */
public class BlogRssServlet extends RssServlet<PostDetail> {

  private static final long serialVersionUID = -7858489574699990145L;

  @Override
  public Collection<PostDetail> getListElements(String instanceId, int nbReturned) {
    // récupération de la liste des 10 prochains billets du Blog
    BlogService service = BlogServiceFactory.getBlogService();
    return service.getAllValidPosts(instanceId, nbReturned);
  }

  @Override
  public String getElementTitle(PostDetail post, String userId) {
    return post.getPublication().getName();
  }

  @Override
  public String getElementLink(PostDetail post, String userId) {
    return URLUtil.getApplicationURL() + "/Publication/"
        + post.getPublication().getPK().getId();
  }

  @Override
  public String getElementDescription(PostDetail post, String userId) {
    return post.getPublication().getDescription();
  }

  @Override
  public Date getElementDate(PostDetail post) {
    Calendar calElement = GregorianCalendar.getInstance();
    calElement.setTime(post.getPublication().getCreationDate());
    calElement.add(Calendar.HOUR_OF_DAY, 0);
    return calElement.getTime();
  }

  @Override
  public String getElementCreatorId(PostDetail post) {
    return post.getPublication().getUpdaterId();
  }
}