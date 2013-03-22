/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package com.silverpeas.blog.control;

import java.util.Collection;
import java.util.Date;

import com.silverpeas.SilverpeasComponentService;
import com.silverpeas.blog.model.Archive;
import com.silverpeas.blog.model.Category;
import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.pdc.model.PdcClassification;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * Services provided by the Blog Silverpeas component.
 */
public interface BlogService extends SilverpeasComponentService<PostDetail> {

  public String createPost(final PostDetail post);

  public String createPost(final PostDetail post, PdcClassification classification);

  public void updatePost(final PostDetail post);

  public void deletePost(String postId, String instanceId);

  public Collection<PostDetail> getAllPosts(String instanceId);
  
  public Collection<PostDetail> getAllValidPosts(String instanceId, int nbReturned);

  public Date getDateEvent(String pubId);

  public Collection<PostDetail> getPostsByCategory(String categoryId, String instanceId);

  public Collection<PostDetail> getPostsByArchive(String beginDate, String endDate,
          String instanceId);

  public Collection<PostDetail> getPostsByDate(String date, String instanceId);

  public Collection<PostDetail> getResultSearch(String word, String userId, String spaceId,
          String instanceId);

  public String createCategory(final Category category);

  public void deleteCategory(String categoryId, String instanceId);

  public void updateCategory(final Category category);

  public Category getCategory(final NodePK nodePK);

  public Collection<NodeDetail> getAllCategories(String instanceId);

  public Collection<Archive> getAllArchives(String instanceId);

  public void indexBlog(String componentId);

  public void externalElementsOfPublicationHaveChanged(final PublicationPK pubPK, String userId);

  public void addSubscription(final NodePK topicPK, String userId);

  public void sendSubscriptionsNotification(final NodePK fatherPK, PostDetail post, Comment comment,
          String type, String senderId);

  public void draftOutPost(final PostDetail post);
}
