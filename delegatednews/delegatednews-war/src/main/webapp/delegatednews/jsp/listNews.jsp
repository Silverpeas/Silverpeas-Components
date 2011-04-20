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
  <c:set var="componentId" value="${requestScope.componentId}" />
  <c:set var="browseContext" value="${requestScope.browseContext}" />
  <fmt:setLocale value="${sessionScope[sessionController].language}" />
  <view:setBundle bundle="${requestScope.resources.multilangBundle}" var="DML"/>
  <view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
  <view:setBundle basename="com.stratelia.webactiv.kmelia.multilang.kmeliaBundle" var="KML"/>
  <view:setBundle basename="com.stratelia.webactiv.multilang.generalMultilang" var="GML"/>
  
  <fmt:message key="GML.hourFormat" bundle="${GML}" var="hourFormat"/>
  <c:set var="dateHourFormat"><c:out value="${dateFormat}" /> <c:out value="${hourFormat}" /></c:set>
  
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
    for (int i=0; i<listNews.size(); i++) {
		DelegatedNews delegatedNews = (DelegatedNews) listNews.get(i);
		int pubId = delegatedNews.getPubId();
		
		ArrayLine arrayLine = arrayPane.addArrayLine();
		arrayLine.addArrayCellLink(delegatedNews.getPublicationDetail().getName(), "javascript:onClick=openPublication('"+pubId+"')");
		
		/*String updateDate = dateFormat.format(delegatedNews.getPublicationDetail().getUpdateDate());
		System.out.println("updateDate"=+updateDate);*/
		/*ArrayCellText cellUpdateDate = arrayLine.addArrayCellText(resources.getOutputDate(updateDate));
		cellUpdateDate.setCompareOn(updateDate);*/
		//<fmt:formatDate var="updateDate" pattern="${dateFormat}" value="${delegatedNews.publicationDetail.updateDate}" />
        
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
