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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.auth.*" %>
<%@ page import="com.ecyrd.jspwiki.ui.progress.*" %>
<%@ page import="com.ecyrd.jspwiki.auth.permissions.*" %>
<%@ page import="java.security.Permission" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<view:setBundle basename="templates.default"/>
<%
  int MAXATTACHNAMELENGTH = 30;
  WikiContext c = WikiContext.findContext(pageContext);
  String progressId = c.getEngine().getProgressManager().getNewProgressIdentifier();
%>

<div id="addattachment">
<h3><fmt:message key="attach.add"/></h3>
<wiki:Permission permission="upload">
  <wiki:Permission permission="upload">
  <form action="<wiki:Link jsp='attach' format='url' absolute='true'><wiki:Param name='progressid' value='<%=progressId%>'/></wiki:Link>"
         class="wikiform"
            id="uploadform"
        method="post"
       enctype="multipart/form-data" accept-charset="<wiki:ContentEncoding/>"
      onsubmit="return Wiki.submitUpload(this, '<%=progressId%>');" >
    <table>
    <tr>
      <td colspan="2"><div class="formhelp"><fmt:message key="attach.add.info" /></div></td>
    </tr>
    <tr>
      <td><label for="attachfilename"><fmt:message key="attach.add.selectfile"/></label></td>
      <td><input type="file" name="content" id="attachfilename" size="60"/></td>
    </tr>
    <tr>
      <td><label for="attachnote"><fmt:message key="attach.add.changenote"/></label></td>
      <td><input type="text" name="changenote" id="attachnote" maxlength="80" size="60" />
    <input type="hidden" name="nextpage" value="<wiki:UploadLink format="url"/>" /></td>
    </tr>

   <tr>
      <td></td>
      <td>
        <input type="hidden" name="page" value="<wiki:Variable var="pagename"/>" />
        <input type="submit" name="upload" id="upload" value="<fmt:message key='attach.add.submit'/>" />
        <input type="hidden" name="action" value="upload" />
        <div id="progressbar"><div class="ajaxprogress"></div></div>
      </td>
    </tr>
    </wiki:Permission>

    </table>
  </form>
  <wiki:Messages div="error" />
</wiki:Permission>
<wiki:Permission permission="!upload">
<div class="formhelp"><fmt:message key="attach.add.permission"/></div>
</wiki:Permission>
</div>

<wiki:HasAttachments>

<h3><fmt:message key="attach.list"/></h3>

  <%--<small><fmt:message key="attach.listsubtitle"/></small>--%>

  <wiki:Permission permission="delete">
    <%-- hidden delete form --%>
    <form action="tbd"
           class="wikiform"
              id="deleteForm" style="display:none;"
          method="post" accept-charset="<wiki:ContentEncoding />"
        onsubmit="return(confirm('<fmt:message key="attach.deleteconfirm"/>') && Wiki.submitOnce(this) );" >

      <input id="delete-all" name="delete-all" type="submit" value="Delete" />

    </form>
  </wiki:Permission>

  <div class="zebra-table"><div class="slimbox-img sortable">
  <table class="wikitable">
    <tr>
      <th><fmt:message key="info.attachment.type"/></th>
      <th><fmt:message key="info.attachment.name"/></th>
      <th><fmt:message key="info.size"/></th>
      <th><fmt:message key="info.version"/></th>
      <th><fmt:message key="info.date"/></th>
      <th><fmt:message key="info.author"/></th>
      <wiki:Permission permission="delete"><th><fmt:message key="info.actions"/></th></wiki:Permission>
      <th class="changenote"><fmt:message key="info.changenote"/></th>
    </tr>

    <wiki:AttachmentsIterator id="att">
    <%
      String name = att.getFileName();
      int dot = name.lastIndexOf(".");
      String attachtype = ( dot != -1 ) ? name.substring(dot+1) : "";

      String sname = name;
      if( sname.length() > MAXATTACHNAMELENGTH ) sname = sname.substring(0,MAXATTACHNAMELENGTH) + "...";
    %>
    <tr>
      <td><div id="attach-<%= attachtype %>" class="attachtype"><%= attachtype %></div></td>
      <td><wiki:LinkTo title="<%= name %>" ><%= sname %></wiki:LinkTo></td>
      <td style="white-space:nowrap;text-align:right;">
        <fmt:formatNumber value='<%=Double.toString(att.getSize()/1000.0)%>' groupingUsed='false' maxFractionDigits='1' minFractionDigits='1'/>&nbsp;<fmt:message key="info.kilobytes"/>
      </td>
      <td style="text-align:center;">
        <a href="<wiki:PageInfoLink format='url' />" title="<fmt:message key='attach.moreinfo.title'/>"><wiki:PageVersion /></a>
      </td>
	  <td style="white-space:nowrap;"><fmt:formatDate value="<%= att.getLastModified() %>" pattern="${prefs['DateFormat']}" /></td>
      <td><wiki:Author /></td>
      <wiki:Permission permission="delete">
      <td>
          <input type="button"
                value="<fmt:message key='attach.delete'/>"
                  src="<wiki:Link format='url' context='<%=WikiContext.DELETE%>' />"
              onclick="$('deleteForm').setProperty('action',this.src); $('delete-all').click();" />
      </td>
      </wiki:Permission>
      <td class="changenote">
      <%
         String changeNote = (String)att.getAttribute(WikiPage.CHANGENOTE);
         if( changeNote != null ) {
         %><%=changeNote%><%
         }
      %>
      </td>
    </tr>
    </wiki:AttachmentsIterator>

  </table>
  </div></div>

</wiki:HasAttachments>
