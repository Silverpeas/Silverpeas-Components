<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>

<%@ include file="checkSurvey.jsp" %>

<%
	Collection users = (Collection) request.getAttribute("Users"); 


	// déclaration des boutons
	Button close = (Button) gef.getFormButton(resources.getString("GML.close"), "javaScript:window.close();", false);
	String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>

<html>
<head>
	<title></title>
	<% out.println(gef.getLookStyleSheet()); %>
	<script type="text/javascript" src="<%=iconsPath%>/util/javaScript/animation.js"></script>
	<script language="JavaScript1.2">

        function viewResultByUser(userId, userName)
        {
        	url = "UserResult?UserId="+userId+"&UserName="+userName;
 		    windowName = "resultByUser";
 		    larg = "700";
 		    haut = "500";
 		    windowParams = "directories=0,menubar=0,toolbar=0,resizable=1,scrollbars=1,alwaysRaised";
 		    suggestions = SP_openWindow(url, windowName, larg , haut, windowParams);
 		    suggestions.focus();
        }
 				   
     	</script>
</head>

<body>
<% 
	Window window = gef.getWindow();
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(surveyScc.getSpaceLabel());
	browseBar.setComponentName(surveyScc.getComponentLabel(),"surveyList.jsp");
	Frame frame = gef.getFrame();
	
	out.println(window.printBefore());
	out.println(frame.printBefore());

	ArrayPane arrayPane = gef.getArrayPane("", "", request, session);
	arrayPane.addArrayColumn(resources.getString("GML.name"));
	
	if (users != null)
	{	
		Iterator it = users.iterator();
		while (it.hasNext())
		{
			String userId = (String) it.next();
			UserDetail user = surveyScc.getUserDetail(userId);	
			ArrayLine ligne = arrayPane.addArrayLine();
			String url = "<a href=\"javaScript:onClick=viewResultByUser('"+userId+"','"+Encode.javaStringToHtmlString(user.getDisplayedName())+"');\">"+Encode.javaStringToHtmlString(user.getDisplayedName())+"</a>";
			ligne.addArrayCellText(url);
		}
	}

	out.println(arrayPane.print());
	
	ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(close);
	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
	
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>
