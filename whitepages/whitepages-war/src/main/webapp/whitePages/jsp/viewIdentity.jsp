<%@ page import="com.stratelia.silverpeas.util.PairObject"%>
<%@ page import="com.silverpeas.whitePages.model.*"%>
<%@ page import="com.silverpeas.form.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>


<%@ include file="checkWhitePages.jsp" %>

<%
	String hostSpaceName = (String) request.getAttribute("HostSpaceName");
  	String hostPath = (String)request.getAttribute("HostPath");
  	Boolean ficheVisible = (Boolean) request.getAttribute("IsFicheVisible");
  	
  	browseBar.setDomainName(hostSpaceName);
  	browseBar.setPath(hostPath);
  
	Card card = (Card) request.getAttribute("card");
	Form userForm = (Form) request.getAttribute("Form");
	PagesContext context = (PagesContext) request.getAttribute("context"); 
	DataRecord data = (DataRecord) request.getAttribute("data"); 
 
	boolean isAdmin = (request.getAttribute("isAdmin") == null) ? false : ((Boolean) request.getAttribute("isAdmin")).booleanValue();
	//boolean isFicheVisible = isAdmin || scc.getUserId().equals(card.getUserId());
	boolean isFicheVisible = ficheVisible.booleanValue();

	tabbedPane.addTab(resource.getString("whitePages.id"), routerUrl+"viewIdentity", true, false);
	if (isFicheVisible) {
		tabbedPane.addTab(resource.getString("whitePages.fiche"), routerUrl+"viewCard?userCardId="+card.getPK().getId(), false, true);
	}
	
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.back"), routerUrl+"Main", false));

%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
</HEAD>

<BODY class="yui-skin-sam" marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<FORM NAME="myForm" METHOD="POST" ACTION="#">
<%
out.println(window.printBefore());
out.println(tabbedPane.print());
out.println(frame.printBefore());
%>

<center>
<%
	userForm.display(out, context, data);
%>

<br>
<%=buttonPane.print() %>
</center>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>

</FORM>
</BODY>
</HTML>
