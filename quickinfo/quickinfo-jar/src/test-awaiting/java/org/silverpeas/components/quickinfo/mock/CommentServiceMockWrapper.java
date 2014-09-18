/*
 * Copyright (C) 2000-2014 Silverpeas
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
package org.silverpeas.components.quickinfo.mock;

import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.model.CommentedPublicationInfo;
import com.silverpeas.comment.service.CommentService;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import static org.mockito.Mockito.mock;

import java.util.List;

/**
 * A wrapper of a mock of an {@code CommentService} instance dedicated to the tests. This
 * wrapper decorates the mock and it is used to be managed by an IoC container as an
 * {@code CommentService} instance.
 * @author mmoquillon
 */
public class CommentServiceMockWrapper implements CommentService {

  private final CommentService mock = mock(CommentService.class);

  public CommentService getMock() {
    return mock;
  }

  @Override
  public void createComment(Comment cmt) {
    mock.createComment(cmt);
  }

  @Override
  public void createAndIndexComment(Comment cmt) {
    mock.createAndIndexComment(cmt);
  }

  @Override
  public void deleteComment(CommentPK pk) {
    mock.deleteComment(pk);
  }

  @Override
  public void deleteAllCommentsOnPublication(final String resourceType, final WAPrimaryKey pk) {
    mock.deleteAllCommentsOnPublication(resourceType, pk);
  }

  @Override
  public void deleteAllCommentsByComponentInstanceId(String instanceId) {
    mock.deleteAllCommentsByComponentInstanceId(instanceId);
  }

  @Override
  public void deleteComment(Comment comment) {
    mock.deleteComment(comment);
  }

  @Override
  public void moveComments(String resourceType, WAPrimaryKey fromPK, WAPrimaryKey toPK) {
    mock.moveComments(resourceType, fromPK, toPK);
  }

  @Override
  public void moveAndReindexComments(String resourceType, WAPrimaryKey fromPK, WAPrimaryKey toPK) {
    mock.moveAndReindexComments(resourceType, fromPK, toPK);
  }

  @Override
  public void updateComment(Comment cmt) {
    mock.updateComment(cmt);
  }

  @Override
  public void updateAndIndexComment(Comment cmt) {
    mock.updateAndIndexComment(cmt);
  }

  @Override
  public Comment getComment(CommentPK pk) {
    return mock.getComment(pk);
  }

  @Override
  public List<Comment> getAllCommentsOnPublication(String resourceType, WAPrimaryKey pk) {
    return mock.getAllCommentsOnPublication(resourceType, pk);
  }

  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublicationsInfo(String resourceType,
      List<? extends WAPrimaryKey> pks) {
    return mock.getMostCommentedPublicationsInfo(resourceType, pks);
  }

  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublicationsInfo(String resourceType) {
    return mock.getMostCommentedPublicationsInfo(resourceType);
  }

  @Override
  public List<CommentedPublicationInfo> getAllMostCommentedPublicationsInfo() {
    return mock.getAllMostCommentedPublicationsInfo();
  }

  @Override
  public int getCommentsCountOnPublication(String resourceType, WAPrimaryKey pk) {
    return mock.getCommentsCountOnPublication(resourceType, pk);
  }

  @Override
  public void indexAllCommentsOnPublication(String resourceType, WAPrimaryKey pk) {
    mock.indexAllCommentsOnPublication(resourceType, pk);
  }

  @Override
  public void unindexAllCommentsOnPublication(String resourceType, WAPrimaryKey pk) {
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
  public ResourceLocator getComponentMessages(String language) {
    return mock.getComponentMessages(language);
  }

  @Override
  public List<Comment> getLastComments(String resourceType, int count) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
