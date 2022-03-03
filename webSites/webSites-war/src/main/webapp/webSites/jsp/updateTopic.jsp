<%--

    Copyright (C) 2000 - 2022 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.persistence.jdbc.DBUtil"%>

<%@ include file="checkScc.jsp" %>

<%
String name = "";
String description = "";

//R�cup�ration des param�tres
String id = org.owasp.encoder.Encode.forUriComponent(request.getParameter("ChildId"));
String path = request.getParameter("Path");

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);


NodeDetail folderDetail = (NodeDetail) request.getAttribute("CurrentFolder");

if (folderDetail != null) {
    name = folderDetail.getName();
    description = folderDetail.getDescription();
}

%>

<!-- updateTopic -->

<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<view:looknfeel withCheckFormScript="true"/>
<script LANGUAGE="JavaScript" TYPE="text/javascript">
function isCorrect(nom) {

    if (nom.indexOf("&")>-1 || nom.indexOf(";")>-1 ||
        nom.indexOf(":")>-1 || nom.indexOf("+")>-1 ||
        nom.indexOf("%")>-1 || nom.indexOf("#")>-1) {
        return false;
    }
    return true;

}

/************************************************************************************/

function sendData() {
	ifCorrectFormExecute(function() {
		window.opener.document.liste.Action.value = "Update";
		window.opener.document.liste.ChildId.value = document.topicForm.ChildId.value;
		window.opener.document.liste.Name.value = stripInitialWhitespace(document.topicForm.Name.value);
		window.opener.document.liste.description.value = stripInitialWhitespace(document.topicForm.description.value);
		window.opener.document.liste.submit();
		window.close();
      });
}

/************************************************************************************/
function ifCorrectFormExecute(callback) {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.topicForm.Name.value);
     if (isWhitespace(title)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++;
     }
     if (! isCorrect(title)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("MustNotContainSpecialChar")%>\n  <%=resources.getString("Char6")%>\n";
       errorNb++;
     }
     switch(errorNb) {
        case 0 :
            callback.call(this);
            break;
        case 1 :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
            jQuery.popup.error(errorMsg);
            break;
        default :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
            jQuery.popup.error(errorMsg);
     }
}
</script>
</HEAD>
<BODY onload="document.topicForm.Name.focus()">
<%
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel);
    browseBar.setPath(resources.getString("FolderUpdateTitle"));

    //Le cadre
    Frame frame = gef.getFrame();

	//Le board
	Board board = gef.getBoard();

    //D�but code
    out.println(window.printBefore());
    out.println(frame.printBefore());
	out.print(board.printBefore());

%>
    <TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
	<FORM NAME="topicForm">
    <TR>
        <TD class="txtlibform"><%=resources.getString("GML.path")%> : </TD>
            <TD valign="top"><%
                          out.println(Encode.javaStringToHtmlString(path));
                             %>
                        <input type="hidden" name="ChildId" value="<%=id%>" maxlength="<%=DBUtil.getTextFieldLength()%>"></TD>
        </TR>

    <TR>
        <TD class="txtlibform"><%=resources.getString("GML.name")%> : </TD>
            <TD><input type="text" name="Name" value="<%=Encode.javaStringToHtmlString(name)%>" size="60" maxlength="50">
                &nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5" maxlength="50"></TD>
        </TR>

    <TR>
        <TD class="txtlibform"><%=resources.getString("GML.description")%> : </TD>
            <TD><input type="text" name="description" value="<%=Encode.javaStringToHtmlString(description)%>" size="60" maxlength="50"></TD>
        </TR>
          <TR>
            <TD colspan="2">(<img border="0" src="<%=mandatoryField%>" width="5" height="5">
              : <%=resources.getString("GML.requiredField")%>)</TD>
          </TR>
	</FORM>
    </TABLE>
<!-- BUTTONS -->
<%
    //fin du code
	out.print(board.printAfter());
    out.println(frame.printMiddle());

    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);

	out.println("<br><center>"+buttonPane.print()+"</center><br>");

    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
<!-- END BUTTONS -->
</BODY>
</HTML>