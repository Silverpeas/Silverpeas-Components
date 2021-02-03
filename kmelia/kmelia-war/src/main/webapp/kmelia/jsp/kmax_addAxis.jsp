<%--

    Copyright (C) 2000 - 2021 Silverpeas

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

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.i18n.I18NHelper" %>
<%@ include file="checkKmelia.jsp" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
%>
<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<view:looknfeel withCheckFormScript="true"/>
<script LANGUAGE="JavaScript" TYPE="text/javascript">
function sendData() {
  var errorMsg = "";
  var errorNb = 0;
  var title = stripInitialWhitespace(document.axisForm.Name.value);
  var description = stripInitialWhitespace(document.axisForm.Description.value);
  if (isWhitespace(title)) {
   errorMsg+="  - <%=kmeliaScc.getString("TheField")%> '<%=kmeliaScc.getString("AxisTitle")%>' <%=kmeliaScc.getString("MustContainsText")%>\n";
   errorNb++;
  }
  switch(errorNb)
  {
    case 0 :
        document.axisForm.action = "KmaxAddAxis";
        document.axisForm.submit();
        break;
    case 1 :
        errorMsg = "<%=kmeliaScc.getString("ThisFormContains")%> 1 <%=kmeliaScc.getString("Error")%> : \n" + errorMsg;
        jQuery.popup.error(errorMsg);
        break;
    default :
        errorMsg = "<%=kmeliaScc.getString("ThisFormContains")%> " + errorNb + " <%=kmeliaScc.getString("Errors")%> :\n" + errorMsg;
        jQuery.popup.error(errorMsg);
  }
  return result;
}
</script>
</HEAD>
<BODY>
<view:browseBar extraInformations='<%=kmeliaScc.getString("AxisCreationTitle")%>'/>
<view:window popup="true">
<view:frame>
<view:board>
	<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0>
		<FORM NAME="axisForm" method="POST">
				<%=I18NHelper.getFormLine(resources, null, kmeliaScc.getLanguage())%>
			  <TR><TD class="txtlibform"><%=kmeliaScc.getString("AxisTitle")%> :</TD>
		      <TD><input type="text" name="Name" value="" size="60" maxlength="60"> <img border="0" src="<%=mandatoryField%>" width="5" height="5"></TD></TR>
		  <TR><TD class="txtlibform"><%=kmeliaScc.getString("AxisDescription")%> :</TD>
		      <TD><input type="text" name="Description" value="" size="60" maxlength="200"></TD></TR>
		  <TR><TD colspan="2">( <img border="0" src="<%=mandatoryField%>" width="5" height="5"> : <%=kmeliaScc.getString("ChampsObligatoires")%> )</TD></TR>
		</FORM>
	</TABLE>
</view:board>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
 	out.println("<center><br>");
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