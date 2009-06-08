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

  public static void setComponentId(String newId, ServletConfig config,
      HttpServletRequest request) {
    String oldId = (String) componentID.get();
    componentID.set(newId);
    manageInstanceChange(oldId, newId, config, request);
  }

  public static String getComponentId() {
    return (String) componentID.get();
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
