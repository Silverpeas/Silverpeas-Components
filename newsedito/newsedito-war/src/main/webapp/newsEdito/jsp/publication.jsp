<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ include file="imports.jsp" %>
<%@ include file="declarations.jsp.inc" %>
<%@ include file="newsUtils.jsp.inc" %>
<%@ include file="publicationUtils.jsp.inc" %>

<%!

void showRemovePublication(JspWriter out, NewsEditoSessionController news, ResourceLocator settings)
    throws NewsEditoException,IOException
{
	try{
		news.removePublication(news.getPublicationId());
	}
	catch(NewsEditoException e){
		throw new NewsEditoException("publicationEdit_JSP.displayEditInfoModel",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_SHOW_PUBLI_DELETED",e);
	}
		out.println("<BODY onLoad=gotoTitle('"+news.getTitleId() +"')>");
		out.println("</BODY>");
}

%>


<%
	if (action == null)
    	action = "View";

  	if (action.equals("SelectPublication"))
	{
		String   publicationId = (String) request.getParameter("PublicationId");
    	news.initNavigationForPublication(publicationId);
		action = "View";
	}

%>
<%@ include file="init.jsp.inc" %>
<%
	String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>
<HTML>
<HEAD>
<%out.println(gef.getLookStyleSheet());%>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<Script language="JavaScript">

function viewPublication()
{
    document.publicationForm.Action.value = "View";
    document.publicationForm.submit();
}

function gotoTitle(titleId)
{
    window.location.replace("manageArticles.jsp?Action=SelectTitle&TitleId="+titleId);
}

function viewSubTitle()
{
    document.manageArticleForm.Action.value = "Consult";
    document.manageArticleForm.submit();
}

function updatePublication()
{
	document.publicationEditForm.action = "UpdatePublication";
	document.publicationEditForm.Action.value = "UpdatePublication";
    document.publicationEditForm.submit();
}

function publicationDeleteConfirm(name)
{
    if (window.confirm("<%=news.getString("supprimerPublicationConfirmation")%> '" + name + "' ?")){
          document.publicationForm.Action.value = "RemovePublication";
          document.publicationForm.submit();
    }
}

function selectPublicationModel()
{
    document.publicationEditForm.action = "ListModels";
    document.publicationEditForm.submit();
}

function reallySelectPublicationModel(modelId)
{
    document.publicationForm.Action.value = "ReallySelectPublicationModel";
    document.publicationForm.ModelId.value = modelId;
    document.publicationForm.submit();
}


function viewArchive()
{
    document.newsForm.Action.value = "View";
    document.newsForm.submit();
}
function selectArchive(archiveId)
{
    document.manageArticlesForm.Action.value = "SelectArchive";
    document.manageArticlesForm.ArchiveId.value = archiveId;
    document.manageArticlesForm.submit();
}

function selectTitle(titleId)
{

    document.manageArticlesForm.Action.value = "SelectTitle";
    document.manageArticlesForm.TitleId.value = titleId;
    document.manageArticlesForm.submit();


}
function addEditorialPicture()
{
    SP_openWindow("editoPictureForm.jsp?Action=Choose", "addPictureWindow", "600", "150", "alwayRaised");
}

function doConsult(){
    document.newsForm.Action.value = "Consult";
    document.newsForm.submit();

}
function doOrganize(){
    document.manageForm.Action.value = "<%if (flag.equals("publisher")) out.print("ManageArticles"); else out.print("Organize"); %>";
    document.manageForm.submit();
}
function doSetInLine(){
    document.publishForm.Action.value = "Publish";
    document.publishForm.submit();
}
</script>
</HEAD>

<%

  if (action.equals("RemovePublication")) {
    showRemovePublication(out, news, settings);
  }
  else {
%>

<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5>

<FORM NAME="publicationForm" ACTION="publication.jsp" METHOD=POST >

<%

  if (action.equals("View")) {

	CompletePublication pubComplete = news.getCompletePublication();
 %>

<%
	Window window = gef.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(news.getComponentLabel(),"newsEdito.jsp");
	browseBar.setDomainName(news.getSpaceLabel());
	browseBar.setPath(navigationString);

	//Les onglets
    TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(consultationTP, "javaScript:doConsult()", false);

    if (flag.equals("publisher") || flag.equals("admin"))
    	tabbedPane.addTab(organisationTP, "javaScript:doOrganize()", true);

	if (flag.equals("admin"))
		tabbedPane.addTab(inLineSettingTP, "javaScript:doSetInLine()", false);

	//Le cadre
	Frame frame = gef.getFrame();

	OperationPane operationPane = window.getOperationPane();
	//operationPane.addOperation("icons/agenda_note_off.gif", "Nouvelle note", "javascript:onClick=addJournal()");
	//operationPane.addLine();


  if ( (flag.equals("publisher") && news.getUserId().equals(pubComplete.getPublicationDetail().getCreatorId()) && !archiveDetail.getStatus().equals("onLine") )
		 || flag.equals("admin")) {
			operationPane.addOperation(settings.getString("updatePublicationIcon"),
				news.getString("modifierPub"),
				"javascript:onClick=updatePublication()"
			);
			operationPane.addOperation(settings.getString("deletePublicationIcon"),
				news.getString("supprimerPub"),
				"javascript:onClick=publicationDeleteConfirm('" + Encode.javaStringToHtmlString(Encode.javaStringToJsString(pubComplete.getPublicationDetail().getName())) + "')"
			);
			operationPane.addOperation(settings.getString("selectPublicationModelIcon"),
				news.getString("selectionnerModelePub"),
				"javascript:onClick=selectPublicationModel()"
			);
			operationPane.addOperation(settings.getString("addSmallPic"),
				choisirImageEditorialOP,
				"javascript:onClick=addEditorialPicture()"
			);
	}

	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
%>


<TABLE CELLPADDING=5 CELLSPACING=2 BORDER=0 WIDTH="98%"><TR><TD NOWRAP>
<%
	// wysiwyg
	displayPublication(out, news, pubComplete,true, settings);
	if (WysiwygController.haveGotWysiwyg(pubComplete.getPublicationDetail().getPK().getComponentName(), pubComplete.getPublicationDetail().getPK().getId(), pubComplete.getPublicationDetail().getLanguage()))
	{
		out.flush();
		displayViewWysiwyg(news.getPublicationId(), pubComplete.getPublicationDetail().getLanguage(), news.getComponentId(), request, response);
	}

	// formulaire XML
	if (StringUtil.isDefined(pubComplete.getInfoDetail().getPK().getId()))
	{
		out.flush();
		Form			xmlForm 	= (Form) request.getAttribute("XMLForm");
		DataRecord		xmlData		= (DataRecord) request.getAttribute("XMLData");
		if (xmlForm != null)
		{
			PagesContext xmlContext = new PagesContext("myForm", "0", resources.getLanguage(), false, news.getComponentId(), news.getUserId());
			xmlContext.setObjectId(pubComplete.getPublicationDetail().getPK().getId());
			xmlContext.setBorderPrinted(false);
			xmlContext.setContentLanguage(news.getLanguage());

	    	xmlForm.display(out, xmlContext, xmlData);
		}
	}

%>
</TD></TR></TABLE>


<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>



    <%
  }
  else
    out.println("Error : unknown action = " + action);
%>

  <input type="hidden" name="Action">
  <input type="hidden" name="ModelId">
</FORM>

<FORM NAME="manageArticleForm" ACTION="manageArticle.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>

<FORM NAME="publicationEditForm" ACTION="publicationEdit.jsp" METHOD=POST ENCTYPE="multipart/form-data">
  <input type="hidden" name="Action">
</FORM>

<FORM NAME="newsForm" ACTION="newsEdito.jsp" METHOD=POST >
  <input type="hidden" name="ArchiveId">
  <input type="hidden" name="Action">
</FORM>
<FORM NAME="manageArticlesForm" ACTION="manageArticles.jsp" METHOD=POST >
  <input type="hidden" name="Action">
  <input type="hidden" name="TitleId">
  <input type="hidden" name="ArchiveId">
</FORM>
<FORM NAME="manageForm" ACTION="<%if (flag.equals("publisher")) out.print("manageArticles.jsp"); else out.print("manageNews.jsp");%>" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>
 <FORM NAME="publishForm" ACTION="publishNews.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>


</BODY>
  <%}// end of action to display%>
</HTML>
