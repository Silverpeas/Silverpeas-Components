<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ include file="check.jsp" %>
<%@page import="com.silverpeas.util.StringUtil"%>

<% 
	boolean				isDraftEnabled 	= ((Boolean) request.getAttribute("IsDraftEnabled")).booleanValue();
	ClassifiedDetail	classified		= (ClassifiedDetail) request.getAttribute("Classified");
	String 				profile			= (String) request.getAttribute("Profile");
	String 				userId			= (String) request.getAttribute("UserId");
	boolean				anonymousAccess	= ((Boolean) request.getAttribute("AnonymousAccess")).booleanValue();

	Collection 			comments		= (Collection) request.getAttribute("AllComments");
	
	// param�tres du formulaire
	Form		xmlForm 				= (Form) request.getAttribute("Form");
	DataRecord	xmlData					= (DataRecord) request.getAttribute("Data");
	
	// d�claration des variables :
	String 		classifiedId 			= Integer.toString(classified.getClassifiedId());
	String 		title 					= classified.getTitle();
	String 		instanceId				= classified.getInstanceId();
	String 		creatorId				= classified.getCreatorId();
	String 		creatorName				= classified.getCreatorName();
	String		creatorEmail			= classified.getCreatorEmail();
	String 		creationDate			= null;
	if (classified.getCreationDate() != null){
		creationDate = resource.getOutputDateAndHour(classified.getCreationDate());
	}
  	else {
  		creationDate = "";
  	}
	String 		updateDate		= null;
	if (classified.getUpdateDate() != null){
		updateDate = resource.getOutputDateAndHour(classified.getUpdateDate());
	}
  	else {
  		updateDate = "";
  	}
	String 		status				= classified.getStatus();
	String 		validatorId			= classified.getValidatorId();
	String 		validatorName		= classified.getValidatorName();
	String 		validateDate		= null;
	if (classified.getValidateDate() != null) {
		validateDate = resource.getOutputDateAndHour(classified.getValidateDate());
	}
  	else {
  		validateDate = "";
  	}
	
	boolean isAutorized = userId.equals(creatorId) || profile.equals("admin");
	
	Button validateComment 	= (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
	Button cancelButton 	= (Button) gef.getFormButton(resource.getString("GML.cancel"), "Main", false);


	Board board	= gef.getBoard();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">

	var refusalMotiveWindow = window;
	
	function deleteConfirm(id) 
	{
		// confirmation de suppression de l'annonce
		if(window.confirm("<%=resource.getString("classifieds.confirmDeleteClassified")%>"))
		{
  			document.classifiedForm.action = "DeleteClassified";
  			document.classifiedForm.ClassifiedId.value = id;
  			document.classifiedForm.submit();
		}
	}

	function updateClassified(id) 
	{
		document.classifiedForm.action = "EditClassified";
		document.classifiedForm.ClassifiedId.value = id;
		document.classifiedForm.submit();
	}
	function sendData() 
	{
		//if (isCorrectForm()) 
		//{
			document.commentForm.action = "AddComment";
			document.commentForm.submit();
		//}
	}

	function isCorrectForm() 
	{
     	var errorMsg = "";
     	var errorNb = 0;
     	var message = stripInitialWhitespace(document.commentForm.Message.value);
		window.alert("message = " + message);
     	if (message == "") 
     	{ 
			errorMsg+="  - '<%=resource.getString("classifieds.comment")%>'  <%=resource.getString("GML.MustBeFilled")%>\n";
           	errorNb++;
     	}
   				     			     				    
     	switch(errorNb) 
     	{
        	case 0 :
            	result = true;
            	break;
        	case 1 :
            	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
            	window.alert(errorMsg);
            	result = false;
            	break;
        	default :
            	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            	window.alert(errorMsg);
            	result = false;
            	break;
     	} 
     	return result;
	}

	function updateComment(id, classifiedId)
	{
	    SP_openWindow("<%=m_context%>/comment/jsp/newComment.jsp?id="+id+"&IndexIt=1", "blank", "600", "250","scrollbars=no, resizable, alwaysRaised");
	    document.commentForm.action = "UpdateComment";
	   	document.commentForm.ClassifiedId.value = classifiedId;
		document.commentForm.submit();
	}
	
	function removeComment(id)
	{
	    if (window.confirm("<%=resource.getString("classifieds.confirmDeleteComment")%>"))
	    {
	    	document.commentForm.action = "DeleteComment";
	    	document.commentForm.CommentId.value = id;
			document.commentForm.submit();
	    }
	}

	function commentCallBack()
	{
		location.href="<%=m_context+URLManager.getURL("useless", instanceId)%>ViewClassified?ClassifiedId=<%=classifiedId%>";
	}
	
	function draftIn(id) {
		location.href = "<%=m_context+URLManager.getURL("useless", instanceId)%>DraftIn?ClassifiedId="+id;
	}

	function draftOut(id) {
		location.href = "<%=m_context+URLManager.getURL("useless", instanceId)%>DraftOut?ClassifiedId="+id;
	}
	
	function validate(id) {
		location.href = "<%=m_context+URLManager.getURL("useless", instanceId)%>ValidateClassified?ClassifiedId="+id;
	}

	function refused(id) {
		url = "WantToRefuseClassified?ClassifiedId="+id;
	    windowName = "refusalMotiveWindow";
		larg = "550";
		haut = "350";
	    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
	    if (!refusalMotiveWindow.closed && refusalMotiveWindow.name== "refusalMotiveWindow")
	        refusalMotiveWindow.close();
	    refusalMotiveWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
	}
	
	
</script>
</head>
<body id="classified-view">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(resource.getString("classifieds.classified"));
	
	// affichage des options
	if (isAutorized) {
		operationPane.addOperation(resource.getIcon("classifieds.update"),resource.getString("classifieds.updateClassified"), "javaScript:updateClassified('"+classifiedId+"')");
		operationPane.addOperation(resource.getIcon("classifieds.delete"),resource.getString("classifieds.deleteClassified"), "javaScript:deleteConfirm('"+classifiedId+"')");
		// op�rations du mode brouillon si option activ�e et si pas admin
		if (isDraftEnabled) {
			operationPane.addLine();
			if ((ClassifiedDetail.DRAFT).equals(classified.getStatus()))
				operationPane.addOperation(resource.getIcon("classifieds.draftOut"), resource.getString("classifieds.draftOut"), "javaScript:draftOut('"+classifiedId+"')");
			else
				operationPane.addOperation(resource.getIcon("classifieds.draftIn"), resource.getString("classifieds.draftIn"), "javaScript:draftIn('"+classifiedId+"')");
		}
		// op�rations de validation (ou refus)
		if ("admin".equals(profile) && (ClassifiedDetail.TO_VALIDATE).equals(classified.getStatus())) {
			operationPane.addLine();
			operationPane.addOperation(resource.getIcon("classifieds.validate"), resource.getString("classifieds.validate"), "javaScript:validate('"+classifiedId+"')");
			operationPane.addOperation(resource.getIcon("classifieds.refused"), resource.getString("classifieds.refused"), "javaScript:refused('"+classifiedId+"')");
		}
	}

	out.println(window.printBefore());
    out.println(frame.printBefore());
%>
<table cellpadding="5" width="100%">
<tr>
	<td>
		<div class="tableBoard" id="classified-view-header">
			<h1 class="titreFenetre" id="classified-title"><%=title%></h1>				
			<div id="classified-view-header-owner">
				<span class="txtlibform"><%=resource.getString("classifieds.annonceur")%> : </span>
				<span class="txtvalform"><%=creatorName%> (<%=Encode.javaStringToHtmlString(creatorEmail)%>) </span>
			</div>
			<div id="classified-view-header-parutionDate">
				<span class="txtlibform"><%=resource.getString("classifieds.parutionDate")%> : </span>
				<span class="txtvalform"><%=creationDate%> </span>
			</div>
			<% if (StringUtil.isDefined(updateDate)) { %>
				<div id="classified-view-header-updateDate">
					<span class="txtlibform"><%=resource.getString("classifieds.updateDate")%> : </span>
					<span class="txtvalform"><%=updateDate%></span>
				</div>
			<% } %>
			<% if (StringUtil.isDefined(validateDate) && StringUtil.isDefined(validatorName)) { %>
				<div id="classified-view-header-validateDate">
					<span class="txtlibform"><%=resource.getString("classifieds.validateDate")%> : </span>
					<span class="txtvalform"><%=validateDate%> &nbsp; <span><%=resource.getString("classifieds.by")%></span>&nbsp;<%=validatorName%></span>
				</div>
			<% } %>
		<hr class="clear" />
		</div>

	<% if (xmlForm != null) { %>
		<div class="tableBoard" id="classified-view-content">
		<!-- AFFICHAGE du formulaire -->
		<%
			PagesContext xmlContext = new PagesContext("myForm", "0", resource.getLanguage(), false, instanceId, null);
			xmlContext.setBorderPrinted(false);
			xmlContext.setIgnoreDefaultValues(true);
			
	    	xmlForm.display(out, xmlContext, xmlData);
	    %>
		<hr class="clear" />
		</div>
	<% } %>	
	</td>
</tr>
<tr>
	<td>
		<!--Afficher les commentaires-->
		<div class="commentaires">
		<% if (!anonymousAccess) { %>
			<form name="commentForm" action="AddComment" method="post">	
				<p class="txtlibform"><%=resource.getString("classifieds.addComment")%></p>
				<textarea rows="4" cols="100" name="Message"></textarea>
				<input type="hidden" name="ClassifiedId" value="<%=classifiedId%>"/>
				<input type="hidden" name="CommentId" value=""/>
			</form>

			<%
			ButtonPane buttonPaneComment = gef.getButtonPane();
			buttonPaneComment.addButton(validateComment);
			buttonPaneComment.addButton(cancelButton);
			out.println("<br/><center>"+buttonPaneComment.print()+"</center><br/>");
			%>
			<hr />
		<% } %>
			
		<% if (comments != null) {
				Iterator itCom = (Iterator) comments.iterator();
				while (itCom.hasNext()) {
					Comment unComment = (Comment) itCom.next();
					String commentDate = resource.getOutputDate(unComment.getCreationDate());
					String ownerId = Integer.toString(unComment.getOwnerId());
					%>
					<div class="oneComment">
						<div>
							<div class="avatar">
								<img src="<%=m_context%><%=unComment.getOwnerDetail().getAvatar() %>"/>
							</div>
							<p class="author">
								<%=unComment.getOwnerDetail().getDisplayedName()%>
								<span class="date"> - <%=resource.getString("classifieds.postOn")%> <%=commentDate%></span>
							</p>
							<% if ("admin".equals(profile) || ownerId.equals(userId) ) { %>
								<div class="action">
									<a href="javascript:updateComment(<%=unComment.getCommentPK().getId()%>,<%=classifiedId%>)"><img src="<%=resource.getIcon("classifieds.smallUpdate") %>" alt="<%=resource.getString("GML.update")%>" title="<%=resource.getString("GML.update")%>" align="absmiddle"/></a>
									<a href="javascript:removeComment(<%=unComment.getCommentPK().getId()%>)"><img src="<%=resource.getIcon("classifieds.smallDelete") %>" alt="<%=resource.getString("GML.delete")%>" title="<%=resource.getString("GML.delete")%>" align="absmiddle"/></a>
								</div>
							<% } %>
							<p class="message"><%=Encode.javaStringToHtmlParagraphe(unComment.getMessage())%></p>
						</div>
					</div>
					<%
				}
			}
			%>
		
		</div><!-- End commentaires-->

	</td>
</tr>
</table>
<% 
  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>	

<form name="classifiedForm" action="" method="post">
	<input type="hidden" name="ClassifiedId"/>
</form>
</body>
</html>