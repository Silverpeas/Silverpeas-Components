<%--

    Copyright (C) 2000 - 2009 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkKmelia.jsp" %>

<%
//R�cup�ration des param�tres
PublicationDetail pubDetail 	= (PublicationDetail) request.getAttribute("PublicationToRefuse");
String			  currentLang 	= (String) request.getAttribute("Language");

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
%>
<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script LANGUAGE="JavaScript" TYPE="text/javascript">
function sendData() {
      if (isCorrectForm()) {
			window.opener.document.refusalForm.Motive.value = stripInitialWhitespace(document.refusalForm.Motive.value);
			window.opener.document.refusalForm.submit();
			window.close();
      }
}

function isCorrectForm() {
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
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel);
    browseBar.setPath(kmeliaScc.getString("PublicationRefused"));
	
    Frame frame = gef.getFrame();

    out.println(window.printBefore());
    out.println(frame.printBefore());
%>

<FORM NAME="refusalForm" Action="Unvalidate" Method="POST">
<TABLE ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr>
		<td>
		<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
  <TR><TD class="txtlibform"><%=kmeliaScc.getString("PublicationName")%> :</TD>
      <TD valign="top"><%=Encode.javaStringToHtmlString(pubDetail.getName(currentLang))%></TD>
  <TR><TD class="txtlibform" valign=top><%=kmeliaScc.getString("RefusalMotive")%> :</TD>
      <TD><textarea name="Motive" rows="5" cols="60"></textarea>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"> </TD></TR>
  <TR><TD colspan="2">( <img border="0" src="<%=mandatoryField%>" width="5" height="5"> : <%=resources.getString("GML.requiredField")%> )</TD></TR>
        </TABLE>	
		</td>
	</tr>
</TABLE>

</FORM>
<%
    out.println(frame.printMiddle());

    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
	
	String bodyPart ="<center>";
	bodyPart += buttonPane.print();
	bodyPart +="</center><br>";

	out.println(bodyPart);

	out.println(frame.printAfter());
	out.println(window.printAfter());	
%>
</BODY>
</HTML>