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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="modelUtils.jsp" %>
<%@ include file="attachmentUtils.jsp" %>
<%@ include file="topicReport.jsp.inc" %>
<%@ include file="tabManager.jsp.inc" %>

<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="com.silverpeas.form.*"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<%!
 //Icons
String pubValidateSrc;
String pubUnvalidateSrc;
String alertSrc;
String deletePubliSrc;
String pdfSrc;
String inDraftSrc;
String outDraftSrc;
String validateSrc;
String refusedSrc;

%>

<%
  	String creatorName	= "";
  	String creationDate	= "";
  	String updateDate	= "";
  	String updaterName	= "";
  	String status		= "";
  	String author 		= "";
	
  	ResourceLocator uploadSettings 		= new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", resources.getLanguage());
  	ResourceLocator publicationSettings = new ResourceLocator("com.stratelia.webactiv.util.publication.publicationSettings", resources.getLanguage());  	
  	
	//Recuperation des parametres
	String 					profile 		= (String) request.getAttribute("Profile");
	String					alias			= (String) request.getAttribute("IsAlias");
	String 					action 			= (String) request.getAttribute("Action");
	UserCompletePublication userPubComplete = (UserCompletePublication) request.getAttribute("Publication");
	String 					wizard			= (String) request.getAttribute("Wizard");
	Integer					rang			= (Integer) request.getAttribute("Rang");
	Integer					nbPublis		= (Integer) request.getAttribute("NbPublis");
	String					language		= (String) request.getAttribute("Language");
	List					languages		= (List) request.getAttribute("Languages");
	String					contentLanguage	= (String) request.getAttribute("ContentLanguage");
	String					singleFileURL	= (String) request.getAttribute("SingleAttachmentURL");
	ValidationStep			validation		= (ValidationStep) request.getAttribute("ValidationStep");
	int						validationType	= ((Integer) request.getAttribute("ValidationType")).intValue();
	boolean isWriterApproval = ((Boolean) request.getAttribute("WriterApproval")).booleanValue();
	boolean notificationAllowed = ((Boolean) request.getAttribute("NotificationAllowed")).booleanValue();

	if (action == null)
		action = "View";
	
	SilverTrace.info("kmelia","JSPdesign", "root.MSG_GEN_PARAM_VALUE","ACTION pubManager = "+action);

	CompletePublication 		pubComplete 	= userPubComplete.getPublication();
	PublicationDetail 			pubDetail 		= pubComplete.getPublicationDetail();
	UserDetail 					ownerDetail 	= userPubComplete.getOwner();
	String						pubName			= pubDetail.getName(language);
	String 						id 				= pubDetail.getPK().getId();

	String contextComponentId = componentId;
	//surcharge le componentId du composant courant (cas de l'alias)
	componentId = pubDetail.getPK().getInstanceId();
	
	
	TopicDetail currentTopic 		= null;
	String 		linkedPathString 	= kmeliaScc.getSessionPath();
	String 		pathString 			= "";
	
	boolean 	debut				= rang.intValue() == 0;
	boolean 	fin					= rang.intValue() == nbPublis.intValue()-1;
	
	boolean 	suppressionAllowed	= false;

	Board boardHelp = gef.getBoard();
	
	//Icons
	pubValidateSrc			= m_context + "/util/icons/publicationValidate.gif";
	pubUnvalidateSrc		= m_context + "/util/icons/publicationUnvalidate.gif";
	alertSrc				= m_context + "/util/icons/alert.gif";
	deletePubliSrc			= m_context + "/util/icons/publicationDelete.gif";
	pdfSrc              	= m_context + "/util/icons/publication_to_pdf.gif";
	inDraftSrc				= m_context + "/util/icons/masque.gif";
	outDraftSrc				= m_context + "/util/icons/visible.gif";
	validateSrc				= m_context + "/util/icons/ok.gif";
	refusedSrc				= m_context + "/util/icons/wrong.gif";
	String pubDraftInSrc	= m_context + "/util/icons/publicationDraftIn.gif";
	String pubDraftOutSrc	= m_context + "/util/icons/publicationDraftOut.gif";
	String exportSrc		= m_context + "/util/icons/exportComponent.gif";

	String screenMessage = "";
	String user_id = kmeliaScc.getUserId();

	//Vrai si le user connecte est le createur de cette publication ou si il est admin
	boolean isOwner = false;
	
	if (action.equals("ValidationComplete") || action.equals("ValidationInProgress") || action.equals("Unvalidate") || action.equals("Suspend")) {
		Board boardStatus = gef.getBoard();
		screenMessage += boardStatus.printBefore();
		screenMessage += "<TABLE ALIGN=\"CENTER\" WIDTH=\"100%\"><tr>";
		screenMessage += "<td align=\"center\">";
		if (action.equals("ValidationComplete"))
			screenMessage += resources.getString("PubValidate");
		else if (action.equals("ValidationInProgress"))
			screenMessage += resources.getString("kmelia.PublicationValidationInProgress");
		else if (action.equals("Unvalidate"))
			screenMessage += resources.getString("PubUnvalidate");
		else if (action.equals("Suspend"))
			screenMessage += resources.getString("kmelia.PublicationSuspended");
		screenMessage += ("</td></tr></TABLE>");
		screenMessage += boardStatus.printAfter();
	    action = "ViewPublication";
	}
	else if (action.equals("GeneratePdf")) {
		String link = (String) request.getAttribute("Link");
	    out.println("<BODY marginheight=\"5\" marginwidth=\"5\" leftmargin=\"5\" topmargin=\"5\" onLoad=\"compileResult('"+link+"')\">");
	    out.println("</BODY>");
	}
		
	if (action.equals("ValidateView")) {
    	kmeliaScc.setSessionOwner(true);
        action = "UpdateView";
        isOwner = true;
    } else {
        if (profile.equals("admin") || profile.equals("publisher") || profile.equals("supervisor") || (ownerDetail != null && kmeliaScc.getUserDetail().getId().equals(ownerDetail.getId()) && profile.equals("writer")))
        {
        	isOwner = true;
        	
        	if (!kmeliaScc.isSuppressionOnlyForAdmin() || (profile.equals("admin") && kmeliaScc.isSuppressionOnlyForAdmin()))
        	{
        		// suppressionAllowed = true car si c'est un r�dacteur, c'est le propri�taire de la publication
        		suppressionAllowed = true;
        	}
        }
		else if ( !profile.equals("user") && kmeliaScc.isCoWritingEnable() )
		{
			// si publication en co-r�daction, consid�rer qu'elle appartient aux co-r�dacteur au m�me titre qu'au propri�taire
			// mais suppressionAllowed = false pour que le co-r�dacteur ne puisse pas supprimer la publication
			isOwner = true;
			suppressionAllowed = false;
		}
		
        if (isOwner) {
            kmeliaScc.setSessionOwner(true);
        } else {
		    //modification pour acc�der � l'onglet voir aussi
            kmeliaScc.setSessionOwner(false);
        }
	}

    creationDate = resources.getOutputDate(pubDetail.getCreationDate());
  	
  	status	= pubDetail.getStatus();
  	author 	= pubDetail.getAuthor();
  	
  	String creatorId = pubDetail.getCreatorId();
	creatorName	= resources.getString("kmelia.UnknownUser");
	if (creatorId != null && creatorId.length() > 0)
	{
		UserDetail creator = kmeliaScc.getUserDetail(creatorId);
		if (creator != null)
			creatorName = creator.getDisplayedName();
	}
	
	String 	updaterId = pubDetail.getUpdaterId();
	updaterName = resources.getString("kmelia.UnknownUser");
	if (updaterId != null && updaterId.length() > 0)
	{
		UserDetail updater = kmeliaScc.getUserDetail(updaterId);
		if (updater != null)
			updaterName = updater.getDisplayedName();
	}


	boolean highlightFirst 		= resources.getSetting("highlightFirstOccurence", false);	
%>

<%@page import="com.stratelia.silverpeas.versioning.model.DocumentPK"%><HTML>
<head>
<%
out.println(gef.getLookStyleSheet());
%>
<title></title>
<link type="text/css" rel="stylesheet" href="<%=m_context%>/kmelia/jsp/styleSheets/pubHighlight.css" >
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery-1.3.2.min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.qtip-1.0.0-rc3.min.js"></script>
<script type="text/javascript" src="<%=m_context%>/kmelia/jsp/javaScript/glossaryHighlight.js"></script>

<script language="javascript">

var refusalMotiveWindow = window;
var publicVersionsWindow = window;
var suspendMotiveWindow = window;
var attachmentWindow = window;

function clipboardCopy() {
  top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>copy.jsp?Id=<%=id%>';
}

function clipboardCut() {
  top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>cut.jsp?Id=<%=id%>';
}

function compileResult(fileName) {
    SP_openWindow(fileName, "PdfGeneration","770", "550", "toolbar=no, directories=no, menubar=no, locationbar=no ,resizable, scrollbars");
}

function generatePdf()
{
 document.toRouterForm.action = "<%=routerUrl%>GeneratePdf";
 document.toRouterForm.PubId.value = "<%=id%>";
 document.toRouterForm.submit();
}

function pubDeleteConfirm() {
	closeWindows();
    if(window.confirm("<%=resources.getString("ConfirmDeletePub")%>")){
		document.toRouterForm.action = "<%=routerUrl%>DeletePublication";
        document.toRouterForm.PubId.value = "<%=id%>";
        document.toRouterForm.submit();
    }
}

function pubValidate() {
	document.toRouterForm.action = "<%=routerUrl%>ValidatePublication";
	document.toRouterForm.submit();
}

function pubUnvalidate() {
	document.pubForm.PubId.value = "<%=id%>";
	url = "WantToRefusePubli?PubId="+<%=id%>;
    windowName = "refusalMotiveWindow";
	larg = "550";
	haut = "350";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!refusalMotiveWindow.closed && refusalMotiveWindow.name== "refusalMotiveWindow")
        refusalMotiveWindow.close();
    refusalMotiveWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function pubSuspend() {
	document.pubForm.PubId.value = "<%=id%>";
	url = "WantToSuspendPubli?PubId="+<%=id%>;
    windowName = "suspendMotiveWindow";
	larg = "550";
	haut = "350";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!suspendMotiveWindow.closed && suspendMotiveWindow.name== "suspendMotiveWindow")
        suspendMotiveWindow.close();
    suspendMotiveWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function pubDraftIn() {
	location.href = "<%=routerUrl%>DraftIn?From=ViewPublication";
}

function pubDraftOut() {
	if (<%=kmeliaScc.isDraftOutAllowed()%>)
	{
		location.href = "<%=routerUrl%>DraftOut?From=ViewPublication";
	}
	else
	{
		window.alert("<%=resources.getString("kmelia.PdcClassificationMandatory")%>");
	}
}

function topicGoTo(id) {
	closeWindows();
	location.href="GoToTopic?Id="+id;
}

function closeWindows() {
    if (window.publicationWindow != null)
        window.publicationWindow.close();
    if (window.publicVersionsWindow != null)
    	window.publicVersionsWindow.close();
}

function alertUsers()
{
	<% 
		if (!"Valid".equals(pubDetail.getStatus())) 
		{
			%>
				if (window.confirm("<%=Encode.javaStringToJsString(resources.getString("kmelia.AlertButPubNotValid"))%>"))
				{
					goToOperationInAnotherWindow('ToAlertUser', '<%=id%>', 'ViewAlert');
				}
			<%
		} else {
			%>
				goToOperationInAnotherWindow('ToAlertUser', '<%=id%>', 'ViewAlert');
			<%
		}
	%>
}

function openSingleAttachment() {
<%
	if (StringUtil.isDefined(singleFileURL))
	{
		out.print("url = \""+Encode.javaStringToJsString(singleFileURL)+"\";");
%>
		windowName = "attachmentWindow";
		windowParams = "directories=1,menubar=1,toolbar=1,location=1,resizable=1,scrollbars=1,status=1,alwaysRaised";
		if (!attachmentWindow.closed && attachmentWindow.name== "attachmentWindow")
    		attachmentWindow.close();
    	
    	attachmentWindow = SP_openWindow(url, windowName, "600", "400", windowParams);
<%
	}
%>
}

function showTranslation(lang)
{
	location.href="ViewPublication?SwitchLanguage="+lang;
}

function zipPublication()
{
	SP_openWindow("ZipPublication", "ZipPublication", "500", "300", "toolbar=no, directories=no, menubar=no, locationbar=no ,resizable, scrollbars");
}

</script>
</head>
<BODY class="yui-skin-sam" onUnload="closeWindows()" onLoad="openSingleAttachment()">
<% 
        Window window = gef.getWindow();
        Frame frame = gef.getFrame();

        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(spaceLabel);
        if (kmaxMode)
        	browseBar.setComponentName(componentLabel, "KmaxSearchResult");
        else
        	browseBar.setComponentName(componentLabel, "Main");
        browseBar.setPath(linkedPathString);
		browseBar.setExtraInformation(pubName);
		browseBar.setI18N(languages, contentLanguage);

        OperationPane operationPane = window.getOperationPane();

        if (notificationAllowed)
        {
        	operationPane.addOperation(alertSrc, resources.getString("GML.notify"), "javaScript:alertUsers()");
        }
        operationPane.addOperation(exportSrc, resources.getString("kmelia.DownloadPublication"), "javaScript:zipPublication()");
        if (!toolboxMode)
   		{
   			operationPane.addOperation(pdfSrc, resources.getString("GML.generatePDF"), "javascript:generatePdf()");
   		}
		operationPane.addLine();
		if (isOwner) {
			if (!"supervisor".equals(profile))
			{
				if (suppressionAllowed)
				{
                	operationPane.addOperation(deletePubliSrc, resources.getString("GML.delete"), "javaScript:pubDeleteConfirm()");
                }
				operationPane.addOperation("#", resources.getString("kmelia.AddFile"), "javaScript:AddAttachment()");
				
				if (kmeliaScc.isDraftEnabled() && !pubDetail.haveGotClone())
				{
					if ("Draft".equals(pubDetail.getStatus()))
						operationPane.addOperation(pubDraftOutSrc, resources.getString("PubDraftOut"), "javaScript:pubDraftOut()");
					else
						operationPane.addOperation(pubDraftInSrc, resources.getString("PubDraftIn"), "javaScript:pubDraftIn()");
				}
				operationPane.addLine();
            }
		}
		
		if (!kmaxMode)
		{
        	operationPane.addOperation(resources.getIcon("kmelia.copy"), resources.getString("GML.copy"), "javaScript:clipboardCopy()");
        	if (isOwner)
        		operationPane.addOperation(resources.getIcon("kmelia.cut"), resources.getString("GML.cut"), "javaScript:clipboardCut()");
		}
        if (!toolboxMode && isOwner) {
            if (profile.equals("admin") || profile.equals("publisher") || isWriterApproval) {
							/*if ("Valid".equals(pubDetail.getStatus())) {
								operationPane.addLine();
								operationPane.addOperation(pubUnvalidateSrc, resources.getString("PubUnvalidate?"), "javaScript:pubUnvalidate()");
							} else*/ if ("ToValidate".equals(pubDetail.getStatus())) {
								if (validation == null)
								{
									operationPane.addLine();
									operationPane.addOperation(pubValidateSrc, resources.getString("PubValidate?"), "javaScript:pubValidate()");
									operationPane.addOperation(pubUnvalidateSrc, resources.getString("PubUnvalidate?"), "javaScript:pubUnvalidate()");
								}
							}
            }
            if (profile.equals("supervisor"))
            {
            	operationPane.addLine();
							operationPane.addOperation(pubUnvalidateSrc, resources.getString("kmelia.PubSuspend"), "javaScript:pubSuspend()");
            }
        }
        out.println(window.printBefore());
        action = "View";
        if (isOwner)
            displayAllOperations(id, kmeliaScc, gef, action, resources, out, kmaxMode);
        else
            displayUserOperations(id, kmeliaScc, gef, action, resources, out, kmaxMode);
        out.println(frame.printBefore());

        if ("finish".equals(wizard))
    	{
    		//  cadre d'aide
    	    out.println(boardHelp.printBefore());
    		out.println("<table border=\"0\"><tr>");
    		out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resources.getIcon("kmelia.info")+"\"></td>");
    		out.println("<td>"+kmeliaScc.getString("kmelia.HelpPubli")+"</td>");
    		out.println("</tr></table>");
    	    out.println(boardHelp.printAfter());
    	    out.println("<BR>");
    	}
        
        if (screenMessage != null && screenMessage.length()>0)
	    	out.println("<center>"+screenMessage+"</center>");
        
        InfoDetail 			infos 	= pubComplete.getInfoDetail();
    	ModelDetail 		model 	= pubComplete.getModelDetail();
	
	    int type 	= 0;
	    if (kmeliaScc.isVersionControlled())
	        type = 1; // Versioning
		
    /*********************************************************************************************************************/
		/** Affichage du header de la publication																			**/
		/*********************************************************************************************************************/
    	out.println("<TABLE border=\"0\" width=\"98%\" align=center>");
    	out.println("<TR><TD align=\"left\">");

    	out.print("<span class=\"txtnav\"><b>");
    	out.print(EncodeHelper.javaStringToHtmlString(pubDetail.getName(language)));
    	out.println("</b></span>");
    	
		if (!"user".equals(profile))
		{
			if ("ToValidate".equals(status))
				out.println("<img src=\""+outDraftSrc+"\" alt=\""+resources.getString("PubStateToValidate")+"\" align=\"absmiddle\">");
			else if ("Draft".equals(status))
				out.println("<img src=\""+inDraftSrc+"\" alt=\""+resources.getString("PubStateDraft")+"\" align=\"absmiddle\">");
			else if ("Valid".equals(status))
				out.println("<img src=\""+validateSrc+"\" alt=\""+resources.getString("PublicationValidated")+"\" align=\"absmiddle\">");
			else if ("UnValidate".equals(status))
				out.println("<img src=\""+refusedSrc+"\" alt=\""+resources.getString("PublicationRefused")+"\" align=\"absmiddle\">");
		}
    	
		out.println("<br><b>"+EncodeHelper.javaStringToHtmlParagraphe(pubDetail.getDescription(language))+"<b><BR><BR>");

		out.println("</td><td valign=top align=\"right\">");
		
		/*********************************************************************************************************************/
		/** Affichage des boutons de navigation (next / previous)															**/
		/*********************************************************************************************************************/
		if (nbPublis.intValue() > 1) {
	    %>
			<!-- AFFICHAGE des boutons de navigation -->
			<table border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td align="center" width="15">
						<%	if ( !debut ) { %>
							<a href="PreviousPublication"><img src="<%=resources.getIcon("kmelia.previous")%>" align="middle" border=0 alt="<%=resources.getString("kmelia.previous")%>" title="<%=resources.getString("kmelia.previous")%>"></a>
						<% } else { %>
							&nbsp;
						<% } %>
					</td>
					<td nowrap class="txtnav">
						<center><%=rang.intValue()+1%> / <%=nbPublis.intValue()%></center>
				    </td>
					<td align="center" width="15">
						<% if ( !fin ) { %>
							<a href="NextPublication"><img src="<%=resources.getIcon("kmelia.next")%>" align="middle" border=0 alt="<%=resources.getString("kmelia.next")%>" title="<%=resources.getString("kmelia.next")%>"></a>
						<% } else { %>
							&nbsp;
						<% } %>
					</td>
				</tr>
			</table>
		<%
		}
		
		out.println("</TD></TR></table>");
		
		/*********************************************************************************************************************/
		/** Affichage du contenu de la publication																			**/
		/*********************************************************************************************************************/
		out.println("<TABLE border=\"0\" width=\"98%\" align=\"center\">");
		out.println("<TR><TD valign=\"top\" width=\"100%\" id=\"richContent\">");
    	if (WysiwygController.haveGotWysiwyg(spaceId, componentId, id)) {
        	out.flush();
        	getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+id+"&SpaceId="+spaceId+"&ComponentId="+componentId+"&Language="+language+"&axisId="+kmeliaScc.getAxisIdGlossary()+"&onlyFirst="+highlightFirst).include(request, response);
    	} else if (infos != null && model != null) {
       	    displayViewInfoModel(out, model, infos, resources, publicationSettings, m_context);
    	} else {
	    	Form			xmlForm 	= (Form) request.getAttribute("XMLForm");
			DataRecord		xmlData		= (DataRecord) request.getAttribute("XMLData");
			if (xmlForm != null)
			{
				PagesContext xmlContext = new PagesContext("myForm", "0", resources.getLanguage(), false, componentId, kmeliaScc.getUserId());
				xmlContext.setObjectId(id);
				if (kmeliaMode)
					xmlContext.setNodeId(kmeliaScc.getSessionTopic().getNodeDetail().getNodePK().getId());
				xmlContext.setBorderPrinted(false);
				xmlContext.setContentLanguage(language);
				%>
				<view:highlight axisId="<%=kmeliaScc.getAxisIdGlossary()%>" className="highlight-silver" language="<%=language%>" onlyFirst="<%=highlightFirst%>">
				<%
		    	xmlForm.display(out, xmlContext, xmlData);
				%>
				</view:highlight>
				<%
		    }
    	}
    	out.println("</TD>");
    	
    	/*********************************************************************************************************************/
		/** Affichage des fichiers joints																					**/
		/*********************************************************************************************************************/
   
		boolean showTitle 				= true;
		boolean showFileSize 			= true;
		boolean showDownloadEstimation 	= true;
		boolean showInfo 				= true;
		if ("no".equals(resources.getSetting("showTitle")))
			showTitle = false;	        
		if ("no".equals(resources.getSetting("showFileSize")))
			showFileSize = false;
		if ("no".equals(resources.getSetting("showDownloadEstimation")))
			showDownloadEstimation = false;	        
		if ("no".equals(resources.getSetting("showInfo")))
			showInfo = false;
		boolean showIcon = true;
		if (infos != null) {
		    if (!"bottom".equals(resources.getSetting("attachmentPosition"))) {
				out.println("<TD valign=\"top\" align=\"center\">");
				out.println("<A NAME=attachments></a>");
		   	}
		   	else {
				out.println("</TR><TR>");
				out.println("<TD valign=\"top\" align=\"left\">");
				out.println("<A NAME=attachments></a>");
		    }
			try
			{
				out.flush();
				boolean	indexIt 	= kmeliaScc.isIndexable(pubDetail);
				String	pIndexIt	= "0";
				if (indexIt)
					pIndexIt = "1";
				if (kmeliaScc.isVersionControlled(componentId))
					getServletConfig().getServletContext().getRequestDispatcher("/versioningPeas/jsp/displayDocuments.jsp?Id="+id+"&ComponentId="+componentId+"&Alias="+alias+"&Context=Images&AttachmentPosition="+resources.getSetting("attachmentPosition")+"&ShowIcon="+showIcon+"&ShowTitle="+showTitle+"&ShowFileSize="+showFileSize+"&ShowDownloadEstimation="+showDownloadEstimation+"&ShowInfo="+showInfo+"&UpdateOfficeMode="+kmeliaScc.getUpdateOfficeMode()+"&Profile="+kmeliaScc.getProfile()+"&NodeId="+kmeliaScc.getSessionTopic().getNodePK().getId()+"&TopicRightsEnabled="+kmeliaScc.isRightsOnTopicsEnabled()+"&VersionningFileRightsMode="+kmeliaScc.getVersionningFileRightsMode()+"&CallbackUrl="+URLManager.getURL("useless",componentId)+"ViewPublication&IndexIt="+pIndexIt).include(request, response);
				else
					getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/displayAttachments.jsp?Id="+id+"&ComponentId="+componentId+"&Alias="+alias+"&Context=Images&AttachmentPosition="+resources.getSetting("attachmentPosition")+"&ShowIcon="+showIcon+"&ShowTitle="+showTitle+"&ShowFileSize="+showFileSize+"&ShowDownloadEstimation="+showDownloadEstimation+"&ShowInfo="+showInfo+"&UpdateOfficeMode="+kmeliaScc.getUpdateOfficeMode()+"&Language="+language+"&Profile="+kmeliaScc.getProfile()+"&CallbackUrl="+URLManager.getURL("useless",componentId)+"ViewPublication&IndexIt="+pIndexIt).include(request, response);
			}
			catch (Exception e)
			{
				throw new KmeliaException("JSPpublicationManager.displayUserModelAndAttachmentsView()",SilverpeasException.ERROR,"root.EX_DISPLAY_ATTACHMENTS_FAILED", e);
			}
			out.println("</TD>");
		    out.println("</TR>");
		}
    	out.println("</TABLE>");
    	
    	out.println("<CENTER>");
    	out.print("<span class=\"txtBaseline\">");
    	if (kmeliaScc.isAuthorUsed() && pubDetail.getAuthor() != null && !pubDetail.getAuthor().equals(""))
		{
			out.print("<BR>");
			out.print(resources.getString("GML.author")+" : "+pubDetail.getAuthor());
		}
    	out.print("<BR>");
    	    	
		out.print(creatorName+" - "+resources.getOutputDate(pubDetail.getCreationDate()));
		if (updaterId != null)
		{
			out.print(" | ");
			out.print(resources.getString("kmelia.LastModification")+" : "+updaterName+" - "+resources.getOutputDate(pubDetail.getUpdateDate()));
		}
		
		out.print(" | ");
		out.print(resources.getString("kmelia.consulted")+" "+pubDetail.getNbAccess()+" "+resources.getString("kmelia.time"));
		
		if (URLManager.displayUniversalLinks())
    	{
    		String link = null;
			if (!pubDetail.getPK().getInstanceId().equals(contextComponentId))
				link = URLManager.getSimpleURL(URLManager.URL_PUBLI, pubDetail.getPK().getId(), contextComponentId);
			else
				link = URLManager.getSimpleURL(URLManager.URL_PUBLI, pubDetail.getPK().getId());
			out.print(" | <a href=\""+link+"\"><img src=\""+resources.getIcon("kmelia.link")+"\" border=\"0\" align=\"absmiddle\" alt=\""+Encode.convertHTMLEntities(resources.getString("kmelia.CopyPublicationLink"))+"\" title=\""+Encode.convertHTMLEntities(resources.getString("kmelia.CopyPublicationLink"))+"\"/></a>");
    	}
		
		out.print("</span>");
		out.println("</CENTER>");
		
		out.flush();

        out.println(frame.printAfter());
        out.println(window.printAfter());
%>
<FORM NAME="pubForm" ACTION="<%=routerUrl%>publication.jsp" METHOD="POST">
	<input type="hidden" name="Action">
	<input type="hidden" name="PubId">
	<input type="hidden" name="Profile" value="<%=profile%>">
</FORM>
<FORM NAME="refusalForm" action="<%=routerUrl%>Unvalidate">
  	<input type="hidden" name="PubId" value="<%=id%>">
  	<input type="hidden" name="Motive" value="">
</FORM>
<FORM NAME="defermentForm" ACTION="<%=routerUrl%>SuspendPublication" METHOD="POST">
  	<input type="hidden" name="PubId" value="<%=id%>">
  	<input type="hidden" name="Motive" value="">
</FORM>
<FORM name="toRouterForm">
	<input type="hidden" name="PubId" value="<%=id%>">
</FORM>
</BODY>
</HTML>