/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

import com.silverpeas.pdc.PdcServiceFactory;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.pdc.web.PdcClassificationEntity;
import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.csv.CSVReader;
import com.silverpeas.util.csv.CSVWriter;
import com.silverpeas.util.csv.Variant;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.infoLetter.InfoLetterException;
import com.stratelia.silverpeas.infoLetter.InfoLetterPeasTrappedException;
import com.stratelia.silverpeas.infoLetter.model.*;
import com.stratelia.silverpeas.notificationManager.*;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilTrappedException;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

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
  /**
   * the tuning parameters
   */
  private static final ResourceLocator smtpSettings = new ResourceLocator(
      "org.silverpeas.notificationserver.channel.smtp.smtpSettings", "");
  public final static String EXPORT_CSV_NAME = "_emails.csv";
  public final static boolean EXPORT_OK = true;

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
  public String initUserPanel(WAPrimaryKey letterPK) throws InfoLetterException {
    String hostSpaceName = getSpaceLabel();
    PairObject hostComponentName = new PairObject(getComponentLabel(),
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

    InternalSubscribers internalSubs = dataInterface.getInternalSuscribers(letterPK);
    List<UserDetail> users = internalSubs.getUsers();
    String[] usersArray = new String[users.size()];
    for (UserDetail userDetail : users) {
      usersArray[users.indexOf(userDetail)] = userDetail.getId();
    }
    List<Group> groups = internalSubs.getGroups();
    String[] groupsArray = new String[groups.size()];
    for (Group group : groups) {
      groupsArray[groups.indexOf(group)] = group.getId();
    }
    sel.setSelectedElements(usersArray);
    sel.setSelectedSets(groupsArray);
    if (usersArray.length == 0 && groupsArray.length == 0) {
      sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);
    } else {
      sel.setFirstPage(Selection.FIRST_PAGE_CART);
    }
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /*
   * Retour du UserPanel
   */
  public void retourUserPanel(WAPrimaryKey letterPK) {
    Selection sel = getSelection();
    UserDetail[] users = SelectionUsersGroups.getUserDetails(sel.getSelectedElements());
    Group[] groups = SelectionUsersGroups.getGroups(sel.getSelectedSets());
    InternalSubscribers internalSubs = new InternalSubscribers(Arrays.asList(users), Arrays.asList(
        groups));
    dataInterface.setInternalSuscribers(letterPK, internalSubs);
  }

  // Creation d'une lettre d'information
  public void createInfoLetter(InfoLetter ie) {
    dataInterface.createInfoLetter(ie);
    createIndex(ie);
  }

  // Suppression d'une lettre d'information
  public void deleteInfoLetter(WAPrimaryKey pk) {
    deleteIndex(dataInterface.getInfoLetter(pk));
    dataInterface.deleteInfoLetter(pk);
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
    WysiwygController.createFileAndAttachment(content, WysiwygController.getWysiwygFileName(
        ilp.getId(), getLanguage()), getComponentId(), DocumentType.wysiwyg.getName(), ilp.getId(),
        getUserId());
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
      } catch (JAXBException e) {
        SilverTrace.error("Forum", "ForumActionHelper.actionManagement",
            "PdcClassificationEntity error", "Problem to read JSON", e);
      }
      if (ilClassification != null && !ilClassification.isUndefined()) {
        List<PdcPosition> pdcPositions = ilClassification.getPdcPositions();
        String ilpId = ilp.getPK().getId();
        PdcClassification classification = aPdcClassificationOfContent(ilpId, ilp.getInstanceId()).
            withPositions(pdcPositions);
        if (!classification.isEmpty()) {
          PdcClassificationService service = PdcServiceFactory.getFactory()
              .getPdcClassificationService();
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

  // Validation d'une publication
  public void validateInfoLetterPublication(InfoLetterPublicationPdC ilp) {
    ilp.setInstanceId(getComponentId());
    dataInterface.validateInfoLetterPublication(ilp);
    createIndex(ilp);
  }

  // Recuperation d'une publication par sa clef
  public InfoLetterPublicationPdC getInfoLetterPublication(WAPrimaryKey publiPK) {
    return dataInterface.getInfoLetterPublication(publiPK);
  }

  protected SilverpeasTemplate getNotificationMessageTemplate() {
    ResourceLocator rs =
        new ResourceLocator("org.silverpeas.infoLetter.settings.infoLetterSettings", "");
    Properties templateConfiguration = new Properties();
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, rs.getString(
        "templatePath"));
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, rs.getString(
        "customersTemplatePath"));

    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfiguration);
  }

  // Notification des abonnes internes
  public void notifySuscribers(InfoLetterPublicationPdC ilp) {
    NotificationSender ns = new NotificationSender(getComponentId());
    IdPK pk = new IdPK();
    pk.setId(String.valueOf(ilp.getLetterId()));
    String sSubject = getString("infoLetter.emailSubject") + ilp.getName();

    InternalSubscribers internalSubs = dataInterface.getInternalSuscribers(pk);
    List<UserDetail> users = internalSubs.getUsers();
    UserDetail[] usersArray = users.toArray(new UserDetail[users.size()]);

    List<Group> groups = internalSubs.getGroups();
    Group[] groupsArray = groups.toArray(new Group[groups.size()]);

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
      for (UserDetail userDetail : usersArray) {
        notifMetaData.addUserRecipient(new UserRecipient(userDetail));
      }
      for (Group group : groupsArray) {
        notifMetaData.addGroupRecipient(new GroupRecipient(group));
      }
      notifMetaData.setSource(getSpaceLabel() + " - " + getComponentLabel());
      notifMetaData.setLink(url);

      ns.notifyUser(notifMetaData);

    } catch (com.stratelia.silverpeas.notificationManager.NotificationManagerException e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.control.InfoLetterSessionController",
          SilverpeasRuntimeException.ERROR, e.getMessage(), e);
    }
  }

  // Notification des abonnes externes
  public String[] notifyExternals(InfoLetterPublicationPdC ilp, String server, List<String> emails) {
    // Retrieve SMTP server information
    String host = getSmtpHost();
    boolean isSmtpAuthentication = isSmtpAuthentication();
    int smtpPort = getSmtpPort();
    String smtpUser = getSmtpUser();
    String smtpPwd = getSmtpPwd();
    boolean isSmtpDebug = isSmtpDebug();
    Transport transport = null;

    List<String> emailErrors = new ArrayList<String>();

    if (emails.size() > 0) {
      int i = 0;

      // Corps et sujet du message
      String subject = getString("infoLetter.emailSubject") + ilp.getName();
      // Email du publieur
      String from = getUserDetail().geteMail();
      // create some properties and get the default Session
      Properties props = System.getProperties();
      props.put("mail.smtp.host", host);
      props.put("mail.smtp.auth", String.valueOf(isSmtpAuthentication));

      Session session = Session.getInstance(props, null);
      session.setDebug(isSmtpDebug); // print on the console all SMTP messages.

      SilverTrace.info("infoLetter", "InfoLetterSessionController.notifyExternals()",
          "root.MSG_GEN_PARAM_VALUE", "subject = " + subject);
      SilverTrace.info("infoLetter", "InfoLetterSessionController.notifyExternals()",
          "root.MSG_GEN_PARAM_VALUE", "from = " + from);
      SilverTrace.info("infoLetter", "InfoLetterSessionController.notifyExternals()",
          "root.MSG_GEN_PARAM_VALUE", "host= " + host);

      try {
        // create a message
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setSubject(subject, CharEncoding.UTF_8);
        ForeignPK foreignKey = new ForeignPK(ilp.getPK().getId(), getComponentId());
        // create and fill the first message part
        MimeBodyPart mbp1 = new MimeBodyPart();
        List<SimpleDocument> contents = AttachmentServiceFactory.getAttachmentService().
            listDocumentsByForeignKeyAndType(foreignKey, DocumentType.wysiwyg, null);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (SimpleDocument content : contents) {
          AttachmentServiceFactory.getAttachmentService().getBinaryContent(buffer, content.getPk(),
              content.getLanguage());
        }
        mbp1.setDataHandler(new DataHandler(new ByteArrayDataSource(replaceFileServerWithLocal(
            IOUtils.toString(buffer.toByteArray(), CharEncoding.UTF_8), server),
            MimeTypes.HTML_MIME_TYPE)));
        IOUtils.closeQuietly(buffer);
        // Fichiers joints
        WAPrimaryKey publiPK = ilp.getPK();
        publiPK.setComponentName(getComponentId());
        publiPK.setSpace(getSpaceId());

        // create the Multipart and its parts to it
        String mimeMultipart = getSettings().getString("SMTPMimeMultipart", "related");
        Multipart mp = new MimeMultipart(mimeMultipart);
        mp.addBodyPart(mbp1);

        // Images jointes        
        List<SimpleDocument> fichiers = AttachmentServiceFactory.getAttachmentService().
            listDocumentsByForeignKeyAndType(foreignKey, DocumentType.image, null);
        for (SimpleDocument attachment : fichiers) {
          // create the second message part
          MimeBodyPart mbp2 = new MimeBodyPart();

          // attach the file to the message
          FileDataSource fds = new FileDataSource(attachment.getAttachmentPath());
          mbp2.setDataHandler(new DataHandler(fds));
          // For Displaying images in the mail
          mbp2.setFileName(attachment.getFilename());
          mbp2.setHeader("Content-ID", attachment.getFilename());
          SilverTrace.info("infoLetter", "InfoLetterSessionController.notifyExternals()",
              "root.MSG_GEN_PARAM_VALUE", "content-ID= " + mbp2.getContentID());

          // create the Multipart and its parts to it
          mp.addBodyPart(mbp2);
        }

        // Fichiers joints
        fichiers = AttachmentServiceFactory.getAttachmentService().
            listDocumentsByForeignKeyAndType(foreignKey, DocumentType.attachment, null);


        if (!fichiers.isEmpty()) {
          for (SimpleDocument attachment : fichiers) {
            // create the second message part
            MimeBodyPart mbp2 = new MimeBodyPart();

            // attach the file to the message
            FileDataSource fds = new FileDataSource(attachment.getAttachmentPath());
            mbp2.setDataHandler(new DataHandler(fds));
            mbp2.setFileName(attachment.getFilename());
            // For Displaying images in the mail
            mbp2.setHeader("Content-ID", attachment.getFilename());
            SilverTrace.info("infoLetter", "InfoLetterSessionController.notifyExternals()",
                "root.MSG_GEN_PARAM_VALUE", "content-ID= " + mbp2.getContentID());

            // create the Multipart and its parts to it
            mp.addBodyPart(mbp2);
          }
        }

        // add the Multipart to the message
        msg.setContent(mp);
        // set the Date: header
        msg.setSentDate(new Date());
        // create a Transport connection (TCP)
        transport = session.getTransport("smtp");

        InternetAddress[] address = new InternetAddress[1];
        String email = null;
        for (i = 0; i < emails.size(); i++) {
          email = emails.get(i);
          try {
            address[0] = new InternetAddress(email);
            msg.setRecipients(Message.RecipientType.TO, address);
            // add Transport Listener to the transport connection.
            if (isSmtpAuthentication) {
              SilverTrace.info("infoLetter", "InfoLetterSessionController.notifyExternals()",
                  "root.MSG_GEN_PARAM_VALUE", "host = " + host + " m_Port=" + smtpPort + " m_User="
                  + smtpUser);
              transport.connect(host, smtpPort, smtpUser, smtpPwd);
              msg.saveChanges();
            } else {
              transport.connect();
            }
            transport.sendMessage(msg, address);
          } catch (Exception ex) {
            SilverTrace.error("infoLetter", "InfoLetterSessionController.notifyExternals()",
                "root.MSG_GEN_PARAM_VALUE", "Email = " + email, new InfoLetterException(
                "com.stratelia.silverpeas.infoLetter.control.InfoLetterSessionController",
                SilverpeasRuntimeException.ERROR, ex.getMessage(), ex));
            emailErrors.add(email);
          } finally {
            if (transport != null) {
              try {
                transport.close();
              } catch (Exception e) {
                SilverTrace.error("infoLetter", "InfoLetterSessionController.notifyExternals()",
                    "root.EX_IGNORED", "ClosingTransport", e);
              }
            }
          }
        }
      } catch (Exception e) {
        throw new InfoLetterException(
            "com.stratelia.silverpeas.infoLetter.control.InfoLetterSessionController",
            SilverpeasRuntimeException.ERROR, e.getMessage(), e);
      }
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
  public void suscribeUser(WAPrimaryKey letterPK) {
    dataInterface.toggleSuscriber(getUserId(), letterPK, true);
  }

  // Desabonnement d'un utilisateur
  public void unsuscribeUser(WAPrimaryKey letterPK) {
    dataInterface.toggleSuscriber(getUserId(), letterPK, false);
  }

  // test d'abonnement d'un utilisateur interne
  public boolean isSuscriber(WAPrimaryKey letterPK) {
    return dataInterface.isSuscriber(getUserId(), letterPK);
  }

  // Mise a jour du template a partir d'une publication
  public void updateTemplate(InfoLetterPublicationPdC ilp) {
    copyWYSIWYG(ilp.getPK().getId(), InfoLetterPublication.TEMPLATE_ID + ilp.getLetterId());
  }

  // copie d'un repertoire wysiwyg vers un autre
  public void copyWYSIWYG(String source, String target) {
    try {
      if (WysiwygController.loadFileAndAttachment(getComponentId(), target) != null) {
        WysiwygController.deleteWysiwygAttachments(getComponentId(), target);
      }
      String publicationSource = source;
      /* if(StringUtil.isDefined(source) && source.startsWith(InfoLetterPublication.TEMPLATE_ID)) {
       publicationSource = source.substring(InfoLetterPublication.TEMPLATE_ID.length());
       }*/
      WysiwygController.copy(getComponentId(), publicationSource, getComponentId(), target,
          getUserId());
    } catch (Exception e) {
      throw new InfoLetterException("InfoLetterSessionController.copyWYSIWYG",
          SilverpeasRuntimeException.ERROR, e.getMessage(), e);
    }
  }

  public boolean isTemplateExist(InfoLetterPublicationPdC ilp) {
    String template = WysiwygController.loadFileAndAttachment(getComponentId(),
        InfoLetterPublication.TEMPLATE_ID + ilp.getLetterId());
    return !"<body></body>".equalsIgnoreCase(template);
  }

  public String getTemplate(InfoLetterPublicationPdC ilp) {
    return WysiwygController.getWysiwygPath(getComponentId(), InfoLetterPublication.TEMPLATE_ID
        + ilp.getLetterId());
  }

  // Indexation d'une publication
  public void createIndex(InfoLetterPublicationPdC ilp) {
    FullIndexEntry indexEntry = null;
    if (ilp != null) {
      indexEntry = new FullIndexEntry(getComponentId(), "Publication", ilp.getPK().getId());
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
    FullIndexEntry indexEntry = null;
    if (il != null) {
      indexEntry = new FullIndexEntry(getComponentId(), "Lettre", il.getPK().getId());
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

  // Remplacement des appels au FileServer par le nom de fichier simple
  private String replaceFileServerWithLocal(String message, String server) {
    SilverTrace.info("infoLetter", "InfoLetterSessionController.replaceFileServerWithLocal()",
        "root.MSG_GEN_PARAM_VALUE", "wysiwygText avant = " + message);
    String retour = replacePermalinkWithServer(message, server);
    while (retour.indexOf("/FileServer/") > -1) {
      int place = retour.indexOf("/FileServer/");
      String debut = retour.substring(0, place);
      int srcPlace = debut.lastIndexOf("\"");
      debut = debut.substring(0, srcPlace + 1);
      String suite =
          retour.substring(place + "/FileServer/".length(), retour.length());
      int finNom = suite.indexOf('?');
      String nomFichier = "cid:" + suite.substring(0, finNom);
      nomFichier.replace('_', ' ');
      int finURL = suite.indexOf('\"');
      suite = suite.substring(finURL, suite.length());
      retour = debut + nomFichier + suite;
    }
    SilverTrace.info("infoLetter", "InfoLetterSessionController.replaceFileServerWithLocal()",
        "root.MSG_GEN_PARAM_VALUE", "wysiwygText après = " + retour);
    return retour;
  }

  /**
   * Replace /silverpeas/xxx by http(s)://server:port/silverpeas/xxx (Only in notifyExternals case)
   *
   * @param codeHtml
   * @param serveur: http(s)://server:port
   * @return codeHtml
   */
  private String replacePermalinkWithServer(String message, String server) {
    SilverTrace.info("infoLetter", "InfoLetterSessionController.replacePermalinkWithServer()",
        "root.MSG_GEN_PARAM_VALUE", "wysiwygText avant = " + message);
    String urlContext = URLManager.getApplicationURL();
    String retour = message.replaceAll("\"".toLowerCase() + urlContext + "/",
        "\"" + server + urlContext + "/");
    SilverTrace.info("infoLetter", "InfoLetterSessionController.replacePermalinkWithServer()",
        "root.MSG_GEN_PARAM_VALUE", "wysiwygText après = " + retour);
    return retour;
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
    return (InfoLetter) listLettres.get(0);
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
    csvReader.initCSVFormat("org.silverpeas.infoLetter.settings.usersCSVFormat", "User",
        ";");

    Variant[][] csvValues;
    try {
      csvValues = csvReader.parseStream(is);
    } catch (UtilTrappedException ute) {
      ute.setGoBackPage("Emails");
      throw ute;
    }

    StringBuffer listErrors = new StringBuffer("");
    String email;

    for (int i = 0; i < csvValues.length; i++) {
      // email
      email = csvValues[i][0].getValueString();
      if (email.length() == 0) {// champ obligatoire
        listErrors.append(getString("GML.ligne") + " = " + Integer.toString(i + 1) + ", ");
        listErrors.append(getString("GML.colonne") + " = 1, ");
        listErrors.append(getString("GML.valeur") + " = " + email + ", ");
        listErrors.append(getString("GML.obligatoire") + "<BR>");
      } else if (email.length() > 100) {// verifier 100 char max
        listErrors.append(getString("GML.ligne") + " = " + Integer.toString(i + 1) + ", ");
        listErrors.append(getString("GML.colonne") + " = 1, ");
        listErrors.append(getString("GML.valeur") + " = " + email + ", ");
        listErrors.append(getString("GML.nbCarMax") + " 100 " + getString("GML.caracteres")
            + "<BR>");
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
    for (int i = 0; i < csvValues.length; i++) {
      // Email
      email = csvValues[i][0].getValueString();
      emails.add(email);
    }
    dataInterface.setExternalsSuscribers(this.getCurrentLetter().getPK(), emails);
  }

  /**
   * Export Csv emails
   *
   * @throws FileNotFoundException
   * @throws IOException
   * @throws InfoLetterException
   * @return boolean
   */
  public boolean exportCsvEmails() throws FileNotFoundException, IOException, InfoLetterException {
    boolean exportOk = true;
    FileOutputStream fileOutput =
        new FileOutputStream(FileRepositoryManager.getTemporaryPath() + getCurrentLetter().
        getName() + EXPORT_CSV_NAME);
    try {
      List<String> emails = getExternalsSuscribers(getCurrentLetter().getPK());

      CSVWriter csvWriter = new CSVWriter(getLanguage());
      csvWriter.initCSVFormat("org.silverpeas.infoLetter.settings.usersCSVFormat",
          "User", ";");

      String email;
      for (int i = 0; i < emails.size(); i++) {
        email = emails.get(i);
        fileOutput.write(email.getBytes());
        fileOutput.write("\n".getBytes());
      }
    } catch (Exception e) {
      exportOk = false;
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.control.InfoLetterSessionController",
          SilverpeasRuntimeException.ERROR, e.getMessage(), e);
    } finally {
      fileOutput.flush();
      fileOutput.close();
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
    String[] userIds = getOrganizationController().getUsersIdsByRoleNames(getComponentId(), roles);
    if (userIds != null) {
      for (int i = 0; i < userIds.length; i++) {
        String userId = userIds[i];
        String email = getUserDetail(userId).geteMail();
        if (StringUtil.isDefined(email)) {
          emails.add(email);
        }
      }
    }
    return emails;
  }

  private String getSmtpHost() {
    return smtpSettings.getString("SMTPServer");
  }

  private boolean isSmtpAuthentication() {
    return smtpSettings.getBoolean("SMTPAuthentication", false);
  }

  private boolean isSmtpDebug() {
    return smtpSettings.getBoolean("SMTPDebug", false);
  }

  private int getSmtpPort() {
    return Integer.parseInt(smtpSettings.getString("SMTPPort"));
  }

  private String getSmtpUser() {
    return smtpSettings.getString("SMTPUser");
  }

  private String getSmtpPwd() {
    return smtpSettings.getString("SMTPPwd");
  }
}
