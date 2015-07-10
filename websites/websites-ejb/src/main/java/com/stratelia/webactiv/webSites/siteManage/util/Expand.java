/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.webSites.siteManage.util;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.exception.SilverpeasException;
import com.stratelia.webactiv.webSites.control.WebSitesException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Unzip a file.
 */
public class Expand {

  private File dest; // required
  private File source; // required

  /**
   * Do the work.
   * @throws WebSitesException Thrown in unrecoverable error.
   */
  public void execute() throws WebSitesException {
    if (source == null) {
      throw new WebSitesException("Expand.execute()", SilverpeasException.ERROR,
          "webSites.EXE_SOURCE_FILE_ATTRIBUTE_MUST_BE_SPECIFIED");
    }
    if (dest == null) {
      throw new WebSitesException("Expand.execute()", SilverpeasException.ERROR,
          "webSites.EXE_DESTINATION_FILE_ATTRIBUTE_MUST_BE_SPECIFIED");
    }
    expandFile(source, dest);
  }

  /**
   * ExpandFile
   */
  private void expandFile(File srcF, File dir) {
    ZipFile zf = null;
    try {

      zf = new ZipFile(srcF);
      ZipEntry ze;

      Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zf.entries();
      while (entries.hasMoreElements()) {
        ze = entries.nextElement();
        String entryName = dir.getAbsolutePath();
        if (!entryName.endsWith(File.separator) && !ze.getName().startsWith(File.separator)) {
          entryName = entryName + File.separatorChar + ze.getName();
        } else {
          entryName = entryName + ze.getName();
        }
        entryName = validateFilename(entryName, dir.getAbsolutePath());
        File f = new File(entryName);
        try {
          // create intermediary directories - sometimes zip don't add them
          File dirF = new File(f.getParent());
          dirF.mkdirs();

          if (ze.isDirectory()) {
            f.mkdirs();
          } else {
            byte[] buffer = new byte[1024];
            int length = 0;
            InputStream zis = zf.getInputStream(ze);
            FileOutputStream fos = new FileOutputStream(f);

            while ((length = zis.read(buffer)) >= 0) {
              fos.write(buffer, 0, length);
            }

            fos.close();
          }

        } catch (FileNotFoundException ex) {
          SilverTrace.warn("webSites", "Expand.expandFile()", "root.EX_FILE_NOT_FOUND",
              "file = " + f.getPath(), ex);
        }
      }
    } catch (IOException ioe) {
      SilverTrace.warn("webSites", "Expand.expandFile()", "webSites.EXE_ERROR_WHILE_EXPANDING_FILE",
          "sourceFile = " + srcF.getPath(), ioe);
    } finally {
      if (zf != null) {
        try {
          zf.close();
        } catch (IOException e) {
          SilverTrace.warn("webSites", "Expand.expandFile()",
              "webSites.EXE_ERROR_WHILE_CLOSING_ZIPINPUTSTREAM", null, e);
        }
      }
    }
  }

  private String validateFilename(String fileName, String intendedDir)
      throws java.io.IOException {
    File f = new File(fileName);
    String canonicalPath = f.getCanonicalPath();

    File iD = new File(intendedDir);
    String canonicalID = iD.getCanonicalPath();

    if (canonicalPath.startsWith(canonicalID)) {
      return canonicalPath;
    } else {
      throw new IllegalStateException(
          "File is outside extraction target directory (security): " + fileName);
    }
  }

  /**
   * Set the destination directory. File will be unzipped into the destination directory.
   * @param d Path to the directory.
   */
  public void setDest(File d) {
    this.dest = d;
  }

  /**
   * Set the path to zip-file.
   * @param s Path to zip-file.
   */
  public void setSrc(File s) {
    this.source = s;
  }
}
