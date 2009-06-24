<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="tabManager.jsp.inc" %>
<%@ include file="topicReport.jsp.inc" %>

<%
PublicationDetail 	pubDetail 	= kmeliaScc.getSessionPublication().getPublication().getPublicationDetail();

String 	profile 	= (String) request.getParameter("Profile");
String 	id 			= (String) request.getParameter("PubId");
String 	wizard		= (String) request.getAttribute("Wizard");
String	currentLang = (String) request.getAttribute("Language");

String 	pubName		= pubDetail.getName(currentLang);

//Vrai si le user connecte est le createur de cette publication ou si il est admin
boolean isOwner = false;
if (kmeliaScc.getSessionOwner())
	isOwner = true;

String linkedPathString = kmeliaScc.getSessionPath();

String user_id = kmeliaScc.getUserId();

%>
<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<TITLE></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script language="javascript">
function topicGoTo(id) {
	location.href="GoToTopic?Id="+id;
}

function sendOperation(operation) {
    document.pubForm.Action.value = operation;
    document.pubForm.submit();
}
</script>
</HEAD>
<BODY>
<%
        Window window = gef.getWindow();
        Frame frame = gef.getFrame();

        Board boardHelp = gef.getBoard();
        
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(spaceLabel);
        browseBar.setComponentName(componentLabel, "Main");
        browseBar.setPath(linkedPathString);
		browseBar.setExtraInformation(pubName);

        out.println(window.printBefore());

        if (isOwner)
            displayAllOperations(id, kmeliaScc, gef, "ViewComment", resources, out, kmaxMode);
        else
            displayUserOperations(id, kmeliaScc, gef, "ViewComment", resources, out, kmaxMode);

        out.println(frame.printBefore());
        
        if ("finish".equals(wizard))
    	{
    		//  cadre d'aide
    	    out.println(boardHelp.printBefore());
    		out.println("<table border=\"0\"><tr>");
    		out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resources.getIcon("kmelia.info")+"\"></td>");
    		out.println("<td>"+kmeliaScc.getString("kmelia.HelpComment")+"</td>");
    		out.println("</tr></table>");
    	    out.println(boardHelp.printAfter());
    	    out.println("<BR>");
    	}
        
       	out.flush();

       	String url = kmeliaScc.getComponentUrl()+"Comments";
       	String indexIt = "0";
	   	if (kmeliaScc.isIndexable(pubDetail))
			indexIt = "1";

       getServletConfig().getServletContext().getRequestDispatcher("/comment/jsp/comments.jsp?id="+id+"&userid="+user_id+"&profile="+profile+"&url="+url+"&component_id="+kmeliaScc.getComponentId()+"&IndexIt="+indexIt).include(request, response);

       out.println(frame.printAfter());
       out.println(window.printAfter());
%>
</BODY>
</HTML>