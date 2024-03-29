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
<%@page import="org.silverpeas.components.delegatednews.model.DelegatedNews"%>
<%@page import="org.silverpeas.core.web.util.viewgenerator.html.UserNameGenerator"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.List"%>

<%@ include file="check.jsp"%>
<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="listNewsJSON" value="${requestScope.ListNewsJSON}"/>

<%
  List<DelegatedNews> listNews = (List<DelegatedNews>) request.getAttribute("ListNews");
  boolean isAdmin = newsScc.isAdmin();

%>

<c:set var="isAdmin" value="<%=isAdmin%>"/>

<view:sp-page angularJsAppName="silverpeas.delegatedNews">
  <view:sp-head-part withCheckFormScript="true">
    <view:includePlugin name="datepicker"/>
    <view:includePlugin name="toggle"/>
    <script type="text/javascript">
    <!--
    function openPublication(pubId, instanceId) {
      url = "OpenPublication?PubId="+pubId+"&InstanceId="+instanceId;
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
      var errorMsg = "";
      var errorNb = 0;
      
      var beginDate = {dateId : 'BeginDate', hourId : 'BeginHour'};
      var endDate = {dateId : 'EndDate', hourId : 'EndHour', defaultDateHour : '23:59'};
      var dateErrors = isPeriodEndingInFuture(beginDate, endDate);
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
    
    var listDelegatedNewsJSON = ${listNewsJSON};
    
    $(document).ready(function() {
        $('#newsList tbody').bind('sortupdate', function(event, ui) {
            var updatedDelegatedNews = new Array(); //tableau de DelegatedNewsEntity réordonnés sérialisés en JSON
            var data = $('#newsList tbody').sortable('toArray'); //tableau de valeurs delegatedNews_{pubId} réordonnés
            for (var i=0; i<data.length; i++)
            {
              var pubId = data[i]; //delegatedNews_{pubId}
              pubId = pubId.substring(14); //{pubId}
              
              for (var j=0; j<listDelegatedNewsJSON.length; j++)
              {
                var delegatedNewsJSON = listDelegatedNewsJSON[j];
                if(pubId == delegatedNewsJSON.pubId) {
                  updatedDelegatedNews[i] = delegatedNewsJSON;
                }
              }
            }
            sortDelegatedNews(updatedDelegatedNews);
          });
      });
      
      function sortDelegatedNews(updatedDelegatedNewsJSON){
        var url = "<%=m_context%>/services/delegatednews/<%=newsScc.getComponentId()%>";
        var ajaxRequest = window.sp.ajaxRequest(url).byPutMethod();
        ajaxRequest.sendAndPromiseJsonResponse(updatedDelegatedNewsJSON).then(function(data) {
          listDelegatedNewsJSON = data;
        });
      }
      
      function deleteDelegatedNews(pubId) {
    	  var listDNToDelete = new Array(pubId);
        var updatedDelegatedNews = new Array(); //tableau de DelegatedNewsEntity sans l'élément supprimé sérialisés en JSON
        var k = 0;
        for (var i=0; i<listDelegatedNewsJSON.length; i++) {
          var delegatedNewsJSON = listDelegatedNewsJSON[i];
          var pubIdJSON = delegatedNewsJSON.pubId;
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
    	  var nbNews = listDelegatedNewsJSON.length;
    	  if(nbNews > 0) {
    		  var updatedDelegatedNews = new Array(); //tableau de DelegatedNewsEntity sans les éléments supprimés sérialisés en JSON
    		  var listDNToDelete = $("input:checked");
    		  var k = 0;
    		  for (var i=0; i<nbNews; i++) {
    			  var delegatedNewsJSON = listDelegatedNewsJSON[i];
            var pubIdJSON = delegatedNewsJSON.pubId;
            if(! isAppartient(pubIdJSON, listDNToDelete)) {
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
        var url = "<%=m_context%>/services/delegatednews/<%=newsScc.getComponentId()%>";
        var ajaxRequest = window.sp.ajaxRequest(url).byPutMethod();
        ajaxRequest.sendAndPromiseJsonResponse(updatedDelegatedNews).then(function(data) {
          var listPubIdToDelete = getAllPubIdToDelete(data);
          for (var i=0; i<listPubIdToDelete.length; i++) {
            var trToDelete = "#delegatedNews_" + listPubIdToDelete[i];
            $(trToDelete).remove();
          }
          listDelegatedNewsJSON = data;
        });
      }
      
      function isAppartient(id, list) {
    	  for (var i=0; i<list.length; i++) {
    		  var value = list[i].value;
    		  if(value == null) {
    			  value = list[i];
    		  }
    		  if(id == value) {
    			  return true;
    		  }
    	  }
    	  return false;
      }
      
      function getAllPubIdToDelete(listDelegatedNewsAfterDelete) {
    	  var result = new Array();
    	  var k=0;
        for (var i=0; i<listDelegatedNewsJSON.length; i++)
        {
        	  var delegatedNewsJSON = listDelegatedNewsJSON[i];
            var pubIdJSON = delegatedNewsJSON.pubId;
            var trouve = false;
            for (var j=0; j<listDelegatedNewsAfterDelete.length; j++) {
              var newDelegatedNewsJSON = listDelegatedNewsAfterDelete[j];
              if (pubIdJSON == newDelegatedNewsJSON.pubId) {
                trouve = true;
                break;
              }
            }
            if(! trouve) {
            	result[k] = pubIdJSON;
              k++;
            }
        }
        return result;
      }
      
    //-->
    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <c:if test="${isAdmin}">
      <fmt:message key="delegatednews.icons.delete" var="deleteIcon" bundle="${icons}" />
      <fmt:message key="delegatednews.action.delete" var="deleteAction" />
      <view:operationPane>
        <view:operation altText="${deleteAction}" icon="${deleteIcon}" action="javascript:onClick=deleteSelectedDelegatedNews();" />
      </view:operationPane>
    </c:if>
    <view:window>
      <view:frame>
        <view:componentInstanceIntro componentId="<%=newsScc.getComponentId()%>" language="<%=resources.getLanguage()%>"/>
        <c:if test="${isAdmin}">
          <div class="inlineMessage"><fmt:message key="delegatednews.homePageMessage"/></div>
          <br clear="all"/>
        </c:if>
      <form name="tabForm" method="post">
  <%
    ArrayPane arrayPane = gef.getArrayPane("newsList", "Main", request, session);
    arrayPane.setVisibleLineNumber(1000);
    arrayPane.setMovableLines(true);
    arrayPane.setTitle(resources.getString("delegatednews.listNews"));
    arrayPane.addArrayColumn(resources.getString("delegatednews.news.title"));
    ArrayColumn column = arrayPane.addArrayColumn(resources.getString("delegatednews.updateDate"));
    column.setWidth("70px");
    arrayPane.addArrayColumn(resources.getString("delegatednews.contributor"));
    arrayPane.addArrayColumn(resources.getString("delegatednews.news.state"));
    arrayPane.addArrayColumn(resources.getString("delegatednews.visibilityBeginDate"));
    arrayPane.addArrayColumn(resources.getString("delegatednews.visibilityEndDate"));

    if(isAdmin) {
      ArrayColumn arrayColumnOp = arrayPane.addArrayColumn(resources.getString("GML.operations"));
      arrayColumnOp.setSortable(false);
      arrayPane.addArrayColumn("");
    }
    
    SimpleDateFormat hourFormat = new SimpleDateFormat(resources.getString("GML.hourFormat"));
    for (int i=0; i<listNews.size(); i++) {
      DelegatedNews delegatedNews = (DelegatedNews) listNews.get(i);
      
      String pubId = delegatedNews.getPubId();
      String instanceId = delegatedNews.getInstanceId();
      ArrayLine arrayLine = arrayPane.addArrayLine();
      arrayLine.setId("delegatedNews_"+pubId);
      arrayLine.addArrayCellLink(delegatedNews.getPublicationDetail().getName(resources.getLanguage()), "javascript:onClick=openPublication('"+pubId+"', '"+instanceId+"');");
      
      String updateDate = resources.getOutputDate(delegatedNews.getPublicationDetail().getLastUpdateDate());
      ArrayCellText cellUpdateDate = arrayLine.addArrayCellText(updateDate);
      cellUpdateDate.setCompareOn(delegatedNews.getPublicationDetail().getLastUpdateDate());
      
      arrayLine.addArrayCellText(UserNameGenerator.toString(delegatedNews.getContributorId(), userId));
      
      String status = delegatedNews.getStatus();
      arrayLine.addArrayCellText(resources.getString("delegatednews.status."+status));
      
      String beginDate = "";
      String beginHour = "";
      if(delegatedNews.getBeginDate() != null) {
        beginDate = resources.getInputDate(delegatedNews.getBeginDate());
        beginHour = hourFormat.format(delegatedNews.getBeginDate());
        ArrayCellText cellBeginDate = arrayLine.addArrayCellText(resources.getOutputDateAndHour(delegatedNews.getBeginDate()));
        cellBeginDate.setCompareOn(delegatedNews.getBeginDate());
      } else {
        arrayLine.addArrayCellText("");
      }
      
      String endDate = "";
      String endHour = "";
      if(delegatedNews.getEndDate() != null) {
        endDate = resources.getInputDate(delegatedNews.getEndDate());
        endHour = hourFormat.format(delegatedNews.getEndDate());
        ArrayCellText cellEndDate = arrayLine.addArrayCellText(resources.getOutputDateAndHour(delegatedNews.getEndDate()));
        cellEndDate.setCompareOn(delegatedNews.getEndDate());
      } else {
        arrayLine.addArrayCellText("");
      }
  
      if(isAdmin) {
        IconPane iconPane = gef.getIconPane();
        Icon iconUpdate = iconPane.addIcon();
        iconUpdate.setProperties(m_context+"/util/icons/update.gif", resources.getString("GML.modify"), "javascript:onClick=updateDateDelegatedNews('"+pubId+"', '"+beginDate+"', '"+beginHour+"', '"+endDate+"', '"+endHour+"');");
        
        Icon iconValidate = iconPane.addIcon();
        iconValidate.setProperties(m_context+"/util/icons/ok.gif", resources.getString("delegatednews.action.validate"), "javascript:onClick=validateDelegatedNews('"+pubId+"');");
        
        Icon iconRefused = iconPane.addIcon();
        iconRefused.setProperties(m_context+"/util/icons/wrong.gif", resources.getString("delegatednews.action.refuse"), "javascript:onClick=refuseDelegatedNews('"+pubId+"');");
        
        Icon iconDelete = iconPane.addIcon();
        iconDelete.setProperties(m_context+"/util/icons/delete.gif", resources.getString("GML.delete"), "javascript:onClick=deleteDelegatedNews('"+pubId+"');");
        
        arrayLine.addArrayCellIconPane(iconPane); 
        
        arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"checkedDelegatedNews\" value=\""+pubId+"\"/>");
      }
  }

  out.print(arrayPane.print());
  %>
      </form>
      </view:frame>
    </view:window>

<!-- Dialog to refuse --> 
<div id="refuseDialog" title="<fmt:message key="delegatednews.action.refuse"/>">
  <form>
    <table>
        <tr>
            <td class="txtlibform"><fmt:message key="GML.notification.message" /> :</td>
          <td><textarea name="txtMessage" id="txtMessage" cols="60" rows="8"></textarea></td>
        </tr>
      </table>
  </form>
</div>

<!-- Dialog to edit dates --> 
<div id="datesDialog" title="<fmt:message key="GML.modify"/>">
  <form>
    <table cellspacing="0" cellpadding="5">
      <tr id="beginArea">
        <td class="txtlibform"><label for="BeginDate"><fmt:message key="delegatednews.visibilityBeginDate"/></label></td>
        <td><input type="text" class="dateToPick" id="BeginDate" value="" size="12" maxlength="10"/>
          <span class="txtsublibform">&nbsp;<fmt:message key="delegatednews.hour"/>&nbsp;</span><input type="text" id="BeginHour" value="" size="5" maxlength="5"/> <i>(hh:mm)</i></td>
      </tr>
      <tr id="endArea">
        <td class="txtlibform"><label for="EndDate"><fmt:message key="delegatednews.visibilityEndDate"/></label></td>
        <td><input type="text" class="dateToPick" id="EndDate" value="" size="12" maxlength="10"/>
          <span class="txtsublibform">&nbsp;<fmt:message key="delegatednews.hour"/>&nbsp;</span><input type="text" id="EndHour" value="" size="5" maxlength="5"/> <i>(hh:mm)</i></td>
      </tr>
    </table>
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