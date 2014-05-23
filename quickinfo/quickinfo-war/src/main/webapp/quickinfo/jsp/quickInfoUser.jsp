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
<%@page import="org.silverpeas.components.quickinfo.model.News"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkQuickInfo.jsp" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ page import="org.silverpeas.wysiwyg.control.WysiwygController" %>
<%@ page import="com.silverpeas.util.*" %>
<%@ page import="com.silverpeas.util.i18n.I18NHelper" %>

<%
List<News> infosI = (List<News>) request.getAttribute("infos");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>QuickInfo - User</title>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/formUtil.js"></script>
<view:looknfeel/>
</head>
<body class="txtlist" id="quickinfo">
<div id="<%=componentId %>">
  <%
	Window mainWin = gef.getWindow();
	Frame maFrame = gef.getFrame();
	OperationPane operationPane = mainWin.getOperationPane();

	out.println(mainWin.printBefore());
	out.println(maFrame.printBefore());

	ArrayPane arrayPane = gef.getArrayPane("quickinfoList", pageContext);
	arrayPane.setXHTML(true);
	arrayPane.addArrayColumn(null);

	ArrayCellText cellText = null;
	for (News news : infosI) {
		PublicationDetail pub = news.getPublication();
		ArrayLine line = arrayPane.addArrayLine();
		String st = "<b>" + pub.getName() + "</b>";
		UserDetail user = quickinfo.getUserDetail(pub.getUpdaterId());
		String date = resources.getOutputDate(pub.getBeginDate());
		if (!StringUtil.isDefined(date))
		{
		  date = resources.getOutputDate(pub.getUpdateDate());
		}
		st += "<br/>"+user.getDisplayedName()+" - "+date;
		String description = WysiwygController.load(pub.getPK().getInstanceId(), pub.getPK().getId(), I18NHelper.defaultLanguage);
		if (StringUtil.isDefined(description)) {
		   st = st + "<br/>" + description;
		}
		line.addArrayCellText(st);
	}
	out.println(arrayPane.print());

	out.println(maFrame.printAfter());
	out.println(mainWin.printAfter());
  %>
</div>
</body>
</html>