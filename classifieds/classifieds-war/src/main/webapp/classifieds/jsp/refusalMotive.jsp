<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ include file="check.jsp" %>

<%
ClassifiedDetail classified 	= (ClassifiedDetail) request.getAttribute("ClassifiedToRefuse");

String classifiedId = Integer.toString(classified.getClassifiedId());

Button cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData()", false);
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script LANGUAGE="JavaScript" TYPE="text/javascript">
function sendData() {
     if (isCorrectForm()) {
			document.refusalForm.submit();
			window.close();
      }
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var motive = stripInitialWhitespace(document.refusalForm.Motive.value);
     if (isWhitespace(motive)) {
       errorMsg+="  - '<%=resource.getString("classifieds.refusalMotive")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
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
</script>
</HEAD>

<BODY>
<%
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel);
    browseBar.setPath(resource.getString("Classifieds.refused"));

    out.println(window.printBefore());
    out.println(frame.printBefore());
%>

<FORM NAME="refusalForm" Action="RefusedClassified" Method="POST">
<TABLE>
	<tr>
		<td>
		<TABLE>
			<TR><TD class="txtlibform"><%=resource.getString("classifieds.number")%> :</TD>
				<td><%=Encode.javaStringToHtmlString(classifiedId)%>
					<input type="hidden" name="ClassifiedId" value="<%=Encode.javaStringToHtmlString(classifiedId)%>">    	  
				</TD>
			</TR>
		    <TR>
		  		<TD class="txtlibform"><%=resource.getString("GML.title")%> :</TD>
		      	<TD valign="top"><%=Encode.javaStringToHtmlString(classified.getTitle())%></TD>
		  	<TR><TD class="txtlibform" valign=top><%=resource.getString("classifieds.refusalMotive")%> :</TD>
		      	<TD><textarea name="Motive" rows="5" cols="60"></textarea>&nbsp;<img border="0" src="<%=resource.getIcon("classifieds.mandatory")%>" width="5" height="5"> </TD>
		    </TR>
		  	<TR><TD colspan="2">( <img border="0" src="<%=resource.getIcon("classifieds.mandatory")%>" width="5" height="5"> : <%=resource.getString("GML.requiredField")%> )</TD>
		  	</TR>
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