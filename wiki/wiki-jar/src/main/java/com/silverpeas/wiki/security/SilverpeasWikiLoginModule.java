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
package com.silverpeas.wiki.security;

import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import com.ecyrd.jspwiki.auth.Authorizer;
import com.ecyrd.jspwiki.auth.WikiPrincipal;
import com.ecyrd.jspwiki.auth.authorize.Role;
import com.ecyrd.jspwiki.auth.authorize.SilverpeasWikiAuthorizer;
import com.ecyrd.jspwiki.auth.authorize.WebAuthorizer;
import com.ecyrd.jspwiki.auth.login.AbstractLoginModule;
import com.ecyrd.jspwiki.auth.login.AuthorizerCallback;
import com.ecyrd.jspwiki.auth.login.HttpRequestCallback;
import com.ecyrd.jspwiki.auth.login.PrincipalWrapper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
/**
 *
 */
public class SilverpeasWikiLoginModule extends AbstractLoginModule {

  /**
   * Logs in the user.
   * 
   * @see javax.security.auth.spi.LoginModule#login()
   */
  @SuppressWarnings("unchecked")
  public boolean login() throws LoginException {
    HttpRequestCallback rcb = new HttpRequestCallback();
    AuthorizerCallback acb = new AuthorizerCallback();
    Callback[] callbacks = new Callback[] { rcb, acb };
    try {
      // First, try to extract a Principal object out of the request
      // directly. If we find one, we're done.
      m_handler.handle(callbacks);
      HttpServletRequest request = rcb.getRequest();
      if (request == null) {
        throw new LoginException("No Http request supplied.");
      }
      UserDetail userDetail = (UserDetail) request
          .getAttribute(SilverpeasWikiAuthorizer.USER_ATTR_NAME);
      if (userDetail == null) {
        throw new LoginException("No user supplied.");
      }
      String[] userRoles = (String[]) request
          .getAttribute(SilverpeasWikiAuthorizer.ROLE_ATTR_NAME);
      Principal principal = new WikiPrincipal(userDetail.getLogin(),
          WikiPrincipal.LOGIN_NAME);
      Principal principalFullName = new WikiPrincipal(userDetail
          .getDisplayedName(), WikiPrincipal.FULL_NAME);
      Principal principalWikiName = new WikiPrincipal(userDetail
          .getDisplayedName(), WikiPrincipal.WIKI_NAME);
      SilverTrace.debug("wiki", "SilverpeasWikiLoginModule", "Added Principal "
          + principal.getName() + ",Role.ANONYMOUS,Role.ALL");
      m_principals.add(new PrincipalWrapper(principal));
      m_principals.add(new PrincipalWrapper(principalWikiName));
      m_principals.add(new PrincipalWrapper(principalFullName));
      // Add any container roles
      injectWebAuthorizerRoles(acb.getAuthorizer(), request);
      // If login succeeds, commit these roles
      for (String userRole : userRoles) {
        m_principals.add(convertSilverpeasRole(userRole));
      }
      // If login succeeds, remove these principals/roles
      m_principalsToOverwrite.add(WikiPrincipal.GUEST);
      m_principalsToOverwrite.add(Role.ANONYMOUS);
      m_principalsToOverwrite.add(Role.ASSERTED);
      // If login fails, remove these roles
      m_principalsToRemove.add(Role.AUTHENTICATED);

      return true;
    } catch (IOException e) {
      SilverTrace
          .error("wiki", "SilverpeasWikiLoginModule", "wiki.EX_LOGIN", e);
      return false;
    } catch (UnsupportedCallbackException e) {
      SilverTrace
          .error("wiki", "SilverpeasWikiLoginModule", "wiki.EX_LOGIN", e);
      return false;
    }
  }

  /**
   * If the current Authorizer is a
   * {@link com.ecyrd.jwpwiki.auth.authorize.WebAuthorizer}, this method
   * iterates through each role returned by the authorizer (via
   * {@link com.ecyrd.jwpwiki.auth.authorize.WebAuthorizer#isUserInRole(HttpServletRequest, Role)}
   * ) and injects the appropriate ones into the Subject.
   * 
   * @param acb
   *          the authorizer callback
   * @param rcb
   *          the HTTP request
   */
  @SuppressWarnings("unchecked")
  private final void injectWebAuthorizerRoles(Authorizer authorizer,
      HttpServletRequest request) {
    Principal[] roles = authorizer.getRoles();
    Set<Principal> foundRoles = new HashSet<Principal>();
    if (authorizer instanceof WebAuthorizer) {
      WebAuthorizer wa = (WebAuthorizer) authorizer;
      for (int i = 0; i < roles.length; i++) {
        if (wa.isUserInRole(request, roles[i])) {
          foundRoles.add(roles[i]);
          SilverTrace.debug("wiki", "SilverpeasWikiLoginModule",
              "Added Principal " + roles[i].getName() + ".");
        }
      }
    }
    // Add these container roles if login succeeds
    m_principals.addAll(foundRoles);
    // Make sure the same ones are removed if login fails
    m_principalsToRemove.addAll(foundRoles);
  }
  
  /**
   * Maps a classic Silverpeas role to a JSPWiki role.
   * JSPWiki roles are defined in jspwiki.policy with their permissions.
   * @param userRole the silverpeas role.
   * @return the JSPWiki corresponding Role.
   */
  protected Role convertSilverpeasRole(String userRole) {
    SilverpeasRole role = SilverpeasRole.valueOf(userRole);
    switch(role) {
      case admin :
        return new Role("Administrator");
      case publisher :
      case writer :
        return new Role("Contributor");
      case user :
        return new Role("Reader");
      default:
        return Role.AUTHENTICATED;
    }
  }

}
