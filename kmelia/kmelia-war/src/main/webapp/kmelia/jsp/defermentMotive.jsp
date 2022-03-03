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
<%@ include file="checkKmelia.jsp" %>

<%
//R�cup�ration des param�tres
PublicationDetail pubDetail 	= (PublicationDetail) request.getAttribute("PublicationToSuspend");
String			  currentLang 	= (String) request.getAttribute("Language");

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

Button cancelButton 	= gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button validateButton 	= gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
%>
<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<view:looknfeel withCheckFormScript="true"/>
<script LANGUAGE="JavaScript" TYPE="text/javascript">
function sendData() {
     var errorMsg = "";
     var errorNb = 0;
     var motive = stripInitialWhitespace(document.refusalForm.Motive.value);
     if (isWhitespace(motive)) {
       errorMsg+="  - '<%=kmeliaScc.getString("RefusalMotive")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++; 
     }
     switch(errorNb)
     {
        case 0 :
            window.opener.document.defermentForm.Motive.value = stripInitialWhitespace(document.refusalForm.Motive.value);
            window.opener.document.defermentForm.submit();
            window.close();
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
<BODY>
<view:browseBar path='<%=kmeliaScc.getString("kmelia.PublicationSuspended")%>'/>
<view:window popup="true">
<view:frame>
<view:board>
<FORM NAME="refusalForm" Action="SuspendPublication" Method="POST">
<TABLE ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr>
		<td>
		<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
  <TR><TD class="txtlibform"><%=resources.getString("GML.publication")%> :</TD>
      <TD valign="top"><%=Encode.javaStringToHtmlString(pubDetail.getName(currentLang))%></TD>
  <TR><TD class="txtlibform" valign=top><%=kmeliaScc.getString("RefusalMotive")%> :</TD>
      <TD><textarea name="Motive" rows="5" cols="60"></textarea>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"> </TD></TR>
  <TR><TD colspan="2">( <img border="0" src="<%=mandatoryField%>" width="5" height="5"> : <%=resources.getString("GML.requiredField")%> )</TD></TR>
        </TABLE>	
		</td>
	</tr>
</TABLE>
</FORM>
</view:board>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
	
	out.println("<br><center>"+buttonPane.print()+"</center>");
%>
</view:frame>
</view:window>
</BODY>
</HTML>