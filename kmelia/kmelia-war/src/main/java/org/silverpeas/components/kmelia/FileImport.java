/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia;

import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.importexport.control.ImportSettings;
import org.silverpeas.core.importexport.control.MassiveDocumentImport;
import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.report.ImportReport;
import org.silverpeas.core.importexport.report.MassiveReport;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.ZipUtil;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;

import java.io.*;

import static org.silverpeas.core.util.Charsets.UTF_8;

/**
 * Class for unit and massive import
 * @author dlesimple
 */
public class FileImport {

  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.importExport.settings.mapping");

  /**
   * Private or Public (ie DocumentVersion)
   */
  private int versionType;
  private final File fileUploaded;
  private final KmeliaSessionController kmeliaScc;
  private final MassiveDocumentImport docImport = MassiveDocumentImport.get();

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
   * @throws ImportExportException if the import fails.
   */
  public ImportReport importFile(boolean draft) throws ImportExportException {
    ImportSettings importSettings = getImportSettings(fileUploaded.getParent(), draft);

    return MassiveDocumentImport.get().importDocuments(importSettings, new MassiveReport());
  }

  /**
   * Import a zip file for a unique publication with attachments
   * @return a report of the import
   */
  public ImportReport importFiles(boolean draft) {
    ImportReport importReport = null;
    try {
      File tempFolder = unzipUploadedFile();

      FileUtil.moveAllFilesAtRootFolder(tempFolder);

      ImportSettings importSettings = getImportSettings(tempFolder.getPath(), draft);
      importSettings.getPublicationForAllFiles()
          .setName(importSettings.getPublicationName(fileUploaded.getName()));
      importReport = docImport.importDocuments(importSettings, new MassiveReport());
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      SilverpeasTransverseErrorUtil.throwTransverseErrorIfAny(e,
          UserDetail.getCurrentRequester().getUserPreferences().getLanguage());
    }
    return importReport;
  }

  private File unzipUploadedFile() {
    String tempFolderName = Long.toString(System.currentTimeMillis()) + '_' + kmeliaScc.getUserId();
    File tempFolder = new File(FileRepositoryManager.getTemporaryPath(), tempFolderName);
    if (!tempFolder.exists()) {
      FileRepositoryManager.createGlobalTempPath(tempFolderName);
    }
    ZipUtil.extract(fileUploaded, tempFolder);
    return tempFolder;
  }

  /**
   * Import a zip file for a publication per file in zip
   * @return a report of the import
   */
  public ImportReport importFilesMultiPubli(boolean draft) {
    ImportReport importReport = null;
    try {
      File tempFolder = unzipUploadedFile();

      if (!kmeliaScc.getHighestSilverpeasUserRole().isGreaterThanOrEquals(SilverpeasRole.ADMIN)) {
        FileUtil.moveAllFilesAtRootFolder(tempFolder);
      }

      ImportSettings importSettings = getImportSettings(tempFolder.getPath(), draft);
      importReport = MassiveDocumentImport.get().importDocuments(importSettings, new MassiveReport());
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      SilverpeasTransverseErrorUtil.throwTransverseErrorIfAny(e,
          UserDetail.getCurrentRequester().getUserPreferences().getLanguage());
    }
    return importReport;
  }

  /**
   * Write import report into a log file
   */
  public void writeImportToLog(ImportReport importReport, MultiSilverpeasBundle resource) {
    if (importReport != null) {
      String reportLogFile = settings.getString("importExportLogFile");
      File file = new File(reportLogFile);
      try {
        if (!file.exists()) {
          boolean created = file.createNewFile();
          if (!created) {
            SilverLogger.getLogger(this).error("Log file " + file.getName() + " cannot be created");
            return;
          }
        }
        try (Writer fileWriter = new OutputStreamWriter(new FileOutputStream(file.getPath(), true)
        , UTF_8)) {
          fileWriter.write(importReport.writeToLog(resource));
        }
      } catch (IOException ex) {
        SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      }
    }
  }

  private ImportSettings getImportSettings(String path, boolean draft) {
    ImportSettings importSettings =
        new ImportSettings(path, kmeliaScc.getUserDetail(), kmeliaScc.getComponentId(),
            kmeliaScc.getCurrentFolderId(), draft, true, ImportSettings.FROM_MANUAL);
    importSettings.setVersioningUsed(kmeliaScc.isVersionControlled());
    importSettings.setVersionType(versionType);
    return importSettings;
  }
}
