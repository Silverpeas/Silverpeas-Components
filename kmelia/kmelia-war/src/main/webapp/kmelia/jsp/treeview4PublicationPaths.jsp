<%--

    Copyright (C) 2000 - 2020 Silverpeas

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

<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="org.silverpeas.core.node.model.NodeDetail"%>
<%@ page import="org.silverpeas.core.contribution.publication.model.Location" %>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>

<%
MultiSilverpeasBundle resources = (MultiSilverpeasBundle)request.getAttribute("resources");

List<NodeDetail> 	otherTree 	= (List<NodeDetail>) request.getAttribute("Tree");
String	currentLang = (String) request.getAttribute("Language");
List<Location>	locations		= (List<Location>) request.getAttribute("Aliases");
%>

<table>
	<caption></caption>
	<th id="locations"></th>
<%
for(NodeDetail topic: otherTree)
{
	if (topic.getId() != 1 && topic.getId() != 2)
	{
			String name = WebEncodeHelper.convertHTMLEntities(topic.getName(currentLang));

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
			for(Location location: locations)
			{
				String nodeId = location.getId();

				if (location.isNode(topic))
				{
					checked = " checked";
					aliasDecoration = "<i>"+WebEncodeHelper.convertHTMLEntities(location.getAlias().getUserName())+" - "+resources.getOutputDateAndHour(
              location.getAlias().getDate())+"</i>";
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