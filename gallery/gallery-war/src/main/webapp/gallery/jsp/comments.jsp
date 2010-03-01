<%--

    Copyright (C) 2000 - 2009 Silverpeas

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
<%@ include file="check.jsp" %>

<% 
	// récupération des paramètres :
	PhotoDetail photo			= (PhotoDetail) request.getAttribute("Photo");
	String 		profile			= (String) request.getAttribute("Profile");
	String		userId 			= (String) request.getAttribute("UserId");
	List  	path 			= (List) request.getAttribute("Path");
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
	displayPath(path, browseBar);

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