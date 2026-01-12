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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
  response.setHeader("Cache-Control","no-store"); //HTTP 1.1
  response.setHeader("Pragma","no-cache"); //HTTP 1.0
  response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ page import="org.silverpeas.core.web.treemenu.process.TreeHandler"%>
<%@ page import="org.silverpeas.core.web.treemenu.model.MenuConstants"%>
<%@ include file="checkKmelia.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/menuTree" prefix="menuTree"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib prefix="slif" uri="http://www.silverpeas.com/tld/silverFunctions" %>

<c:set var="closeWindowOnLoad" value=""/>
<c:if test="${silfn:isDefined(requestScope.NbLinks)}">
  <c:set var="closeWindowOnLoad" value="closeAndReturn();"/>
</c:if>

<c:set var="webContext" value="${silfn:applicationURL()}"/>
<c:set var="componentURL" value="${silfn:componentURL(requestScope.PublicationPK.instanceId)}"/>
<c:set var="pubId" value="${requestScope.PublicationPK.id}"/>
<c:set var="menuinJson"><%=TreeHandler.processMenu(request,MenuConstants.SEE_ALSO_MENU_TYPE, true)%></c:set>
<c:set var="linkTitle" value="${requestScope.kmelia.getString('kmelia.linkManager.home.title')}"/>
<c:set var="linkDesc" value="${requestScope.kmelia.getString('kmelia.linkManager.home.description')}"/>
<view:setConstant
        constant="org.silverpeas.components.kmelia.model.KmeliaPublicationSort.SORT_TITLE_ASC"
        var="sort"/>
<view:setConstant constant="org.silverpeas.core.web.treemenu.model.MenuConstants.SEE_ALSO_MENU_TYPE"
                  var="menuType"/>

<view:sp-page>
<view:sp-head-part>
  <title>${requestScope.resources.getString("GML.popupTitle")}</title>
  <style>
    #pubList .selection input {
      display: block;
    }
  </style>
  <%-- load the css and js file used by tree menu --%>
  <menuTree:head displayCssFile="true"
                 displayJavascriptFile="true"
                 displayIconsStyles="false"
                 contextName="${webContext}">
  </menuTree:head>
  <%-- personalizable Javascript for  YUI treeView menu --%>
  <script src="${webContext}/util/javaScript/treeMenu/menu.js"></script>

  <script>
    const context = '${webContext}';
    const rootTopic = "0";
    let currentNodeId;
    let currentNodeIndex;
    let currentComponent;
    const pubId = '${pubId}';
    let currentSort = ${sort};
    const menuType='${menuType}';
    function buildTree() {
      let mes;
      //create a new tree:
      tree = new YAHOO.widget.TreeView("treeDiv1");
      //turn dynamic loading on for entire tree:
      tree.setDynamicLoad(loadNodeData, 0);
      //get root node for tree:
      const root = tree.getRoot();

      //add child nodes for tree; our top level nodes are
      try{
        mes =YAHOO.lang.JSON.stringify(${menuinJson});
        mes = YAHOO.lang.JSON.parse(mes);
      } catch(x) {
        notyError("JSON Parse failed: "+x);
        return;
      }
      for (let i=0, j=mes.length; i<j; i++) {
        const tempNode = new YAHOO.widget.TextNode(mes[i], root, false);
        tempNode.multiExpand =false;
      }
      //render tree with these toplevel nodes; all descendants of these nodes
      //will be generated as needed by the dynamic loader.
      tree.draw();
      //action when a user click on a node
      tree.subscribe('clickEvent',function(oArgs) {
        currentComponent = oArgs.node.data.componentId;
        // highlight selected node
        const $currentNode = $("#ygtvcontentel"+currentNodeIndex);
        $currentNode.css({'font-weight':'normal'});
        setCurrentNodeId(oArgs.node.data.id);
        currentNodeIndex = oArgs.node.index;
        $currentNode.css({'font-weight':'bold'});
        //if node is a topic, display the publications
        if (oArgs.node.data.componentId !== undefined && oArgs.node.data.nodeType === 'THEME') {
          displayPublications(oArgs.node.data.componentId, getCurrentNodeId());
          //displays the publication localized at the root of a theme tracker
        } else if (oArgs.node.data.nodeType === 'COMPONENT' &&
                oArgs.node.data.id.indexOf('kmelia') === 0) {
          setCurrentNodeId(rootTopic);
          displayPublications(oArgs.node.data.id, rootTopic);
        } else {
          displayHomeMessage();
        }
      });
    }

    function sortGoTo(selectedIndex) {
      if (selectedIndex !== 0 && selectedIndex !== 1) {
        const sort = document.publicationsForm.sortBy[selectedIndex].value;
        const ieFix = new Date().getTime();
        $.get('${webContext}/RAjaxPublicationsListServlet', {
          TopicToLinkId: getCurrentNodeId(),
          ComponentId: currentComponent,
          ToLink: 1,
          Sort: sort,
          IEFix: ieFix
        }, function(data) {
          currentSort = sort;
          $('#pubList').html(data);
        }, "html");
      }
    }

    function getCurrentNodeId(){
      return currentNodeId;
    }

    function setCurrentNodeId(id){
      currentNodeId = id;
    }

    function doPagination(index, nbItemsPerPage){
      const ieFix = new Date().getTime();
      $.get('${webContext}/RAjaxPublicationsListServlet', {
          Index: index,
          NbItemsPerPage: nbItemsPerPage,
          ComponentId: currentComponent,
          ToLink: 1,
          Sort: currentSort,
          IEFix: ieFix
        },
        function(data){
          $('#pubList').html(data);
        },"html");
    }

    function closeAndReturn() {
      window.opener.location.href = "${webContext}${componentURL}ViewPublication?PubId="+pubId;
      window.close();
    }

    function displayPublications(CompoId,topicId){
      const ieFix = new Date().getTime();
      $.get('${webContext}/RAjaxPublicationsListServlet', {
          ComponentId: CompoId,
          TopicToLinkId: topicId,
          ToLink: 1,
          Sort: currentSort,
          IEFix: ieFix},
        function(data){
          $('#pubList').html(data);
        },"html");
    }

    function sendPubId(pubId,checked){
      let action = checked ? "Action=bindToPub" : "Action=unbindToPub";
      const ieFix = new Date().getTime();
      $.get('${webContext}/KmeliaAJAXServlet?'+action, {
        TopicToLinkId: pubId,
        IEFix: ieFix
        });
    }

    function linkTo(){
      location.href = "${webContext}${componentURL}AddLinksToPublication?PubId=${pubId}"
    }

    function displayHomeMessage(){
      document.getElementById('pubList').innerHTML =
              '<p style="text-align: center">${linkTitle}</p> <p style="text-align: center"><br><br>${linkDesc}';
    }

    function showPublicationOperations() {
      // not implemented but necessary to prevent javascript errors
    }

    function hidePublicationOperations () {
      // not implemented but necessary to prevent javascript errors
    }
  </script>
</view:sp-head-part>

<view:sp-body-part cssClass="yui-skin-sam" onLoad="${closeWindowOnLoad}">

<table class="dimensionTable">
  <th></th>
  <tr style="vertical-align: top">
    <td>
      <div id="treeDiv1" class="treeDivDisplay"> </div>
    </td>
    <td class="secondTd">
      <div id="pubList" class="publistDisplay">
        <p style="text-align: center">${linkTitle}</p>
        <p style="text-align: center"><br><br>${linkDesc}</p>
      </div>
      <div class="center">
        <view:buttonPane>
          <view:button label="${requestScope.resources.getString('GML.close')}"
                       action="javascript:window.close();"/>
          <view:button label="${requestScope.resources.getString('GML.linkTo')}"
                       action="javaScript:linkTo();"/>
        </view:buttonPane>
      </div>
    </td>
  </tr>
</table>

</view:sp-body-part>
</view:sp-page>