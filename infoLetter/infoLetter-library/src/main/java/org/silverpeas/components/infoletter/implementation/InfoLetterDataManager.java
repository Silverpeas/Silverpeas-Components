/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.components.infoletter.implementation;

import org.silverpeas.components.infoletter.InfoLetterContentManager;
import org.silverpeas.components.infoletter.InfoLetterException;
import org.silverpeas.components.infoletter.model.InfoLetter;
import org.silverpeas.components.infoletter.model.InfoLetterPublication;
import org.silverpeas.components.infoletter.model.InfoLetterPublicationPdC;
import org.silverpeas.components.infoletter.model.InfoLetterService;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformer;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.content.wysiwyg.service.process.MailContentProcess;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.mail.MailSending;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.persistence.jdbc.bean.PersistenceException;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionServiceProvider;
import org.silverpeas.core.subscription.service.ComponentSubscription;
import org.silverpeas.core.subscription.service.ComponentSubscriptionResource;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.silverpeas.core.mail.MailAddress.eMail;
import static org.silverpeas.core.mail.MailContent.extractTextBodyPartFromHtmlContent;
import static org.silverpeas.core.mail.MailContent.getHtmlBodyPartFromHtmlContent;

/**
 * Class declaration
 * @author
 */
@Singleton
public class InfoLetterDataManager implements InfoLetterService {

  private static final String TABLE_EXTERNAL_EMAILS = "SC_IL_ExtSus";
  private static final String INSTANCE_ID = "instanceId = '";

  private SilverpeasBeanDAO<InfoLetter> infoLetterDAO;
  private SilverpeasBeanDAO<InfoLetterPublication> infoLetterPublicationDAO;

  @Inject
  private InfoLetterContentManager infoLetterContentManager;

  public InfoLetterDataManager() {
    try {
      infoLetterDAO =
          SilverpeasBeanDAOFactory.getDAO("org.silverpeas.components.infoletter.model.InfoLetter");
      infoLetterPublicationDAO = SilverpeasBeanDAOFactory
          .getDAO("org.silverpeas.components.infoletter.model.InfoLetterPublication");
    } catch (PersistenceException pe) {
      throw new InfoLetterException(pe);
    }
  }

  /**
   * Implementation of InfoLetterService interface
   */
  @Override
  public void createInfoLetter(InfoLetter il) {
    try {
      WAPrimaryKey pk = infoLetterDAO.add(il);
      il.setPK(pk);
    } catch (PersistenceException pe) {
      throw new InfoLetterException(pe);
    }
  }

  @Override
  public void updateInfoLetter(InfoLetter ie) {
    try {
      infoLetterDAO.update(ie);
    } catch (PersistenceException pe) {
      throw new InfoLetterException(pe);
    }
  }

  @Override
  public List<InfoLetter> getInfoLetters(String instanceId) {
    String whereClause = INSTANCE_ID + instanceId + "'";
    try {
      return new ArrayList<>(infoLetterDAO.findByWhereClause(new IdPK(), whereClause));
    } catch (PersistenceException pe) {
      throw new InfoLetterException(pe);
    }
  }

  @Override
  public List<InfoLetterPublication> getInfoLetterPublications(WAPrimaryKey letterPK) {
    try {
      InfoLetter letter = getInfoLetter(letterPK);
      String whereClause =
          INSTANCE_ID + letter.getInstanceId() + "' AND letterId = " + letterPK.getId() +
              " ORDER BY id desc";
      return new ArrayList<>(infoLetterPublicationDAO.findByWhereClause(letterPK, whereClause));
    } catch (PersistenceException pe) {
      throw new InfoLetterException(pe);
    }
  }

  @Override
  public void createInfoLetterPublication(InfoLetterPublicationPdC ilp, String userId) {

    Connection con = openConnection();

    try {
      WAPrimaryKey pk = infoLetterPublicationDAO.add(con, ilp);
      ilp.setPK(pk);
      infoLetterContentManager.createSilverContent(con, ilp, userId);
    } catch (Exception pe) {
      DBUtil.rollback(con);
      throw new InfoLetterException(pe);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteInfoLetterPublication(WAPrimaryKey pk, String componentId) {
    Connection con = openConnection();
    try {
      infoLetterPublicationDAO.remove(pk);
      infoLetterContentManager.deleteSilverContent(con, pk.getId(), componentId);
    } catch (Exception pe) {
      DBUtil.rollback(con);
      throw new InfoLetterException(pe);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void updateInfoLetterPublication(InfoLetterPublicationPdC ilp) {
    try {
      infoLetterPublicationDAO.update(ilp);
      infoLetterContentManager.updateSilverContentVisibility(ilp);
    } catch (Exception e) {
      throw new InfoLetterException(e);
    }
  }

  @Override
  public InfoLetter getInfoLetter(WAPrimaryKey letterPK) {
    InfoLetter retour;
    try {
      retour = infoLetterDAO.findByPrimaryKey(letterPK);
    } catch (PersistenceException pe) {
      throw new InfoLetterException(pe);
    }
    return retour;
  }

  @Override
  public InfoLetterPublicationPdC getInfoLetterPublication(WAPrimaryKey publiPK) {
    InfoLetterPublicationPdC retour;
    try {
      retour = new InfoLetterPublicationPdC(infoLetterPublicationDAO.findByPrimaryKey(publiPK));
    } catch (PersistenceException pe) {
      throw new InfoLetterException(pe);
    }
    return retour;
  }

  @Override
  public InfoLetter createDefaultLetter(String componentId) {
    OrganizationController oc = OrganizationControllerProvider.getOrganisationController();
    ComponentInst ci = oc.getComponentInst(componentId);
    InfoLetter ie = new InfoLetter();
    ie.setInstanceId(componentId);
    ie.setName(ci.getLabel());
    createInfoLetter(ie);
    initTemplate(componentId, ie.getPK(), "0");
    return ie;
  }

  /**
   * Deletes all the info letters (and then all the publications and external subscribers) in the
   * specified component instance.
   * @param componentId the unique identifier of the InfoLetter instance.
   */
  @Override
  public void deleteAllInfoLetters(final String componentId) {
    try (Connection connection = openConnection()) {
      infoLetterPublicationDAO.removeWhere(connection, null, INSTANCE_ID + componentId + "'");
      infoLetterDAO.removeWhere(connection, null,
          INSTANCE_ID + componentId + "'");//TABLE_EXTERNAL_EMAILS
      try (PreparedStatement statement = connection.prepareStatement(
          "delete from " + TABLE_EXTERNAL_EMAILS + " where instanceId = ?")) {
        statement.setString(1, componentId);
        statement.execute();
      }
    } catch (Exception e) {
      throw new InfoLetterException(e);
    }
  }

  @Override
  public int getSilverObjectId(String pubId, String componentId) {

    try {
      int silverObjectId = infoLetterContentManager.getSilverContentId(pubId, componentId);
      if (silverObjectId == -1) {
        IdPK publiPK = new IdPK();
        publiPK.setId(pubId);
        InfoLetterPublicationPdC infoLetter = getInfoLetterPublication(publiPK);
        silverObjectId = infoLetterContentManager
            .createSilverContent(null, infoLetter, infoLetter.getCreatorId());
      }
      return silverObjectId;
    } catch (Exception e) {
      throw new InfoLetterException(e);
    }
  }

  @Override
  public SubscriptionSubscriberList getInternalSuscribers(final String componentId) {
    return ResourceSubscriptionProvider.getSubscribersOfComponent(componentId);
  }

  @Override
  public void setInternalSuscribers(final String componentId, final UserDetail[] users,
      final Group[] groups) {

    // Initializing necessary subscriptions
    Collection<Subscription> subscriptions = new ArrayList<>(users.length + groups.length);
    for (UserDetail user : users) {
      subscriptions.add(
          new ComponentSubscription(UserSubscriptionSubscriber.from(user.getId()), componentId));
    }
    for (Group group : groups) {
      subscriptions.add(
          new ComponentSubscription(GroupSubscriptionSubscriber.from(group.getId()), componentId));
    }

    // Getting all existing subscriptions and selecting those that have to be deleted
    Collection<Subscription> subscriptionsToDelete =
        SubscriptionServiceProvider.getSubscribeService()
            .getByResource(ComponentSubscriptionResource.from(componentId));
    subscriptionsToDelete.removeAll(subscriptions);

    // Deleting
    SubscriptionServiceProvider.getSubscribeService().unsubscribe(subscriptionsToDelete);

    // Creating subscriptions (nothing is registered for subscriptions that already exist)
    SubscriptionServiceProvider.getSubscribeService().subscribe(subscriptions);
  }

  @Override
  public Set<String> getEmailsExternalsSuscribers(WAPrimaryKey letterPK) {
    Set<String> retour = new LinkedHashSet<>();
    try (Connection con = openConnection()) {
      InfoLetter letter = getInfoLetter(letterPK);
      String selectQuery = "SELECT * FROM " + TABLE_EXTERNAL_EMAILS;
      selectQuery += " where instanceId = '" + letter.getInstanceId() + "' ";
      selectQuery += " and letter = " + letterPK.getId() + " ";

      try (Statement selectStmt = con.createStatement()) {
        try (ResultSet rs = selectStmt.executeQuery(selectQuery)) {
          while (rs.next()) {
            retour.add(rs.getString("email"));
          }
        }
      }
    } catch (Exception e) {
      throw new InfoLetterException(e);
    }
    return retour;
  }

  @Override
  public void setEmailsExternalsSubscribers(WAPrimaryKey letterPK, Set<String> emails) {
    try (Connection con = openConnection()) {
      InfoLetter letter = getInfoLetter(letterPK);
      String query = "DELETE FROM " + TABLE_EXTERNAL_EMAILS;
      query += " where instanceId = '" + letter.getInstanceId() + "' ";
      query += " and letter = " + letterPK.getId() + " ";

      try (Statement stmt = con.createStatement()) {
        stmt.executeUpdate(query);
      }
      if (!emails.isEmpty()) {
        for (String email : emails) {
          query = "INSERT INTO " + TABLE_EXTERNAL_EMAILS + "(letter, email, instanceId)";
          query +=
              " values (" + letterPK.getId() + ", '" + email + "', '" + letter.getInstanceId() +
                  "')";
          try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(query);
          }
        }
      }
    } catch (Exception e) {
      throw new InfoLetterException(e);
    }
  }

  @Override
  public void toggleSuscriber(String userId, String componentId, boolean isUserSubscribing) {
    Subscription subscription =
        new ComponentSubscription(UserSubscriptionSubscriber.from(userId), componentId);
    if (isUserSubscribing) {
      SubscriptionServiceProvider.getSubscribeService().subscribe(subscription);
    } else {
      SubscriptionServiceProvider.getSubscribeService().unsubscribe(subscription);
    }
  }

  @Override
  public boolean isUserSuscribed(String userId, String componentId) {
    return SubscriptionServiceProvider.getSubscribeService().existsSubscription(
        new ComponentSubscription(UserSubscriptionSubscriber.from(userId), componentId));
  }

  @Override
  public void initTemplate(String componentId, WAPrimaryKey letterPK, String userId) {
    try {
      String basicTemplate = "<body></body>";
      WysiwygController.createUnindexedFileAndAttachment(basicTemplate,
          new ResourceReference(InfoLetterPublication.TEMPLATE_ID + letterPK.getId(), componentId), userId,
          I18NHelper.defaultLanguage);
    } catch (Exception e) {
      throw new InfoLetterException(e);
    }
  }

  /**
   * open connection
   * @return Connection
   * @throws InfoLetterException
   */
  private Connection openConnection() {
    Connection con;
    try {
      con = DBUtil.openConnection();
    } catch (Exception e) {
      throw new InfoLetterException(e);
    }
    return con;
  }

  private void attachFilesToMail(Multipart mp, List<SimpleDocument> listAttachedFiles)
      throws MessagingException {
    for (SimpleDocument attachment : listAttachedFiles) {
      // create the second message part
      MimeBodyPart mbp = new MimeBodyPart();

      // attach the file to the message
      FileDataSource fds = new FileDataSource(attachment.getAttachmentPath());
      mbp.setDataHandler(new DataHandler(fds));
      // For Displaying images in the mail
      mbp.setFileName(attachment.getFilename());
      mbp.setHeader("Content-ID", "<" + attachment.getFilename() + ">");


      // create the Multipart and its parts to it
      mp.addBodyPart(mbp);
    }
  }

  private Multipart createContentMessageMail(InfoLetterPublicationPdC ilp, String mimeMultipart)
      throws MessagingException, SilverpeasException {
    Multipart multipart = new MimeMultipart(mimeMultipart);

    // create and fill the first message part
    ResourceReference foreignKey = new ResourceReference(ilp.getPK().getId(), ilp.getComponentInstanceId());

    // Load and transform WYSIWYG content for mailing
    String wysiwygContent =
        WysiwygController.load(foreignKey.getInstanceId(), foreignKey.getId(), null);
    MailContentProcess.MailResult wysiwygMailTransformResult =
        WysiwygContentTransformer.on(wysiwygContent).toMailContent();

    // Prepare Mail parts
    final String htmlContent = wysiwygMailTransformResult.getWysiwygContent();
    if ("alternative".equals(mimeMultipart)) {
      // First the WYSIWYG as brut text
      multipart.addBodyPart(extractTextBodyPartFromHtmlContent(htmlContent));
      // Then all the referenced media content
      wysiwygMailTransformResult.applyOn(multipart);
      // Finally the WYSIWYG (the preferred one)
      multipart.addBodyPart(getHtmlBodyPartFromHtmlContent(htmlContent));
    } else {
      // First the WYSIWYG (the main one)
      multipart.addBodyPart(getHtmlBodyPartFromHtmlContent(htmlContent));
      // Then all the referenced media content
      wysiwygMailTransformResult.applyOn(multipart);
      // Finally the WYSIWYG as brut text
      multipart.addBodyPart(extractTextBodyPartFromHtmlContent(htmlContent));
    }

    // Finally explicit attached files
    List<SimpleDocument> listAttachedFilesFromTab =
        AttachmentServiceProvider.getAttachmentService().
            listDocumentsByForeignKeyAndType(foreignKey, DocumentType.attachment, null);
    attachFilesToMail(multipart, listAttachedFilesFromTab);

    // The completed multipart mail to send
    return multipart;
  }

  @Override
  public Set<String> sendLetterByMail(InfoLetterPublicationPdC ilp, String server,
      String mimeMultipart, Set<String> listEmailDest, String subject, String emailFrom) {

    Set<String> emailErrors = new LinkedHashSet<>();

    if (!listEmailDest.isEmpty()) {
      try {
        // create the Multipart and its parts to it
        Multipart mp = createContentMessageMail(ilp, mimeMultipart);
        for (String receiverEmail : listEmailDest) {
          sendMail(subject, emailFrom, emailErrors, mp, receiverEmail);
        }
      } catch (Exception e) {
        throw new InfoLetterException(e);
      }
    }
    return emailErrors;
  }

  private void sendMail(final String subject, final String emailFrom, final Set<String> emailErrors,
      final Multipart mp, final String receiverEmail) {
    try {
      // Verifying the email
      new InternetAddress(receiverEmail);

      // Prepare the mail
      MailSending mail =
          MailSending.from(eMail(emailFrom)).to(eMail(receiverEmail)).withSubject(subject)
              .withContent(mp);

      // Sending the mail
      mail.send();

    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex);
      emailErrors.add(receiverEmail);
    }
  }
}
