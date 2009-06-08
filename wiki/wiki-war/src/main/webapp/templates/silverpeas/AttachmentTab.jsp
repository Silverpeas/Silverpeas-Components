<%@ page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.auth.*" %>
<%@ page import="com.ecyrd.jspwiki.ui.progress.*" %>
<%@ page import="com.ecyrd.jspwiki.auth.permissions.*" %>
<%@ page import="java.security.Permission" %>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
 <view:setBundle bundle="${requestScope.resources.iconsBundle}" var="silverpeas_icons" />
<fmt:setBundle basename="templates.default"/>

<%
  int MAXATTACHNAMELENGTH = 30;
  WikiContext c = WikiContext.findContext(pageContext);
  String progressId = c.getEngine().getProgressManager().getNewProgressIdentifier();
%>

<div id="addattachment">
<h3><fmt:message key="attach.add"/></h3>
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
    <form action="tdb"
           class="wikiform"
              id="deleteAttForm" style="display:none;"
          method="post" accept-charset="<wiki:ContentEncoding />"
        onsubmit="return(confirm('<fmt:message key="attach.deleteconfirm"/> + Attachment') && Wiki.submitOnce(this) );" >
      <input id="delete-all" name="delete-all" type="submit" value="Delete" />

    </form>
  </wiki:Permission>

  <div ><div>
  <table class="tableArrayPane" width="100%" cellspacing="2" cellpadding="2" border="0">
    <tr>
      <th class="ArrayColumn"><fmt:message key="info.attachment.type"/></th>
      <th class="ArrayColumn"><fmt:message key="info.attachment.name"/></th>
      <th class="ArrayColumn"><fmt:message key="info.size"/></th>
      <th class="ArrayColumn"><fmt:message key="info.version"/></th>
      <th class="ArrayColumn"><fmt:message key="info.date"/></th>
      <th class="ArrayColumn"><fmt:message key="info.author"/></th>
      <wiki:Permission permission="delete"><th class="ArrayColumn"><fmt:message key="info.actions"/></th></wiki:Permission>
      <th class="ArrayColumn"><fmt:message key="info.changenote"/></th>
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
      <td class="ArrayCell"><view:mimeTypeIcon divId="<%="attach-" + attachtype%>" ><%= attachtype%></view:mimeTypeIcon></td>
      <td class="ArrayCell"><wiki:LinkTo title="<%= name %>" ><%= sname %></wiki:LinkTo></td>
      <td class="ArrayCell" style="white-space:nowrap;text-align:right;">
        <fmt:formatNumber value='<%=Double.toString(att.getSize()/1000.0)%>' groupingUsed='false' maxFractionDigits='1' minFractionDigits='1'/>&nbsp;<fmt:message key="info.kilobytes"/>
      </td>
      <td class="ArrayCell" style="text-align:center;">
        <a href="<wiki:PageInfoLink format='url' />" title="<fmt:message key='attach.moreinfo.title'/>"><wiki:PageVersion /></a>
      </td>
      <td class="ArrayCell" style="white-space:nowrap;"><fmt:formatDate value="<%= att.getLastModified() %>" /></td> 
      <td><wiki:Author /></td>
      <wiki:Permission permission="delete">
      <td class="ArrayCell" style="text-align:center; valign:middle;">     
          <fmt:message key="wiki.icons.deleteAttachment" bundle="${pageScope.silverpeas_icons}" var="deleteAttachmentIcon" />
          <a href="javascript:$('deleteAttForm').setProperty('action', '<wiki:Link format='url' context='<%=WikiContext.DELETE%>' />'); $('delete-all').click();" ><img src="<c:url value="${deleteAttachmentIcon}" />"  border="0" /></a>
      </td>
      </wiki:Permission>
      <td class="ArrayCell">
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