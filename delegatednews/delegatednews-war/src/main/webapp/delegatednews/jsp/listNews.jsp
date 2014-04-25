<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.UserNameGenerator"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCell"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellLink"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="com.silverpeas.delegatednews.model.DelegatedNews"%>

<%@ include file="check.jsp"%>
<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="listNewsJSON" value="${requestScope.ListNewsJSON}"/>
  
<%
  List<DelegatedNews> listNews = (List<DelegatedNews>) request.getAttribute("ListNews"); //List<DelegatedNews>
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <view:looknfeel />
    <view:includePlugin name="datepicker"/>
    <view:includePlugin name="userZoom"/>
    <script type="text/javascript" src="<c:url value='/util/javaScript/animation.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/util/javaScript/checkForm.js'/>"></script>
    <script type="text/javascript">
    <!--
    function openPublication(pubId, instanceId) {
      url = "OpenPublication?PubId="+pubId+"&InstanceId="+instanceId;
      SP_openWindow(url,'publication','800','600','scrollbars=yes, noresize, alwaysRaised');
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
    
    function areDatesCorrect(beginDate, beginHour, endDate, endHour, language) {
      var errorMsg = "";
      var errorNb = 0;
      var beginDateOK = true;
      if (!isWhitespace(beginDate)) {
      	if (!isDateOK(beginDate, language)) {
        	errorMsg+="  - '<fmt:message key="delegatednews.visibilityBeginDate"/>' <fmt:message key="GML.MustContainsCorrectDate"/>\n";
            errorNb++;
            beginDateOK = false;
		} 
      }
      if (!checkHour(beginHour)) {
      	errorMsg+="  - '<fmt:message key="delegatednews.hour"/>' <fmt:message key="GML.MustContainsCorrectHour"/>\n";
        errorNb++;
	  }
      if (!isWhitespace(endDate)) {
      	if (!isDateOK(endDate, language)) {
        	errorMsg+="  - '<fmt:message key="delegatednews.visibilityEndDate"/>' <fmt:message key="GML.MustContainsCorrectDate"/>\n";
            errorNb++;
		} else {
        	if (!isWhitespace(beginDate) && !isWhitespace(endDate)) {
            	if (beginDateOK && !isDate1AfterDate2(endDate, beginDate, language)) {
            		errorMsg+="  - '<fmt:message key="delegatednews.visibilityEndDate"/>' <fmt:message key="GML.MustContainsPostOrEqualDateTo"/> "+beginDate+"\n";
                	errorNb++;
				}
			} else {
            	if (isWhitespace(beginDate) && !isWhitespace(endDate)) {
                	if (!isFuture(endDate, language)) {
                    	errorMsg+="  - '<fmt:message key="delegatednews.visibilityEndDate"/>' <fmt:message key="GML.MustContainsPostDate"/>\n";
                        errorNb++;
                    }
				}
			}
		}
	  }
      if (!checkHour(endHour))
      {
      	errorMsg+="  - '<fmt:message key="delegatednews.hour"/>' <fmt:message key="GML.MustContainsCorrectHour"/>\n";
        errorNb++;
	  }
         
      switch(errorNb) {
      	case 0 :
			result = true;
            break;
		case 1 :
        	errorMsg = '<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n' + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
		default :
        	errorMsg = '<fmt:message key="GML.ThisFormContains" /> " + errorNb + " <fmt:message key="GML.errors"/> :\n' + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
	  }
      return result;
    }
    
    $(function() {
        $("#refuseDialog").dialog({
        autoOpen: false,
        resizable: false,
        modal: true,
        height: "auto",
        width: 500,
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
            var beginDate = $("#datesDialog #BeginDate").val();
            var beginHour = $("#datesDialog #BeginHour").val();
            var endDate = $("#datesDialog #EndDate").val();
            var endHour = $("#datesDialog #EndHour").val();
            if (areDatesCorrect(beginDate, beginHour, endDate, endHour, '<%=resources.getLanguage()%>')) {
              document.listDelegatedNews.action = "UpdateDateDelegatedNews";
              document.listDelegatedNews.BeginDate.value = beginDate;
              document.listDelegatedNews.BeginHour.value = beginHour;
              document.listDelegatedNews.EndDate.value = endDate;
              document.listDelegatedNews.EndHour.value = endHour;
              document.listDelegatedNews.submit();
            }
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
      
      function sortDelegatedNews(updatedDelegatedNewsJSON)
      {
          $.ajax({
              url:"<%=m_context%>/services/delegatednews/<%=newsScc.getComponentId()%>",
              type: "PUT",
              contentType: "application/json",
              dataType: "json",
              cache: false,
              data: $.toJSON(updatedDelegatedNewsJSON),
              success: function (data) {
                listDelegatedNewsJSON = data;
              }
              ,
              error: function(jqXHR, textStatus, errorThrown) {
                if (onError == null)
                 alert(errorThrown);
                else
                 onError({
                   status: jqXHR.status, 
                   message: errorThrown
                 });
              }
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
        if (confirm('<fmt:message key="delegatednews.deleteOne.confirm"/>')) {
        	  deleteDelegagedNews(updatedDelegatedNews);
        }
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
            if (confirm('<fmt:message key="delegatednews.delete.confirm"/>')) {
            	deleteDelegagedNews(updatedDelegatedNews);
            }
          }
        }
      }
      
      function deleteDelegagedNews(updatedDelegatedNews) {
    	  $.ajax({
              url:"<%=m_context%>/services/delegatednews/<%=newsScc.getComponentId()%>",
              type: "PUT",
              contentType: "application/json",
              dataType: "json",
              cache: false,
              data: $.toJSON(updatedDelegatedNews),
              success: function (data) {
                var listPubIdToDelete = getAllPubIdToDelete(data);
                for (var i=0; i<listPubIdToDelete.length; i++)
                {
                  var trToDelete = "#delegatedNews_" + listPubIdToDelete[i];
                  $(trToDelete).remove();
                }
                listDelegatedNewsJSON = data;
              }
             ,
             error: function(jqXHR, textStatus, errorThrown) {
             if (onError == null)
              alert(errorThrown);
             else
              onError({
              status: jqXHR.status, 
              message: errorThrown
              });
             }
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
  </head>  
  <body>
    <fmt:message key="delegatednews.icons.delete" var="deleteIcon" bundle="${icons}" />
    <fmt:message key="delegatednews.action.delete" var="deleteAction" />
    <view:operationPane>
      <view:operation altText="${deleteAction}" icon="${deleteIcon}" action="javascript:onClick=deleteSelectedDelegatedNews();" />
    </view:operationPane>
    <view:window>
      <view:frame>
      <div class="inlineMessage"><fmt:message key="delegatednews.homePageMessage"/></div>
      <br clear="all"/>
      <form name="tabForm" method="post">
  <%
    ArrayPane arrayPane = gef.getArrayPane("newsList", "Main", request, session);
    arrayPane.setVisibleLineNumber(20);
    arrayPane.setSortableLines(true);
    arrayPane.setTitle(resources.getString("delegatednews.listNews"));
    arrayPane.addArrayColumn(resources.getString("delegatednews.news.title"));
    arrayPane.addArrayColumn(resources.getString("delegatednews.updateDate"));
    arrayPane.addArrayColumn(resources.getString("delegatednews.contributor"));
    arrayPane.addArrayColumn(resources.getString("delegatednews.news.state"));
    arrayPane.addArrayColumn(resources.getString("delegatednews.visibilityBeginDate"));
    arrayPane.addArrayColumn(resources.getString("delegatednews.visibilityEndDate"));
    
    boolean isAdmin = newsScc.isAdmin();
      if(isAdmin) {
      ArrayColumn arrayColumnOp = arrayPane.addArrayColumn(resources.getString("GML.operations"));
      arrayColumnOp.setSortable(false);
      arrayPane.addArrayColumn("");
    }
    
    SimpleDateFormat hourFormat = new SimpleDateFormat(resources.getString("GML.hourFormat"));
    for (int i=0; i<listNews.size(); i++) {
      DelegatedNews delegatedNews = (DelegatedNews) listNews.get(i);
      
      int pubId = delegatedNews.getPubId();
      String instanceId = delegatedNews.getInstanceId();
      ArrayLine arrayLine = arrayPane.addArrayLine();
      arrayLine.setId("delegatedNews_"+pubId);
      arrayLine.addArrayCellLink(delegatedNews.getPublicationDetail().getName(resources.getLanguage()), "javascript:onClick=openPublication('"+pubId+"', '"+instanceId+"');");
      
      String updateDate = resources.getOutputDate(delegatedNews.getPublicationDetail().getUpdateDate());
      ArrayCellText cellUpdateDate = arrayLine.addArrayCellText(updateDate);
      cellUpdateDate.setCompareOn(delegatedNews.getPublicationDetail().getUpdateDate());
      
      arrayLine.addArrayCellText(UserNameGenerator.toString(delegatedNews.getContributorId(), "unknown"));
      
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
        <td class="txtlibform"><fmt:message key="delegatednews.visibilityBeginDate"/></td>
        <td><input type="text" class="dateToPick" id="BeginDate" value="" size="12" maxlength="10"/>
          <span class="txtsublibform">&nbsp;<fmt:message key="delegatednews.hour"/>&nbsp;</span><input type="text" id="BeginHour" value="" size="5" maxlength="5"/> <i>(hh:mm)</i></td>
      </tr>
      <tr id="endArea">
        <td class="txtlibform"><fmt:message key="delegatednews.visibilityEndDate"/></td>
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

</body>
</html>