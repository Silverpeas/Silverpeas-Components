/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
import com.silverpeas.importExport.model.ImportExportException;
import com.silverpeas.importExport.report.ImportReport;
import com.silverpeas.importExport.report.MassiveReport;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ZipManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import org.apache.commons.io.IOUtils;
import org.silverpeas.util.error.SilverpeasTransverseErrorUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.io.File.separator;
import static org.silverpeas.util.Charsets.UTF_8;

/**
 * Class for unit and massive import
 * @author dlesimple
 */
public class FileImport {

  private static final ResourceLocator settings =
      new ResourceLocator("org.silverpeas.importExport.settings.mapping", "");

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
   * @return a report of the import
   * @throws ImportExportException
   */
  public ImportReport importFile(boolean draft) throws ImportExportException {
    MassiveDocumentImport massiveImporter = MassiveDocumentImport.getInstance();
    ImportSettings importSettings = getImportSettings(fileUploaded.getParent(), draft);

    return massiveImporter.importDocuments(importSettings, new MassiveReport());
  }

  /**
   * Import a zip file for a unique publication with attachments
   * @return a report of the import
   */
  public ImportReport importFiles(boolean draft) {
    SilverTrace.info("kmelia", "FileImport.importFiles()", "root.MSG_GEN_ENTER_METHOD");
    ImportReport importReport = null;
    try {
      File tempFolder = unzipUploadedFile();

      FileUtil.moveAllFilesAtRootFolder(tempFolder);

      MassiveDocumentImport massiveImporter = new MassiveDocumentImport();
      ImportSettings settings = getImportSettings(tempFolder.getPath(), draft);
      settings.getPublicationForAllFiles()
          .setName(settings.getPublicationName(fileUploaded.getName()));
      importReport = massiveImporter.importDocuments(settings, new MassiveReport());
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "FileImport.importFiles()", "root.EX_LOAD_ATTACHMENT_FAILED", e);
      SilverpeasTransverseErrorUtil.throwTransverseErrorIfAny(e,
          UserDetail.getCurrentRequester().getUserPreferences().getLanguage());
    }
    return importReport;
  }

  private File unzipUploadedFile() {
    int nbFiles = ZipUtil.getNbFiles(fileUploaded);
    String tempFolderName = Long.toString(System.currentTimeMillis()) + '_' + kmeliaScc.getUserId();
    File tempFolder = new File(FileRepositoryManager.getTemporaryPath(), tempFolderName);
    if (!tempFolder.exists()) {
      FileRepositoryManager.createGlobalTempPath(tempFolderName);
    }
    SilverTrace.info("kmelia", "FileImport.importFiles()", "root.MSG_GEN_PARAM_VALUE",
        "nbFiles = " + nbFiles);
    ZipUtil.extract(fileUploaded, tempFolder);
    SilverTrace.info("kmelia", "FileImport.importFiles()", "root.MSG_GEN_PARAM_VALUE",
        "tempFolderPath.getPath() = " + tempFolder.getPath());
    return tempFolder;
  }

  /**
   * Import a zip file for a publication per file in zip
   * @return a report of the import
   */
  public ImportReport importFilesMultiPubli(boolean draft) {
    SilverTrace.info("kmelia", "FileImport.importFilesMultiPubli()", "root.MSG_GEN_ENTER_METHOD");
    ImportReport importReport = null;
    try {
      File tempFolder = unzipUploadedFile();

      if (!kmeliaScc.getHighestSilverpeasUserRole().isGreaterThanOrEquals(SilverpeasRole.admin)) {
        FileUtil.moveAllFilesAtRootFolder(tempFolder);
      }

      MassiveDocumentImport massiveImporter = MassiveDocumentImport.getInstance();
      ImportSettings settings = getImportSettings(tempFolder.getPath(), draft);
      importReport = massiveImporter.importDocuments(settings, new MassiveReport());
    } catch (Exception e) {
      SilverTrace
          .warn("kmelia", "FileImport.importFilesMultiPubli()", "root.EX_LOAD_ATTACHMENT_FAILED",
              e);
      SilverpeasTransverseErrorUtil.throwTransverseErrorIfAny(e,
          UserDetail.getCurrentRequester().getUserPreferences().getLanguage());
    }
    SilverTrace.info("kmelia", "FileImport.importFilesMultiPubli()", "root.MSG_GEN_EXIT_METHOD");
    return importReport;
  }

  /**
   * Write import report into a log file
   */
  public void writeImportToLog(ImportReport importReport, ResourcesWrapper resource) {
    if (importReport != null) {
      String reportLogFile = settings.getString("importExportLogFile");
      ResourceBundle resources = FileUtil
          .loadBundle("com.stratelia.silverpeas.silvertrace.settings.silverTrace",
              new Locale("", ""));
      String reportLogPath = resources.getString("ErrorDir");
      File file = new File(reportLogPath + separator + reportLogFile);
      Writer fileWriter = null;
      try {
        if (!file.exists()) {
          file.createNewFile();
        }
        fileWriter = new OutputStreamWriter(new FileOutputStream(file.getPath(), true), UTF_8);
        fileWriter.write(importReport.writeToLog(resource));
      } catch (IOException ex) {
        SilverTrace.error("kmelia", "FileImport.writeImportToLog()", "root.EX_CANT_WRITE_FILE", ex);
      } finally {
        IOUtils.closeQuietly(fileWriter);
      }
    }
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
