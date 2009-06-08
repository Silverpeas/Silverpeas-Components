package com.silverpeas.wiki;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.silverpeas.util.FileUtil;
import com.silverpeas.versioning.VersioningInstanciator;
import com.silverpeas.wiki.control.WikiException;
import com.silverpeas.wiki.control.WikiPageDAO;
import com.silverpeas.wiki.control.model.PageDetail;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

public class WikiInstanciator implements ComponentsInstanciatorIntf {

  public WikiInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("wiki", "WikiInstanciator.create()",
        "root.MSG_GEN_PARAM_VALUE", "spaceId = " + spaceId
            + " , componentId = " + componentId);
    try {
      createPages(componentId);
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

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {

    SilverTrace.info("wiki", "WikiInstanciator.delete()",
        "root.MSG_GEN_PARAM_VALUE", "spaceId = " + spaceId
            + " , componentId = " + componentId);

    // delete wiki metadata from database
    WikiPageDAO wikiDAO = new WikiPageDAO();
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
      FileFolderManager.deleteFolder(FileRepositoryManager
          .getAbsolutePath(componentId));
    } catch (Exception e) {
      SilverTrace.info("wiki", "WikiInstanciator.delete()",
          "root.EX_RECORD_DELETE_FAILED", "componentId " + componentId, e);
    }
  }

  protected void createPages(String componentId) throws IOException,
      WikiException {
    WikiPageDAO wikiDAO = new WikiPageDAO();
    File directory = new File(FileRepositoryManager
        .getAbsolutePath(componentId));
    directory.mkdirs();
    ZipFile zipFile = new ZipFile(this.getClass().getClassLoader().getResource(
        "pages.zip").getPath());
    Enumeration<? extends ZipEntry> pages = zipFile.entries();
    while (pages.hasMoreElements()) {
      ZipEntry page = pages.nextElement();
      String pageName = page.getName();
      File newPage = new File(directory, pageName);
      if (page.isDirectory()) {
        newPage.mkdirs();
      } else {
        FileUtil.writeFile(newPage, new InputStreamReader(zipFile
            .getInputStream(page), "UTF-8"));
        PageDetail newPageDetail = new PageDetail();
        newPageDetail.setInstanceId(componentId);
        newPageDetail.setPageName(pageName.substring(0, pageName
            .lastIndexOf('.')));
        wikiDAO.createPage(newPageDetail);   
      }
    }
  }
}