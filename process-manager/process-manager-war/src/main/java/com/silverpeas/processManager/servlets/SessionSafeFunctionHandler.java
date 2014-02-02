package com.silverpeas.processManager.servlets;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.processManager.ProcessManagerException;
import com.silverpeas.processManager.ProcessManagerSessionController;
import com.silverpeas.util.StringUtil;
import org.silverpeas.servlet.FileUploadUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.UtilException;
import org.silverpeas.servlet.HttpRequest;

/**
 * A SessionSafeFunctionHandler must be used to prevent conflicts in HTTP Session when user navigate using several windows sharing the same session.
 *
 * @author Ludovic Bertin
 *
 */
public abstract class SessionSafeFunctionHandler implements FunctionHandler {

  private static final String PROCESS_MANAGERTOKEN_ID = "processManagertokenId";
  private static final String CANCEL_PARAMETER = "cancel";

  @Override
  final public String getDestination(String function, ProcessManagerSessionController session,
      HttpServletRequest req) throws ProcessManagerException {
    HttpRequest request = HttpRequest.decorate(req);
    List<FileItem> items = null;

    // Retrieves current token Id
    String currentTokenId = session.getCurrentTokenId();

    // Retrieves items in case of multipart request
    if (request.isContentInMultipart()) {
      try {
        items = new ArrayList<FileItem>();
        if (request.getAttribute("ALREADY_PROCESSED") == null) {
          items.addAll(request.getFileItems());
          request.setAttribute("ALREADY_PROCESSED", true);
        }
      } catch (UtilException e) {
        SilverTrace.error("processManager", "SessionSafeFunctionHandler.getDestination()",
            "processManager.TOKENID_CHECK_FAILURE", e);
      }
    }

    // if multiple windows within the same session, user is redirected to an explicit error page
    if (!checkTokenId(session, currentTokenId, request, items)) {
      return "/processManager/jsp/multiWindowDetected.jsp";
    }

    // No weird usage detected, let's the handler do its stuff.
    return computeDestination(function, session, request, items);
  }

  /**
   * Checks if a parameter named "processManagertokenId" is present in request and equal to given
   * tokenId.
   * @param session ProcessManager Session Controller
   * @param currentTokenId the current token Id to compare to parameter's value
   * @param request the HTTP request
   * @param items FileItems list to be completed if request is multipart
   * @return false is parameter is not present or different from given token id
   */
  private boolean checkTokenId(ProcessManagerSessionController session, String currentTokenId,
      HttpRequest request, List<FileItem> items) {

    String givenTokenId = null;
    boolean isCancellation = false;

    // multipart form submission.
    if ( (request.isContentInMultipart()) && (items != null) ) {
      String cancel = FileUploadUtil.getParameter(items, CANCEL_PARAMETER);
      isCancellation = StringUtil.getBooleanValue(cancel);

      // retrieve given token id
      givenTokenId = FileUploadUtil.getParameter(items, PROCESS_MANAGERTOKEN_ID);
    }

    // simple form submission
    else {
      // in case of creation cancellation, test must be bypassed and current tolen id resetted
      String cancel = request.getParameter(CANCEL_PARAMETER);
      isCancellation = StringUtil.getBooleanValue(cancel);

      // compare given token id with current token id
      givenTokenId = request.getParameter(PROCESS_MANAGERTOKEN_ID);
    }

    if (isCancellation) {
      resetTokenId(session, request);
    }

    return doVerifications(currentTokenId, givenTokenId, isCancellation);
  }

  /**
   * Checks for incorrect usages :
   *
   * CASE 1 :         no conflict detection
   * scenario :       user cancels a current action (from action's form)
   * how to detect :  a parameter named "cancel" is present in request and equals to true
   *
   * CASE 2 :         conflict detection
   * scenario :       an action is being processed, user open another window and try to access same workflow
   * how to detect :  no token ID present in request, but there's a current token ID in session controller
   *
   * CASE 3 :         no conflict detection
   * scenario :       user submits action form correctly
   * how to detect :  a token ID is present in request and equal to current token ID stored in session controller
   *
   * CASE 4 :         conflict detection
   * scenario :       an action is being processed, user open another window and logged in => previous Silverpeas session data is lost, then user open a instance procedure from same workflow and at least try submit action form from the first window
   * how to detect :  a token ID is present in request but different from current token ID stored in session controller
   *
   * CASE 5 :         conflict detection
   * scenario :       an action is being processed, user open another window and logged in => previous Silverpeas session data is lost then try submit action form from the first window
   * how to detect :  a token ID is present in request but no current token ID stored in session controller
   *
   * CASE 6 :         no conflict detection
   * scenario :       user navigates in only one window, or in several windows but only in read-only uses cases
   * how to detect :  no current token ID in session controller and no token ID present in request
   *
   * @param currentTokenId
   * @param isCancellation
   * @return
   */
  private boolean doVerifications(String currentTokenId, String givenTokenId, boolean isCancellation) {

    // CASE 1 : user cancels a current action (from action's form)
    if (isCancellation) {
      return true;
    }

    else if ( StringUtil.isDefined(currentTokenId) ) {

      // CASE 2 : an action is being processed, user open another window and try to access same workflow
      if (!StringUtil.isDefined(givenTokenId)) {
        SilverTrace.error("processManager", "SessionSafeFunctionHandler.doVerifications", "processManager.BAD_TOKEN", "CASE 2 : an action is being processed, user open another window and try to access same workflow");
        return false;
      }

      else {
        // CASE 3 : user submits action form correctly
        if (givenTokenId.equals(currentTokenId)) {
          return true;
        }

        // CASE 4 :
        // an action is being processed, user open another window and logged in => previous Silverpeas session data is lost
        // then user open a instance procedure from same workflow
        // and at least try submit action form from the first window
        else {
          SilverTrace.error("processManager", "SessionSafeFunctionHandler.doVerifications", "processManager.BAD_TOKEN", "CASE 4 : an action is being processed, user open another window and logged in");
          return false;
        }
      }
    }

    else {
      // CASE 5 : an action is being processed, user open another window and logged in => previous Silverpeas session data is lost then try submit action form from the first window
      if (StringUtil.isDefined(givenTokenId)) {
        SilverTrace.error("processManager", "SessionSafeFunctionHandler.doVerifications", "processManager.BAD_TOKEN", "CASE 5 : an action is being processed, user open another window and logged in");
        return false;
      }

      // CASE 6 : user navigates in only one window, or in several windows but only in read-only uses cases
      else {
        return true;
      }
    }
}

  /**
   * Generate random UUID and store it in request as attribute and in process manager session controller
   *
   * @param session     the process manager session controller
   * @param request     the http servlet request
   */
  protected void generateTokenId(ProcessManagerSessionController session, HttpServletRequest request) {
    // Generates and store new token id if needed
    UUID newTokenId = UUID.randomUUID();
    session.setCurrentTokenId(newTokenId.toString());
    request.setAttribute("currentTokenId", newTokenId.toString());
  }

  /**
   * Reset token Id.
   *
   * @param session     the process manager session controller
   * @param request     the http servlet request
   */
  protected void resetTokenId(ProcessManagerSessionController session, HttpServletRequest request) {
    session.setCurrentTokenId(null);
    request.removeAttribute("currentTokenId");
  }

  /**
   * Main scenario to be implemented by handler.
   *
   * @param function    the name of use case to realize
   * @param session     the process manager session controller
   * @param request     the http servlet request
   * @param items       eventual submitted items
   *
   * @return  the JSP servlet to be forwarded to.
   *
   * @throws ProcessManagerException
   */
  abstract protected String computeDestination(String function,
      ProcessManagerSessionController session,
      HttpServletRequest request, List<FileItem> items) throws ProcessManagerException;

}
