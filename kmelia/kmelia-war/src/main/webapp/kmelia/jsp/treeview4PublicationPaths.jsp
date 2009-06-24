<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.Alias" %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>

<%
ResourcesWrapper resources = (ResourcesWrapper)request.getAttribute("resources");

List 	otherTree 	= (List) request.getAttribute("Tree");
String	currentLang = (String) request.getAttribute("Language");
List	aliases		= (List) request.getAttribute("Aliases");
%>

<table border="0" width="100%">
<%
Iterator otherTopics = otherTree.iterator();
while(otherTopics.hasNext())
{
	NodeDetail topic = (NodeDetail) otherTopics.next();
		
	if (topic.getId() != 1 && topic.getId() != 2)
	{
			String name = Encode.convertHTMLEntities(topic.getName(currentLang));
			    	    			
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
					aliasDecoration = "<i>"+Encode.convertHTMLEntities(alias.getUserName())+" - "+resources.getOutputDateAndHour(alias.getDate())+"</i>";
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
%>
</table>