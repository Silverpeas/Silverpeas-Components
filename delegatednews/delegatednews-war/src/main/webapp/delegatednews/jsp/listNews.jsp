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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.silverpeas.delegatednews.model.DelegatedNews"%>


<%@ include file="check.jsp"%>
  <fmt:setLocale value="${sessionScope[sessionController].language}" />
  
<%
	List listNews = (List) request.getAttribute("ListNews"); //List<DelegatedNews> 
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <view:looknfeel />
    <script type="text/javascript" src="<c:url value='/util/javaScript/animation.js'/>"></script>
    <script type="text/javascript">
    function openPublication(pubId) {
      url = "OpenPublication?PubId="+pubId;
      SP_openWindow(url,'publication','800','600','scrollbars=yes, noresize, alwaysRaised');
    }
    
    function refuseDelegatedNews(pubId) {
      url = "EditRefuseReason?PubId="+pubId;
      SP_openWindow(url,'refuseReason','500','150','scrollbars=no, noresize, alwaysRaised');
    }
    
    function updateDateDelegatedNews(pubId, BeginDate, BeginHour, EndDate, EndHour) {
      url = "EditUpdateDate?PubId="+pubId+"&BeginDate="+BeginDate+"&BeginHour="+BeginHour+"&EndDate="+EndDate+"&EndHour="+EndHour;
      SP_openWindow(url,'updateDate','500','240','scrollbars=no, noresize, alwaysRaised');
    }
    </script>
  </head>  
  <body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
    <view:window>
      <view:frame>
        <view:board>
      <view:tabs>
        
      <FORM NAME="listDelegatedNews" ACTION="" METHOD="POST">
      <input type="hidden" name="PubId">
      <input type="hidden" name="RefuseReasonText">
      <input type="hidden" name="BeginDate">
      <input type="hidden" name="BeginHour">
      <input type="hidden" name="EndDate">
      <input type="hidden" name="EndHour">
          
  <%
	ResourceLocator kmeliaResourceLocator = new ResourceLocator("com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", newsScc.getLanguage());
    ArrayPane arrayPane = gef.getArrayPane("newsList", "Main", request, session);
    arrayPane.setVisibleLineNumber(10);
    arrayPane.setTitle(resources.getString("delegatednews.listNews"));
    arrayPane.addArrayColumn(kmeliaResourceLocator.getString("PubTitre"));
    arrayPane.addArrayColumn(resources.getString("delegatednews.updateDate"));
    arrayPane.addArrayColumn(resources.getString("delegatednews.contributor"));
    arrayPane.addArrayColumn(kmeliaResourceLocator.getString("PubState"));
    arrayPane.addArrayColumn(resources.getString("delegatednews.visibilityBeginDate"));
    arrayPane.addArrayColumn(resources.getString("delegatednews.visibilityEndDate"));
    
	boolean isAdmin = newsScc.isAdmin();
    if(isAdmin) {
		ArrayColumn arrayColumnOp = arrayPane.addArrayColumn(kmeliaResourceLocator.getString("Operations"));
		arrayColumnOp.setSortable(false);
	}
    
    SimpleDateFormat dateFormat = new SimpleDateFormat(resources.getString("GML.dateFormat"));
    SimpleDateFormat hourFormat = new SimpleDateFormat(resources.getString("GML.hourFormat"));
    SimpleDateFormat dateHourFormat = new SimpleDateFormat(resources.getString("GML.dateFormat") + " " + resources.getString("GML.hourFormat"));
    for (int i=0; i<listNews.size(); i++) {
		DelegatedNews delegatedNews = (DelegatedNews) listNews.get(i);
		
		int pubId = delegatedNews.getPubId();
		ArrayLine arrayLine = arrayPane.addArrayLine();
		arrayLine.addArrayCellLink(delegatedNews.getPublicationDetail().getName(newsScc.getLanguage()), "javascript:onClick=openPublication('"+pubId+"')");
		
		String updateDate = dateFormat.format(delegatedNews.getPublicationDetail().getUpdateDate());
		ArrayCellText cellUpdateDate = arrayLine.addArrayCellText(updateDate);
		cellUpdateDate.setCompareOn(updateDate);
		
		String contributorId = delegatedNews.getContributorId();
        String contributorName = newsScc.getUserDetail(contributorId).getDisplayedName();
		arrayLine.addArrayCellText(contributorName);
		
		String status = delegatedNews.getStatus();
		arrayLine.addArrayCellText(resources.getString("delegatednews.status."+status));
		
		String beginDate = "";
		String beginHour = "";
		String beginDateHour = "";
		if(delegatedNews.getBeginDate() != null) {
			beginDate = dateFormat.format(delegatedNews.getBeginDate());
			beginHour = hourFormat.format(delegatedNews.getBeginDate());
			beginDateHour = dateHourFormat.format(delegatedNews.getBeginDate());
			ArrayCellText cellBeginDate = arrayLine.addArrayCellText(beginDateHour);
			cellBeginDate.setCompareOn(beginDateHour);
		} else {
			arrayLine.addArrayCellText("");
		}
		
		String endDate = "";
		String endHour = "";
		String endDateHour = "";
		if(delegatedNews.getEndDate() != null) {
			endDate = dateFormat.format(delegatedNews.getEndDate());
			endHour = hourFormat.format(delegatedNews.getEndDate());
			endDateHour = dateHourFormat.format(delegatedNews.getEndDate());
			ArrayCellText cellEndDate = arrayLine.addArrayCellText(endDateHour);
			cellEndDate.setCompareOn(endDateHour);
		} else {
			arrayLine.addArrayCellText("");
		}

        if(isAdmin) {
			IconPane iconPane = gef.getIconPane();
			Icon iconUpdate = iconPane.addIcon();
			iconUpdate.setProperties(m_context+"/util/icons/update.gif", resources.getString("GML.modify"), "javascript:onClick=updateDateDelegatedNews('"+pubId+"', '"+beginDate+"', '"+beginHour+"', '"+endDate+"', '"+endHour+"')");
			
			Icon iconValidate = iconPane.addIcon();
			iconValidate.setProperties(m_context+"/util/icons/ok.gif", kmeliaResourceLocator.getString("Validate"), "ValidateDelegatedNews?PubId="+pubId);
			
			Icon iconRefused = iconPane.addIcon();
			iconRefused.setProperties(m_context+"/util/icons/delete.gif", kmeliaResourceLocator.getString("PubUnvalidate?"), "javascript:onClick=refuseDelegatedNews('"+pubId+"')");
			
			arrayLine.addArrayCellIconPane(iconPane);	
		}
	}
   
       
  out.print(arrayPane.print());
  %>
     
      </FORM>  
       
      </view:tabs>
  
        </view:board>
      </view:frame>
    </view:window>
  </body>
</html>
