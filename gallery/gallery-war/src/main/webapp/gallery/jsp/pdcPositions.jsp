
<%@ include file="check.jsp" %>

<% 
	// récupération des paramètres :
	PhotoDetail photo			= (PhotoDetail) request.getAttribute("Photo");
	String 		profile			= (String) request.getAttribute("Profile");
	String		userId 			= (String) request.getAttribute("UserId");
	Collection 	path 			= (Collection) request.getAttribute("Path");
	Integer		nbCom			= (Integer) request.getAttribute("NbComments");
	Integer		silverObjetId	= (Integer) request.getAttribute("SilverObjetId");
	String 		xmlFormName		= (String) request.getAttribute("XMLFormName");
	boolean		showComments	= ((Boolean) request.getAttribute("ShowCommentsTab")).booleanValue();
		
	// déclaration des variables :
	
	String 		photoId			= photo.getPhotoPK().getId();
	String 		nbComments 		= nbCom.toString();
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
	
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(displayPath(path));
	
	operationPane.addOperation(resource.getIcon("gallery.PDCNewPosition"), resource.getString("GML.PDCNewPosition"), "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')");
	operationPane.addOperation(resource.getIcon("gallery.PDCDeletePosition"), resource.getString("GML.PDCDeletePosition"), "javascript:getSelectedItems()");
		

   	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("gallery.photo"), "PreviewPhoto?PhotoId="+photoId, false);
	tabbedPane.addTab(resource.getString("gallery.info"), "EditInformation?PhotoId="+photoId, false);
	if (showComments)
		tabbedPane.addTab(resource.getString("gallery.comments")+" ("+nbComments+")", "Comments?PhotoId="+photoId, false);
	tabbedPane.addTab(resource.getString("gallery.accessPath"), "AccessPath?PhotoId="+photoId, false);
	tabbedPane.addTab(resource.getString("GML.PDC"), "#", true, false);
	
	out.println(window.printBefore());
	out.println(tabbedPane.print());
    out.println(frame.printBefore());

	out.flush();    
	String url = URLManager.getURL("useless", componentId) + "PdcPositions?PhotoId="+photoId;
	getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/positionsInComponent.jsp?SilverObjectId="+silverObjetId+"&ComponentId="+componentId+"&ReturnURL="+URLEncoder.encode(url)).include(request, response);

  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<FORM NAME="toComponent" ACTION="PdcPositions" METHOD=POST>
	<input type="hidden" name="PhotoId" value="<%=photoId%>">
</FORM>
</body>
</html>