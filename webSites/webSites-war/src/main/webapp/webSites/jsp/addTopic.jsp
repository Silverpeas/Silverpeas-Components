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

<%@ include file="checkScc.jsp" %>

<%

SilverTrace.info("websites", "JSPaddTopic", "root.MSG_GEN_ENTER_METHOD");

//Recuperation des parametres
String fatherId = org.owasp.encoder.Encode.forUriComponent(request.getParameter("Id"));
String path = request.getParameter("Path");
String action = request.getParameter("Action");

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);

%>

<!-- addTopic -->

<%
if (action.equals("View")) {
%>
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
            document.topicDetailForm.Action.value = "Add";
            document.topicDetailForm.Name.value = stripInitialWhitespace(document.topicForm.Name.value);
            document.topicDetailForm.description.value = stripInitialWhitespace(document.topicForm.description.value);
            document.topicDetailForm.submit();
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
     switch(errorNb)
     {
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
<view:browseBar path='<%=resources.getString("FolderCreationTitle")%>'/>
<view:window popup="true">
<view:frame>
<view:board>
    <TABLE CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
	<FORM NAME="topicForm">
    <TR>
        <TD class="txtlibform"><%=resources.getString("GML.path")%> : </TD>
            <TD valign="top"><%
                           out.println(Encode.javaStringToHtmlString(path));
                            %></TD>
    </TR>

    <TR>
        <TD class="txtlibform"><%=resources.getString("GML.name")%> : </TD>
            <TD valign="top">
                <input type="text" name="Name" value="" size="60" maxlength="50">
                &nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></TD>
        </TR>


    <TR>
        <TD class="txtlibform"><%=resources.getString("GML.description")%> : </TD>
            <TD valign="top">
                <input type="text" name="description" value="" size="60" maxlength="50"/></TD>
        </TR>

        <TR>
            <TD colspan="2">(<img border="0" src="<%=mandatoryField%>" width="5" height="5">
              : <%=resources.getString("GML.requiredField")%>)</TD>
      </TR>
	</FORM>
    </TABLE>
</view:board>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
    out.println(buttonPane.print());
%>
</view:frame>
</view:window>
<FORM NAME="topicDetailForm" ACTION="AddTopic" METHOD=POST>
  <input type="hidden" name="Action">
  <input type="hidden" name="Id" value="<%=fatherId%>">
  <input type="hidden" name="Name">
  <input type="hidden" name="description">
</FORM>
</BODY>
</HTML>
<% } //End View

else if (action.equals("Add")) {
%>

	<HTML>
      <HEAD>
      <script language="Javascript">
          function closeAndReplace() {
            window.opener.location.replace("organize.jsp?Action=Search&Id=<%=fatherId%>");
            window.close();
          }
      </script>
      </HEAD>
      <BODY onLoad="closeAndReplace()">
      </BODY>
      </HTML>
<%  }  %>