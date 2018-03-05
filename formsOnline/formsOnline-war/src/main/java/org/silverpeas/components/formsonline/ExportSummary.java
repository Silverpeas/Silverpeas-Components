package org.silverpeas.components.formsonline;

import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;

import java.io.File;

public class ExportSummary {

  private String fileName;
  private int nbItems;

  public ExportSummary(String fileName, int nbExportedItems) {
    this.fileName = fileName;
    this.nbItems = nbExportedItems;
  }

  public String getFilename() {
    return fileName;
  }

  public long getFileSize() {
    File file = new File(FileRepositoryManager.getTemporaryPath() + fileName);
    return file.length();
  }

  public String getDownloadURL() {
    return FileServerUtils.getUrlToTempDir(fileName);
  }

  public int getNbExportedItems() {
    return nbItems;
  }

}