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
<HTML>
<HEAD>
<TITLE></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<%
out.println(gef.getLookStyleSheet());
%>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>

</HEAD>
<BODY>
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

<FORM NAME="toComponent" ACTION="quickInfoEdit.jsp" METHOD=POST >
        <input type="hidden" name="Action" value="changePage">
        <input type="hidden" name="Id" value="<%=pubId%>">
        <input type="hidden" name="page" value="2">
</FORM>

</BODY>
</HTML>