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
<%@ page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>
<%@ page import="com.ecyrd.jspwiki.*"%>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*"%>
<fmt:setLocale value="${userLanguage}" />
<view:setBundle basename="templates.default" />
<%
  WikiContext c = WikiContext.findContext(pageContext);
            int attCount = c.getEngine().getAttachmentManager()
                    .listAttachments(c.getPage()).size();
            String attTitle = LocaleSupport.getLocalizedMessage(pageContext,
                    "attach.tab");
            if (attCount != 0)
                attTitle += " (" + attCount + ")";
%>

<view:tabs>
  <c:set var="tabContentTitle"><%=LocaleSupport.getLocalizedMessage(pageContext, "edit.tab.edit")%></c:set>
  <view:tab label="${tabContentTitle}" action="javascript: hideHelp()" selected="true" />
  <wiki:Permission permission="comment">
    <c:set var="tabCommentsTitle"><%=LocaleSupport.getLocalizedMessage(pageContext,
                      "comment.tab.addcomment")%></c:set>
    <c:set var="commentsAction" value="<%=c.getURL(WikiContext.COMMENT, c.getPage().getName())%>" />
    <view:tab label="${tabCommentsTitle}" action="${commentsAction}" selected="false" />
  </wiki:Permission>
  <wiki:PageExists>
    <c:set var="tabAttachTitle"><%=attTitle%></c:set>
    <c:set var="attachAction" value="<%=c.getURL(WikiContext.VIEW, c.getPage().getName())%>" />
    <view:tab label="${tabAttachTitle}" action="${attachAction}&attach=true" selected="false" />
    <c:set var="tabInfoTitle"><%=LocaleSupport.getLocalizedMessage(pageContext, "info.tab")%></c:set>
    <c:set var="infoAction" value="<%=c.getURL(WikiContext.INFO, c.getPage().getName())%>" />
    <view:tab label="${tabInfoTitle}" action="${infoAction}" selected="false" />
  </wiki:PageExists>
</view:tabs>
<view:frame>

  <div id="helpZone" style="display: none;"><wiki:InsertPage page="EditPageHelp" /> <wiki:NoSuchPage page="EditPageHelp">
      <div class="error"><fmt:message key="comment.edithelpmissing">
          <fmt:param>
            <wiki:EditLink page="EditPageHelp">EditPageHelp</wiki:EditLink>
          </fmt:param>
        </fmt:message></div>
    </wiki:NoSuchPage></div>

  <div id="editZone">
    <wiki:CheckLock mode="locked" id="lock">
      <div class="error">
        <fmt:message key="edit.locked">
          <fmt:param>
            <c:out value="${lock.locker}" />
          </fmt:param>
          <fmt:param>
            <c:out value="${lock.timeLeft}" />
          </fmt:param>
        </fmt:message>
      </div>
    </wiki:CheckLock>
    <wiki:CheckVersion mode="notlatest">
      <div class="warning">
        <fmt:message key="edit.restoring">
          <fmt:param>
            <wiki:PageVersion />
          </fmt:param>
        </fmt:message>
      </div>
    </wiki:CheckVersion>
    <wiki:Editor />
  </div>
</view:frame>

<script language="javascript">
  function getStyleObject(objectId) {
    // cross-browser function to get an object's style object given its id
    if (document.getElementById && document.getElementById(objectId)) {
      // W3C DOM
      return document.getElementById(objectId).style;
    } else if (document.all && document.all(objectId)) {
      // MSIE 4 DOM
      return document.all(objectId).style;
    } else if (document.layers && document.layers[objectId]) {
      // NN 4 DOM.. note: this won't find nested layers
      return document.layers[objectId];
    } else {
      return false;
    }
  }

  function showHelp() {
    var helpStyle = getStyleObject('helpZone');
    var editStyle = getStyleObject('editZone');

    editStyle.display = 'none';
    helpStyle.display = 'block';
  }

  function hideHelp() {
    var helpStyle = getStyleObject('helpZone');
    var editStyle = getStyleObject('editZone');

    helpStyle.display = 'none';
    editStyle.display = 'block';
  }
</script>
