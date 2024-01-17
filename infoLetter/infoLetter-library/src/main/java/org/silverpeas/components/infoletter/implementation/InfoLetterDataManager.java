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
package org.silverpeas.components.infoletter.implementation;

import org.silverpeas.components.infoletter.InfoLetterContentManager;
import org.silverpeas.components.infoletter.InfoLetterException;
import org.silverpeas.components.infoletter.model.InfoLetter;
import org.silverpeas.components.infoletter.model.InfoLetterPublication;
import org.silverpeas.components.infoletter.model.InfoLetterPublicationPdC;
import org.silverpeas.components.infoletter.model.InfoLetterService;
import org.silverpeas.components.infoletter.model.InfoLetterTemplateContributionWrapper;
import org.silverpeas.core.ApplicationServiceProvider;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformer;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.mail.MailAddress;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.persistence.jdbc.bean.PersistenceException;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionServiceProvider;
import org.silverpeas.core.subscription.service.ComponentSubscription;
import org.silverpeas.core.subscription.service.ComponentSubscriptionResource;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.internet.InternetAddress;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.silverpeas.core.mail.MailAddress.eMail;
import static org.silverpeas.core.mail.ReceiverMailAddressSet.with;

/**
 * Class declaration
 * @author
 */
@Service
@Named("infoLetter" + ApplicationServiceProvider.SERVICE_NAME_SUFFIX)
public class InfoLetterDataManager implements InfoLetterService {

  private static final String MESSAGES_PATH
      = "org.silverpeas.infoLetter.multilang.infoLetterBundle";
  private static final String SETTINGS_PATH
      = "org.silverpeas.infoLetter.settings.infoLetterSettings";
  private static final SettingBundle settings = ResourceLocator.getSettingBundle(SETTINGS_PATH);

  private static final String TABLE_EXTERNAL_EMAILS = "SC_IL_ExtSus";
  private static final String INSTANCE_ID = "instanceId = '";

  private SilverpeasBeanDAO<InfoLetter> infoLetterDAO;
  private SilverpeasBeanDAO<InfoLetterPublication> infoLetterPublicationDAO;

  @Inject
  private InfoLetterContentManager infoLetterContentManager;

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Contribution> Optional<T> getContributionById(
      final ContributionIdentifier contributionId) {
    if (InfoLetterPublicationPdC.TYPE.equals(contributionId.getType())) {
      final String localId = contributionId.getLocalId();
      final IdPK pk = new IdPK(localId);
      return (Optional<T>) Optional.ofNullable(getInfoLetterPublication(pk));
    } else if (InfoLetter.TYPE.equals(contributionId.getType())) {
      return (Optional<T>) getInfoLetters(contributionId.getComponentInstanceId()).stream()
          .map(InfoLetterTemplateContributionWrapper::new)
          .findFirst();
    }
    throw new IllegalStateException(
        MessageFormat.format("type {0} is not handled", contributionId.getType()));
  }

  @Override
  public SettingBundle getComponentSettings() {
    return settings;
  }

  @Override
  public LocalizationBundle getComponentMessages(final String language) {
    return ResourceLocator.getLocalizationBundle(MESSAGES_PATH, language);
  }

  @Override
  public boolean isRelatedTo(final String instanceId) {
    return instanceId.startsWith("infoLetter");
  }

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
      deleteIndex(ie);
      createIndex(ie);
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

  @Transactional
  @Override
  public void deleteInfoLetterPublication(WAPrimaryKey pk, String componentId) {
    try (Connection con = openConnection()) {
      infoLetterPublicationDAO.remove(pk);
      infoLetterContentManager.deleteSilverContent(con, pk.getId(), componentId);
      final InfoLetterPublication entity = new InfoLetterPublication(
          new ResourceReference(pk.getId(), componentId), componentId, null, null, null, 0, 0);
      deleteIndex(entity);
      entity.deleteContent();
    } catch (Exception pe) {
      throw new InfoLetterException(pe);
    }
  }

  @Override
  public void updateInfoLetterPublication(InfoLetterPublicationPdC ilp) {
    try {
      infoLetterPublicationDAO.update(ilp);
      infoLetterContentManager.updateSilverContentVisibility(ilp);
      if (ilp._isValid()) {
        deleteIndex(ilp);
        createIndex(ilp);
      }
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

  @Transactional
  @Override
  public void setEmailsExternalsSubscribers(WAPrimaryKey letterPK, Set<String> emails) {
    try (Connection con = openConnection()) {
      final InfoLetter letter = getInfoLetter(letterPK);
      final int letterId = Integer.parseInt(letterPK.getId());
      JdbcSqlQuery.createDeleteFor(TABLE_EXTERNAL_EMAILS)
          .where("instanceId = ?", letter.getInstanceId())
          .and("letter = ?", letterId)
          .executeWith(con);
      for (String email : emails) {
        JdbcSqlQuery.createInsertFor(TABLE_EXTERNAL_EMAILS)
            .addInsertParam("letter", letterId)
            .addInsertParam("email", email)
            .addInsertParam("instanceId", letter.getInstanceId())
            .executeWith(con);
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
          new ResourceReference(InfoLetter.TEMPLATE_ID + letterPK.getId(), componentId), userId,
          I18NHelper.DEFAULT_LANGUAGE);
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

  @Override
  public Set<String> sendTemplateByMail(final InfoLetter il, final String mimeMultipart,
      final Set<String> listEmailDest, final String subject, final String emailFrom) {
    return sendContributionByMail(il.getTemplateIdentifier(), mimeMultipart, listEmailDest, subject,
        emailFrom);
  }

  @Override
  public Set<String> sendLetterByMail(InfoLetterPublicationPdC ilp, String mimeMultipart,
      Set<String> listEmailDest, String subject, String emailFrom) {
    return sendContributionByMail(ilp.getIdentifier(), mimeMultipart, listEmailDest, subject,
        emailFrom);
  }

  private Set<String> sendContributionByMail(final ContributionIdentifier cId,
      final String mimeMultipart, final Set<String> listEmailDest, final String subject,
      final String emailFrom) {
    final Set<String> emailErrors = new HashSet<>();
    final Set<String> emailDest = new HashSet<>();
    // Verifying emails
    listEmailDest.forEach(m -> {
      try {
        new InternetAddress(m);
        emailDest.add(m);
      } catch (Exception ex) {
        SilverLogger.getLogger(this).error(ex);
        emailErrors.add(m);
      }
    });
    if (!emailDest.isEmpty()) {
      try {
        final ResourceReference foreignKey = cId.toReference();
        final List<SimpleDocument> listAttachedFiles =
            AttachmentServiceProvider.getAttachmentService().
                listDocumentsByForeignKeyAndType(foreignKey, DocumentType.attachment, null);
        final String wysiwygContent =
            WysiwygController.load(foreignKey.getInstanceId(), foreignKey.getId(), null);
        WysiwygContentTransformer.on(wysiwygContent)
            .toMailContent()
            .withMimeMultipart(mimeMultipart)
            .addAttachments(listAttachedFiles)
            .prepareMailSendingFrom(eMail(emailFrom))
            .to(with(listEmailDest.stream().map(MailAddress::eMail).collect(toSet())))
            .withSubject(subject)
            .oneMailPerReceiver()
            .send();
      } catch (Exception e) {
        throw new InfoLetterException(e);
      }
    }
    return emailErrors;
  }

  @Override
  public void indexInfoLetter(final String componentId) {
    final InfoLetter infoLetter = getInfoLetters(componentId).get(0);
    createIndex(infoLetter);
    indexPublications(infoLetter);
  }

  private void indexPublications(final InfoLetter infoLetter) {
    try {
      Optional.ofNullable(getInfoLetterPublications(infoLetter.getPK())).stream()
          .flatMap(Collection::stream)
          .forEach(this::processPublicationIndexation);
    } catch (Exception e) {
      throw new InfoLetterException(e);
    }
  }

  private void processPublicationIndexation(final InfoLetterPublication pub) {
    try {
      if (pub._isValid()) {
        createIndex(pub);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("Error during indexation of newsletter {0}", pub.getPK().getId(), e);
    }
  }

  private void createIndex(InfoLetter il) {
    if (il != null) {
      final FullIndexEntry indexEntry = new FullIndexEntry(il.getInstanceId(), InfoLetter.TYPE,
          il.getPK().getId());
      indexEntry.setTitle(il.getName());
      indexEntry.setPreview(il.getDescription());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  private void deleteIndex(InfoLetter il) {
    final IndexEntryKey indexEntry = new IndexEntryKey(il.getInstanceId(), InfoLetter.TYPE,
        il.getPK().getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  private void createIndex(InfoLetterPublication pub) {
    if (pub != null) {
      final ContributionIdentifier identifier = pub.getIdentifier();
      final FullIndexEntry indexEntry = new FullIndexEntry(identifier.getComponentInstanceId(),
          InfoLetterPublicationPdC.TYPE, identifier.getLocalId());
      indexEntry.setTitle(pub.getTitle());
      indexEntry.setPreview(pub.getDescription());
      try {
        indexEntry.setCreationDate(DateUtil.parse(pub.getParutionDate()));
      } catch (ParseException e) {
        SilverLogger.getLogger(this).warn(e);
      }
      indexEntry.setCreationUser(User.getCurrentUser().getId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  private void deleteIndex(InfoLetterPublication pub) {
    final ContributionIdentifier identifier = pub.getIdentifier();
    final IndexEntryKey indexEntry = new IndexEntryKey(identifier.getComponentInstanceId(),
        InfoLetterPublicationPdC.TYPE, identifier.getLocalId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }
}
