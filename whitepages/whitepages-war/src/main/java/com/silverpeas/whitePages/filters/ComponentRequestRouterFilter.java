/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.whitePages.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.stratelia.silverpeas.peasCore.URLManager;

/**
 * Le filtre LoginFilter a pour effet de contrôler que l'utilisateur courant n'a pas une fiche à
 * remplir dans une instance de whitePages. Si c'est le cas, 2 attributs sont mis en sessions : -
 * RedirectToComponentId : componentId de l'instance pour que le mecanisme de redirection le
 * renvoie
 * sur le composant - - forceCardCreation : componentId de l'instance Le filtre RequestRouterFilter
 * verifie la présence
 * @author Ludovic Bertin
 */
public class ComponentRequestRouterFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String sURI = httpRequest.getRequestURI();

    if (sURI.startsWith(URLManager.getApplicationURL() + "/R")) {
      HttpSession session = httpRequest.getSession(false);
      if (session == null) {
        chain.doFilter(request, response);
        return;
      }
      String componentId = (String) session.getAttribute(LoginFilter.ATTRIBUTE_FORCE_CARD_CREATION);
      if (isRedirectRequired(httpRequest, componentId)) {
        StringBuilder redirectURL = new StringBuilder(512);
        redirectURL.append(URLManager.getURL(null, componentId));
        redirectURL.append("ForceCardCreation");
        RequestDispatcher dispatcher = request.getRequestDispatcher(redirectURL.toString());
        dispatcher.forward(request, response);
      } else {
        chain.doFilter(request, response);
      }
    } else {
      chain.doFilter(request, response);
    }
  }

  private boolean isRedirectRequired(HttpServletRequest request, String componentId) {
    String requestURL = request.getRequestURL().toString();
    return ((!requestURL.contains("RpdcClassify")) &&
        (!requestURL.contains("RwhitePages")) && (!requestURL.contains("Rclipboard")) &&
        (!requestURL.contains("importCalendar")) && (componentId != null));
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void destroy() {
  }
}
