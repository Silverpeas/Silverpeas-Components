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

  void updatePost(final PostDetail post, final PdcClassification classification);

  void deletePost(String instanceId, String postId);

  Collection<PostDetail> getLastPosts(String instanceId, final BlogFilters filters);

  Collection<PostDetail> getAllPosts(String instanceId);

  Collection<PostDetail> getLastValidPosts(String instanceId, BlogFilters filters);

  Collection<PostDetail> getPostsByCategory(String instanceId, String categoryId,
      final BlogFilters filters);

  Collection<PostDetail> getPostsByArchive(String instanceId, String beginDate, String endDate,
      final BlogFilters filters);

  Collection<PostDetail> getPostsByEventDate(String instanceId, String date,
      final BlogFilters filters);

  Collection<PostDetail> getResultSearch(String instanceId, String word, String userId,
      final BlogFilters filters);

  void createCategory(final Category category);

  void deleteCategory(String instanceId, String categoryId);

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
