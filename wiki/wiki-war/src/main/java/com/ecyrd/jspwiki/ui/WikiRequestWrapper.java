/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.ecyrd.jspwiki.ui;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiSession;
import com.ecyrd.jspwiki.auth.SessionMonitor;
import com.ecyrd.jspwiki.auth.authorize.Role;

/**
 * Servlet request wrapper that encapsulates an incoming HTTP request and
 * overrides its security methods so that the request returns JSPWiki-specific
 * values.
 * 
 * @author Andrew Jaquith
 * @since 2.8
 */
public class WikiRequestWrapper extends HttpServletRequestWrapper {
  private final WikiSession m_session;

  /**
   * Constructs a new wrapped request.
   * 
   * @param engine
   *          the wiki engine
   * @param request
   *          the request to wrap
   */
  public WikiRequestWrapper(WikiEngine engine, HttpServletRequest request) {
    super(request);

    // Get and stash a reference to the current WikiSession
    m_session = SessionMonitor.getInstance(engine).find(request.getSession());
  }

  /**
   * Returns the remote user for the HTTP request, taking into account both
   * container and JSPWiki custom authentication status. Specifically, if the
   * wrapped request contains a remote user, this method returns that remote
   * user. Otherwise, if the user's WikiSession is an authenticated session
   * (that is, {@link WikiSession#isAuthenticated()} returns <code>true</code>,
   * this method returns the name of the principal returned by
   * {@link WikiSession#getLoginPrincipal()}.
   */
  public String getRemoteUser() {
    if (super.getRemoteUser() != null) {
      return super.getRemoteUser();
    }

    if (m_session.isAuthenticated()) {
      return m_session.getLoginPrincipal().getName();
    }
    return null;
  }

  /**
   * Returns the user principal for the HTTP request, taking into account both
   * container and JSPWiki custom authentication status. Specifically, if the
   * wrapped request contains a user principal, this method returns that
   * principal. Otherwise, if the user's WikiSession is an authenticated session
   * (that is, {@link WikiSession#isAuthenticated()} returns <code>true</code>,
   * this method returns the value of {@link WikiSession#getLoginPrincipal()}.
   */
  public Principal getUserPrincipal() {
    if (super.getUserPrincipal() != null) {
      return super.getUserPrincipal();
    }

    if (m_session.isAuthenticated()) {
      return m_session.getLoginPrincipal();
    }
    return null;
  }

  /**
   * Determines whether the current user possesses a supplied role, taking into
   * account both container and JSPWIki custom authentication status.
   * Specifically, if the wrapped request shows that the user possesses the
   * role, this method returns <code>true</code>. If not, this method iterates
   * through the built-in Role objects (<em>e.g.</em>, ANONYMOUS, ASSERTED,
   * AUTHENTICATED) returned by {@link WikiSession#getRoles()} and checks to see
   * if any of these principals' names match the supplied role.
   */
  public boolean isUserInRole(String role) {
    boolean hasContainerRole = super.isUserInRole(role);
    if (hasContainerRole) {
      return true;
    }

    // Iterate through all of the built-in roles and look for a match
    Principal[] principals = m_session.getRoles();
    for (int i = 0; i < principals.length; i++) {
      if (principals[i] instanceof Role) {
        Role principal = (Role) principals[i];
        if (Role.isBuiltInRole(principal) && principal.getName().equals(role)) {
          return true;
        }
      }
    }

    // None of the built-in roles match, so no luck
    return false;
  }

}
