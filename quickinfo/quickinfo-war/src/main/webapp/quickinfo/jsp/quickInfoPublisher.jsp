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
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="org.silverpeas.components.quickinfo.model.News"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkQuickInfo.jsp" %>

<%
  //Collection infos = Tous les quickInfos
  List<News> infos = (List<News>) request.getAttribute("infos");
boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
  String pdcUtilizationSrc  = m_context + "/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Quick Info - Publieur</title>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/formUtil.js"></script>
<script type="text/javascript">
function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

function editQuickInfo(id) {
	document.quickInfoEditForm.Id.value = id;
	document.quickInfoEditForm.action = "Edit";
	document.quickInfoEditForm.submit();
}

function addQuickInfo() {
	document.quickInfoEditForm.action = "Add";
	document.quickInfoEditForm.submit();
}
</script>
</head>
<body id="quickinfo">
<div id="<%=componentId %>">
<form name="quickInfoForm" action="quickInfoPublisher.jsp" method="post">
  <input type="hidden" name="Action"/>
<%
        Window window = gef.getWindow();

        BrowseBar browseBar = window.getBrowseBar();

        OperationPane operationPane = window.getOperationPane();
        if (isAdmin && quickinfo.isPdcUsed()) {
            operationPane.addOperation(pdcUtilizationSrc, resources.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId=" + quickinfo.getComponentId() + "','utilizationPdc1')");
            operationPane.addLine();
        }
        operationPane.addOperationOfCreation(m_context+"/util/icons/create-action/add-news.png", resources.getString("creation"), "javascript:onClick=addQuickInfo()");

        out.println(window.printBefore());
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<%
          ArrayPane arrayPane = gef.getArrayPane("quickinfoList", pageContext);
          arrayPane.setXHTML(true);
          arrayPane.addArrayColumn(null);
          arrayPane.addArrayColumn(resources.getString("GML.title"));
          arrayPane.addArrayColumn(resources.getString("GML.publisher"));
          arrayPane.addArrayColumn(resources.getString("GML.dateBegin"));
          arrayPane.addArrayColumn(resources.getString("GML.dateEnd"));

          for (News news : infos) {
            PublicationDetail pub = news.getPublication();

			ArrayLine line = arrayPane.addArrayLine();
		IconPane iconPane1 = gef.getIconPane();
		Icon debIcon = iconPane1.addIcon();
		debIcon.setProperties(m_context+"/util/icons/Actualite.gif", "", "");
		line.addArrayCellIconPane(iconPane1);	
            line.addArrayCellLink(EncodeHelper.javaStringToHtmlString(pub.getName()), "View?Id="+pub.getPK().getId());
                                                try {
                                                        UserDetail detail = quickinfo.getUserDetail(pub.getCreatorId());
                                                        line.addArrayCellText(detail.getLastName() + " " + detail.getFirstName());
                                                } catch (Exception e) {
                                                        SilverTrace.error("quickinfo", "quickInfoPublisher.jsp", "admin.EX_ERR_GET_USER_DETAILS", e);
                                                        line.addArrayEmptyCell();
                                                }

            if (pub.getBeginDate() == null)
              line.addArrayEmptyCell();
            else {
              ArrayCellText text = line.addArrayCellText(resources.getOutputDate(pub.getBeginDate()));
              text.setCompareOn(pub.getBeginDate());
            }
            if (pub.getEndDate() == null)
              line.addArrayEmptyCell();
            else {
              ArrayCellText text = line.addArrayCellText(resources.getOutputDate(pub.getEndDate()));
              text.setCompareOn(pub.getEndDate());
            }
          }
          out.println(arrayPane.print());
%>
</view:frame>
<%
        out.println(window.printAfter());
%>
</form>
<form name="quickInfoEditForm" action="" method="post">
  <input type="hidden" name="Id"/>
</form>
</div>
</body>
</html>