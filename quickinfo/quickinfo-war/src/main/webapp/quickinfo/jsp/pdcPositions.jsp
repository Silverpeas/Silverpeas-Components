<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page import="javax.servlet.*,
                 com.stratelia.webactiv.util.publication.model.PublicationDetail,
                 com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane,
                 com.stratelia.webactiv.quickinfo.control.QuickInfoSessionController"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.io.File"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>

<%@ include file="checkQuickInfo.jsp" %>

<%

String pubId   = (String)request.getAttribute("Id");
PublicationDetail quickInfoDetail = (PublicationDetail) request.getAttribute("info");

boolean isNewSubscription = true;
if (pubId != null && pubId != "-1") {
    isNewSubscription = false;
}

%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<%
out.println(gef.getLookStyleSheet());
%>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>
</head>
<body id="quickinfo">
<%
        Window                  window                  = gef.getWindow();
        Frame                   frame                   = gef.getFrame();
        OperationPane   operationPane   = window.getOperationPane();
        BrowseBar               browseBar               = window.getBrowseBar();

        Frame maFrame = gef.getFrame();

        browseBar.setDomainName(spaceLabel);
        browseBar.setComponentName(componentLabel, "Main");
        browseBar.setPath(resources.getString("edition"));

        String routerUrl = URLManager.getApplicationURL() + URLManager.getURL("quickinfo", quickinfo.getSpaceId(), quickinfo.getComponentId());
        String retUrl = quickinfo.getComponentUrl()+ "quickInfoEdit.jsp?Action=changePage&Id="+pubId+"&page=2";
                                    
        operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_add.gif", resources.getString("GML.PDCNewPosition"), "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')");
        operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_del.gif", resources.getString("GML.PDCDeletePosition"), "javascript:getSelectedItems()");
        out.println(window.printBefore());

        TabbedPane tabbedPane = gef.getTabbedPane();
        tabbedPane.addTab(resources.getString("GML.head"), routerUrl + "quickInfoEdit.jsp?Action=changePage&Id="+pubId+"&page=1",
         quickinfo.getPageId() == QuickInfoSessionController.PAGE_HEADER, !isNewSubscription);
        if (!isNewSubscription) {
        tabbedPane.addTab( resources.getString("GML.PDC"), routerUrl + "quickInfoEdit.jsp?Action=changePage&Id="+pubId
                +"&page=2", quickinfo.getPageId() != QuickInfoSessionController.PAGE_HEADER, true);
        }
        out.println(tabbedPane.print());

        out.println(frame.printBefore());

        out.flush();
        int silverObjectId = quickinfo.getSilverObjectId(pubId);
        getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/positionsInComponent.jsp?SilverObjectId=" +silverObjectId+"&ComponentId="+quickinfo.getComponentId()+"&ReturnURL=" +  URLEncoder.encode(retUrl) ).include(request, response);

        out.println(frame.printAfter());
        out.println(window.printAfter());
%>

<form name="toComponent" action="quickInfoEdit.jsp" method="post">
        <input type="hidden" name="Action" value="changePage"/>
        <input type="hidden" name="Id" value="<%=pubId%>"/>
        <input type="hidden" name="page" value="2"/>
</form>

</body>
</html>