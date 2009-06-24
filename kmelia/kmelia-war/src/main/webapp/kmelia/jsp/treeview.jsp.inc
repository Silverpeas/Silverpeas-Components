	<SCRIPT language="JavaScript" type="text/javascript">

	//TREEVIEW'S ELEMENTS - ELEMENTS DU TREEVIEW
	var elements_treeview = new TreeViewElements();
		
	<%
		NodeDetail 	node 			= null;
		String 		nodeId			= null;
		String 		nodeFatherId 	= null;
		String 		nodeName 		= null;
		NodeDetail 	current 		= null;
		String		nbPublis		= "";
		for (int i=0; treeview != null && i<treeview.size(); i++)
		{
			node 		= (NodeDetail) treeview.get(i);
			nodeId 		= node.getNodePK().getId();
			
			if (displayNbPublis.booleanValue())
				nbPublis = " ("+node.getNbObjects()+")";
				
			nodeName	= Encode.javaStringToHtmlString(node.getName(translation))+nbPublis;
			
			if (nodeId.equals("0"))
				nodeName = Encode.javaStringToHtmlString(componentLabel)+nbPublis;
			if (nodeId.equals(id))
				current = node;
			if (!"1".equals(nodeId) && !"2".equals(nodeId))
			{
				nodeFatherId = node.getFatherPK().getId();
				if ("-1".equals(nodeFatherId))
					nodeFatherId = "0";
				out.println("elements_treeview.addElement(\""+nodeName+"\", "+nodeId+", "+nodeFatherId+", \"dossier\", \"folder\", \"Language="+language+"\");");
			}
		}
	%>
	
	//TREEVIEW CONTROL - CONTRÔLE TREEVIEW
	var treeview = new TreeView("treeview", "<%=m_context%>");
	treeview.define (elements_treeview);
	treeview.validate(); // Elements Validation
	treeview.height = "590px";
	treeview.width = "190px";
	
	treeview.verbose = false;

	/* Preloader - Préchargeur */
	treeview.load_all = true;
	treeview.use_preloader_feature = false;
	treeview.preloader_position = "top"; // top or bottom
	treeview.preloader_addButton = true;

	/* Folder - Dossier */
	treeview.use_folder_feature = true;

	/* Links - Liens */
	treeview.use_link_feature = true;
	treeview.link_target = "_self"; // _blank , _parent , _self , _top , _a_frame_or_iframe_name
	treeview.link_prefix = "<%=m_context+URLManager.getURL("useless", componentId)%>GoToTopic?";
	treeview.link_suffix = "Action=Search&SwitchLanguage=<%=translation%>&Id=";
	treeview.link_add_nodeId = true; // false ou true

	/* Displaying - Affichage */
	treeview.display();
	treeview.reduce_all();
	//treeview.display_childrens("<%=id%>");
	
	<%
		if (current != null)
		{
			String currentPath = current.getPath()+current.getNodePK().getId();
			//out.println("alert(\""+currentPath+"\");");
			StringTokenizer st = new StringTokenizer(currentPath, "/");
			while (st.hasMoreTokens())
			{
				String s = st.nextToken();
				%>
					treeview.display_childrens("<%=s%>");
					//out.println("display_folder(\"treeview\",\""+s+"\");");
					treeview.display_imageDossier_open("<%=s%>");
					treeview.display_imageMinus("<%=s%>");
					<% if (!id.equals("1") && s.equals(id)) { %>
						document.getElementById ( "treeview" + "_label_" + <%=s%>).className = "label_selected";
					<% } %>
				<%
			}
		}
	%>
	//display_childrens("treeview", "<%=id%>");

	/* HTML Elements - Eléments HTML */
	treeview.control.ondblclick = function ( ) { window.status = "control" ; }

	/* Preloader - Préchargeur */
	/*treeview.button_expandAll.ondblclick = function ( ) { alert ( "button_expandAll" ) ; }
	treeview.button_reduceAll.ondblclick = function ( ) { alert ( "button_reduceAll" ) ; }
	treeview.button_builtAll.ondblclick = function ( ) { alert ( "button_builtAll" ) ; }
	treeview.button_destroyAll.ondblclick = function ( ) { alert ( "button_destroyAll" ) ; }
	treeview.preload.onclick = function ( ) { alert ( "preload" ) ; }
	treeview.preloader.onclick = function ( ) { window.status = "preloader" ; }
	treeview.preload_container.onmouseover = function ( ) { window.status = "preload_container" ; }*/

	</SCRIPT>