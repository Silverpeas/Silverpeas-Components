/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.community.control;

import org.silverpeas.components.community.CommunityComponentSettings;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.Navigation;
import org.silverpeas.core.web.mvc.webcomponent.annotation.Homepage;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternalJsp;
import org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import static org.silverpeas.core.util.URLUtil.getApplicationURL;
import static org.silverpeas.core.web.mvc.util.WysiwygRouting.WysiwygRoutingContext.fromComponentSessionController;

/**
 * <p>
 * The Web Component Controller of the application.
 * </p>
 * <p>
 * It takes in charge, per user, the web navigation of the user in the application. It is a session
 * scoped bean; it is instantiated for each user session.
 * </p>
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@WebComponentController(CommunityComponentSettings.COMPONENT_NAME)
public class CommunityWebController extends
    org.silverpeas.core.web.mvc.webcomponent.WebComponentController<CommunityWebRequestContext> {
  private static final long serialVersionUID = -8606482122697353961L;

  /**
   * Standard Web Controller Constructor.
   * @param mainSessionCtrl the main user session controller.
   * @param componentContext The component's context.
   */
  public CommunityWebController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, CommunityComponentSettings.MESSAGES_PATH,
        CommunityComponentSettings.SETTINGS_PATH, CommunityComponentSettings.SETTINGS_PATH);
  }

  /**
   * This method is called one times once this web component controller is instantiated for a given
   * user.
   * You can perform here some specific treatments here. For example, you can register Web
   * navigation listeners that will be invoked at each navigation step change. For simple web
   * navigation, this method is usually empty.
   * @param context the web request context.
   */
  @Override
  protected void onInstantiation(final CommunityWebRequestContext context) {
  }

  @Override
  protected void beforeRequestProcessing(final CommunityWebRequestContext context) {
    super.beforeRequestProcessing(context);
    context.getRequest().setAttribute("isMember", context.isMember());
  }

  /**
   * Prepares the rendering of the home page.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("Main")
  @Homepage
  @RedirectToInternalJsp("main.jsp")
  public void home(CommunityWebRequestContext context) {
    final WysiwygContent content = context.getCommunity().getSpaceFacadeContent();
    context.getRequest().setAttribute("spaceFacadeContent", content.getRenderer().renderView());
  }

  @GET
  @Path("spaceHomePage/edit")
  public Navigation editSpaceHomePage(CommunityWebRequestContext context) {
    final WysiwygContent content = context.getCommunity().getSpaceFacadeContent();
    return context.redirectToHtmlEditor(fromComponentSessionController(this)
        .withBrowseInfo(getString("community.edition.spaceHomePage"))
        .withContributionId(content.getContribution().getIdentifier())
        .withIndexation(false)
        .withComeBackUrl(getApplicationURL() + context.getComponentUriBase() + "Main"));
  }
}