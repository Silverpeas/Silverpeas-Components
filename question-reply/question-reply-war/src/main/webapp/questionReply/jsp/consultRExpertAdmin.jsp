<%@ page import="java.util.*"%>

<%@ include file="checkQuestionReply.jsp" %>
<%
	Reply reply = (Reply) request.getAttribute("reply");
	String title = Encode.javaStringToHtmlString(reply.getTitle());
	String content = Encode.javaStringToHtmlParagraphe(reply.getContent());
	String date = resource.getOutputDate(reply.getCreationDate());
	String id = reply.getPK().getId();
	String creator = Encode.javaStringToHtmlString(reply.readCreatorName());
	int status = reply.getPublicReply();
	Question question = (Question) request.getAttribute("question");
	String titleQ = Encode.javaStringToHtmlString(question.getTitle());
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
	browseBar.setPath("<A HREF="+routerUrl+"Main>"+resource.getString("questionReply.listQ")+ "</A> > <A HREF="+routerUrl+"ConsultQuestionQuery?questionId="+reply.getQuestionId()+"> "+titleQ+ "</A>  > " + resource.getString("questionReply.consultR"));
	
	if (status == 1)
	{
		operationPane.addOperation(resource.getIcon("questionReply.modifR"), resource.getString("questionReply.modifPublicR"), "javascript:updR();");	
		operationPane.addLine();
		operationPane.addOperation(resource.getIcon("questionReply.delR"), resource.getString("questionReply.delPublicR"), "javascript:onClick=DeleteRadmin('"+id+"');");	
	}
	out.println(window.printBefore());    
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>

<center>
<table CELLPADDING=5 width="100%">
	<tr align=center> 
		<td class="intfdcolor4" valign="baseline" align=left class="txtlibform" width="100"><%=resource.getString("questionReply.reponse")%> :</td>
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
<%
out.println(board.printAfter());
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>