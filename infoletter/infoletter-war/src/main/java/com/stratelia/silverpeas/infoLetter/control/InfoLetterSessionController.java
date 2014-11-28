/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.infoLetter.control;


import com.silverpeas.pdc.PdcServiceProvider;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.pdc.web.PdcClassificationEntity;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.ui.DisplayI18NHelper;
import com.stratelia.silverpeas.infoLetter.InfoLetterException;
import com.stratelia.silverpeas.infoLetter.InfoLetterPeasTrappedException;
import com.stratelia.silverpeas.infoLetter.model.InfoLetter;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterDataInterface;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterPublication;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterPublicationPdC;
import com.stratelia.silverpeas.notificationManager.GroupRecipient;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.IdPK;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;
import org.silverpeas.util.FileRepositoryManager;
import org.silverpeas.util.Pair;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.WAPrimaryKey;
import org.silverpeas.util.csv.CSVReader;
import org.silverpeas.util.csv.CSVWriter;
import org.silverpeas.util.csv.Variant;
import org.silverpeas.util.exception.DecodingException;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.util.exception.SilverpeasRuntimeException;
import org.silverpeas.util.exception.UtilTrappedException;
import org.silverpeas.util.i18n.I18NHelper;
import org.silverpeas.util.template.SilverpeasTemplate;
import org.silverpeas.util.template.SilverpeasTemplateFactory;
import org.silverpeas.wysiwyg.control.WysiwygController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import static com.silverpeas.pdc.model.PdcClassification.aPdcClassificationOfContent;

/**
 * Class declaration
 *
 * @author
 */
public class InfoLetterSessionController extends AbstractComponentSessionController {

  /**
   * Interface metier du composant
   */
  private InfoLetterDataInterface dataInterface = null;
  
  public final static String EXPORT_CSV_NAME = "_emails.csv";

  /**
   * Standard Session Controller Constructeur
   *
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public InfoLetterSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.infoLetter.multilang.infoLetterBundle",
        "org.silverpeas.infoLetter.settings.infoLetterIcons",
        "org.silverpeas.infoLetter.settings.infoLetterSettings");
    // Initialize business interface
    if (dataInterface == null) {
      dataInterface = ServiceFactory.getInfoLetterData();
    }
  }

  /*
   * Initialize UserPanel with the list of Silverpeas subscribers
   */
  public String initUserPanel() throws InfoLetterException {
    String hostSpaceName = getSpaceLabel();
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(),
        URLManager.getApplicationURL() + "/RinfoLetter/" + getComponentId() + "/Main");
    String hostUrl = URLManager.getApplicationURL() + "/RinfoLetter/" + getComponentId()
        + "/RetourPanel";
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
    Map<SubscriberType, Collection<String>> subscriberIdsByTypes =
        dataInterface.getInternalSuscribers(getComponentId());
    // Users
    sel.setSelectedElements(subscriberIdsByTypes.get(SubscriberType.USER));
    // Groups
    sel.setSelectedSets(subscriberIdsByTypes.get(SubscriberType.GROUP));

    if (sel.getSelectedElements().length == 0 && sel.getSelectedSets().length == 0) {
      sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);
    } else {
      sel.setFirstPage(Selection.FIRST_PAGE_CART);
    }
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
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
    File template = new File(WysiwygController.getWysiwygPath(getComponentId(),
        InfoLetterPublication.TEMPLATE_ID + ilp.getLetterId()));

    String content = "";
    if (template.exists() && template.isFile()) {
      content = FileUtils.readFileToString(template);
    }
    WysiwygController.save(content, getComponentId(), ilp.getId(), getUserId(),
        I18NHelper.defaultLanguage, true);
    // Classify content on PdC
    classifyInfoLetterPublication(ilp);
  }

  /**
   * Classify the info letter publication on the PdC only if the positions attribute is filled
   * inside object parameter
   *
   * @param ilp the InfoLetterPublication to classify
   */
  private void classifyInfoLetterPublication(InfoLetterPublicationPdC ilp) {
    String positions = ilp.getPositions();
    if (StringUtil.isDefined(positions)) {
      PdcClassificationEntity ilClassification = null;
      try {
        ilClassification = PdcClassificationEntity.fromJSON(positions);
      } catch (DecodingException e) {
        SilverTrace.error("Forum", "ForumActionHelper.actionManagement",
            "PdcClassificationEntity error", "Problem to read JSON", e);
      }
      if (ilClassification != null && !ilClassification.isUndefined()) {
        List<PdcPosition> pdcPositions = ilClassification.getPdcPositions();
        String ilpId = ilp.getPK().getId();
        PdcClassification classification = aPdcClassificationOfContent(ilpId, ilp.getInstanceId()).
            withPositions(pdcPositions);
        if (!classification.isEmpty()) {
          PdcClassificationService service = PdcServiceProvider.getPdcClassificationService();
          classification.ofContent(ilpId);
          service.classifyContent(ilp, classification);
        }
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
          "com.stratelia.silverpeas.infoLetter.control.InfoLetterSessionController",
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
    ResourceLocator rs =
        new ResourceLocator("org.silverpeas.infoLetter.settings.infoLetterSettings", "");
    Properties templateConfiguration = new Properties();
    templateConfiguration
        .setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, rs.getString("templatePath"));
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, rs.getString(
        "customersTemplatePath"));

    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfiguration);
  }

  // Notification des abonnes internes
  public void notifySuscribers(InfoLetterPublicationPdC ilp) {
    NotificationSender ns = new NotificationSender(getComponentId());
    String sSubject = getString("infoLetter.emailSubject") + ilp.getName();

    try {
      Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
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
        template.setAttribute("silverpeasURL", url);

        ResourceLocator localizedMessage = new ResourceLocator(
            "org.silverpeas.infoLetter.multilang.infoLetterBundle", lang);
        notifMetaData.addLanguage(lang, localizedMessage.getString("infoLetter.emailSubject",
            getString("infoLetter.emailSubject")) + ilp.getName(), "");
      }
      notifMetaData.setSender(getUserId());
      notifMetaData.setSource(getSpaceLabel() + " - " + getComponentLabel());
      notifMetaData.setLink(url);

      // Internal subscribers
      Map<SubscriberType, Collection<String>> subscriberIdsByTypes =
          dataInterface.getInternalSuscribers(getComponentId());
      for (String userId : subscriberIdsByTypes.get(SubscriberType.USER)) {
        notifMetaData.addUserRecipient(new UserRecipient(userId));
      }
      for (String groupId : subscriberIdsByTypes.get(SubscriberType.GROUP)) {
        notifMetaData.addGroupRecipient(new GroupRecipient(groupId));
      }

      ns.notifyUser(notifMetaData);

    } catch (com.stratelia.silverpeas.notificationManager.NotificationManagerException e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.control.InfoLetterSessionController",
          SilverpeasRuntimeException.ERROR, e.getMessage(), e);
    }
  }
  
  
  // Notification des abonnes externes
  public String[] notifyExternals(InfoLetterPublicationPdC ilp, String server, List<String> emails) {
    List<String> emailErrors = new ArrayList<String>();

    if (emails.size() > 0) {
      // create the Multipart and its parts to it
      String mimeMultipart = getSettings().getString("SMTPMimeMultipart", "related");
      
      // Subject of the mail
      String subject = getString("infoLetter.emailSubject") + ilp.getName();
      
      // Email address of the manager
      String emailFrom = getUserDetail().geteMail();
      
      ilp.setInstanceId(getComponentId());
      emailErrors = dataInterface.notifyExternals(ilp, server, mimeMultipart, emails, subject, emailFrom);

    }
    return emailErrors.toArray(new String[emailErrors.size()]);
  }

  // Notification des abonnes externes
  public String[] notifyExternals(InfoLetterPublicationPdC ilp, String server) {
    IdPK letterPK = new IdPK(String.valueOf(ilp.getLetterId()));

    // Recuperation de la liste de emails
    List<String> extmails = getExternalsSuscribers(letterPK);

    return notifyExternals(ilp, server, extmails);
  }

  /**
   * Send letter to managers
   *
   * @param ilp
   * @param server
   * @return
   */
  public String[] notifyManagers(InfoLetterPublicationPdC ilp, String server) {
    // Recuperation de la liste de emails
    List<String> extmails = getEmailsManagers();

    return notifyExternals(ilp, server, extmails);
  }

  // Recuperation de la liste des emails externes
  public List<String> getExternalsSuscribers(WAPrimaryKey letterPK) {
    return new ArrayList<String>(dataInterface.getExternalsSuscribers(letterPK));
  }

  // Ajouter des emails externes
  public void addExternalsSuscribers(WAPrimaryKey letterPK, String newmails) {
    StringTokenizer st = new StringTokenizer(newmails);
    List<String> emails = getExternalsSuscribers(letterPK);
    while (st.hasMoreTokens()) {
      String mail = st.nextToken().trim();
      if (mail.indexOf('@') > -1) { // Current address contains arobase
        if (!emails.contains(mail)) {
          emails.add(mail);
        }
      }
    }
    dataInterface.setExternalsSuscribers(letterPK, emails);
  }

  // Supprimer des emails externes
  public void deleteExternalsSuscribers(WAPrimaryKey letterPK, String[] mails) {
    if (mails != null) {
      List<String> curExternalEmails = getExternalsSuscribers(letterPK);
      for (String email : mails) {
        curExternalEmails.remove(email);
      }
      dataInterface.setExternalsSuscribers(letterPK, curExternalEmails);
    }
  }

  /**
   * Remove all external emails
   *
   * @param letterPK
   */
  public void deleteAllExternalsSuscribers(WAPrimaryKey letterPK) {
    List<String> externalEmails = getExternalsSuscribers(letterPK);
    externalEmails.clear();
    dataInterface.setExternalsSuscribers(letterPK, externalEmails);
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
      WysiwygController.copy(getComponentId(), publicationSource, getComponentId(), target,
          getUserId());
    } catch (Exception e) {
      throw new InfoLetterException("InfoLetterSessionController.copyWYSIWYG",
          SilverpeasRuntimeException.ERROR, e.getMessage(), e);
    }
  }

  public boolean isTemplateExist(InfoLetterPublicationPdC ilp) {
    String template = WysiwygController.load(getComponentId(),
        InfoLetterPublication.TEMPLATE_ID + ilp.getLetterId(), I18NHelper.defaultLanguage);
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
      indexEntry.setPreView(ilp.getDescription());
      indexEntry.setCreationDate(ilp.getParutionDate());
      indexEntry.setCreationUser(getUserId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  // Suppression de l'index d'une publication
  private void deleteIndex(InfoLetterPublicationPdC ilp) {
    IndexEntryPK indexEntry =
        new IndexEntryPK(getComponentId(), "Publication", ilp.getPK().getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  // Indexation d'une lettre
  public void createIndex(InfoLetter il) {
    if (il != null) {
      FullIndexEntry indexEntry =
          new FullIndexEntry(getComponentId(), "Lettre", il.getPK().getId());
      indexEntry.setTitle(il.getName());
      indexEntry.setPreView(il.getDescription());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  // Suppression de l'index d'une lettre
  private void deleteIndex(InfoLetter il) {
    IndexEntryPK indexEntry = new IndexEntryPK(getComponentId(), "Publication", il.getPK().getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  public boolean isPdcUsed() {
    String parameterValue = getComponentParameterValue("usepdc");
    return "yes".equals(parameterValue.toLowerCase());
  }

  /**
   * Method declaration
   *
   * @return
   */
  private InfoLetter getCurrentLetter() {
    List<InfoLetter> listLettres = getInfoLetters();
    return listLettres.get(0);
  }

  /**
   * Import Csv emails
   *
   * @param filePart
   * @throws UtilTrappedException
   * @throws InfoLetterPeasTrappedException
   * @throws InfoLetterException
   */
  public void importCsvEmails(FileItem filePart) throws UtilTrappedException,
      InfoLetterPeasTrappedException, InfoLetterException {
    InputStream is;
    try {
      is = filePart.getInputStream();
    } catch (IOException e) {
      InfoLetterPeasTrappedException ie = new InfoLetterPeasTrappedException(
          "InfoLetterSessionController.importCsvEmails", SilverpeasException.ERROR,
          "infoLetter.EX_CSV_FILE", e);
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
      InfoLetterPeasTrappedException ie = new InfoLetterPeasTrappedException(
          "InfoLetterSessionController.importCsvEmails", SilverpeasException.ERROR,
          "infoLetter.EX_CSV_FILE", listErrors.toString());
      ie.setGoBackPage("Emails");
      throw ie;
    }

    // pas d'erreur, on importe les emails
    List<String> emails = new ArrayList<String>();
    for (final Variant[] csvValue : csvValues) {
      // Email
      email = csvValue[0].getValueString();
      emails.add(email);
    }
    
    // suppression des doublons de la liste tout en gardant l'ordre
    Set<String> setEmails = new LinkedHashSet<String>(emails);
    emails.clear();
    emails.addAll(setEmails);
    
    dataInterface.setExternalsSuscribers(this.getCurrentLetter().getPK(), emails);
  }

  /**
   * Export Csv emails
   *
   * @throws IOException
   * @throws InfoLetterException
   * @return boolean
   */
  public boolean exportCsvEmails() throws IOException, InfoLetterException {
    boolean exportOk = true;
    File fileOutput =
        new File(FileRepositoryManager.getTemporaryPath(), getComponentId() + EXPORT_CSV_NAME);
    try {
      List<String> emails = getExternalsSuscribers(getCurrentLetter().getPK());

      CSVWriter csvWriter = new CSVWriter(getLanguage());
      csvWriter.initCSVFormat("org.silverpeas.infoLetter.settings.usersCSVFormat", "User", ";");

      for (String email : emails) {
        FileUtils.writeStringToFile(fileOutput, email+"\n", true);
      }
    } catch (Exception e) {
      exportOk = false;
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.control.InfoLetterSessionController",
          SilverpeasRuntimeException.ERROR, e.getMessage(), e);
    }
    return exportOk;
  }

  /**
   * Get emails of component Manager
   *
   * @return Vector of emails
   */
  private List<String> getEmailsManagers() {
    List<String> emails = new ArrayList<String>();
    List<String> roles = new ArrayList<String>();
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
}
