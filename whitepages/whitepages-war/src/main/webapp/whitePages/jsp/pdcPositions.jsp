<%@ page import="javax.servlet.*"%>
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

<%@ include file="checkWhitePages.jsp" %>

<%

String userCardId		= (String) request.getAttribute("UserCardId");
String url				= (String) request.getAttribute("ReturnURL");
String silverContentId	= (String) request.getAttribute("SilverContentId");

String firstVisite		= (String) request.getAttribute("FirstVisite");

%>
<HTML>
<HEAD>
<TITLE></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">

	function newPosition() {
		document.newPosition.action = "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')";	
		document.newPosition.submit();	
	}

</script>
</HEAD>

<BODY <%if (firstVisite.equals("1")) out.print("onload=\"javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition');\"");%>>

<BODY>
<%
	browseBar.setDomainName(spaceLabel);
	if (containerContext == null) {
		browseBar.setComponentName(componentLabel, "Main");
	} else {
		browseBar.setComponentName(componentLabel, m_context+containerContext.getReturnURL()); 
	}
	browseBar.setPath(resource.getString("whitePages.usersList") + " > "+ resource.getString("whitePages.PdcClassification"));

	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_add.gif", resource.getString("whitePages.NewPdcPosition"), "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')");
	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_del.gif", resource.getString("whitePages.DeletePdcPosition"), "javascript:getSelectedItems()");

	out.println(window.printBefore());

	tabbedPane.addTab(resource.getString("whitePages.id"), routerUrl+"consultIdentity?userCardId="+userCardId, false, true);
	tabbedPane.addTab(resource.getString("whitePages.fiche"), routerUrl+"consultCard?userCardId="+userCardId, false, true);
	tabbedPane.addTab(resource.getString("whitePages.PdcClassification"), routerUrl+"ViewPdcPositions?userCardId="+userCardId, true, false);

	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.back"), routerUrl+"Main", false));
	
	out.println(tabbedPane.print());
	out.println(frame.printBefore());

	out.flush();
 
	getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/positionsInComponent.jsp?SilverObjectId="+silverContentId+"&ComponentId="+componentId+"&ReturnURL="+URLEncoder.encode(url)).include(request, response);
		
%>
<br>
<center>
<%=buttonPane.print() %>
</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

<FORM NAME="toComponent" ACTION="ViewPdcPositions" METHOD=POST>
	<input type="hidden" name="userCardId" value="<%=userCardId%>">
</FORM>
<FORM NAME="newPosition" ACTION="" METHOD=POST>
</FORM>
</BODY>
</HTML>