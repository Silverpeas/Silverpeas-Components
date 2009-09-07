<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkForums.jsp" %>
<%@ include file="tabManager.jsp" %>
<%
    int forumId = getIntParameter(request, "forumId");
    int params = getIntParameter(request, "params");
    String url = fsc.getComponentUrl() + ActionUrl.getUrl("pdcPositions", -1, params, forumId);
%>
<html>
<head>
    <title></title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"><%

    out.println(graphicFactory.getLookStyleSheet());
%>
    <script type="text/javascript" src="<%=context%>/forums/jsp/javaScript/forums.js"></script>
    <script type="text/javascript" src="<%=context%>/util/javaScript/animation.js"></script>
</head>

<body>
<%
    Window window = graphicFactory.getWindow();
    Frame frame = graphicFactory.getFrame();

    if (!isReader)
    {
    	OperationPane operationPane = window.getOperationPane();
        operationPane.addOperation(context + "/pdcPeas/jsp/icons/pdcPeas_position_to_add.gif",
            fsc.getString("NewPdcPosition"), "javascript:openSPWindow('" + context
                + "/RpdcClassify/jsp/NewPosition','newposition')");
        operationPane.addOperation(context + "/pdcPeas/jsp/icons/pdcPeas_position_to_del.gif",
            fsc.getString("DeletePdcPosition"), "javascript:getSelectedItems()");
    }
    
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(fsc.getSpaceLabel());
    browseBar.setComponentName(fsc.getComponentLabel(), ActionUrl.getUrl("main"));

    out.println(window.printBefore());

    displayTabs(params, forumId, fsc, graphicFactory, "ViewPdcPositions", out);
    
    out.println(frame.printBefore());

    out.flush();

    getServletConfig().getServletContext().getRequestDispatcher(
            "/pdcPeas/jsp/positionsInComponent.jsp"
            + "?SilverObjectId=" + fsc.getSilverObjectId(params)
            + "&ComponentId=" + fsc.getComponentId()
            + "&ReturnURL=" + URLEncoder.encode(url, "ISO-8859-1"))
        .include(request, response);

    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
    <form name="toComponent" action="pdcPositions.jsp" method="post">
        <input type="hidden" name="params" value="<%=params%>">
        <input type="hidden" name="forumId" value="<%=forumId%>">
    </form>
</body>
</html>