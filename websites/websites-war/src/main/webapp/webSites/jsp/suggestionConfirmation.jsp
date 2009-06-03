<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="java.lang.*"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>

<%@ page import="com.stratelia.webactiv.util.*"%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>

<%@ include file="checkScc.jsp" %>
<%@ include file="util.jsp" %>

<%

String nomSite = (String) request.getParameter("nomSite");
String nomPage = (String) request.getParameter("nomPage");

%>

<HTML>
<HEAD>

<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY>
<%     
	Window window = gef.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
    //CBO : UPDATE
	//browseBar.setDomainName(scc.getSpaceLabel());
	browseBar.setDomainName(spaceLabel);
	//CBO : UPDATE
//browseBar.setComponentName(scc.getComponentLabel());
browseBar.setComponentName(componentLabel);
	browseBar.setExtraInformation(resources.getString("SuggestionLink"));

	//Le cadre
	Frame frame = gef.getFrame();

	//Le board
	Board board = gef.getBoard();

	//Début code
    out.println(window.printBefore());
    out.println(frame.printBefore());
	out.print(board.printBefore());
%>
	 <TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
		<tr>            
            <td class="intfdcolor4"><%=resources.getString("ConfirmationSuggestionMessage1") + "'<B>" + nomSite + "</B>' (" + nomPage + ") " + resources.getString("ConfirmationSuggestionMessage2")+"<BR><BR>"%> 
            </td>
        </tr>
	</TABLE>
<%
	//fin du code
	out.print(board.printAfter());
    out.println(frame.printMiddle());

	ButtonPane buttonPane = gef.getButtonPane();
	Button closeButton = (Button) gef.getFormButton(resources.getString("GML.close"), "javascript:onClick=window.close();", false);
	buttonPane.addButton(closeButton);
	buttonPane.setHorizontalPosition(); 

	out.println("<br><center>"+buttonPane.print()+"</center><br>");
	
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

</BODY>
</HTML>