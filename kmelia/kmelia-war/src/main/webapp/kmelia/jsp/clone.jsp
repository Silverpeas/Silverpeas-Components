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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="modelUtils.jsp" %>
<%@ include file="tabManager.jsp.inc" %>

<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="com.silverpeas.form.*"%>

<%!
void displayViewWysiwyg(String id, String spaceId, String componentId, HttpServletRequest request, HttpServletResponse response) throws KmeliaException {
    try {
        getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+id+"&SpaceId="+spaceId+"&ComponentId="+componentId).include(request, response);
    } catch (Exception e) {
	  throw new KmeliaException("JSPpublicationManager.displayViewWysiwyg()",SilverpeasException.ERROR,"root.EX_DISPLAY_WYSIWYG_FAILED", e);
    }
}
%>

<%
  	ResourceLocator publicationSettings = new ResourceLocator("com.stratelia.webactiv.util.publication.publicationSettings", resources.getLanguage());

	//Recuperation des parametres
	String 					profile 		= (String) request.getAttribute("Profile");
	String 					action 			= (String) request.getAttribute("Action");
	String 					checkPath 		= (String) request.getAttribute("CheckPath");
	KmeliaPublication kmeliaPublication = (KmeliaPublication) request.getAttribute("Publication");
	String					visiblePubId	= (String) request.getAttribute("VisiblePublicationId");
	boolean 				attachmentsEnabled = ((Boolean) request.getAttribute("AttachmentsEnabled")).booleanValue();

	if (action == null) {
		action = "ViewClone";
	}

	CompletePublication 		pubComplete 	= kmeliaPublication.getCompleteDetail();
	PublicationDetail 			pubDetail 		= pubComplete.getPublicationDetail();
	UserDetail 					ownerDetail 	= kmeliaPublication.getCreator();
	String						pubName			= pubDetail.getName();
	String 						id 				= pubDetail.getPK().getId();

	String 		linkedPathString 	= kmeliaScc.getSessionPath();

	//Icons
	String pubValidateSrc	= m_context + "/util/icons/publicationValidate.gif";
	String pubUnvalidateSrc	= m_context + "/util/icons/publicationUnvalidate.gif";
	String alertSrc			= m_context + "/util/icons/alert.gif";
	String deletePubliSrc	= m_context + "/util/icons/publicationDelete.gif";
	String pdfSrc           = m_context + "/util/icons/publication_to_pdf.gif";
	String inDraftSrc		= m_context + "/util/icons/masque.gif";
	String outDraftSrc		= m_context + "/util/icons/visible.gif";
	String validateSrc		= m_context + "/util/icons/ok.gif";
	String refusedSrc		= m_context + "/util/icons/wrong.gif";
	String pubDraftOutSrc	= m_context + "/util/icons/publicationDraftOut.gif";

	String screenMessage = "";

	//Vrai si le user connecte est le createur de cette publication ou si il est admin
	boolean isOwner = false;

	if (action.equals("Unvalidate")) {
		screenMessage += ("<TABLE ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor><tr><td>");
		screenMessage += ("<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH=\"100%\" CLASS=intfdcolor4><tr>");
		screenMessage += ("<td align=center>"+resources.getString("kmelia.CloneUnvalidate")+"</td>");
		screenMessage += ("</tr></TABLE></td></tr></TABLE>");
	    action = "ViewPublication";
	}
	else if (action.equals("GeneratePdf")) {
		String link = (String) request.getAttribute("Link");
	    out.println("<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 onLoad=\"compileResult('"+link+"')\">");
	    out.println("</BODY>");
	}

	if (action.equals("ValidateView")) {
    	kmeliaScc.setSessionOwner(true);
        action = "UpdateView";
        isOwner = true;
    } else {
        if (profile.equals("admin") || profile.equals("publisher") || profile.equals("supervisor") || (ownerDetail != null && kmeliaScc.getUserDetail().getId().equals(ownerDetail.getId()) && profile.equals("writer"))) {
            isOwner = true;
        }

        if (isOwner) {
            kmeliaScc.setSessionOwner(true);
        } else {
		    //modification pour acceder e l'onglet voir aussi
            kmeliaScc.setSessionOwner(false);
        }
	}

  	String author 	= pubDetail.getAuthor();

  	String creatorId = pubDetail.getCreatorId();
	String updaterId = pubDetail.getUpdaterId();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
<view:includePlugin name="wysiwyg"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">

var refusalMotiveWindow = window;
var publicVersionsWindow = window;
var suspendMotiveWindow = window;
var attachmentWindow = window;

function clipboardCopy() {
  top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>copy.jsp?Id=<%=id%>';
}

function compileResult(fileName) {
    SP_openWindow(fileName, "PdfGeneration","770", "550", "toolbar=no, directories=no, menubar=no, locationbar=no ,resizable, scrollbars");
}

function pubDeleteConfirm(id) {
	closeWindows();
    if(window.confirm("<%=resources.getString("ConfirmDeletePub")%> ?")){
          document.toRouterForm.action = "<%=routerUrl%>DeletePublication";
          document.toRouterForm.PubId.value = id;
          document.toRouterForm.submit();
    }
}

function deleteCloneConfirm() {
    if(window.confirm("<%=EncodeHelper.javaStringToJsString(resources.getString("kmelia.ConfirmDeleteClone"))%>")){
          document.toRouterForm.action = "<%=routerUrl%>DeleteClone";
          document.toRouterForm.submit();
    }
}

function pubValidate(id) {
	document.toRouterForm.action = "<%=routerUrl%>ValidatePublication";
	document.toRouterForm.submit();
}

function pubUnvalidate(id) {
	document.pubForm.PubId.value = id;
	url = "WantToRefusePubli?PubId="+id;
    windowName = "refusalMotiveWindow";
	larg = "550";
	haut = "350";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!refusalMotiveWindow.closed && refusalMotiveWindow.name== "refusalMotiveWindow")
        refusalMotiveWindow.close();
    refusalMotiveWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function pubSuspend(id) {
	document.pubForm.PubId.value = id;
	url = "WantToSuspendPubli?PubId="+id;
    windowName = "suspendMotiveWindow";
	larg = "550";
	haut = "350";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!suspendMotiveWindow.closed && suspendMotiveWindow.name== "suspendMotiveWindow")
        suspendMotiveWindow.close();
    suspendMotiveWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
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

function viewPublicVersions(docId) {
	url = "<%=m_context+URLManager.getURL("VersioningPeas", spaceId, componentId)%>ListPublicVersionsOfDocument?DocId="+docId;
    windowName = "publicVersionsWindow";
	larg = "550";
	haut = "350";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!publicVersionsWindow.closed && publicVersionsWindow.name== "publicVersionsWindow")
        publicVersionsWindow.close();
    publicVersionsWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function pubDraftOut() {
	if (<%=kmeliaScc.isDraftOutAllowed()%>) {
		location.href = "<%=routerUrl%>DraftOut?From=ViewPublication";
	} else {
		window.alert("<%=kmeliaScc.getString("kmelia.PdcClassificationMandatory")%>");
	}
}
</script>
</head>
<body class="yui-skin-sam" onunload="closeWindows()" onload="openSingleAttachment()">
<%
        Window window = gef.getWindow();
        Frame frame = gef.getFrame();

        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(spaceLabel);
        browseBar.setComponentName(componentLabel, "javascript:onClick=topicGoTo('0')");
        browseBar.setPath(linkedPathString);
        browseBar.setExtraInformation(pubName);

        OperationPane operationPane = window.getOperationPane();
        if (!"supervisor".equals(profile)) {
          if (attachmentsEnabled) {
          	operationPane.addOperationOfCreation("#", resources.getString("kmelia.AddFile"), "javaScript:AddAttachment()");
          }
          if (kmeliaScc.isDraftEnabled()) {
            if (pubDetail.isDraft()) {
              operationPane.addLine();
              operationPane.addOperation(pubDraftOutSrc, resources.getString("PubDraftOut"), "javaScript:pubDraftOut()");
            }
          }
          operationPane.addOperation(deletePubliSrc, resources.getString("kmelia.DeleteClone"), "javaScript:deleteCloneConfirm();");
        }
        if (profile.equals("admin") || profile.equals("publisher")) {
            if (pubDetail.isValid()) {
              operationPane.addLine();
              operationPane.addOperation(pubUnvalidateSrc, resources.getString("PubUnvalidate?"), "javaScript:pubUnvalidate('" + id + "')");
            } else if (pubDetail.isValidationRequired() || pubDetail.isClone()) {
              operationPane.addLine();
              operationPane.addOperation(pubValidateSrc, resources.getString("PubValidate?"), "javaScript:pubValidate('" + id + "')");
              operationPane.addOperation(pubUnvalidateSrc, resources.getString("PubUnvalidate?"), "javaScript:pubUnvalidate('" + id + "')");
            }
          }
        if (profile.equals("supervisor")) {
          operationPane.addLine();
          operationPane.addOperation(pubUnvalidateSrc, resources.getString("kmelia.PubSuspend"), "javaScript:pubSuspend('"+id+"')");
        }
        out.println(window.printBefore());

        displayAllOperations(id, kmeliaScc, gef, "ViewClone", resources, out);

        out.println(frame.printBefore());

        if (screenMessage != null && screenMessage.length()>0) {
          out.println("<center>"+screenMessage+"</center>");
        }
          
          InfoDetail infos = pubComplete.getInfoDetail();
          ModelDetail model = pubComplete.getModelDetail();

	    int type 	= 0;
	    if (kmeliaScc.isVersionControlled()) {
	        type = 1; // Versioning
	    }

        /*********************************************************************************************************************/
		/** Affichage du header de la publication																			**/
		/*********************************************************************************************************************/
    	out.println("<TABLE border=\"0\" width=\"98%\" align=center>");
    	out.println("<TR><TD align=\"left\">");

    	out.println("<span class=\"txtnav\"><b>"+EncodeHelper.convertHTMLEntities(pubDetail.getName())+"</b></span>");
          if (!"user".equals(profile)) {
            if (pubDetail.isValidationRequired()) {
                                out.println("<img src=\""+outDraftSrc+"\" alt=\""+resources.getString("PubStateToValidate")+"\" align=\"absmiddle\">");
			} else if (pubDetail.isDraft()) {
                                out.println("<img src=\""+inDraftSrc+"\" alt=\""+resources.getString("PubStateDraft")+"\" align=\"absmiddle\">");
			} else if (pubDetail.isValid()) {
                                out.println("<img src=\""+validateSrc+"\" alt=\""+resources.getString("PublicationValidated")+"\" align=\"absmiddle\">");
			} else if (pubDetail.isRefused()) {
                                out.println("<img src=\""+refusedSrc+"\" alt=\""+resources.getString("PublicationRefused")+"\" align=\"absmiddle\">");
			}
		}

		out.println("<br><b>"+EncodeHelper.javaStringToHtmlParagraphe(EncodeHelper.convertHTMLEntities(pubDetail.getDescription()))+"<b><BR><BR>");

		out.println("</TD></TR></table>");
%>		
	<view:areaOfOperationOfCreation/>	
<%
		/*********************************************************************************************************************/
		/** Affichage du contenu de la publication																			**/
		/*********************************************************************************************************************/
		out.println("<TABLE border=\"0\" width=\"98%\" align=center>");
		out.println("<TR><TD valign=\"top\">");
    	if (WysiwygController.haveGotWysiwyg(spaceId, componentId, id)) {
        	out.flush();
        	getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+id+"&SpaceId="+spaceId+"&ComponentId="+componentId).include(request, response);
    	} else if (infos != null && model != null) {
       	    displayViewInfoModel(out, model, infos, resources, publicationSettings, m_context);
    	} else {
            Form xmlForm = (Form) request.getAttribute("XMLForm");
            DataRecord xmlData = (DataRecord) request.getAttribute("XMLData");
            String currentLang = (String) request.getAttribute("Language");
            if (xmlForm != null) {
              PagesContext xmlContext = new PagesContext("myForm", "0", resources.getLanguage(),
                  false, componentId, kmeliaScc.getUserId());
              xmlContext.setObjectId(id);
              xmlContext.setNodeId(kmeliaScc.getCurrentFolderId());
              xmlContext.setBorderPrinted(false);
              xmlContext.setContentLanguage(currentLang);
                
              xmlForm.display(out, xmlContext, xmlData);
            }
          }
    	out.println("</TD>");
    	
    	if (attachmentsEnabled) {
	    	/*********************************************************************************************************************/
			/** Affichage des fichiers joints																					**/
			/*********************************************************************************************************************/
			boolean showTitle 				= resources.getSetting("showTitle", true);
			boolean showFileSize 			= resources.getSetting("showFileSize", true);
			boolean showDownloadEstimation 	= resources.getSetting("showDownloadEstimation", true);
			boolean showInfo 				= resources.getSetting("showInfo", true);
			boolean showIcon = true;
		    if (!"bottom".equals(resources.getSetting("attachmentPosition"))) {
				out.println("<td width=\"25%\" valign=\"top\" align=\"center\">");
				out.println("<a name=\"attachments\"></a>");
		   	} else {
				out.println("</tr><tr>");
				out.println("<td valign=\"top\" align=\"left\">");
				out.println("<a name=\"attachments\"></a>");
		    }
			try {
				out.flush();
				if (kmeliaScc.isVersionControlled()) {
					getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/displayAttachedFiles.jsp?Id="+visiblePubId+"&ComponentId="+componentId+"&Context=Images&AttachmentPosition="+resources.getSetting("attachmentPosition")+"&ShowIcon="+showIcon+"&ShowTitle="+showTitle+"&ShowFileSize="+showFileSize+"&ShowDownloadEstimation="+showDownloadEstimation+"&ShowInfo="+showInfo).include(request, response);
				} else {
					getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/displayAttachedFiles.jsp?Id="+id+"&ComponentId="+componentId+"&Context=Images&AttachmentPosition="+resources.getSetting("attachmentPosition")+"&ShowIcon="+showIcon+"&ShowTitle="+showTitle+"&ShowFileSize="+showFileSize+"&ShowDownloadEstimation="+showDownloadEstimation+"&ShowInfo="+showInfo+"&Profile="+profile).include(request, response);
				}
			} catch (Exception e) {
				throw new KmeliaException("JSPpublicationManager.displayUserModelAndAttachmentsView()",SilverpeasException.ERROR,"root.EX_DISPLAY_ATTACHMENTS_FAILED", e);
			}
			out.println("</td>");
    	}
	    out.println("</TR>");
		out.println("</TABLE>");

    	out.println("<center>");
    	out.print("<span class=\"txtBaseline\">");
    	if (kmeliaScc.isAuthorUsed() && pubDetail.getAuthor() != null && !pubDetail.getAuthor().equals("")) {
			out.print("<br/>");
			out.print(resources.getString("GML.author")+" : "+pubDetail.getAuthor());
		}
    	out.print("<br/>");
    	%>
    		<view:username userId="<%=creatorId%>" /> - <%=resources.getOutputDate(pubDetail.getCreationDate()) %>
    	<%
		if (updaterId != null) { %>
			 | <%=resources.getString("kmelia.LastModification") %> : <view:username userId="<%=updaterId%>" /> - <%=resources.getOutputDate(pubDetail.getUpdateDate()) %>
		<% }
		out.println("</center>");

		out.flush();

        out.println(frame.printAfter());
        out.println(window.printAfter());
%>
<form name="pubForm" action="<%=routerUrl%>clone.jsp" method="post">
	<input type="hidden" name="Action"/>
	<input type="hidden" name="PubId"/>
	<input type="hidden" name="Profile" value="<%=profile%>"/>
</form>
<form name="refusalForm" action="<%=routerUrl%>Unvalidate">
  	<input type="hidden" name="PubId" value="<%=id%>"/>
  	<input type="hidden" name="Motive" value=""/>
</form>
<form name="defermentForm" action="<%=routerUrl%>SuspendPublication" method="post">
  	<input type="hidden" name="PubId" value="<%=id%>"/>
  	<input type="hidden" name="Motive" value=""/>
</form>
<form name="toRouterForm" method="post">
	<input type="hidden" name="PubId"/>
</form>
</body>
</html>