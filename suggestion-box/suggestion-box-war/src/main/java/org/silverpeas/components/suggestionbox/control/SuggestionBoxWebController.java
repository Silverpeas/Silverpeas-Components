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
package org.silverpeas.components.suggestionbox.control;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.Navigation;
import com.stratelia.silverpeas.peasCore.servlets.annotation.Homepage;
import com.stratelia.silverpeas.peasCore.servlets.annotation.Invokable;
import com.stratelia.silverpeas.peasCore.servlets.annotation.InvokeAfter;
import com.stratelia.silverpeas.peasCore.servlets.annotation.LowestRoleAccess;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectToInternalJsp;
import com.stratelia.silverpeas.peasCore.servlets.annotation.WebComponentController;
import com.stratelia.webactiv.SilverpeasRole;
import org.silverpeas.wysiwyg.control.WysiwygController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@WebComponentController("SuggestionBox")
public class SuggestionBoxWebController extends
    com.stratelia.silverpeas.peasCore.servlets
        .WebComponentController<SuggestionBoxWebRequestContext> {

  /**
   * Standard Session Controller Constructor
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public SuggestionBoxWebController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.components.suggestionbox.multilang.SuggestionBoxBundle",
        "org.silverpeas.components.suggestionbox.settings.SuggestionBoxIcons",
        "org.silverpeas.components.suggestionbox.settings.SuggestionBoxSettings");
  }

  @Override
  protected void commonRequestContextInitialization(final SuggestionBoxWebRequestContext context) {
    super.commonRequestContextInitialization(context);
    context.getRequest().setAttribute("suggestionBox", context.getSuggestionBox());
  }

  /**
   * Perform homepage
   * @param context
   * @return destination
   */
  @GET
  @Homepage
  @RedirectToInternalJsp("suggestionBox.jsp")
  @InvokeAfter("isEdito")
  public void home(SuggestionBoxWebRequestContext context) {
    // Nothing to do for now...
  }

  @GET
  @Path("edito/modify")
  @LowestRoleAccess(SilverpeasRole.admin)
  public Navigation modifyEdito(SuggestionBoxWebRequestContext context) {
    return context.redirectToHtmlEditor(context.getSuggestionBox().getId(), "fromWysiwyg", false);
  }

  /**
   * Sets into request attributes the isEdito constant.
   * @param context
   */
  @Invokable("isEdito")
  public void setIsEditoIntoRequest(SuggestionBoxWebRequestContext context) {
    if (WysiwygController
        .haveGotWysiwyg(context.getComponentInstanceId(), context.getSuggestionBox().getId(),
            null)) {
      context.getRequest().setAttribute("isEdito", true);
    }
  }
}