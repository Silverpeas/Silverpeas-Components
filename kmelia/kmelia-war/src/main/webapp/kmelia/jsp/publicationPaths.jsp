<%--

    Copyright (C) 2000 - 2019 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="org.silverpeas.core.contribution.publication.model.Location" %>
<%@ page import="org.silverpeas.components.kmelia.model.Treeview" %>
<%@page import="org.silverpeas.components.kmelia.jstl.KmeliaDisplayHelper"%>
<%@ page import="org.silverpeas.components.kmelia.model.KmeliaPublication" %>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>

<%
KmeliaPublication publication 		= (KmeliaPublication) request.getAttribute("Publication");
String 				linkedPathString 	= (String) request.getAttribute("LinkedPathString");
Collection<NodeDetail> topics			= (Collection<NodeDetail>) request.getAttribute("Topics");
String				currentLang 		= (String) request.getAttribute("Language");
List<Treeview>		components			= (List<Treeview>) request.getAttribute("Components");
Collection<Location> locations = (Collection<Location>) request.getAttribute("Locations");

String pubName 	= publication.getDetail().getName(currentLang);
String id 		= publication.getDetail().getPK().getId();

Button validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData();", false);
Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "ViewPublication?PubId="+id, false);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xml:lang="<%=currentLang%>%>">
<head>
<title></title>
<view:looknfeel/>
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
	background-image: url(<%=m_context%>/admin/jsp/icons/silverpeasV5/degrade20px.jpg);
	background-repeat: repeat-x;
}

.ui-icon {
	display: none;
}
</style>
<script type="text/javascript">
	$(document).ready(function(){
		$('#accordion').accordion({   
			clearStyle: false,  
			autoHeight: false
		}).bind("accordionactivate", function(event, ui) {
			doSomething(ui.newHeader[0].id);
		});
	});

	var doSomething = function(paneId) 
	{
		if (loadedPanes.indexOf(","+paneId+",") != "-1")
		{
			//Pane already loaded
		}
		else
		{
			$.post("<%=routerUrl%>ShowAliasTree?ComponentId="+paneId, function(data){
				$("#content_"+paneId).html(data);
			});

			loadedPanes += paneId+",";
		}
	};

	var loadedPanes = ",<%=componentId%>,";
</script>
<script type="text/javascript">
function sendData() {
	var selectedPaths = getSelectedOjects();
	if (selectedPaths.indexOf(',<%=componentId%>,') == -1) {
    jQuery.popup.error('<%=WebEncodeHelper.javaStringToHtmlString(WebEncodeHelper.javaStringToJsString(resources.getString("kmelia.paths.mandatory")))%>');
	} else {
		document.getElementById("LoadedComponentIds").value=","+loadedPanes+",";
		document.paths.submit();
	}
}

function topicGoTo(id) {
	location.href="GoToTopic?Id="+id;
}

function getSelectedOjects() {
	return getObjects(true);
}

function getObjects(selected) {
	var  items = "";
	var boxItems = document.paths.topicChoice;
	if (boxItems != null){
		// au moins une checkbox exist
		var nbBox = boxItems.length;
		if ( (nbBox == null) && (boxItems.checked == selected) ){
			// il n'y a qu'une checkbox non selectionn�e
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
</head>
<body>
  <%
    Window window = gef.getWindow();
    Frame frame = gef.getFrame();

    BrowseBar browseBar = window.getBrowseBar();

    browseBar.setDomainName(spaceLabel);
      browseBar.setComponentName(componentLabel, "javascript:onClick=topicGoTo('0')");
      browseBar.setPath(linkedPathString);
      browseBar.setExtraInformation(pubName);

    out.println(window.printBefore());

    KmeliaDisplayHelper.displayAllOperations(id, kmeliaScc, gef, "ViewPath", resources, out);

  out.println(frame.printBefore());

        Board board	= gef.getBoard();
        
        out.println(board.printBefore());
        
     	//regarder si la publication est dans la corbeille
    		if (NodePK.BIN_NODE_ID.equals(publication.getLocation().getId())) {
    			//la publi est dans la corbeille
        		out.println(kmeliaScc.getString("kmelia.PubInBasket")+"<br/><br/>");
    		}
        %>
        <form name="paths" action="SetPath" method="post">
        	<input type="hidden" name="PubId" value="<%=id%>"/>
        	<input type="hidden" name="LoadedComponentIds" id="LoadedComponentIds" value=""/> 
        	
        <%
    	if(topics != null && !topics.isEmpty()) {
    	  	// toolbox case
    		out.println("<table>");
    		for (NodeDetail topic : topics) {
    			if (topic.getId() != 1 && topic.getId() != 2) {
    				String name = topic.getName(currentLang);
    				
    				String ind = "";
    				if(topic.getLevel() > 2) { // calcul chemin arbre
    					int sizeOfInd = topic.getLevel() - 2;
    					if(sizeOfInd > 0) {
    						for(int i=0;i<sizeOfInd;i++) {
    							ind += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    						}
    					}
    				}
    				name = ind + name;	
    				
    				// recherche si ce th�me est dans la liste des locations de la publication
					  String usedCheck = "";
    				for (Location node : locations) {
	    				String nodeId = node.getId();
	    				if (Integer.toString(topic.getId()).equals(nodeId)) {
	    					usedCheck = " checked=\"checked\"";
	    				}
	    			}
    				
    				boolean displayCheckbox = false;
    				if (topic.getUserRole() == null || !topic.getUserRole().equals("user")) {
    					displayCheckbox = true;
    				}
    				
    	        	out.println("<tr><td width=\"10px\">");
    	        	if (displayCheckbox) {
    	        		out.println("<input type=\"checkbox\" valign=\"absmiddle\" name=\"topicChoice\" value=\""+topic.getId()+","+topic.getNodePK().getInstanceId()+"\""+usedCheck+"/>");
    	        	} else {
    	        		out.println("&nbsp;");
    	        	}
    		    		    					
    	        	out.println("</td><td>"+name+"</td></tr>");
    			}
    		}
    		out.println("</table>");
    	} else {
    	  	// kmelia case
			out.println("<div id=\"accordion\" class=\"basic\">");
	
	    	Treeview treeview = null;
	    	String nbAliases = "";
	    	String panelTitle = "";
	    	for (int t=0; components != null && t<components.size(); t++) {
	    		treeview = components.get(t);
	    		
	    		nbAliases = "";
	    		if (t == 0) {
	    			panelTitle = resources.getString("kmelia.paths.local");
	    		} else {
	    			panelTitle = treeview.getPath();
	    		}
	    		
	    		if (treeview.getNbAliases() == 1) {
	    			nbAliases = "<span align=\"right\">"+treeview.getNbAliases()+" "+resources.getString("kmelia.paths.path")+"</span>";
	    		} else if (treeview.getNbAliases() > 1) {
	    			nbAliases = "<span align=\"right\">"+treeview.getNbAliases()+" "+resources.getString("kmelia.paths.paths")+"</span>";
	    		}  
	%>
				<a href="javascript: void(0)" id="<%=treeview.getComponentId()%>"><table><caption></caption><th id="all-locations"></th><tr><td><%=panelTitle%></td><td text-align="right"><%=nbAliases%></td></tr></table></a>
					<div id="content_<%=treeview.getComponentId()%>" class="content">
	<%
					out.println("<table border=\"0\">");
					if (t == 0) {
		    			List<NodeDetail> otherTree = treeview.getTree();
		    			for (NodeDetail topic : otherTree) {
		        			if (topic.getId() != 1 && topic.getId() != 2) {
	    	    				String name = topic.getName(currentLang);
	    	    				    	    			
	    	    				String ind = "";
	    	    				if(topic.getLevel() > 2) { // calcul chemin arbre
	    	    					int sizeOfInd = topic.getLevel() - 2;
	    	    					if(sizeOfInd > 0) {
	    	    						for(int i=0;i<sizeOfInd;i++) {
	    	    							ind += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	    	    						}
	    	    					}
	    	    				}
	    	    				name = ind + name;
	    	    				
	    	    				// recherche si ce dossier est dans la liste des dossiers de la publication
	    	    				String aliasDecoration = "&nbsp;";
	    	    				String checked = "";
	    	    				String readonly = "";
	    	    				for (Location location : locations) {
	    		    				String nodeId = location.getId();
	    		    				if (Integer.toString(topic.getId()).equals(nodeId) && topic.getNodePK().getInstanceId().equals(
													location.getInstanceId())) {
	    		    					checked = " checked=\"checked\"";
	    		    					if (location.isAlias()) {
	    		    						aliasDecoration = "<i>"+
															location.getAlias().getUserName()+" - "+resources.getOutputDateAndHour(location.getAlias().getDate())+"</i>";
	    		    					} else {
	    		    						readonly = "readonly=\"readonly\" disabled=\"disabled\"";
												}
	    		    				}
	    		    			}
	    	    				boolean displayCheckbox = false;
                    String readonlyCheckbox = "";
	    	    				if (topic.getUserRole()==null || !topic.getUserRole().equals("user")) {
	    	    					displayCheckbox = true;
                      if ("writer".equals(topic.getUserRole())) {
                        readonlyCheckbox = " onclick=\"return false\"";
                      }
	    	    				}
	    	    	        	out.println("<tr><td width=\"10px\">");
	    	    	        	if (displayCheckbox) {
	    	    	        		out.println("<input type=\"checkbox\" valign=\"absmiddle\" name=\"topicChoice\" value=\""+topic.getId()+","+topic.getNodePK().getInstanceId()+"\""+checked+readonlyCheckbox+readonly+">");
	    	    	        	} else {
	    	    	        		out.println("&nbsp;");
	    	    	        	}
	    	    	        	out.println("</td><td nowrap=\"nowrap\">"+name+"</td><td align=\"right\">"+aliasDecoration+"</td></tr>");
		        			}
		        			
		        		}
					} else {
						out.println("<tr><td>Chargement en cours...</td></tr>");
					}
					out.println("</table>");
	%>
					</div>
	<%  			
	    	}
	    	out.println("</div>");
    	}
    	out.println("</form>");
    	
    	out.println(board.printAfter());
    	
    	ButtonPane buttonPane = gef.getButtonPane();
    	buttonPane.addButton(validateButton);
    	buttonPane.addButton(cancelButton);
    	out.println("<br/>"+buttonPane.print());
    	
        out.println(frame.printAfter());
        out.println(window.printAfter());
%>
</body>
</html>