/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.wiki.servlets;

import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.auth.AuthenticationManager;
import com.ecyrd.jspwiki.auth.WikiSecurityException;
import com.ecyrd.jspwiki.auth.authorize.SilverpeasWikiAuthorizer;
import com.ecyrd.jspwiki.event.WikiEngineEvent;
import com.ecyrd.jspwiki.event.WikiEventManager;
import com.ecyrd.jspwiki.i18n.SilverpeasWikiInternationalizationManager;
import com.silverpeas.wiki.control.WikiMultiInstanceManager;
import com.silverpeas.wiki.control.WikiSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

public class WikiRequestRouter extends ComponentRequestRouter<WikiSessionController> {
  private WikiEngine m_engine;

  /**
   * {@inheritDoc}
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    m_engine = WikiEngine.getInstance(config);
    SilverTrace.info("wiki", "WikiRequestRouter.init()",
        "root.MSG_GEN_PARAM_VALUE", "WikiServlet initialized.");
  }

  public void destroy() {
    SilverTrace.info("wiki", "WikiRequestRouter.destroy()",
        "root.MSG_GEN_PARAM_VALUE", "WikiServlet shutdown.");
    if (WikiEventManager.isListening(m_engine)) {
      WikiEventManager.fireEvent(m_engine, new WikiEngineEvent(m_engine,
          WikiEngineEvent.SHUTDOWN));
    }
    m_engine.getFilterManager().destroy();
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "Wiki";
  }

  /**
   * Method declaration
   *
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public WikiSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new WikiSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param wikiSC   The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex : "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, WikiSessionController wikiSC,
      HttpServletRequest request) {
    SilverTrace.info("wiki", "WikiRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + wikiSC.getUserId()
        + " Function=" + function);
    String[] roles = wikiSC.getUserRoles();
    request.setAttribute(SilverpeasWikiAuthorizer.ROLE_ATTR_NAME, roles);
    request.setAttribute(SilverpeasWikiAuthorizer.USER_ATTR_NAME, wikiSC
        .getUserDetail());
    WikiMultiInstanceManager.setComponentId(wikiSC.getComponentId(),
        getServletConfig(), request);
    SilverpeasWikiInternationalizationManager.setPreferredLanguage(wikiSC
        .getLanguage());
    request.setAttribute("userLanguage", wikiSC.getLanguage());
    String destination = "/wiki/jsp/" + function;
    try {
      if (function.startsWith("Main")) {
        WikiEngine engine = WikiEngine.getInstance(getServletConfig());
        AuthenticationManager authManager = engine.getAuthenticationManager();
        try {
          authManager.login(request);
        } catch (WikiSecurityException e) {
          e.printStackTrace();
        }
        destination = "/wiki/jsp/Wiki.jsp?Main";
      } else if (function.startsWith("searchResult")) {
        String pageName = request.getParameter("Id");
        destination = "/wiki/jsp/Wiki.jsp?page=" + pageName;
      } else if (function.startsWith("template")) {
        destination = "/" + function;
      } else if (function.startsWith("attach")) {
        destination = "/" + function;
      } else if (function.startsWith("JSON-RPC")) {
        destination = "/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }
    SilverTrace.info("wiki", "WikiRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }
}
