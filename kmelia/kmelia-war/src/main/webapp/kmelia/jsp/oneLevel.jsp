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

<%@ include file="checkKmelia.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/kmelia" prefix="kmelia" %>
<%@page import="org.silverpeas.components.kmelia.SearchContext"%>
<%@page import="org.silverpeas.core.admin.user.model.SilverpeasRole"%>
<%@ page import="org.silverpeas.core.i18n.I18NHelper" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window" %>
<%@ page import="org.silverpeas.core.webapi.node.NodeType" %>

<c:url var="mandatoryFieldUrl" value="/util/icons/mandatoryField.gif"/>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var='highestUserRole' value='<%=SilverpeasRole.fromString((String) request.getAttribute("Profile"))%>'/>
<c:set var="displaySearch" value="${silfn:booleanValue(requestScope.DisplaySearch)}"/>
<c:set var="componentId" value="<%=componentId%>"/>
<%
  String    rootId        = "0";

  String   profile      = (String) request.getAttribute("Profile");
  String  translation   = (String) request.getAttribute("Language");
  boolean displayNBPublis = ((Boolean) request.getAttribute("DisplayNBPublis")).booleanValue();
  Boolean rightsOnTopics  = (Boolean) request.getAttribute("RightsOnTopicsEnabled");
  int    currentPageIndex = (Integer) request.getAttribute("PageIndex");

  SearchContext searchContext = (SearchContext) request.getAttribute("SearchContext");

  String id     = (String) request.getAttribute("CurrentFolderId");
  String    pubIdToHighlight  = (String) request.getAttribute("PubIdToHighlight"); //used when we have found publication from search (only toolbox)
  String language = kmeliaScc.getLanguage();

  if (id == null) {
    id = rootId;
  }

  String userId = kmeliaScc.getUserId();
%>

<view:sp-page>
  <view:sp-head-part withCheckFormScript="true">
    <view:script src="/util/javaScript/browseBarComplete.js"/>
    <view:includePlugin name="datepicker" />
    <view:includePlugin name="subscription"/>
    <view:includePlugin name="preview"/>
    <view:includePlugin name="rating" />
    <view:includePlugin name="basketSelection"/>

    <view:script src="javaScript/navigation.js"/>
    <view:script src="javaScript/searchInTopic.js"/>
    <view:script src="javaScript/publications.js"/>

    <script type="text/javascript">
      var isSearchTopicEnabled = ${displaySearch};

      <%--The below triggered function has to be defined as soon as possible in HTML code in order to
      increase chances to perform the treatment when "menuRender" event is fired --%>
      $(document).ready(function() {
        menuRenderedPromise.then(function(){

          sp.i18n.load({
            bundle : 'org.silverpeas.kmelia.multilang.kmeliaBundle',
            language : '<%=language%>'
          });
          displayTopicContent('<%=id%>');
        });
      });

      function topicGoTo(id) {
        closeWindows();
        displayTopicContent(id);

      }

      function getCurrentUserId() {
        return "<%=userId%>";
      }

      function getWebContext() {
        return "<%=m_context%>";
      }

      function getComponentId() {
        return "${componentId}";
      }

      function getComponentLabel() {
        return "<%=WebEncodeHelper.javaStringToJsString(componentLabel)%>";
      }

      function getLanguage() {
        return "<%=language%>";
      }

      function getPubIdToHighlight() {
        return "<%=pubIdToHighlight%>";
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

      function focusOnSearch()
      {
        $("#topicQuery").focus();
      }
    </script>
  </view:sp-head-part>

  <view:sp-body-part cssClass="yui-skin-sam treeView" id="${componentId}">
    <div compile-directive style="display: none"></div>
    <div id="${componentId}" class="<%=profile%>">
      <%
        Window window = gef.getWindow();
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setI18N("GoToCurrentTopic", translation);

        //Display operations - following lines are mandatory to init menu correctly
        OperationPane operationPane = window.getOperationPane();
        operationPane.addOperation("", resources.getString("FavoritesAdd1")+" "+kmeliaScc.getString("FavoritesAdd2"), "javaScript:addCurrentNodeAsFavorite()");

        out.println(window.printBefore());
      %>
      <view:frame>
        <div id="subTopics"></div>

        <kmelia:searchZone enabled="${displaySearch}"/>

        <div id="topicDescription" class="rich-content"></div>
        <view:areaOfOperationOfCreation/>
        <div class="dragAndDropUpload" style="min-height: 75px">
          <div id="pubList">
            <br/>
            <view:board>
              <br/>
              <center><%=resources.getString("kmelia.inProgressPublications") %>
                <br/><br/><img src="<%=resources.getIcon("kmelia.progress") %>"/></center>
              <br/>
            </view:board>
          </div>
        </div>
        <div id="footer" class="txtBaseline"></div>
      </view:frame>
      <%
        out.println(window.printAfter());
      %>

      <form name="topicDetailForm" method="post">
        <input type="hidden" name="Id" value="<%=id%>"/>
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
        var icons = new Object();
        icons["permalink"] = "<%=resources.getIcon("kmelia.link")%>";
        icons["operation.addTopic"] = "<%=resources.getIcon("kmelia.operation.addTopic")%>";
        icons["operation.addPubli"] = "<%=resources.getIcon("kmelia.operation.addPubli")%>";
        icons["operation.importFile"] = "<%=resources.getIcon("kmelia.operation.importFile")%>";
        icons["operation.importFiles"] = "<%=resources.getIcon("kmelia.operation.importFiles")%>";
        icons["operation.subscribe"] = "<%=resources.getIcon("kmelia.operation.subscribe")%>";
        icons["operation.favorites"] = "<%=resources.getIcon("kmelia.operation.favorites")%>";

        var params = new Object();
        params["rightsOnTopic"] = <%=rightsOnTopics.booleanValue()%>;
        params["i18n"] = <%=I18NHelper.isI18nContentActivated%>;
        params["nbPublisDisplayed"] = <%=displayNBPublis%>;

        var searchInProgress = <%=searchContext != null%>;
        var searchFolderId = "<%=id%>";

        function getComponentPermalink() {
          return "<%=URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, componentId)%>";
        }

        function copyCurrentNode()  {
          top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>copy?Object=Node&Id='+getCurrentNodeId();
        }

        function cutCurrentNode() {
          top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>cut?Object=Node&Id='+getCurrentNodeId();
        }

        function changeCurrentTopicStatus() {
          changeStatus(getCurrentNodeId(), getCurrentTopicStatus());
        }

        function updateUIStatus(nodeId, newStatus) {
          setCurrentTopicStatus(newStatus);
          displayOperations(nodeId);
        }

        function displayTopicContent(id) {
          if (id !== searchFolderId) {
            // search session is over
            searchInProgress = false;
          }

          if (!searchInProgress) {
            clearSearchQuery();
          }

          setCurrentNodeId(id);

          if (isSpecialFolder(id) || id === "1") {
            muteDragAndDrop(); //mute dropzone
            $("#footer").css({'visibility':'hidden'}); //hide footer
            $("#searchZone").css({'display':'none'}); //hide search
            $("#subTopics").empty();
            if (id === getToValidateFolderId()) {
              hideOperations();

              //update breadcrumb
              removeBreadCrumbElements();
              addBreadCrumbElement("#", "<%=resources.getString("ToValidate")%>");
            } else if (id === getNonVisiblePubsFolderId())  {
              hideOperations();

              //update breadcrumb
              removeBreadCrumbElements();
              addBreadCrumbElement("#", "<%=resources.getString("kmelia.folder.nonvisiblepubs")%>");
            } else {
              displayPath(id);
              displayOperations(id);
            }
            displayPublications(id);

          } else {
            if (searchInProgress) {
              doPagination(<%=currentPageIndex%>);
            } else {
              displayPublications(id);
            }
            displayPath(id);
            displayOperations(id);
            $("#searchZone").css({'display':'block'});
            displaySubTopics(id);
          }

          //display topic information
          displayTopicInformation(id);

          //display topic rich description
          displayTopicDescription(id);

          if (isSearchTopicEnabled) {
            setTimeout(focusOnSearch,500);
          }

        }


        function displaySubTopics(id) {
          var sUrl = "<%=m_context%>/services/folders/<%=componentId%>/"+id+"/children?lang="+getTranslation();
          $.ajax(sUrl, {
            type: 'GET',
            dataType : 'json',
            async : false,
            cache : false,
            success : function(data){
              $("#subTopics").empty();
              $("#subTopics").append("<ul>");
              var basket = "";
              var tovalidate = "";
              var nonVisiblePubs = "";
              $.each(data, function(i, folder) {
                var folderId = folder.attr["id"];
                if (folderId === "1") {
                  basket = getSubFolder(folder);
                } else if (folderId === getToValidateFolderId()) {
                  tovalidate = getSubFolder(folder);
                } else if (folderId === getNonVisiblePubsFolderId()) {
                  nonVisiblePubs = getSubFolder(folder);
                } else if (folderId !== "2") {
                  $("#subTopics ul").append(getSubFolder(folder));
                }
              });
              if (id === "0") {
                $("#subTopics ul").append(tovalidate);
                $("#subTopics ul").append(nonVisiblePubs);
                $("#subTopics ul").append(basket);
              }
              $("#subTopics").append("</ul>");
              if ($("#subTopics ul li").length > 0) {
                $("#subTopics").append("<br clear=\"all\">");
              } else {
                $("#subTopics").empty();
              }
            },
            error : function(data) {
              //alert("error");
            }
          });
        }

        function getSubFolder(folder) {
          var id = folder.attr["id"];
          var nbItems = folder.attr["nbItems"];
          var name = folder.text;
          var desc = folder.attr["description"];
          var folderType = folder["type"];

          var str = '<li id="topic_'+id+'">';
          str += '<a href="#" onclick="topicGoTo(\''+id+'\')" ';
          if (id === getToValidateFolderId()) {
            str += 'class="toValidate"';
          } else if (id === getNonVisiblePubsFolderId()) {
            str += 'class="nonVisiblePubs"';
          } else if (id === "1") {
            str += 'class="trash"';
          } else if (folderType === '<%=NodeType.FOLDER_WITH_RIGHTS%>') {
            str += 'class="folder-with-rights"';
          }
          str += '>';
          str += '<strong>'+name+' ';
          if (nbItems && typeof(nbItems) !== "undefined") {
            str += '<span>'+nbItems+'</span>';
          }
          str += '</strong>';
          if (typeof(desc) !== "undefined" && desc.length > 0) {
            str += '<span title="'+desc+'">'+desc+'</span>';
          }
          str += '</a>';
          str += '</li>';
          return str;
        }

        function getString(key) {
          return sp.i18n.get(key);
        }
      </script>
    </div>
    <div id="visibleInvisible-message" style="display: none;">
      <p>
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
            <td><input type="text" name="Name" id="folderName" maxlength="60"/>
              <input type="hidden" name="ParentId" id="parentId"/>
              <input type="hidden" name="ChildId" id="topicId"/>&nbsp;<img border="0" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5"/></td>
          </tr>

          <tr>
            <td class="txtlibform"><fmt:message key="TopicDescription" /> :</td>
            <td><input type="text" name="Description" id="folderDescription" maxlength="200"/></td>
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
            <td colspan="2"><img border="0" alt="mandatory" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5"/> : <fmt:message key="GML.requiredField"/></td>
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
      var myapp = angular.module('silverpeas.kmelia', ['silverpeas.services', 'silverpeas.directives']);
    </script>
  </view:sp-body-part>
</view:sp-page>