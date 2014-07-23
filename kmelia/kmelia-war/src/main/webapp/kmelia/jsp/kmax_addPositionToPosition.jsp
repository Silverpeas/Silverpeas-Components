<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.DBUtil"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>

<%@ include file="checkKmelia.jsp" %>

<%
String action = request.getParameter("Action");
String axisId = request.getParameter("AxisId");
String translation = request.getParameter("Translation");

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";
ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(kmeliaScc.getLanguage());

Button cancelButton = gef.getFormButton(generalMessage.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button validateButton = gef.getFormButton(generalMessage.getString("GML.validate"), "javascript:onClick=sendData()", false);

%>
<HTML>
<HEAD>
<TITLE><%=kmeliaScc.getString("ComponentCreationTitle")%></TITLE>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<script LANGUAGE="JavaScript" TYPE="text/javascript">
function sendData() {
  if (isCorrectForm()) {
        document.axisForm.action = "KmaxAddPosition";
        document.axisForm.submit();
        window.close();
  }
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.axisForm.Name.value);
     var description = stripInitialWhitespace(document.axisForm.Description.value);
     if (isWhitespace(title)) {
       errorMsg+="  - <%=kmeliaScc.getString("TheField")%> '<%=kmeliaScc.getString("ComponentTitle")%>' <%=kmeliaScc.getString("MustContainsText")%>\n";
       errorNb++;
     }
	if (!isValidTextArea(document.axisForm.Description)) {
          errorMsg+="  - <%=kmeliaScc.getString("TheField")%> '<%=kmeliaScc.getString("ComponentDescription")%>' <%=kmeliaScc.getString("ContainsTooLargeText")%> <%=DBUtil.getTextAreaLength()%> <%=kmeliaScc.getString("Characters")%>\n";
          errorNb++;
    }
     switch(errorNb)
     {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=kmeliaScc.getString("ThisFormContains")%> 1 <%=kmeliaScc.getString("Error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=kmeliaScc.getString("ThisFormContains")%> " + errorNb + " <%=kmeliaScc.getString("Errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}
</script>
</HEAD>
<BODY>
<view:browseBar extraInformations='<%=kmeliaScc.getString("ComponentCreationTitle")%>'/>
<view:window popup="true">
<view:frame>
<view:board>
	<FORM NAME="axisForm" method="POST" target="MyMain">
		<input type="hidden" name="Translation" value="<%=translation%>">
		<TABLE cellPadding="5" cellSpacing="0" border="0">
			<%=I18NHelper.getFormLine(resources, null, kmeliaScc.getLanguage())%>
		  <TR><TD class="txtlibform"><%=kmeliaScc.getString("ComponentTitle")%> :</TD>
		      <TD><input type="hidden" name="AxisId" value="<%=axisId%>"><input type="text" id="nodeNameValue" name="Name" value="" size="60" maxlength="60"> <img border="0" src="<%=mandatoryField%>" width="5" height="5"></TD></TR>
		  <TR><TD class="txtlibform"><%=kmeliaScc.getString("ComponentDescription")%> :</TD>
		      <TD><input type="text" id="nodeDesc" name="Description" value="" size="60" maxlength="200"></TD></TR>
		  <TR><TD colspan="2">( <img border="0" src="<%=mandatoryField%>" width="5" height="5"> = <%=kmeliaScc.getString("ChampsObligatoires")%> )</TD></TR>
		</TABLE>
	</FORM>
</view:board>
<%
ButtonPane buttonPane = gef.getButtonPane();
buttonPane.addButton(validateButton);
buttonPane.addButton(cancelButton);
out.println("<center>");
out.println(buttonPane.print());
out.println("</center>");
%>
</view:frame>
</view:window>
</BODY>
<script language="javascript">
	document.axisForm.Name.focus();
</script>
</HTML>