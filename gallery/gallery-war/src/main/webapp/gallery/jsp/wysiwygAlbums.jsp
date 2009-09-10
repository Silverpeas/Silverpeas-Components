<%@ page import="java.util.List"%>
<%@ page import="java.util.Collection"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%
	String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
%>

<% 
List 	albums 		= (List) request.getAttribute("Albums");
String 	language 	= (String) request.getAttribute("Language");
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/treeview.css">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/treeview/TreeView.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/treeview/TreeViewElements.js"></script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
	<SCRIPT language="JavaScript" type="text/javascript">

	//TREEVIEW'S ELEMENTS - ELEMENTS DU TREEVIEW
	var elements_treeview = new TreeViewElements();
	
	<%
		NodeDetail album = null;
		String albumId	= null;
		String fatherId = null;
		for (int a=0; albums != null && a<albums.size(); a++)
		{
			album = (NodeDetail) albums.get(a);
			albumId = album.getNodePK().getId();
			fatherId = album.getFatherPK().getId();
			if ("-1".equals(fatherId))
				fatherId = "0";
			out.println("elements_treeview.addElement(\""+Encode.javaStringToHtmlString(album.getName())+"\", "+albumId+", "+fatherId+", \"dossier\", \"folder\", \"Language="+language+"&ComponentId="+album.getNodePK().getInstanceId()+"\");");
		}
	%>
	
	//TREEVIEW CONTROL - CONTRÔLE TREEVIEW
	var treeview = new TreeView("treeview", "<%=m_context%>");
	treeview.define (elements_treeview);
	treeview.validate(); // Elements Validation
	treeview.height = "590px";
	treeview.width = "190px";

	/* Preloader - Préchargeur */
	treeview.load_all = true;
	treeview.use_preloader_feature = false;
	treeview.preloader_position = "top"; // top or bottom
	treeview.preloader_addButton = true;

	/* Folder - Dossier */
	treeview.use_folder_feature = true;

	/* Links - Liens */
	treeview.use_link_feature = true;
	treeview.link_target = "images"; // _blank , _parent , _self , _top , _a_frame_or_iframe_name
	treeview.link_prefix = "<%=m_context%>/GalleryInWysiwyg/dummy?";
	treeview.link_suffix = "&AlbumId=";
	treeview.link_add_nodeId = true; // false ou true

	/* Displaying - Affichage */
	treeview.display();
	treeview.reduce_all();

	</SCRIPT>

</body>
</html>