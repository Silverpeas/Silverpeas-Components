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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<%@ include file="check.jsp"%>
<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<c:set var="currentUserLanguage" value="${requestScope.resources.language}"/>
<c:set var="sessionController" value="${requestScope.DelegatedNews}" />
<c:set var="listNewsJSON" value="${requestScope.ListNewsJSON}"/>
<c:set var="listNews" value="${requestScope.ListNews}"/>
<c:set var="isAdmin" value="${sessionController.admin}" />
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="context" value="${requestScope.context}"/>

<view:sp-page angularJsAppName="silverpeas.delegatedNews">
  <view:sp-head-part withCheckFormScript="true">
    <view:includePlugin name="datepicker"/>
    <view:includePlugin name="toggle"/>
    <script type="text/javascript">
    function openPublication(pubId, instanceId) {
      let url = "OpenPublication?PubId="+pubId+"&InstanceId="+instanceId;
      SP_openWindow(url,'publication','850','600','scrollbars=yes, noresize, alwaysRaised');
    }
    
    function validateDelegatedNews(pubId) {
      document.listDelegatedNews.action = "ValidateDelegatedNews";
      document.listDelegatedNews.PubId.value = pubId;
      document.listDelegatedNews.submit();
    }

    function refuseDelegatedNews(pubId) {
      document.listDelegatedNews.PubId.value = pubId;
      $("#refuseDialog").dialog("open");
    }
    
    function updateDateDelegatedNews(pubId, BeginDate, BeginHour, EndDate, EndHour) {
      document.listDelegatedNews.PubId.value = pubId;
      $("#datesDialog #BeginDate").val(BeginDate);
      $("#datesDialog #BeginHour").val(BeginHour);
      $("#datesDialog #EndDate").val(EndDate);
      $("#datesDialog #EndHour").val(EndHour);
      $("#datesDialog").dialog("open");
    }
    
    function ifDatesCorrectExecute(callback) {
      let errorMsg = "";
      let errorNb = 0;

      let beginDate = {dateId : 'BeginDate', hourId : 'BeginHour'};
      let endDate = {dateId : 'EndDate', hourId : 'EndHour', defaultDateHour : '23:59'};
      let dateErrors = isPeriodEndingInFuture(beginDate, endDate);
      $(dateErrors).each(function(index, error) {
        errorMsg += " - " + error.message + "\n";
        errorNb++;
      });
         
      switch(errorNb) {
      	case 0 :
          callback.call(this);
          break;
		case 1 :
          errorMsg = '<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n' + errorMsg;
          jQuery.popup.error(errorMsg);
          break;
		default :
          errorMsg = '<fmt:message key="GML.ThisFormContains" /> ' + errorNb + ' <fmt:message key="GML.errors"/> :\n' + errorMsg;
          jQuery.popup.error(errorMsg);
	    }
    }
    
    $(function() {
        $("#refuseDialog").dialog({
        autoOpen: false,
        resizable: false,
        modal: true,
        height: "auto",
        width: 600,
        buttons: {
          "<fmt:message key="GML.ok"/>": function() {
            var message = $("#txtMessage").val();
            document.listDelegatedNews.action = "RefuseDelegatedNews";
              document.listDelegatedNews.RefuseReasonText.value = message;
              document.listDelegatedNews.submit();
          },
          "<fmt:message key="GML.cancel" />": function() {
            $(this).dialog("close");
          }
        }
      });
        
        $("#datesDialog").dialog({
        autoOpen: false,
        resizable: false,
        modal: true,
        height: "auto",
        width: 500,
        buttons: {
          "<fmt:message key="GML.ok"/>": function() {
            ifDatesCorrectExecute(function() {
              document.listDelegatedNews.action = "UpdateDateDelegatedNews";
              document.listDelegatedNews.BeginDate.value = $("#datesDialog #BeginDate").val();
              document.listDelegatedNews.BeginHour.value = $("#datesDialog #BeginHour").val();
              document.listDelegatedNews.EndDate.value = $("#datesDialog #EndDate").val();
              document.listDelegatedNews.EndHour.value = $("#datesDialog #EndHour").val();
              document.listDelegatedNews.submit();
            });
          },
          "<fmt:message key="GML.cancel" />": function() {
            $(this).dialog("close");
          }
        }
      });
    });

    let listDelegatedNewsJSON = ${listNewsJSON};
    
    $(document).ready(function() {
        $('#newsList tbody').bind('sortupdate', function(event, ui) {
            var updatedDelegatedNews = new Array(); //tableau de DelegatedNewsEntity réordonnés sérialisés en JSON
            var data = $('#newsList tbody').sortable('toArray'); //tableau de valeurs delegatedNews_{pubId} réordonnés
            for (let i=0; i<data.length; i++)
            {
              let pubId = data[i]; //delegatedNews_{pubId}
              pubId = pubId.substring(14); //{pubId}
              
              for (let j=0; j<listDelegatedNewsJSON.length; j++)
              {
                let delegatedNewsJSON = listDelegatedNewsJSON[j];
                if(pubId === delegatedNewsJSON.pubId) {
                  updatedDelegatedNews[i] = delegatedNewsJSON;
                }
              }
            }
            sortDelegatedNews(updatedDelegatedNews);
          });
      });
      
      function sortDelegatedNews(updatedDelegatedNewsJSON){
        let url = "${context}/services/delegatednews/${componentId}";
        let ajaxRequest = window.sp.ajaxRequest(url).byPutMethod();
        ajaxRequest.sendAndPromiseJsonResponse(updatedDelegatedNewsJSON).then(function(data) {
          listDelegatedNewsJSON = data;
        });
      }
      
      function deleteDelegatedNews(pubId) {
        let listDNToDelete = new Array(pubId);
        let updatedDelegatedNews = new Array(); //tableau de DelegatedNewsEntity sans l'élément supprimé sérialisés en JSON
        let k = 0;
        for (let i=0; i<listDelegatedNewsJSON.length; i++) {
          let delegatedNewsJSON = listDelegatedNewsJSON[i];
          let pubIdJSON = delegatedNewsJSON.pubId;
          if(! isAppartient(pubIdJSON, listDNToDelete)) {
            updatedDelegatedNews[k] = delegatedNewsJSON;
            k++;  
          }
        }
        jQuery.popup.confirm('<fmt:message key="delegatednews.deleteOne.confirm"/>', function() {
          deleteDelegagedNews(updatedDelegatedNews);
        });
      }
      
      function deleteSelectedDelegatedNews() {

        let nbNews = listDelegatedNewsJSON.length;
        if (nbNews > 0) {
          // array of JSON-serialized DelegatedNewsEntity objects without the removed items
          let updatedDelegatedNews = [];
          let listDNToDelete = $("input:checked");
          let k = 0;
          for (let i = 0; i < nbNews; i++) {
            let delegatedNewsJSON = listDelegatedNewsJSON[i];
            let pubIdJSON = delegatedNewsJSON.pubId;
            if (!isAppartient(pubIdJSON, listDNToDelete)) {
              updatedDelegatedNews[k] = delegatedNewsJSON;
              k++;
            }
          }
          if (nbNews > updatedDelegatedNews.length) { //on a coché au - une news à supprimer
            jQuery.popup.confirm('<fmt:message key="delegatednews.delete.confirm"/>', function() {
              deleteDelegagedNews(updatedDelegatedNews);
            });
          }
        }
      }
      
      function deleteDelegagedNews(updatedDelegatedNews) {
        let url = "${context}/services/delegatednews/${componentId}";
        let ajaxRequest = window.sp.ajaxRequest(url).byPutMethod();
        ajaxRequest.sendAndPromiseJsonResponse(updatedDelegatedNews).then(function(data) {
          let listPubIdToDelete = getAllPubIdToDelete(data);
          for (let i=0; i<listPubIdToDelete.length; i++) {
            let trToDelete = "#delegatedNews_" + listPubIdToDelete[i];
            $(trToDelete).remove();
          }
          listDelegatedNewsJSON = data;
        });
      }
      
      function isAppartient(id, list) {
        for (let i = 0; i < list.length; i++) {
          let value = list[i].value;
          if (value == null) {
            value = list[i];
          }
          if (id === value) {
            return true;
          }
        }
        return false;
      }
      
      function getAllPubIdToDelete(listDelegatedNewsAfterDelete) {
        let result = [];
        let k = 0;
        for (let i=0; i<listDelegatedNewsJSON.length; i++) {
          let delegatedNewsJSON = listDelegatedNewsJSON[i];
          let pubIdJSON = delegatedNewsJSON.pubId;
          let trouve = false;
          for (let j=0; j<listDelegatedNewsAfterDelete.length; j++) {
            let newDelegatedNewsJSON = listDelegatedNewsAfterDelete[j];
            if (pubIdJSON === newDelegatedNewsJSON.pubId) {
              trouve = true;
              break;
            }
          }
          if (!trouve) {
            result[k] = pubIdJSON;
            k++;
          }
        }
        return result;
      }
      
    </script>
  </view:sp-head-part>

  <fmt:message var="listNewsTitle" key="delegatednews.listNews"/>
  <fmt:message var="columnTitle" key="delegatednews.news.title"/>
  <fmt:message var="columnUpdateDate" key="delegatednews.updateDate"/>
  <fmt:message var="columnContributor" key="delegatednews.contributor"/>
  <fmt:message var="columnState" key="delegatednews.news.state"/>
  <fmt:message var="columnVisibilityBeginDate" key="delegatednews.visibilityBeginDate"/>
  <fmt:message var="columnVisibilityEndDate" key="delegatednews.visibilityEndDate"/>

  <fmt:message var="labelModify" key="GML.modify"/>
  <fmt:message var="labelValidate" key="delegatednews.action.validate"/>
  <fmt:message var="labelRefuse" key="delegatednews.action.refuse"/>
  <fmt:message var="labelDelete" key="GML.delete"/>
  <fmt:message var="labelOperations"  key="GML.operations"/>

  <c:url var="updateIcon" value="/util/icons/update.gif"/>
  <c:url var="validateIcon" value="/util/icons/ok.gif"/>
  <c:url var="refuseIcon" value="/util/icons/wrong.gif"/>
  <c:url var="deleteIcon" value="/util/icons/delete.gif"/>

  <view:sp-body-part>
    <c:if test="${isAdmin}">
      <fmt:message key="delegatednews.action.delete" var="deleteAllAction" />
      <view:operationPane>
        <view:operation altText="${deleteAllAction}" icon="${deleteIcon}" action="javascript:onClick=deleteSelectedDelegatedNews();" />
      </view:operationPane>
    </c:if>
    <view:window>
      <view:frame>
        <view:componentInstanceIntro componentId="${componentId}" language="${currentUserLanguage}"/>
        <c:if test="${isAdmin}">
          <div class="inlineMessage"><fmt:message key="delegatednews.homePageMessage"/></div>
          <br/>
        </c:if>
      <form name="tabForm" method="post">
        <view:arrayPane title="${listNewsTitle}" var="newsList" routingAddress="Main" movableLines="true" summary="true">
          <view:arrayColumn title="${columnTitle}"
                            compareOn="${r -> r.news.publicationDetail.name}"/>
          <view:arrayColumn title="${columnUpdateDate}" width="70px" compareOn="${r -> r.news.publicationDetail.lastUpdateDate}"/>
          <view:arrayColumn title="${columnContributor}"/>
          <view:arrayColumn title="${columnState}"/>
          <view:arrayColumn title="${columnVisibilityBeginDate}"  compareOn="${r -> r.news.beginDate}"/>
          <view:arrayColumn title="${columnVisibilityEndDate}" compareOn="${r -> r.news.endDate}"/>
          <c:if test="${isAdmin}">
            <view:arrayColumn title="${labelOperations}" sortable="true"/>
            <view:arrayColumn title=""/>
          </c:if>
          <c:forEach items="${listNews}" var="news" varStatus="status">
            <c:set var="pubId" value="${news.pubId}"/>
            <c:set var="instanceId" value="${news.instanceId}"/>
            <view:arrayLine id="delegatedNews_${pubId}">
              <view:arrayCellText>
                <a href="javascript:onClick=openPublication('${pubId}','${instanceId}');">${news.publicationDetail.name}</a>
              </view:arrayCellText>
              <view:arrayCellText>
                ${silfn:formatDateAndHour(news.publicationDetail.lastUpdateDate, currentUserLanguage)}
              </view:arrayCellText>
              <view:arrayCellText>
                <view:username userId="${news.contributorId}"/>
              </view:arrayCellText>

              <fmt:message var="status" key="delegatednews.status.${news.status}"/>
              <view:arrayCellText text="${status}"/>

              <c:set var="beginDate" value="${silfn:formatDate(news.beginDate, currentUserLanguage)}"/>
              <c:set var="endDate" value="${silfn:formatDate(news.endDate, currentUserLanguage)}"/>
              <c:set var="beginHour" value="${silfn:formatDateHour(news.beginDate, currentUserLanguage)}"/>
              <c:set var="endHour" value="${silfn:formatDateHour(news.endDate, currentUserLanguage)}"/>
              <view:arrayCellText>
                <c:if test="${not empty news.beginDate}">
                  ${silfn:formatDateAndHour(news.beginDate, currentUserLanguage)}
                </c:if>
              </view:arrayCellText>
              <view:arrayCellText>
                <c:if test="${not empty news.endDate}">
                  ${silfn:formatDateAndHour(news.endDate, currentUserLanguage)}
                </c:if>
              </view:arrayCellText>
              <view:arrayCellText>
                <c:if test="${isAdmin}">
                  <view:icons>
                    <c:set var="updateAction" value="javascript:onClick=updateDateDelegatedNews('${pubId}','${beginDate}','${beginHour}','${endDate}','${endHour}');"/>
                    <view:icon iconName="${updateIcon}" altText="${labelModify}" action="${updateAction}"/>

                    <c:set var="validateAction" value="javascript:onClick=validateDelegatedNews('${pubId}');"/>
                    <view:icon iconName="${validateIcon}" altText="${labelValidate}" action="${validateAction}"/>

                    <c:set var="refuseAction" value="javascript:onClick=refuseDelegatedNews('${pubId}');"/>
                    <view:icon iconName="${refuseIcon}" altText="${labelRefuse}" action="${refuseAction}"/>

                    <c:set var="deleteAction" value="javascript:onClick=deleteDelegatedNews('${pubId}');"/>
                    <view:icon iconName="${deleteIcon}" altText="${labelDelete}" action="${deleteAction}"/>
                  </view:icons>
                </c:if>
              </view:arrayCellText>
              <view:arrayCellCheckbox checked="false" name="checkedDelegatedNews" value="${pubId}"/>
            </view:arrayLine>
          </c:forEach>
      </view:arrayPane>
      </form>
      </view:frame>
    </view:window>

<!-- Dialog to refuse --> 
<div id="refuseDialog" title="<fmt:message key="delegatednews.action.refuse"/>">
  <form>
    <div class="skinFieldset">
      <div class="fields">
        <div  class="field entireWidth">
          <label class="txtlibform" for="txtMessage">
            <fmt:message key="GML.notification.message"/>
          </label>
          <div class="champs">
            <textarea name="txtMessage" id="txtMessage" cols="60" rows="8"></textarea>
          </div>
        </div>
      </div>
    </div>
  </form>
</div>

<!-- Dialog to edit dates -->
<div id="datesDialog" title="<fmt:message key="GML.modify"/>">
  <form>
    <div class="skinFieldset">
      <div id="beginArea" class="fields">
        <div class="field entireWidth">
          <label class="txtlibform" for="BeginDate">
            <fmt:message key="delegatednews.visibilityBeginDate"/>
          </label>
          <div class="champs">
            <input type="text" class="dateToPick" id="BeginDate" value="" size="12" maxlength="10"/>
            <span class="txtsublibform" for="BeginHour">&nbsp;
              <fmt:message key="delegatednews.hour"/>&nbsp;
            </span>
            <input type="text" name="BeginHour" id="BeginHour" value="" size="5" maxlength="5"/>
            &nbsp;<i>(hh:mm)</i>
          </div>
        </div>
      </div>
      <div id="endArea" class="fields">
        <div class="field entireWidth">
          <label class="txtlibform" for="EndDate">
            <fmt:message key="delegatednews.visibilityEndDate"/>
          </label>
          <div class="champs">
            <input type="text" class="dateToPick" id="EndDate" value="" size="12" maxlength="10"/>
            <span class="txtsublibform" for="EndHour">&nbsp;
              <fmt:message key="delegatednews.hour"/>&nbsp;
            </span>
            <input type="text" name="EndHour" id="EndHour" value="" size="5" maxlength="5"/>
            &nbsp;<i>(hh:mm)</i>
          </div>
        </div>
      </div>
    </div>
  </form>
</div>

<form name="listDelegatedNews" action="" method="post">
  <input type="hidden" name="PubId"/>
    <input type="hidden" name="RefuseReasonText"/>
    <input type="hidden" name="BeginDate"/>
    <input type="hidden" name="BeginHour"/>
    <input type="hidden" name="EndDate"/>
    <input type="hidden" name="EndHour"/>
</form>

</view:sp-body-part>
</view:sp-page>