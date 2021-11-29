/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.blog.service;

import org.silverpeas.components.blog.model.Archive;
import org.silverpeas.components.blog.model.Category;
import org.silverpeas.components.blog.model.PostDetail;
import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

/**
 * Services provided by the Blog Silverpeas component.
 */
public interface BlogService extends ApplicationService {

  static BlogService get() {
    return ServiceProvider.getService(BlogService.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  Optional<PostDetail> getContributionById(ContributionIdentifier contributionId);

  String createPost(final PostDetail post);

  String createPost(final PostDetail post, PdcClassification classification);

  void updatePost(final PostDetail post);

  void deletePost(String postId, String instanceId);

  Collection<PostDetail> getAllPosts(String instanceId);

  Collection<PostDetail> getAllValidPosts(String instanceId, int nbReturned);

  Date getDateEvent(String pubId);

  Collection<PostDetail> getPostsByCategory(String categoryId, String instanceId);

  Collection<PostDetail> getPostsByArchive(String beginDate, String endDate,
          String instanceId);

  Collection<PostDetail> getPostsByDate(String date, String instanceId);

  Collection<PostDetail> getResultSearch(String word, String userId, String instanceId);

  void createCategory(final Category category);

  void deleteCategory(String categoryId, String instanceId);

  void updateCategory(final Category category);

  Category getCategory(final NodePK nodePK);

  Collection<NodeDetail> getAllCategories(String instanceId);

  Collection<Archive> getAllArchives(String instanceId);

  void indexBlog(String componentId);

  boolean isSubscribed(final String userId, final String instanceId);

  void sendSubscriptionsNotification(final NodePK fatherPK, PostDetail post, Comment comment,
          String type, String senderId);

  void draftOutPost(final PostDetail post);
}
