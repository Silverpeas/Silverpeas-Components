<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored ="false" %> 
<%@ page import="org.apache.log4j.*" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="java.security.Principal" %>
<%@ page import="com.ecyrd.jspwiki.auth.*" %>
<%@ page import="com.ecyrd.jspwiki.auth.login.CookieAssertionLoginModule" %>
<%@ page import="com.ecyrd.jspwiki.auth.login.CookieAuthenticationLoginModule" %>
<%@ page import="com.ecyrd.jspwiki.auth.user.DuplicateUserException" %>
<%@ page import="com.ecyrd.jspwiki.auth.user.UserProfile" %>
<%@ page import="com.ecyrd.jspwiki.workflow.DecisionRequiredException" %>
<%@ page import="com.ecyrd.jspwiki.tags.WikiTagBase" %>
<%@ page errorPage="Error.jsp" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%!
    Logger log = Logger.getLogger("JSPWiki");
%>
<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    AuthenticationManager mgr = wiki.getAuthenticationManager();
    WikiContext wikiContext = wiki.createContext( request, WikiContext.LOGIN );
    pageContext.setAttribute( WikiTagBase.ATTR_CONTEXT,
                              wikiContext,
                              PageContext.REQUEST_SCOPE );
    WikiSession wikiSession = wikiContext.getWikiSession();
    ResourceBundle rb = wikiContext.getBundle("CoreResources");

    // Set the redirect-page variable if one was passed as a parameter
    if( request.getParameter( "redirect" ) != null )
    {
        wikiContext.setVariable( "redirect", request.getParameter( "redirect" ) );
    }
    else
    {
        wikiContext.setVariable( "redirect", wiki.getFrontPage());
    }

    // Are we saving the profile?
    if( "saveProfile".equals(request.getParameter("action")) )
    {
        UserManager userMgr = wiki.getUserManager();
        UserProfile profile = userMgr.parseProfile( wikiContext );
         
        // Validate the profile
        userMgr.validateProfile( wikiContext, profile );

        // If no errors, save the profile now & refresh the principal set!
        if ( wikiSession.getMessages( "profile" ).length == 0 )
        {
            try
            {
                userMgr.setUserProfile( wikiSession, profile );
                CookieAssertionLoginModule.setUserCookie( response, profile.getFullname() );
            }
            catch( DuplicateUserException e )
            {
                // User collision! (full name or wiki name already taken)
                wikiSession.addMessage( "profile", e.getMessage() );
            }
            catch( DecisionRequiredException e )
            {
                String redirect = wiki.getURL(WikiContext.VIEW,"ApprovalRequiredForUserProfiles",null,true);
                response.sendRedirect( redirect );
                return;
            }
            catch( WikiSecurityException e )
            {
                // Something went horribly wrong! Maybe it's an I/O error...
                wikiSession.addMessage( "profile", e.getMessage() );
            }
        }
        if ( wikiSession.getMessages( "profile" ).length == 0 )
        {
            String redirectPage = request.getParameter( "redirect" );
            response.sendRedirect( wiki.getViewURL(redirectPage) );
            return;
        }
    }

    // If NOT using container auth, perform all of the access control logic here...
    // (Note: if using the container for auth, it will handle all of this for us.)
    if( !mgr.isContainerAuthenticated() )
    {
        // If user got here and is already authenticated, it means
        // they just aren't allowed access to what they asked for.
        // Weepy tears and hankies all 'round.
        if( wikiSession.isAuthenticated() )
        {
            response.sendError( HttpServletResponse.SC_FORBIDDEN, rb.getString("login.error.noaccess") );
            return;
        }

        // If using custom auth, we need to do the login now

        String action = request.getParameter("action");
        if( request.getParameter("submitlogin") != null )
        {
            String uid    = request.getParameter( "j_username" );
            String passwd = request.getParameter( "j_password" );
            log.debug( "Attempting to authenticate user " + uid );

            // Log the user in!
            if ( mgr.login( wikiSession, uid, passwd ) )
            {
                log.info( "Successfully authenticated user " + uid + " (custom auth)" );
            }
            else
            {
                log.info( "Failed to authenticate user " + uid );
                wikiSession.addMessage( "login", rb.getString("login.error.password") );
            }
        }
    }
    else
    {
        //
        //  Have we already been submitted?  If yes, then we can assume that
        //  we have been logged in before.
        //
        Object seen = session.getAttribute("_redirect");
        if( seen != null )
        {
            response.sendError( HttpServletResponse.SC_FORBIDDEN, rb.getString("login.error.noaccess") );
            session.removeAttribute("_redirect");
            return;
        }
        session.setAttribute("_redirect","I love Outi"); // Just any marker will do

        // If using container auth, the container will have automatically
        // attempted to log in the user before Login.jsp was loaded.
        // Thus, if we got here, the container must have authenticated
        // the user already. All we do is simply record that fact.
        // Nice and easy.

        Principal user = wikiSession.getLoginPrincipal();
        log.info( "Successfully authenticated user " + user.getName() + " (container auth)" );
    }

    // If user logged in, set the user cookie with the wiki principal's name.
    // redirect to wherever we're supposed to go. If login.jsp
    // was called without parameters, this will be the front page. Otherwise,
    // there's probably a 'page' parameter telling us where to go.

    if( wikiSession.isAuthenticated() )
    {
        String rember = request.getParameter( "j_remember" );

        // Set user cookie
        Principal principal = wikiSession.getUserPrincipal();
        CookieAssertionLoginModule.setUserCookie( response, principal.getName() );

        if( rember != null )
        {
            CookieAuthenticationLoginModule.setLoginCookie( wiki, response, principal.getName() );
        }

        // If wiki page was "Login", redirect to main, otherwise use the page supplied
        String redirectPage = request.getParameter( "redirect" );
        if ( redirectPage == null )
        {
           redirectPage = wiki.getFrontPage();
        }
        String viewUrl = ( "Login".equals( redirectPage ) ) ? "Wiki.jsp" : wiki.getViewURL( redirectPage );

        // Redirect!
        log.info( "Redirecting user to " + viewUrl );
        response.sendRedirect( viewUrl );
        return;
    }

    // If we've gotten here, the user hasn't authenticated yet.
    // So, find the login form and include it. This should be in the same directory
    // as this page. We don't need to use the wiki:Include tag.

    response.setContentType("text/html; charset="+wiki.getContentEncoding() );

%><jsp:include page="LoginForm.jsp" />