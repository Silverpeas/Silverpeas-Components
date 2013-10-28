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
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.tags.*" %>
<%@ page import="com.ecyrd.jspwiki.filters.SpamFilter" %>
<%@ page import="com.ecyrd.jspwiki.ui.*" %>
<%@ page import="com.ecyrd.jspwiki.rpc.*" %>
<%@ page import="com.ecyrd.jspwiki.rpc.json.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<viewt:setBundle basename="templates.default"/>
<%--
        This is a plain editor for JSPWiki.
--%>
<%
	WikiContext context = WikiContext.findContext( pageContext ); 
   WikiEngine engine = context.getEngine();
   JSONRPCManager.requestJSON( context );  //FIXME: to be replace by standard mootools lib
   
   String contextPath = request.getContextPath() + "/wiki/jsp";

  TemplateManager.addResourceRequest( context, "script",  contextPath + "/scripts/jspwiki-edit.js" );
  TemplateManager.addResourceRequest( context, "script",  contextPath + "/scripts/posteditor.js" );
   String usertext = EditorManager.getEditedText( pageContext );
%>
<wiki:CheckRequestContext context="edit">
<wiki:NoSuchPage> <%-- this is a new page, check if we're cloning --%>
<%
  String clone = request.getParameter( "clone" ); 
  if( clone != null )
  {
    WikiPage p = engine.getPage( clone );
    if( p != null )
    {
      usertext = engine.getPureText( p );
    }
  }
%>
</wiki:NoSuchPage>
<%
  if( usertext == null )
  {
    usertext = engine.getPureText( context.getPage() );
  }
%>
</wiki:CheckRequestContext>
<% if( usertext == null ) usertext = "";  %>


<div style="width:100%"> <%-- Required for IE6 on Windows --%>

<form action="<wiki:CheckRequestContext 
     context='edit'><wiki:EditLink format='url'/></wiki:CheckRequestContext><wiki:CheckRequestContext 
     context='comment'><wiki:CommentLink format='url'/></wiki:CheckRequestContext>" 
       class="wikiform"
          id="editform" 
    onsubmit="return Wiki.submitOnce(this);"
      method="post" accept-charset="<wiki:ContentEncoding/>"
     enctype="application/x-www-form-urlencoded" >

  <%-- Edit.jsp relies on these being found.  So be careful, if you make changes. --%>
  <p id="submitbuttons">
  <input name="page" type="hidden" value="<wiki:Variable var='pagename' />" />
  <input name="action" type="hidden" value="save" />
  <%=SpamFilter.insertInputFields( pageContext )%>
  <input name="<%=SpamFilter.getHashFieldName(request)%>" type="hidden" value="<c:out value='${lastchange}' />" />
  <input type="submit" name="ok" value="<fmt:message key='editor.plain.save.submit'/>" 
    accesskey="s"
        title="<fmt:message key='editor.plain.save.title'/>" />
  <input type="submit" name="preview" value="<fmt:message key='editor.plain.preview.submit'/>" 
    accesskey="v"
        title="<fmt:message key='editor.plain.preview.title'/>" />
  <input type="submit" name="cancel" value="<fmt:message key='editor.plain.cancel.submit'/>" 
    accesskey="q" 
        title="<fmt:message key='editor.plain.cancel.title'/>" />
  <input type="button" name="tbREDO" id="tbREDO" value="<fmt:message key='editor.plain.redo.submit' />" 
        title="<fmt:message key='editor.plain.redo.title' />" disabled="disabled" />
  <input type="button" name="tbUNDO" id="tbUNDO" value="<fmt:message key='editor.plain.undo.submit' />" 
        title="<fmt:message key='editor.plain.undo.title' />" disabled="disabled" accesskey="z"/>
  </p>
  
  <div>
  <textarea id="editorarea" name="<%=EditorManager.REQ_EDITEDTEXT%>" 
         class="editor" 
       onkeyup="getSuggestions(this.id)"
       onclick="setCursorPos(this.id)" 
      onchange="setCursorPos(this.id)"
          rows="20" cols="80"><%=TextUtil.replaceEntities(usertext)%></textarea>
  </div>
  <%-- This following field is only for the SpamFilter to catch bots which are just randomly filling all fields and submitting.
       Normal user should never see this field, nor type anything in it. --%>
  <div style="display:none;">Authentication code: <input type="text" name="<%=SpamFilter.getBotFieldName()%>" id="<%=SpamFilter.getBotFieldName()%>" value=""/></div>
  <div style="display:none;">
    <div id="editassist">
      <a href="#" class="tool closed" rel="" title="<fmt:message key='editor.plain.editassist.title'/>">
        <fmt:message key='editor.plain.editassist'/>
      </a>
    </div>

    <div id="toolbar">
	  <a href="#" class="tool" rel="" id="tbLink" title="<fmt:message key='editor.plain.tbLink.title'/>">link</a>
	  <a href="#" class="tool" rel="break" id="tbH1" title="<fmt:message key='editor.plain.tbH1.title'/>">h1</a>
	  <a href="#" class="tool" rel="break" id="tbH2" title="<fmt:message key='editor.plain.tbH2.title'/>">h2</a>
	  <a href="#" class="tool" rel="break" id="tbH3" title="<fmt:message key='editor.plain.tbH3.title'/>">h3</a>
      <span>&nbsp;</span>
	  <a href="#" class="tool" rel="break" id="tbHR" title="<fmt:message key='editor.plain.tbHR.title'/>">hr</a>
	  <a href="#" class="tool" rel="" id="tbBR" title="<fmt:message key='editor.plain.tbBR.title'/>">br</a>
	  <a href="#" class="tool" rel="break" id="tbPRE" title="<fmt:message key='editor.plain.tbPRE.title'/>">pre</a>
	  <a href="#" class="tool" rel="break" id="tbDL" title="<fmt:message key='editor.plain.tbDL.title'/>">dl</a>
      <span>&nbsp;</span>
	  <a href="#" class="tool" rel="" id="tbB" title="<fmt:message key='editor.plain.tbB.title'/>">bold</a>
	  <a href="#" class="tool" rel="" id="tbI" title="<fmt:message key='editor.plain.tbI.title'/>">italic</a>
	  <a href="#" class="tool" rel="" id="tbMONO" title="<fmt:message key='editor.plain.tbMONO.title'/>">mono</a>
	  <a href="#" class="tool" rel="" id="tbSUP" title="<fmt:message key='editor.plain.tbSUP.title'/>">sup</a>
	  <a href="#" class="tool" rel="" id="tbSUB" title="<fmt:message key='editor.plain.tbSUB.title'/>">sub</a>
	  <a href="#" class="tool" rel="" id="tbSTRIKE" title="<fmt:message key='editor.plain.tbSTRIKE.title'/>">strike</a>
      <span>&nbsp;</span>
	  <a href="#" class="tool" rel="break" id="tbTOC" title="<fmt:message key='editor.plain.tbTOC.title'/>">toc</a>
	  <a href="#" class="tool" rel="break" id="tbTAB" title="<fmt:message key='editor.plain.tbTAB.title'/>">tab</a>
	  <a href="#" class="tool" rel="break" id="tbTABLE" title="<fmt:message key='editor.plain.tbTABLE.title'/>">table</a>
	  <a href="#" class="tool" rel="" id="tbIMG" title="<fmt:message key='editor.plain.tbIMG.title'/>">img</a>
	  <a href="#" class="tool" rel="break" id="tbCODE" title="<fmt:message key='editor.plain.tbCODE.title'/>">code</a>
	  <a href="#" class="tool" rel="break" id="tbQUOTE" title="<fmt:message key='editor.plain.tbQUOTE.title'/>">quote</a>
	  <a href="#" class="tool" rel="break" id="tbSIGN" title="<fmt:message key='editor.plain.tbSIGN.title'/>">sign</a>
	  <div style="clear:both;">
	  </div>
      <div style="display:none;">
      <input type="checkbox" name="tabcompletion" id="tabcompletion" <%=TextUtil.isPositive((String)session.getAttribute("tabcompletion")) ? "checked='checked'" : ""%>/>
      <label for="tabcompletion" title="<fmt:message key='editor.plain.tabcompletion.title'/>"><fmt:message key="editor.plain.tabcompletion"/></label>
      <input type="checkbox" name="smartpairs" id="smartpairs" <%=TextUtil.isPositive((String)session.getAttribute("smartpairs")) ? "checked='checked'" : ""%>/>
      <label for="smartpairs" title="<fmt:message key='editor.plain.smartpairs.title'/>"><fmt:message key="editor.plain.smartpairs"/></label>	  
	  </div>
    </div>
  </div>
    <p>
    <label for="changenote"><fmt:message key='editor.plain.changenote'/></label>
    <input type="text" name="changenote" id="changenote" size="80" maxlength="80" value="<c:out value='${changenote}'/>"/>
    </p>
  <wiki:CheckRequestContext context="comment">
    <fieldset>
	<legend><fmt:message key="editor.commentsignature"/></legend>
    <p>
    <label for="authorname" accesskey="n"><fmt:message key="editor.plain.name"/></label></td>
    <input type="text" name="author" id="authorname" value="<c:out value='${sessionScope.author}' />" />
    <input type="checkbox" name="remember" id="rememberme" <%=TextUtil.isPositive((String)session.getAttribute("remember")) ? "checked='checked'" : ""%>/>
    <label for="rememberme"><fmt:message key="editor.plain.remember"/></label>
    </p>
    <p>
    <label for="link" accesskey="m"><fmt:message key="editor.plain.email"/></label>
    <input type="text" name="link" id="link" size="24" value="<c:out value='${sessionScope.link}' />" />
    </p>
    </fieldset>
  </wiki:CheckRequestContext>

</form>
</div>

<form id="searchbar" action="#" class='wikiform'>
<p style="display:none;">
<%-- Search and replace section --%>
  <span style="white-space:nowrap;">
  <label for="tbFIND"><fmt:message key="editor.plain.find"/></label>
  <input type="text" name="tbFIND" id="tbFIND" size="16" />
  </span>
  <span style="white-space:nowrap;">
  <label for="tbREPLACE"><fmt:message key="editor.plain.replace"/></label>
  <input type="text" name="tbREPLACE" id="tbREPLACE" size="16" />
  </span>
  <span style="white-space:nowrap;">
  <input type="checkbox" name="tbMatchCASE" id="tbMatchCASE" />
  <label for="tbMatchCASE"><fmt:message key="editor.plain.matchcase"/></label>
  </span>
  <span style="white-space:nowrap;">
  <input type="checkbox" name="tbREGEXP" id="tbREGEXP" />
  <label for="tbREGEXP"><fmt:message key="editor.plain.regexp"/></label>
  </span>
  <span style="white-space:nowrap;">
  <input type="checkbox" name="tbGLOBAL" id="tbGLOBAL" checked="checked" />
  <label for="tbGLOBAL"><fmt:message key="editor.plain.global"/></label>
  </span>
  <span style="white-space:nowrap;">
  <input type="button" name="replace" id="replace" value="<fmt:message key='editor.plain.find.submit' />" />
  </span>
</p>
</form>
