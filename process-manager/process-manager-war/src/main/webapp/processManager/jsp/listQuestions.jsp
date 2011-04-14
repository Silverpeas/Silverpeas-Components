<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkProcessManager.jsp" %>

<%
	ProcessInstance 			process					= (ProcessInstance) request.getAttribute("process");
    com.silverpeas.form.Form 	form 					= (com.silverpeas.form.Form) request.getAttribute("form");
    PagesContext 				context 				= (PagesContext) request.getAttribute("context");
	Task[] 						tasks 					= (Task[]) request.getAttribute("tasks");
	Boolean 					isActiveUser 			= (Boolean) request.getAttribute("isActiveUser");
	Boolean 					isAttachmentTabEnable 	= (Boolean) request.getAttribute("isAttachmentTabEnable");
	Boolean 					isHistoryTabEnable 		= (Boolean) request.getAttribute("isHistoryTabEnable");
	boolean 					isProcessIdVisible 		= ((Boolean) request.getAttribute("isProcessIdVisible")).booleanValue();

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"listProcess");
	String processId = "";
	if (isProcessIdVisible)
		processId = "#"+process.getInstanceId()+" > ";
	browseBar.setPath(processId+process.getTitle(currentRole, language));

	tabbedPane.addTab(resource.getString("processManager.details"), "viewProcess?processId=" + process.getInstanceId()+"&force=true", false, true);
	if (isAttachmentTabEnable.booleanValue() && isActiveUser != null && isActiveUser.booleanValue())
		tabbedPane.addTab(resource.getString("processManager.attachments"), "attachmentManager?processId=" + process.getInstanceId(), false, true);
	tabbedPane.addTab(resource.getString("processManager.actions"), "listTasks", false, true);
	tabbedPane.addTab(resource.getString("processManager.questions"), "" , true, false);
	if (isHistoryTabEnable.booleanValue())
		tabbedPane.addTab(resource.getString("processManager.history"), "viewHistory?processId=" + process.getInstanceId(), false, true);
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
	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
%>
<CENTER>
<%
   boolean noQuestion = true;
	State state = null;
	Question[] questions = null;
   for (int i=0; tasks!=null && i<tasks.length; i++)
   {
	   state = tasks[i].getState();
	   questions = tasks[i].getPendingQuestions();

	   for (int j=0; questions!=null && j<questions.length ; j++)
	   {
			noQuestion = false;
			%>
<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr>
		<td CLASS=intfdcolor4 NOWRAP>
			<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="100%" CLASS=intfdcolor>
				<tr>
					<td CLASS=intfdcolor4 NOWRAP>
						<table CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="100%">
							<tr>
								<td class="intfdcolor" nowrap width="100%">
									<img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5"><span class="txtNav"><%=resource.getString("processManager.questionsToAnswer")%> <%= questions[j].getFromUser().getFullName() %></span>
								</td>
							</tr>
						</table>
					</td>
				</tr>	
			</table>

         <table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="100%" CLASS=intfdcolor4><tr><td colspan="2">&nbsp;</td></tr><tr>
			   <td width="80%">
			      <img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5">
					<span class="textePetitBold">
			         <%= questions[j].getQuestionText() %>
			      </span>
				</td>
				<td>
					<%
						ButtonPane buttonPane = gef.getButtonPane();
						buttonPane.addButton((Button) gef.getFormButton(
							resource.getString("processManager.answer"),
							"editResponse?questionId="+questions[j].getId(),
							false));
						out.println(buttonPane.print());
					%>
				</td>
			</tr><tr><td colspan="2">&nbsp;</td></tr></table>
		</td>
	</tr>	
</table>
		<% }

	   questions = tasks[i].getSentQuestions();
	   for (int j=0; questions!=null && j<questions.length ; j++)
	   {
			noQuestion = false;
			%>
<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr>
		<td CLASS=intfdcolor4 NOWRAP>
			<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="100%" CLASS=intfdcolor>
				<tr>
					<td CLASS=intfdcolor4 NOWRAP>
						<table CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="100%">
							<tr>
								<td class="intfdcolor" nowrap width="100%">
									<img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5"><span class="txtNav"><%=resource.getString("processManager.pendingQuestions")%> <%= questions[j].getToUser().getFullName() %></span>
								</td>
							</tr>
						</table>
					</td>
				</tr>	
			</table>

         <table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="100%" CLASS=intfdcolor4><tr><td colspan="2">&nbsp;</td></tr><tr>
			   <td width="80%">
			      <img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5">
			      <span class="textePetitBold">
			         <%= questions[j].getQuestionText() %>
			      </span>
				</td>
				<td>
				   &nbsp;
				</td>
			</tr><tr><td colspan="2">&nbsp;</td></tr></table>
		</td>
	</tr>	
</table>
		<%
		}

	   questions = tasks[i].getRelevantQuestions();
	   for (int j=0; questions!=null && j<questions.length ; j++)
	   {
			noQuestion = false;
			%>
<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr>
		<td CLASS=intfdcolor4 NOWRAP>
			<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="100%" CLASS=intfdcolor>
				<tr>
					<td CLASS=intfdcolor4 NOWRAP>
						<table CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="100%">
							<tr>
								<td class="intfdcolor" nowrap width="100%">
									<img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5"><span class="txtNav"><%=resource.getString("processManager.answeredQuestions")%> <%= questions[j].getToUser().getFullName() %></span>
								</td>
							</tr>
						</table>
					</td>
				</tr>	
			</table>

         <table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="100%" CLASS=intfdcolor4><tr><td colspan="2">&nbsp;</td></tr><tr>
			   <td width="50%">
			      <img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5">
			      <span class="textePetit">
			         <%= questions[j].getQuestionText() %>
			      </span>
				</td>
				<td>
			      <span class="textePetitBold">
			         <%= questions[j].getResponseText() %>
			      </span>
				</td>
			</tr><tr><td colspan="2">&nbsp;</td></tr></table>
		</td>
	</tr>	
</table>
		<%
		}
   }
%>
<% if (noQuestion)
   { %>
<% out.println(board.printBefore()); %>
         <table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="100%">
			<tr><td>&nbsp;</td></tr>
			<tr>
			   <td width="80%">
			      <img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5">
				  <span class="textePetitBold"><%=resource.getString("processManager.noQuestion")%></span>
				</td>
			</tr>
			<tr><td>&nbsp;</td></tr>
		</table>
<% out.println(board.printAfter()); %>
<% } %>
</CENTER>
<%
   out.println(frame.printAfter());
   out.println(window.printAfter());
%>
</BODY>
