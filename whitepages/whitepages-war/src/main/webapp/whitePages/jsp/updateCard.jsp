<%@ page import="java.util.*"%>
<%@ page import="com.silverpeas.whitePages.model.*"%>
<%@ page import="com.silverpeas.form.*"%>

<%@ include file="checkWhitePages.jsp" %>

<%
	browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel);
	browseBar.setPath(resource.getString("whitePages.usersList") + " > "+ resource.getString("whitePages.editCard"));
		
	Card			card		= (Card) request.getAttribute("card");
	Form			updateForm	= (Form) request.getAttribute("Form");
	PagesContext	context		= (PagesContext) request.getAttribute("context"); 
	DataRecord		data		= (DataRecord) request.getAttribute("data"); 	
%>


<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<%
   updateForm.displayScripts(out, context);
%>
<script language="JavaScript">
<!--	
	function B_VALIDER_ONCLICK(idCard) {
		if (isCorrectForm())
		{	   
		 
			document.myForm.action = "<%=routerUrl%>effectiveUpdate?userCardId="+idCard;
			document.myForm.submit();
		}
	}

	function B_ANNULER_ONCLICK(idCard) {
		self.close();
	}	
//-->
</script>
</HEAD>

<BODY class="yui-skin-sam" marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<FORM NAME="myForm" METHOD="POST" ENCTYPE="multipart/form-data">

<%
out.println(window.printBefore());
out.println(tabbedPane.print());
out.println(frame.printBefore());
%>
<center>
<%
	updateForm.display(out, context, data);
%>
<br>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK('"+card.getPK().getId()+"');", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK('"+card.getPK().getId()+"');", false));
    out.println(buttonPane.print());
%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</FORM>
</BODY>
</HTML>