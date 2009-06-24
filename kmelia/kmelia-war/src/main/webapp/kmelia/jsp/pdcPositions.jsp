<%@ include file="checkKmelia.jsp" %>
<%@ include file="tabManager.jsp.inc"%>

<%
String	wizardLast		= (String) request.getAttribute("WizardLast");
String 	wizard			= (String) request.getAttribute("Wizard");
String 	wizardRow		= (String) request.getAttribute("WizardRow");
String	currentLang 	= (String) request.getAttribute("Language");

if (!StringUtil.isDefined(wizardLast))
	wizardLast = "4";

if (wizardRow == null)
	wizardRow = "4";

boolean isEnd = true;

String pubName			= kmeliaScc.getSessionPublication().getPublication().getPublicationDetail().getName(currentLang);
String linkedPathString = kmeliaScc.getSessionPath();
String pubId			= kmeliaScc.getSessionPublication().getPublication().getPublicationDetail().getPK().getId();
String url				= kmeliaScc.getComponentUrl()+"ViewPdcPositions";

%>
<HTML>
<HEAD>
<TITLE></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
function topicGoTo(id) {
	location.href="GoToTopic?Id="+id;
}
function validate() {
	if (<%=kmeliaScc.isDraftOutAllowed()%>)
	{
		document.validForm.action = "WizardNext";
		document.validForm.submit();
	}
	else
	{
		window.alert("<%=kmeliaScc.getString("kmelia.PdcClassificationMandatory")%>");
	}
}
</script>

</HEAD>
<BODY>
<%
	Window			window			= gef.getWindow();
	Frame			frame			= gef.getFrame();
	
	OperationPane	operationPane	= window.getOperationPane();
	BrowseBar		browseBar		= window.getBrowseBar();
	
	browseBar.setDomainName(kmeliaScc.getSpaceLabel());
	browseBar.setComponentName(kmeliaScc.getComponentLabel(), "javascript:onClick=topicGoTo('0')");
	browseBar.setPath(linkedPathString);
	browseBar.setExtraInformation(pubName);

	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_add.gif", resources.getString("GML.PDCNewPosition"), "javascript:openSPWindow('"+m_context+"/RpdcClassify/jsp/NewPosition','newposition')");
	operationPane.addOperation(m_context+"/pdcPeas/jsp/icons/pdcPeas_position_to_del.gif", resources.getString("GML.PDCDeletePosition"), "javascript:getSelectedItems()");

	Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "DeletePublication?PubId="+pubId, false);
	Button nextButton;
	if (isEnd)
		nextButton = (Button) gef.getFormButton(resources.getString("kmelia.End"), "javascript:validate()", false);
	else
		nextButton = (Button) gef.getFormButton(resources.getString("GML.next"), "javascript:validate()", false);

	out.println(window.printBefore());

	if ("progress".equals(wizard))
		displayWizardOperations(wizardRow, pubId, kmeliaScc, gef, "ViewPdcPositions", resources, out, kmaxMode);
	else
		displayAllOperations(pubId, kmeliaScc, gef, "ViewPdcPositions", resources, out);

	out.println(frame.printBefore());
	if ("finish".equals(wizard) || "progress".equals(wizard))
	{
		//  cadre d'aide
		Board boardHelp = gef.getBoard();
	    out.println(boardHelp.printBefore());
		out.println("<table border=\"0\"><tr>");
		out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resources.getIcon("kmelia.info")+"\"></td>");
		out.println("<td>"+kmeliaScc.getString("kmelia.HelpPdc")+"</td>");
		out.println("</tr></table>");
	    out.println(boardHelp.printAfter());
	    out.println("<BR>");
	}
	out.flush();

	getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/positionsInComponent.jsp?SilverObjectId="+kmeliaScc.getSilverObjectId(pubId)+"&ComponentId="+componentId+"&ReturnURL="+URLEncoder.encode(url)+"&SendSubscriptions=0").include(request, response);

	if ("progress".equals(wizard))
	{
		ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton(nextButton);
		buttonPane.addButton(cancelButton);
		buttonPane.setHorizontalPosition();
		out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
	}
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<FORM NAME="validForm" ACTION="WizardNext" METHOD=POST >
	<input type="hidden" name="Action">
	<input type="hidden" name="Position" value="Pdc">
</FORM>
<FORM NAME="toComponent" ACTION="ViewPdcPositions" METHOD=POST >
</FORM>
</BODY>
</HTML>