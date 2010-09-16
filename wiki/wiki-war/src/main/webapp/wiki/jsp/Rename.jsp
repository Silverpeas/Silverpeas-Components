<%--

    Copyright (C) 2000 - 2009 Silverpeas

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
<%@ page errorPage="Error.jsp" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:setBundle basename="CoreResources"/>
<%!
    Logger log = Logger.getLogger("JSPWiki");
%>

<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context and check for authorization
	WikiContext wikiContext = wiki.createContext( request, WikiContext.RENAME );
    if(!wikiContext.hasAccess( response )) return;

    String renameFrom = wikiContext.getName();
    String renameTo = request.getParameter( "renameto");

    boolean changeReferences = false;

    ResourceBundle rb = wikiContext.getBundle("CoreResources");

    if (request.getParameter("references") != null)
    {
        changeReferences = true;
    }

    // Set the content type and include the response content
    response.setContentType("text/html; charset="+wiki.getContentEncoding() );

    log.info("Page rename request for page '"+renameFrom+ "' to new name '"+renameTo+"' from "+request.getRemoteAddr()+" by "+request.getRemoteUser() );

    WikiSession wikiSession = wikiContext.getWikiSession();
    try
    {
        if (renameTo.length() > 0)
        {
            String renamedTo = wiki.renamePage(wikiContext, renameFrom, renameTo, changeReferences);

            log.info("Page successfully renamed to '"+renamedTo+"'");

            response.sendRedirect( wikiContext.getURL( WikiContext.VIEW, renamedTo ) );
            return;
        }
        else
        {
            wikiSession.addMessage(rb.getString("rename.empty"));

            log.info("Page rename request failed because new page name was left blank");

%>
            <h3><fmt:message key="rename.error.title"/></h3>

            <dl>
               <dt><b><fmt:message key="rename.error.reason"/></b></dt>
               <dd>
                  <wiki:Messages div="error" />
               </dd>
            </dl>
<%
        }

    }
    catch (WikiException e)
    {
        if (e.getMessage().equals("Page exists"))
        {
            if (renameTo.equals( renameFrom ))
            {
                log.info("Page rename request failed because page names are identical");
                wikiSession.addMessage( rb.getString("rename.identical") );
            }
            else
            {
                log.info("Page rename request failed because new page name is already in use");
                Object[] args = { renameTo };
                wikiSession.addMessage(MessageFormat.format(rb.getString("rename.exists"),args));
            }
        }
        else
        {
            Object[] args = { e.toString() };
            wikiSession.addMessage( MessageFormat.format(rb.getString("rename.unknownerror"),args));
        }

%>
       <h3><fmt:message key="rename.error.title"/></h3>

       <dl>
          <dt><b><fmt:message key="rename.error.reason"/></b></dt>
          <dd>
             <wiki:Messages div="error" />
          </dd>
       </dl>
<%
    }
%>
