<%@ page import="com.stratelia.webactiv.util.publication.model.Alias" %>
<%@ page import="com.stratelia.webactiv.kmelia.model.Treeview" %>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="tabManager.jsp.inc" %>

<%
//Récupération des paramètres
String 				wizard				= (String) request.getAttribute("Wizard");
PublicationDetail 	publication 		= (PublicationDetail) request.getAttribute("Publication");
Collection			pathList 			= (Collection) request.getAttribute("PathList");
String 				linkedPathString 	= (String) request.getAttribute("LinkedPathString");
Collection 			topics				= (Collection) request.getAttribute("Topics");
String				currentLang 		= (String) request.getAttribute("Language");
List				otherComponents		= (List) request.getAttribute("OtherComponents");
List				aliases				= (List) request.getAttribute("Aliases");

// déclaration des variables
String pubName 	= publication.getName(currentLang);
String id 		= publication.getPK().getId();

// déclaration des boutons
Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData();", false);
Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "ViewPublication?PubId="+id, false);

%>

<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<TITLE></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<style>
.content {
	padding-left: 0px;
	overflow: auto;
	height: 250px;
}

.basic  {
	width: 90%;
}

.basic a {
	cursor:pointer;
	display:block;
	text-decoration: none;
	color: #000000;
	width: 100%;
	margin-top: 2px;
	padding: 5px;
	background-image: url(<%=m_context%>/admin/jsp/icons/silverpeasV4/fondOff.gif);
	background-repeat: repeat-x;
}

</style>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery-1.2.6.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/ui.core.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/ui.accordion.js"></script>
<script type='text/javascript'>
	$(document).ready(function(){
		$('#accordion').accordion({   
			clearStyle: false,  
			autoHeight: false
		}).bind("accordionchange", function(event, ui) {
			doSomething(ui.newHeader[0].id);
		});
	});

	var doSomething = function(paneId) 
	{
		if (loadedPanes.indexOf(paneId) != "-1")
		{
			//Pane already loaded
		}
		else
		{
			$.post("<%=routerUrl%>ShowAliasTree?ComponentId="+paneId, function(data){
				$("#content_"+paneId).html(data);
			});

			loadedPanes += ","+paneId;
		}
	};

	var loadedPanes = "<%=componentId%>";
</script>
<script language="javascript">

function sendData() 
{
	var selectedPaths = getSelectedOjects();
	if (selectedPaths.indexOf(',<%=componentId%>,') == -1)
	{
		alert('<%=Encode.javaStringToHtmlString(Encode.javaStringToJsString(resources.getString("kmelia.LocalPathMandatory")))%>');
	}
	else
	{
		document.getElementById("LoadedComponentIds").value=","+loadedPanes+",";
		document.paths.submit();
	}
}

function topicGoTo(id) 
{
	location.href="GoToTopic?Id="+id;
}

function getSelectedOjects()
{
	return getObjects(true);
}

function getObjects(selected)
{
	var  items = "";
	var boxItems = document.paths.topicChoice;
	if (boxItems != null){
		// au moins une checkbox exist
		var nbBox = boxItems.length;
		if ( (nbBox == null) && (boxItems.checked == selected) ){
			// il n'y a qu'une checkbox non selectionnée
			items += boxItems.value+",";
		} else{
			// search not checked boxes 
			for (i=0;i<boxItems.length ;i++ ){
				if (boxItems[i].checked == selected){
					items += boxItems[i].value+",";
				}
			}
		}
	}
	return items;
}

</script>
</HEAD>
<BODY>
<% 		
        Window window = gef.getWindow();
        Frame frame = gef.getFrame();

        BrowseBar browseBar = window.getBrowseBar();
        Board boardHelp = gef.getBoard();
        
        browseBar.setDomainName(spaceLabel);
        browseBar.setComponentName(componentLabel, "javascript:onClick=topicGoTo('0')");
        browseBar.setPath(linkedPathString);
		browseBar.setExtraInformation(pubName);

        out.println(window.printBefore());

        displayAllOperations(id, kmeliaScc, gef, "ViewPath", resources, out);
        
        out.println(frame.printBefore());
        if ("finish".equals(wizard))
    	{
    		//  cadre d'aide
    	    out.println(boardHelp.printBefore());
    		out.println("<table border=\"0\"><tr>");
    		out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resources.getIcon("kmelia.info")+"\"></td>");
    		out.println("<td>"+kmeliaScc.getString("kmelia.HelpPubPath")+"</td>");
    		out.println("</tr></table>");
    	    out.println(boardHelp.printAfter());
    	    out.println("<BR>");
    	}
        
        Board board	= gef.getBoard();
        
        out.println(board.printBefore());
        
     	//regarder si la publication est dans la corbeille
		Iterator itB = pathList.iterator();
		while (itB.hasNext()) 
    	{
    		NodePK node = (NodePK) itB.next();
    		String nodeId = node.getId();
    		if (nodeId.equals("1"))
    		{
    			//la publi est dans la corbeille
        		out.println(kmeliaScc.getString("kmelia.PubInBasket")+"<BR><BR>");
    		}
    	}
        %>
        <form name="paths" action="SetPath" method="POST">
        	<input type="hidden" name="PubId" value="<%=id%>"/>
        	<input type="hidden" name="LoadedComponentIds" id="LoadedComponentIds" value=""/> 
        	
        <%
    	if(otherComponents.size() == 1 && topics != null && !topics.isEmpty())
    	{
    		out.println("<table>");
    		Iterator iter = topics.iterator();
    		while(iter.hasNext())
    		{
    			NodeDetail topic = (NodeDetail) iter.next();
     			
    			if (topic.getId() != 1 && topic.getId() != 2)
    			{
	    			//if(topic.getLevel() > 1)
	    			//{ // on n'affiche pas le noeud racine
	    				String name = topic.getName(currentLang);
	    				
	    				String ind = "";
	    				if(topic.getLevel() > 2)
	    				{// calcul chemin arbre
	    					int sizeOfInd = topic.getLevel() - 2;
	    					if(sizeOfInd > 0)
	    					{
	    						for(int i=0;i<sizeOfInd;i++)
	    						{
	    							ind += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	    						}
	    					}
	    				}
	    				name = ind + name;	
	    				
	    				// recherche si ce thème est dans la liste des alias de la publication
						String usedCheck = "";
	    				Iterator it = pathList.iterator();
	    				while (it.hasNext()) 
		    			{
		    				NodePK node = (NodePK) it.next();
		    				String nodeId = node.getId();

		    				if (new Integer(topic.getId()).toString().equals(nodeId))
		    					usedCheck = " checked";
		    			}
	    				
	    				boolean displayCheckbox = false;
	    				if (topic.getUserRole() == null  || !topic.getUserRole().equals("user"))
	    					displayCheckbox = true;
	    				
	    	        	out.println("<tr><td width=\"10px\">");
	    	        	if (displayCheckbox)
	    	        		out.println("<input type=\"checkbox\" valign=\"absmiddle\" name=\"topicChoice\" value=\""+topic.getId()+","+topic.getNodePK().getInstanceId()+"\""+usedCheck+">");
	    	        	else
	    	        		out.println("&nbsp;");
	    		    		    					
	    	        	out.println("</td><td>"+name+"</td></tr>");
	    			//}
    			}
    		}
    		out.println("</table>");
    	}
    	
		out.println("<div id=\"accordion\" class=\"basic\">");

    	Treeview treeview = null;
    	String nbAliases = "";
    	String panelTitle = "";
    	for (int t=0; otherComponents.size() > 1 && t<otherComponents.size(); t++)
    	{
    		treeview = (Treeview) otherComponents.get(t);
    		
    		nbAliases = "";
    		if (t == 0)
    			panelTitle = "Emplacements locaux";
    		else
    			panelTitle = treeview.getPath();
    		
    		if (treeview.getNbAliases() > 0)
    			nbAliases = "<span align=\"right\">"+treeview.getNbAliases()+" emplacement(s)</span>";
%>
			<a href="javascript: void(0)" id="<%=treeview.getComponentId()%>"><table width="100%" cellspacing="0" cellpadding="0"><tr><td><%=panelTitle%></td><td align="right"><%=nbAliases%></td></tr></table></a>
				<div id="content_<%=treeview.getComponentId()%>" class="content">
<%
				out.println("<table border=\"0\">");
				if (t == 0)
				{
	    			List otherTree = (List) treeview.getTree();
	    			Iterator otherTopics = otherTree.iterator();
	    			while(otherTopics.hasNext())
	        		{
	        			NodeDetail topic = (NodeDetail) otherTopics.next();
	         			
	        			if (topic.getId() != 1 && topic.getId() != 2)
	        			{
	    	    				String name = topic.getName(currentLang);
	    	    				    	    			
	    	    				String ind = "";
	    	    				if(topic.getLevel() > 2)
	    	    				{// calcul chemin arbre
	    	    					int sizeOfInd = topic.getLevel() - 2;
	    	    					if(sizeOfInd > 0)
	    	    					{
	    	    						for(int i=0;i<sizeOfInd;i++)
	    	    						{
	    	    							ind += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	    	    						}
	    	    					}
	    	    				}
	    	    				name = ind + name;
	    	    				
	    	    				// recherche si ce thème est dans la liste des thèmes de la publication
	    	    				String aliasDecoration = "&nbsp;";
	    	    				String checked = "";
	    	    				Iterator it = aliases.iterator();
	    	    				while (it.hasNext()) 
	    		    			{
	    		    				Alias alias = (Alias) it.next();
	    		    				String nodeId = alias.getId();
	    		    				
	    		    				if (Integer.toString(topic.getId()).equals(nodeId) && topic.getNodePK().getInstanceId().equals(alias.getInstanceId()))
	    		    				{
	    		    					checked = " checked";
	    		    					if (!alias.getInstanceId().equals(componentId))
	    		    						aliasDecoration = "<i>"+alias.getUserName()+" - "+resources.getOutputDateAndHour(alias.getDate())+"</i>";
	    		    				}
	    		    			}
	    	    				boolean displayCheckbox = false;
	    	    				if (topic.getUserRole() ==null  || topic.getUserRole().equals("admin") || topic.getUserRole().equals("publisher"))
	    	    					displayCheckbox = true;
	    	    	        	out.println("<tr><td width=\"10px\">");
	    	    	        	if (displayCheckbox)
	    	    	        		out.println("<input type=\"checkbox\" valign=\"absmiddle\" name=\"topicChoice\" value=\""+topic.getId()+","+topic.getNodePK().getInstanceId()+"\""+checked+">");
	    	    	        	else
	    	    	        		out.println("&nbsp;");
	    	    	        	out.println("</td><td nowrap=\"nowrap\">"+name+"</td><td align=\"right\">"+aliasDecoration+"</td></tr>");
	        			}
	        			
	        		}
				}
				else
				{
					out.println("<tr><td>Chargement en cours...</td></tr>");
				}
				out.println("</table>");
%>
				</div>
<%  			
    	}
    	out.println("</div>"); 
    	out.println("</form>");
    	
    	out.println(board.printAfter());
    	
    	ButtonPane buttonPane = gef.getButtonPane();
    	buttonPane.addButton(validateButton);
    	buttonPane.addButton(cancelButton);
    	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
    	
        out.println(frame.printAfter());
        out.println(window.printAfter());
%>
</BODY>
</HTML>