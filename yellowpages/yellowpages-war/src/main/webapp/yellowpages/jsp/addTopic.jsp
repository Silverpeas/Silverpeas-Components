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
<%@ include file="checkYellowpages.jsp" %>
<%@ include file="topicReport.jsp.inc" %>
<%@ include file="tabManager.jsp.inc" %>

<%
//Recuperation des parametres
TopicDetail CurrentTopic=yellowpagesScc.getCurrentTopic();
String fatherId = request.getParameter("Id");
String path = request.getParameter("Path");

Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
%>
<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<script LANGUAGE="JavaScript" TYPE="text/javascript">

function sendData() {
      if (isCorrectForm()) {
            window.document.topicDetailForm.Name.value = stripInitialWhitespace(window.document.topicForm.Name.value);
            window.document.topicDetailForm.Description.value = stripInitialWhitespace(window.document.topicForm.Description.value);
            window.document.topicDetailForm.submit();
      }
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(window.document.topicForm.Name.value);
     var description = stripInitialWhitespace(window.document.topicForm.Description.value);
     if (isWhitespace(title)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=yellowpagesScc.getString("TopicTitle")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++; 
     }
     switch(errorNb)
     {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}
</script>
</HEAD>

<BODY>

<%
Window window = gef.getWindow();
Frame frame = gef.getFrame();
Board board = gef.getBoard();
BrowseBar browseBar = window.getBrowseBar();

browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel);
browseBar.setPath(resources.getString("TopicCreationTitle"));

out.println(window.printBefore());
displayAllOperations(resources, "", gef, "ViewDesc", out);
out.println(frame.printBefore());
out.println(board.printBefore());

%>

<center>
<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%">
<FORM NAME="topicForm">
    <tr>
        <td NOWRAP>
            <table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
                <tr>            
                    <td valign="baseline" align=left  class="txtlibform">
                        <%=yellowpagesScc.getString("TopicTitle")%> :
                    </td>
                    <td align=left valign="baseline">
                        <input type="text" name="Name" value="" size="60" maxlength="60">&nbsp;<img border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>" width="5" height="5"> 
                    </td>
                </tr>
                <tr>            
                    <td valign="baseline" align=left  class="txtlibform">
                        <%=resources.getString("GML.description")%> :&nbsp;
                    </td>
                    <td align=left valign="baseline">
                        <input type="text" name="Description" value="" size="60" maxlength="200">
                    </td>
                </tr>
                <tr> 
                    <td colspan="2">(<img border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>" width="5" height="5"> 
              : <%=resources.getString("GML.requiredField")%>)
          </td>
                </tr>
            </table>
        </td>
    </tr>
    </FORM>
</table>

<!-- BUTTONS -->
<br>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
		out.println(board.printAfter());
    out.println("<br><center>"+buttonPane.print()+"</center><br>");
    out.println(frame.printAfter());
%>
<!-- END BUTTONS -->
</center>
<% out.println(window.printAfter()); %>
<FORM NAME="topicDetailForm" ACTION="add.jsp" METHOD=POST>
  <input type="hidden" name="Id" value="<%=fatherId%>">
  <input type="hidden" name="Path" value="<%=path%>">
  <input type="hidden" name="Name"><input type="hidden" name="Description">
</FORM>
</BODY>
<script language="javascript">
	document.topicDetailForm.Name.focus();
</script>
</HTML>