<%@ include file="check.jsp" %>

<%
Form 			formUpdate 		= (Form) request.getAttribute("Form");
DataRecord 		data 			= (DataRecord) request.getAttribute("Data"); 
PhotoDetail	 	photo 			= (PhotoDetail) request.getAttribute("Photo");
Collection		path 			= (Collection) request.getAttribute("Path");
Integer			nbCom			= (Integer) request.getAttribute("NbComments");
Boolean			isUsePdc		= (Boolean) request.getAttribute("IsUsePdc");
boolean			showComments	= ((Boolean) request.getAttribute("ShowCommentsTab")).booleanValue();

PagesContext 		context 	= new PagesContext("myForm", "0", resource.getLanguage(), false, componentId, gallerySC.getUserId(), gallerySC.getAlbum(gallerySC.getCurrentAlbumId()).getNodePK().getId());
context.setObjectId(photo.getId());
context.setBorderPrinted(false);

String photoId 	= photo.getPhotoPK().getId();
String nbComments = nbCom.toString();
%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<% formUpdate.displayScripts(out, context); %>
<script language="javaScript">
function B_VALIDER_ONCLICK()
{
	if (isCorrectForm())
	{
		document.myForm.submit();
	}
}
</script>
</HEAD>
<BODY class="yui-skin-sam" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" >
<%
    Board board = gef.getBoard();
    
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
    browseBar.setPath(displayPath(path));

	Button cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "PreviewPhoto?PhotoId="+photoId, false);
	Button validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false);
		
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("gallery.photo"), "PreviewPhoto?PhotoId="+photoId, false);
	tabbedPane.addTab(resource.getString("GML.head"), "EditPhoto?PhotoId="+photoId, false);
	tabbedPane.addTab(resource.getString("gallery.form"), "#", true, false);
	tabbedPane.addTab(resource.getString("gallery.info"), "EditInformation?PhotoId="+photoId, false);
	if (showComments)
		tabbedPane.addTab(resource.getString("gallery.comments")+" ("+nbComments+")", "Comments?PhotoId="+photoId, false);
	tabbedPane.addTab(resource.getString("gallery.accessPath"), "AccessPath?PhotoId="+photoId, false);
	if (isUsePdc.booleanValue())
		tabbedPane.addTab(resource.getString("GML.PDC"), "PdcPositions?PhotoId="+photoId, false);
	
    out.println(window.printBefore());
    out.println(tabbedPane.print());
    out.println(frame.printBefore());
    out.println(board.printBefore());
    
%>
<FORM NAME="myForm" METHOD="POST" ACTION="UpdateXMLForm" ENCTYPE="multipart/form-data">
	<% 
		formUpdate.display(out, context, data); 
	%>
	<input type="hidden" name="PhotoId" value="<%=photoId%>">
</FORM>
<%
	out.println(board.printAfter());
	
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(validateButton);
	buttonPane.addButton(cancelButton);
	out.println("<br><center>"+buttonPane.print()+"</center>");

    out.println(frame.printAfter());
    out.println(window.printAfter()); 
%>
</BODY>
</HTML>