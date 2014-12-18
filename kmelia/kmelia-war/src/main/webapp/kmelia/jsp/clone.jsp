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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@include file="checkKmelia.jsp" %>

<%@page import="com.silverpeas.publicationTemplate.*"%>
<%@page import="com.silverpeas.form.*"%>
<%@page import="org.silverpeas.kmelia.jstl.KmeliaDisplayHelper"%>
<%@ page import="org.silverpeas.util.exception.SilverpeasException" %>

<%
	ResourceLocator publicationSettings = new ResourceLocator("org.silverpeas.util.publication.publicationSettings", resources.getLanguage());

	//Recuperation des parametres
	String 					profile 		= (String) request.getAttribute("Profile");
	String 					action 			= (String) request.getAttribute("Action");
	KmeliaPublication 		kmeliaPublication = (KmeliaPublication) request.getAttribute("Publication");
	boolean 				attachmentsEnabled = (Boolean) request.getAttribute("AttachmentsEnabled");
	boolean 				userCanValidate = (Boolean) request.getAttribute("UserCanValidate");
	boolean draftOutTaxonomyOK = (Boolean) request.getAttribute("TaxonomyOK");
  	boolean draftOutValidatorsOK = (Boolean) request.getAttribute("ValidatorsOK");
  	boolean highlightFirst = resources.getSetting("highlightFirstOccurence", false);

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
	String deletePubliSrc	= m_context + "/util/icons/publicationDelete.gif";
	String inDraftSrc		= m_context + "/util/icons/masque.gif";
	String outDraftSrc		= m_context + "/util/icons/visible.gif";
	String validateSrc		= m_context + "/util/icons/ok.gif";
	String refusedSrc		= m_context + "/util/icons/wrong.gif";
	String pubDraftOutSrc	= m_context + "/util/icons/publicationDraftOut.gif";

	String screenMessage = "";

	//Vrai si le user connecte est le createur de cette publication ou si il est admin
	boolean isOwner = false;

	//display message according to previous action
	if (action.equals("ValidationComplete") || action.equals("ValidationInProgress") || action.equals("Unvalidate") || action.equals("Suspend")) {
	  if (action.equals("ValidationComplete")) {
	    screenMessage = "<div class=\"inlineMessage-ok\">" + resources.getString("PubValidate") + "</div>";
	  } else if (action.equals("ValidationInProgress")) {
	    screenMessage = "<div class=\"inlineMessage\">" + resources.getString("kmelia.PublicationValidationInProgress") + "</div>";
	  } else if (action.equals("Unvalidate")) {
	    screenMessage = "<div class=\"inlineMessage-nok\">" + resources.getString("kmelia.CloneUnvalidate") + "</div>";
	  } else if (action.equals("Suspend")) {
	    screenMessage = "<div class=\"inlineMessage-nok\">" + resources.getString("kmelia.PublicationSuspended") + "</div>";
	  }
	  action = "ViewPublication";
	}

	// display message according to current state of publication
	if (!StringUtil.isDefined(screenMessage)) {
	   if (pubDetail.isRefused()) {
	     screenMessage = "<div class=\"inlineMessage-nok\">" + resources.getString("PublicationRefused") + "</div>";
	   } else if (pubDetail.isValidationRequired()) {
	     screenMessage = "<div class=\"inlineMessage\">" + resources.getString("kmelia.publication.tovalidate.state");
	  	 if (userCanValidate) {
	  	   screenMessage += " " + resources.getString("kmelia.publication.tovalidate.action")+"<br/>";
	 	   screenMessage += "<a href=\"javascript:onclick=pubValidate()\" class=\"button validate\"><span>"+resources.getString("PubValidate?")+"</span></a>";
	 	   screenMessage += "<a href=\"javascript:onclick=pubUnvalidate()\" class=\"button refuse\"><span>"+resources.getString("PubUnvalidate?")+"</span></a>";
	     }
	     screenMessage += "</div>";
	   }
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
<view:includePlugin name="popup"/>
<script type="text/javascript">

var publicVersionsWindow = window;
var suspendMotiveWindow = window;
var attachmentWindow = window;

function deleteCloneConfirm() {
    if(window.confirm("<%=EncodeHelper.javaStringToJsString(resources.getString("kmelia.ConfirmDeleteClone"))%>")){
          document.toRouterForm.action = "<%=routerUrl%>DeleteClone";
          document.toRouterForm.submit();
    }
}

function pubValidate() {
	document.toRouterForm.action = "<%=routerUrl%>ValidatePublication";
	document.toRouterForm.submit();
}

function pubUnvalidate() {
	$('#publication-refusal-form').dialog('open');
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
    if (window.publicVersionsWindow != null) {
    	window.publicVersionsWindow.close();
    }
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
	if (<%= draftOutTaxonomyOK && draftOutValidatorsOK %>) {
        location.href = "<%=routerUrl%>DraftOut?From=ViewPublication";
    } else {
    	$("#publication-draftout").dialog('open');
    }
}

$(function() {
    $("#publication-draftout").dialog({
        autoOpen: false,
        title: "<%=resources.getString("PubDraftOut")%>",
        modal: true,
        minWidth: 500,
        resizable : false,
        buttons: {
          'OK': function() {
            $(this).dialog("close");
          }
        }
      });

    $("#publication-refusal-form").dialog({
        autoOpen: false,
        title: "<%=resources.getString("PubUnvalidate?")%>",
        modal: true,
        minWidth: 500,
        resizable : false,
        buttons: {
          'OK': function() {
        	  if (!$.trim($("#refusal-motive").val())) {
        		  window.alert("'<%=kmeliaScc.getString("RefusalMotive")%>' <%=resources.getString("GML.MustBeFilled")%>");
        		  return true;
        	  } else {
		  		document.refusalForm.submit();
        	  }
          },
          '<%= resources.getString("GML.cancel") %>': function() {
              $(this).dialog("close");
          }
        }
      });
  });
</script>
</head>
<body class="yui-skin-sam" onunload="closeWindows()" id="<%=componentId%>">
<div id="preview-clone">
<%
        Window window = gef.getWindow();
        Frame frame = gef.getFrame();

        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(spaceLabel);
        browseBar.setComponentName(componentLabel, "javascript:onclick=topicGoTo('0')");
        browseBar.setPath(linkedPathString);
        browseBar.setExtraInformation(pubName);

        OperationPane operationPane = window.getOperationPane();
        if (!"supervisor".equals(profile)) {
          if (attachmentsEnabled) {
          	operationPane.addOperation("#", resources.getString("kmelia.AddFile"), "javascript:addAttachment('" +pubDetail.getId() + "')");
          }
          if (kmeliaScc.isDraftEnabled()) {
            if (pubDetail.isDraft()) {
              operationPane.addLine();
              operationPane.addOperation(pubDraftOutSrc, resources.getString("PubDraftOut"), "javascript:pubDraftOut()");
            }
          }
          operationPane.addOperation(deletePubliSrc, resources.getString("kmelia.DeleteClone"), "javascript:deleteCloneConfirm();");
        }
        if (userCanValidate) {
	      operationPane.addLine();
	      operationPane.addOperation(pubValidateSrc, resources.getString("PubValidate?"), "javascript:pubValidate()");
	      operationPane.addOperation(pubUnvalidateSrc, resources.getString("PubUnvalidate?"), "javascript:pubUnvalidate()");
        }
        if (profile.equals("supervisor")) {
          operationPane.addLine();
          operationPane.addOperation(pubUnvalidateSrc, resources.getString("kmelia.PubSuspend"), "javascript:pubSuspend('"+id+"')");
        }
        out.println(window.printBefore());

        KmeliaDisplayHelper.displayAllOperations(id, kmeliaScc, gef, "ViewClone", resources, out);
        out.println(frame.printBefore());
%>
	<div class="rightContent">
<%
		/*********************************************************************************************************************/
		/** Colonne de droite																							    **/
		/*********************************************************************************************************************/
    	if (attachmentsEnabled) {
	    	/*********************************************************************************************************************/
			/** Affichage des fichiers joints																					**/
			/*********************************************************************************************************************/
			boolean showTitle = resources.getSetting("showTitle", true);
			boolean showFileSize = resources.getSetting("showFileSize", true);
			boolean showDownloadEstimation = resources.getSetting("showDownloadEstimation", true);
			boolean showInfo = resources.getSetting("showInfo", true);
			boolean showIcon = true;

		    out.println("<a name=\"attachments\"></a>");
			try {
				out.flush();
        getServletConfig().getServletContext().getRequestDispatcher(
            "/attachment/jsp/displayAttachedFiles.jsp?Id=" + id + "&ComponentId=" + componentId +
                "&Context=attachment&AttachmentPosition=" +
                resources.getSetting("attachmentPosition") + "&ShowIcon=" + showIcon +
                "&ShowTitle=" + showTitle + "&ShowFileSize=" + showFileSize +
                "&ShowDownloadEstimation=" + showDownloadEstimation + "&ShowInfo=" + showInfo +
                "&Profile=" + profile).include(request, response);
			} catch (Exception e) {
				throw new KmeliaException("JSPpublicationManager.displayUserModelAndAttachmentsView()",
            SilverpeasException.ERROR,"root.EX_DISPLAY_ATTACHMENTS_FAILED", e);
			}
    	}

        /*********************************************************************************************************************/
        /** Affichage des Info de publication																		**/
        /*********************************************************************************************************************/
		%>
		<div id="infoPublication" class="bgDegradeGris">

       		<% if (kmeliaScc.isAuthorUsed() && StringUtil.isDefined(pubDetail.getAuthor())) { %>
				<p id="authorInfo"><%=resources.getString("GML.author")%> : <b><%=pubDetail.getAuthor()%></b></p>
			<% }%>

       		<% if (updaterId != null) {%>
			  	<div id="lastModificationInfo" class="paragraphe">
			  		<%=resources.getString("PubDateUpdate")%>  <br />
                                   <b><%=resources.getOutputDate(pubDetail.getUpdateDate())%></b> <%=resources.getString("GML.by")%> <view:username userId="<%=kmeliaPublication.getLastModifier().getId()%>"/>
			  		<div class="profilPhoto"><img src="<%=m_context %><%=kmeliaPublication.getLastModifier().getAvatar() %>" alt="" class="defaultAvatar"/></div>
		  		</div>
		  	<% }%>

			<div id="creationInfo" class="paragraphe">
			 	<%=resources.getString("PubDateCreation")%> <br/>
			 	<b><%=resources.getOutputDate(pubDetail.getCreationDate())%></b> <%=resources.getString("GML.by")%> <view:username userId="<%=kmeliaPublication.getCreator().getId()%>"/>
			 	<div class="profilPhoto"><img src="<%=m_context %><%=kmeliaPublication.getCreator().getAvatar() %>" alt="" class="defaultAvatar"/></div>
		 	</div>

		</div>
      </div>
      <%
        /*********************************************************************************************************************/
        /** Colonne Pricipale																									**/
        /*********************************************************************************************************************/
    	out.println("<div class=\"principalContent\">");
        if (StringUtil.isDefined(screenMessage)) {
	      out.println(screenMessage);
        }
	        /*********************************************************************************************************************/
	        /** Affichage du header de la publication																			**/
	        /*********************************************************************************************************************/
	        out.print("<h2 class=\"publiName\">");

	        out.print(pubDetail.getName());

	     	if (!"user".equals(profile)) {
	          if (pubDetail.isValidationRequired()) {
	            out.println(" <img src=\"" + outDraftSrc + "\" alt=\"" + resources.getString(
	                "PubStateToValidate") + "\"  id=\"status\"/>");
	          } else if (pubDetail.isDraft()) {
	            out.println(
	                " <img src=\"" + inDraftSrc + "\" alt=\"" + resources.getString("PubStateDraft") + "\"  id=\"status\"/>");
	          } else if (pubDetail.isValid()) {
	            out.println(" <img src=\"" + validateSrc + "\" alt=\"" + resources.getString(
	                "PublicationValidated") + "\"  id=\"status\"/>");
	          } else if (pubDetail.isRefused()) {
	            out.println(" <img src=\"" + refusedSrc + "\" alt=\"" + resources.getString(
	                "PublicationRefused") + "\"  id=\"status\"/>");
	          }
			}

	        out.println("</h2>");

	        String description = EncodeHelper.javaStringToHtmlParagraphe(pubDetail.getDescription());
	        if (StringUtil.isDefined(description)) {
	        	out.println("<p class=\"publiDesc text2\">" + description + "</p>");
	        }

	        /*********************************************************************************************************************/
	        /** Affichage du contenu de la publication																			**/
	        /*********************************************************************************************************************/

	        out.println("<div id=\"richContent\">");
	        if (WysiwygController.haveGotWysiwygToDisplay(componentId, id, resources.getLanguage())) {
	          %>
	          <view:displayWysiwyg objectId="<%=id%>" componentId="<%=componentId %>" language="<%=resources.getLanguage() %>" axisId="<%=kmeliaScc.
	              getAxisIdGlossary() %>" highlightFirst="<%=String.valueOf(highlightFirst) %>"/>
	          <%
	        } else {
	          Form xmlForm = (Form) request.getAttribute("XMLForm");
	          DataRecord xmlData = (DataRecord) request.getAttribute("XMLData");
	          if (xmlForm != null) {
	            PagesContext xmlContext = new PagesContext("myForm", "0", resources.getLanguage(),
	                false, componentId, kmeliaScc.getUserId());
	            xmlContext.setObjectId(id);
	            if (kmeliaMode) {
	              xmlContext.setNodeId(kmeliaScc.getCurrentFolderId());
	            }
	            xmlContext.setBorderPrinted(false);
				xmlForm.display(out, xmlContext, xmlData);
			  }
			}
			out.println("</div>");

		out.println("</div>");

		out.flush();

        out.println(frame.printAfter());
        out.println(window.printAfter());
%>
<form name="pubForm" action="<%=routerUrl%>clone.jsp" method="post">
	<input type="hidden" name="Action"/>
	<input type="hidden" name="PubId"/>
	<input type="hidden" name="Profile" value="<%=profile%>"/>
</form>
<form name="defermentForm" action="<%=routerUrl%>SuspendPublication" method="post">
  	<input type="hidden" name="PubId" value="<%=id%>"/>
  	<input type="hidden" name="Motive" value=""/>
</form>
<form name="toRouterForm" method="post">
	<input type="hidden" name="PubId"/>
</form>
<div id="publication-draftout" style="display: none;">
	<%=resources.getString("kmelia.publication.draftout.impossible")%>
	<ul>
   	<% if(!draftOutTaxonomyOK) { %>
   		<li><%=resources.getString("kmelia.PdcClassificationMandatory")%></li>
   	<% } %>
   	<% if(!draftOutValidatorsOK) { %>
   		<li><%=resources.getString("kmelia.publication.validators.mandatory")%></li>
   	<% } %>
   	</ul>
</div>
<div id="publication-refusal-form" style="display: none;">
	<form name="refusalForm" action="Unvalidate" method="post">
    	<table>
      		<tr>
      			<td class="txtlibform" valign="top"><%=kmeliaScc.getString("RefusalMotive")%></td>
      			<td><textarea name="Motive" id="refusal-motive" rows="10" cols="60"></textarea><input type="hidden" name="PubId" value="<%=id%>"/>&nbsp;<img border="0" src="<%=resources.getIcon("kmelia.mandatory")%>" width="5" height="5"/></td>
      		</tr>
      	</table>
    </form>
</div>
</div>
</body>
</html>