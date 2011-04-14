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
package com.silverpeas.wiki.control;

import java.security.Principal;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiSession;
import com.ecyrd.jspwiki.auth.AuthenticationManager;
import com.ecyrd.jspwiki.auth.WikiSecurityException;
import com.ecyrd.jspwiki.event.WikiEventManager;
import com.ecyrd.jspwiki.event.WikiSecurityEvent;

public class WikiMultiInstanceManager {

  private static final String BREADCRUMBTRAIL_KEY = "breadCrumbTrail";

  private static ThreadLocal<String> componentID = new ThreadLocal<String>();

  public static void setComponentId(String newId, ServletConfig config, HttpServletRequest request) {
    String oldId = componentID.get();
    componentID.set(newId);
    manageInstanceChange(oldId, newId, config, request);
  }

  public static String getComponentId() {
    return componentID.get();
  }

  private static void manageInstanceChange(String oldId, String newId,
      ServletConfig config, HttpServletRequest request) {
    if ((oldId == null)) {
      WikiEngine engine = WikiEngine.getInstance(config);
      AuthenticationManager authManager = engine.getAuthenticationManager();
      try {
        authManager.login(request);
      } catch (WikiSecurityException e) {
        e.printStackTrace();
      }
      return;
    }
    if (oldId.equals(newId)) {
      return;
    }
    HttpSession httpSession = request.getSession(true);
    httpSession.removeAttribute(BREADCRUMBTRAIL_KEY);
    WikiEngine engine = WikiEngine.getInstance(config);
    WikiSession session = WikiSession.getWikiSession(engine, request);
    Principal originalPrincipal = session.getLoginPrincipal();
    session.invalidate();
    AuthenticationManager authManager = engine.getAuthenticationManager();
    if (WikiEventManager.isListening(authManager)) {
      WikiEventManager.fireEvent(authManager, new WikiSecurityEvent(
          authManager, WikiSecurityEvent.LOGOUT, originalPrincipal, null));
    }
    try {
      authManager.login(request);
    } catch (WikiSecurityException e) {
      e.printStackTrace();
    }
  }

}
