<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ page import="java.util.*"%>
<%@ page import="com.stratelia.silverpeas.containerManager.*"%>

<%@ include file="checkQuestionReply.jsp" %>
<%@ include file="tabManager.jsp.inc" %>
<%
	String		profil		= (String) request.getAttribute("Flag");
	Question 	question 	= (Question) request.getAttribute("question");
	String		userId		= (String) request.getAttribute("UserId");
	
	String title = Encode.javaStringToHtmlString(question.getTitle());
	String content = Encode.javaStringToHtmlString(question.getContent());
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
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

<script language="javascript">

function deleteConfirm(replyId)
{
	//confirmation de suppression de la question
	if(window.confirm("<%=resource.getString("MessageSuppressionR")%>"))
	{
			document.RForm.action = "DeleteR";
			document.RForm.replyId.value = replyId;
			document.RForm.submit();
	}
}
</script>

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<%
	browseBar.setDomainName(spaceLabel);
   	browseBar.setPath(title);
	
   	if (profil.equals("admin") || profil.equals("writer"))
   	{
		/* Pour relance vers les experts
		if (status == 0)
			operationPane.addOperation(resource.getIcon("questionReply.relanceQ"), resource.getString("questionReply.relanceQ"), routerUrl+"RelaunchQuery");
		*/
		if (status == 1)
			operationPane.addOperation(resource.getIcon("questionReply.cloreQ"), resource.getString("questionReply.cloreQ"), "javascript:onClick=CloseQ('"+id+"');");	
		if (status != 2)
		{
			operationPane.addOperation(resource.getIcon("questionReply.ajoutR"), resource.getString("questionReply.ajoutR"), "CreateRQuery?QuestionId="+id);	
		}
		if (status == 2)
			operationPane.addOperation(resource.getIcon("questionReply.delQ"), resource.getString("questionReply.delQ"), "javascript:onClick=DeleteQ('"+id+"');");
   	}
   	
	out.println(window.printBefore());    

	boolean updateQ = true;
	// le demandeur ne peut pas modifier les questions qui ne sont pas � lui ou qui n'ont pas de r�ponses
	if (profil.equals("publisher") && !question.getCreatorId().equals(userId))
		updateQ = false;
	else if (profil.equals("publisher"))
		if (status != 0)
			updateQ = false;
	boolean pdc = true;
	if (profil.equals("publisher"))
		pdc = false;
	if (!profil.equals("user"))
		displayTabs(updateQ, pdc, id, resource, gef, "ViewQuestion", routerUrl, out);

	out.println(frame.printBefore());
	out.println(board.printBefore());
%>

<!-- affichage de la question -->
<table width="100%">
	<tr>
		<td nowrap>
			<img src="<%=resource.getIcon("questionReply.miniconeQuestion")%>">
		</td>
		<td width="100%" class="titreQuestionReponse">
			<%=Encode.javaStringToHtmlParagraphe(title)%>
		</td>
		<td nowrap>
		<a href="#"><img border=0 src="<%=resource.getIcon("questionReply.update")%>"/></a>
		</td>
	</tr>
	<tr>
		<td colspan="3"><%=Encode.javaStringToHtmlParagraphe(content)%></td>
	</tr>
	<tr>
		<td colspan="3">			
			<span class="txtBaseline">
			<%=resource.getString("questionReply.questionOf")%> <%=creator%> <%=resource.getString("questionReply.replyBy")%> <%=date%>
			</span>
		</td>
	</tr>
</table>
<% out.println(board.printAfter()); 
// affichage des r�ponses

	while(it.hasNext())
	{
		Reply reply = (Reply) it.next();
		String titleR = Encode.javaStringToHtmlString(reply.getTitle());
		String contentR = Encode.javaStringToHtmlString(reply.getContent());
		String creatorR = Encode.javaStringToHtmlString(reply.readCreatorName());
		String dateR = resource.getOutputDate(reply.getCreationDate());
		String idR = reply.getPK().getId();
		int statusR = reply.getPublicReply();
		out.println("<br>");
		out.println(board.printBefore());
		%>
		<table width="100%">
			<tr>
				<td nowrap>
					<% 
						if (statusR == 1 && !profil.equals("user"))
							out.println("<img border=\"0\" src=\""+resource.getIcon("questionReply.minicone")+"\"/>"); 
						else
							out.println("<img border=\"0\" src=\""+resource.getIcon("questionReply.miniconeReponse")+"\"/>");							
					%>
				</td>
				<td width="100%" class="titreQuestionReponse">
					<%=Encode.javaStringToHtmlParagraphe(titleR)%>
				</td>
				<td nowrap>
					<%
					if (profil.equals("admin") || profil.equals("writer"))
					{ 
					%>
						<a href="UpdateR?replyId=<%=idR%>"><img border=0 src="<%=resource.getIcon("questionReply.update")%>"/></a>
						<a href="javaScript:deleteConfirm('<%=idR%>')"><img border=0 src="<%=resource.getIcon("questionReply.delete")%>"/></a>
					<%
					}
					%>
									
				</td>
			</tr>
			<tr>
				<td colspan="3"><%=Encode.javaStringToHtmlParagraphe(contentR)%></td>
			</tr>
			<tr>
				<td colspan="3">			
					<span class="txtBaseline">
					<%=resource.getString("questionReply.replyOf")%> <%=creatorR%> <%=resource.getString("questionReply.replyBy")%> <%=dateR%>
					</span>
				</td>
			</tr>
		</table>
		<%
		out.println(board.printAfter());
		
	}

out.println(frame.printAfter());
out.println(window.printAfter());
%>
<form name="RForm" action="" Method="POST">
	<input type="hidden" name="replyId">
</form>

</BODY>
</HTML>