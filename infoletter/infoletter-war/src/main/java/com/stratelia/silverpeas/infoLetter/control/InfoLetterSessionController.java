/**
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.infoLetter.control;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.csv.CSVReader;
import com.silverpeas.util.csv.CSVWriter;
import com.silverpeas.util.csv.Variant;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
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
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilTrappedException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

/**
 * Class declaration
 * @author
 */
public class InfoLetterSessionController extends AbstractComponentSessionController {

  // Membres roides de bon aloi
  /** Interface metier du composant */
  private InfoLetterDataInterface dataInterface = null;
  /** the tuning parameters */
  private static final ResourceLocator smtpSettings = new ResourceLocator(
      "com.stratelia.silverpeas.notificationserver.channel.smtp.smtpSettings", "");
  /** the current publication id */
  private String currentPublicationId = null;
  public final static String EXPORT_CSV_NAME = "_emails.csv";
  public final static boolean EXPORT_OK = true;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public InfoLetterSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.stratelia.silverpeas.infoLetter.multilang.infoLetterBundle",
        "com.stratelia.silverpeas.infoLetter.settings.infoLetterIcons",
        "com.stratelia.silverpeas.infoLetter.settings.infoLetterSettings");
    // Initialisation de l'interface metier
    if (dataInterface == null) {
      dataInterface = ServiceFactory.getInfoLetterData();
    }
  }

  /*
   * Initialisation du UserPanel avec les abonnes Silverpeas
   */
  public String initUserPanel(WAPrimaryKey letterPK) throws InfoLetterException {
    int i = 0;
    String context = GeneralPropertiesManager.getGeneralResourceLocator().getString(
        "ApplicationURL");
    String hostSpaceName = getSpaceLabel();
    PairObject hostComponentName = new PairObject(getComponentLabel(),
        context + "/RinfoLetter/" + getComponentId() + "/Main");
    String hostUrl = context + "/RinfoLetter/" + getComponentId() + "/RetourPanel";
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

    Vector v = dataInterface.getInternalSuscribers(letterPK);
    Vector groups = (Vector) v.elementAt(0);
    Vector users = (Vector) v.elementAt(1);
    String[] usersArray = new String[users.size()];
    for (i = 0; i < users.size(); i++) {
      usersArray[i] = ((UserDetail) users.elementAt(i)).getId();
    }
    String[] groupsArray = new String[groups.size()];
    for (i = 0; i < groups.size(); i++) {
      groupsArray[i] = ((Group) groups.elementAt(i)).getId();
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
    Vector abonnes = new Vector();
    UserDetail[] users = SelectionUsersGroups.getUserDetails(sel.getSelectedElements());
    Group[] groups = SelectionUsersGroups.getGroups(sel.getSelectedSets());
    abonnes.add(groups);
    abonnes.add(users);
    dataInterface.setInternalSuscribers(letterPK, abonnes);
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
  public void createInfoLetterPublication(InfoLetterPublicationPdC ilp) {
    ilp.setInstanceId(getComponentId());
    dataInterface.createInfoLetterPublication(ilp, getUserId());
    copyWYSIWYG(InfoLetterPublication.TEMPLATE_ID + ilp.getLetterId(), ilp.getPK().getId());
  }

  // Suppression d'une publication
  public void deleteInfoLetterPublication(WAPrimaryKey pk) {
    deleteIndex(getInfoLetterPublication(pk));
    try {
      WysiwygController.deleteWysiwygAttachments(getSpaceId(), getComponentId(), pk.getId());
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
    currentPublicationId = publiPK.getId();
    return dataInterface.getInfoLetterPublication(publiPK);
  }

  protected SilverpeasTemplate getNewTemplate() {
    ResourceLocator rs =
        new ResourceLocator("com.stratelia.silverpeas.infoLetter.settings.infoLetterSettings", "");
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
    int i = 0;
    pk.setId(String.valueOf(ilp.getLetterId()));
    InfoLetter il = dataInterface.getInfoLetter(pk);
    String sSubject = getString("infoLetter.emailSubject") + ilp.getName();

    Vector v = dataInterface.getInternalSuscribers(pk);
    Vector groups = (Vector) v.elementAt(0);
    Vector users = (Vector) v.elementAt(1);
    UserDetail[] usersArray = new UserDetail[users.size()];
    for (i = 0; i < users.size(); i++) {
      usersArray[i] = (UserDetail) users.elementAt(i);
    }
    Group[] groupsArray = new Group[groups.size()];
    for (i = 0; i < groups.size(); i++) {
      groupsArray[i] = (Group) groups.elementAt(i);
    }

    try {
      Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
      NotificationMetaData notifMetaData =
          new NotificationMetaData(NotificationParameters.NORMAL, sSubject, templates,
              "infoLetterNotification");

      String url = "/RinfoLetter/" + getComponentId() + "/View?parution=" + ilp.getPK().getId();
      for (String lang : DisplayI18NHelper.getLanguages()) {
        SilverpeasTemplate template = getNewTemplate();
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
            "com.stratelia.silverpeas.infoLetter.multilang.infoLetterBundle", lang);
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
    IdPK letterPK = new IdPK(String.valueOf(ilp.getLetterId()));

    // Infos du serveur SMTP
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
      InfoLetter il = dataInterface.getInfoLetter(letterPK);
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
        msg.setSubject(subject, "UTF-8");

        // create and fill the first message part
        MimeBodyPart mbp1 = new MimeBodyPart();
        String fileName = WysiwygController.getWysiwygFileName(ilp.getPK().getId());
        String htmlMessagePath =
            com.stratelia.webactiv.util.FileRepositoryManager.getAbsolutePath(
                getComponentId()) + "Attachment" + java.io.File.separator + "wysiwyg"
                + java.io.File.separator;
        FileReader htmlCodeFile = new FileReader(new File(htmlMessagePath + fileName));

        StringBuilder msgText1 = new StringBuilder();
        int c;
        c = htmlCodeFile.read();
        while (c != -1) {
          msgText1.append((char) c);
          c = htmlCodeFile.read();
        }

        // mbp1.setText(msgText1.toString());
        mbp1.setDataHandler(new DataHandler(
            new ByteArrayDataSource(replaceFileServerWithLocal(msgText1.toString(), server),
                "text/html")));

        // Fichiers joints
        // AttachmentController ac = new AttachmentController();
        WAPrimaryKey publiPK = ilp.getPK();
        publiPK.setComponentName(getComponentId());
        publiPK.setSpace(getSpaceId());

        // create the Multipart and its parts to it
        // Multipart mp = new MimeMultipart("related");
        String mimeMultipart = getSettings().getString("SMTPMimeMultipart", "related");
        Multipart mp = new MimeMultipart(mimeMultipart);
        mp.addBodyPart(mbp1);

        // Images jointes
        AttachmentPK foreignKey = new AttachmentPK(ilp.getPK().getId(), getSpaceId(),
            getComponentId());
        Collection<AttachmentDetail> fichiers =
            AttachmentController.searchAttachmentByPKAndContext(foreignKey,
                WysiwygController.getImagesFileName(ilp.getPK().getId()));

        String attachmentPath =
            AttachmentController.createPath(getComponentId(), WysiwygController.
                getImagesFileName(ilp.getPK().getId()));
        Iterator<AttachmentDetail> imageIter = fichiers.iterator();
        while (imageIter.hasNext()) {
          AttachmentDetail ad = imageIter.next();
          // create the second message part
          MimeBodyPart mbp2 = new MimeBodyPart();

          // attach the file to the message
          FileDataSource fds = new FileDataSource(attachmentPath + ad.getPhysicalName());
          mbp2.setDataHandler(new DataHandler(fds));
          // For Displaying images in the mail
          mbp2.setFileName(ad.getLogicalName());
          mbp2.setHeader("Content-ID", ad.getLogicalName());
          SilverTrace.info("infoLetter", "InfoLetterSessionController.notifyExternals()",
              "root.MSG_GEN_PARAM_VALUE", "content-ID= " + mbp2.getContentID());

          // create the Multipart and its parts to it
          mp.addBodyPart(mbp2);
        }

        // Fichiers joints
        fichiers = AttachmentController.searchAttachmentByPKAndContext(publiPK, "Images");
        attachmentPath =
            com.stratelia.webactiv.util.FileRepositoryManager.getAbsolutePath(
                getComponentId()) + "Attachment" + java.io.File.separator + "Images"
                + java.io.File.separator;

        if (fichiers.size() > 0) {
          imageIter = fichiers.iterator();
          while (imageIter.hasNext()) {
            AttachmentDetail ad = (AttachmentDetail) imageIter.next();
            // create the second message part
            MimeBodyPart mbp2 = new MimeBodyPart();

            // attach the file to the message
            FileDataSource fds = new FileDataSource(attachmentPath + ad.getPhysicalName());
            mbp2.setDataHandler(new DataHandler(fds));
            mbp2.setFileName(ad.getLogicalName());
            // For Displaying images in the mail
            mbp2.setHeader("Content-ID", ad.getLogicalName());
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

        // redefine the TransportListener interface.
        TransportListener transportListener = new TransportListener() {

          /**
           * Method declaration
           * @param e
           * @see
           */
          public void messageDelivered(TransportEvent e) { // catch all messages delivered to the
            // SMTP server.
          }

          /**
           * Method declaration
           * @param e
           * @see
           */
          public void messageNotDelivered(TransportEvent e) { // catch all messages NOT delivered to
            // the SMTP server.
          }

          /**
           * Method declaration
           * @param e
           * @see
           */
          public void messagePartiallyDelivered(TransportEvent e) {
          }
        };

        // Chaine de destination
        transport.addTransportListener(transportListener);

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
                  "root.MSG_GEN_PARAM_VALUE", "host = " + host + " m_Port=" + smtpPort + " m_User=" +
                      smtpUser);
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
    return (String[]) emailErrors.toArray(new String[0]);
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
      if (WysiwygController.loadFileAndAttachment(getSpaceId(), getComponentId(), target) != null) {
        WysiwygController.deleteWysiwygAttachments(getSpaceId(), getComponentId(), target);
      }
      WysiwygController.copy(getSpaceId(), getComponentId(), source, getSpaceId(),
          getComponentId(),
          target, getUserId());
    } catch (Exception e) {
      throw new InfoLetterException("InfoLetterSessionController.copyWYSIWYG",
          SilverpeasRuntimeException.ERROR, e.getMessage(), e);
    }
  }

  public boolean isTemplateExist(InfoLetterPublicationPdC ilp) {
    String template;
    try {
      template =
          WysiwygController.loadFileAndAttachment("useless", getComponentId(),
              InfoLetterPublication.TEMPLATE_ID + ilp.getLetterId());
    } catch (WysiwygException e) {
      throw new InfoLetterException("InfoLetterSessionController.isTemplateExist",
          SilverpeasRuntimeException.ERROR, e.getMessage(), e);
    }
    return !"<body></body>".equals(template);
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

  public int getCurrentSilverObjectId() {
    return dataInterface.getSilverObjectId(currentPublicationId, getComponentId());
  }

  /**
   * Method declaration
   * @return
   */
  private InfoLetter getCurrentLetter() {
    List<InfoLetter> listLettres = getInfoLetters();
    return (InfoLetter) listLettres.get(0);
  }

  /**
   * Import Csv emails
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
    csvReader.initCSVFormat("com.stratelia.silverpeas.infoLetter.settings.usersCSVFormat", "User",
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
      csvWriter.initCSVFormat("com.stratelia.silverpeas.infoLetter.settings.usersCSVFormat",
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
