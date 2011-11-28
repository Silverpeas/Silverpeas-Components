package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.FolderZIPInfo;
import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;

/**
 * Handler for use case : user wants to download folder.
 *
 * @author Ludovic Bertin
 *
 */
public class DownloadFolderHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {
    // Get requested folder name
    String folderName = (String) request.getParameter("FolderName");

    // Generate ZIP file
    FolderZIPInfo zipInfo = sessionController.zipFolder(folderName);

    // Store objects in request as attributes
    request.setAttribute("Name", zipInfo.getFileZip());
    request.setAttribute("ZipURL", zipInfo.getUrl());
    request.setAttribute("Size", zipInfo.getSize());
    request.setAttribute("SizeMax", zipInfo.getMaxiSize());

    // return page to redirect to
    return "download.jsp";
  }

}
