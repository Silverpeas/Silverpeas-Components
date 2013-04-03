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
package com.silverpeas.blog.control.ejb;

import java.rmi.RemoteException;

import java.util.Collection;
import java.util.Date;

import javax.ejb.EJBObject;

import com.silverpeas.blog.model.Archive;
import com.silverpeas.blog.model.Category;
import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.comment.model.Comment;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * @author
 */
@Deprecated
public interface BlogBm extends EJBObject {
  public String createPost(PostDetail post) throws RemoteException;

  public void updatePost(PostDetail post) throws RemoteException;

  public void deletePost(String postId, String instanceId) throws RemoteException;

  public PostDetail getPost(PublicationPK pubPK) throws RemoteException;

  public Collection<PostDetail> getAllPosts(String instanceId)
      throws RemoteException;

  public Collection<PostDetail> getLastPosts(String instanceId) throws RemoteException;

  public Date getDateEvent(String pubId) throws RemoteException;

  public Collection<PostDetail> getPostsByCategory(String categoryId, String instanceId)
      throws RemoteException;

  public Collection<PostDetail> getPostsByArchive(String beginDate, String endDate,
      String instanceId) throws RemoteException;

  public Collection<PostDetail> getPostsByDate(String date, String instanceId)
      throws RemoteException;

  public Collection<PostDetail> getResultSearch(String word, String userId, String spaceId,
      String instanceId) throws RemoteException;

  public String createCategory(Category category) throws RemoteException;

  public void deleteCategory(String categoryId, String instanceId) throws RemoteException;

  public void updateCategory(Category category) throws RemoteException;

  public Category getCategory(NodePK nodePK) throws RemoteException;

  public Collection<NodeDetail> getAllCategories(String instanceId) throws RemoteException;

  public Collection<Archive> getAllArchives(String instanceId) throws RemoteException;

  public int getSilverObjectId(PublicationPK pubPK) throws RemoteException;

  public void indexBlog(String componentId) throws RemoteException;

  public void externalElementsOfPublicationHaveChanged(PublicationPK pubPK, String userId)
      throws RemoteException;

  public void addSubscription(String userId, String instanceId) throws RemoteException;

  public void sendSubscriptionsNotification(NodePK fatherPK, PostDetail post, Comment comment, 
      String type, String senderId) throws RemoteException;
  
  public void draftOutPost(PostDetail post) throws RemoteException;
}
