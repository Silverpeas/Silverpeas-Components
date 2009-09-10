<%@ include file="check.jsp" %>

<% 
	// récupération des paramètres :
	PhotoDetail photo			= (PhotoDetail) request.getAttribute("Photo");
	String 		profile			= (String) request.getAttribute("Profile");
	String		userId 			= (String) request.getAttribute("UserId");
	Collection 	path 			= (Collection) request.getAttribute("Path");
	String 		galleryUrl		= (String) request.getAttribute("Url");
	Integer		nbCom			= (Integer) request.getAttribute("NbComments");
	Boolean		isUsePdc		= (Boolean) request.getAttribute("IsUsePdc");
	String 		XMLFormName		= (String) request.getAttribute("XMLFormName");
	boolean		updateAllowed	= ((Boolean) request.getAttribute("UpdateImageAllowed")).booleanValue();
		
	// déclaration des variables :
	
	String 		photoId			= new Integer(photo.getPhotoPK().getId()).toString();
	String 		nbComments 		= nbCom.toString();
	
	boolean 	pdc				= isUsePdc.booleanValue();
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

   	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("gallery.photo"), "PreviewPhoto?PhotoId="+photoId, false);
	if (updateAllowed)
	{
		tabbedPane.addTab(resource.getString("gallery.info"), "EditInformation?PhotoId="+photoId, false);
	}
	tabbedPane.addTab(resource.getString("gallery.comments")+" ("+nbComments+")", "#", true);
	if (updateAllowed)
	{
		tabbedPane.addTab(resource.getString("gallery.accessPath"), "AccessPath?PhotoId="+photoId, false);
		if (pdc)
			tabbedPane.addTab(resource.getString("GML.PDC"), "PdcPositions?PhotoId="+photoId, false);
	}
	
	out.println(window.printBefore());
	out.println(tabbedPane.print());
    out.println(frame.printBefore());

	String url = URLManager.getURL("useless", componentId) + "Comments";
	out.flush();    
   	getServletConfig().getServletContext().getRequestDispatcher("/comment/jsp/comments.jsp?id="+photoId+"&userid="+userId+"&profile="+profile+"&url="+url+"&component_id="+componentId).include(request, response);

  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>