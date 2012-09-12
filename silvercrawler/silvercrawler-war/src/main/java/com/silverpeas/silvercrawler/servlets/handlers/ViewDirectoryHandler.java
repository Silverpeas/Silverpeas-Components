package com.silverpeas.silvercrawler.servlets.handlers;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.silvercrawler.model.FileFolder;

/**
 * Handler for use case : View Directory.
 *
 * @author Ludovic Bertin
 *
 */
public class ViewDirectoryHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController, HttpServletRequest request)
      throws Exception {

    // Get current folder
    String userHighestRole = getUserHighestRole(sessionController);
    boolean isAdmin = (userHighestRole.equals("admin"));
    FileFolder currentFolder = sessionController.getCurrentFolder(isAdmin);

    // Special case: folder list is forbidden
    if (!currentFolder.isReadable()) {
      request.setAttribute("errorMessage", sessionController.getString("silverCrawler.notAllowedToReadFolderContent"));
    }

    // Reset UploadReport
    sessionController.resetLastUploadReport();

    // Store objects in request as attributes
    request.setAttribute("Folder", currentFolder);
    request.setAttribute("Path", sessionController.getPath());
    request.setAttribute("IsDownload", sessionController.isDownload());
    request.setAttribute("IsRootPath", new Boolean(sessionController.isRootPath()));
    request.setAttribute("IsAllowedNav", sessionController.isAllowedNav());
    request.setAttribute("RootPath", sessionController.getRootPath());
    request.setAttribute("isReadWriteActivated", sessionController.isReadWriteActivated());
    request.setAttribute("userAllowedToSetRWAccess", sessionController.checkRWSettingsAccess(false));
    request.setAttribute("userAllowedToLANAccess", sessionController.checkUserLANAccess(request.getRemoteAddr()));

    // tables settings (nb files/folders per page)
    request.setAttribute("MaxDirectories", sessionController.getNbMaxDirectoriesByPage());
    request.setAttribute("MaxFiles", sessionController.getNbMaxFilesByPage());

    // return page to redirect to
    return "viewDirectory.jsp";
  }

}
