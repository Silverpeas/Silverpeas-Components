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

import com.silverpeas.importExport.control.ImportSettings;
import com.silverpeas.importExport.control.MassiveDocumentImport;
import com.silverpeas.importExport.control.PublicationsTypeManager;
import com.silverpeas.importExport.model.ImportExportException;
import com.silverpeas.importExport.model.PublicationType;
import com.silverpeas.importExport.model.PublicationsType;
import com.silverpeas.importExport.report.ImportReportManager;
import com.silverpeas.importExport.report.MassiveReport;
import com.silverpeas.node.importexport.NodePositionType;
import com.silverpeas.node.importexport.NodePositionsType;
import com.silverpeas.util.ZipManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.silverpeas.importExport.attachment.AttachmentDetail;
import org.silverpeas.importExport.attachment.AttachmentsType;

/**
 * Class for unit and massive import
 *
 * @author dlesimple
 */
public class FileImport {

  /**
   * Private or Public (ie DocumentVersion)
   */
  private int versionType;
  private File fileUploaded;
  private KmeliaSessionController kmeliaScc;

  public FileImport(KmeliaSessionController kmeliaScc, File uploadedFile) {
    this.kmeliaScc = kmeliaScc;
    this.fileUploaded = uploadedFile;
  }

  public void setVersionType(int versionType) {
    this.versionType = versionType;
  }

  /**
   * Import a single file for a unique publication
   *
   * @return ArrayList of PublicationDetail
   * @throws ImportExportException
   */
  public List<PublicationDetail> importFile(boolean draft) throws ImportExportException {
    MassiveDocumentImport massiveImporter = new MassiveDocumentImport();
    ImportSettings settings = getImportSettings(fileUploaded.getParent(), draft);

    return massiveImporter.importDocuments(settings, new MassiveReport());
  }

  /**
   * Import a zip file for a unique publication with attachments
   *
   * @return ArrayList of PublicationsDetails
   */
  public List<PublicationDetail> importFiles(boolean draft) {
    SilverTrace.info("kmelia", "FileImport.importFiles()", "root.MSG_GEN_ENTER_METHOD");
    List<PublicationDetail> publicationDetails = new ArrayList<PublicationDetail>();
    ImportReportManager reportManager = new ImportReportManager();
    try {
      PublicationsTypeManager typeMgr = new PublicationsTypeManager();

      ImportSettings settings = getImportSettings(fileUploaded.getParent(), draft);

      PublicationsType publicationsType = new PublicationsType();
      PublicationType publication = new PublicationType();
      PublicationDetail pubDetail = getPublicationDetail(fileUploaded, settings);
      publication.setPublicationDetail(pubDetail);

      String tempFolderPath = unzipUploadedFile();
      Collection<File> filesExtracted = FileUtils.listFiles(new File(tempFolderPath), null, true);
      SilverTrace.info("kmelia", "FileImport.importFiles()", "root.MSG_GEN_PARAM_VALUE",
          "nb filesExtracted = " + filesExtracted.size());

      // set files to attach to publication
      AttachmentsType attachmentsType = new AttachmentsType();
      List<AttachmentDetail> attachments = new ArrayList<AttachmentDetail>();
      for (File file : filesExtracted) {
        AttachmentDetail attachment = new AttachmentDetail();
        attachment.setPhysicalName(file.getAbsolutePath());
        attachment.setAuthor(kmeliaScc.getUserId());
        attachments.add(attachment);
      }
      attachmentsType.setListAttachmentDetail(attachments);
      publication.setAttachmentsType(attachmentsType);

      // set folder where to create publication
      NodePositionsType nodesType = new NodePositionsType();
      List<NodePositionType> nodes = new ArrayList<NodePositionType>();
      NodePositionType node = new NodePositionType();
      node.setId(Integer.valueOf(kmeliaScc.getCurrentFolderId()));
      nodes.add(node);
      nodesType.setListNodePositionType(nodes);
      publication.setNodePositionsType(nodesType);

      List<PublicationType> publications = new ArrayList<PublicationType>();
      publications.add(publication);
      publicationsType.setListPublicationType(publications);

      // import files and create publication
      typeMgr.processImport(publicationsType, settings, reportManager);

      FileFolderManager.deleteFolder(tempFolderPath);
      if (pubDetail != null) {
        publicationDetails.add(pubDetail);
      }
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "FileImport.importFiles()", "root.EX_LOAD_ATTACHMENT_FAILED", e);
    }
    reportManager.reportImportEnd();
    SilverTrace.info("kmelia", "FileImport.importFiles()", "root.MSG_GEN_EXIT_METHOD");
    return publicationDetails;
  }

  private String unzipUploadedFile() {
    int nbFiles = ZipManager.getNbFiles(fileUploaded);
    String tempFolderName = Long.toString(System.currentTimeMillis()) + '_' + kmeliaScc.getUserId();
    String tempFolderPath =
        FileRepositoryManager.getTemporaryPath() + File.separator + tempFolderName;
    File tempFolder = new File(tempFolderPath);
    if (!tempFolder.exists()) {
      FileRepositoryManager.createGlobalTempPath(tempFolderName);
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
  public List<PublicationDetail> importFilesMultiPubli(boolean draft) {
    SilverTrace.info("kmelia", "FileImport.importFilesMultiPubli()", "root.MSG_GEN_ENTER_METHOD");
    List<PublicationDetail> publicationDetails = new ArrayList<PublicationDetail>();
    try {
      String tempFolderPath = unzipUploadedFile();
      MassiveDocumentImport massiveImporter = new MassiveDocumentImport();
      ImportSettings settings = getImportSettings(tempFolderPath, draft);
      publicationDetails = massiveImporter.importDocuments(settings, new MassiveReport());
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "FileImport.importFilesMultiPubli()",
          "root.EX_LOAD_ATTACHMENT_FAILED", e);
    }
    SilverTrace.info("kmelia", "FileImport.importFilesMultiPubli()", "root.MSG_GEN_EXIT_METHOD");
    return publicationDetails;
  }

  /**
   * Return a Publication Detail (filled by the Office properties if possible)
   *
   * @param file
   * @return PublicationDetail
   */
  private PublicationDetail getPublicationDetail(File file, ImportSettings settings) {
    SilverTrace.info("kmelia", "FileImport.getPublicationDetail()",
        "root.MSG_GEN_PARAM_VALUE", "fileName = " + file.getName() + " filepath=" + file.
        getAbsolutePath());
    String pubName = settings.getPublicationName(file.getName());
    PublicationDetail publicationDetail =
        new PublicationDetail(null, pubName, "", new Date(), new Date(), null,
        kmeliaScc.getUserDetail().getId(), "1", null, "", "", null, "", "");
    return publicationDetail;
  }

  private ImportSettings getImportSettings(String path, boolean draft) {
    ImportSettings settings =
        new ImportSettings(path, kmeliaScc.getUserDetail(), kmeliaScc.getComponentId(),
        kmeliaScc.getCurrentFolderId(), draft, true, ImportSettings.FROM_MANUAL);
    settings.setVersioningUsed(kmeliaScc.isVersionControlled());
    settings.setVersionType(versionType);
    return settings;
  }
}
