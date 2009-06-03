<%@ page import="java.util.*"%>

<%@ include file="checkQuestionReply.jsp" %>

<%
	Question question = (Question) request.getAttribute("question");
	String title = Encode.javaStringToHtmlString(question.getTitle());
	String content = Encode.javaStringToHtmlParagraphe(question.getContent());
	String date = resource.getOutputDate(question.getCreationDate());
	String id = question.getPK().getId();
	String creator = Encode.javaStringToHtmlString(question.readCreatorName());
	Collection replies = question.readReplies();
	Iterator it = replies.iterator();
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>

<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">
function vue(replyId){
	SP_openWindow(routerUrl+'ConsultReplyQuery?replyId='+replyId, 'consult_reponse', '500', '230', 'menubar=no,scrollbars=no,statusbar=no')
}
</script>

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setPath(title);
	
	out.println(window.printBefore());
 	out.println(frame.printBefore());
 	out.println(board.printBefore());
%>
<center>
<table CELLPADDING=5 width="100%">
	<tr align=center> 
		<td  class="intfdcolor4" valign="baseline" align=left width="100" class="txtlibform"><%=resource.getString("questionReply.question")%> :</td>
		<td  class="intfdcolor4" valign="baseline" align=left><%=title%></td>
	</tr>
	<tr align=center> 
		<td  class="intfdcolor4" valign="top" align=left class="txtlibform"><%=resource.getString("questionReply.description")%> :</td>
		<td  class="intfdcolor4" valign="top" align=left><%=content%></td>
	</tr>
	<tr align=center> 
		<td  class="intfdcolor4" valign="baseline" align=left class="txtlibform"><%=resource.getString("GML.date")%> :</td>
		<td  class="intfdcolor4" valign="baseline" align=left><%=date%></td>
	</tr>
	<tr align=center> 
		<td  class="intfdcolor4" valign="baseline" align=left class="txtlibform"><%=resource.getString("GML.publisher")%> :</td>
		<td  class="intfdcolor4" valign="baseline" align=left><%=creator%></td>
	</tr>
</table>
</CENTER>
<% out.println(board.printAfter()); %>
<br>
<%
	ArrayPane arrayPane = gef.getArrayPane("questionReply", routerUrl+"ConsultQuestion", request, session);	
	ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
	arrayColumn0.setSortable(false);
	arrayPane.addArrayColumn(resource.getString("GML.name"));
	arrayPane.addArrayColumn(resource.getString("GML.date"));
	ArrayColumn arrayColumn = arrayPane.addArrayColumn(resource.getString("GML.publisher"));
	arrayColumn.setSortable(false);
	while(it.hasNext())
	{
		Reply reply = (Reply) it.next();
		String titleR = Encode.javaStringToHtmlString(reply.getTitle());
		String creatorR = Encode.javaStringToHtmlString(reply.readCreatorName());
		String dateR = resource.getOutputDate(reply.getCreationDate());
		String idR = reply.getPK().getId();

		ArrayLine arrayLine = arrayPane.addArrayLine();
		
		IconPane iconPane1 = gef.getIconPane();
		Icon debIcon = iconPane1.addIcon();
		debIcon.setProperties(resource.getIcon("questionReply.miniconeReponse"), "", "javascript:vueR('"+idR+"');");
		arrayLine.addArrayCellIconPane(iconPane1);	
		arrayLine.addArrayCellLink(titleR, "javascript:vueR('"+idR+"');");
		arrayLine.addArrayCellText(dateR);
		arrayLine.addArrayCellText(creatorR);				
	}				
	out.println(arrayPane.print());
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>