<%@ include file="checkKmelia.jsp" %>
<%@ include file="tabManager.jsp.inc" %>

<%
//Récupération des paramètres
PublicationDetail 	publication 		= (PublicationDetail) request.getAttribute("Publication");
String 				linkedPathString 	= (String) request.getAttribute("LinkedPathString");
String 				wizard 				= (String) request.getAttribute("Wizard");
String				currentLang 		= (String) request.getAttribute("Language");

String pubName = publication.getName(currentLang);
String pubId = publication.getPK().getId();

boolean isOwner = false;
if (kmeliaScc.getSessionOwner())
      isOwner = true;
%>

<html>
<head><title><%=resources.getString("ReadingControlTitle")%></title>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">
function topicGoTo(id) 
{
	closeWindows();
	location.href="GoToTopic?Id="+id;
}

function closeWindows() 
{
    if (window.publicationWindow != null)
        window.publicationWindow.close();
}
</script>
</head>
<body onUnload="closeWindows()">
<% 
	Window window = gef.getWindow();
	Frame frame = gef.getFrame();
	Board boardHelp = gef.getBoard();
	  
	
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(linkedPathString);
	browseBar.setExtraInformation(pubName);
	
	OperationPane operationPane = window.getOperationPane();
	
	out.println(window.printBefore());
	  
	if (isOwner)
		displayAllOperations(pubId, kmeliaScc, gef, "ViewReadingControl", resources, out, kmaxMode);
	else
	    displayUserOperations(pubId, kmeliaScc, gef, "ViewReadingControl", resources, out, kmaxMode);
	  
	out.println(frame.printBefore());
	if ("finish".equals(wizard))
	{
		//  cadre d'aide
		out.println(boardHelp.printBefore());
		out.println("<table border=\"0\"><tr>");
		out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resources.getIcon("kmelia.info")+"\"></td>");
		out.println("<td>"+resources.getString("kmelia.HelpReadingControl")+"</td>");
		out.println("</tr></table>");
		out.println(boardHelp.printAfter());
		out.println("<BR>");
	}
	
	String url = kmeliaScc.getComponentUrl()+"ReadingControl";
	String objectType = "Publication";
	
	out.flush();
	getServletConfig().getServletContext().getRequestDispatcher("/statistic/jsp/readingControl.jsp?id="+pubId+"&url="+url+"&componentId="+componentId+"&objectType="+objectType).include(request, response);
		
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>