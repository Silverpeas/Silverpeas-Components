/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.components.suggestionbox.mock;

import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.model.CommentedPublicationInfo;
import com.silverpeas.comment.service.CommentService;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;

import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * @author: Yohann Chastagnier
 */
public class CommentServiceMockWrapper implements CommentService {

  private final CommentService mock = mock(CommentService.class);

  public CommentService getMock() {
    return mock;
  }


  @Override
  public void createComment(final Comment cmt) {
    mock.createComment(cmt);
  }

  @Override
  public void createAndIndexComment(final Comment cmt) {
    mock.createAndIndexComment(cmt);
  }

  @Override
  public void deleteComment(final CommentPK pk) {
    mock.deleteComment(pk);
  }

  @Override
  public void deleteAllCommentsOnPublication(final String resourceType, final WAPrimaryKey pk) {
    mock.deleteAllCommentsOnPublication(resourceType, pk);
  }

  @Override
  public void deleteAllCommentsByComponentInstanceId(final String instanceId) {
    mock.deleteAllCommentsByComponentInstanceId(instanceId);
  }

  @Override
  public void deleteComment(final Comment comment) {
    mock.deleteComment(comment);
  }

  @Override
  public void moveComments(final String resourceType, final WAPrimaryKey fromPK,
      final WAPrimaryKey toPK) {
    mock.moveComments(resourceType, fromPK, toPK);
  }

  @Override
  public void moveAndReindexComments(final String resourceType, final WAPrimaryKey fromPK,
      final WAPrimaryKey toPK) {
    mock.moveAndReindexComments(resourceType, fromPK, toPK);
  }

  @Override
  public void updateComment(final Comment cmt) {
    mock.updateComment(cmt);
  }

  @Override
  public void updateAndIndexComment(final Comment cmt) {
    mock.updateAndIndexComment(cmt);
  }

  @Override
  public Comment getComment(final CommentPK pk) {
    return mock.getComment(pk);
  }

  @Override
  public List<Comment> getAllCommentsOnPublication(final String resourceType,
      final WAPrimaryKey pk) {
    return mock.getAllCommentsOnPublication(resourceType, pk);
  }

  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublicationsInfo(final String resourceType,
      final List<WAPrimaryKey> pks) {
    return mock.getMostCommentedPublicationsInfo(resourceType, pks);
  }

  @Override
  public List<CommentedPublicationInfo> getAllMostCommentedPublicationsInfo() {
    return mock.getAllMostCommentedPublicationsInfo();
  }

  @Override
  public int getCommentsCountOnPublication(final String resourceType, final WAPrimaryKey pk) {
    return mock.getCommentsCountOnPublication(resourceType, pk);
  }

  @Override
  public void indexAllCommentsOnPublication(final String resourceType, final WAPrimaryKey pk) {
    mock.indexAllCommentsOnPublication(resourceType, pk);
  }

  @Override
  public void unindexAllCommentsOnPublication(final String resourceType, final WAPrimaryKey pk) {
    mock.unindexAllCommentsOnPublication(resourceType, pk);
  }

  @Override
  public Comment getContentById(final String contentId) {
    return mock.getContentById(contentId);
  }

  @Override
  public ResourceLocator getComponentSettings() {
    return mock.getComponentSettings();
  }

  @Override
  public ResourceLocator getComponentMessages(final String language) {
    return mock.getComponentMessages(language);
  }
}
