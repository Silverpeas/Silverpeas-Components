<%@ include file="check.jsp" %>
<html>
<head>
<%
	String zipUrl = (String) request.getAttribute("ZipURL");
	String name = (String) request.getAttribute("Name");
	Long sizeZipP = (Long) request.getAttribute("Size");
	
	Long sizeMaxP = (Long) request.getAttribute("SizeMax");
	
	long sizeZip = sizeZipP.longValue();
	long sizeMax = sizeMaxP.longValue();

	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">
</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "download.jsp");

Board	board		 = gef.getBoard();

out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
<table>
<tr>
	<td class="txtlibform">
		<%=resource.getString("silverCrawler.fileZip")%> : 
	</td>
	<td>
		<img border="0" src="<%=resource.getIcon("silverCrawler.zip")%>" >
	</td>
	<td>
	<%
		if (name == null || name.equals("null"))
		{%>
			<%=resource.getString("silverCrawler.sizeMax")%> (<%=sizeMax%> Mo)
		<%}
		else
		{
			if ("".equals(name))
			{%>
				<%=resource.getString("silverCrawler.noFileZip")%>
			<%}
			else
			{%>
				<a href="<%=zipUrl%>"><%=name%></a>&nbsp;(<%=FileRepositoryManager.formatFileSize(sizeZip)%>)
			<%}
		}%>
	</td>
</tr>
</table>	
		

<%
out.println(board.printAfter());
out.println(frame.printMiddle());
ButtonPane buttonPane = gef.getButtonPane();
Button button = (Button) gef.getFormButton(resource.getString("GML.close"), "javaScript:window.close();", false);
buttonPane.addButton(button);
out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>