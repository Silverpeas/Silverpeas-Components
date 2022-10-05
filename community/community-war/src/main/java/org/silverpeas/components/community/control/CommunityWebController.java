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

import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.div;
import org.silverpeas.components.community.CommunityComponentSettings;
import org.silverpeas.components.community.CommunityWebManager;
import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceHomePageType;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.html.WebPlugin;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.look.proxy.SpaceHomepageProxy;
import org.silverpeas.core.web.look.proxy.SpaceHomepageProxyManager;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.AbstractNavigationContextListener;
import org.silverpeas.core.web.mvc.webcomponent.Navigation;
import org.silverpeas.core.web.mvc.webcomponent.NavigationContext;
import org.silverpeas.core.web.mvc.webcomponent.annotation.Homepage;
import org.silverpeas.core.web.mvc.webcomponent.annotation.LowestRoleAccess;
import org.silverpeas.core.web.mvc.webcomponent.annotation.NavigationStep;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectTo;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternal;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternalJsp;
import org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static org.silverpeas.components.community.CommunityWebManager.NO_PAGINATION;
import static org.silverpeas.components.community.web.html.JavascriptPluginInclusion.COMMUNITY_SUBSCRIPTION;
import static org.silverpeas.core.util.URLUtil.getApplicationURL;
import static org.silverpeas.core.util.WebEncodeHelper.javaStringToJsString;
import static org.silverpeas.core.web.look.LookHelper.getLookHelper;
import static org.silverpeas.core.web.mvc.util.WysiwygRouting.WysiwygRoutingContext.fromComponentSessionController;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.scriptContent;

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

  // Some navigation step identifier definitions
  private static final String MEMBER_LIST_NS_ID = "memberListNavStepIdentifier";
  private static final String HISTORY_VIEW_NS_ID = "historyViewNavStepIdentifier";

  // Some identifier definitions
  public static final String MEMBER_LIST_ARRAYPANE_IDENTIFIER = "communityMemberListIdentifier";
  public static final String HISTORY_ARRAYPANE_IDENTIFIER = "communityHistoryViewIdentifier";

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
    context.getNavigationContext()
        .addListener(new AbstractNavigationContextListener<CommunityWebRequestContext>() {
          @Override
          public void navigationContextCleared(
              final NavigationContext<CommunityWebRequestContext> navigationContext) {
            super.navigationContextCleared(navigationContext);
            clearArrayStates(navigationContext.getWebComponentRequestContext());
          }

          @Override
          public void navigationStepCreated(
              final NavigationContext<CommunityWebRequestContext> navigationContext) {
            super.navigationStepCreated(navigationContext);
            final var navigationStep = navigationContext.getCurrentNavigationStep();
            final LocalizationBundle multilang = context.getMultilang();
            if (MEMBER_LIST_NS_ID.equals(navigationStep.getIdentifier())) {
              navigationStep.withLabel(multilang.getString("community.browsebar.item.members"));
            } else if (HISTORY_VIEW_NS_ID.equals(navigationStep.getIdentifier())) {
              navigationStep.withLabel(multilang.getString("community.browsebar.item.history"));
            }
          }

          /**
           * Clears the state of arrays.
           */
          private void clearArrayStates(CommunityWebRequestContext context) {
            setSessionAttribute(context, MEMBER_LIST_ARRAYPANE_IDENTIFIER, null);
            setSessionAttribute(context, HISTORY_ARRAYPANE_IDENTIFIER, null);
          }
        });
  }

  @Override
  protected void beforeRequestProcessing(final CommunityWebRequestContext context) {
    super.beforeRequestProcessing(context);
    final HttpRequest request = context.getRequest();
    request.setAttribute("communityOfUsers", context.getCommunity());
    request.setAttribute("adminMustValidateNewMember", context.adminMustValidateNewMember());
    request.setAttribute("isSpaceHomepage", context.isSpaceHomePage());
    request.setAttribute("isMember", context.isMember());
    request.setAttribute("isMembershipPending", context.isMembershipPending());
  }

  /**
   * Prepares the rendering of the home page.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("Main")
  @Homepage
  @RedirectToInternal("{view}?ComponentMainPage={isComponentMainPage}")
  public void home(CommunityWebRequestContext context) {
    if (context.isMember() && context.isSpaceHomePage()) {
      context.addRedirectVariable("view", "spaceHomepageProxy");
      context.addRedirectVariable("isComponentMainPage", Boolean.FALSE.toString());
    } else {
      context.addRedirectVariable("view", "appHomepage");
      context.addRedirectVariable("isComponentMainPage", Boolean.TRUE.toString());
    }
  }

  /**
   * Prepares the rendering of the home page.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("appHomepage")
  @RedirectToInternalJsp("main.jsp")
  public void appHomepage(CommunityWebRequestContext context) {
    final HttpRequest request = context.getRequest();
    final CommunityWebManager manager = CommunityWebManager.get();
    if (context.canValidateNewMember()) {
      final var membersToValidate =
          manager.getMembersToValidate(context.getCommunity(), NO_PAGINATION);
      request.setAttribute("membersToValidate", membersToValidate);
    }
    final WysiwygContent content = context.getCommunity().getSpacePresentationContent();
    request.setAttribute("spacePresentationContent", content.getRenderer().renderView());
    request.setAttribute("displayNbMembersForNonMembers", context.displayNbMembersForNonMembers());
    request.setAttribute("displayCharterOnSpaceHomepage", context.displayCharterOnSpaceHomepage());
  }

  @POST
  @Path("parameters/displayCharterOnSpaceHomepage")
  @Produces(MediaType.APPLICATION_JSON)
  public void setDisplayCharterOnSpaceHomepage(CommunityWebRequestContext context) {
    CommunityWebManager.get()
        .setDisplayCharterOnSpaceHomepage(context.getCommunity(),
            context.getRequest().getParameterAsBoolean("value"));
  }

  /**
   * Prepares the rendering of the home page.
   * @param context the context of the incoming request.
   */
  @GET
  @Path("spaceHomepageProxy")
  @RedirectTo("{path}?SpaceId={spaceId}")
  public void spaceHomepageProxy(CommunityWebRequestContext context) {
    final CommunityOfUsers community = context.getCommunity();
    final SpaceInst space = OrganizationController.get().getSpaceInstById(community.getSpaceId());
    final SpaceHomepageProxy proxy = SpaceHomepageProxyManager.get().getProxyOf(space);
    final Pair<String, SpaceHomePageType> homePage = community.getHomePage();
    proxy.setFirstPageType(homePage.getSecond().ordinal());
    proxy.setFirstPageExtraParam(homePage.getFirst());
    final ElementContainer xhtml = new ElementContainer();
    xhtml.addElement(WebPlugin.get().getHtml(COMMUNITY_SUBSCRIPTION, getLanguage()));
    final div communityHtml = new div();
    communityHtml.setID("community-membership");
    communityHtml.addElement(String.format("<silverpeas-community-membership \n" +
            "        v-bind:display-nb-members-for-non-members='%s' \n" +
            "        v-bind:display-charter-on-space-homepage='%s' \n" +
            "        v-on:membership-join='reloadPage' \n" +
            "        v-on:membership-leave='reloadPage'></silverpeas-community-membership>",
        context.displayNbMembersForNonMembers(),
        context.displayCharterOnSpaceHomepage()));
    xhtml.addElement(communityHtml);
    final String vueJsStarter = String.format("new Vue({\n" +
            "        el : '#community-membership',\n" +
            "        provide : function() {\n" +
            "          return {\n" +
            "            context: this.context,\n" +
            "            communityService: new CommunityService(this.context),\n" +
            "            membershipService: new CommunityMembershipService(this.context)}},\n" +
            "        data : {\n" +
            "          context : {\n" +
            "            currentUser : currentUser,\n" +
            "            componentInstanceId : '%s',\n" +
            "            spaceLabel : '%s'}},\n" +
            "        methods : {\n" +
            "          reloadPage : function() {\n" +
            "            spWindow.loadSpace('%s');}}\n" +
            "      });\n",
        community.getComponentInstanceId(),
        javaStringToJsString(context.getSpaceLabel()),
        community.getSpaceId());
    xhtml.addElement(scriptContent(vueJsStarter));
    final var topWidget = new SpaceHomepageProxy.Widget();
    topWidget.setTitle(context.getComponentInstanceLabel());
    topWidget.setContent(xhtml.toString());
    proxy.setThinWidget(topWidget);
    final String defaultHomepage = getLookHelper(
        context.getRequest().getSession(false)).getSettings("defaultHomepage", "/dt");
    context.addRedirectVariable("path", defaultHomepage);
    context.addRedirectVariable("spaceId", community.getSpaceId());
  }

  @GET
  @Path("members")
  @RedirectToInternalJsp("members.jsp")
  @NavigationStep(identifier = MEMBER_LIST_NS_ID)
  @LowestRoleAccess(value = SilverpeasRole.READER)
  public void listMembers(CommunityWebRequestContext context) {
    final HttpRequest request = context.getRequest();
    final var members = CommunityWebManager.get().getMembers(context.getCommunity(), NO_PAGINATION);
    request.setAttribute("members", members);
  }

  @POST
  @Path("members/join")
  @Produces(MediaType.APPLICATION_JSON)
  public void join(CommunityWebRequestContext context) {
    CommunityWebManager.get().join(context.getCommunity());
  }

  @POST
  @Path("members/join/validate/{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  @LowestRoleAccess(value = SilverpeasRole.ADMIN)
  public void validate(CommunityWebRequestContext context) {
    final HttpRequest request = context.getRequest();
    final User requester = User.getById(context.getPathVariables().get("userId"));
    final boolean accept = request.getParameterAsBoolean("accept");
    final String message = request.getParameter("message");
    CommunityWebManager.get().validateRequestOf(requester, context.getCommunity(), accept, message);
  }

  @POST
  @Path("members/leave/{userId}")
  @LowestRoleAccess(value = SilverpeasRole.ADMIN)
  @Produces(MediaType.APPLICATION_JSON)
  public void endMembershipOf(CommunityWebRequestContext context) {
    final User member = User.getById(context.getPathVariables().get("userId"));
    CommunityWebManager.get().endMembershipOf(context.getCommunity(), member);
  }

  @POST
  @Path("members/leave")
  @LowestRoleAccess(value = SilverpeasRole.READER)
  @Produces(MediaType.APPLICATION_JSON)
  public void leave(CommunityWebRequestContext context) {
    final HttpRequest request = context.getRequest();
    final int reason = request.getParameterAsInteger("reason");
    final String message = request.getParameter("message");
    final boolean contactInFuture = request.getParameterAsBoolean("contactInFuture");
    CommunityWebManager.get().leave(context.getCommunity(), reason, message, contactInFuture);
  }

  @GET
  @Path("members/history")
  @RedirectToInternalJsp("history.jsp")
  @NavigationStep(identifier = HISTORY_VIEW_NS_ID)
  @LowestRoleAccess(value = SilverpeasRole.ADMIN)
  public void getHistory(CommunityWebRequestContext context) {
    final HttpRequest request = context.getRequest();
    final var history = CommunityWebManager.get().getHistory(context.getCommunity(), NO_PAGINATION);
    request.setAttribute("history", history);
  }

  @GET
  @Path("spaceHomepage/edit")
  public Navigation editSpaceHomePage(CommunityWebRequestContext context) {
    final WysiwygContent content = context.getCommunity().getSpacePresentationContent();
    return context.redirectToHtmlEditor(fromComponentSessionController(this)
        .withBrowseInfo(getString("community.browsebar.item.spaceHomePage.edition"))
        .withContributionId(content.getContribution().getIdentifier())
        .withIndexation(false)
        .withComeBackUrl(getApplicationURL() + context.getComponentUriBase() + "Main"));
  }
}