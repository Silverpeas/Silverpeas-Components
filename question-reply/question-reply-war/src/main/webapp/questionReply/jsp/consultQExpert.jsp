<%@ page import="java.util.*"%>

<%@ include file="checkQuestionReply.jsp" %>
<%

	Question question = (Question) request.getAttribute("question");
	String title = Encode.javaStringToHtmlString(question.getTitle());
	String content = Encode.javaStringToHtmlParagraphe(question.getContent());
	String date = resource.getOutputDate(question.getCreationDate());
	String id = question.getPK().getId();
	int status = question.getStatus();
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

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	browseBar.setDomainName(spaceLabel);
   	browseBar.setPath("<a href="+routerUrl+"Main>"+resource.getString("questionReply.listQ")+ "</a> > " + title);
	
	if (status == 0)
		operationPane.addOperation(resource.getIcon("questionReply.modifQ"), resource.getString("questionReply.modifQ"), "javascript:updQ();");	
	if (status == 1)
		operationPane.addOperation(resource.getIcon("questionReply.cloreQ"), resource.getString("questionReply.cloreQ"), "javascript:onClick=CloseQ('"+id+"');");	
	if (status == 2)
		operationPane.addOperation(resource.getIcon("questionReply.delQ"), resource.getString("questionReply.delQ"), "javascript:onClick=DeleteQ('"+id+"');");		
	if (status != 2)
	{
		operationPane.addLine();
		operationPane.addOperation(resource.getIcon("questionReply.ajoutR"), resource.getString("questionReply.ajoutR"), "javascript:addR();");	
	}
	if (existPublicR(replies))
	{
		operationPane.addLine();
		operationPane.addOperation(resource.getIcon("questionReply.delR"), resource.getString("questionReply.delPublicRs"), "javascript:onClick=DeletesR();");	
	}
	
	out.println(window.printBefore());
 	out.println(frame.printBefore());
 	out.println(board.printBefore());
%>

<center>
<table CELLPADDING=5 width="100%">
	<tr align=center> 
		<td class="intfdcolor4" valign="baseline" align=left width="100" class="txtlibform"><%=resource.getString("questionReply.question")%> :</td>
		<td class="intfdcolor4" valign="baseline" align=left><%=title%></td>
	</tr>
	<tr align=center> 
		<td class="intfdcolor4" valign="top" align=left class="txtlibform"><%=resource.getString("GML.description")%> :</td>
		<td class="intfdcolor4" valign="top" align=left><%=content%></td>
	</tr>
	<tr align=center> 
		<td class="intfdcolor4" valign="baseline" align=left class="txtlibform"><%=resource.getString("GML.date")%> :</td>
		<td class="intfdcolor4" valign="baseline" align=left><%=date%></td>
	</tr>
	<tr align=center> 
		<td class="intfdcolor4" valign="baseline" align=left class="txtlibform"><%=resource.getString("GML.publisher")%> :</td>
		<td class="intfdcolor4" valign="baseline" align=left><%=creator%></td>
	</tr>
</table>
</CENTER>
<% out.println(board.printAfter()); %>
<br>
<FORM METHOD=POST ACTION="">
<%
	ArrayPane arrayPane = gef.getArrayPane("questionReply", routerUrl+"ConsultQuestion", request, session);		
	ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
	arrayColumn0.setSortable(false);
	arrayPane.addArrayColumn(resource.getString("GML.name"));
	arrayPane.addArrayColumn(resource.getString("GML.date"));
	arrayPane.addArrayColumn(resource.getString("GML.publisher"));
	arrayPane.addArrayColumn(resource.getString("GML.status"));
	ArrayColumn arrayColumn = arrayPane.addArrayColumn(resource.getString("GML.operation"));
	arrayColumn.setSortable(false);
	while(it.hasNext())
	{
		Reply reply = (Reply) it.next();
		String titleR = Encode.javaStringToHtmlString(reply.getTitle());
		String creatorR = Encode.javaStringToHtmlString(reply.readCreatorName());
		String dateR = resource.getOutputDate(reply.getCreationDate());
		String idR = reply.getPK().getId();
		int statusR = reply.getPublicReply();
		ArrayLine arrayLine = arrayPane.addArrayLine();
		
		IconPane iconPane1 = gef.getIconPane();
		Icon debIcon = iconPane1.addIcon();
		debIcon.setProperties(resource.getIcon("questionReply.miniconeReponse"), "", routerUrl+"ConsultReplyQuery?replyId="+idR);
		arrayLine.addArrayCellIconPane(iconPane1);	
		arrayLine.addArrayCellLink(titleR, routerUrl+"ConsultReplyQuery?replyId="+idR);
		arrayLine.addArrayCellText(dateR);
		arrayLine.addArrayCellText(creatorR);
		if (statusR == 1)
		{
			arrayLine.addArrayCellText(displayIcon(resource.getIcon("questionReply.minicone"), resource.getString("questionReply.publique")));
			arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"checkedReply\" value=\""+idR+"\">");
		}
		else
		{
			arrayLine.addArrayCellText("");
			arrayLine.addArrayCellText("");
		}
	}
	out.println(arrayPane.print());
%>
</FORM>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>