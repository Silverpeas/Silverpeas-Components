/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.stratelia.webactiv.kmelia;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.silverpeas.attachment.importExport.AttachmentImportExport;
import com.silverpeas.util.MSdocumentPropertiesManager;
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

/**
 * Class for unitary and massive import
 * @author dlesimple
 */
public class FileImport {

  private AttachmentImportExport attachmentImportExport;
  private VersioningImportExport versioningImportExport;
  private MSdocumentPropertiesManager MSdpManager;
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
    // For reading the properties in an Office document
    MSdpManager = new MSdocumentPropertiesManager();
  }

  /**
   * Import a single file for a unique publication
   * @return ArrayList of PublicationDetail
   */
  public ArrayList importFile() {
    ArrayList publicationDetails = new ArrayList();
    // Get files of the concerned upload directory
    File[] filesToProcess = fileUploaded.getParentFile().listFiles();
    // Create publication
    PublicationDetail publicationDetail = processImportFile(
        attachmentImportExport, versioningImportExport, filesToProcess,
        KmeliaSessionController.UNITARY_IMPORT_MODE);
    if (publicationDetail != null)
      publicationDetails.add(publicationDetail);
    return publicationDetails;
  }

  /**
   * Import a zip file for a unique publication with attachments
   * @param KmeliaSessionController kmeliaScc
   * @return ArrayList of PublicationsDetails
   */
  public ArrayList importFiles() {
    SilverTrace.info("kmelia", "FileImport.importFiles()",
        "root.MSG_GEN_ENTER_METHOD");
    ArrayList publicationDetails = new ArrayList();
    int nbFiles = ZipManager.getNbFiles(fileUploaded);

    // Name of temp folder: timestamp and userId
    String tempFolderName = new Long(new Date().getTime()).toString() + "_"
        + kmeliaScc.getUserId();

    // Directory Temp for the extracted files
    try {
      String tempFolderPath = FileRepositoryManager.getAbsolutePath(kmeliaScc
          .getComponentId())
          + GeneralPropertiesManager.getGeneralResourceLocator().getString(
          "RepositoryTypeTemp") + File.separator + tempFolderName;
      // Create folder if necessary
      if (!new File(tempFolderPath).exists())
        FileRepositoryManager.createAbsolutePath(kmeliaScc.getComponentId(),
            GeneralPropertiesManager.getGeneralResourceLocator().getString(
            "RepositoryTypeTemp")
            + File.separator + tempFolderName);

      // Extraction of the files
      SilverTrace.info("kmelia", "FileImport.importFiles()",
          "root.MSG_GEN_PARAM_VALUE", "nbFiles = " + nbFiles);
      ZipManager.extract(fileUploaded, new File(tempFolderPath));
      SilverTrace.info("kmelia", "FileImport.importFiles()",
          "root.MSG_GEN_PARAM_VALUE", "tempFolderPath.getPath() = "
          + new File(tempFolderPath).getPath());

      // Get files of the concerned upload directory
      File[] filesExtracted = new File(tempFolderPath + File.separator)
          .listFiles();

      SilverTrace.info("kmelia", "FileImport.importFiles()",
          "root.MSG_GEN_PARAM_VALUE", "nb filesExtracted = "
          + filesExtracted.length);

      // Create publication
      PublicationDetail publicationDetail = processImportFile(
          attachmentImportExport, versioningImportExport, filesExtracted,
          KmeliaSessionController.MASSIVE_IMPORT_MODE_ONE_PUBLICATION);
      FileFolderManager.deleteFolder(tempFolderPath);
      if (publicationDetail != null)
        publicationDetails.add(publicationDetail);
    } catch (Exception e) {
      // Other exception
      SilverTrace.warn("kmelia", "FileImport.importFiles()",
          "root.EX_LOAD_ATTACHMENT_FAILED", e);
    }
    SilverTrace.info("kmelia", "FileImport.importFiles()",
        "root.MSG_GEN_EXIT_METHOD");
    return publicationDetails;
  }

  /**
   * Import a zip file for a publication per file in zip
   * @return List of PublicationsDetail
   */
  public ArrayList importFilesMultiPubli() {
    SilverTrace.info("kmelia", "FileImport.importFilesMultiPubli()",
        "root.MSG_GEN_ENTER_METHOD");
    ArrayList publicationDetails = new ArrayList();

    // Name of temp folder: timestamp and userId
    String tempFolderName = new Long(new Date().getTime()).toString() + "_"
        + kmeliaScc.getUserId();
    // Directory Temp for the extracted files
    try {
      String tempFolderPath = FileRepositoryManager.getAbsolutePath(kmeliaScc
          .getComponentId())
          + GeneralPropertiesManager.getGeneralResourceLocator().getString(
          "RepositoryTypeTemp") + File.separator + tempFolderName;
      // Create folder if necessary
      if (!new File(tempFolderPath).exists())
        FileRepositoryManager.createAbsolutePath(kmeliaScc.getComponentId(),
            GeneralPropertiesManager.getGeneralResourceLocator().getString(
            "RepositoryTypeTemp")
            + File.separator + tempFolderName);

      // Extraction of the files
      ZipManager.extract(fileUploaded, new File(tempFolderPath));
      SilverTrace.info("kmelia", "FileImport.importFilesMultiPubli()",
          "root.MSG_GEN_PARAM_VALUE", "tempFolderPath.getPath() = "
          + new File(tempFolderPath).getPath());

      // Get files of the concerned upload directory
      File[] filesExtracted = new File(tempFolderPath + File.separator)
          .listFiles();

      SilverTrace.info("kmelia", "FileImport.importFilesMultiPubli()",
          "root.MSG_GEN_PARAM_VALUE", "nb filesExtracted = "
          + filesExtracted.length + " File="
          + fileUploaded.getAbsolutePath());

      for (int i = 0; i < filesExtracted.length; i++) {
        File[] fileToProcess = new File[1];
        fileToProcess[0] = filesExtracted[i];
        // Create publications
        PublicationDetail publicationDetail = processImportFile(
            attachmentImportExport, versioningImportExport, fileToProcess,
            KmeliaSessionController.MASSIVE_IMPORT_MODE_MULTI_PUBLICATIONS);
        if (publicationDetail != null)
          publicationDetails.add(publicationDetail);
      }
      FileFolderManager.deleteFolder(tempFolderPath);
    } catch (Exception e) {
      // Other exception
      SilverTrace.warn("kmelia", "FileImport.importFilesMultiPubli()",
          "root.EX_LOAD_ATTACHMENT_FAILED", e);
    }
    SilverTrace.info("kmelia", "FileImport.importFilesMultiPubli()",
        "root.MSG_GEN_EXIT_METHOD");
    return publicationDetails;
  }

  /**
   * Convert File into a publication with an attachment
   * @param attachmentIE
   * @param versioningIE
   * @param fileToProcess
   * @param importMode
   * @return PublicationDetail
   */
  private PublicationDetail processImportFile(
      AttachmentImportExport attachmentIE, VersioningImportExport versioningIE,
      File[] filesToProcess, String importMode) {
    String componentId = kmeliaScc.getComponentId();
    UserDetail userDetail = kmeliaScc.getUserDetail();
    boolean isVersioningUsed = kmeliaScc.isVersionControlled();
    boolean componentDraftMode = kmeliaScc.isDraftEnabled();
    PublicationDetail pubDetailToCreate = null;
    try {

      // Get informations of the document to create the publication
      if (importMode
          .equals(KmeliaSessionController.MASSIVE_IMPORT_MODE_MULTI_PUBLICATIONS))
        pubDetailToCreate = getPublicationDetail(filesToProcess[0]);
      else
        pubDetailToCreate = getPublicationDetail(fileUploaded);
      pubDetailToCreate.setPk(new PublicationPK("unknown", "useless",
          componentId));

      // Override draft Mode if the component use it
      if (componentDraftMode && draftMode)
        pubDetailToCreate.setStatus("Draft");
      else if (componentDraftMode && !draftMode)
        pubDetailToCreate.setStatus("Valid");

      // Create the publication
      String pubId = kmeliaScc.getKmeliaBm().createPublicationIntoTopic(
          pubDetailToCreate, new NodePK(topicId, componentId));
      pubDetailToCreate.getPK().setId(pubId);
      SilverTrace.info("kmelia", "FileImport.processImportFile()",
          "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);

      // Add the attachments(s)
      List attachments = new ArrayList();
      if (isVersioningUsed) {
        // Versioning Mode
        for (int i = 0; i < filesToProcess.length; i++) {
          AttachmentDetail attDetail = new AttachmentDetail();
          attDetail.setPhysicalName(filesToProcess[i].getAbsolutePath());
          SilverTrace.info("kmelia", "FileImport.processImportFile()",
              "root.MSG_GEN_PARAM_VALUE",
              "filesExtracted[i].getPath() versioning = "
              + filesToProcess[i].getAbsolutePath());
          attDetail.setAuthor(userDetail.getId());
          attDetail.setInstanceId(componentId);
          attDetail.setPK(new AttachmentPK(componentId));
          // Copy the file on the server and enhance the AttachmentDetail
          SilverTrace.info("kmelia", "FileImport.processImportFile()",
              "root.MSG_GEN_PARAM_VALUE",
              "versioningIE.getVersioningPath(componentId) = "
              + versioningIE.getVersioningPath(componentId));

          String filePath = filesToProcess[i].getAbsolutePath();
          if (MSdpManager.isSummaryInformation(filePath)) {
            attDetail.setTitle(getOfficeTitle(filePath, ""));
            attDetail.setDescription(getOfficeSubject(filePath, ""));
            attDetail.setAuthor(getOfficeAuthor(filePath, ""));
          }
          attachments.add(attDetail);
        }
        List copiedAttachments = attachmentIE.copyFiles(componentId,
            attachments, versioningIE.getVersioningPath(componentId));
        SilverTrace.info("kmelia", "FileImport.processImportFile()",
            "root.MSG_GEN_PARAM_VALUE", "copiedAttachments.size() = "
            + copiedAttachments);
        versioningIE.importDocuments(pubDetailToCreate.getId(), componentId,
            copiedAttachments, new Integer(userDetail.getId()).intValue(),
            versionType, KmeliaHelper.isIndexable(pubDetailToCreate));
      } else {
        // Add attachments
        for (int i = 0; i < filesToProcess.length; i++) {
          AttachmentDetail attDetail = new AttachmentDetail();
          SilverTrace.info("kmelia", "FileImport.processImportFile()",
              "root.MSG_GEN_PARAM_VALUE",
              "filesExtracted[i].getPath() Non versionning = "
              + filesToProcess[i].getAbsolutePath());
          attDetail.setPhysicalName(filesToProcess[i].getAbsolutePath());
          attDetail.setAuthor(userDetail.getId());

          // Get information from Office document
          String filePath = filesToProcess[i].getAbsolutePath();
          if (MSdpManager.isSummaryInformation(filePath)) {
            attDetail.setTitle(getOfficeTitle(filePath, ""));
            attDetail.setDescription(getOfficeSubject(filePath, ""));
          }
          attachments.add(attDetail);
        }
        attachmentIE.importAttachments(pubDetailToCreate.getId(), componentId,
            attachments, userDetail.getId(), KmeliaHelper
            .isIndexable(pubDetailToCreate));
      }
    } catch (Exception ex) {
      SilverTrace.error("kmelia", "FileImport.processImportFile()",
          "root.EX_NO_MESSAGE", ex);
    }
    return pubDetailToCreate;
  }

  /**
   * Return a Publication Detail (filled by the Office properties if possible)
   * @param file
   * @return PublicationDetail
   */
  private PublicationDetail getPublicationDetail(File file) {
    SilverTrace.info("kmelia", "FileImport.getPublicationDetail()",
        "root.MSG_GEN_PARAM_VALUE", "fileName = " + file.getName()
        + " filepath=" + file.getAbsolutePath());
    String pubName = formatNameFile(file.getName());
    String description = formatNameFile(file.getName());
    String author = "";
    String keywords = "";
    String content = "";
    String filePath = file.getAbsolutePath();
    if (MSdpManager.isSummaryInformation(file.getAbsolutePath())) {
      pubName = getOfficeTitle(filePath, pubName);
      description = getOfficeSubject(filePath, description);
      author = getOfficeAuthor(filePath, author);
      keywords = getOfficeKeywords(filePath, keywords);
    }
    PublicationDetail publicationDetail = new PublicationDetail(null,
        pubName/* nom */, description/* description */, new Date()/*
                                                                   * date de création
                                                                   */,
        new Date()/* date de début de validité */, null/*
                                                        * date de fin de validité
                                                        */, kmeliaScc
        .getUserDetail().getId()/* id user */, "1"/* importance */,
        null/* version de la publication */, keywords/* keywords */, content,
        null, author);
    if (kmeliaScc.isAuthorUsed())
      publicationDetail.setAuthor(author);
    return publicationDetail;
  }

  /**
   * Format file name without extension
   * @param fileName
   * @return fileName formatted
   */
  private String formatNameFile(String fileName) {
    String name = fileName;
    if (fileName.lastIndexOf(".") != -1)
      name = fileName.substring(0, fileName.lastIndexOf('.'));
    return name;
  }

  /**
   * Get the title of the document
   * @param file Office File
   * @param value Name of the field
   * @return Title
   */
  private String getOfficeTitle(String file, String value) {
    String officeValue = value;
    if (MSdpManager.getTitle(file) != null) {
      if (!MSdpManager.getTitle(file).equals("")
          && MSdpManager.getTitle(file).length() > 1)
        officeValue = MSdpManager.getTitle(file);
    }
    return officeValue;
  }

  /**
   * Get the subject of the document
   * @param file
   * @param value
   * @return Subject
   */
  private String getOfficeSubject(String file, String value) {
    String officeValue = value;
    if (MSdpManager.getSubject(file) != null) {
      if (!MSdpManager.getSubject(file).equals("")
          && MSdpManager.getSubject(file).length() > 1)
        officeValue = MSdpManager.getSubject(file);
    }
    return officeValue;
  }

  /**
   * Get the author of the document
   * @param file
   * @param value
   * @return Author name
   */
  private String getOfficeAuthor(String file, String value) {
    String officeValue = value;
    if (MSdpManager.getAuthor(file) != null) {
      if (!MSdpManager.getAuthor(file).equals("")
          && MSdpManager.getAuthor(file).length() > 1)
        officeValue = MSdpManager.getAuthor(file);
    }
    return officeValue;
  }

  /**
   * Get the keywords of the document
   * @param file
   * @param value
   * @return
   */
  private String getOfficeKeywords(String file, String value) {
    String officeValue = value;
    if (MSdpManager.getKeywords(file) != null) {
      if (!MSdpManager.getKeywords(file).equals("")
          && MSdpManager.getKeywords(file).length() > 1)
        officeValue = MSdpManager.getKeywords(file);
    }
    return officeValue;
  }

}