/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.servlets;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.components.suggestionbox.control.SuggestionBoxSessionController;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.servlet.HttpRequest;
import org.silverpeas.wysiwyg.control.WysiwygController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SuggestionBoxRequestRouter
    extends ComponentRequestRouter<SuggestionBoxSessionController> {
  private static final long serialVersionUID = -7378638602035981580L;

  /**
   * This method has to be implemented in the component request rooter class.
   * returns the session control bean name to be put in the request object
   * ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "SuggestionBox";
  }

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public SuggestionBoxSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new SuggestionBoxSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter
   * it has to compute a destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param suggestionBoxSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex : "/almanach/jsp/almanach
   * .jsp?flag=user")
   */
  @Override
  public String getDestination(String function, SuggestionBoxSessionController suggestionBoxSC,
      HttpRequest request) {
    String destination;
    SilverTrace.info("suggestion-box", "SuggestionBoxRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE",
        "User=" + suggestionBoxSC.getUserId() + " Function=" + function);

    // Greater user role
    SilverpeasRole greaterUserRole =
        SilverpeasRole.getGreaterFrom(SilverpeasRole.from(suggestionBoxSC.getUserRoles()));

    // Suggestion box is reloaded at each request to ensure that the entity is always the last
    // persisted one.
    SuggestionBox suggestionBox =
        SuggestionBox.getByComponentInstanceId(suggestionBoxSC.getComponentId());
    request.setAttribute("suggestionBox", suggestionBox);
    request.setAttribute("greaterUserRole", greaterUserRole);

    try {
      // External destinations
      if (function.equals("edito/modify") &&
          greaterUserRole.isGreaterThanOrEquals(SilverpeasRole.admin)) {
        return modifyEdito(request, suggestionBoxSC, suggestionBox);
      }

      // Internal JSP destinations
      if (function.equals("Main")) {
        destination = home(request, suggestionBoxSC, suggestionBox);
      } else {
        destination = home(request, suggestionBoxSC, suggestionBox);
      }
      destination = "/suggestion-box/jsp/" + destination;
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("suggestion-box", "SuggestionBoxRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  /**
   * Perform homepage
   * @param request
   * @param componentSC
   * @param suggestionBox
   * @return destination
   */
  private String home(HttpServletRequest request, SuggestionBoxSessionController componentSC,
      final SuggestionBox suggestionBox) {

    // Edito
    if (WysiwygController
        .haveGotWysiwyg(suggestionBox.getComponentInstanceId(), suggestionBox.getId(), null)) {
      request.setAttribute("isEdito", true);
    }

    // Destination
    return "suggestionBox.jsp";
  }

  /**
   * Handles the navigation to the edition of the edito.
   * @param request
   * @param suggestionBoxSC
   * @param suggestionBox
   * @return
   * @throws UnsupportedEncodingException
   */
  private String modifyEdito(HttpServletRequest request,
      SuggestionBoxSessionController suggestionBoxSC, SuggestionBox suggestionBox)
      throws UnsupportedEncodingException {
    request.setAttribute("SpaceId", suggestionBoxSC.getSpaceId());
    request.setAttribute("SpaceName",
        URLEncoder.encode(suggestionBoxSC.getSpaceLabel(), CharEncoding.UTF_8));
    request.setAttribute("ComponentId", suggestionBoxSC.getComponentId());
    request.setAttribute("ComponentName",
        URLEncoder.encode(suggestionBoxSC.getComponentLabel(), CharEncoding.UTF_8));
    request.setAttribute("ObjectId", suggestionBox.getId());
    request.setAttribute("Language", null);
    request.setAttribute("ReturnUrl", URLManager.getApplicationURL() +
        URLManager.getURL("suggestion-box", "useless", suggestionBoxSC.getComponentId()) +
        "fromWysiwyg");
    request.setAttribute("UserId", suggestionBoxSC.getUserId());
    request.setAttribute("IndexIt", "false");
    // Destination
    return "/wysiwyg/jsp/htmlEditor.jsp";
  }
}
