package com.silverpeas.silvercrawler.servlets.handlers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.silvercrawler.control.SilverCrawlerSessionController;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;

/**
 * Handler for use case : upload file form has been submitted.
 * @author Ludovic Bertin
 */
public class UploadFileHandler extends FunctionHandler {

  @Override
  public String getDestination(SilverCrawlerSessionController sessionController,
      HttpServletRequest request)
      throws Exception {

    // Is User has admin or publisher profile
    String userHisghestRole = getUserHighestRole(sessionController);
    boolean isAdminOrPublisher =
        (userHisghestRole.equals("admin") || userHisghestRole.equals("publisher"));

    if (!isAdminOrPublisher) {
      request.setAttribute("errorMessage", "User has not admin/publisher rights");
      return "operationFailed.jsp";
    }

    // Retrieve File and policy for existing files
    List<FileItem> parameters = FileUploadUtil.parseRequest(request);
    FileItem file = FileUploadUtil.getFile(parameters, "newFile");
    boolean replaceFile = StringUtil.getBooleanValue( FileUploadUtil.getParameter(parameters, "replaceExistingFile") );

    // Save File
    try {
      sessionController.saveFile(file, replaceFile);
    } catch (Exception e) {
      request.setAttribute("errorMessage", e.getMessage());
    }

    // returns page to redirect to
    return HandlerProvider.getHandler("ViewDirectory").getDestination(sessionController, request);
  }

}
