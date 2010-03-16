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
<%@ page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>
<%@ page import="com.ecyrd.jspwiki.*"%>
<%@ page import="com.ecyrd.jspwiki.attachment.*"%>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ page import="javax.servlet.jsp.jstl.fmt.*"%>
<fmt:setLocale value="${userLanguage}" />
<view:setBundle basename="templates.default" />
<%
  WikiContext c = WikiContext.findContext(pageContext);
  int attCount = c.getEngine().getAttachmentManager().listAttachments(
      c.getPage()).size();
  String attTitle = LocaleSupport.getLocalizedMessage(pageContext,
      "attach.tab");
  if (attCount != 0)
    attTitle += " (" + attCount + ")";
%>

<view:tabs>

  <c:set var="tabContentTitle"><%=LocaleSupport.getLocalizedMessage(pageContext, "view.tab")%></c:set>
  <c:set var="viewAction" value="<%=c.getURL(WikiContext.VIEW, c.getPage().getName())%>" />
  <c:if test="${empty param.attach}">
    <view:tab label="${tabContentTitle}" action="${'#'}" selected="true" />
  </c:if>
  <c:if test="${not empty param.attach}">
    <view:tab label="${tabContentTitle}" action="${viewAction}" selected="false" />
  </c:if>

  <wiki:PageExists>
    <wiki:Permission permission="comment">
      <c:set var="tabCommentsTitle"><%=LocaleSupport.getLocalizedMessage(pageContext,
                      "comment.tab.addcomment")%></c:set>
      <c:set var="commentsAction" value="<%=c.getURL(WikiContext.COMMENT, c.getPage().getName())%>" />
      <view:tab label="${tabCommentsTitle}" action="${commentsAction}" selected="false" />
    </wiki:Permission>

    <c:set var="tabAttachTitle"><%=attTitle%></c:set>
    <c:set var="attachAction" value="<%=c.getURL(WikiContext.VIEW, c.getPage().getName())%>" />
    <c:if test="${not empty param.attach}">
      <view:tab label="${tabAttachTitle}" action="${'#'}" selected="true" />
    </c:if>
    <c:if test="${empty param.attach}">
      <view:tab label="${tabAttachTitle}" action="${attachAction}&attach=true" selected="false" />
    </c:if>


    <c:set var="tabInfoTitle"><%=LocaleSupport.getLocalizedMessage(pageContext, "info.tab")%></c:set>
    <c:set var="infoAction" value="<%=c.getURL(WikiContext.INFO, c.getPage().getName())%>" />
    <view:tab label="${tabInfoTitle}" action="${infoAction}" selected="false" />
  </wiki:PageExists>

</view:tabs>

<view:frame>


  <c:if test="${empty param.attach}">
    <wiki:Include page="PageTab.jsp" />
    <wiki:PageType type="attachment">
      <div class="information"><fmt:message key="info.backtoparentpage">
        <fmt:param>
          <wiki:LinkToParent>
            <wiki:ParentPageName />
          </wiki:LinkToParent>
        </fmt:param>
      </fmt:message></div>
      <div style="overflow: hidden;"><wiki:Translate>[<%=c.getPage().getName()%>]</wiki:Translate></div>
    </wiki:PageType>
  </c:if>

  <wiki:PageExists>

    <wiki:PageType type="page">
      <c:if test="${not empty param.attach}">
        <wiki:Include page="AttachmentTab.jsp" />
      </c:if>
    </wiki:PageType>

  </wiki:PageExists>

</view:frame>
