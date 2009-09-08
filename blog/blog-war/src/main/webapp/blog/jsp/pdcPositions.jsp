<%@ include file="check.jsp" %>

<% 
	// récupération des paramètres :
	PostDetail 	post			= (PostDetail) request.getAttribute("Post");
	Integer		silverObjetId	= (Integer) request.getAttribute("SilverObjetId");
		
	// déclaration des variables :
	String 		postId			= post.getPublication().getPK().getId();
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body id="blog">
<div id="<%=instanceId %>">
<%
	operationPane.addOperation(resource.getIcon("blog.PDCNewPosition"), resource.getString("GML.PDCNewPosition"), "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')");
	operationPane.addOperation(resource.getIcon("blog.PDCDeletePosition"), resource.getString("GML.PDCDeletePosition"), "javascript:getSelectedItems()");
		
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("GML.head"), "EditPost?PostId=" + postId, false);
	tabbedPane.addTab(resource.getString("blog.content"), "ViewContent?PostId=" + postId, false);
	tabbedPane.addTab(resource.getString("GML.PDC"), "#", true, false);
	
	out.println(window.printBefore());
	out.println(tabbedPane.print());
    out.println(frame.printBefore());

	out.flush();    
	String url = URLManager.getURL("useless", instanceId) + "PdcPositions?PostId="+postId;
	getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/positionsInComponent.jsp?SilverObjectId="+silverObjetId+"&ComponentId="+instanceId+"&ReturnURL="+URLEncoder.encode(url)).include(request, response);

  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<FORM NAME="toComponent" ACTION="PdcPositions" METHOD=POST>
	<input type="hidden" name="PostId" value="<%=postId%>">
</FORM>
</div>
</body>
</html>