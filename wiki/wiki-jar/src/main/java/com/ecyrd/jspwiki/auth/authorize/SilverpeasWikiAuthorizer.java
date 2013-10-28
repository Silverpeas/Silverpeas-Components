/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ecyrd.jspwiki.auth.authorize;

import java.security.Principal;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiSession;
import com.ecyrd.jspwiki.auth.WikiPrincipal;
import com.ecyrd.jspwiki.auth.WikiSecurityException;
import com.ecyrd.jspwiki.auth.authorize.WebAuthorizer;

public class SilverpeasWikiAuthorizer implements WebAuthorizer {

  public static final String ROLE_ATTR_NAME = "SilverpeasWikiRole";
  public static final String USER_ATTR_NAME = "SilverpeasWikiUser";
  private static Principal[] principals;
  static {
    principals = new WikiPrincipal[3];
    principals[0] = new WikiPrincipal("Admin");
    principals[1] = new WikiPrincipal("Authenticated");
    principals[2] = new WikiPrincipal("All");

  }

  public boolean isUserInRole(HttpServletRequest request, Principal principal) {
    String[] roles = (String[]) request.getAttribute(ROLE_ATTR_NAME);
    String requiredRole = principal.getName();
    for (int i = 0; i < roles.length; i++) {
      if (roles[i].equals(requiredRole))
        return true;
    }
    return false;
  }

  public Principal findRole(String role) {
    // TODO Auto-generated method stub
    return new WikiPrincipal(role);
  }

  public Principal[] getRoles() {
    return principals;
  }

  public void initialize(WikiEngine arg0, Properties arg1)
      throws WikiSecurityException {
    // TODO Auto-generated method stub

  }

  public boolean isUserInRole(WikiSession arg0, Principal arg1) {
    // TODO Auto-generated method stub
    return false;
  }

}
