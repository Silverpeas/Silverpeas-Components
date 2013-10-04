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
package com.ecyrd.jspwiki.providers;

import java.io.BufferedInputStream;
import java.io.File;
import com.ecyrd.jspwiki.*;
import com.ecyrd.jspwiki.attachment.Attachment;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.wiki.control.WikiException;
import com.silverpeas.wiki.control.WikiMultiInstanceManager;
import com.silverpeas.wiki.control.WikiPageDAO;
import com.silverpeas.wiki.control.model.PageDetail;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.attachment.AttachmentException;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.UnlockContext;

import com.silverpeas.util.i18n.I18NHelper;

public class WikiVersioningAttachmentProvider implements WikiAttachmentProvider {

  private WikiEngine m_engine;
  public static final String PAGEDIR = "OLD";
  public static final String DOCUMENT_ID = "DOCUMENT_ID";
  public static final String VERSION_ID = "VERSION_ID";
  public static final String FILE_PATH = "FILE_PATH";
  private WikiPageDAO pageDAO;
  private OrganizationController controller = new OrganizationController();

  @Override
  public void deleteAttachment(Attachment att) throws ProviderException {
    try {
      String id = (String) att.getAttribute(DOCUMENT_ID);
      SimpleDocument document = AttachmentServiceFactory.getAttachmentService().searchDocumentById(
          new SimpleDocumentPK(id, WikiMultiInstanceManager.getComponentId()), null);
      AttachmentServiceFactory.getAttachmentService().deleteAttachment(document);
    } catch (AttachmentException e) {
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    }

  }

  @Override
  public void deleteVersion(Attachment att) throws ProviderException {
    try {
      String id = (String) att.getAttribute(DOCUMENT_ID);
      SimpleDocument document = AttachmentServiceFactory.getAttachmentService().searchDocumentById(
          new SimpleDocumentPK(id, WikiMultiInstanceManager.getComponentId()), null);
      AttachmentServiceFactory.getAttachmentService().deleteAttachment(document);
    } catch (AttachmentException e) {
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    }

  }

  @Override
  public Collection<Attachment> findAttachments(QueryItem[] query) {
    return null;
  }

  @Override
  public InputStream getAttachmentData(Attachment att) throws ProviderException, IOException {
    String path = (String) att.getAttribute(FILE_PATH);
    return new FileInputStream(path);
  }

  @Override
  public Attachment getAttachmentInfo(WikiPage page, String name, int version)
      throws ProviderException {
    Attachment att = new Attachment(m_engine, page.getName(), name);
    SimpleDocument doc = getAttachedDocument(page.getName(), name);
    if (doc == null) {
      return null;
    }
    SimpleDocumentPK docPk = doc.getPk();
    SimpleDocument currentVersion = getDocumentVersion(docPk, version);
    fillAttachment(att, currentVersion);
    return att;
  }

  protected void fillAttachment(Attachment att, SimpleDocument currentVersion) {
    att.setVersion(currentVersion.getMajorVersion());
    att.setAttribute(DOCUMENT_ID, currentVersion.getId());
    att.setAttribute(VERSION_ID, currentVersion.getId());
    att.setAttribute(FILE_PATH, currentVersion.getAttachmentPath());
    att.setFileName(currentVersion.getFilename());
    att.setLastModified(currentVersion.getCreated());
    att.setSize(currentVersion.getSize());
    UserDetail user = controller.getUserDetail(currentVersion.getCreatedBy());
    if (user != null) {
      att.setAuthor(user.getDisplayedName());
    }
    att.setAttribute("userId", "" + currentVersion.getCreatedBy());
    att.setAttribute(WikiPage.CHANGENOTE, currentVersion.getDescription());
  }

  @SuppressWarnings("unchecked")
  protected SimpleDocument getDocumentVersion(SimpleDocumentPK docPk, int versionNumber) throws
      ProviderException {
    try {
      if (versionNumber != WikiProvider.LATEST_VERSION) {
        List<SimpleDocument> versions = ((HistorisedDocument) AttachmentServiceFactory.
            getAttachmentService().searchDocumentById(docPk, null)).getHistory();
        for (SimpleDocument version : versions) {
          if (versionNumber == version.getMajorVersion()) {
            return version;
          }
        }
      }
      return AttachmentServiceFactory.getAttachmentService().searchDocumentById(docPk, null);
    } catch (AttachmentException e) {
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    }
  }

  @SuppressWarnings("unchecked")
  protected List<SimpleDocument> getAllDocumentVersions(SimpleDocumentPK docPk) throws
      ProviderException {
    try {
      return ((HistorisedDocument) AttachmentServiceFactory.getAttachmentService().
          searchDocumentById(docPk, null)).getHistory();
    } catch (AttachmentException e) {
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    }
  }

  @SuppressWarnings("unchecked")
  protected SimpleDocument getAttachedDocument(String pageName, String fileName) throws
      ProviderException {
    try {
      String componentId = WikiMultiInstanceManager.getComponentId();
      SimpleDocumentPK pk = new SimpleDocumentPK(null, componentId);
      PageDetail detail = pageDAO.getPage(pageName, componentId);
      if (detail != null) {
        return AttachmentServiceFactory.getAttachmentService().findExistingDocument(pk, fileName,
            new ForeignPK("" + detail.getId(), componentId), null);
      }
      return null;
    } catch (WikiException e) {
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    } catch (AttachmentException e) {
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    }
  }

  @Override
  public List<Attachment> getVersionHistory(Attachment att) {
    String documentId = (String) att.getAttribute(DOCUMENT_ID);
    SimpleDocumentPK docPk = new SimpleDocumentPK(documentId, WikiMultiInstanceManager
        .getComponentId());
    String pageName = att.getParentName();
    List<Attachment> attachments = new ArrayList<Attachment>();
    try {
      List<SimpleDocument> versions = getAllDocumentVersions(docPk);
      for (SimpleDocument currentVersion : versions) {
        Attachment attachment = new Attachment(m_engine, pageName, currentVersion.getFilename());
        fillAttachment(attachment, currentVersion);
        attachments.add(attachment);
      }
    } catch (ProviderException e) {
    }
    return attachments;
  }

  @Override
  public List<Attachment> listAllChanged(Date timestamp) throws ProviderException {
    return new ArrayList<Attachment>();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Attachment> listAttachments(WikiPage page) throws ProviderException {
    List<Attachment> result = new ArrayList<Attachment>();
    try {
      PageDetail detail = pageDAO.getPage(page.getName(), WikiMultiInstanceManager.getComponentId());
      if (detail != null) {
        List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
            listDocumentsByForeignKey(new ForeignPK("" + detail.getId(), detail.getInstanceId()),
            null);
        for (SimpleDocument document : documents) {
          SimpleDocument version = getDocumentVersion(document.getPk(), LATEST_VERSION);
          Attachment att = new Attachment(m_engine, page.getName(), document.getFilename());
          fillAttachment(att, version);
          result.add(att);
        }
      }
    } catch (WikiException e) {
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    } catch (AttachmentException e) {
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    }
    return result;
  }

  @Override
  public void moveAttachmentsForPage(String oldParent, String newParent) throws ProviderException {
    // Since the page doesn't change its id we don't have to modify the
    // attachments
  }

  @Override
  public void putAttachmentData(Attachment att, InputStream data) throws ProviderException,
      IOException {
    try {
      String instanceId = WikiMultiInstanceManager.getComponentId();
      PageDetail page = pageDAO.getPage(att.getParentName(), instanceId);
      Date creationDate = new Date();
      String fileName = FileUtil.getFilename(att.getFileName());
      if (!StringUtil.isDefined(fileName)) {
        throw new ProviderException("empty.uploaded.file");
      }
      String author = (String) att.getAttribute("userId");
      SimpleDocumentPK docPK = new SimpleDocumentPK(null, instanceId);
      File tempFile = File.createTempFile("silverpeas_", fileName);
      FileUtils.copyInputStreamToFile(data, tempFile);
      SimpleDocument document = getAttachedDocument(att.getParentName(), att.getFileName());
      boolean exists = document != null;
      if (exists) {
        document.setSize(tempFile.length());
      } else {
        document = new HistorisedDocument(docPK, "" + page.getId(), 0, author, new SimpleAttachment(
            fileName, I18NHelper.defaultLanguage, fileName, "", tempFile.length(), FileUtil.
            getMimeType(fileName), author, creationDate, null));
      }
      String comment = (String) att.getAttribute(WikiPage.CHANGENOTE);
      if (comment == null) {
        comment = "";
      }
      InputStream content = new BufferedInputStream(new FileInputStream(tempFile));
      if (!exists) {
        AttachmentServiceFactory.getAttachmentService().createAttachment(document,
            content, true, true);
      } else {
        document.edit(author);
        AttachmentServiceFactory.getAttachmentService().lock(document.getId(), author, null);
        AttachmentServiceFactory.getAttachmentService().updateAttachment(document, content,true, true);
        UnlockContext unlockContext = new UnlockContext(document.getId(), author, null, comment);
        AttachmentServiceFactory.getAttachmentService().unlock(unlockContext);
      }
      content.close();
      FileUtils.deleteQuietly(tempFile);
    } catch (AttachmentException e) {
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    } catch (WikiException e) {
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    }
  }

  @Override
  public String getProviderInfo() {
    return "Silverpeas Provider";
  }

  @Override
  public void initialize(WikiEngine engine, Properties properties)
      throws NoRequiredPropertyException, IOException {
    m_engine = engine;
    pageDAO = new WikiPageDAO();
  }
}
