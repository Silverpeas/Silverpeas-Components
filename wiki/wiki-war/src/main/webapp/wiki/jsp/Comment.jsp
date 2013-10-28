<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@ page import="com.ecyrd.jspwiki.filters.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.ecyrd.jspwiki.htmltowiki.HtmlStringToWikiTranslator" %>
<%@ page import="com.ecyrd.jspwiki.ui.EditorManager" %>
<%@ page import="com.ecyrd.jspwiki.util.HttpUtil" %>
<%@ page import="com.ecyrd.jspwiki.auth.login.CookieAssertionLoginModule" %>
<%@ page import="com.ecyrd.jspwiki.workflow.DecisionRequiredException" %>
<%@ page errorPage="Error.jsp" %>
<%@ page import="javax.servlet.http.Cookie" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<%!
    Logger log = Logger.getLogger("JSPWiki");

	String findParam( PageContext ctx, String key )
	{
	    ServletRequest req = ctx.getRequest();
	
	    String val = req.getParameter( key );
	
	    if( val == null )
	    {
	        val = (String)ctx.findAttribute( key );
	    }
	
	    return val;
	}
%>

<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context and check for authorization
    WikiContext wikiContext = wiki.createContext( request, WikiContext.COMMENT );
    if( !wikiContext.hasAccess( response ) ) return;
    String pagereq = wikiContext.getName();

    ResourceBundle rb = wikiContext.getBundle("CoreResources");
    WikiSession wikiSession = wikiContext.getWikiSession();
    String storedUser = wikiSession.getUserPrincipal().getName();

    if( wikiSession.isAnonymous() )
    {
        storedUser  = TextUtil.replaceEntities( request.getParameter( "author" ) );
    }

    String ok      = request.getParameter("ok");
    String preview = request.getParameter("preview");
    String cancel  = request.getParameter("cancel");
    String author  = TextUtil.replaceEntities( request.getParameter("author") );
    String link    = TextUtil.replaceEntities( request.getParameter("link") );
    String remember = request.getParameter("remember");
    String changenote = TextUtil.replaceEntities( request.getParameter( "changenote" ) );

    WikiPage wikipage = wikiContext.getPage();
    WikiPage latestversion = wiki.getPage( pagereq );
    if( latestversion == null )
    {
        latestversion = wikiContext.getPage();
    }

    //
    //  Setup everything for the editors and possible preview.  We store everything in the
    //  session.
    //

    if( remember == null )
    {
        remember = (String)session.getAttribute("remember");
    }

    if( remember == null ) remember = "false";

    session.setAttribute("remember",remember);

    if( author == null )
    {
        author = storedUser;
    }
    if( author == null || author.length() == 0 ) author = "AnonymousCoward";

    session.setAttribute("author",author);

    if( link == null )
    {
        link = HttpUtil.retrieveCookieValue( request, "link" );
        if( link == null ) link = "";
    }

    session.setAttribute( "link", link );

    //
    //  Branch
    //
    log.debug("preview="+preview+", ok="+ok);

    if( ok != null )
    {
        log.info("Saving page "+pagereq+". User="+storedUser+", host="+request.getRemoteAddr() );

        //  Modifications are written here before actual saving

        WikiPage modifiedPage = (WikiPage)wikiContext.getPage().clone();

        //  FIXME: I am not entirely sure if the JSP page is the
        //  best place to check for concurrent changes.  It certainly
        //  is the best place to show errors, though.

        String spamhash = request.getParameter( SpamFilter.getHashFieldName(request) );
        
        if( !SpamFilter.checkHash(wikiContext,pageContext) )
        {
            return;
        }
        
        //
        //  We expire ALL locks at this moment, simply because someone has
        //  already broken it.
        //
        PageLock lock = wiki.getPageManager().getCurrentLock( wikipage );
        wiki.getPageManager().unlockPage( lock );
        session.removeAttribute( "lock-"+pagereq );

        //
        //  Set author and changenote information
        //

        modifiedPage.setAuthor( storedUser );

        if( changenote == null ) changenote = (String) session.getAttribute("changenote");

        session.removeAttribute("changenote");

        modifiedPage.setAttribute( WikiPage.CHANGENOTE, "Comment by "+storedUser );

        //
        //  Build comment part
        //

        StringBuffer pageText = new StringBuffer(wiki.getPureText( wikipage ));

        log.debug("Page initial contents are "+pageText.length()+" chars");

        //
        //  Add a line on top only if we need to separate it from the content.
        //
        if( pageText.length() > 0 )
        {
            pageText.append( "\n\n----\n\n" );
        }
        
        String commentText = EditorManager.getEditedText(pageContext);
        
        //
        //  WYSIWYG editor sends us its greetings
        //
        String htmlText = findParam( pageContext, "htmlPageText" );
        if( htmlText != null && cancel == null )
        {
        	commentText = new HtmlStringToWikiTranslator().translate(htmlText,wikiContext);
        }
        
        pageText.append( commentText );

        log.debug("Author name ="+author);
        if( author != null && author.length() > 0 )
        {
            String signature = author;

            if( link != null && link.length() > 0 )
            {
                link = HttpUtil.guessValidURI( link );

                signature = "["+author+"|"+link+"]";
            }

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy");

            pageText.append("\n\n--"+signature+", "+fmt.format(cal.getTime()));
        }

        if( TextUtil.isPositive(remember) )
        {
            if( link != null )
            {
                Cookie linkcookie = new Cookie("link", link);
                linkcookie.setMaxAge(1001*24*60*60);
                response.addCookie( linkcookie );
            }

            CookieAssertionLoginModule.setUserCookie( response, author );
        }
        else
        {
            session.removeAttribute("link");
            session.removeAttribute("author");
        }

        try
        {
            wikiContext.setPage( modifiedPage );
            wiki.saveText( wikiContext, pageText.toString() );
        }
        catch( DecisionRequiredException e )
        {
        	String redirect = wikiContext.getURL(WikiContext.VIEW,"ApprovalRequiredForPageChanges");
            response.sendRedirect( redirect );
            return;
        }
        catch( RedirectException e )
        {
            session.setAttribute( VariableManager.VAR_MSG, e.getMessage() );
            response.sendRedirect( e.getRedirect() );
            return;
        }
        response.sendRedirect(wiki.getViewURL(pagereq));
        return;
    }
    else if( preview != null )
    {
        log.debug("Previewing "+pagereq);
        
        String commentText = EditorManager.getEditedText(pageContext);
        
        //
        //  WYSIWYG editor sends us its greetings
        //
        String htmlText = findParam( pageContext, "htmlPageText" );
        if( htmlText != null && cancel == null )
        {
        	commentText = new HtmlStringToWikiTranslator().translate(htmlText,wikiContext);
        }
        
        session.setAttribute(EditorManager.REQ_EDITEDTEXT, commentText);
        response.sendRedirect( TextUtil.replaceString( wiki.getURL(WikiContext.PREVIEW, pagereq, "action=comment", true),"&amp;","&") );
        return;
    }
    else if( cancel != null )
    {
        log.debug("Cancelled editing "+pagereq);
        PageLock lock = (PageLock) session.getAttribute( "lock-"+pagereq );

        if( lock != null )
        {
            wiki.getPageManager().unlockPage( lock );
            session.removeAttribute( "lock-"+pagereq );
        }
        response.sendRedirect( wiki.getViewURL(pagereq) );
        return;
    }

    log.info("Commenting page "+pagereq+". User="+request.getRemoteUser()+", host="+request.getRemoteAddr() );

    //
    //  Determine and store the date the latest version was changed.  Since
    //  the newest version is the one that is changed, we need to track
    //  that instead of the edited version.
    //
    long lastchange = 0;

    Date d = latestversion.getLastModified();
    if( d != null ) lastchange = d.getTime();

    pageContext.setAttribute( "lastchange",
                              Long.toString( lastchange ),
                              PageContext.REQUEST_SCOPE );

    //  This is a hack to get the preview to work.
    // pageContext.setAttribute( "comment", Boolean.TRUE, PageContext.REQUEST_SCOPE );

    //
    //  Attempt to lock the page.
    //
    PageLock lock = wiki.getPageManager().lockPage( wikipage,
                                                    storedUser );

    if( lock != null )
    {
        session.setAttribute( "lock-"+pagereq, lock );
    }

    // Set the content type and include the response content
    response.setContentType("text/html; charset="+wiki.getContentEncoding() );
    response.setHeader( "Cache-control", "max-age=0" );
    response.setDateHeader( "Expires", new Date().getTime() );
    response.setDateHeader( "Last-Modified", new Date().getTime() );
    String contentPage = wiki.getTemplateManager().findJSP( pageContext,
                                                            wikiContext.getTemplate(),
                                                            "EditTemplate.jsp" );

%><wiki:Include page="<%=contentPage%>" />
