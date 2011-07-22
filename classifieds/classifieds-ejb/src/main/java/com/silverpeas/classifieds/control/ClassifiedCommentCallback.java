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

import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.CommentServiceFactory;
import java.util.Set;

import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import java.util.HashMap;
import java.util.Map;
import com.silverpeas.classifieds.control.ejb.ClassifiedsBm;
import com.silverpeas.classifieds.control.ejb.ClassifiedsBmHome;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.model.ClassifiedsRuntimeException;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.comment.service.CallBackOnCommentAction;
import com.silverpeas.comment.model.Comment;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.List;
import static com.silverpeas.classifieds.ClassifiedUtil.*;

/**
 * The callback invoked at the adding of a comment on a classified.
 *
 * This callback is invoked each time a comment for a classified is created.
 * It notifies the author of the ad about the new comment. The users that have commented out the ad
 * are also notified about it.
 */
public class ClassifiedCommentCallback extends CallBackOnCommentAction {

  /**
   * The name of the attribute in a notification message that refers the comment for which the
   * callback is invoked.
   */
  protected static final String NOTIFICATION_COMMENT_ATTRIBUTE = "comment";
  /**
   * The property valued with the message carried by the notification. This property is defined
   * in the resources of the classifieds module.
   */
  protected static final String SUBJECT_COMMENT_ADDING = "classifieds.commentAddingSubject";
  private CommentService commentController = null;
  private ClassifiedsBm classifiedsBm = null;

  @Override
  public void subscribe() {
    subscribeForCommentAdding();
  }

  @Override
  public void commentAdded(int publicationId, String componentInstanceId, Comment addedComment) {
    if (componentInstanceId.startsWith("classifieds")) {
      try {
        ClassifiedDetail detail = getClassifiedsBm().getClassified(String.valueOf(publicationId));
        Set<String> recipients = getInterestedUsers(addedComment, detail);
        NotificationMetaData notification = createNotification(SUBJECT_COMMENT_ADDING, detail,
            addedComment);
        notifyUsers(recipients, notification);
      } catch (Exception ex) {
        SilverTrace.error("classifieds", getClass().getSimpleName() + ".commentAdded()",
            "root.EX_NO_MESSAGE", ex);
      }
    }
  }

  @Override
  public void commentRemoved(int publicationId, String componentInstanceId, Comment removedComment) {
    SilverTrace.warn("classifieds", getClass().getSimpleName() + ".doInvoke()",
        "classifieds.MSG_WARN_BAD_CALLBACK_INVOCATION");
  }

  /**
   * Gets a business controller on comments of resources.
   * @return a DefaultCommentService instance.
   */
  protected CommentService getCommentController() {
    if (commentController == null) {
      commentController = CommentServiceFactory.getFactory().getCommentService();
    }
    return commentController;
  }

  /**
   * Gets the business service through which ads are handled.
   * @return a ClassifiedsBm object.
   */
  protected ClassifiedsBm getClassifiedsBm() {
    if (classifiedsBm == null) {
      try {
        ClassifiedsBmHome classifiedsBmHome =
            EJBUtilitaire.getEJBObjectRef(JNDINames.CLASSIFIEDSBM_EJBHOME,
            ClassifiedsBmHome.class);
        classifiedsBm = classifiedsBmHome.create();
      } catch (Exception e) {
        throw new ClassifiedsRuntimeException(getClass().getSimpleName() + ".getClassifiedsBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return classifiedsBm;
  }

  /**
   * Gets the notification sender with which notifications to users will be done.
   * @param instanceId the identifier of the classified instance within which the classified and its
   * comments were created.
   * @return a NotificationSender instance.
   */
  protected NotificationSender getNotificationSender(final String instanceId) {
    return new NotificationSender(instanceId);
  }

  /**
   * Notifies the specified users, identified by their identifier, with the specified notification
   * information.
   * @param recipients the recipients of the notification.
   * @param notification the notification information.
   * @throws NotificationManagerException if the notification of the recipients fail.
   */
  protected void notifyUsers(final Set<String> recipients, final NotificationMetaData notification)
      throws NotificationManagerException {
    for (String recipient : recipients) {
      notification.addUserRecipient(new UserRecipient(recipient));
    }
    NotificationSender sender = getNotificationSender(notification.getComponentId());
    sender.notifyUser(notification);
  }

  /**
   * Gets the users that are interested by the adding or the removing of the specified comment for
   * the specified classified.
   *
   * The interested users are actually the authors of the others comments on the specified classified
   * and the creator of the classified. The author of the added or removed comment isn't considered
   * as interested by the comment he has himself added (as he knowns already about this!).
   * @param theComment the comment that is added or removed.
   * @param theDetail the classified detail for which a comment is added or removed.
   * @return a list with the identifier of the interested users.
   * @throws RemoteException if an error occurs while getting the other authors.
   */
  private Set<String> getInterestedUsers(final Comment theComment, final ClassifiedDetail theDetail)
      throws RemoteException {
    Set<String> interestedUsers = new LinkedHashSet<String>();
    int currentAuthor = theComment.getOwnerId();
    List<Comment> comments = getCommentController().getAllCommentsOnPublication(theComment.
        getForeignKey());
    for (Comment aComment : comments) {
      int commentAuthor = aComment.getOwnerId();
      if (commentAuthor != currentAuthor) {
        interestedUsers.add(String.valueOf(commentAuthor));
      }
    }
    if (!String.valueOf(currentAuthor).equals(theDetail.getCreatorId())) {
      interestedUsers.add(theDetail.getCreatorId());
    }
    return interestedUsers;
  }

  /**
   * Creates a notification information with the specified subject and from the specified classified
   * detail and comment.
   * @param subjectKey the key in the messages bundle that is associated with the subject of
   * the notification.
   * @param detail the detail about the ad concerned by the comment.
   * @param comment the comment about what a notification data are created.
   * @return a notification information.
   */
  private NotificationMetaData createNotification(final String subjectKey,
      final ClassifiedDetail detail, final Comment comment) {
    Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
        getNotificationSubject(subjectKey), templates, "commented");
    for (String language : I18NHelper.getAllSupportedLanguages()) {
      SilverpeasTemplate template = newTemplate(detail);
      template.setAttribute(NOTIFICATION_COMMENT_ATTRIBUTE, comment);
      templates.put(language, template);
      notifMetaData.addLanguage(language, getNotificationSubject(subjectKey, language), "");
    }
    notifMetaData.setLink(getClassifiedUrl(detail));
    notifMetaData.setComponentId(detail.getInstanceId());
    notifMetaData.setSender(String.valueOf(comment.getOwnerId()));

    return notifMetaData;
  }

  /**
   * Gets the specified notification subject in the default language (platform language).
   * @param key identifying the subject in a message bundle of the module.
   * @return the notification subject.
   */
  private String getNotificationSubject(final String subjectKey) {
    return getMessage(subjectKey);
  }

  /**
   * Gets the specified notification subject in the specified language.
   * @param subjectKey the key identifying the subject in the message bundle of the module.
   * @param language the language in which the subject is written.
   * @return the notification subject.
   */
  private String getNotificationSubject(final String subjectKey, final String language) {
    return getMessage(subjectKey, language);
  }
}
