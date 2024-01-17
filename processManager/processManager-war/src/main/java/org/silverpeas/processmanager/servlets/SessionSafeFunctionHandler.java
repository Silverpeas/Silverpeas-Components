/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.processmanager.servlets;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.processmanager.ProcessManagerException;
import org.silverpeas.processmanager.ProcessManagerSessionController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A SessionSafeFunctionHandler must be used to prevent conflicts in HTTP Session when user
 * navigate
 * using several windows sharing the same session.
 * @author Ludovic Bertin
 */
public abstract class SessionSafeFunctionHandler implements FunctionHandler {

  private static final String PROCESS_MANAGERTOKEN_ID = "processManagertokenId";
  private static final String CANCEL_PARAMETER = "cancel";

  @Override
  public final String getDestination(String function, ProcessManagerSessionController session,
      HttpServletRequest req) throws ProcessManagerException {
    HttpRequest request = HttpRequest.decorate(req);
    List<FileItem> items = null;

    // Retrieves current token Id
    String currentTokenId = session.getCurrentTokenId();

    // Retrieves items in case of multipart request
    if (request.isContentInMultipart()) {
      try {
        items = new ArrayList<>();
        if (request.getAttribute("ALREADY_PROCESSED") == null) {
          items.addAll(request.getFileItems());
          request.setAttribute("ALREADY_PROCESSED", true);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
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

    String givenTokenId;
    boolean isCancellation;

    // multipart form submission.
    if ((request.isContentInMultipart()) && (items != null)) {
      String cancel = FileUploadUtil.getParameter(items, CANCEL_PARAMETER);
      isCancellation = StringUtil.getBooleanValue(cancel);

      // retrieve given token id
      givenTokenId = FileUploadUtil.getParameter(items, PROCESS_MANAGERTOKEN_ID);
    } else {
      // simple form submission
      // in case of creation cancellation, test must be bypassed and current token id reset
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
   * <p>
   * Checks for incorrect usages:
   * </p>
   * <dl>
   * <dt>CASE 1: no conflict detection</dt>
   * <dd><ul>
   *  <li>scenario:       user cancels a current action (from action's form)</li>
   *  <li>how to detect:  a parameter named "cancel" is present in request and equals to true</li>
   * </ul></dd>
   * <dt>CASE 2: conflict detection</dt>
   * <dd><ul>
   *  <li>scenario: an action is being processed, user open another window and try to access same
   * workflow</li>
   *  <li>how to detect: no token ID present in request, but there's a current token ID in session
   * controller</li>
   * </ul></dd>
   * <dt>CASE 3: no conflict detection</dt>
   * <dd><ul>
   *  <li>scenario: user submits action form correctly</li>
   *  <li>how to detect :  a token ID is present in request and equal to current token ID stored in
   * session controller</li>
   * </ul></dd>
   * <dt>CASE 4: conflict detection</dt>
   * <dd><ul>
   *   <li>scenario: an action is being processed, user open another window and logged in =>
   * previous Silverpeas session data is lost, then user open a instance procedure from same
   * workflow and at least try submit action form from the first window</li>
   *  <li>how to detect: a token ID is present in request but different from current token ID stored
   * in session controller</li>
   * </ul></dd>
   * <dt>CASE 5: conflict detection</dt>
   * <dd><ul>
   *   <li>scenario: an action is being processed, user open another window and logged in =>
   * previous Silverpeas session data is lost then try submit action form from the first window</li>
   *  <li>how to detect: a token ID is present in request but no current token ID stored in session
   * controller</li>
   * </ul></dd>
   * <dt>CASE 6: no conflict detection</dt>
   * <dd><ul>
   *   <li>scenario: user navigates in only one window, or in several windows but only in read-only
   * uses cases</li>
   *  <li>how to detect: no current token ID in session controller and no token ID present in
   *  request</li>
   * </ul></dd>
   * </dl>
   * @param currentTokenId the current token identifier
   * @param givenTokenId the given token identifier
   * @param isCancellation is a cancellation asked
   * @return a boolean indicating the status of the verification: true if the usage is correct,
   * false otherwise.
   */
  private boolean doVerifications(String currentTokenId, String givenTokenId,
      boolean isCancellation) {
    // CASE 1 : user cancels a current action (from action's form)
    if (isCancellation) {
      return true;
    } else if (StringUtil.isDefined(currentTokenId)) {

      // CASE 2 : an action is being processed, user open another window and try to access same
      // workflow
      if (!StringUtil.isDefined(givenTokenId)) {
        SilverLogger.getLogger(this)
            .error("Provided token empty! " +
                "CASE 2: an action is being processed, user open another window and try to access" +
                " same workflow");
        return false;
      } else {
        // CASE 3 : user submits action form correctly
        if (givenTokenId.equals(currentTokenId)) {
          return true;
        }

        // CASE 4 :
        // an action is being processed, user open another window and logged in => previous
        // Silverpeas session data is lost
        // then user open a instance procedure from same workflow
        // and at least try submit action form from the first window
        else {
          SilverLogger.getLogger(this)
              .error("Bad token " + givenTokenId +
                  "CASE 4: an action is being processed, user open another window and logged in");
          return false;
        }
      }
    } else {
      // CASE 5 : an action is being processed, user open another window and logged in =>
      // previous Silverpeas session data is lost then try submit action form from the first window
      if (StringUtil.isDefined(givenTokenId)) {
        SilverLogger.getLogger(this)
            .error("No current token" +
                "CASE 5: an action is being processed, user open another window and logged in");
        return false;
      }
      // CASE 6 : user navigates in only one window, or in several windows but only in read-only
      // uses cases
      else {
        return true;
      }
    }
  }

  /**
   * Generate random UUID and store it in request as attribute and in process manager session
   * controller
   * @param session the process manager session controller
   * @param request the http servlet request
   */
  protected void generateTokenId(ProcessManagerSessionController session,
      HttpServletRequest request) {
    // Generates and store new token id if needed
    UUID newTokenId = UUID.randomUUID();
    session.setCurrentTokenId(newTokenId.toString());
    request.setAttribute("currentTokenId", newTokenId.toString());
  }

  /**
   * Reset token Id.
   * @param session the process manager session controller
   * @param request the http servlet request
   */
  protected void resetTokenId(ProcessManagerSessionController session, HttpServletRequest request) {
    session.setCurrentTokenId(null);
    request.removeAttribute("currentTokenId");
  }

  /**
   * Main scenario to be implemented by handler.
   * @param function the name of use case to realize
   * @param session the process manager session controller
   * @param request the http servlet request
   * @param items eventual submitted items
   * @return the JSP servlet to be forwarded to.
   * @throws ProcessManagerException if an error occurs while computing the next destination of the
   * user navigation in the workflow.
   */
  protected abstract String computeDestination(String function,
      ProcessManagerSessionController session, HttpServletRequest request, List<FileItem> items)
      throws ProcessManagerException;

}
