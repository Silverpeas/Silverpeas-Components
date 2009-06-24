<%@ include file="checkKmelia.jsp" %>

<%
//Récupération des paramètres
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