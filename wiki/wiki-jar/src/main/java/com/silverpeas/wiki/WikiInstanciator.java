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
package com.silverpeas.wiki;

import com.silverpeas.util.ConfigurationClassLoader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.zip.ZipEntry;
import org.apache.commons.io.FileUtils;

import com.silverpeas.util.FileUtil;
import com.silverpeas.versioning.VersioningInstanciator;
import com.silverpeas.wiki.control.WikiException;
import com.silverpeas.wiki.control.WikiPageDAO;
import com.silverpeas.wiki.control.model.PageDetail;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.FileRepositoryManager;
import java.util.zip.ZipInputStream;

public class WikiInstanciator implements ComponentsInstanciatorIntf {

  private WikiPageDAO wikiDAO;
  private ClassLoader loader;

  public WikiInstanciator() {
    wikiDAO = new WikiPageDAO();
    loader = new ConfigurationClassLoader(WikiInstanciator.class.getClassLoader());
  }

  public WikiInstanciator(WikiPageDAO dao) {
    wikiDAO = dao;
  }

  @Override
  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("wiki", "WikiInstanciator.create()",
        "root.MSG_GEN_PARAM_VALUE", "spaceId = " + spaceId
        + " , componentId = " + componentId);
    try {
      File directory = getComponentDirectory(componentId);
      directory.mkdirs();
      createPages(directory, componentId);
    } catch (WikiException e) {
      SilverTrace.error("wiki", "WikiInstanciator.create()",
          "root.EX_RECORD_INSERT_FAILED", "componentId " + componentId, e);
      throw new InstanciationException("root.EX_RECORD_INSERT_FAILED");
    } catch (IOException e) {
      SilverTrace.error("wiki", "WikiInstanciator.create()",
          "root.EX_RECORD_INSERT_FAILED", "componentId " + componentId, e);
      throw new InstanciationException("root.EX_RECORD_INSERT_FAILED");
    }
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("wiki", "WikiInstanciator.delete()",
        "root.MSG_GEN_PARAM_VALUE", "spaceId = " + spaceId
        + " , componentId = " + componentId);
    // delete wiki metadata from database
    try {
      wikiDAO.deleteAllPages(componentId);
    } catch (Exception e) {
      SilverTrace.info("wiki", "WikiInstanciator.delete()",
          "root.EX_RECORD_DELETE_FAILED", "componentId " + componentId, e);
    }
    // delete versioning infos
    VersioningInstanciator version = new VersioningInstanciator();
    version.delete(con, spaceId, componentId, userId);
    // delete files from filesystem
    try {
      FileUtils.forceDelete(getComponentDirectory(componentId));
    } catch (Exception e) {
      SilverTrace.info("wiki", "WikiInstanciator.delete()",
          "root.EX_RECORD_DELETE_FAILED", "componentId " + componentId, e);
    }
  }

  protected File getComponentDirectory(String componentId) {
    return new File(FileRepositoryManager.getAbsolutePath(componentId));
  }

  protected void createPages(File directory, String componentId) throws IOException,
      WikiException {

    ZipInputStream zipFile = new ZipInputStream(loader.getResourceAsStream("pages.zip"));
    ZipEntry page = zipFile.getNextEntry();
    while (page != null) {
      String pageName = page.getName();
      File newPage = new File(directory, pageName);
      if (page.isDirectory()) {
        newPage.mkdirs();
      } else {
        FileUtil.writeFile(newPage, new InputStreamReader(zipFile, "UTF-8"));
        PageDetail newPageDetail = new PageDetail();
        newPageDetail.setInstanceId(componentId);
        newPageDetail.setPageName(pageName.substring(0, pageName.lastIndexOf('.')));
        wikiDAO.createPage(newPageDetail);
        zipFile.closeEntry();
        page = zipFile.getNextEntry();
      }
    }
  }
}
