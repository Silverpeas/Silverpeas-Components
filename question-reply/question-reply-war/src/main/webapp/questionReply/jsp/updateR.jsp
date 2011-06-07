<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@ page import="java.util.*"%>

<%@ include file="checkQuestionReply.jsp" %>
<%
	Reply reply = (Reply) request.getAttribute("reply");
	String title = EncodeHelper.javaStringToHtmlString(reply.getTitle());
	String content = EncodeHelper.javaStringToHtmlString(reply.loadWysiwygContent());
	String date = resource.getOutputDate(reply.getCreationDate());
	String id = reply.getPK().getId();
	String creator = EncodeHelper.javaStringToHtmlString(reply.readCreatorName());
	int status = reply.getPublicReply();
%>

<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel />
<script type="text/javascript" src="<c:url value='/wysiwyg/jsp/FCKeditor/fckeditor.js'/>"></script>
<script language="JavaScript">
<!--
function isCorrectForm() {
     	var errorMsg = "";
     	var errorNb = 0;

	var title = document.forms[0].title.value;
	var content = document.forms[0].content;

	if (isWhitespace(title)) {
           errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
           errorNb++;
        }

     	if (!isValidTextArea(content)) {
     		errorMsg+="  - '<%=resource.getString("GML.description")%>' <%=resource.getString("questionReply.containsTooLargeText")+resource.getString("questionReply.nbMaxTextArea")+resource.getString("questionReply.characters")%>\n";
           	errorNb++;
		}

     switch(errorNb)
     {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;

}
function save()
{
	if (isCorrectForm())
		document.forms[0].submit();
}
//-->
</SCRIPT>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF" onLoad="document.forms[0].title.focus();">

<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setPath(resource.getString("questionReply.modif"));

	tabbedPane.addTab(resource.getString("GML.head"), "#", true, false);
  tabbedPane.addTab(resource.getString("GML.attachments"), "ViewAttachments", false);

	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>

<center>
<table CELLPADDING=5 width="100%">
	<FORM METHOD=POST NAME="myForm" ACTION="EffectiveUpdateR">
	<tr>
		<td class="txtlibform"><%=resource.getString("questionReply.reponse")%> :</td>
		<td><input type="text" name="title" size="120" maxlength="100" value="<%=title%>"><img src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5"></td>
	</tr>
	<tr valign="top">
		<td class="txtlibform"><%=resource.getString("GML.description")%> :</td>
		<td><textarea cols="120" rows="5" name="content" id="content"><%=content%></textarea></td>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.date")%> :</td>
		<td><%=date%></td>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.publisher")%> :</td>
		<td><%=creator%></td>
	</tr>
	<tr>
		<td colspan=2><span class="txt">(<img src="<%=resource.getIcon("questionReply.mandatory")%>" width="5" height="5"> : <%=resource.getString("GML.requiredField")%>)</span></td>
	</tr>
	</FORM>
</table>
<% out.println(board.printAfter()); %>
<br>
<CENTER>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:save();", false));
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "ConsultQuestionQuery", false));
    out.println(buttonPane.print());
%>
</CENTER>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<script type="text/javascript">
  <fmt:message key='configFile' var='configFile'/>
  <c:if test="${configFile eq '???configFile???'}">
  <c:url value="/wysiwyg/jsp/javaScript/myconfig.js" var="configFile"/>
  </c:if>
  var oFCKeditor = new FCKeditor('content');
  oFCKeditor.Width = "500";
  oFCKeditor.Height = "300";
  oFCKeditor.BasePath = "<c:url value='/wysiwyg/jsp/FCKeditor/'/>";
  oFCKeditor.DisplayErrors = true;
  oFCKeditor.Config["AutoDetectLanguage"] = false;
  oFCKeditor.Config["DefaultLanguage"] = "<c:out value='${language}'/>";
  oFCKeditor.Config["CustomConfigurationsPath"] = "<c:out value='${configFile}'/>"
  oFCKeditor.ToolbarSet = 'questionreply';
  oFCKeditor.Config["ToolbarStartExpanded"] = true;
  oFCKeditor.ReplaceTextarea();
</script>
</body>
</html>