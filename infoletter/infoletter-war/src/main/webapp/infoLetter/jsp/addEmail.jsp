<%@ include file="check.jsp" %>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">
	function submitForm() {
		if (!isValidTextArea(document.addmails.newmails)) {
			window.alert("<%= resource.getString("infoLetter.makeMyDay") %>");
		} else {
			document.addmails.action = "NewMail";
			document.addmails.submit();
		} 
	}

	function cancelForm() {
	    document.addmails.action = "Emails";
	    document.addmails.submit();
	}
</script>
</head>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Accueil");
	browseBar.setPath("<a href=\"Accueil\">" + resource.getString("infoLetter.listParutions") + "</a> > " + resource.getString("infoLetter.externSubscribers")+ " > " + resource.getString("infoLetter.addExternSubscribers"));



	out.println(window.printBefore());
 
	//Instanciation du cadre avec le view generator

    
	out.println(frame.printBefore());	
	
%>

<% // Ici debute le code de la page %>

<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
<form name="addmails" action="" method="post">
	<tr> 
		<td>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
				<tr align=center> 

					<td  class="intfdcolor4" valign="top" align=left>
						<span class="txtlibform"><%=resource.getString("infoLetter.emailExternSubscribers")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="top" align=left>
					<textarea cols="49" rows="8" name="newmails"></textarea>&nbsp;<img src="<%=resource.getIcon("infoLetter.mandatory")%>" width="5" height="5">
					</td>
				</tr>
				<tr align=center>				 
					<td class="intfdcolor4" valign="baseline" align=left colspan=2><span class="txt">(<img src="<%=resource.getIcon("infoLetter.mandatory")%>" width="5" height="5"> : <%=resource.getString("infoLetter.infoComboEmail")%>)</span> 
					</td>
				</tr>
			</table>
		</td>
	</tr>
</form>
</table>

<br>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:submitForm();", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:cancelForm();", false));
    out.println(buttonPane.print());
%>
</center>


<% // Ici se termine le code de la page %>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>

