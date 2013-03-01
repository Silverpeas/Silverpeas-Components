/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
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
package com.stratelia.webactiv.kmelia;

import com.silverpeas.attachment.importExport.AttachmentImportExport;
import com.silverpeas.importExport.control.MassiveDocumentImport;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.MetaData;
import com.silverpeas.util.MetadataExtractor;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.ZipManager;
import com.silverpeas.versioning.importExport.VersioningImportExport;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Class for unitary and massive import
 *
 * @author dlesimple
 */
public class FileImport {

  private AttachmentImportExport attachmentImportExport;
  private VersioningImportExport versioningImportExport;
  private MetadataExtractor metadataExtractor;
  /**
   * Private or Public (ie DocumentVersion)
   */
  private int versionType;
  /**
   * Import in draft mode or not
   */
  private boolean draftMode;
  private String topicId;
  private File fileUploaded;
  private KmeliaSessionController kmeliaScc;

  public void setVersionType(int versionType) {
    this.versionType = versionType;
  }

  public void setDraftMode(boolean draftMode) {
    this.draftMode = draftMode;
  }

  public void setTopicId(String topicId) {
    this.topicId = topicId;
  }

  public void setFileUploaded(File fileUploaded) {
    this.fileUploaded = fileUploaded;
  }

  public void setKmeliaScc(KmeliaSessionController kmeliaScc) {
    this.kmeliaScc = kmeliaScc;
  }

  public FileImport() {
    attachmentImportExport = new AttachmentImportExport();
    versioningImportExport = new VersioningImportExport();
    metadataExtractor = new MetadataExtractor();
  }

  /**
   * Import a single file for a unique publication
   *
   * @return ArrayList of PublicationDetail
   */
  public List<PublicationDetail> importFile() {
    List<PublicationDetail> publicationDetails = new ArrayList<PublicationDetail>();
    // Get files of the concerned upload directory
    File[] filesToProcess = fileUploaded.getParentFile().listFiles();
    PublicationDetail publicationDetail = processImportFile(
        attachmentImportExport, versioningImportExport, filesToProcess,
        KmeliaSessionController.UNITARY_IMPORT_MODE);
    if (publicationDetail != null) {
      publicationDetails.add(publicationDetail);
    }
    return publicationDetails;
  }

  /**
   * Import a zip file for a unique publication with attachments
   *
   * @return ArrayList of PublicationsDetails
   */
  public List<PublicationDetail> importFiles() {
    SilverTrace.info("kmelia", "FileImport.importFiles()", "root.MSG_GEN_ENTER_METHOD");
    List<PublicationDetail> publicationDetails = new ArrayList<PublicationDetail>();
    try {
      String tempFolderPath = unzipUploadedFile();
      Collection<File> filesExtracted = FileUtils.listFiles(new File(tempFolderPath), null, true);
      SilverTrace.info("kmelia", "FileImport.importFiles()", "root.MSG_GEN_PARAM_VALUE",
          "nb filesExtracted = " + filesExtracted.size());
      PublicationDetail publicationDetail = processImportFile(attachmentImportExport,
          versioningImportExport, filesExtracted.toArray(new File[filesExtracted.size()]),
          KmeliaSessionController.MASSIVE_IMPORT_MODE_ONE_PUBLICATION);
      FileFolderManager.deleteFolder(tempFolderPath);
      if (publicationDetail != null) {
        publicationDetails.add(publicationDetail);
      }
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "FileImport.importFiles()", "root.EX_LOAD_ATTACHMENT_FAILED", e);
    }
    SilverTrace.info("kmelia", "FileImport.importFiles()", "root.MSG_GEN_EXIT_METHOD");
    return publicationDetails;
  }

  private String unzipUploadedFile() {
    int nbFiles = ZipManager.getNbFiles(fileUploaded);
    String tempFolderName = Long.toString(System.currentTimeMillis()) + '_' + kmeliaScc.getUserId();
    String tempFolderPath = FileRepositoryManager.getAbsolutePath(kmeliaScc.getComponentId()) +
        GeneralPropertiesManager.getString("RepositoryTypeTemp") +
        File.separator + tempFolderName;
    File tempFolder = new File(tempFolderPath);
    if (!tempFolder.exists()) {
      FileRepositoryManager.createAbsolutePath(kmeliaScc.getComponentId(),
          GeneralPropertiesManager.getString("RepositoryTypeTemp") +
              File.separator + tempFolderName);
    }
    SilverTrace.info("kmelia", "FileImport.importFiles()", "root.MSG_GEN_PARAM_VALUE",
        "nbFiles = " + nbFiles);
    ZipManager.extract(fileUploaded, tempFolder);
    SilverTrace.info("kmelia", "FileImport.importFiles()", "root.MSG_GEN_PARAM_VALUE",
        "tempFolderPath.getPath() = " + tempFolder.getPath());
    return tempFolderPath;
  }

  /**
   * Import a zip file for a publication per file in zip
   *
   * @return List of PublicationsDetail
   */
  public List<PublicationDetail> importFilesMultiPubli() {
    SilverTrace.info("kmelia", "FileImport.importFilesMultiPubli()", "root.MSG_GEN_ENTER_METHOD");
    List<PublicationDetail> publicationDetails = new ArrayList<PublicationDetail>();
    try {
      String tempFolderPath = unzipUploadedFile();
      MassiveDocumentImport massiveImporter = new MassiveDocumentImport();
      publicationDetails = massiveImporter
          .importDocuments(kmeliaScc, tempFolderPath, Integer.parseInt(topicId), draftMode, true);
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "FileImport.importFilesMultiPubli()",
          "root.EX_LOAD_ATTACHMENT_FAILED", e);
    }
    SilverTrace.info("kmelia", "FileImport.importFilesMultiPubli()", "root.MSG_GEN_EXIT_METHOD");
    return publicationDetails;
  }

  /**
   * Convert File into a publication with an attachment
   *
   * @param attachmentIE
   * @param versioningIE
   * @param filesToProcess
   * @param importMode
   * @return PublicationDetail
   */
  private PublicationDetail processImportFile(AttachmentImportExport attachmentIE,
      VersioningImportExport versioningIE, File[] filesToProcess, String importMode) {
    String componentId = kmeliaScc.getComponentId();
    UserDetail userDetail = kmeliaScc.getUserDetail();
    boolean isVersioningUsed = kmeliaScc.isVersionControlled();
    boolean componentDraftMode = kmeliaScc.isDraftEnabled();
    PublicationDetail pubDetailToCreate = null;
    try {
      // Get informations of the document to create the publication
      if (KmeliaSessionController.MASSIVE_IMPORT_MODE_MULTI_PUBLICATIONS.equals(importMode)) {
        pubDetailToCreate = getPublicationDetail(filesToProcess[0]);
      } else {
        pubDetailToCreate = getPublicationDetail(fileUploaded);
      }
      pubDetailToCreate.setPk(new PublicationPK("unknown", "useless",
          componentId));

      // Override draft Mode if the component use it
      if (componentDraftMode && draftMode) {
        pubDetailToCreate.setStatus(PublicationDetail.DRAFT);
      } else if (componentDraftMode && !draftMode) {
        pubDetailToCreate.setStatus(PublicationDetail.VALID);
      }

      // Create the publication
      String pubId = kmeliaScc.getKmeliaBm().createPublicationIntoTopic(
          pubDetailToCreate, new NodePK(topicId, componentId));
      pubDetailToCreate.getPK().setId(pubId);
      SilverTrace.info("kmelia", "FileImport.processImportFile()",
          "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);

      // Add the attachments(s)
      List<AttachmentDetail> attachments = new ArrayList<AttachmentDetail>();
      if (isVersioningUsed) {
        // Versioning Mode
        for (File filesToProces : filesToProcess) {
          AttachmentDetail attDetail = new AttachmentDetail();
          attDetail.setPhysicalName(filesToProces.getAbsolutePath());
          SilverTrace.info("kmelia", "FileImport.processImportFile()",
              "root.MSG_GEN_PARAM_VALUE", "filesExtracted[i].getPath() versioning = " +
              filesToProces.getAbsolutePath());
          attDetail.setAuthor(userDetail.getId());
          attDetail.setInstanceId(componentId);
          attDetail.setPK(new AttachmentPK(componentId));
          // Copy the file on the server and enhance the AttachmentDetail
          SilverTrace.info("kmelia", "FileImport.processImportFile()", "root.MSG_GEN_PARAM_VALUE",
              "versioningIE.getVersioningPath(componentId) = " + versioningIE.getVersioningPath(
                  componentId));

          String filePath = filesToProces.getAbsolutePath();
          MetaData metadata = metadataExtractor.extractMetadata(filePath);
          if (FileUtil.isOpenOfficeCompatible(filePath)) {
            attDetail.setTitle(getOfficeTitle(metadata, filesToProces.getName()));
            attDetail.setDescription(getOfficeSubject(metadata, ""));
            attDetail.setAuthor(getOfficeAuthor(metadata, ""));
          }
          attachments.add(attDetail);
        }
        List<AttachmentDetail> copiedAttachments = attachmentIE.copyFiles(componentId,
            attachments, versioningIE.getVersioningPath(componentId));
        SilverTrace.info("kmelia", "FileImport.processImportFile()",
            "root.MSG_GEN_PARAM_VALUE", "copiedAttachments.size() = " +
            copiedAttachments);
        versioningIE.importDocuments(pubDetailToCreate.getId(), componentId,
            copiedAttachments, Integer.parseInt(userDetail.getId()),
            versionType, KmeliaHelper.isIndexable(pubDetailToCreate));
      } else {
        // Add attachments
        for (File filesToProces : filesToProcess) {
          AttachmentDetail attDetail = new AttachmentDetail();
          SilverTrace.info("kmelia", "FileImport.processImportFile()",
              "root.MSG_GEN_PARAM_VALUE", "filesExtracted[i].getPath() Non versionning = " +
              filesToProces.getAbsolutePath());
          attDetail.setPhysicalName(filesToProces.getAbsolutePath());
          attDetail.setAuthor(userDetail.getId());

          // Get information from Office document
          String filePath = filesToProces.getAbsolutePath();
          MetaData metadata = metadataExtractor.extractMetadata(filePath);
          if (FileUtil.isOpenOfficeCompatible(filePath)) {
            attDetail.setTitle(getOfficeTitle(metadata, filesToProces.getName()));
            attDetail.setDescription(getOfficeSubject(metadata, ""));
          }
          attachments.add(attDetail);
        }
        attachmentIE.importAttachments(pubDetailToCreate.getId(), componentId,
            attachments, userDetail.getId(), KmeliaHelper.isIndexable(pubDetailToCreate));
      }
    } catch (Exception ex) {
      SilverTrace.error("kmelia", "FileImport.processImportFile()", "root.EX_NO_MESSAGE", ex);
    }
    return pubDetailToCreate;
  }

  /**
   * Return a Publication Detail (filled by the Office properties if possible)
   *
   * @param file
   * @return PublicationDetail
   */
  private PublicationDetail getPublicationDetail(File file) {
    SilverTrace.info("kmelia", "FileImport.getPublicationDetail()",
        "root.MSG_GEN_PARAM_VALUE", "fileName = " + file.getName() +
        " filepath=" + file.getAbsolutePath());
    String pubName = formatNameFile(file.getName());
    String description = formatNameFile(file.getName());
    String author = "";
    String keywords = "";
    String content = "";
    MetaData metadata = metadataExtractor.extractMetadata(file);
    if (FileUtil.isOpenOfficeCompatible(file.getAbsolutePath())) {
      pubName = getOfficeTitle(metadata, pubName);
      description = getOfficeSubject(metadata, description);
      author = getOfficeAuthor(metadata, author);
      keywords = getOfficeKeywords(metadata, keywords);
    }
    PublicationDetail publicationDetail =
        new PublicationDetail(null, pubName, description, new Date(), new Date(), null,
            kmeliaScc.getUserDetail().getId(), "1", null, keywords, content, null, "", author);
    if (kmeliaScc.isAuthorUsed()) {
      publicationDetail.setAuthor(author);
    }
    return publicationDetail;
  }

  /**
   * Format file name without extension
   *
   * @param fileName
   * @return fileName formatted
   */
  private String formatNameFile(String fileName) {
    String name = fileName;
    if (fileName.lastIndexOf('.') != -1) {
      name = fileName.substring(0, fileName.lastIndexOf('.'));
    }
    return name;
  }

  /**
   * Get the title of the document
   *
   * @param metadata
   * @param value    Name of the field
   * @return Title
   */
  private String getOfficeTitle(MetaData metadata, String value) {
    String officeValue = value;
    if (StringUtil.isDefined(metadata.getTitle())) {
      officeValue = metadata.getTitle();
    }
    return officeValue;
  }

  /**
   * Get the subject of the document
   *
   * @param metadata
   * @param value
   * @return Subject
   */
  private String getOfficeSubject(MetaData metadata, String value) {
    String officeValue = value;
    if (StringUtil.isDefined(metadata.getSubject())) {
      officeValue = metadata.getSubject();
    }
    return officeValue;
  }

  /**
   * Get the author of the document
   *
   * @param metadata
   * @param value
   * @return Author name
   */
  private String getOfficeAuthor(MetaData metadata, String value) {
    String officeValue = value;
    if (StringUtil.isDefined(metadata.getAuthor())) {
      officeValue = metadata.getAuthor();
    }
    return officeValue;
  }

  /**
   * Get the keywords of the document
   *
   * @param metadata
   * @param value
   * @return
   */
  private String getOfficeKeywords(MetaData metadata, String value) {
    String officeValue = value;
    if (metadata.getKeywords()!= null && metadata.getKeywords().length > 0 ) {
      officeValue = StringUtils.join(metadata.getKeywords(), ';');
    }
    return officeValue;
  }
}