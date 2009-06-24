<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>

<%

String topicId		= request.getParameter("TopicId");
String formAction 	= "ExportTopic";
if (!StringUtil.isDefined(topicId) || "0".equals(topicId))
	formAction = "ExportComponent";

Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button exportButton = gef.getFormButton(resources.getString("GML.export"), "javascript:onClick=validateForm();", false);

%>

<html>
<head><title><%=resources.getString("GML.popupTitle")%></title>
<%
out.println(gef.getLookStyleSheet());
%>
<script language="javaScript">
function validateForm() {
	var obj = document.getElementById("Processing");
	if (obj != null)
		obj.style.visibility = "visible";
	obj = document.getElementById("ImgProcessing");
	if (obj != null)
		obj.style.visibility = "visible";
	document.frm_exportComponent.submit();	
}
</script>
</head>
<body>
<%
  Window window = gef.getWindow();
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(spaceLabel);
  browseBar.setComponentName(componentLabel);
  browseBar.setPath(resources.getString("kmelia.ExportTitle"));
	
  Frame frame = gef.getFrame();
  Board board = gef.getBoard();

  out.println(window.printBefore());
  out.println(frame.printBefore());
  out.println(board.printBefore());
%>
	<TABLE CELLPADDING="5" CELLSPACING="0" BORDER="0" WIDTH="100%">
		<tr>
			<td align="center"><b><%=resources.getString("kmelia.ExportTitle")%></b></td>
		</tr>
		<tr>
		  	<td align="center"><%=resources.getString("kmelia.WarningExport")%></td>
		</tr>
		<tr>
			<td>
				<div align="center" id="ImgProcessing" style="visibility:hidden"><img src="<%=resources.getIcon("kmelia.progress")%>" border="0"></div>
			</td>
		</tr>
		<tr>
			<td>
				<div align="center" id="Processing" style="visibility:hidden"><b><%=resources.getString("kmelia.ExportProcessing")%></b></div>
			</td>				
		</tr>
	</table>
<%
	out.println(board.printAfter());
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(exportButton);
    buttonPane.addButton(cancelButton);
    out.println("<br/><center>"+buttonPane.print()+"</center>");
    
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<form name="frm_exportComponent" action="<%=formAction%>" Method="POST">
	<input type="hidden" name="TopicId" value="<%=topicId%>"/>
</form>
</body>
</html>