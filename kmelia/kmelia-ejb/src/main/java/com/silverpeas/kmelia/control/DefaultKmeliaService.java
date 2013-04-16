/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package com.silverpeas.kmelia.control;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.notification.CommentUserNotificationService;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * Default implementation of the services provided by the Blog component. It is managed by the
 * underlying IoC container. At initialization by the IoC container, it registers itself among
 * different services for which it is interested.
 */
@Named("kmeliaService")
public class DefaultKmeliaService implements KmeliaService {

  public static final String COMPONENT_NAME = "kmelia";
  private static final String MESSAGES_PATH = "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle";
  private static final String SETTINGS_PATH =
      "com.stratelia.webactiv.kmelia.settings.kmeliaSettings";
  private static final ResourceLocator settings = new ResourceLocator(SETTINGS_PATH, "");
  @Inject
  private CommentUserNotificationService commentUserNotificationService;
  @Inject
  private CommentService commentService;

  /**
   * Initializes this service by registering itself among Silverpeas core services as interested by
   * events.
   */
  @PostConstruct
  public void initialize() {
    commentUserNotificationService.register(COMPONENT_NAME, this);
  }

  /**
   * Releases all the resources required by this service. For instance, it unregisters from the
   * Silverpeas core services.
   */
  @PreDestroy
  public void release() {
    commentUserNotificationService.unregister(COMPONENT_NAME);
  }

  @Override
  public PublicationDetail getContentById(String contentId) {
    return getPublicationBm().getDetail(new PublicationPK(contentId));
  }

  @Override
  public ResourceLocator getComponentSettings() {
    return settings;
  }

  @Override
  public ResourceLocator getComponentMessages(String language) {
    return new ResourceLocator(MESSAGES_PATH, language);
  }

  private PublicationBm getPublicationBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBm.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(getClass().getSimpleName() + ".getPublicationBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * Gets a DefaultCommentService instance.
   *
   * @return a DefaultCommentService instance.
   */
  protected CommentService getCommentService() {
    return commentService;
  }

  @Override
  public void pasteAttachmentsAsDocuments(WAPrimaryKey from, WAPrimaryKey to, String lang) {
    String language = lang;
    if (!StringUtil.isDefined(language)) {
      language = I18NHelper.defaultLanguage;
    }
    SilverTrace.info("kmelia", "DefaultKmeliaService.pasteAttachmentsAsDocuments()",
        "root.MSG_GEN_ENTER_METHOD", "from = " + from + ", to = " + to);

    List<SimpleDocument> attachments = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKeyAndType(from, DocumentType.attachment, language);

    SilverTrace.info("kmelia", "DefaultKmeliaService.pasteAttachmentsAsDocuments()",
        "root.MSG_GEN_PARAM_VALUE", attachments.size() + " attachments to paste");

    if (attachments.isEmpty()) {
      return;
    }

    ForeignPK target = new ForeignPK(to);
    // paste each attachment
    for (SimpleDocument attachment : attachments) {
      SilverTrace.info("kmelia", "DefaultKmeliaService.pasteAttachmentsAsDocuments()",
          "root.MSG_GEN_PARAM_VALUE", "attachment name = " + attachment.getTitle());
      SimpleDocumentPK pk = AttachmentServiceFactory.getAttachmentService().copyDocument(attachment,
          target);
      AttachmentServiceFactory.getAttachmentService().changeVersionState(pk);
    }
  }

  @Override
  public void pasteDocumentsAsAttachments(WAPrimaryKey from, WAPrimaryKey to, String lang,
      String userId) {
    String language = lang;
    if (!StringUtil.isDefined(language)) {
      language = I18NHelper.defaultLanguage;
    }
    SilverTrace.info("kmelia", "DefaultKmeliaService.pasteDocumentsAsAttachments()",
        "root.MSG_GEN_ENTER_METHOD", "from = " + from + ", to = " + to);
    // paste versioning documents attached to publication
    List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKeyAndType(from, DocumentType.attachment, language);

    SilverTrace.info("kmelia", "DefaultKmeliaService.pasteDocumentsAsAttachments()",
        "root.MSG_GEN_PARAM_VALUE", documents.size() + " documents to paste");

    if (documents.isEmpty()) {
      return;
    }

    // paste each document
    for (SimpleDocument document : documents) {
      SilverTrace.info("kmelia", "DefaultKmeliaService.pasteDocumentsAsAttachments()",
          "root.MSG_GEN_PARAM_VALUE", "document name = " + document.getTitle());
      // retrieve last public versions of the document
      SimpleDocument lastVersion = document.getLastPublicVersion();
      if (lastVersion != null) {
        SimpleDocument newVersion = new SimpleDocument(
            new SimpleDocumentPK(null, to.getInstanceId()),
            to.getId(), lastVersion.getOrder(), false, lastVersion.getEditedBy(),
            new SimpleAttachment(lastVersion.getFilename(),
            lastVersion.getLanguage(), lastVersion.getTitle(), lastVersion.getDescription(),
            lastVersion.getSize(), lastVersion.getContentType(), userId, new Date(),
            lastVersion.getXmlFormId()));

        ByteArrayInputStream in = null;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
          AttachmentServiceFactory.getAttachmentService().getBinaryContent(buffer, lastVersion.
              getPk(), language);
          in = new ByteArrayInputStream(buffer.toByteArray());
          newVersion = AttachmentServiceFactory.getAttachmentService().createAttachment(newVersion,
              in, false);
        } finally {
          IOUtils.closeQuietly(buffer);
          IOUtils.closeQuietly(in);
        }
        for (String currentLang : I18NHelper.getAllSupportedLanguages()) {
          if (!currentLang.equalsIgnoreCase(language)) {
            buffer = new ByteArrayOutputStream();
            try {
              AttachmentServiceFactory.getAttachmentService().getBinaryContent(buffer, lastVersion.
                  getPk(), currentLang);
              in = new ByteArrayInputStream(buffer.toByteArray());
              lastVersion = AttachmentServiceFactory.getAttachmentService().
                  searchDocumentById(document.getPk(), currentLang).getLastPublicVersion();
              newVersion =
                  new SimpleDocument(newVersion.getPk(), to.getId(), lastVersion.getOrder(),
                  false, lastVersion.getEditedBy(), new SimpleAttachment(lastVersion.getFilename(),
                  lastVersion.getLanguage(), lastVersion.getTitle(), lastVersion.getDescription(),
                  lastVersion.getSize(), lastVersion.getContentType(), userId, new Date(),
                  lastVersion.getXmlFormId()));
              AttachmentServiceFactory.getAttachmentService().updateAttachment(newVersion, in,
                  false, true);
            } finally {
              IOUtils.closeQuietly(buffer);
              IOUtils.closeQuietly(in);
            }
          }
        }
      }
    }
  }
}
