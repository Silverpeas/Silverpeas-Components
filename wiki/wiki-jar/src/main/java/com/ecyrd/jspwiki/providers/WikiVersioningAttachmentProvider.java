package com.ecyrd.jspwiki.providers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.ejb.CreateException;

import com.ecyrd.jspwiki.NoRequiredPropertyException;
import com.ecyrd.jspwiki.QueryItem;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.WikiProvider;
import com.ecyrd.jspwiki.attachment.Attachment;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.versioning.VersioningIndexer;
import com.silverpeas.wiki.control.WikiException;
import com.silverpeas.wiki.control.WikiMultiInstanceManager;
import com.silverpeas.wiki.control.WikiPageDAO;
import com.silverpeas.wiki.control.model.PageDetail;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.UtilException;

public class WikiVersioningAttachmentProvider implements WikiAttachmentProvider {

  private VersioningBm versioningBm = null;

  private WikiEngine m_engine;

  public static final String PAGEDIR = "OLD";

  public static final String DOCUMENT_ID = "DOCUMENT_ID";

  public static final String VERSION_ID = "VERSION_ID";

  public static final String FILE_PATH = "FILE_PATH";

  private WikiPageDAO pageDAO;

  private OrganizationController controller = new OrganizationController();

  private VersioningIndexer indexer = new VersioningIndexer();

  private VersioningBm getVersioningBm() throws UtilException, RemoteException,
      CreateException {
    if (versioningBm == null) {
      VersioningBmHome versioningBmHome = (VersioningBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.VERSIONING_EJBHOME, VersioningBmHome.class);
      versioningBm = versioningBmHome.create();
    }
    return versioningBm;
  }

  @Override
  public void deleteAttachment(Attachment att) throws ProviderException {
    try {
      String id = (String) att.getAttribute(DOCUMENT_ID);
      int documentId = Integer.parseInt(id);
      versioningBm.deleteDocument(new DocumentPK(documentId,
          WikiMultiInstanceManager.getComponentId()));
    } catch (NumberFormatException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    } catch (RemoteException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    }

  }

  @Override
  public void deleteVersion(Attachment att) throws ProviderException {
    try {
      String id = (String) att.getAttribute(DOCUMENT_ID);
      int documentId = Integer.parseInt(id);
      versioningBm.deleteDocument(new DocumentPK(documentId,
          WikiMultiInstanceManager.getComponentId()));
    } catch (NumberFormatException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    } catch (RemoteException e) {
      e.printStackTrace();
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
  public InputStream getAttachmentData(Attachment att)
      throws ProviderException, IOException {
    String path = (String) att.getAttribute(FILE_PATH);
    return new FileInputStream(path);
  }

  @Override
  public Attachment getAttachmentInfo(WikiPage page, String name, int version)
      throws ProviderException {
    Attachment att = new Attachment(m_engine, page.getName(), name);
    Document doc = getAttachedDocument(page.getName(), name);
    if (doc == null) {
      return null;
    }
    DocumentPK docPk = doc.getPk();
    DocumentVersion currentVersion = getDocumentVersion(docPk, version);
    fillAttachment(att, currentVersion);
    return att;
  }

  protected void fillAttachment(Attachment att, DocumentVersion currentVersion) {
    att.setVersion(currentVersion.getMajorNumber());
    att.setAttribute(DOCUMENT_ID, currentVersion.getDocumentPK().getId());
    att.setAttribute(VERSION_ID, currentVersion.getPk().getId());
    att.setAttribute(FILE_PATH, currentVersion.getDocumentPath());
    att.setFileName(currentVersion.getLogicalName());
    att.setLastModified(currentVersion.getCreationDate());
    att.setSize(currentVersion.getSize());
    UserDetail user = controller.getUserDetail(""
        + currentVersion.getAuthorId());
    if (user != null) {
      att.setAuthor(user.getDisplayedName());
    }
    att.setAttribute("userId", "" + currentVersion.getAuthorId());
    att.setAttribute(WikiPage.CHANGENOTE, currentVersion.getComments());
  }

  @SuppressWarnings("unchecked")
  protected DocumentVersion getDocumentVersion(DocumentPK docPk,
      int versionNumber) throws ProviderException {
    try {
      if (versionNumber != WikiProvider.LATEST_VERSION) {
        List<DocumentVersion> versions = getVersioningBm().getDocumentVersions(
            docPk);
        for (DocumentVersion version : versions) {
          if (versionNumber == version.getMajorNumber()) {
            return version;
          }
        }
      }
      return getVersioningBm().getLastDocumentVersion(docPk);
    } catch (RemoteException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    } catch (UtilException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    } catch (CreateException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    }
  }

  @SuppressWarnings("unchecked")
  protected List<DocumentVersion> getAllDocumentVersions(DocumentPK docPk)
      throws ProviderException {
    try {
      return getVersioningBm().getDocumentVersions(docPk);
    } catch (RemoteException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    } catch (UtilException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    } catch (CreateException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    }
  }

  @SuppressWarnings("unchecked")
  protected Document getAttachedDocument(String pageName, String name)
      throws ProviderException {
    try {
      PageDetail detail = pageDAO.getPage(pageName, WikiMultiInstanceManager
          .getComponentId());
      if (detail != null) {
        List<Document> documents = getVersioningBm().getDocuments(
            new ForeignPK("" + detail.getId(), detail.getInstanceId()));
        for (Document doc : documents) {
          if (name.equals(doc.getName())) {
            return doc;
          }
        }
      }
      return null;
    } catch (WikiException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    } catch (RemoteException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    } catch (UtilException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    } catch (CreateException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    }
  }

  @Override
  public List<Attachment> getVersionHistory(Attachment att) {
    int documentId = Integer.parseInt((String) att.getAttribute(DOCUMENT_ID));
    DocumentPK docPk = new DocumentPK(documentId, WikiMultiInstanceManager
        .getComponentId());    
    String pageName = att.getParentName();
    List<Attachment> attachments = new ArrayList<Attachment>();
    try {
    List<DocumentVersion> versions = getAllDocumentVersions(docPk);
    for (DocumentVersion currentVersion : versions) {
      Attachment attachment = new Attachment(m_engine, pageName, currentVersion
          .getLogicalName());
      fillAttachment(attachment, currentVersion);
      attachments.add(attachment);
    }
    }catch (ProviderException e) {
      e.printStackTrace();
    }
    return attachments;
  }

  @Override
  public List<Attachment> listAllChanged(Date timestamp)
      throws ProviderException {
    return new ArrayList<Attachment>();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Attachment> listAttachments(WikiPage page)
      throws ProviderException {
    List<Attachment> result = new ArrayList<Attachment>();
    try {
      PageDetail detail = pageDAO.getPage(page.getName(),
          WikiMultiInstanceManager.getComponentId());
      if (detail != null) {
        List<Document> documents = getVersioningBm().getDocuments(
            new ForeignPK("" + detail.getId(), detail.getInstanceId()));
        for (Document document : documents) {
          DocumentVersion version = getDocumentVersion(document.getPk(),
              LATEST_VERSION);
          Attachment att = new Attachment(m_engine, page.getName(), document
              .getName());
          fillAttachment(att, version);
          result.add(att);
        }
      }
    } catch (WikiException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    } catch (RemoteException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    } catch (UtilException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    } catch (CreateException e) {
      e.printStackTrace();
      ProviderException ex = new ProviderException(e.getMessage());
      ex.initCause(e);
      throw ex;
    }
    return result;
  }

  @Override
  public void moveAttachmentsForPage(String oldParent, String newParent)
      throws ProviderException {
    // Since the page doesn't change its id we don't have to modify the
    // attachments

  }

  @Override
  public void putAttachmentData(Attachment att, InputStream data)
      throws ProviderException, IOException {
    try {
      String instanceId = WikiMultiInstanceManager.getComponentId();
      PageDetail page = pageDAO.getPage(att.getParentName(), instanceId);
      Date creationDate = new Date();
      String logicalName = att.getFileName();
      String path = indexer.createPath(null, instanceId);
      String physicalName = new Long(creationDate.getTime()).toString() + "."
          + FileRepositoryManager.getFileExtension(logicalName);
      java.io.File f = new java.io.File(path + physicalName);
      String author = (String) att.getAttribute("userId");
      FileUtil.writeFile(f, data);
      String mimeType = FileUtil.getMimeType(logicalName);
      // int user_id = Integer.parseInt(userId);
      ForeignPK pubForeignKey = new ForeignPK("" + page.getId(), instanceId);
      int userId = Integer.parseInt(author);
      int majorNumber = 1;
      int minorNumber = 0;
      DocumentPK docPK = new DocumentPK(-1, null, instanceId);
      Document document = getAttachedDocument(att.getParentName(), att
          .getFileName());
      boolean exists = document != null;
      if (exists) {        
        docPK = document.getPk();
        DocumentVersion lastVersion = getVersioningBm().getLastDocumentVersion(docPK);
        majorNumber = lastVersion.getMajorNumber();
      } else {
        document = new Document(docPK, pubForeignKey, logicalName, "", -1,
            userId, creationDate, null, null, null, null, 0, 0);
      }
      String comment = (String) att.getAttribute(WikiPage.CHANGENOTE);
      if (comment == null) {
        comment = "";
      }
      DocumentVersion newVersion = new DocumentVersion(null, docPK,
          majorNumber, minorNumber, userId, creationDate, comment, 0, 0,
          physicalName, logicalName, mimeType, (int) att.getSize(), instanceId);
      if (exists) {
        getVersioningBm().addDocumentVersion(document, newVersion);
      } else {
        // create the document with its first version
        DocumentPK documentPK = getVersioningBm().createDocument(document,
            newVersion);
        document.setPk(documentPK);
      }
      if (newVersion.getType() == DocumentVersion.TYPE_PUBLIC_VERSION) {
        CallBackManager.invoke(CallBackManager.ACTION_VERSIONING_UPDATE,
            newVersion.getAuthorId(), document.getForeignKey().getInstanceId(),
            document.getForeignKey().getId());
        indexer.createIndex(document, newVersion);
      }
    } catch (Exception e) {
      e.printStackTrace();
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
