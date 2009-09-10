
<%@ include file="check.jsp" %>

<% 
	// récupération des paramètres :
	String 		albumId			= (String) request.getAttribute("AlbumId");

	String 		profile			= (String) request.getAttribute("Profile");
	String		userId 			= (String) request.getAttribute("UserId");
	Collection 	path 			= (Collection) request.getAttribute("Path");
		
	String 		photoId 		= "";
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">

function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}
</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(displayPath(path));
	
	operationPane.addOperation(resource.getIcon("gallery.PDCNewPosition"), resource.getString("GML.PDCNewPosition"), "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')");
	operationPane.addOperation(resource.getIcon("gallery.PDCDeletePosition"), resource.getString("GML.PDCDeletePosition"), "javascript:getSelectedItems()");
		
	
	out.println(window.printBefore());
    out.println(frame.printBefore());

	out.flush();    
	String url = URLManager.getURL("useless", componentId) + "PdcPositions?PhotoId="+photoId;
	getServletConfig().getServletContext().getRequestDispatcher("/RpdcClassify/jsp/NewPosition?ComponentId="+componentId+"&ReturnURL="+URLEncoder.encode(url)).include(request, response);

  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<FORM NAME="toComponent" ACTION="PdcPositions" METHOD=POST>
	<input type="hidden" name="PhotoId" value="<%=photoId%>">
</FORM>
</body>
</html>