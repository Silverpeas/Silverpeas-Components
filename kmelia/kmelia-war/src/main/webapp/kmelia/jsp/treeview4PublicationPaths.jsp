<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

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
			
			// recherche si ce th�me est dans la liste des th�mes de la publication
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