<%@ include file="checkKmelia.jsp" %>
<%@ include file="tabManager.jsp.inc" %>

<%
//Récupération des paramètres
PublicationDetail 	publication 		= (PublicationDetail) request.getAttribute("Publication");
String 				linkedPathString 	= (String) request.getAttribute("LinkedPathString");
String				currentLang 		= (String) request.getAttribute("Language");
List				validationSteps		= (List) request.getAttribute("ValidationSteps");
String				profile				= (String) request.getAttribute("Role");

String pubName = publication.getName(currentLang);
String pubId = publication.getPK().getId();

%>

<html>
<head>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">
function topicGoTo(id) 
{
	location.href="GoToTopic?Id="+id;
}

function pubForceValidate() {
	location.href="ForceValidatePublication";
}
</script>
</head>
<body>
<% 
	Window window = gef.getWindow();
	Frame frame = gef.getFrame();
	
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(linkedPathString);
	browseBar.setExtraInformation(pubName);
	
	OperationPane operationPane = window.getOperationPane();
	
	out.println(window.printBefore());
	  
	displayAllOperations(pubId, kmeliaScc, gef, "ViewValidationSteps", resources, out, kmaxMode);
	  
	out.println(frame.printBefore());
	
	if ("admin".equals(profile))
	{
		Board boardHelp = gef.getBoard();
		
		Button validButton = (Button) gef.getFormButton(resources.getString("kmelia.ForceValidation"), "javaScript:pubForceValidate();", false);
		
		out.println(boardHelp.printBefore());
		out.println("<center>");
		out.println("<table border=\"0\" width=\"600px\"><tr><td align=\"center\">");
		out.println(resources.getString("kmelia.ForceValidationHelp")+"<br/>");
		out.println("</td></tr></table>");
		out.println(validButton.print());
		out.println("</center>");
		out.println(boardHelp.printAfter());
		out.println("<br/>");
	}
	
    ArrayPane arrayPane = gef.getArrayPane("validationSteps", "ViewValidationSteps", request, session);
    arrayPane.setVisibleLineNumber(20);

    arrayPane.addArrayColumn(resources.getString("GML.user"));
    arrayPane.addArrayColumn(resources.getString("kmelia.validationDate"));
    
    Iterator it = validationSteps.iterator();
    while (it.hasNext())
    {
    	ArrayLine ligne = arrayPane.addArrayLine();
    	
    	ValidationStep step = (ValidationStep) it.next();
    	ligne.addArrayCellText(step.getUserFullName());
    	
    	Date validationDate = step.getValidationDate();
    	String sDate = "";
        if (validationDate == null) 
        	sDate = resources.getString("kmelia.PublicationValidationInWait");
        else 
        	sDate = resources.getOutputDateAndHour(validationDate);
        ArrayCellText cell1 = ligne.addArrayCellText(sDate);
        cell1.setCompareOn(validationDate);
    }
    	
    out.println(arrayPane.print());  
		
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>