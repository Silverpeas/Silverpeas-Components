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
<%@page import="org.silverpeas.components.kmelia.KmeliaPublicationHelper"%>
<%@page import="org.silverpeas.core.admin.user.model.SilverpeasRole"%>
<%@ page import="org.silverpeas.core.i18n.I18NHelper" %>
<%@ page import="org.silverpeas.core.webapi.node.NodeType" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkKmelia.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/kmelia" prefix="kmelia" %>

<c:url var="mandatoryFieldUrl" value="/util/icons/mandatoryField.gif"/>
<fmt:setLocale value="${requestScope.kmelia.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var='highestUserRole' value='<%=SilverpeasRole.fromString((String) request.getAttribute("Profile"))%>'/>
<view:setConstant var="adminRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.ADMIN"/>
<c:set var="displaySearch" value="${silfn:booleanValue(requestScope.DisplaySearch)}"/>
<c:set var="componentId" value="<%=componentId%>"/>

<fmt:message key="GML.ForbiddenAccessContent" var="labelForbiddenAccess"/>

<c:set var="folderId" value="${requestScope.CurrentFolderId}"/>
<c:if test="${silfn:isNotDefined(folderId)}">
  <c:set var="folderId" value="0"/>
</c:if>

<c:set var="kmelia" value="${requestScope.kmelia}"/>
<jsp:useBean id="kmelia" type="org.silverpeas.components.kmelia.control.KmeliaSessionController"/>

<c:set var="profile" value="${requestScope.Profile}"/>
<c:set var="rightsOnTopics" value="${requestScope.RightsOnTopicsEnabled}"/>
<c:set var="displayNBPublis" value="${requestScope.DisplayNBPublis}"/>
<c:set var="searchContext" value="${requestScope.SearchContext}"/>
<c:set var="currentPageIndex" value="${requestScope.PageIndex}"/>
<c:set var="pubIdToHighlight" value="${requestScope.PubIdToHighlight}"/>
<c:set var="userId" value="${kmelia.userId}"/>

<c:set var="userCanManageRoot" value="${'admin'.equalsIgnoreCase(profile)}"/>
<c:set var="userCanManageTopics"
       value="${rightsOnTopics || userCanManageRoot || kmelia.topicManagementDelegated}"/>
<c:set var="userCanEmptyTrash" value="${kmelia.isSuppressionAllowed(profile)}"/>
<c:set var="isPasteNodeAllowed" value="${kmelia.pasteNodeAllowed}"/>
<c:set var="isPastePublicationAllowed" value="${kmelia.isPastePublicationAllowed(folderId)}"/>

<%
  String translation = (String) request.getAttribute("Language");
%>
<view:sp-page>
<view:sp-head-part withCheckFormScript="true">
  <view:script src="/util/javaScript/browseBarComplete.js"/>
  <view:script src="/util/javaScript/jquery/jstree.min.js"/>
  <view:script src="/util/javaScript/jquery/jquery.cookie.js"/>
  <view:link href="/util/javaScript/jquery/themes/default/style.min.css"/>

  <view:includePlugin name="subscription"/>
  <view:includePlugin name="preview"/>
  <view:includePlugin name="rating" />
  <view:includePlugin name="basketSelection"/>

  <view:script src="javaScript/navigation.js"/>
  <view:script src="javaScript/searchInTopic.js"/>
  <view:script src="javaScript/publications.js"/>
  <script type="text/javascript">
    let isSearchTopicEnabled = ${displaySearch};

    function canEmptyTrash(nodeType) {
      const isUserCanEmptyTrash = ${userCanEmptyTrash};
      return nodeType === "bin" && isUserCanEmptyTrash;
    }

    function topicGoTo(id) {
      closeWindows();
      displayTopicContent(id);
      getTreeview().deselect_all();
      const node = getTreeview().get_node('#' + id);
      getTreeview().select_node(node);
    }

    function getCurrentUserId() {
      return "${userId}";
    }

    function getWebContext() {
      return webContext;
    }

    function getComponentId() {
      return "${componentId}";
    }

    function getComponentLabel() {
      return "<%=WebEncodeHelper.javaStringToJsString(WebEncodeHelper.javaStringToHtmlString(componentLabel))%>";
    }

    function getLanguage() {
      return "${kmelia.language}";
    }

    function getPubIdToHighlight() {
      return "${pubIdToHighlight}";
    }

    function getTranslation() {
      return "<%=translation%>";
    }

    function getToValidateFolderId() {
      return "<%=KmeliaHelper.SPECIALFOLDER_TOVALIDATE%>";
    }

    function getNonVisiblePubsFolderId() {
      return "<%=KmeliaHelper.SPECIALFOLDER_NONVISIBLEPUBS%>";
    }

    function isSpecialFolder(id) {
      return id === getToValidateFolderId() || id === getNonVisiblePubsFolderId();
    }

    function getCurrentFolderId() {
      return "${folderId}";
    }

    function arePublicationsOnRootAllowed() {
      return <%=KmeliaPublicationHelper.isPublicationsOnRootAllowed(componentId)%>;
    }

    const icons = {};
    icons["permalink"] = "<%=resources.getIcon("kmelia.link")%>";
    icons["operation.addTopic"] = "<%=resources.getIcon("kmelia.operation.addTopic")%>";
    icons["operation.addPubli"] = "<%=resources.getIcon("kmelia.operation.addPubli")%>";
    icons["operation.importFile"] = "<%=resources.getIcon("kmelia.operation.importFile")%>";
    icons["operation.importFiles"] = "<%=resources.getIcon("kmelia.operation.importFiles")%>";
    icons["operation.subscribe"] = "<%=resources.getIcon("kmelia.operation.subscribe")%>";
    icons["operation.favorites"] = "<%=resources.getIcon("kmelia.operation.favorites")%>";

    const params = {};
    params["rightsOnTopic"] = ${rightsOnTopics};
    params["i18n"] = <%=I18NHelper.isI18nContentActivated%>;
    params["nbPublisDisplayed"] = ${displayNBPublis};

    let searchInProgress = ${searchContext != null};
    let searchFolderId = "${folderId}";
  </script>
</view:sp-head-part>
<view:sp-body-part cssClass="yui-skin-sam treeView" id="${componentId}">
  <div compile-directive style="display: none"></div>
  <div id="<%=componentId %>" class="${profile}">
  <%
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setI18N("GoToCurrentTopic", translation);

    //Display operations - following lines are mandatory to init menu correctly
    OperationPane operationPane = window.getOperationPane();
    operationPane.addOperation("useless", resources.getString("FavoritesAdd1")+" "+resources.getString("FavoritesAdd2"), "javaScript:addCurrentNodeAsFavorite()");

    out.println(window.printBefore());
  %>
  <view:frame>
    <div class="wrap">
      <div class="resizable resizable1"><div id="treeDiv1"></div></div>
      <div id="rightSide" class="resizable resizable2">
        <kmelia:searchZone enabled="${displaySearch}"/>
        <div id="topicDescription" class="rich-content"></div>
        <view:areaOfOperationOfCreation/>
        <div class="dragAndDropUpload" style="min-height: 75px">
          <div id="pubList">
            <br/>
            <view:board>
              <br/>
              <div class="center"><%=resources.getString("kmelia.inProgressPublications") %>
                <br/><br/>
                <img alt="progress" src="<%=resources.getIcon("kmelia.progress")%>"/>
              </div>
              <br/>
            </view:board>
          </div>
        </div>
        <div id="footer" class="txtBaseline"></div>
      </div>
    </div>
  </view:frame>
  <%
    out.println(window.printAfter());
  %>

  <form name="topicDetailForm" method="post">
    <input type="hidden" name="Id" value="${folderId}"/>
    <input type="hidden" name="ChildId"/>
    <input type="hidden" name="Status"/>
    <input type="hidden" name="Recursive"/>
  </form>

  <form name="pubForm" action="ViewPublication" method="GET">
    <input type="hidden" name="PubId"/>
    <input type="hidden" id="CheckPath" name="CheckPath"/>
  </form>

  <form name="fupload" action="fileUpload.jsp" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
    <input type="hidden" name="Action" value="initial"/>
  </form>

  <script type="text/javascript">
  const rootNodeId = "0";
  const binNodeId = "1";

  function getComponentPermalink() {
    return "<%=URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, componentId, false)%>";
  }

  function deleteNode(nodeId) {
    // removing nb items displayed in nodeLabel
    const node = getTreeview().get_node('#' + nodeId);
    let nodeLabel = node.text;
    if (params["nbPublisDisplayed"]) {
      const idx = nodeLabel.lastIndexOf('(');
      if (idx > 1) {
        nodeLabel = nodeLabel.substring(0, idx-1);
      }
    }
    deleteFolder(nodeId, nodeLabel);
  }

  function getNodeTitle(nodeId) {
    let nodeTitle = getTreeview().get_text("#" + nodeId);
    const idx = nodeTitle.lastIndexOf('(');
    if (idx > 1) {
      nodeTitle = nodeTitle.substring(0, idx-1);
    }
    return nodeTitle;
  }

  function getNbPublis(nodeId) {
    const nodeLabel = getTreeview().get_text("#" + nodeId);
    const idx = nodeLabel.lastIndexOf('(');
    const nbPublis = nodeLabel.substring(idx + 1, nodeLabel.length - 1);
    return parseInt(nbPublis);
  }

  function addNbPublis(nodeId, nb) {
    const previousNbPublis = getNbPublis(nodeId);
    const nodeTitle = getNodeTitle(nodeId);
    const nbPublis = previousNbPublis + nb;
    getTreeview().rename_node("#"+nodeId, nodeTitle+" ("+nbPublis+")");
  }

  function _isInBin(nodeId) {
    let isInBin = false;
    const path = getTreeview().get_path("#" + nodeId, false, true);
    path.forEach(function (parentId) {
      if (parentId !== nodeId && parentId !== rootNodeId) {
        isInBin = isInBin || parentId === binNodeId;
      }
    });
    return isInBin;
  }

  function nodeDeleted(nodeId, isInBin) {
    if (isInBin) {
      _deleteNodeInBin(nodeId);
    } else {
      const parentId = getTreeview().get_parent("#" + nodeId);
      reloadPage(parentId);
    }
  }

  function _deleteNodeInBin(nodeId) {
    const treeview = getTreeview();
    if (params["nbPublisDisplayed"]) {
      // change nb publications on each parent of deleted node (except root)
      const nbPublisRemoved = getNbPublis(nodeId);
      const path = treeview.get_path("#" + nodeId, false, true);
      path.forEach(function(parentId) {
        if (parentId !== nodeId) {
          const nodeTitle = getNodeTitle(parentId);
          const nbPublis = getNbPublis(parentId) - nbPublisRemoved;
          treeview.rename_node("#" + parentId, nodeTitle + " (" + nbPublis + ")");
        }
      });
    }
    treeview.delete_node("#"+nodeId);
  }

  function resetNbPublis(nodeId) {
    const nodeTitle = getNodeTitle(nodeId);
    getTreeview().rename_node("#"+nodeId, nodeTitle+" (0)");
  }

  function emptyTrash() {
    const label = "<%=kmeliaScc.getString("ConfirmFlushTrashBean")%>";
    jQuery.popup.confirm(label, function() {
      $.progressMessage();
      $.post('<%=m_context%>/KmeliaAJAXServlet', {ComponentId:'<%=componentId%>',Action:'EmptyTrash'},
          function(data){
            const treeview = getTreeview();
            $.closeProgressMessage();
            if (data === "ok") {
              if (params["nbPublisDisplayed"]) {
                // remove nb publis to root
                const nbPublisDeleted = getNbPublis(binNodeId);
                addNbPublis(rootNodeId, -nbPublisDeleted);
                // set nb publis on bin to 0
                resetNbPublis(binNodeId);
              }
              displayTopicContent(binNodeId);
              const children = treeview.get_children_dom("#" + binNodeId);
              for (let i = 0; i < children.length; i++) {
                treeview.delete_node("#" + children[i].id);
              }
            } else {
              notyError(data);
            }
          }, 'text');
    });
  }

  function copyNode(id)	{
    top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>copy?Object=Node&Id='+id;
  }

  function copyCurrentNode()	{
    copyNode(getCurrentNodeId());
  }

  function cutNode(id) {
    top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>cut?Object=Node&Id='+id;
  }

  function cutCurrentNode() {
    cutNode(getCurrentNodeId());
  }

  function changeCurrentTopicStatus() {
    const node = getTreeview()._get_node("#" + getCurrentNodeId());
    changeStatus(getCurrentNodeId(), node.original.attr["status"]);
  }

  function updateUIStatus(nodeId, newStatus, recursive) {
    // updating data stored in treeview
    const node = getTreeview().get_node("#" + nodeId);
    node.original.attr["status"] = newStatus;

    //changing label style according to topic's new status
    $('#' + nodeId).removeClass("Visible Invisible").addClass(newStatus);

    //
    if (recursive === binNodeId) {
      const children = node.children;
      for (let i=0; i<children.length; i++) {
        try {
          updateUIStatus(children[i].id, newStatus, recursive);
        } catch (e) {
        }
      }
    }

    if (nodeId === getCurrentNodeId()) {
      // refreshing operations of current folder
      displayOperations(nodeId);
    }
  }

  function displayTopicContent(id) {
    if (id !== searchFolderId) {
      // search session is over
      searchInProgress = false;
    }

    setCurrentNodeId(id);

    if (!searchInProgress) {
      clearSearchQuery();
    }

    if (isSpecialFolder(id) || id === binNodeId) {
      muteDragAndDrop(); //mute dropzone
      $("#footer").css({'visibility':'hidden'}); //hide footer
      $("#searchZone").css({'display':'none'}); //hide search

      if (id === getToValidateFolderId())	{
        hideOperations();

        //update breadcrumb
        removeBreadCrumbElements();
        addBreadCrumbElement("#", "<%=resources.getString("ToValidate")%>");
      } else if (id === getNonVisiblePubsFolderId()) {
        hideOperations();

        //update breadcrumb
        removeBreadCrumbElements();
        addBreadCrumbElement("#", "<%=resources.getString("kmelia.folder.nonvisiblepubs")%>");
      } else {
        displayOperations(id);
        displayPath(id);
      }
      displayPublications(id);
    } else {
      displayPath(id);
      displayOperations(id);
      $("#searchZone").css({'display':'block'});
      if (searchInProgress) {
        doPagination(${currentPageIndex});
      } else {
        displayPublications(id);
      }
    }

    //display topic information
    displayTopicInformation(id);

    //display topic rich description
    displayTopicDescription(id);

    if (isSearchTopicEnabled)
      $("#topicQuery").focus();

  }

  let rightClickHelpAlreadyShown = false;

  function showRightClickHelp() {
    const rightClickCookieName = "Silverpeas_GED_RightClickHelp";
    const rightClickCookieValue = $.cookie(rightClickCookieName);
    if (!rightClickHelpAlreadyShown && "IKnowIt" !== rightClickCookieValue) {
      rightClickHelpAlreadyShown = true;
      $( "#rightClick-message" ).dialog({
        modal: true,
        resizable: false,
        width: 400,
        dialogClass: 'help-modal-message',
        buttons: {
          "<%=resources.getString("kmelia.help.rightclick.buttons.ok") %>": function() {
            $.cookie(rightClickCookieName, "IKnowIt", { expires: 3650, path: '/', secure: ${pageContext.request.secure} });
            $( this ).dialog( "close" );
          },
          "<%=resources.getString("kmelia.help.rightclick.buttons.remind") %>": function() {
            $( this ).dialog( "close" );
          }
        }
      });
    }
  }

  function getTreeview() {
    return $.jstree.reference("#treeDiv1");
  }

  function customMenu(node) {
    <c:choose>
    <c:when test="${!userCanManageTopics}">
    //user can not manage folders
    return false;
    </c:when>
    <c:otherwise>
    const nodeType = node.type;
    let userRole = '${profile}';
    if (params["rightsOnTopic"]) {
      userRole = node.original.attr["role"];
    }
    if (isSpecialFolder(nodeType)) {
      return false;
    } else if (canEmptyTrash(nodeType)) {
      return {
        emptyItem: {
          label: "<%=resources.getString("EmptyBasket")%>",
          action: function () {
            emptyTrash();
          }
        }
      };
    }

    // The default set of all items
    const items = {
      addItem: {
        label: "<%=resources.getString("CreerSousTheme")%>",
        action: function (obj) {
          const node = getTreeview().get_node(obj.reference);
          topicAdd(node.id, false);
        }
      },
      editItem: {
        label: "<%=resources.getString("ModifierSousTheme")%>",
        action: function (obj) {
          const node = getTreeview().get_node(obj.reference);
          const url = getWebContext() + "/services/folders/" + getComponentId() + "/" + node.id;
          $.getJSON(url, function (topic) {
            const name = topic.text;
            const desc = topic.attr["description"];
            if (params["i18n"]) {
              storeTranslations(topic.translations);
            } else {
              setDataInFolderDialog(name, desc);
            }
            topicUpdate(topic.id);
          });
        }
      },
      deleteItem: {
        label: "<%=resources.getString("SupprimerSousTheme")%>",
        action: function (obj) {
          const node = getTreeview().get_node(obj.reference);
          deleteNode(node.id);
        }
      },
      sortItem: {
        label: "<%=resources.getString("kmelia.SortTopics")%>",
        action: function (obj) {
          const node = getTreeview().get_node(obj.reference);
          closeWindows();
          SP_openWindow("ToOrderTopics?Id=" + node.id, "topicAddWindow", "600", "500", "directories=0,menubar=0,toolbar=0,scrollbars=1,alwaysRaised,resizable");
        },
        "separator_after": true
      },
      <% if (kmeliaScc.isPdcUsed()) { %>
      pdcItem: {
        label: "<%=resources.getString("GML.PDCPredefinePositions")%>",
        action: function (obj) {
          const node = getTreeview().get_node(obj.reference);
          openPredefinedPdCClassification(parseInt(node.id));
        },
        "separator_after": true
      },
      <% } %>
      copyItem: {
        label: "<%=resources.getString("kmelia.operation.folder.copy")%>",
        action: function (obj) {
          const node = getTreeview().get_node(obj.reference);
          copyNode(node.id);
        }
      },
      cutItem: {
        label: "<%=resources.getString("kmelia.operation.folder.cut")%>",
        action: function (obj) {
          const node = getTreeview().get_node(obj.reference);
          cutNode(node.id);
        }
      }
      <c:if test="${isPasteNodeAllowed || isPastePublicationAllowed}">
      , pasteItem: {
        label: "<%=resources.getString("GML.paste")%>",
        action: function (obj) {
          const node = getTreeview().get_node(obj.reference);
          pasteNode(node.id);
        },
        "separator_after": true
      }
      </c:if>
      <% if (kmeliaScc.isWysiwygOnTopicsEnabled()) { %>
      ,
      wysiwygItem: {
        label: "<%=resources.getString("TopicWysiwyg")%>",
        action: function (obj) {
          const node = getTreeview().get_node(obj.reference);
          updateTopicWysiwyg(node.id);
        }
      }
      <% } %>
    };

    if (nodeType === "root") {
      <c:choose>
        <c:when test="${!userCanManageRoot}">
      // user can not manage root folder
      return false;
        </c:when>
        <c:otherwise>
      delete items.editItem;
      delete items.deleteItem;
      delete items.copyItem;
      delete items.cutItem;
      delete items.statusItem;
        </c:otherwise>
      </c:choose>
    }

    if (items.statusItem) {
      if (node.original.attr["status"] === "Invisible") {
        items.statusItem.label = "<%=resources.getString("TopicInvisible2Visible")%>";
      }
    }

    if (userRole === "admin") {
      // all actions are allowed
    } else if (kmeliaWebService.getUserProfileSynchronously(rootNodeId) === "admin") {
      // a minimal contextual menu is always available for app admins
      getMinimalContextualMenuForAdmins(items);
    } else if (userRole === "user") {
      const parentProfile = getTreeview()._get_parent(node).attr("role");
      if (parentProfile !== "admin") {
        //do not show the menu
        return;
      } else {
        // a minimal contextual menu is always available for folder admins
        getMinimalContextualMenuForAdmins(items);
      }
    } else {
      let isTopicManagementDelegated = <%=kmeliaScc.isTopicManagementDelegated()%>;
      const userId = "<%=kmeliaScc.getUserId()%>";
      const creatorId = node.original.attr["creatorId"];
      if (isTopicManagementDelegated && userRole !== "admin") {
        if (creatorId !== userId) {
          //do not show the menu
          return;
        } else if (creatorId === userId) {
          if (items.pdcItem) {
            items.pdcItem._disabled = true;
          }
          items.copyItem._disabled = true;
          items.cutItem._disabled = true;
          items.pasteItem._disabled = true;
          if (items.wysiwygItem) {
            items.wysiwygItem._disabled = true;
          }
          if (items.statusItem) {
            items.statusItem._disabled = true;
          }
        }
      } else {
        if (userRole !== "admin") {
          //do not show the menu
          return;
        }
      }
    }
    return items;
      </c:otherwise>
    </c:choose>
  }

  function getMinimalContextualMenuForAdmins(items) {
    items.addItem._disabled = true;
    items.sortItem._disabled = true;
    if (items.pdcItem) {
      items.pdcItem._disabled = true;
    }
    items.copyItem._disabled = true;
    items.cutItem._disabled = true;
    items.pasteItem._disabled = true;
    if (items.wysiwygItem) {
      items.wysiwygItem._disabled = true;
    }
    if (items.statusItem) {
      items.statusItem._disabled = true;
    }
  }

  function decorateNodeName(node) {
    <%-- This data is filled only if the application parameter 'displayNB' is activated --%>
    const nbPublicationsTheNodeContains = node.attr['nbItems'];
    if (nbPublicationsTheNodeContains) {
      node.text = node.text + " (" + nbPublicationsTheNodeContains + ")";
    }
  }

  function spreadNbItems(children) {
    if (children) {
      for(let i = 0; i < children.length; i++) {
        let child = children[i];
        child.a_attr = { title: child.attr['description'].unescapeHTML()};
        decorateNodeName(child);
        if (child.children && child.children.length > 0) {
          spreadNbItems(child.children);
        } else {
          child.children = true;
        }
      }
    }
  }

  function publicationMovedInError(id, data) {
    let pubName = getPublicationName(id);
    notyError("<%=resources.getString("kmelia.drag.publication.error1")%>" + pubName + "<%=resources.getString("kmelia.drag.publication.error2")%>" + "<br/><br/>" + data);
  }

  function getPublicationName(id) {
    return $("#pubList #pub-"+id).html();
  }

  function extractPublicationId(id) {
    return id.substring(4, id.length);
  }

  function extractFolderId(id) {
    if (id) {
      let idx = id.indexOf('_');
      if (idx > 0) {
        id = id.substring(0, idx);
      }
    }
    return id;
  }

  function publicationMovedSuccessfully(id, targetId) {
    let pubName = getPublicationName(id);
    notySuccess("<%=resources.getString("kmelia.drag.publication.success1")%>" + pubName + "<%=resources.getString("kmelia.drag.publication.success2")%>");

    if (params["nbPublisDisplayed"]) {
      // add one publi to target node and its parents
      let path = getTreeview().get_path("#"+targetId, false, true);
      for (let i=0; i<path.length; i++) {
        let parentId = path[i];
        if (parentId !== rootNodeId) {
          addNbPublis(parentId, 1);
        }
      }

      // remove one publi to current node and its parents
      path = getTreeview().get_path("#"+getCurrentNodeId(), false, true);
      for (let i=0; i<path.length; i++) {
        let parentId = path[i];
        if (parentId !== rootNodeId) {
          addNbPublis(parentId, -1);
        }
      }
    }

    try {
      // remove one publi to publications header
      let previousNb = parseInt($("#pubsHeader #pubsCounter span").html());
      if (previousNb === 1) {
        $("#pubsHeader #pubsCounter").html("<%=resources.getString("GML.publications")%>");
        $("#pubsHeader #pubsSort").hide();
        $("#pubList ul").html("<%=resources.getString("PubAucune")%>")
      } else {
        $("#pubsHeader #pubsCounter span").html(previousNb - 1);
      }
    } catch (e) {

    }

    // remove publication from publications list
    $("#pubList #pub-"+id).closest("li").fadeOut('500', function() {
      $(this).remove();
    });
  }

  function publicationsRemovedSuccessfully(nb) {
    const pubCount = parseInt(nb);
    if (params["nbPublisDisplayed"]) {
      if (getCurrentNodeId() === binNodeId) {
        // publications are definitively removed
        // remove nb publis from trash and root folders
        addNbPublis(binNodeId, -pubCount);
        addNbPublis(rootNodeId, -pubCount);
      } else {
        // publications goes to trash
        // remove nb publi to current node and its parents except root
        const path = getTreeview().get_path("#" + getCurrentNodeId(), false, true);
        for (let i=0; i<path.length; i++) {
          const parentId = path[i];
          if (parentId !== rootNodeId) {
            addNbPublis(parentId, -pubCount);
          }
        }

        // add nb publi to trash
        addNbPublis(binNodeId, pubCount);
      }
    }
  }

  function getString(key) {
    return sp.i18n.get(key);
  }

  $(document).ready(function() {
        //build the tree
        $("#treeDiv1").jstree({
          "core" : {
            force_text : false,
            "data" : {
              "url" : function (node) {
                let url = getWebContext() + "/services/folders/" + getComponentId() + "/" +
                        getCurrentFolderId() + "/treeview?lang=" + getTranslation() + "&IEFix=" +
                        new Date().getTime();
                if (node && node.id !== '#') {
                  url = getWebContext() + "/services/folders/" + getComponentId() + "/" + node.id +
                      "/children?lang=" + getTranslation() + "&IEFix=" + new Date().getTime();
                }
                return url;
              },
              "success" : function(node) {
                if (node) {
                  if (node.length) {
                    // this is subfolders
                    spreadNbItems(node);
                  } else {
                    if (node.text) {
                      // this is the root
                      node.text = "<%=WebEncodeHelper.javaStringToHtmlString(componentLabel)%>";
                      decorateNodeName(node);
                      spreadNbItems(node.children);
                    }
                  }
                }
                return node;
              }
            },
            "check_callback" : true,
            "themes" : {
              "dots" : false,
              "icons" : true
            },
            "multiple" : false
          },
          // Using types - most of the time this is an overkill
          // read the docs carefully to decide whether you need types
          "types" : {
            "#" : {
              // I set both options to -2, as I do not need depth and children count checking
              // Those two checks may slow jstree a lot, so use only when needed
              "max_depth" : -2, // I want only `drive` nodes to be root nodes
              "max_children" : -2, // This will prevent moving or creating any other type as a root node
              "valid_children" : ["root"],
            },
            // The `root` node
            "root" : {
              // those prevent the functions with the same name to be used on `root` nodes
              "valid_children" : ["bin", getToValidateFolderId(), getNonVisiblePubsFolderId()]
            },
            // The `bin` node
            "bin" : {
              // can have files and folders inside, but NOT other `drive` nodes
              "valid_children" : -1,
              "icon" : getWebContext() + "/util/icons/treeview/basket.jpg"
            },
            // The `to validate` node
            "<%=KmeliaHelper.SPECIALFOLDER_TOVALIDATE%>" : {
              // can have files only
              "valid_children" : -1,
              "icon" : getWebContext() + "/util/icons/ok_alpha.gif"
            },
            // The 'non visible publications' node
            "<%=KmeliaHelper.SPECIALFOLDER_NONVISIBLEPUBS%>" : {
              // can have files only
              "valid_children" : -1,
              "icon" : getWebContext() + "/util/icons/masque.gif"
            },
            "folder" : {
              // those prevent the functions with the same name to be used on `root` nodes
              "valid_children" : ["folder"]
            },
            "<%=NodeType.FOLDER_WITH_RIGHTS%>" : {
              // those prevent the functions with the same name to be used on `root` nodes
              "icon" : getWebContext() + "/util/icons/treeview/folder-bicolor.png",
              "valid_children" : ["folder","<%=NodeType.FOLDER_WITH_RIGHTS%>"]
            }
          },
          "contextmenu" : {
            "show_at_node" : false,
            "items" : customMenu
          },
          "dnd" : {
            "is_draggable" : false
            //"check_while_dragging": true,
            //"use_html5" : false
          },
          // the `plugins` array allows you to configure the active plugins on this instance
          "plugins" : ["types", "contextmenu", "dnd"]
        }).on("loaded.jstree", function(event, data) {
          sp.log.debug('jstree loaded');
        }).on("ready.jstree", function(event, data) {
          const node = data.instance.get_node('#' + getCurrentFolderId());
          if (node) {
            data.instance.select_node(node);
          } else {
            $('#pubList').html('<div class=\"inlineMessage-nok\">${silfn:escapeJs(labelForbiddenAccess)}</div>');
            $("#searchZone").css({'display':'none'}); //hide search
          }
        }).on("select_node.jstree", function(e, data) {
          // data.inst is the instance which triggered this event
          const nodeId = data.node.id;

          // open topic in treeview
          if (nodeId >= 0) {
            data.instance.open_node(data.node);
          }

          // display topic content in right panel
          displayTopicContent(nodeId);
        }).on("move_node.jstree", function(e, data) {
          const pubId = extractPublicationId(data.node.id);
          const targetId = data.parent;

          movePublication(pubId, getCurrentNodeId(), targetId);
        });

        // init splitter
        $(".resizable1").resizable({
          autoHide: true,
          handles: 'e',
          maxWidth: 500,
          resize: function (e, ui) {
            const parent = ui.element.parent();
            const remainingSpace = parent.width() - ui.element.outerWidth();
            const divTwo = ui.element.next();
            const divTwoWidth = (remainingSpace - (divTwo.outerWidth() - divTwo.width())) / parent.width() * 100 + "%";
            divTwo.width(divTwoWidth);
          },
          stop: function (e, ui) {
            const parent = ui.element.parent();
            ui.element.css({
              width: ui.element.width() / parent.width() * 100 + "%"
            });
          }
        });

        sp.i18n.load({
          bundle : 'org.silverpeas.kmelia.multilang.kmeliaBundle',
          language : '${sessionScope[sessionController].language}'
        });

        <c:if test="${userCanManageRoot}">
        //Right-click concerns only admins
        showRightClickHelp();
        </c:if>
      }
  );

  // starts the dragging when a publication is holden to be dragged off
  $(document).on('mousedown', '.jstree-draggable', function(e) {
    return $.vakata.dnd.start(e,
        {
          'jstree' : true, 'obj' : $(this),
          'nodes' : [{id : $(this).attr('id'), text : $(this).text()}]
        },
        '<div id="jstree-dnd" class="jstree-default"><i class="jstree-icon jstree-er"></i>' +
        $(this).text().escapeHTML() + '</div>');
  });

  window.__spTreeviewDndContext = {
    lastTarget : {
      id : undefined,
      canDrop : undefined
    }
  };

  $(document).on("dnd_stop.vakata", function(event, data) {
    window.__spTreeviewDndContext.lastTarget = {};
    const treeview = getTreeview();
    if (!treeview.settings.dnd.check_while_dragging) {
      const target = $(data.event.target);
      const targetId = extractFolderId(target.attr('id'));
      const pubId = extractPublicationId(data.data.nodes[0].id);
      // store new parent of publication
      movePublication(pubId, getCurrentNodeId(), targetId);
    }
  }).on("dnd_move.vakata", function(event, data) {
    let target = $(data.event.target);
    let canBeDropped = false;
    const treeview = getTreeview();
    if (target.closest('#treeDiv1').length && target.hasClass('jstree-anchor')) {
      const targetId = extractFolderId(target.attr('id'));
      if (targetId && targetId !== window.__spTreeviewDndContext.lastTarget.id) {
        target = treeview.get_node('#' + targetId);
        if (isSpecialFolder(targetId) || targetId === getCurrentNodeId()) {
          canBeDropped = false;
        } else if (target.type !== 'root' || arePublicationsOnRootAllowed()) {
          const pubId = extractPublicationId(data.data.nodes[0].id);
          const sourceFolderAuthorizations = kmeliaWebService.getPublicationUserAuthorizationsSynchronously(pubId);
          if (targetId === binNodeId) {
            canBeDropped = sourceFolderAuthorizations.canBeDeleted;
          } else {
            const targetFolderAuthorizations = kmeliaWebService.getPublicationUserAuthorizationsSynchronously(pubId, targetId);
            canBeDropped = sourceFolderAuthorizations.canBeCut && targetFolderAuthorizations.canBeCut;
          }
        }
        window.__spTreeviewDndContext.lastTarget = {
          id : targetId,
          canDrop : canBeDropped
        };
      } else if (targetId) {
        canBeDropped = window.__spTreeviewDndContext.lastTarget.canDrop;
      }
    }
    treeview.settings.dnd.check_while_dragging = !canBeDropped;
  });
  </script>
  </div>
  <div id="visibleInvisible-message" style="display: none;">
    <p>
    </p>
  </div>
  <div id="rightClick-message" title="<%=resources.getString("kmelia.help.rightclick.title") %>" style="display: none;">
    <p>
      <%=resources.getStringWithParams("kmelia.help.rightclick.content", WebEncodeHelper.javaStringToHtmlString(componentLabel)) %>
    </p>
  </div>
  <div id="addOrUpdateNode" style="display: none;">
    <form name="topicForm" action="AddTopic" method="post">
      <input type="hidden" id="<%=I18NHelper.HTMLHiddenRemovedTranslationMode %>" name="<%=I18NHelper.HTMLHiddenRemovedTranslationMode %>" value="false"/>
      <table cellpadding="5" width="100%">
        <tr><td class="txtlibform"><fmt:message key="TopicPath"/> :</td>
          <td valign="top" id="path"></td>
        </tr>
        <%=I18NHelper.getFormLine(resources, null, kmeliaScc.getLanguage())%>
        <tr>
          <td class="txtlibform"><fmt:message key="TopicTitle"/> :</td>
          <td><input type="text" name="Name" id="folderName" size="60" maxlength="60"/>
            <input type="hidden" name="ParentId" id="parentId"/>
            <input type="hidden" name="ChildId" id="topicId"/>&nbsp;<img src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5" alt=""/></td>
        </tr>

        <tr>
          <td class="txtlibform"><fmt:message key="TopicDescription" /> :</td>
          <td><input type="text" name="Description" id="folderDescription" size="60" maxlength="200"/></td>
        </tr>

        <% if (kmeliaScc.isNotificationAllowed()) { %>
        <tr>
          <td class="txtlibform" valign="top"><fmt:message key="TopicAlert" /> :</td>
          <td valign="top">
            <select name="AlertType">
              <option value="NoAlert" selected="selected"><fmt:message key="NoAlert" /></option>
              <option value="Publisher"><fmt:message key="OnlyPubsAlert" /></option>
              <option value="All"><fmt:message key="AllUsersAlert" /></option>
            </select>
          </td>
        </tr>
        <% } %>
        <tr>
          <td colspan="2">( <img alt="mandatory" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5"/> : <fmt:message key="GML.requiredField"/> )</td>
        </tr>
      </table>
    </form>
  </div>

  <%@ include file="../../sharing/jsp/createTicketPopin.jsp" %>
  <view:progressMessage/>
  <kmelia:paste highestUserRole="${highestUserRole}" componentInstanceId="<%=componentId%>" />
  <kmelia:dragAndDrop highestUserRole="${highestUserRole}" componentInstanceId="<%=componentId%>" contentLanguage="<%=translation%>" />
  <script type="text/javascript">
    /* declare the module myapp and its dependencies (here in the silverpeas module) */
    const myapp = angular.module('silverpeas.kmelia', ['silverpeas.services', 'silverpeas.directives']);
  </script>
  </view:sp-body-part>
</view:sp-page>