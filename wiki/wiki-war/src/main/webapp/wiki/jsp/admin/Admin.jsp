<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@ page import="com.ecyrd.jspwiki.ui.admin.*" %>
<%@page import="com.ecyrd.jspwiki.ui.SilverpeasTemplateManager"%>
<%@ page import="org.apache.commons.lang.time.StopWatch" %>
<%@ page errorPage="/Error.jsp" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%! 
    Logger log = Logger.getLogger("JSPWiki"); 
%>
<%
    String bean = request.getParameter("bean");
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context and check for authorization
    WikiContext wikiContext = wiki.createContext( request, WikiContext.ADMIN );
    if(!wikiContext.hasAccess( response )) return;
    
    //
    //  This is an experimental feature, so we will turn it off unless the
    //  user really wants to.
    //
    if( !TextUtil.isPositive(wiki.getWikiProperties().getProperty("jspwiki-x.adminui.enable")) )
    {
        %>
        <html>
        <body>
           <h1>Disabled</h1>
           <p>JSPWiki admin UI has been disabled.  This is an experimental feature, and is
           not guaranteed to work.  You may turn it on by specifying</p>
           <pre>
               jspwiki-x.adminui.enable=true
           </pre>
           <p>in your <tt>jspwiki.properties</tt> file.</p>
           <p>Have a nice day.  Don't forget to eat lots of fruits and vegetables.</p>
        </body>
        </html>
        <%
        return;
    }

    // Set the content type and include the response content
    response.setContentType("text/html; charset="+wiki.getContentEncoding() );
    String contentPage = wiki.getTemplateManager().findJSP( pageContext,
                                                            wikiContext.getTemplate(),
                                                            "admin/AdminTemplate.jsp" );
    
    pageContext.setAttribute( "engine", wiki, PageContext.REQUEST_SCOPE );
    pageContext.setAttribute( "context", wikiContext, PageContext.REQUEST_SCOPE );

    if( request.getMethod().equalsIgnoreCase("post") && bean != null )
    {
        AdminBean ab = wiki.getAdminBeanManager().findBean( bean );
        
        if( ab != null )
        {
            ab.doPost( wikiContext );
        }
        else
        {
            wikiContext.getWikiSession().addMessage( "No such bean "+bean+" was found!" );
        }
    }
    
%><wiki:Include page="<%=contentPage%>" />