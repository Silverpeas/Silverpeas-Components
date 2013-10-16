<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="com.stratelia.silverpeas.containerManager.*"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkQuestionReply.jsp" %>
<%@ include file="tabManager.jsp.inc" %>
<%
	String		profil		= (String) request.getAttribute("Flag");
	Question 	question 	= (Question) request.getAttribute("question");
	String		userId		= (String) request.getAttribute("UserId");
	
	String title = EncodeHelper.javaStringToHtmlString(question.getTitle());
	String content = EncodeHelper.javaStringToHtmlString(question.getContent());
	String date = resource.getOutputDate(question.getCreationDate());
	String id = question.getPK().getId();
	int status = question.getStatus();
	String creator = EncodeHelper.javaStringToHtmlString(question.readCreatorName());
	Collection replies = question.readReplies();
	Iterator it = replies.iterator();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">
function deleteConfirm(replyId) {
	//confirmation de suppression de la question
	if(window.confirm("<%=resource.getString("MessageSuppressionR")%>"))
	{
			document.RForm.action = "DeleteR";
			document.RForm.replyId.value = replyId;
			document.RForm.submit();
	}
}
</script>
</head>
<body>
<%
	browseBar.setDomainName(spaceLabel);
   	browseBar.setPath(title);
	
   	if (profil.equals("admin") || profil.equals("writer")) {
		/* Pour relance vers les experts
		if (status == 0)
			operationPane.addOperation(resource.getIcon("questionReply.relanceQ"), resource.getString("questionReply.relanceQ"), routerUrl+"RelaunchQuery");
		*/
		if (status == 1)
			operationPane.addOperation(resource.getIcon("questionReply.cloreQ"), resource.getString("questionReply.cloreQ"), "javascript:onClick=CloseQ('"+id+"');");	
		if (status != 2)
		{
			operationPane.addOperationOfCreation(resource.getIcon("questionReply.ajoutR"), resource.getString("questionReply.ajoutR"), "CreateRQuery?QuestionId="+id);	
		}
		if (status == 2)
			operationPane.addOperation(resource.getIcon("questionReply.delQ"), resource.getString("questionReply.delQ"), "javascript:onClick=DeleteQ('"+id+"');");
   	}
   	
	out.println(window.printBefore());    

	boolean updateQ = true;
	// le demandeur ne peut pas modifier les questions qui ne sont pas a lui ou qui n'ont pas de reponses
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
%>
<view:frame>
<view:board>
<!-- affichage de la question -->
<table width="100%">
	<tr>
		<td nowrap>
			<img src="<%=resource.getIcon("questionReply.miniconeQuestion")%>"/>
		</td>
		<td width="100%" class="titreQuestionReponse">
			<%=EncodeHelper.javaStringToHtmlParagraphe(title)%>
		</td>
		<td nowrap>
		<a href="#"><img border=0 src="<%=resource.getIcon("questionReply.update")%>"/></a>
		</td>
	</tr>
	<tr>
		<td colspan="3"><%=EncodeHelper.javaStringToHtmlParagraphe(content)%></td>
	</tr>
	<tr>
		<td colspan="3">			
			<span class="txtBaseline">
			<%=resource.getString("questionReply.questionOf")%> <%=creator%> <%=resource.getString("questionReply.replyBy")%> <%=date%>
			</span>
		</td>
	</tr>
</table>
</view:board>
<view:areaOfOperationOfCreation/>
<% 
// affichage des reponses
	while(it.hasNext()) {
		Reply reply = (Reply) it.next();
		String titleR = EncodeHelper.javaStringToHtmlString(reply.getTitle());
		String contentR = EncodeHelper.javaStringToHtmlString(reply.getContent());
		String creatorR = EncodeHelper.javaStringToHtmlString(reply.readCreatorName());
		String dateR = resource.getOutputDate(reply.getCreationDate());
		String idR = reply.getPK().getId();
		int statusR = reply.getPublicReply();
		out.println("<br/>");
		%>
		<view:board>
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
					<%=EncodeHelper.javaStringToHtmlParagraphe(titleR)%>
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
				<td colspan="3"><%=EncodeHelper.javaStringToHtmlParagraphe(contentR)%></td>
			</tr>
			<tr>
				<td colspan="3">			
					<span class="txtBaseline">
					<%=resource.getString("questionReply.replyOf")%> <%=creatorR%> <%=resource.getString("questionReply.replyBy")%> <%=dateR%>
					</span>
				</td>
			</tr>
		</table>
		</view:board>
		<%		
	}
%>
</view:frame>
<%
out.println(window.printAfter());
%>
<form name="RForm" action="" method="post">
	<input type="hidden" name="replyId"/>
</form>
</body>
</html>