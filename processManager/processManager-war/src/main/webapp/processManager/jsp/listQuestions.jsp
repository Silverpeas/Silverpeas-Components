<%@ page import="org.silverpeas.core.contribution.content.form.Form" %><%--

    Copyright (C) 2000 - 2021 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkProcessManager.jsp" %>

<%
	ProcessInstance 			process					= (ProcessInstance) request.getAttribute("process");
    Form form 					= (Form) request.getAttribute("form");
    PagesContext 				context 				= (PagesContext) request.getAttribute("context");
	Task[] 						tasks 					= (Task[]) request.getAttribute("tasks");
	Boolean 					isAttachmentTabEnabled 	= (Boolean) request.getAttribute("isAttachmentTabEnabled");
	Boolean 					isHistoryTabEnable 		= (Boolean) request.getAttribute("isHistoryTabEnable");
	boolean 					isProcessIdVisible 		= (Boolean) request.getAttribute("isProcessIdVisible");
  int nbEntriesAboutQuestions = (Integer) request.getAttribute("NbEntriesAboutQuestions");

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"listProcess");
	String processId = "";
	if (isProcessIdVisible)
		processId = "#"+process.getInstanceId()+" > ";
	browseBar.setPath(processId+process.getTitle(currentRole, language));

	tabbedPane.addTab(resource.getString("processManager.details"), "viewProcess?processId=" + process.getInstanceId()+"&force=true", false, true);
	if (isAttachmentTabEnabled)
		tabbedPane.addTab(resource.getString("processManager.attachments"), "attachmentManager?processId=" + process.getInstanceId(), false, true);
	tabbedPane.addTab(resource.getString("processManager.questions")+"("+nbEntriesAboutQuestions+")", "#" , true, true);
	if (isHistoryTabEnable)
		tabbedPane.addTab(resource.getString("processManager.history"), "viewHistory?processId=" + process.getInstanceId(), false, true);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
</head>
<body class="currentProfile_<%=currentRole%> page_questions">
<%
	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());

   boolean noQuestion = true;
	Question[] questions = null;
   for (int i=0; tasks!=null && i<tasks.length; i++)
   {
	   questions = tasks[i].getPendingQuestions();

	   for (int j=0; questions!=null && j<questions.length ; j++)
	   {
			noQuestion = false;
			%>

			
		<p class="txtnav"><%=resource.getString("processManager.questionsToAnswer")%> <%= questions[j].getFromUser().getFullName() %></p>

         <table cellpadding="0" cellspacing="2" width="100%" class="intfdcolor4">
			<tr>
			   <td width="80%">
					<span class="textePetitBold">
			         <%= questions[j].getQuestionText() %>
			      </span>
				</td>
				<td>
					<%
						ButtonPane buttonPane = gef.getButtonPane();
						buttonPane.addButton(gef.getFormButton(
							resource.getString("processManager.answer"),
							"editResponse?questionId="+questions[j].getId(),
							false));
						out.println(buttonPane.print());
					%>
				</td>
			</tr>
		</table>

		<% }

	   questions = tasks[i].getSentQuestions();
	   for (int j=0; questions!=null && j<questions.length ; j++)
	   {
			noQuestion = false;
			%>

			<p class="txtnav"><%=resource.getString("processManager.pendingQuestions")%> <%= questions[j].getToUser().getFullName() %></p>

			<p class="textePetitBold">
			         <%= questions[j].getQuestionText() %>
			</p>


		<%
		}

	   questions = tasks[i].getRelevantQuestions();
	   for (int j=0; questions!=null && j<questions.length ; j++)
	   {
			noQuestion = false;
			%>

			<p class="txtnav"><%=resource.getString("processManager.answeredQuestions")%> <%= questions[j].getToUser().getFullName() %></p>

			 <table cellpadding="0" cellspacing="2"  width="100%" class="intfdcolor4">
				<tr>
				   <td width="50%">
					  <div >
						 <%= questions[j].getQuestionText() %>
					  </div>
					</td>
					<td>
					  <div class="inlineMessage neutral">
						 <%= questions[j].getResponseText() %>
					  </div>
					</td>
				</tr>
			</table>

		<%
		}
   }
%>
<% if (noQuestion)
   { %>

	<div class="inlineMessage-ok"><%=resource.getString("processManager.noQuestion")%></div>


<% } %>

<%
   out.println(frame.printAfter());
   out.println(window.printAfter());
%>
</body>