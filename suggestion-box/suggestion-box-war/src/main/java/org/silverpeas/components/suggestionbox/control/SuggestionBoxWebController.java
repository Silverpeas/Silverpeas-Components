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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.control;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.Navigation;
import com.stratelia.silverpeas.peasCore.servlets.annotation.Homepage;
import com.stratelia.silverpeas.peasCore.servlets.annotation.Invokable;
import com.stratelia.silverpeas.peasCore.servlets.annotation.InvokeAfter;
import com.stratelia.silverpeas.peasCore.servlets.annotation.LowestRoleAccess;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectToInternal;
import com.stratelia.silverpeas.peasCore.servlets.annotation.RedirectToInternalJsp;
import com.stratelia.silverpeas.peasCore.servlets.annotation.WebComponentController;
import com.stratelia.webactiv.SilverpeasRole;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.web.SuggestionEntity;
import org.silverpeas.wysiwyg.control.WysiwygController;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

@WebComponentController("SuggestionBox")
public class SuggestionBoxWebController extends
    com.stratelia.silverpeas.peasCore.servlets.WebComponentController<SuggestionBoxWebRequestContext> {

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
   * Prepares the rendering of the home page.
   * @param context the context of the incoming request.
   */
  @GET
  @Homepage
  @RedirectToInternalJsp("suggestionBox.jsp")
  @InvokeAfter("isEdito")
  public void home(SuggestionBoxWebRequestContext context) {
    // Nothing to do for now...
  }

  /**
   * Asks for the modification of the edito of the current suggestion box. The modification itself
   * is redirected to the WYSIWYG editor.
   * @param context the context of the incoming request.
   * @return the navigation information within which the next resource to which the control will be
   * passed is indicated.
   */
  @GET
  @Path("edito/modify")
  @LowestRoleAccess(SilverpeasRole.admin)
  public Navigation modifyEdito(SuggestionBoxWebRequestContext context) {
    return context.redirectToHtmlEditor(context.getSuggestionBox().getId(), "fromWysiwyg", false);
  }

  /**
   * Sets into request attributes the isEdito constant.
   * @param context the context of the incoming request.
   */
  @Invokable("isEdito")
  public void setIsEditoIntoRequest(SuggestionBoxWebRequestContext context) {
    if (WysiwygController
        .haveGotWysiwyg(context.getComponentInstanceId(), context.getSuggestionBox().getId(),
            null)) {
      context.getRequest().setAttribute("isEdito", true);
    }
  }

  /**
   * Asks for purposing a new suggestion. It renders an HTML page to input the content of a new
   * suggestion.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("suggestion/{id}")
  @RedirectToInternalJsp("suggestion.jsp")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void editSuggestion(SuggestionBoxWebRequestContext context) {
    String suggestionId = context.getPathVariables().get("id");
    if (!"new".equalsIgnoreCase(suggestionId)) {
      SuggestionBox suggestionBox = context.getSuggestionBox();
      Suggestion suggestion = suggestionBox.getSuggestions().get(suggestionId);
      if (suggestion.isDefined()) {
        SuggestionEntity entity = SuggestionEntity.fromSuggestion(suggestion);
        context.getRequest().setAttribute("suggestion", entity);
      } else {
        throw new WebApplicationException(Status.NOT_FOUND);
      }
    }
  }

  /**
   * Adds a new suggestion into the current suggestion box. The suggestion's data are
   * carried within the request's context.
   * @param context the context of the incoming request.
   */
  @POST
  @Path("suggestion/add")
  @RedirectToInternal("Main")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void addSuggestion(SuggestionBoxWebRequestContext context) {
    SuggestionBox suggestionBox = context.getSuggestionBox();
    String title = context.getRequest().getParameter("title");
    String content = context.getRequest().getParameter("content");
    Suggestion suggestion = new Suggestion(title);
    suggestion.setContent(content);
    suggestion.setCreator(context.getUser());
    suggestionBox.getSuggestions().add(suggestion);
    context.getMessager().addSuccess(getMultilang().getString(
        "suggestionBox.message.suggestion.created"));
  }

  /**
   * Updates the specified suggestion in the current suggestion box. The suggestion's data are
   * carried within the request's context.
   * @param context the context of the incoming request.
   */
  @PUT
  @Path("suggestion/${id}")
  @RedirectToInternal("Main")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void updateSuggestion(SuggestionBoxWebRequestContext context) {
    String id = context.getPathVariables().get("id");
    SuggestionBox suggestionBox = context.getSuggestionBox();
    Suggestion suggestion = suggestionBox.getSuggestions().get(id);
    if (suggestion.isDefined()) {
      suggestion.setTitle(context.getRequest().getParameter("title"));
      suggestion.setContent(context.getRequest().getParameter("content"));
      suggestion.setLastUpdater(context.getUser());
      suggestion.save();
      context.getMessager().addSuccess(getMultilang().getString(
          "suggestionBox.message.suggestion.modified"));
    } else {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }
}
