<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
<%@ page import="org.silverpeas.components.yellowpages.control.DisplayContactsHelper" %>
<%@ page import="org.silverpeas.components.yellowpages.model.TopicDetail" %>
<%@ page import="org.silverpeas.components.yellowpages.model.YellowPagesGroupDetail" %>
<%@ page import="org.silverpeas.core.admin.component.model.CompoSpace" %>
<%@ page import="org.silverpeas.core.node.model.NodeDetail" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window" %>
<%@ page import="java.util.List" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ include file="checkYellowpages.jsp" %>
<%!
  private String afficheArbo(List<NodeDetail> tree, String idNodeSelected,
      YellowpagesSessionController yellowpagesScc) {
    StringBuilder resultat = new StringBuilder();
    StringBuilder espace;
    for (NodeDetail nodeDetail : tree) {
      String nodeId = nodeDetail.getNodePK().getId();
      if (nodeDetail.isRoot()) {
        resultat.append("<option value=\"").append(nodeId).append("\">")
            .append(yellowpagesScc.getComponentLabel()).append("</option>");
      } else {
        int nodeLevel = nodeDetail.getLevel();
        espace = new StringBuilder();
          espace.append("&nbsp;&nbsp;&nbsp;".repeat(Math.max(0, nodeLevel - 1)));
        resultat.append("<option value=\"").append(nodeId).append("\"");
        if (idNodeSelected.equals(nodeId)) {
          resultat.append("selected");
        }
        resultat.append(">").append(espace.toString()).append(nodeDetail.getName())
            .append("</option>");
      }
    }
    return resultat.toString();
  }
%>
<%
  TopicDetail currentTopic = (TopicDetail) request.getAttribute("CurrentTopic");
  YellowPagesGroupDetail group = (YellowPagesGroupDetail) request.getAttribute("Group");
  Boolean bPortletMode = (Boolean) request.getAttribute("PortletMode");
  boolean portletMode = (bPortletMode != null && bPortletMode);
  String searchCriteria = (String) request.getAttribute("SearchCriteria");

  String profile = request.getParameter("Profile");
  String action = request.getParameter("Action");
  //noinspection unchecked
  List<NodeDetail> tree = (List<NodeDetail>) request.getAttribute("Tree");

  if (action == null) {
    action = "GoTo";
  }

  String id;
  if (currentTopic != null) {
    id = currentTopic.getNodePK().getId();
  } else {
    id = "group_" + group.getId();
  }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.yellowpages">
<head>
  <view:looknfeel/>
  <view:includePlugin name="toggle"/>
  <script type="text/javascript">
    var printWindow = window;
    var contactWindow = window;

    function printList() {
      printWindow =
          SP_openWindow("PrintList", "printWindow", '800', '600', 'scrollbars=yes, alwayRaised');
    }

    function closeWindows() {
      if (!printWindow.closed && printWindow.name === "printWindow") printWindow.close();
      if (!contactWindow.closed && contactWindow.name === "contactWindow") contactWindow.close();
    }

    function topicGoToSelected() {
      var id = document.topicDetailForm.selectTopic.options[document.topicDetailForm.selectTopic.selectedIndex].value;
      document.topicDetailForm.Id.value = id;
      document.topicDetailForm.Action.value = "GoTo";
      document.topicDetailForm.action = "GoTo";
      document.topicDetailForm.submit();
    }

    function search() {
      document.topicDetailForm.action = "Search";
      document.topicDetailForm.submit();
    }

    function contactGoToUserInTopic(id, topic) {
      width = <%=resources.getSetting("popupWidth", 600)%>;
      height = <%=resources.getSetting("popupHeight", 480)%>;
      url = "ContactView?ContactId=" + id + "&TopicId=" + topic;
      window.contactPopup = jQuery.popup.load(url);
      window.contactPopup.show('free', {
        title : 'Contact', width : width, height : height
      });
    }

    function goToUser(id) {
      closeWindows();
      windowName = "contactWindow";
      windowParams =
          "directories=0,menubar=0,toolbar=0,height=400,width=600,alwaysRaised,scrollbars=yes";
      width = <%=resources.getSetting("popupWidth", 600)%>;
      height = <%=resources.getSetting("popupHeight", 480)%>;
      contactWindow =
          SP_openWindow("ViewUserFull?Id=" + id, windowName, width, height, windowParams);
    }

    function topicGoTo(id) {
      document.topicDetailForm.action = "GoTo";
      document.topicDetailForm.Id.value = id;
      document.topicDetailForm.submit();
    }

    function manage(profile) {
      closeWindows();
      location.href = "topicManager.jsp?Profile=" + profile;
    }

    function exportCSV() {
      sp.preparedDownloadRequest('ExportCSV').download();
    }

    whenSilverpeasReady(function() {
      $("#searchButton a").click(function() {
        search();
      });

      $("#searchInput").keypress(function(e) {
        if (e.which === 13) {
          e.preventDefault();
          search();
          return false;
        }
        return true;
      });
    });
  </script>
</head>
<body id="<%=componentId %>" class="yellowpages">
<%
  Window window = gef.getWindow();

  if (!portletMode) {
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel);
    browseBar.setPath(resources.getString("Consultation"));

    OperationPane operationPane = window.getOperationPane();
    operationPane
        .addOperation(resources.getIcon("yellowpages.printPage"), resources.getString("GML.print"),
            "javaScript:printList();");

    operationPane.addOperation("useless", resources.getString("GML.ExportCSV"), "javaScript:exportCSV();");
  }

  Frame frame = gef.getFrame();
  Board board = gef.getBoard();
  out.println(window.printBefore());

  if (!portletMode) {
%>
<view:componentInstanceIntro componentId="<%=componentId%>" language="<%=resources.getLanguage()%>"/>
<%
    if ("admin".equals(profile) || "publisher".equals(profile)) {
      //Onglets
      TabbedPane tabbedPane = gef.getTabbedPane();
      tabbedPane.addTab(resources.getString("Consultation"), "#", true);
      tabbedPane
          .addTab(resources.getString("GML.management"), "javascript:manage('" + profile + "');",
              false);
      out.println(tabbedPane.print());
    }
  }
  out.println(frame.printBefore());
  out.println(board.printBefore());
%>
<form name="topicDetailForm" action="" method="post">
  <input type="hidden" name="Action"/> <input type="hidden" name="Id" value="<%=id%>"/>
  <table cellpadding="1" cellspacing="0" border="0" width="98%">
    <tr>
      <td><!--Recherche-->
        <table cellpadding="5" cellspacing="2" border="0" width="98%">
          <tr>
            <td class="intfdcolor4" nowrap="nowrap">
              <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                  <td nowrap="nowrap" valign="middle"><input type="text" name="SearchCriteria"
                                                             id="searchInput" size="50" placeholder="<%=resources.getString("GML.search")%>"
                                                             value="<%if (searchCriteria != null) {
				out.print(searchCriteria);
			}%>"/>
                  </td>
                  <td valign="middle" id="searchButton">
                    <a class="sp_button" href="#">Ok</a>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
        <!--***************************--></td>
      <%
        CompoSpace[] instances = yellowpagesScc.getYellowPagesInstances();
        if ((instances != null) && (instances.length > 1)) {
      %>
      <td><!--Container--> <!--Acc�s aux autres annuaires-->
        <table cellpadding="2" cellspacing="1" border="0" width="100%" id="otherComponents">
          <tr>
            <td align="center" nowrap="nowrap" width="100%" height="24"><span
                class="selectNS"> <select name="select2"
                                          onchange="window.open(this.options[this.selectedIndex].value,'_self')">
					<option selected><%=resources.getString("Access")%></option>
					<%
                        for (CompoSpace instance : instances) {

                            if (!instance.getComponentId().equals(yellowpagesScc.getComponentId())) {
                                if (!portletMode) {
                                    out.println("<option value=\"" + m_context + "/Ryellowpages/" +
                                            instance.getComponentId() + "/Main\">" + instance.getSpaceLabel() +
                                            " - " + instance.getComponentLabel() + "</option>");
                                } else {
                                    out.println("<option value=\"" + m_context + "/Ryellowpages/" +
                                            instance.getComponentId() + "/portlet\">" + instance.getSpaceLabel() +
                                            " - " + instance.getComponentLabel() + "</option>");
                                }
                            }
                        }
          %>
				</select> </span></td>
          </tr>
        </table>
        <!--***************************--></td>
      <% } %>
      <% if (tree.size() > 1) { %>
      <td><!--access to the topics -->
          <span class="selectNS"> <select name="selectTopic"
                                          onchange="topicGoToSelected()">
					<%=afficheArbo(tree, id, yellowpagesScc)%>
				</select> </span>
        <!--***************************--></td>
      <% } %>
      <td>&nbsp;</td>
    </tr>
  </table>
</form>
<!--Description of the topic -->
<%
  String nodeName = null;
  String nodeDesc = null;
  if (group != null) {
    nodeName = group.getName();
    nodeDesc = group.getDescription();
  } else {
    if (!"0".equals(id)) {
      NodeDetail nodeDetail = currentTopic.getNodeDetail();
      nodeName = WebEncodeHelper.javaStringToHtmlString(nodeDetail.getName().toUpperCase());
      nodeDesc = WebEncodeHelper.javaStringToHtmlString(nodeDetail.getDescription());
    }
  }
  if (nodeDesc != null && !nodeDesc.isEmpty()) {
%>
<div>&nbsp;&nbsp;<strong><%=nodeName%>&nbsp;:&nbsp;</strong><%=nodeDesc%>
</div>
<br/>
<%
  }

  out.println(board.printAfter());
  out.println("<br/>");
  DisplayContactsHelper
      .displayContactsUser(yellowpagesScc, id, gef, request, session, resources, out);

  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
<form name="contactForm" action="contactManager.jsp" target="contactWindow" method="post">
  <input type="hidden" name="Action"/>
  <input type="hidden" name="ContactId"/>
  <input type="hidden" name="TopicId"/>
  <input type="hidden" name="Path"/>
</form>

<script type="text/javascript">
  /* declare the module myapp and its dependencies (here in the silverpeas module) */
  let myapp = angular.module('silverpeas.yellowpages',
      ['silverpeas.services', 'silverpeas.directives']);
</script>

</body>
</html>