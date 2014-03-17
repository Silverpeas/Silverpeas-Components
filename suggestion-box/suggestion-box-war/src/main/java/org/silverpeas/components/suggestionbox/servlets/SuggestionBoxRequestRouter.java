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
import com.stratelia.silverpeas.peasCore.servlets.Navigation;
import com.stratelia.silverpeas.peasCore.servlets.WebComponentRequestRouter;
import com.stratelia.silverpeas.peasCore.servlets.annotation.Homepage;
import com.stratelia.silverpeas.peasCore.servlets.annotation.LowestRoleAccess;
import com.stratelia.webactiv.SilverpeasRole;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.components.suggestionbox.control.SuggestionBoxSessionController;
import org.silverpeas.wysiwyg.control.WysiwygController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SuggestionBoxRequestRouter
    extends WebComponentRequestRouter<SuggestionBoxSessionController, SuggestionBoxWebContext> {
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

  @Override
  protected void commonContextInitialization(final SuggestionBoxWebContext context) {
    super.commonContextInitialization(context);
    context.getRequest().setAttribute("suggestionBox", context.getSuggestionBox());
  }

  /**
   * Perform homepage
   * @param context
   * @return destination
   */
  @GET
  @Homepage
  public Navigation home(SuggestionBoxWebContext context) {

    // Edito
    setIsEdito(context);

    // Destination
    return context.navigateToInternalJsp("suggestionBox.jsp");
  }

  @GET
  @Path("edito/modify")
  @LowestRoleAccess(SilverpeasRole.admin)
  public Navigation modifyEdito(SuggestionBoxWebContext context)
      throws UnsupportedEncodingException {
    context.getRequest().setAttribute("SpaceId", context.getSpaceId());
    context.getRequest()
        .setAttribute("SpaceName", URLEncoder.encode(context.getSpaceLabel(), CharEncoding.UTF_8));
    context.getRequest().setAttribute("ComponentId", context.getComponentInstanceId());
    context.getRequest().setAttribute("ComponentName",
        URLEncoder.encode(context.getComponentInstanceLabel(), CharEncoding.UTF_8));
    context.getRequest().setAttribute("ObjectId", context.getSuggestionBox().getId());
    context.getRequest().setAttribute("Language", null);
    context.getRequest().setAttribute("ReturnUrl", URLManager.getApplicationURL() +
        URLManager.getURL("suggestionBox", "useless", context.getComponentInstanceId()) +
        "fromWysiwyg");
    context.getRequest().setAttribute("UserId", context.getUser().getId());
    context.getRequest().setAttribute("IndexIt", "false");
    // Destination
    return context.navigateTo("/wysiwyg/jsp/htmlEditor.jsp");
  }

  /**
   * Sets into request attributes the isEdito constants
   * @param context
   */
  private void setIsEdito(SuggestionBoxWebContext context) {
    if (WysiwygController
        .haveGotWysiwyg(context.getComponentInstanceId(), context.getSuggestionBox().getId(),
            null)) {
      context.getRequest().setAttribute("isEdito", true);
    }
  }
}
