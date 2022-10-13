<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="java.util.List"%>
<%@ page import="org.silverpeas.core.node.model.NodeDetail"%>
<%@ page import="org.silverpeas.core.contribution.publication.model.Location" %>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.core.util.Mutable" %>
<%@ page import="org.apache.ecs.xhtml.input" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%
MultiSilverpeasBundle resources = (MultiSilverpeasBundle)request.getAttribute("resources");

//noinspection unchecked
  List<NodeDetail> 	otherTree 	= (List<NodeDetail>) request.getAttribute("Tree");
String	currentLang = (String) request.getAttribute("Language");
//noinspection unchecked
  List<Location>	locations		= (List<Location>) request.getAttribute("Locations");
%>

<% if (otherTree != null) { %>
<table>
  <caption>Locations of the publication in Silverpeas</caption>
	<th id="locations"></th>
<%
  for (NodeDetail topic : otherTree) {
    if (!topic.isBin() && !topic.isUnclassified()) {
      String name = Encode.forHtml(topic.getName(currentLang));

      StringBuilder ind = new StringBuilder();
      if(topic.getLevel() > 2) { // calcul chemin arbre
        int sizeOfInd = topic.getLevel() - 2;
        if(sizeOfInd > 0) {
          ind.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;".repeat(sizeOfInd));
        }
      }
      name = ind + name;

      // recherche si ce dossier est dans la liste des dossiers de la publication
      final Mutable<String> aliasDecoration = Mutable.of("");
      final input topicChoiceInput = new input().setType("checkbox").setName("topicChoice").setValue(topic.getId()+","+topic.getNodePK().getInstanceId());
      topicChoiceInput.setID(topic.getId()+"-"+topic.getNodePK().getInstanceId());
      topicChoiceInput.addAttribute("valign","absmiddle");
      topicChoiceInput.setOnClick("manageLocation('"+topic.getId()+"','"+topic.getNodePK().getInstanceId()+"', this)");
      locations.stream()
          .filter(l -> topic.getNodePK().equals(l))
          .findFirst()
          .ifPresent(l -> {
            topicChoiceInput.setChecked(true);
            if (l.isAlias()) {
              aliasDecoration.set("<span>&nbsp;</span><i>"+l.getAlias().getUserName()+" - "+resources.getOutputDateAndHour(l.getAlias().getDate())+"</i>");
            } else {
              topicChoiceInput.setReadOnly(true);
              topicChoiceInput.setDisabled(true);
              topicChoiceInput.setOnClick("return false");
            }
          });
      boolean displayCheckbox = false;
      if (topic.getUserRole()==null || !topic.getUserRole().equals("user")) {
        displayCheckbox = true;
        if ("writer".equals(topic.getUserRole())) {
          topicChoiceInput.setReadOnly(true);
          topicChoiceInput.setDisabled(true);
          topicChoiceInput.setOnClick("return false");
        }
      }
      out.println("<tr><td width=\"10px\">");
      if (displayCheckbox) {
        out.println(topicChoiceInput.toString());
      } else {
        out.println("&nbsp;");
      }
      out.println("</td><td nowrap=\"nowrap\">"+name+"</td><td align=\"right\">"+aliasDecoration.get()+"</td></tr>");
    }

  }
%>
</table>
<% } else {
    out.println(resources.getMultilangBundle().getString("kmelia.paths.app.noPublish"));
   }%>
