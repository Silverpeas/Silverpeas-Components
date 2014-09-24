/*
 * Copyright (C) 2000-2013 Silverpeas
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
package com.silverpeas.scheduleevent.test.stub;

import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.model.CommentedPublicationInfo;
import com.silverpeas.comment.service.CommentService;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.WAPrimaryKey;
import java.util.List;
import javax.inject.Named;

/**
 * Implements the CommentServiceStub interface to play the role of a such object in tests.
 */
@Named("commentService")
public class CommentServiceStub implements CommentService {

  @Override
  public void createComment(Comment cmt) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void createAndIndexComment(Comment cmt) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteComment(CommentPK pk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteAllCommentsOnPublication(String resourceType, WAPrimaryKey pk) {
  }

  @Override
  public void deleteAllCommentsByComponentInstanceId(String instanceId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteComment(Comment comment) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void moveComments(String resourceType, WAPrimaryKey fromPK, WAPrimaryKey toPK) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void moveAndReindexComments(String resourceType, WAPrimaryKey fromPK, WAPrimaryKey toPK) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateComment(Comment cmt) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateAndIndexComment(Comment cmt) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Comment getComment(CommentPK pk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Comment> getAllCommentsOnPublication(String resourceType, WAPrimaryKey pk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublicationsInfo(String resourceType,
      List<? extends WAPrimaryKey> pks) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<CommentedPublicationInfo> getAllMostCommentedPublicationsInfo() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getCommentsCountOnPublication(String resourceType, WAPrimaryKey pk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void indexAllCommentsOnPublication(String resourceType, WAPrimaryKey pk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void unindexAllCommentsOnPublication(String resourceType, WAPrimaryKey pk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Comment getContentById(String contentId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public ResourceLocator getComponentSettings() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public ResourceLocator getComponentMessages(String language) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<CommentedPublicationInfo> getMostCommentedPublicationsInfo(String resourceType) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<Comment> getLastComments(String resourceType, int count) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
