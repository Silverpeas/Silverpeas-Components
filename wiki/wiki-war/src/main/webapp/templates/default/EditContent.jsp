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
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<view:setBundle basename="templates.default"/>
<%
  WikiContext c = WikiContext.findContext( pageContext );
  int attCount = c.getEngine().getAttachmentManager().listAttachments(c.getPage()).size();
  String attTitle = LocaleSupport.getLocalizedMessage(pageContext, "attach.tab");
  if( attCount != 0 ) attTitle += " (" + attCount + ")";
%>
  
<wiki:TabbedSection defaultTab="editcontent">  
  <wiki:Tab id="editcontent" title='<%=LocaleSupport.getLocalizedMessage(pageContext,"edit.tab.edit")%>' accesskey="e">
  <wiki:CheckLock mode="locked" id="lock">
    <div class="error">
      <fmt:message key="edit.locked">
        <fmt:param><c:out value="${lock.locker}"/></fmt:param>
        <fmt:param><c:out value="${lock.timeLeft}"/></fmt:param>
      </fmt:message>
    </div>
  </wiki:CheckLock>
  
  <wiki:CheckVersion mode="notlatest">
    <div class="warning">
      <fmt:message key="edit.restoring">
        <fmt:param><wiki:PageVersion/></fmt:param>
      </fmt:message>
    </div>
  </wiki:CheckVersion>
    
  <wiki:Editor />
    
</wiki:Tab>
  
  <wiki:PageExists>  
 <wiki:Tab id="comment" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "comment.tab.addcomment")%>'
           url="<%=c.getURL(WikiContext.COMMENT, c.getPage().getName())%>"
           accesskey="c" >
  </wiki:Tab>
  
  <wiki:Tab id="attach" title="<%= attTitle %>" accesskey="a">
    <wiki:Include page="AttachmentTab.jsp"/>
  </wiki:Tab>

  <wiki:Tab id="info" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "info.tab")%>'
           url="<%=c.getURL(WikiContext.INFO, c.getPage().getName())%>"
           accesskey="i" >
  </wiki:Tab>

  </wiki:PageExists>  
    
  <wiki:Tab id="edithelp" title='<%=LocaleSupport.getLocalizedMessage(pageContext,"edit.tab.help")%>' accesskey="h" >
  <wiki:InsertPage page="EditPageHelp" />
  <wiki:NoSuchPage page="EditPageHelp">
    <div class="error">
      <fmt:message key="comment.edithelpmissing">
        <fmt:param><wiki:EditLink page="EditPageHelp">EditPageHelp</wiki:EditLink></fmt:param>
      </fmt:message>
    </div>
  </wiki:NoSuchPage>  
  </wiki:Tab>

</wiki:TabbedSection>