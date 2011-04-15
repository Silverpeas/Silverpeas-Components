/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.classifieds.control;

import java.util.List;
import com.silverpeas.classifieds.control.ejb.ClassifiedsBm;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.DefaultCommentService;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import java.util.ArrayList;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;
import static com.silverpeas.classifieds.control.NotificationMatchers.*;

/**
 * Unit tests on the callback process.
 */
public class ClassifiedCommentCallbackTest {

  /**
   * Id of the commented classified.
   */
  private static final int CLASSIFIED_ID = 230;
  /**
   * Id of the author that wrote the comment concerned by the tests.
   */
  private static final String COMMENT_AUTHORID = "3";
  /*
   * Id of the classified component instance to use in the tests.
   */
  private static final String CLASSIFIED_INSTANCEID = "classifieds3";
  /**
   * The callback to test. It is partially mocked.
   */
  private ClassifiedCommentCallback callback = null;
  /**
   * The comment to use in the test when invoking the callback.
   */
  private Comment concernedComment = null;
  /**
   * All of the comments on the classified used in the tests.
   */
  private List<Comment> classifiedComments = new ArrayList<Comment>();
  /**
   * The notification sender to mock and that will be used by the callback.
   */
  private NotificationSender notificationSender = null;
  /**
   * The captor of notification information passed to a mocked notification sender.
   */
  private ArgumentCaptor<NotificationMetaData> notifInfoCaptor =
      ArgumentCaptor.forClass(NotificationMetaData.class);

  public ClassifiedCommentCallbackTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    setUpClassifiedComments();
    callback = spy(new ClassifiedCommentCallback());
    doReturn(mockClassifiedBm()).when(callback).getClassifiedsBm();
    doReturn(mockCommentController()).when(callback).getCommentController();
    doReturn(mockNotificationSender()).when(callback).getNotificationSender(CLASSIFIED_INSTANCEID);
  }

  @After
  public void tearDown() {
  }

  /**
   * The commentAdded() method should notify both the author of the commented ad and the authors
   * of all of the ad's comments.
   */
  @Test
  public void commentAddedShouldNotifyClassifiedAndCommentAuthors() throws Exception {
    callback.commentAdded(CLASSIFIED_ID, CLASSIFIED_INSTANCEID, concernedComment);
    verify(callback).getNotificationSender(CLASSIFIED_INSTANCEID);
    verify(notificationSender).notifyUser(notifInfoCaptor.capture());
    NotificationMetaData notif = getCapturedInfoInNotificiation();
    assertNotNull(notif);
    assertThat("The comment should be in the notification", concernedComment, isSetIn(notif));
    assertEquals("The sender should be the author of the comment from which the callback is invoked",
        COMMENT_AUTHORID, notif.getSender());
    for (Comment aComment : classifiedComments) {
      String authorId = String.valueOf(aComment.getOwnerId());
      if (!authorId.equals(String.valueOf(concernedComment.getOwnerId()))) {
        assertThat("The author '" + authorId + "' should be in the notification recipients",
            notif.getUserRecipients(), hasItem(authorId));
      } else {
        assertFalse("The author '" + authorId + "' shouldn't be in the notification recipients",
            notif.getUserRecipients().contains(authorId));
      }
    }
  }

  /**
   * Sets up all of the comments about the classified used in the current test.
   * The comment to use in the invocation of the callback is also set.
   */
  protected void setUpClassifiedComments() {
    ForeignPK publicationPk = new ForeignPK(String.valueOf(CLASSIFIED_ID), CLASSIFIED_INSTANCEID);
    for (int i = 0; i < 5; i++) {
      String date = (new Date()).toString();
      Comment aComment = new Comment(new CommentPK(String.valueOf(i)), publicationPk,
          i, "Toto" + i, "comment " + i, date, date);
      classifiedComments.add(aComment);
    }
    String date = new Date().toString();
    concernedComment = new Comment(
        new CommentPK("10"),
        publicationPk,
        Integer.parseInt(COMMENT_AUTHORID),
        "Toto" + COMMENT_AUTHORID,
        "concerned comment",
        date,
        date);
    classifiedComments.add(concernedComment);
  }

  /**
   * Mocks the ClassifiedsBm EJB to use by the callback.
   * It is expected the commented ad is asked by the callback to get its author. So that, it can
   * notify it about a new comment.
   * @return the mocked classifiedsBm
   * @throws Exception - it is just for satisfying the contract of some called methods of
   * ClassifiedsBm.
   */
  protected ClassifiedsBm mockClassifiedBm() throws Exception {
    ClassifiedDetail detail = new ClassifiedDetail(CLASSIFIED_ID);
    detail.setCreatorId("0");
    detail.setInstanceId(CLASSIFIED_INSTANCEID);
    ClassifiedsBm classifieds = mock(ClassifiedsBm.class);
    when(classifieds.getClassified(String.valueOf(CLASSIFIED_ID))).thenReturn(detail);
    return classifieds;
  }

  /**
   * Mocks the DefaultCommentService to use by the callback.
   * It is expected all of other comments are asked by the callback to get their authors. So that
   * it can notify them about the new comment.
   * @return the mocked comment controller.
   * @throws Exception - it is just for satisfying the contract of some called methods of
   * DefaultCommentService.
   */
  protected CommentService mockCommentController() throws Exception {
    CommentService commentController = mock(DefaultCommentService.class);
    when(commentController.getAllCommentsOnPublication(
        new ForeignPK(String.valueOf(CLASSIFIED_ID), CLASSIFIED_INSTANCEID))).thenReturn(
        classifiedComments);
    return commentController;
  }

  /**
   * Mocks the NotificationSender instance to use by the callback.
   * It is expected it is used by the callback for sending notification to users. The notification
   * information passed to the sender is captured.
   * @return the mocked notification sender.
   * @throws Exception - it is just for satisfying the contract of some called methods of
   * NotifySender.
   */
  protected NotificationSender mockNotificationSender() throws Exception {
    notificationSender = mock(NotificationSender.class);
    return notificationSender;
  }

  /**
   * Gets the captured information from the notification sender at notifiyUser() call by the
   * callback.
   * @return the notification information passed by the callback to the notification sender.
   */
  protected NotificationMetaData getCapturedInfoInNotificiation() {
    return notifInfoCaptor.getValue();
  }
}