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
    "http://repository.silverpeas.com/legal/licensing"

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
<%@ page import="java.util.*" %>
<%@ page import="com.ecyrd.jspwiki.attachment.Attachment" %>
<%@ page errorPage="Error.jsp" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

<%! 
    Logger log = Logger.getLogger("JSPWiki");
%>

<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context and check for authorization
    WikiContext wikiContext = wiki.createContext( request, WikiContext.DELETE );
    if(!wikiContext.hasAccess( response )) return;
    String pagereq = wikiContext.getName();

    WikiPage wikipage      = wikiContext.getPage();
    WikiPage latestversion = wiki.getPage( pagereq );

    String delete = request.getParameter( "delete" );
    String deleteall = request.getParameter( "delete-all" );

    if( latestversion == null )
    {
        latestversion = wikiContext.getPage();
    }

    // If deleting an attachment, go to the parent page.
    String redirTo = pagereq;
    if( wikipage instanceof Attachment ) {
        redirTo = ((Attachment)wikipage).getParentName();
    }

    if( deleteall != null )
    {
        log.info("Deleting page "+pagereq+". User="+request.getRemoteUser()+", host="+request.getRemoteAddr() );

        wiki.deletePage( pagereq );
        String destination = wiki.getViewURL(redirTo);
        log.info( "Redirecting to " + destination );
        response.sendRedirect(wiki.getViewURL(redirTo));
        return;
    }
    else if( delete != null )
    {
        log.info("Deleting a range of pages from "+pagereq);
        
        for( Enumeration params = request.getParameterNames(); params.hasMoreElements(); )
        {
            String paramName = (String)params.nextElement();
            
            if( paramName.startsWith("delver") )
            {
                int version = Integer.parseInt( paramName.substring(7) );
                
                WikiPage p = wiki.getPage( pagereq, version );
                
                log.debug("Deleting version "+version);
                wiki.deleteVersion( p );
            }
        }
        
        response.sendRedirect(wiki.getURL( WikiContext.INFO, redirTo, null, false ));
        return; 
    }

    // Set the content type and include the response content
    // FIXME: not so.
    response.setContentType("text/html; charset="+wiki.getContentEncoding() );
    String contentPage = wiki.getTemplateManager().findJSP( pageContext,
                                                            wikiContext.getTemplate(),
                                                            "EditTemplate.jsp" );
%><wiki:Include page="<%=contentPage%>" />

