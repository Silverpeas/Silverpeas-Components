/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.infoLetter.implementation;

import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.service.ComponentSubscription;
import com.silverpeas.subscribe.service.ComponentSubscriptionResource;
import com.silverpeas.subscribe.service.GroupSubscriptionSubscriber;
import com.silverpeas.subscribe.service.UserSubscriptionSubscriber;
import com.silverpeas.subscribe.util.SubscriptionUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.infoLetter.InfoLetterContentManager;
import com.stratelia.silverpeas.infoLetter.InfoLetterException;
import com.stratelia.silverpeas.infoLetter.control.ByteArrayDataSource;
import com.stratelia.silverpeas.infoLetter.model.InfoLetter;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterDataInterface;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterPublication;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterPublicationPdC;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.wysiwyg.control.WysiwygController;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Class declaration
 *
 * @author
 */
public class InfoLetterDataManager implements InfoLetterDataInterface {

  // Statiques
  private final static String TableExternalEmails = "SC_IL_ExtSus";
  
  /**
   * the tuning parameters
   */
  private static final ResourceLocator smtpSettings = new ResourceLocator(
      "org.silverpeas.notificationserver.channel.smtp.smtpSettings", "");
  
  // Membres
  private SilverpeasBeanDAO<InfoLetter> infoLetterDAO;
  private SilverpeasBeanDAO<InfoLetterPublication> infoLetterPublicationDAO;
  private InfoLetterContentManager infoLetterContentManager;

  // Constructeur
  public InfoLetterDataManager() {
    try {
      infoLetterDAO = SilverpeasBeanDAOFactory.getDAO(
          "com.stratelia.silverpeas.infoLetter.model.InfoLetter");
      infoLetterPublicationDAO = SilverpeasBeanDAOFactory.getDAO(
          "com.stratelia.silverpeas.infoLetter.model.InfoLetterPublication");
      infoLetterContentManager = new InfoLetterContentManager();
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  /**
   * Implementation of InfoLetterDataInterface interface
   */
  // Creation d'une lettre d'information
  @Override
  public void createInfoLetter(InfoLetter il) {
    try {
      WAPrimaryKey pk = infoLetterDAO.add(il);
      il.setPK(pk);
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Mise a jour d'une lettre d'information
  @Override
  public void updateInfoLetter(InfoLetter ie) {
    try {
      infoLetterDAO.update(ie);
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Recuperation de la liste des lettres
  @Override
  public List<InfoLetter> getInfoLetters(String instanceId) {
    String whereClause = "instanceId = '" + instanceId + "'";
    try {
      return new ArrayList<InfoLetter>(infoLetterDAO.findByWhereClause(new IdPK(),
          whereClause));
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Recuperation de la liste des publications
  @Override
  public List<InfoLetterPublication> getInfoLetterPublications(WAPrimaryKey letterPK) {
    try {
      InfoLetter letter = getInfoLetter(letterPK);
      String whereClause = "instanceId = '" + letter.getInstanceId() + "' AND letterId = "
          + letterPK.getId();
      return new ArrayList<InfoLetterPublication>(infoLetterPublicationDAO.findByWhereClause(
          letterPK, whereClause));
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
  }

  // Creation d'une publication
  @Override
  public void createInfoLetterPublication(InfoLetterPublicationPdC ilp, String userId) {
    SilverTrace.info("infoLetter", "InfoLetterDataManager.createInfoLetterPublication()",
        "root.MSG_GEN_ENTER_METHOD", "ilp = " + ilp.toString() + " userId=" + userId);
    Connection con = openConnection();

    try {
      WAPrimaryKey pk = infoLetterPublicationDAO.add(con, ilp);
      ilp.setPK(pk);
      infoLetterContentManager.createSilverContent(con, ilp, userId);
    } catch (Exception pe) {
      DBUtil.rollback(con);
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    } finally {
      DBUtil.close(con);
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * com.stratelia.silverpeas.infoLetter.model.InfoLetterDataInterface#deleteInfoLetterPublication
   * (com.stratelia.webactiv.util.WAPrimaryKey, java.lang.String)
   */
  @Override
  public void deleteInfoLetterPublication(WAPrimaryKey pk, String componentId) {
    Connection con = openConnection();
    try {
      infoLetterPublicationDAO.remove(pk);
      infoLetterContentManager.deleteSilverContent(con, pk.getId(), componentId);
    } catch (Exception pe) {
      DBUtil.rollback(con);
      throw new InfoLetterException("InfoLetterDataManager.createInfoLetterPublication()",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    } finally {
      DBUtil.close(con);
    }
  }

  // Mise a jour d'une publication
  @Override
  public void updateInfoLetterPublication(InfoLetterPublicationPdC ilp) {
    try {
      infoLetterPublicationDAO.update(ilp);
      infoLetterContentManager.updateSilverContentVisibility(ilp);
    } catch (Exception e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    }
  }

  // Recuperation d'une lettre par sa clef
  @Override
  public InfoLetter getInfoLetter(WAPrimaryKey letterPK) {
    InfoLetter retour;
    try {
      retour = infoLetterDAO.findByPrimaryKey(letterPK);
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
    return retour;
  }

  // Recuperation d'une publication par sa clef
  @Override
  public InfoLetterPublicationPdC getInfoLetterPublication(WAPrimaryKey publiPK) {
    InfoLetterPublicationPdC retour;
    try {
      retour = new InfoLetterPublicationPdC(infoLetterPublicationDAO.findByPrimaryKey(publiPK));
    } catch (PersistenceException pe) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, pe.getMessage(), pe);
    }
    return retour;
  }

  // Creation de la lettre par defaut a l'instanciation
  @Override
  public InfoLetter createDefaultLetter(String componentId) {
    OrganizationController oc = new OrganizationController();
    ComponentInst ci = oc.getComponentInst(componentId);
    InfoLetter ie = new InfoLetter();
    ie.setInstanceId(componentId);
    ie.setName(ci.getLabel());
    createInfoLetter(ie);
    initTemplate(componentId, ie.getPK(), "0");
    return ie;
  }

  @Override
  public int getSilverObjectId(String pubId, String componentId) {
    SilverTrace.info("infoLetter", "InfoLetterDataManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
    try {
      int silverObjectId = infoLetterContentManager.getSilverObjectId(pubId, componentId);
      if (silverObjectId == -1) {
        IdPK publiPK = new IdPK();
        publiPK.setId(pubId);
        InfoLetterPublicationPdC infoLetter = getInfoLetterPublication(publiPK);
        silverObjectId = infoLetterContentManager.createSilverContent(null, infoLetter,
            infoLetter.getCreatorId());
      }
      return silverObjectId;
    } catch (Exception e) {
      throw new InfoLetterException("InfoLetterDataManager.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  @Override
  public Map<SubscriberType, Collection<String>> getInternalSuscribers(final String componentId) {
    return SubscriptionUtil.indexSubscriberIdsByType(
        SubscriptionServiceFactory.getFactory().getSubscribeService()
            .getSubscribers(ComponentSubscriptionResource.from(componentId)));
  }

  @Override
  public void setInternalSuscribers(final String componentId, final UserDetail[] users,
      final Group[] groups) {

    // Initializing necessary subscriptions
    Collection<Subscription> subscriptions =
        new ArrayList<Subscription>(users.length + groups.length);
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
        SubscriptionServiceFactory.getFactory().getSubscribeService()
            .getByResource(ComponentSubscriptionResource.from(componentId));
    subscriptionsToDelete.removeAll(subscriptions);

    // Deleting
    SubscriptionServiceFactory.getFactory().getSubscribeService()
        .unsubscribe(subscriptionsToDelete);

    // Creating subscriptions (nothing is registered for subscriptions that already exist)
    SubscriptionServiceFactory.getFactory().getSubscribeService().subscribe(subscriptions);
  }

  // Recuperation de la liste des emails externes
  @Override
  public Collection<String> getExternalsSuscribers(WAPrimaryKey letterPK) {
    Connection con = openConnection();
    List<String> retour = new ArrayList<String>();
    Statement selectStmt = null;
    ResultSet rs = null;
    try {
      InfoLetter letter = getInfoLetter(letterPK);
      String selectQuery = "SELECT * FROM " + TableExternalEmails;
      selectQuery += " where instanceId = '" + letter.getInstanceId() + "' ";
      selectQuery += " and letter = " + letterPK.getId() + " ";
      SilverTrace.info("infoLetter",
          "InfoLetterDataManager.getExternalsSuscribers()",
          "root.MSG_GEN_PARAM_VALUE", "selectQuery = " + selectQuery);
      selectStmt = con.createStatement();
      rs = selectStmt.executeQuery(selectQuery);
      while (rs.next()) {
        retour.add(rs.getString("email"));
      }
    } catch (Exception e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    } finally {
      DBUtil.close(rs, selectStmt);
      DBUtil.close(con);
    }

    return retour;
  }

  // Sauvegarde de la liste des emails externes
  public void setExternalsSuscribers(WAPrimaryKey letterPK, Collection<String> emails) {
    Connection con = openConnection();
    Statement stmt = null;
    try {
      InfoLetter letter = getInfoLetter(letterPK);
      String query = "DELETE FROM " + TableExternalEmails;
      query += " where instanceId = '" + letter.getInstanceId() + "' ";
      query += " and letter = " + letterPK.getId() + " ";
      SilverTrace.info("infoLetter",
          "InfoLetterDataManager.setExternalsSuscribers()",
          "root.MSG_GEN_PARAM_VALUE", "query = " + query);
      stmt = con.createStatement();
      stmt.executeUpdate(query);
      if (emails.size() > 0) {
        for (String email : emails) {
          query = "INSERT INTO " + TableExternalEmails + "(letter, email, instanceId)";
          query += " values (" + letterPK.getId() + ", '" + email + "', '"
              + letter.getInstanceId() + "')";
          stmt = con.createStatement();
          stmt.executeUpdate(query);
        }
      }
    } catch (Exception e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
          SilverpeasRuntimeException.FATAL, e.getMessage());
    } finally {
      DBUtil.close(stmt);
      DBUtil.close(con);
    }
  }

  // abonnement ou desabonnement d'un utilisateur interne
  @Override
  public void toggleSuscriber(String userId, String componentId, boolean isUserSubscribing) {
    Subscription subscription =
        new ComponentSubscription(UserSubscriptionSubscriber.from(userId), componentId);
    if (isUserSubscribing) {
      SubscriptionServiceFactory.getFactory().getSubscribeService().subscribe(subscription);
    } else {
      SubscriptionServiceFactory.getFactory().getSubscribeService().unsubscribe(subscription);
    }
  }

  // test d'abonnement d'un utilisateur interne
  @Override
  public boolean isUserSuscribed(String userId, String componentId) {
    return SubscriptionServiceFactory.getFactory().getSubscribeService().existsSubscription(
        new ComponentSubscription(UserSubscriptionSubscriber.from(userId), componentId));
  }

  // initialisation du template
  @Override
  public void initTemplate(String componentId, WAPrimaryKey letterPK, String userId) {
    try {
      String basicTemplate = "<body></body>";
      WysiwygController.createUnindexedFileAndAttachment(basicTemplate,
          new ForeignPK(InfoLetterPublication.TEMPLATE_ID + letterPK.getId(), componentId), userId,
          I18NHelper.defaultLanguage);
    } catch (Exception e) {
      throw new InfoLetterException(
          "com.stratelia.silverpeas.infoLetter.control.InfoLetterSessionController",
          SilverpeasRuntimeException.ERROR, e.getMessage(), e);
    }
  }

  /**
   * open connection
   *
   * @return Connection
   * @throws InfoLetterException
   * @author frageade
   * @since 26 Fevrier 2002
   */
  @Override
  public Connection openConnection() throws InfoLetterException {
    Connection con;
    try {
      con = DBUtil.makeConnection(JNDINames.INFOLETTER_DATASOURCE);
    } catch (Exception e) {
      throw new InfoLetterException("InfoLetterDataManager.openConnection()",
          SilverpeasRuntimeException.FATAL, e.getMessage(), e);
    }
    return con;
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
  
  private List<SimpleDocument> getListFilesInContent(String message) {
    SilverTrace.info("infoLetter", "InfoLetterDataManager.getListFilesInContent()",
        "root.MSG_GEN_PARAM_VALUE", "wysiwygText = " + message);
       
    List<SimpleDocument> listAttachedFiles = new ArrayList<SimpleDocument>();
    
    String fileUrl = URLManager.getApplicationURL()+"/attached_file/";
    int fileUrlLength = fileUrl.length();
    String attachmentIdText = "/attachmentId/";
    int attachmentIdTextLength = attachmentIdText.length();
    
    while (message.contains(fileUrl)) {
      int placeFileUrl = message.indexOf(fileUrl);
      String end = message.substring(placeFileUrl+fileUrlLength);
      int placeAttachmentId = end.indexOf(attachmentIdText);
      end = end.substring(placeAttachmentId+attachmentIdTextLength);
      int placeSlash = end.indexOf("/");
      String idFile = end.substring(0, placeSlash);
      
      SimpleDocumentPK sdPK = new SimpleDocumentPK(idFile);
      SimpleDocument attachedFile = AttachmentServiceFactory.getAttachmentService().
          searchDocumentById(sdPK, null);
      listAttachedFiles.add(attachedFile);
      
      message = end.substring(placeSlash);
    }
    SilverTrace.info("infoLetter", "InfoLetterDataManager.getListFilesInContent()",
        "root.MSG_GEN_PARAM_VALUE", "nb attached files = "+listAttachedFiles.size());
    return listAttachedFiles;
  }

  
  private GalleryBm getGalleryBm() {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBm.class);
  }

  private List<PhotoDetail> getListImagesFromGalleryInContent(String message) {
    SilverTrace.info("infoLetter", "InfoLetterDataManager.getListImagesFromGalleryInContent()",
        "root.MSG_GEN_PARAM_VALUE", "wysiwygText = " + message);
       
    List<PhotoDetail> listImagesFromGallery = new ArrayList<PhotoDetail>();
    
    String imageUrl = URLManager.getApplicationURL()+"/GalleryInWysiwyg/";
    int imageUrlLength = imageUrl.length();
    String imageIdText = "ImageId=";
    int imageIdTextLength = imageIdText.length();
    String componentIdText = "ComponentId=";
    int componentIdTextLength = componentIdText.length();
    
    while (message.contains(imageUrl)) {
      int placeFileUrl = message.indexOf(imageUrl);
      String end = message.substring(placeFileUrl+imageUrlLength);
      int placeImageId = end.indexOf(imageIdText);
      end = end.substring(placeImageId+imageIdTextLength);
      int placeAmp = end.indexOf("&");
      String imageId = end.substring(0, placeAmp);
      end = end.substring(placeAmp+1);
      int placeComponentId = end.indexOf(componentIdText);
      end = end.substring(placeComponentId+componentIdTextLength);
      placeAmp = end.indexOf("&");
      String componentId = end.substring(0, placeAmp);
      
      PhotoDetail image = getGalleryBm().getPhoto(
          new PhotoPK(imageId, componentId));
      listImagesFromGallery.add(image);
      
      message = end.substring(placeAmp+1);
    }
    SilverTrace.info("infoLetter", "InfoLetterDataManager.getListImagesFromGalleryInContent()",
        "root.MSG_GEN_PARAM_VALUE", "nb images from Gallery attached files = "+listImagesFromGallery.size());
    return listImagesFromGallery;
  }

  private String replacePathFilesWithCidInContent(String message) {
    SilverTrace.info("infoLetter", "InfoLetterDataManager.replacePathFilesWithCidInContent()",
        "root.MSG_GEN_PARAM_VALUE", "wysiwygText before = " + message);
    
    String fileUrl = URLManager.getApplicationURL()+"/attached_file/";
    int fileUrlLength = fileUrl.length();
    String nameText = "/name/";
    int nameTextLength = nameText.length();
    
    while (message.contains(fileUrl)) {
      int placeFileUrl = message.indexOf(fileUrl);
      String begin = message.substring(0, placeFileUrl);
      String end = message.substring(placeFileUrl+fileUrlLength);
      int placeQuote = end.indexOf("\"");
      String pathFileUrl = end.substring(0, placeQuote);
      end = end.substring(placeQuote);
      int placeNameFile = pathFileUrl.indexOf(nameText);
      
      String nameFile = "cid:"+pathFileUrl.substring(placeNameFile+nameTextLength);
      
      message = begin + nameFile + end;
    }
    SilverTrace.info("infoLetter", "InfoLetterDataManager.replacePathFilesWithCidInContent()",
        "root.MSG_GEN_PARAM_VALUE", "wysiwygText after = " + message);
    return message;
  }
  
  private String replacePathImagesFromGalleryWithCidInContent(String message, List<PhotoDetail> listAttachedImagesFromGallery) {
    SilverTrace.info("infoLetter", "InfoLetterDataManager.replacePathImagesFromGalleryWithCidInContent()",
        "root.MSG_GEN_PARAM_VALUE", "wysiwygText before = " + message);
    
    String imageUrl = URLManager.getApplicationURL()+"/GalleryInWysiwyg/";
    int imageUrlLength = imageUrl.length();
    int indexImages = 0;
    
    while (message.contains(imageUrl)) {
      int placeFileUrl = message.indexOf(imageUrl);
      String begin = message.substring(0, placeFileUrl);
      String end = message.substring(placeFileUrl+imageUrlLength);
      int placeQuote = end.indexOf("\"");
      end = end.substring(placeQuote);
      
      PhotoDetail image = listAttachedImagesFromGallery.get(indexImages);
      String nameFile = "cid:"+image.getImageName();
      
      message = begin + nameFile + end;
      indexImages ++;
    }
    SilverTrace.info("infoLetter", "InfoLetterDataManager.replacePathImagesFromGalleryWithCidInContent()",
        "root.MSG_GEN_PARAM_VALUE", "wysiwygText after = " + message);
    return message;
  }
  
  private Multipart attachFilesToMail(Multipart mp, List<SimpleDocument> listAttachedFiles) throws MessagingException {
    for (SimpleDocument attachment : listAttachedFiles) {
      // create the second message part
      MimeBodyPart mbp = new MimeBodyPart();

      // attach the file to the message
      FileDataSource fds = new FileDataSource(attachment.getAttachmentPath());
      mbp.setDataHandler(new DataHandler(fds));
      // For Displaying images in the mail
      mbp.setFileName(attachment.getFilename());
      mbp.setHeader("Content-ID", "<"+attachment.getFilename()+">");
      SilverTrace.info("infoLetter", "InfoLetterDataManager.attachFilesToMail()",
          "root.MSG_GEN_PARAM_VALUE", "Content-ID= " +mbp.getContentID());

      // create the Multipart and its parts to it
      mp.addBodyPart(mbp);
    }
    return mp;
  }
  
  private Multipart createContentMessageMail(InfoLetterPublicationPdC ilp, String mimeMultipart) throws IOException, MessagingException {
    Multipart mp = new MimeMultipart(mimeMultipart);
   
    // create and fill the first message part
    ForeignPK foreignKey = new ForeignPK(ilp.getPK().getId(), ilp.getComponentInstanceId());
    List<SimpleDocument> contents = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKeyAndType(foreignKey, DocumentType.wysiwyg,
        I18NHelper.defaultLanguage);
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    for (SimpleDocument content : contents) {
      AttachmentServiceFactory.getAttachmentService().getBinaryContent(buffer, content.getPk(),
          content.getLanguage());
    }
    String wysiwygContent = IOUtils.toString(buffer.toByteArray(), CharEncoding.UTF_8);
    
    // Parse wysiwyg content to extract list of files to attach to the E-mail
    List<SimpleDocument> listAttachedFiles = getListFilesInContent(wysiwygContent);
    
    // Parse wysiwyg content to replace pathFile by cid
    String parsedFilesWysiwygContent = replacePathFilesWithCidInContent(wysiwygContent);
    
    // Parse wysiwyg content to extract list of images from Gallery, to attach to the E-mail
    List<PhotoDetail> listAttachedImagesFromGallery = getListImagesFromGalleryInContent(parsedFilesWysiwygContent);
    
    // Parse wysiwyg content to replace images from gallery by cid
    String finalParsedWysiwygContent = replacePathImagesFromGalleryWithCidInContent(parsedFilesWysiwygContent, listAttachedImagesFromGallery);
    
    MimeBodyPart mbp1 = new MimeBodyPart();
    mbp1.setDataHandler(new DataHandler(new ByteArrayDataSource(finalParsedWysiwygContent,
        MimeTypes.HTML_MIME_TYPE)));
    IOUtils.closeQuietly(buffer);
    mp.addBodyPart(mbp1);
    
    // Attach Files to E-mail
    mp = attachFilesToMail(mp, listAttachedFiles);

    // Attach Images from Gallery to E-mail
    for (PhotoDetail image : listAttachedImagesFromGallery) {
      // create the second message part
      MimeBodyPart mbp2 = new MimeBodyPart();

      // attach the file to the message
      String imageName = image.getImageName();
      String imagePath = FileRepositoryManager.getAbsolutePath(image.getPhotoPK().getInstanceId()) + "image"
          + image.getId() + "/" + imageName;
      FileDataSource fds = new FileDataSource(imagePath);
      mbp2.setDataHandler(new DataHandler(fds));
      mbp2.setFileName(imageName);
      // For Displaying images in the mail
      mbp2.setHeader("Content-ID", "<"+imageName+">");
      SilverTrace.info("infoLetter", "InfoLetterDataManager.createContentMessageMail()",
          "root.MSG_GEN_PARAM_VALUE", "Content-ID= " + mbp2.getContentID());

      // create the Multipart and its parts to it
      mp.addBodyPart(mbp2);
    }
    
    // Attach Files from attached files tab
    List<SimpleDocument> listAttachedFilesFromTab = AttachmentServiceFactory.getAttachmentService().
                  listDocumentsByForeignKeyAndType(foreignKey, DocumentType.attachment, null);
    mp = attachFilesToMail(mp, listAttachedFilesFromTab);

    return mp;
  }
  
  @Override
  public List<String> notifyExternals(InfoLetterPublicationPdC ilp, String server, String mimeMultipart, 
      List<String> listEmailDest, String subject, String emailFrom) {
    // Retrieve SMTP server information
    String host = getSmtpHost();
    boolean isSmtpAuthentication = isSmtpAuthentication();
    int smtpPort = getSmtpPort();
    String smtpUser = getSmtpUser();
    String smtpPwd = getSmtpPwd();
    boolean isSmtpDebug = isSmtpDebug();

    List<String> emailErrors = new ArrayList<String>();

    if (listEmailDest.size() > 0) {
      // create some properties and get the default Session
      Properties props = System.getProperties();
      props.put("mail.smtp.host", host);
      props.put("mail.smtp.auth", String.valueOf(isSmtpAuthentication));

      Session session = Session.getInstance(props, null);
      session.setDebug(isSmtpDebug); // print on the console all SMTP messages.

      SilverTrace.info("infoLetter", "InfoLetterDataManager.notifyExternals()",
          "root.MSG_GEN_PARAM_VALUE", "subject = " + subject);
      SilverTrace.info("infoLetter", "InfoLetterDataManager.notifyExternals()",
          "root.MSG_GEN_PARAM_VALUE", "from = " + emailFrom);
      SilverTrace.info("infoLetter", "InfoLetterDataManager.notifyExternals()",
          "root.MSG_GEN_PARAM_VALUE", "host= " + host);

      try {
        // create a message
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(emailFrom));
        msg.setSubject(subject, CharEncoding.UTF_8);
        
        // create the Multipart and its parts to it
        Multipart mp = createContentMessageMail(ilp, mimeMultipart);

        // add the Multipart to the message
        msg.setContent(mp);
        // set the Date: header
        msg.setSentDate(new Date());
        
        // create a Transport connection (TCP)
        Transport transport = session.getTransport("smtp");

        InternetAddress[] address = new InternetAddress[1];
        for (String email : listEmailDest) {
          try {
            address[0] = new InternetAddress(email);
            msg.setRecipients(Message.RecipientType.TO, address);
            // add Transport Listener to the transport connection.
            if (isSmtpAuthentication) {
              SilverTrace.info("infoLetter", "InfoLetterDataManager.notifyExternals()",
                  "root.MSG_GEN_PARAM_VALUE", "host = " + host + " m_Port=" + smtpPort + " m_User="
                  + smtpUser);
              transport.connect(host, smtpPort, smtpUser, smtpPwd);
              msg.saveChanges();
            } else {
              transport.connect();
            }
            transport.sendMessage(msg, address);
          } catch (Exception ex) {
            SilverTrace.error("infoLetter", "InfoLetterDataManager.notifyExternals()",
                "root.MSG_GEN_PARAM_VALUE", "Email = " + email, new InfoLetterException(
                "com.stratelia.silverpeas.infoLetter.control.InfoLetterSessionController",
                SilverpeasRuntimeException.ERROR, ex.getMessage(), ex));
            emailErrors.add(email);
          } finally {
            if (transport != null) {
              try {
                transport.close();
              } catch (Exception e) {
                SilverTrace.error("infoLetter", "InfoLetterDataManager.notifyExternals()",
                    "root.EX_IGNORED", "ClosingTransport", e);
              }
            }
          }
        }
      } catch (Exception e) {
        throw new InfoLetterException(
            "com.stratelia.silverpeas.infoLetter.implementation.InfoLetterDataManager",
            SilverpeasRuntimeException.ERROR, e.getMessage(), e);
      }
    }
    return emailErrors;
  }
}
