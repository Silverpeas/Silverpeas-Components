/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.components.infoletter.control;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.silverpeas.components.infoletter.InfoLetterException;
import org.silverpeas.components.infoletter.InfoLetterPeasTrappedException;
import org.silverpeas.components.infoletter.model.InfoLetter;
import org.silverpeas.components.infoletter.model.InfoLetterPublication;
import org.silverpeas.components.infoletter.model.InfoLetterPublicationPdC;
import org.silverpeas.components.infoletter.model.InfoLetterService;
import org.silverpeas.components.infoletter.service.InfoLetterServiceProvider;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.exception.DecodingException;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.exception.UtilTrappedException;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.notification.user.client.GroupRecipient;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberMapBySubscriberType;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;

/**
 * Class declaration
 * @author
 */
public class InfoLetterSessionController extends AbstractComponentSessionController {

  /**
   * Interface metier du composant
   */
  private InfoLetterService dataInterface = null;

  public final static String EXPORT_CSV_NAME = "_emails.csv";

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
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
    deleteIndex(ie);
    createIndex(ie);
  }

  // Recuperation de la liste des lettres
  public List<InfoLetter> getInfoLetters() {
    return dataInterface.getInfoLetters(getComponentId());
  }

  // Recuperation de la liste des publications
  public List<InfoLetterPublication> getInfoLetterPublications(WAPrimaryKey letterPK) {
    return dataInterface.getInfoLetterPublications(letterPK);
  }

  // Creation d'une publication
  public void createInfoLetterPublication(InfoLetterPublicationPdC ilp) throws IOException {
    ilp.setInstanceId(getComponentId());
    dataInterface.createInfoLetterPublication(ilp, getUserId());
    File template = new File(WysiwygController
        .getWysiwygPath(getComponentId(), InfoLetterPublication.TEMPLATE_ID + ilp.getLetterId()));

    String content = "";
    if (template.exists() && template.isFile()) {
      content = FileUtils.readFileToString(template);
    }
    WysiwygController
        .save(content, getComponentId(), ilp.getId(), getUserId(), I18NHelper.defaultLanguage,
            true);
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
        SilverTrace
            .error("Forum", "ForumActionHelper.actionManagement", "PdcClassificationEntity error",
                "Problem to read JSON", e);
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
  public void deleteInfoLetterPublication(WAPrimaryKey pk) {
    deleteIndex(getInfoLetterPublication(pk));
    try {
      WysiwygController.deleteWysiwygAttachments(getComponentId(), pk.getId());
    } catch (Exception e) {
      throw new InfoLetterException(
          "InfoLetterSessionController",
          SilverpeasRuntimeException.ERROR, e.getMessage(), e);
    }
    dataInterface.deleteInfoLetterPublication(pk, getComponentId());
  }

  // Mise a jour d'une publication
  public void updateInfoLetterPublication(InfoLetterPublicationPdC ilp) {
    ilp.setInstanceId(getComponentId());
    dataInterface.updateInfoLetterPublication(ilp);
  }

  // Recuperation d'une publication par sa clef
  public InfoLetterPublicationPdC getInfoLetterPublication(WAPrimaryKey publiPK) {
    return dataInterface.getInfoLetterPublication(publiPK);
  }

  protected SilverpeasTemplate getNotificationMessageTemplate() {
    SettingBundle settings =
        ResourceLocator.getSettingBundle("org.silverpeas.infoLetter.settings.infoLetterSettings");
    Properties templateConfiguration = new Properties();
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR,
        settings.getString("templatePath"));
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR,
        settings.getString("customersTemplatePath"));

    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfiguration);
  }

  /**
   * Notify the newsletter to internal subscribers
   * @param ilp the infoletter to send
   * @param server
   */
  public void notifyInternalSuscribers(InfoLetterPublicationPdC ilp, String server) {

    // Internal subscribers
    SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes =
        dataInterface.getInternalSuscribers(getComponentId()).indexBySubscriberType();

    //Send the newsletter by Mail to internal subscribers
    if (isNewsLetterSendByMail()) {

      Set<String> internalSubscribersEmails = getEmailsInternalSubscribers(subscriberIdsByTypes);
      sendLetterByMail(ilp, server, internalSubscribersEmails);

    } else {
      //Send the newsletter via notification

      NotificationSender ns = new NotificationSender(getComponentId());
      String sSubject = getString("infoLetter.emailSubject") + ilp.getName();

      try {
        Map<String, SilverpeasTemplate> templates = new HashMap<>();
        NotificationMetaData notifMetaData =
            new NotificationMetaData(NotificationParameters.NORMAL, sSubject, templates,
                "infoLetterNotification");

        String url = "/RinfoLetter/" + getComponentId() + "/View?parution=" + ilp.getPK().getId();
        for (String lang : DisplayI18NHelper.getLanguages()) {
          SilverpeasTemplate template = getNotificationMessageTemplate();
          templates.put(lang, template);
          template.setAttribute("infoLetter", ilp);
          template.setAttribute("infoLetterTitle", ilp.getName(lang));
          String desc = ilp.getDescription(lang);
          if ("".equals(desc)) {
            desc = null;
          }
          template.setAttribute("infoLetterDesc", desc);
          template.setAttribute("senderName", getUserDetail().getDisplayedName());

          LocalizationBundle localizedMessage = ResourceLocator.getLocalizationBundle(
              "org.silverpeas.infoLetter.multilang.infoLetterBundle", lang);
          String emailSubject =  localizedMessage.getString("infoLetter.emailSubject");
          if (!StringUtil.isDefined(emailSubject)) {
            emailSubject = getString("infoLetter.emailSubject");
          }
          notifMetaData.addLanguage(lang, emailSubject + ilp.getName(), "");

          Link link = new Link(url, localizedMessage.getString("infoLetter.notifLinkLabel"));
          notifMetaData.setLink(link, lang);
        }
        notifMetaData.setSender(getUserId());
        notifMetaData.setSource(getSpaceLabel() + " - " + getComponentLabel());
        notifMetaData.displayReceiversInFooter();

        // Internal subscribers
        for (String userId : subscriberIdsByTypes.get(SubscriberType.USER).getAllIds()) {
          notifMetaData.addUserRecipient(new UserRecipient(userId));
        }
        for (String groupId : subscriberIdsByTypes.get(SubscriberType.GROUP).getAllIds()) {
          notifMetaData.addGroupRecipient(new GroupRecipient(groupId));
        }

        ns.notifyUser(notifMetaData);

      } catch (NotificationManagerException e) {
        throw new InfoLetterException(
            "InfoLetterSessionController",
            SilverpeasRuntimeException.ERROR, e.getMessage(), e);
      }
    }
  }


  /**
   * Send letter by mail
   * @param ilp
   * @param server
   * @param emails
   * @return tab of dest emails in error
   */
  public String[] sendLetterByMail(InfoLetterPublicationPdC ilp, String server,
      Set<String> emails) {
    Set<String> emailErrors = new LinkedHashSet<>();

    if (emails.size() > 0) {
      // create the Multipart and its parts to it
      String mimeMultipart = getSettings().getString("SMTPMimeMultipart", "related");

      // Subject of the mail
      String subject = getString("infoLetter.emailSubject") + ilp.getName();

      // Email address of the manager
      String emailFrom = getUserDetail().geteMail();

      ilp.setInstanceId(getComponentId());
      emailErrors =
          dataInterface.sendLetterByMail(ilp, server, mimeMultipart, emails, subject, emailFrom);

    }
    return emailErrors.toArray(new String[emailErrors.size()]);
  }

  /**
   * Send letter by mail to external subscribers
   * @param ilp
   * @param server
   * @return tab of emails in error
   */
  public String[] sendByMailToExternalSubscribers(InfoLetterPublicationPdC ilp, String server) {
    IdPK letterPK = new IdPK(String.valueOf(ilp.getLetterId()));

    // Recuperation de la liste de emails
    Set<String> extmails = getEmailsExternalsSuscribers(letterPK);

    // Removing potential already sent emails
    if (isNewsLetterSendByMail()) {
      // Internal subscribers
      SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes =
          dataInterface.getInternalSuscribers(getComponentId()).indexBySubscriberType();
      Set<String> internalSubscribersEmails = getEmailsInternalSubscribers(subscriberIdsByTypes);
      extmails.removeAll(internalSubscribersEmails);
    }

    return sendLetterByMail(ilp, server, extmails);
  }

  /**
   * Send letter to managers
   * @param ilp
   * @param server
   * @return
   */
  public String[] notifyManagers(InfoLetterPublicationPdC ilp, String server) {
    // Recuperation de la liste de emails
    Set<String> extmails = getEmailsManagers();

    return sendLetterByMail(ilp, server, extmails);
  }

  public Set<String> getEmailsExternalsSuscribers(WAPrimaryKey letterPK) {
    return dataInterface.getEmailsExternalsSuscribers(letterPK);
  }

  public void addExternalsSuscribers(WAPrimaryKey letterPK, String newmails) {
    StringTokenizer st = new StringTokenizer(newmails);
    Set<String> emails = getEmailsExternalsSuscribers(letterPK);
    while (st.hasMoreTokens()) {
      String mail = st.nextToken().trim();
      if (mail.indexOf('@') > -1) { // Current address contains arobase
        if (!emails.contains(mail)) {
          emails.add(mail);
        }
      }
    }
    dataInterface.setEmailsExternalsSubscribers(letterPK, emails);
  }

  public void deleteExternalsSuscribers(WAPrimaryKey letterPK, String[] mails) {
    if (mails != null) {
      Set<String> curExternalEmails = getEmailsExternalsSuscribers(letterPK);
      for (String email : mails) {
        curExternalEmails.remove(email);
      }
      dataInterface.setEmailsExternalsSubscribers(letterPK, curExternalEmails);
    }
  }

  /**
   * Remove all external emails
   * @param letterPK
   */
  public void deleteAllExternalsSuscribers(WAPrimaryKey letterPK) {
    Set<String> externalEmails = getEmailsExternalsSuscribers(letterPK);
    externalEmails.clear();
    dataInterface.setEmailsExternalsSubscribers(letterPK, externalEmails);
  }

  // Abonnement d'un utilisateur
  public void suscribeUser() {
    dataInterface.toggleSuscriber(getUserId(), getComponentId(), true);
  }

  // Desabonnement d'un utilisateur
  public void unsuscribeUser() {
    dataInterface.toggleSuscriber(getUserId(), getComponentId(), false);
  }

  // test d'abonnement d'un utilisateur interne
  public boolean isSuscriber() {
    return dataInterface.isUserSuscribed(getUserId(), getComponentId());
  }

  // Mise a jour du template a partir d'une publication
  public void updateTemplate(InfoLetterPublicationPdC ilp) {
    copyWYSIWYG(ilp.getPK().getId(), InfoLetterPublication.TEMPLATE_ID + ilp.getLetterId());
  }

  // copie d'un repertoire wysiwyg vers un autre
  public void copyWYSIWYG(String publicationSource, String target) {
    try {
      if (WysiwygController.haveGotWysiwyg(getComponentId(), target, I18NHelper.defaultLanguage)) {
        WysiwygController.deleteWysiwygAttachments(getComponentId(), target);
      }
      WysiwygController
          .copy(getComponentId(), publicationSource, getComponentId(), target, getUserId());
    } catch (Exception e) {
      throw new InfoLetterException("InfoLetterSessionController.copyWYSIWYG",
          SilverpeasRuntimeException.ERROR, e.getMessage(), e);
    }
  }

  public boolean isTemplateExist(InfoLetterPublicationPdC ilp) {
    String template = WysiwygController
        .load(getComponentId(), InfoLetterPublication.TEMPLATE_ID + ilp.getLetterId(),
            I18NHelper.defaultLanguage);
    return !"<body></body>".equalsIgnoreCase(template);
  }

  public String getTemplate(InfoLetterPublicationPdC ilp) {
    return WysiwygController.getWysiwygPath(getComponentId(), I18NHelper.defaultLanguage,
        InfoLetterPublication.TEMPLATE_ID + ilp.getLetterId());
  }

  // Indexation d'une publication
  public void createIndex(InfoLetterPublicationPdC ilp) {
    if (ilp != null) {
      FullIndexEntry indexEntry =
          new FullIndexEntry(getComponentId(), "Publication", ilp.getPK().getId());
      indexEntry.setTitle(ilp.getTitle());
      indexEntry.setPreview(ilp.getDescription());
      try {
        indexEntry.setCreationDate(DateUtil.parse(ilp.getParutionDate()));
      } catch (ParseException e) {
        SilverLogger.getLogger(this).warn(e);
      }
      indexEntry.setCreationUser(getUserId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  // Suppression de l'index d'une publication
  private void deleteIndex(InfoLetterPublicationPdC ilp) {
    IndexEntryKey indexEntry =
        new IndexEntryKey(getComponentId(), "Publication", ilp.getPK().getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  // Indexation d'une lettre
  public void createIndex(InfoLetter il) {
    if (il != null) {
      FullIndexEntry indexEntry =
          new FullIndexEntry(getComponentId(), "Lettre", il.getPK().getId());
      indexEntry.setTitle(il.getName());
      indexEntry.setPreview(il.getDescription());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  // Suppression de l'index d'une lettre
  private void deleteIndex(InfoLetter il) {
    IndexEntryKey indexEntry = new IndexEntryKey(getComponentId(), "Publication", il.getPK().getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  public boolean isPdcUsed() {
    String parameterValue = getComponentParameterValue("usepdc");
    return "yes".equals(parameterValue.toLowerCase());
  }

  /**
   * Method declaration
   * @return
   */
  private InfoLetter getCurrentLetter() {
    List<InfoLetter> listLettres = getInfoLetters();
    return listLettres.get(0);
  }

  /**
   * Import Csv emails
   * @param filePart
   * @throws UtilTrappedException
   * @throws InfoLetterPeasTrappedException
   * @throws InfoLetterException
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
      ie.setGoBackPage("Emails");
      throw ie;
    }
    CSVReader csvReader = new CSVReader(getLanguage());
    csvReader.initCSVFormat("org.silverpeas.infoLetter.settings.usersCSVFormat", "User", ";");

    Variant[][] csvValues;
    try {
      csvValues = csvReader.parseStream(is);
    } catch (UtilTrappedException ute) {
      ute.setGoBackPage("Emails");
      throw ute;
    }

    StringBuilder listErrors = new StringBuilder("");
    String email;

    for (int i = 0; i < csvValues.length; i++) {
      // email
      email = csvValues[i][0].getValueString();
      if (email.length() == 0) {// champ obligatoire
        listErrors.append(getString("GML.ligne")).append(" = ").append(Integer.toString(i + 1))
            .append(", ");
        listErrors.append(getString("GML.colonne")).append(" = 1, ");
        listErrors.append(getString("GML.valeur")).append(" = ").append(email).append(", ");
        listErrors.append(getString("GML.obligatoire")).append("<BR>");
      } else if (email.length() > 100) {// verifier 100 char max
        listErrors.append(getString("GML.ligne")).append(" = ").append(Integer.toString(i + 1))
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
      ie.setGoBackPage("Emails");
      throw ie;
    }

    // pas d'erreur, on importe les emails
    Set<String> emails = new LinkedHashSet<>();
    for (final Variant[] csvValue : csvValues) {
      // Email
      email = csvValue[0].getValueString();
      emails.add(email);
    }

    dataInterface.setEmailsExternalsSubscribers(this.getCurrentLetter().getPK(), emails);
  }

  /**
   * Export Csv emails
   * @return boolean
   * @throws IOException
   * @throws InfoLetterException
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
      Set<String> emails = getEmailsExternalsSuscribers(getCurrentLetter().getPK());

      for (String email : emails) {
        FileUtils.writeStringToFile(fileOutput, email + "\n", true);
      }
    } catch (Exception e) {
      throw new InfoLetterException(
          "InfoLetterSessionController",
          SilverpeasRuntimeException.ERROR, e.getMessage(), e);
    }
    return true;
  }

  /**
   * Get emails of component Manager
   * @return Set of emails
   */
  private Set<String> getEmailsManagers() {
    Set<String> emails = new LinkedHashSet<>();
    List<String> roles = new ArrayList<>();
    roles.add("admin");
    String[] userIds = getOrganisationController().getUsersIdsByRoleNames(getComponentId(), roles);
    if (userIds != null) {
      for (String userId : userIds) {
        String email = getUserDetail(userId).geteMail();
        if (StringUtil.isDefined(email)) {
          emails.add(email);
        }
      }
    }
    return emails;
  }

  /**
   * Get emails of internal subscribers
   * @return Set of emails
   */
  private Set<String> getEmailsInternalSubscribers(
      SubscriptionSubscriberMapBySubscriberType subscriberIdsByTypes) {
    Set<String> emails = new LinkedHashSet<>();

    for (String userId : subscriberIdsByTypes.getAllUserIds()) {
      String email = getUserDetail(userId).geteMail();
      if (StringUtil.isDefined(email)) {
        emails.add(email);
      }
    }

    return emails;
  }

  /**
   * return true if Newsletter is send by mail to internal users
   * @return boolean
   */
  public boolean isNewsLetterSendByMail() {
    return StringUtil.getBooleanValue(getComponentParameterValue("sendNewsletter"));
  }

  /**
   * save wysiwyg content of the newsletter
   * @param content
   * @param ilp
   */
  public void updateContentInfoLetterPublication(String content, InfoLetterPublicationPdC ilp) {
    // Update the Wysiwyg if exists, create one otherwise
    WysiwygController.updateFileAndAttachment(content, getComponentId(), ilp.getId(), getUserId(),
        I18NHelper.defaultLanguage);
  }
}
