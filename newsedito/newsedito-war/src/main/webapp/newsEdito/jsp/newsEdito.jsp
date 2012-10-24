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

<%@ include file="publicationUtils.jsp.inc" %>

<%!

	String showAddFavorit(JspWriter out, NewsEditoSessionController news)
	  throws NewsEditoException
	{
	  try {
	    news.addFavorit(news.getTitleId());
	    return news.getString("ajoutFavoriEffectue");
	  }
	  catch (NewsEditoException e) {
	    return news.getString("ajoutFavoriImpossible");
	  }

	}
%>

<%
	String sURI 			= request.getRequestURI();
	String sRequestURL 		= HttpUtils.getRequestURL(request).toString();
	String m_sAbsolute 		= sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());

	String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL"); 
	String nameTitle = "";
	String description = "";
	String url = "";
%>

<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet());%>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<Script language="JavaScript">

var favoritWindow = window;

function copyPublications() {

	document.newsForm.action = "multicopy.jsp";
	document.newsForm.target = "IdleFrame";
	document.newsForm.submit();
}

function reallyClose() {
  if (window.favorite != null)
    window.favorite.close()
  if (window.node != null)
		window.node.close();
}

function doConsult(){
    document.newsForm.Action.value = "Consult";
    document.newsForm.submit();	

}
function pdfGeneration()
{
    document.gotoPdfForm.Action.value = "CompilePdf";
    document.gotoPdfForm.submit();
}
<%
if (flag.equals("publisher") || flag.equals("admin"))
{
%>
function doOrganize(){
    document.manageForm.Action.value = "<%if (flag.equals("publisher")) out.print("ManageArticles"); else out.print("Organize"); %>";
    document.manageForm.submit();
}

function viewArchiveStatistic(fatherId)
{
  urlStat = "statistic.jsp?FatherId="+fatherId;
	SP_openWindow(urlStat,'stat','700','350','scrollbars=yes,resizable=yes');
}


<%
	
	if (flag.equals("admin"))
	{
%>
function doSetInLine(){
    document.publishForm.Action.value = "Publish";
    document.publishForm.submit();
}
<%
	}
}
%>

function selectArchive(archiveId)
{
    document.newsForm.Action.value = "SelectArchive";
    document.newsForm.ArchiveId.value = archiveId;
    document.newsForm.submit();
}

function selectTitle(titleId)
{

    document.titleForm.Action.value = "SelectTitle";
    document.titleForm.TitleId.value = titleId;
    document.titleForm.submit();
	
	
}
function selectPublication(publicationId)
{

    document.publicationForm.Action.value = "SelectPublication";
    document.publicationForm.action = "publication";
    document.publicationForm.PublicationId.value = publicationId;
    document.publicationForm.submit();
	
	
}

function viewFavorits()
{
		SP_openWindow('favorite.jsp','Favorite','550','350','alwaysRaised,scrollbars=yes,resizable,scrollbars');
}

/* function addFavorit()
{
    document.titleForm.Action.value = "AddFavorit";
    document.titleForm.submit();
}  */

function addFavorite(m_sAbsolute,m_context,name,description,url) 
	{
  		urlWindow = m_sAbsolute + m_context + "/RmyLinksPeas/jsp/CreateLinkFromComponent?Name="+name+"&Description="+description+"&Url="+url+"&Visible=true";
	    windowName = "favoritWindow";
		larg = "550";
		haut = "250";
	    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
	    if (!favoritWindow.closed && favoritWindow.name== "favoritWindow")
	        favoritWindow.close();
	    favoritWindow = SP_openWindow(urlWindow, windowName, larg, haut, windowParams);
	}
</Script>


</HEAD>

<BODY onUnload="reallyClose()">


<%
	String toPrint = null;
	
	if (action == null)
    	action = "Consult";
  	
  	if (action.equals("Consult")){
  		news.selectFirstOnLineArchive();
  	}
  	else if (action.equals("SelectArchive")) {
    	String archiveId = (String) request.getParameter("ArchiveId");
    	news.setArchiveId(archiveId);
    	action = "Consult";
  	}else if (action.equals("SelectTitle")) {
    	String titleId = (String) request.getParameter("TitleId");
    	news.initNavigationForNode(titleId);
    	action = "Consult";
  	}else if (action.equals("SelectPublication")) {
     	String   publicationId = (String) request.getParameter("PublicationId");
    	news.initNavigationForPublication(publicationId);
    	action = "Consult";
  	}
  	else if (action.equals("AddFavorit")) {
    	toPrint = showAddFavorit(out, news);
    	action = "Consult";
  	}    
  	else if (action.equals("RemoveFavorit")) {
    	String favoritId = (String) request.getParameter("FavoritId");
    	news.removeFavorit(favoritId);
    	action = "Consult";    	
  	}
	
		
 	
%>

<%@ include file="init.jsp.inc" %>

<%
	Window window = gef.getWindow();
	String bodyPart="";
	
	// La barre de navigation
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(news.getComponentLabel(),"newsEdito.jsp");
	browseBar.setDomainName(news.getComponentLabel());		  
	browseBar.setPath(navigationString);
	
	
	// l'operationpane
	OperationPane operationPane = window.getOperationPane();
	

	if ((detailLevel > 0)&&(flag.equals("publisher") || flag.equals("admin")&& archiveDetail !=null)) {

		operationPane.addOperation(settings.getString("statisticIcon"), 
		news.getString("statistiquesDuJournalX") + " " + Encode.javaStringToHtmlString(archiveDetail.getName()), 
							"javascript:onClick=viewArchiveStatistic('" + archiveDetail.getNodePK().getId() + "')");
 	}


	if (detailLevel > 0)
		operationPane.addOperation(settings.getString("pdfIcon"), news.getString("genererPdf"), "javascript:onClick=pdfGeneration()");
	
	if (detailLevel == 2) {
		NodeDetail node = news.getTitleDetail();
       	nameTitle = news.getSpaceLabel() + " > " + news.getComponentLabel() + " > " +node.getName();	
       	description = node.getDescription();
       	url = node.getLink();

		operationPane.addOperation(settings.getString("addFavoriteIcon"),news.getString("ajouterFavori"),"javaScript:addFavorite('"+m_sAbsolute+"','"+m_context+"','"+nameTitle+"','"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(description))+"','"+url+"')");
	}

	//if (detailLevel > 0)
		//operationPane.addOperation(settings.getString("favoriteIcon"), news.getString("mesTitresFavoris"),"javascript:onClick=viewFavorits()");
	
	if (detailLevel == 2) {							
	operationPane.addOperation(settings.getString("copyIcon"),news.getString("copierPublications"),"javascript:onClick=copyPublications()");
	}
	
	//Les onglets
    TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(consultationTP, "javaScript:doConsult()", true);
    
    if (flag.equals("publisher") || flag.equals("admin"))
    	tabbedPane.addTab(organisationTP, "javaScript:doOrganize()", false);
	
	if (flag.equals("admin"))
		tabbedPane.addTab(inLineSettingTP, "javaScript:doSetInLine()", false);

	//Le cadre
	Frame frame = gef.getFrame();

	//debut du code HTML
	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
%>
<FORM NAME="newsForm" ACTION="newsEdito.jsp" METHOD=POST >

<table width="100%" border="0" cellspacing="1" cellpadding="3">
  <% if (toPrint != null) {%>
  <tr valign="top">
    <td>
      <%=toPrint%>
    </td>
  </tr>
  <%}%>
  <tr>
    <td width="25%" valign="top"> 
	<%@ include file="navigationDisplaying.jsp.inc" %>   
    
    </td>
    <td valign="top"> 
    <% 
		

	switch(detailLevel)
	{
		case 0 :
		{
			// nothing to do
		}
		break;
		case 1 :
		{
			Collection editoList = null;
	
	     	try {
	    		editoList = news.getArchivePublicationDetails();
	    	}
	   	catch	(NewsEditoException e) {
			SilverTrace.error("NewsEdito", "newsEdito_JSP", "NewsEdito.EX_PROBLEM_TO_GET_ARCHIVE",e);
		}   

       		if (editoList!=null)
       			displayEditorial(out,editoList,archiveDetail.getModelId(),news,true,"selectPublication","headline","healineBody");
		}
		break;
		case 2 :
		{
            Collection pubList = null;
	    	try {
     				pubList = news.getTitlePublicationDetails();
   			}
	   		catch	(NewsEditoException e) {
				SilverTrace.error("NewsEdito", "publishNews_JSP", "NewsEdito.EX_PROBLEM_TO_GET_PUBLI_TITLE",e);
			}   

           	if (pubList!=null)
           		displayPublicationList(out,pubList,news,false,true,"selectPublication","headline","healineBody");		
 		}
		break;
		case 3 :
		{
			CompletePublication pubComplete = news.getCompletePublication();
			displayPublication(out, news, pubComplete, settings) ;
			if (WysiwygController.haveGotWysiwyg(pubComplete.getPublicationDetail().getPK().getSpace(), pubComplete.getPublicationDetail().getPK().getComponentName(), pubComplete.getPublicationDetail().getPK().getId())) {
				out.flush();
				displayViewWysiwyg(news.getPublicationId(), news.getSpaceId(), news.getComponentId(), request, response);
			}
		}
		break;

	
	}      
       %>

      <p>&nbsp;</p>
      </td>
  </tr>
</table>

<%
	// fin du HTML
	out.println(frame.printAfter());
  	out.println(window.printAfter());
%>


  <input type="hidden" name="Action">
  <input type="hidden" name="TitleId">
  <input type="hidden" name="FavoritId">
  <input type="hidden" name="ArchiveId">
  <input type="hidden" name="Language">
</FORM>

<FORM NAME="titleForm" ACTION="newsEdito.jsp" METHOD=POST >
  <input type="hidden" name="TitleId">
  <input type="hidden" name="Action">
</FORM>

<FORM NAME="publicationForm" ACTION="newsEdito.jsp" METHOD=POST >
  <input type="hidden" name="PublicationId">
  <input type="hidden" name="Action">
</FORM>

<FORM NAME="statisticForm" ACTION="statistic.jsp" METHOD=POST >
  <input type="hidden" name="FatherId">
</FORM>

<FORM NAME="gotoPdfForm" ACTION="pdfCompile.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>

<FORM NAME="manageForm" ACTION="<%if (flag.equals("publisher")) out.print("manageArticles.jsp"); else out.print("manageNews.jsp");%>" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>
 <FORM NAME="publishForm" ACTION="publishNews.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>

</BODY>
</HTML>