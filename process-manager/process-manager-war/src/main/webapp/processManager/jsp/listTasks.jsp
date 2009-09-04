<%@ include file="checkProcessManager.jsp" %>

<%
   ProcessInstance 	process 				= (ProcessInstance) request.getAttribute("process");
   Task[] 			tasks 					= (Task[]) request.getAttribute("tasks");
   Boolean 			isActiveUser 			= (Boolean) request.getAttribute("isActiveUser");
   Boolean 			isAttachmentTabEnable 	= (Boolean) request.getAttribute("isAttachmentTabEnable");
   Boolean			isViewReturn			= (Boolean) request.getAttribute("ViewReturn");
   Boolean 			isInErrorState			= (Boolean) request.getAttribute("Error");
   Boolean 			isHistoryTabEnable 		= (Boolean) request.getAttribute("isHistoryTabEnable");
   boolean 			isProcessIdVisible 		= ((Boolean) request.getAttribute("isProcessIdVisible")).booleanValue();
   
   boolean viewReturn = isViewReturn != null && isViewReturn.booleanValue();

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"listProcess");
	String processId = "";
	if (isProcessIdVisible)
		processId = "#"+process.getInstanceId()+" > ";
	browseBar.setPath(processId+process.getTitle(currentRole, language));
	
	String toto = process.getInstanceId();
	
	tabbedPane.addTab(resource.getString("processManager.details"), "viewProcess?processId=" + process.getInstanceId()+"&force=true", false, true);
	if (isAttachmentTabEnable.booleanValue() && isActiveUser != null && isActiveUser.booleanValue())
		tabbedPane.addTab(resource.getString("processManager.attachments"), "attachmentManager?processId=" + process.getInstanceId(), false, true);
	tabbedPane.addTab(resource.getString("processManager.actions"), "", true, false);
	tabbedPane.addTab(resource.getString("processManager.questions"), "listQuestions?processId=" + process.getInstanceId() , false, true);
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
<BODY>
<%
   out.println(window.printBefore());
   out.println(tabbedPane.print());
   out.println(frame.printBefore());
%>
<CENTER>
<%
   boolean noTask = true;
	for (int i=0; tasks!=null && i<tasks.length; i++)
   {
     noTask = false;
  	  State state = tasks[i].getState();
%>
<% out.println(board.printBefore()); %>
			<table CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="100%">
				<tr>
					<td class="intfdcolor" nowrap width="100%">
						<img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5"><span class="txtNav"><%=resource.getString("processManager.activityStates") %> </span>
					</td>
				</tr>
			</table>
			<table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
				<tr><td><img src="<%=resource.getIcon("processManager.px") %>"></td></tr>
				  <tr>
				   <td>
						 <span class="textePetitBold">&#149;&nbsp;
								<%=state.getLabel(currentRole, language)%></span>
				   </td>
				  </tr>
				  <tr>
				   <td>
<%
		  ButtonPane buttonPane = gef.getButtonPane();
			Action[] actions = state.getFilteredActions();
	   		for (int j=0; actions!=null && j<actions.length ; j++)
			{
				buttonPane.addButton((Button) gef.getFormButton(actions[j].getLabel(currentRole,language), "editAction?state="+state.getName()+"&action="+actions[j].getName() , false));			
			}

	   	// affichage des boutons "retour" si autorisé
	   	if (viewReturn)
	   	{
	   		HistoryStep[] steps = tasks[i].getBackSteps();
	
		    if (steps!=null && steps.length>0)
		    {
		    	for (int j=0; j<steps.length ; j++)
				{
		    		String actorName = steps[j].getUser().getFullName();
	
				    buttonPane.addButton((Button) gef.getFormButton(resource.getString("processManager.backTo") + " "+ actorName, "editQuestion?state="+state.getName()+"&stepId="+steps[j].getId() , false));
				}
			} 
	   	}
	   				
		  out.println(buttonPane.print());
%>
				   </td>
				  </tr>
				<tr><td colspan=3><img src="<%=resource.getIcon("processManager.px") %>"></td></tr>
			</table>
<% out.println(board.printAfter()); %>

<%
	}

   if (noTask)
   { %>
<% out.println(board.printBefore()); %>
         <table CELLPADDING="0" CELLSPACING="2" BORDER="0" WIDTH="100%" CLASS="intfdcolor4"><tr><td>&nbsp;</td></tr><tr>
			   <td width="80%">
			      <img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5"/>
			      <span class="textePetitBold">
			      <% if (isInErrorState.booleanValue()) out.println(resource.getString("processManager.ERR_PROCESS_IN_ERROR")+"&nbsp;"); %>
			      <%=resource.getString("processManager.noTask")%>
			      </span>
				</td>
			</tr><tr><td>&nbsp;</td></tr></table>
<% out.println(board.printAfter()); %>
<% } %>

</CENTER>
<%
   out.println(frame.printAfter());
   out.println(window.printAfter());
%>
</BODY>
