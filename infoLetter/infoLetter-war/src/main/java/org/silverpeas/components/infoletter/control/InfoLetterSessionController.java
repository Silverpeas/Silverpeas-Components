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
package org.silverpeas.components.infoletter.control;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.silverpeas.components.infoletter.InfoLetterException;
import org.silverpeas.components.infoletter.InfoLetterPeasTrappedException;
import org.silverpeas.components.infoletter.model.InfoLetter;
import org.silverpeas.components.infoletter.model.InfoLetterPublication;
import org.silverpeas.components.infoletter.model.InfoLetterPublicationPdC;
import org.silverpeas.components.infoletter.model.InfoLetterService;
import org.silverpeas.components.infoletter.notification.InfoLetterSubscriptionPublicationUserNotification;
import org.silverpeas.components.infoletter.service.InfoLetterServiceProvider;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.content.ddwe.DragAndDropWbeFile;
import org.silverpeas.core.contribution.content.ddwe.model.DragAndDropWebEditorStore;
import org.silverpeas.core.contribution.content.renderer.ContributionContentRenderer;
import org.silverpeas.core.contribution.model.ContributionContent;
import org.silverpeas.core.exception.DecodingException;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilTrappedException;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberMapBySubscriberType;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.csv.CSVReader;
import org.silverpeas.core.util.csv.Variant;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;
import static org.silverpeas.core.util.StringUtil.EMPTY;
import static org.silverpeas.core.util.StringUtil.getBooleanValue;

public class InfoLetterSessionController extends AbstractComponentSessionController {
  private static final long serialVersionUID = -4498344315667761189L;

  private static final String EMAILS = "Emails";

  /**
   * Interface metier du composant
   */
  private transient InfoLetterService dataInterface = null;

  public static final String EXPORT_CSV_NAME = "_emails.csv";

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   *
   */
  public InfoLetterSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.infoLetter.multilang.infoLetterBundle",
        "org.silverpeas.infoLetter.settings.infoLetterIcons",
        "org.silverpeas.infoLetter.settings.infoLetterSettings");
    // Initialize business interface
    if (dataInterface == null) {
      dataInterface = InfoLetterServiceProvider.getInfoLetterData();
    }
  }

  /*
   * Initialize UserPanel with the list of Silverpeas subscribers
   */
  public String initUserPanel() throws InfoLetterException {
    String hostSpaceName = getSpaceLabel();
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(),
        URLUtil.getApplicationURL() + "/RinfoLetter/" + getComponentId() + "/Main");
    String hostUrl =
        URLUtil.getApplicationURL() + "/RinfoLetter/" + getComponentId() + "/RetourPanel";
    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(null);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(hostUrl);

    // Contraintes
    sel.setMultiSelect(true);
    sel.setPopupMode(false);

    // Internal subscribers
    SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes =
        dataInterface.getInternalSuscribers(getComponentId()).indexBySubscriberType();
    // Users
    sel.setSelectedElements(subscriberIdsByTypes.get(SubscriberType.USER).getAllIds());
    // Groups
    sel.setSelectedSets(subscriberIdsByTypes.get(SubscriberType.GROUP).getAllIds());

    return Selection.getSelectionURL();
  }

  /*
   * Retour du UserPanel
   */
  public void retourUserPanel() {
    Selection sel = getSelection();
    UserDetail[] users = SelectionUsersGroups.getUserDetails(sel.getSelectedElements());
    Group[] groups = SelectionUsersGroups.getGroups(sel.getSelectedSets());
    dataInterface.setInternalSuscribers(getComponentId(), users, groups);
  }

  // Mise a jour d'une lettre d'information
  public void updateInfoLetter(InfoLetter ie) {
    dataInterface.updateInfoLetter(ie);
  }

  // Recuperation de la liste des lettres
  public List<InfoLetter> getInfoLetters() {
    return dataInterface.getInfoLetters(getComponentId());
  }

  /**
   * Gets the newsletter template
   */
  public InfoLetter getInfoLetter() {
    return getInfoLetters().get(0);
  }

  public DragAndDropWbeFile getTemplateFileForEdition() {
    final InfoLetter infoLetter = getInfoLetter();
    final DragAndDropWebEditorStore store =
        new DragAndDropWebEditorStore(infoLetter.getTemplateIdentifier());
    if (!store.getFile().exists() && infoLetter.existsTemplateContent()) {
      // If the Drag And Drop editor has not been yet used, taking the WYSIWYG content if any.
      // It permits retrieving old content of template edited before the introduction
      // of the Drag And Drop WEB Editor.
      final String content = infoLetter.getTemplateWysiwygContent()
          .map(ContributionContent::getRenderer)
          .map(ContributionContentRenderer::renderEdition)
          .filter(StringUtil::isDefined)
          .orElse(EMPTY);
      store.getFile().getContainer().getOrCreateTmpContent().setValue(content);
      store.getFile().getContainer().getOrCreateContent().setValue(content);
      store.save();
      infoLetter.saveTemplateContent(null);
    }
    return new DragAndDropWbeFile(store);
  }

  // Recuperation de la liste des publications
  public List<InfoLetterPublication> getInfoLetterPublications() {
    return dataInterface.getInfoLetterPublications(getInfoLetter().getPK());
  }

  public DragAndDropWbeFile getFileForEditionOf(final InfoLetterPublication ilp) {
    final DragAndDropWebEditorStore store = new DragAndDropWebEditorStore(ilp.getIdentifier());
    if (!store.getFile().exists()) {
      // If the Drag And Drop editor has not been yet used, taking the WYSIWYG content if any.
      // It permits retrieving old contents of newsletter edited before the introduction
      // of the Drag And Drop WEB Editor.
      final Optional<String> legacyContent = ilp.getWysiwygContent()
          .map(ContributionContent::getRenderer)
          .map(ContributionContentRenderer::renderEdition)
          .filter(StringUtil::isDefined);
      if (legacyContent.isPresent()) {
        final String content = legacyContent.get();
        store.getFile().getContainer().getOrCreateTmpContent().setValue(content);
        store.getFile().getContainer().getOrCreateContent().setValue(content);
        store.save();
        ilp.saveContent(null);
      } else {
        ilp.initFrom(getInfoLetter());
      }
    }
    return new DragAndDropWbeFile(store);
  }

  @SuppressWarnings("UnusedReturnValue")
  public DragAndDropWbeFile resetWithTemplateFor(final InfoLetterPublication ilp) {
    final DragAndDropWebEditorStore store = new DragAndDropWebEditorStore(ilp.getIdentifier());
    ilp.initFrom(getInfoLetter());
    return new DragAndDropWbeFile(store);
  }

  // Creation d'une publication
  public void createInfoLetterPublication(InfoLetterPublicationPdC ilp) {
    ilp.setInstanceId(getComponentId());
    dataInterface.createInfoLetterPublication(ilp, getUserId());
    // Classify content on PdC
    classifyInfoLetterPublication(ilp);
  }

  /**
   * Classify the info letter publication on the PdC only if the positions attribute is filled
   * inside object parameter
   * @param ilp the InfoLetterPublication to classify
   */
  private void classifyInfoLetterPublication(InfoLetterPublicationPdC ilp) {
    String positions = ilp.getPositions();
    if (StringUtil.isDefined(positions)) {
      PdcClassificationEntity ilClassification = null;
      try {
        ilClassification = PdcClassificationEntity.fromJSON(positions);
      } catch (DecodingException e) {
        SilverLogger.getLogger(this).error("Unable to decode JSON: " + positions, e);
      }
      if (ilClassification != null && !ilClassification.isUndefined()) {
        List<PdcPosition> pdcPositions = ilClassification.getPdcPositions();
        PdcClassification classification = aPdcClassificationOfContent(ilp).
            withPositions(pdcPositions);
        classification.classifyContent(ilp);
      }
    }
  }

  // Suppression d'une publication
  public void deleteInfoLetterPublication(String id) {
    dataInterface.deleteInfoLetterPublication(new IdPK(id), getComponentId());
  }

  // Mise a jour d'une publication
  public void updateInfoLetterPublication(InfoLetterPublicationPdC ilp) {
    ilp.setInstanceId(getComponentId());
    dataInterface.updateInfoLetterPublication(ilp);
  }

  // Recuperation d'une publication par sa clef
  public InfoLetterPublicationPdC getInfoLetterPublication(String id) {
    return dataInterface.getInfoLetterPublication(new IdPK(id));
  }

  /**
   * Notify the newsletter to internal subscribers
   * @param ilp the infoletter to send
   *
   */
  public void notifyInternalSubscribers(InfoLetterPublicationPdC ilp) {
    if (isNewsLetterSendByMail()) {
      //Send the newsletter by Mail to internal subscribers
      SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes =
          dataInterface.getInternalSuscribers(getComponentId()).indexBySubscriberType();
      Set<String> internalSubscribersEmails = getEmailsInternalSubscribers(subscriberIdsByTypes);
      sendLetterByMail(ilp, internalSubscribersEmails);
    } else {
      //Send the newsletter via notification
      UserNotificationHelper.buildAndSend(
          new InfoLetterSubscriptionPublicationUserNotification(ilp, getUserDetail()));
    }
  }

  /**
   * Send letter by mail
   * @param ilp  the classified InfoLetterPublication to send
   * @param emails the email addresses of the receivers.
   * @return tab of dest emails in error
   */
  private String[] sendLetterByMail(InfoLetterPublicationPdC ilp, Set<String> emails) {
    Set<String> emailErrors = new LinkedHashSet<>();

    if (!emails.isEmpty()) {
      // create the Multipart and its parts to it
      String mimeMultipart = getSettings().getString("SMTPMimeMultipart", "related");

      // Subject of the mail
      String subject = getString("infoLetter.emailSubject") + ilp.getName();

      // Email address of the manager
      String emailFrom = getUserDetail().getEmailAddress();

      ilp.setInstanceId(getComponentId());
      emailErrors =
          dataInterface.sendLetterByMail(ilp, mimeMultipart, emails, subject, emailFrom);

    }
    return emailErrors.toArray(new String[0]);
  }

  /**
   * Send letter by mail to external subscribers
   * @param ilp the classified InfoLetterPublication to send
   * @return tab of emails in error
   */
  public String[] sendByMailToExternalSubscribers(InfoLetterPublicationPdC ilp) {
    // Recuperation de la liste de emails
    Set<String> extmails = getEmailsExternalsSubscribers();
    // Removing potential already sent emails
    if (isNewsLetterSendByMail()) {
      // Internal subscribers
      SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes =
          dataInterface.getInternalSuscribers(getComponentId()).indexBySubscriberType();
      Set<String> internalSubscribersEmails = getEmailsInternalSubscribers(subscriberIdsByTypes);
      extmails.removeAll(internalSubscribersEmails);
    }
    return sendLetterByMail(ilp, extmails);
  }

  /**
   * Send letter to itself.
   */
  public String[] notifyMe(InfoLetterPublicationPdC ilp) {
    return sendLetterByMail(ilp, singleton(getUserDetail().getEmailAddress()));
  }

  /**
   * Send letter to managers
   */
  public String[] notifyManagers(InfoLetterPublicationPdC ilp) {
    // Recuperation de la liste de emails
    return sendLetterByMail(ilp, getEmailsManagers());
  }

  public Set<String> getEmailsExternalsSubscribers() {
    return dataInterface.getEmailsExternalsSuscribers(getInfoLetter().getPK());
  }

  public void addExternalsSubscribers(String newMails) {
    StringTokenizer st = new StringTokenizer(newMails);
    Set<String> emails = getEmailsExternalsSubscribers();
    while (st.hasMoreTokens()) {
      String mail = st.nextToken().trim();
      if (mail.indexOf('@') > -1) { // Current address contains arobase
        emails.add(mail);
      }
    }
    dataInterface.setEmailsExternalsSubscribers(getInfoLetter().getPK(), emails);
  }

  public void deleteExternalsSubscribers(String[] mails) {
    if (mails != null) {
      Set<String> curExternalEmails = getEmailsExternalsSubscribers();
      for (String email : mails) {
        curExternalEmails.remove(email);
      }
      dataInterface.setEmailsExternalsSubscribers(getInfoLetter().getPK(), curExternalEmails);
    }
  }

  /**
   * Remove all external emails
   */
  public void deleteAllExternalsSubscribers() {
    Set<String> externalEmails = getEmailsExternalsSubscribers();
    externalEmails.clear();
    dataInterface.setEmailsExternalsSubscribers(getInfoLetter().getPK(), externalEmails);
  }

  // Abonnement d'un utilisateur
  public void subscribeUser() {
    dataInterface.toggleSuscriber(getUserId(), getComponentId(), true);
  }

  // Desabonnement d'un utilisateur
  public void unsubscribeUser() {
    dataInterface.toggleSuscriber(getUserId(), getComponentId(), false);
  }

  // test d'abonnement d'un utilisateur interne
  public boolean isSubscriber() {
    return dataInterface.isUserSuscribed(getUserId(), getComponentId());
  }

  public boolean isPdcUsed() {
    return getBooleanValue(getComponentParameterValue("usepdc"));
  }

  /**
   * Import email addresses of external subscribers to newsletters.
   * @param filePart the uploaded CSV file.
   * @throws UtilTrappedException if an error occurs
   * @throws InfoLetterPeasTrappedException if an error occurs during the import.
   * @throws InfoLetterException if an error occurs during the import
   */
  public void importCsvEmails(FileItem filePart)
      throws UtilTrappedException, InfoLetterPeasTrappedException, InfoLetterException {
    InputStream is;
    try {
      is = filePart.getInputStream();
    } catch (IOException e) {
      InfoLetterPeasTrappedException ie =
          new InfoLetterPeasTrappedException("InfoLetterSessionController.importCsvEmails",
              SilverpeasException.ERROR, "infoLetter.EX_CSV_FILE", e);
      ie.setGoBackPage(EMAILS);
      throw ie;
    }
    CSVReader csvReader = new CSVReader(getLanguage());
    csvReader.initCSVFormat("org.silverpeas.infoLetter.settings.usersCSVFormat", "User", ";");

    Variant[][] csvValues;
    try {
      csvValues = csvReader.parseStream(is);
    } catch (UtilTrappedException ute) {
      ute.setGoBackPage(EMAILS);
      throw ute;
    }

    StringBuilder listErrors = new StringBuilder();
    String email;

    for (int i = 0; i < csvValues.length; i++) {
      // email
      email = csvValues[i][0].getValueString();
      if (email.isEmpty()) {// champ obligatoire
        listErrors.append(getString("GML.ligne")).append(" = ").append(i + 1)
            .append(", ");
        listErrors.append(getString("GML.colonne")).append(" = 1, ");
        listErrors.append(getString("GML.valeur")).append(" = ").append(email).append(", ");
        listErrors.append(getString("GML.obligatoire")).append("<BR>");
      } else if (email.length() > 100) {// verifier 100 char max
        listErrors.append(getString("GML.ligne")).append(" = ").append(i + 1)
            .append(", ");
        listErrors.append(getString("GML.colonne")).append(" = 1, ");
        listErrors.append(getString("GML.valeur")).append(" = ").append(email).append(", ");
        listErrors.append(getString("GML.nbCarMax")).append(" 100 ")
            .append(getString("GML.caracteres")).append("<BR>");
      }
    }

    if (listErrors.length() > 0) {
      InfoLetterPeasTrappedException ie =
          new InfoLetterPeasTrappedException("InfoLetterSessionController.importCsvEmails",
              SilverpeasException.ERROR, "infoLetter.EX_CSV_FILE", listErrors.toString());
      ie.setGoBackPage(EMAILS);
      throw ie;
    }

    // no errors, email addresses are imported
    Set<String> emails = new LinkedHashSet<>();
    for (final Variant[] csvValue : csvValues) {
      // Email
      email = csvValue[0].getValueString();
      emails.add(email);
    }

    dataInterface.setEmailsExternalsSubscribers(this.getInfoLetter().getPK(), emails);
  }

  /**
   * Export emails of external subscribers into a CSV file.
   * @return true if the export succeed.
   * @throws IOException if an error occurs while recording the emails to the CSV file.
   * @throws InfoLetterException if an error occurs during the export.
   */
  public boolean exportCsvEmails() throws IOException, InfoLetterException {
    File fileOutput =
        new File(FileRepositoryManager.getTemporaryPath(), getComponentId() + EXPORT_CSV_NAME);
    if (fileOutput.exists()) {//delete the existing file and recreate new one
      FileUtils.forceDelete(fileOutput);
      fileOutput =
          new File(FileRepositoryManager.getTemporaryPath(), getComponentId() + EXPORT_CSV_NAME);
    }
    try {
      Set<String> emails = getEmailsExternalsSubscribers();
      for (String email : emails) {
        FileUtils.writeStringToFile(fileOutput, email + "\n", Charsets.UTF_8, true);
      }
    } catch (Exception e) {
      throw new InfoLetterException(e);
    }
    return true;
  }

  /**
   * Get emails of component Manager
   * @return Set of emails
   */
  private Set<String> getEmailsManagers() {
    final List<String> roles = List.of("admin");
    String[] managerIds = getOrganisationController().getUsersIdsByRoleNames(getComponentId(),
        roles);
    return getEmailAddressOf(List.of(managerIds), User::isValidState);
  }

  /**
   * Get emails of internal subscribers
   * @return Set of emails
   */
  private Set<String> getEmailsInternalSubscribers(
      SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes) {
    List<String> userIds = subscriberIdsByTypes.getAllUserIds();
    return getEmailAddressOf(userIds, null);
  }

  /**
   * return true if Newsletter is send by mail to internal users
   * @return boolean
   */
  public boolean isNewsLetterSendByMail() {
    return getBooleanValue(getComponentParameterValue("sendNewsletter"));
  }

  private Set<String> getEmailAddressOf(List<String> userIds, Predicate<UserDetail> filter) {
    // we use Administration to get user details in order to access the email addresses even for
    // the users with privacy data
    Function<String, UserDetail> userById = userId -> {
      try {
        return Administration.get().getUserDetail(userId);
      } catch (AdminException e) {
        SilverLogger.getLogger(this).error(e);
        return null;
      }
    };
    Predicate<UserDetail> filterOnUser = filter == null ? u -> true : filter;

    return userIds.stream()
        .filter(StringUtil::isDefined)
        .map(userById)
        .filter(Objects::nonNull)
        .filter(filterOnUser)
        .map(UserDetail::getEmailAddress)
        .filter(StringUtil::isDefined)
        .collect(Collectors.toSet());
  }
}
