/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.blog.servlets;

import com.silverpeas.blog.control.BlogService;
import com.silverpeas.blog.control.BlogServiceFactory;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.peasUtil.RssServlet;
import com.stratelia.silverpeas.peasCore.URLManager;

public class BlogRssServlet extends RssServlet<PostDetail> {
  
  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getListElements(java.lang.String, int)
   */
  @Override
  public Collection<PostDetail> getListElements(String instanceId, int nbReturned)
      throws RemoteException {
    // récupération de la liste des 10 prochains billets du Blog
    BlogService service = BlogServiceFactory.getFactory().getBlogService();
    return service.getAllValidPosts(instanceId, nbReturned);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementTitle(java.lang.Object, java.lang.String)
   */
  @Override
  public String getElementTitle(PostDetail post, String userId) {
    return post.getPublication().getName();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementLink(java.lang.Object, java.lang.String)
   */
  @Override
  public String getElementLink(PostDetail post, String userId) {
    return URLManager.getApplicationURL() + "/Publication/"
        + post.getPublication().getPK().getId();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementDescription(java.lang.Object,
   * java.lang.String)
   */
  @Override
  public String getElementDescription(PostDetail post, String userId) {
    return post.getPublication().getDescription();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.peasUtil.RssServlet#getElementDate(java.lang.Object)
   */
  @Override
  public Date getElementDate(PostDetail post) {
    Calendar calElement = GregorianCalendar.getInstance();
    calElement.setTime(post.getPublication().getCreationDate());
    /*
     * calElement.add(Calendar.HOUR_OF_DAY, -1); //-1 car bug d'affichage du fil RSS qui affiche
     * toujours 1h en trop
     */
    calElement.add(Calendar.HOUR_OF_DAY, 0);
    return calElement.getTime();
  }

  @Override
  public String getElementCreatorId(PostDetail post) {
    return post.getPublication().getUpdaterId();
  }
}