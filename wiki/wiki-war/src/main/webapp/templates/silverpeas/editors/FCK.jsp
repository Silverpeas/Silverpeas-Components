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
<%@ page isELIgnored ="false" %> 
<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>
<%@ page import="java.util.Properties"%>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.render.*" %>
<%@ page import="com.ecyrd.jspwiki.parser.JSPWikiMarkupParser" %>
<%@ page import="com.ecyrd.jspwiki.ui.*" %>
<%@ page import="com.ecyrd.jspwiki.filters.*" %>
<%@ page import="org.apache.commons.lang.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setLocale value="${userLanguage}"/>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:setBundle basename="templates.default"/>
<%--
    This provides the FCK editor for JSPWiki.
--%>
<%
	WikiContext context = WikiContext.findContext( pageContext );
    context.setVariable( RenderingManager.WYSIWYG_EDITOR_MODE, Boolean.TRUE );
    context.setVariable( WikiEngine.PROP_RUNFILTERS,  "false" );

    WikiPage wikiPage = context.getPage();
    String originalCCLOption = (String)wikiPage.getAttribute( JSPWikiMarkupParser.PROP_CAMELCASELINKS );
    wikiPage.setAttribute( JSPWikiMarkupParser.PROP_CAMELCASELINKS, "false" );
    
    String usertext = EditorManager.getEditedText(pageContext);
    TemplateManager.addResourceRequest( context, "script", "scripts/fckeditor/fckeditor.js" );
%>   
<wiki:CheckRequestContext context="edit"><%
    if( usertext == null )
    {
        usertext = context.getEngine().getPureText( context.getPage() );
    }%>
</wiki:CheckRequestContext>
<% if( usertext == null ) usertext = "";

   WikiEngine engine = context.getEngine();
   RenderingManager renderingManager = new RenderingManager();
   
   // since the WikiProperties are shared, we'll want to make our own copy of it for modifying.
   Properties copyOfWikiProperties = new Properties();
   copyOfWikiProperties.putAll( engine.getWikiProperties() );
   copyOfWikiProperties.setProperty( "jspwiki.renderingManager.renderer", WysiwygEditingRenderer.class.getName() );
   renderingManager.initialize( engine, copyOfWikiProperties );
	
   String pageAsHtml = StringEscapeUtils.escapeJavaScript( renderingManager.getHTML( context, usertext ) );
   
   // Disable the WYSIWYG_EDITOR_MODE and reset the other properties immediately
   // after the XHTML for FCK has been rendered.
   context.setVariable( RenderingManager.WYSIWYG_EDITOR_MODE, Boolean.FALSE );
   context.setVariable( WikiEngine.PROP_RUNFILTERS,  null );
   wikiPage.setAttribute( JSPWikiMarkupParser.PROP_CAMELCASELINKS, originalCCLOption );
   
   String templateDir = (String)copyOfWikiProperties.get( WikiEngine.PROP_TEMPLATEDIR );
   
   String protocol = "http://";
   if( request.isSecure() )
   {
       protocol = "https://";
   }   
%>

<form accept-charset="<wiki:ContentEncoding/>" method="post" 
      action="<wiki:CheckRequestContext context='edit'><wiki:EditLink format='url'/></wiki:CheckRequestContext><wiki:CheckRequestContext context='comment'><wiki:CommentLink format='url'/></wiki:CheckRequestContext>" 
      name="editform" id="editform"
      enctype="application/x-www-form-urlencoded">
    <p>
        <%-- Edit.jsp relies on these being found.  So be careful, if you make changes. --%>
        <input name="page" type="hidden" value="<wiki:Variable var="pagename"/>" />
        <input name="action" type="hidden" value="save" />
        <input name="<%=SpamFilter.getHashFieldName(request)%>" type="hidden" value="<c:out value='${lastchange}' />" />
    </p>
<div style="width:100%"> <%-- Required for IE6 on Windows --%>
<script type="text/javascript">
//<![CDATA[
   var oFCKeditor = new FCKeditor( 'htmlPageText' );
   oFCKeditor.BasePath = 'scripts/fckeditor/';
   oFCKeditor.Value = '<%=pageAsHtml%>';
   oFCKeditor.Width  = '100%';
   oFCKeditor.Height = '450';
   oFCKeditor.Config['CustomConfigurationsPath'] = '<%=request.getContextPath()%>/wiki/jsp/scripts/fckconfig.js';
   oFCKeditor.Config['StylesXmlPath'] = '<%=request.getContextPath()%>/wiki/jsp/scripts/fckstyles.xml';
   oFCKeditor.Config['TemplatesXmlPath'] = '<%=request.getContextPath()%>/wiki/jsp/scripts/fcktemplates.xml';
   oFCKeditor.Config['BaseHref'] = '<%=protocol%><%=request.getServerName()%>:<%=request.getServerPort()%><%=request.getContextPath()%>/';
   oFCKeditor.Config['EditorAreaCSS'] = '<%=request.getContextPath()%>/templates/<%=templateDir%>/jspwiki.css';
   oFCKeditor.Config['SmileyPath'] = oFCKeditor.Config['BaseHref'] + 'wiki/jsp/scripts/fckeditor/editor/images/smiley/msn/' ;
   oFCKeditor.Create();
//]]>
</script>
<noscript>
  <div class="error"><fmt:message key="editor.fck.noscript" /></div>
</noscript>
   <wiki:CheckRequestContext context="edit">
    <p>
    <label for="changenote"><fmt:message key='editor.plain.changenote'/></label>
    <input type="text" id="changenote" name="changenote" size="80" maxlength="80" value="<c:out value='${changenote}'/>"/>
    </p>
   </wiki:CheckRequestContext>
   <wiki:CheckRequestContext context="comment">
    <fieldset>
      <legend><fmt:message key="editor.commentsignature"/></legend>
      <label for="authorname" accesskey="n"><fmt:message key="editor.plain.name"/></label>
      <input type="text" readonly="readonly" name="author" id="authorname" value="<c:out value='${sessionScope.author}' />" />
    </fieldset>
  </wiki:CheckRequestContext>
  <p>
    <input name='ok' type='submit' value='<fmt:message key="editor.plain.save.submit"/>' />
    <input name='preview' type='submit' value='<fmt:message key="editor.plain.preview.submit"/>' />
    <input name='cancel' type='submit' value='<fmt:message key="editor.plain.cancel.submit"/>' />
  </p>
</div>
</form>
