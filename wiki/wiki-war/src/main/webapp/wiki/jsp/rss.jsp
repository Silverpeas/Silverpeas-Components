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
<?xml version="1.0" encoding="UTF-8"?>

<%@ page import="java.util.*,com.ecyrd.jspwiki.*" %>
<%@ page import="org.apache.log4j.*" %>
<%@ page import="java.text.*" %>
<%@ page import="com.ecyrd.jspwiki.rss.*" %>
<%@ page import="com.ecyrd.jspwiki.util.*" %>
<%@ taglib uri="/WEB-INF/oscache.tld" prefix="oscache" %>

<%!
    Logger log = Logger.getLogger("JSPWiki");
%>

<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context and check for authorization
    WikiContext wikiContext = wiki.createContext( request, "rss" );
    if(!wikiContext.hasAccess( response )) return;
    WikiPage    wikipage    = wikiContext.getPage();

    // Redirect if baseURL not set or RSS generation not on
    if( wiki.getBaseURL().length() == 0 )
    {
        response.sendError( 500, "The jspwiki.baseURL property has not been defined for this wiki - cannot generate RSS" );
        return;
    }
    
    if( wiki.getRSSGenerator() == null )
    {
        response.sendError( 404, "RSS feeds are disabled at administrator request" );
        return;
    }

    if( wikipage == null || !wiki.pageExists(wikipage.getName()) )
    {
        response.sendError( 404, "No such page "+wikipage.getName() );
        return;
    }

    WatchDog w = wiki.getCurrentWatchDog();
    w.enterState("Generating RSS",60);
    
    // Set the mode and type for the feed
    String      mode        = request.getParameter("mode");
    String      type        = request.getParameter("type");
    
    if( mode == null || !(mode.equals(RSSGenerator.MODE_BLOG) || mode.equals(RSSGenerator.MODE_WIKI)) ) 
    	   mode = RSSGenerator.MODE_BLOG;
    if( type == null || !(type.equals(RSSGenerator.RSS10) || type.equals(RSSGenerator.RSS20) || type.equals(RSSGenerator.ATOM)) ) 
    	   type = RSSGenerator.RSS20;
    
    // Force the TranslatorReader to output absolute URLs
    // regardless of the current settings.
    wikiContext.setVariable( WikiEngine.PROP_REFSTYLE, "absolute" );

    // Set the content type and include the response content
    response.setContentType( RSSGenerator.getContentType(type)+"; charset=UTF-8");

    StringBuffer result = new StringBuffer();
    SimpleDateFormat iso8601fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    Properties properties = wiki.getWikiProperties();
    String channelDescription = wiki.getRequiredProperty( properties, RSSGenerator.PROP_CHANNEL_DESCRIPTION );
    String channelLanguage    = wiki.getRequiredProperty( properties, RSSGenerator.PROP_CHANNEL_LANGUAGE );

    //
    //  Now, list items.
    //
    List changed;
    
    if( mode.equals("blog") )
    {
        com.ecyrd.jspwiki.plugin.WeblogPlugin plug = new com.ecyrd.jspwiki.plugin.WeblogPlugin();
        changed = plug.findBlogEntries(wiki.getPageManager(), 
                                       wikipage.getName(),
                                       new Date(0L),
                                       new Date());
    }
    else
    {
        changed = wiki.getVersionHistory( wikipage.getName() );
    }
    
    //
    //  Check if nothing has changed, so we can just return a 304
    //
    boolean hasChanged = false;
    Date    latest     = new Date(0);

    for( Iterator i = changed.iterator(); i.hasNext(); )
    {
        WikiPage p = (WikiPage) i.next();

        if( !HttpUtil.checkFor304( request, p ) ) hasChanged = true;
        if( p.getLastModified().after( latest ) ) latest = p.getLastModified();
    }

    if( !hasChanged && changed.size() > 0 )
    {
        response.sendError( HttpServletResponse.SC_NOT_MODIFIED );
        w.exitState();
        return;
    }

    response.addDateHeader("Last-Modified",latest.getTime());
    response.addHeader("ETag", HttpUtil.createETag(wikipage) );
%>
<%-- <oscache:cache time="300"> --%>
<%
    out.println(wiki.getRSSGenerator().generateFeed( wikiContext, changed, mode, type ));
%>
<%-- </oscache:cache> --%>
<% w.exitState(); %>
