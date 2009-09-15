<%@ include file="imports.jsp" %>
<%@ include file="declarations.jsp.inc" %>


<HTML>
<HEAD>
<%out.println(gef.getLookStyleSheet()); %>

<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<script type="text/javascript" src="../../util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="../../util/javaScript/animation.js"></script>
<Script language="JavaScript">


function doConsult(){
    document.newsForm.Action.value = "Consult";
    document.newsForm.submit();	
}
<%
if (flag.equals("publisher") || flag.equals("admin"))
{
%>
function doOrganize(){
    document.manageForm.Action.value = "Organize";
    document.manageForm.submit();
}

function archiveDeleteGroup()
{
	if (window.confirm("<%=news.getString("supprimerArchivesSelectionneesConfirmation")%>")){
    document.manageForm.Action.value = "RemoveArchives";
    document.manageForm.submit();
	}

}
function titleDeleteGroup()
{
	if (window.confirm("<%=news.getString("supprimerTitresSelectionnesConfirmation")%>")){
    document.manageForm.Action.value = "RemoveTitles";
    document.manageForm.submit();
	}

}
<%
	
	if (flag.equals("admin"))
	{
%>
function doSetInLine(){
    document.publishForm.Action.value = "Publish";
    document.publishForm.submit();
}
function addArchive()
{
	SP_openWindow("nodeEdit.jsp?Action=AddNode&ActionTitle=ajouterArchive", "Node" , "550" , "250" ,"alwayRaised");
}
function addTitle(archiveId)
{
	SP_openWindow("nodeEdit.jsp?Action=AddNode&ActionTitle=ajouterTitre&FatherId="+archiveId, "Node", "550" , "250" , "alwayRaised, scrollbars=yes, resizable");
}

function updateTitle(titleId)
{

    if (checkString(document.manageForm.Title,"<%=news.getString("champsObligatoireNonRenseigne")+" "+news.getString("nodeName") %>") )
    {
	    if (!isValidTextArea(document.manageForm.Description)) {
			window.alert("<%=news.getString("champsDescriptionTropLong")%>");
		}
		else 
		{    
    		document.manageForm.Action.value = "UpdateTitle";
    		document.manageForm.TitleId.value = titleId;
    		document.manageForm.submit();
		}
	}
}	
<%
	}
}
%>

function selectArchive(archiveId)
{
    document.manageForm.Action.value = "SelectArchive";
    document.manageForm.ArchiveId.value = archiveId;
    document.manageForm.submit();
}

function selectTitle(titleId)
{

    document.manageArticlesForm.Action.value = "SelectTitle";
    document.manageArticlesForm.TitleId.value = titleId;
    document.manageArticlesForm.submit();
	
	
}



</Script>


</HEAD>

<BODY>
<FORM NAME="manageForm" ACTION="manageNews.jsp" METHOD=POST >
<%
	if (action == null)
    	action = "Organize";
  	
  	if (action.equals("Organize"))
  	{
  		news.setArchiveId(null);
  	}
  	if (action.equals("RemoveArchives"))
  	{
      	if (archives!=null)
      	{
	        Iterator i = archives.iterator();
	        while (i.hasNext()) 
	        {
	        	NodeDetail archive = (NodeDetail) i.next();
	            if (request.getParameter("checkbox"+archive.getNodePK().getId())!=null && request.getParameter("checkbox"+archive.getNodePK().getId()).equals("on"))
	            	news.removeTitle(archive.getNodePK().getId());
	  		}
			try {
        		archives = news.getArchiveList();
			}
	   		catch	(NewsEditoException e) {
				SilverTrace.error("NewsEdito", "manageNews_JSP", "NewsEdito.EX_PROBLEM_TO_GET_ARCHIVE_LIST",e);
			}   
		}  		
  		action = "Organize";
  	}else if (action.equals("RemoveTitles"))
  	{
      	if ((archiveDetail!=null)&&(archiveDetail.getChildrenDetails()!=null))
      	{
	        Iterator i = archiveDetail.getChildrenDetails().iterator();
	        while (i.hasNext()) 
	        {
	        	NodeDetail title = (NodeDetail) i.next();
	            if (request.getParameter("checkbox"+title.getNodePK().getId())!=null && request.getParameter("checkbox"+title.getNodePK().getId()).equals("on"))
	            	news.removeTitle(title.getNodePK().getId());
	  		}
			try {
        		archiveDetail = news.getArchiveContent();
			}
	   		catch	(NewsEditoException e) {
				SilverTrace.error("NewsEdito", "manageNews_JSP", "NewsEdito.EX_PROBLEM_TO_GET_ARCHIVE_CONTENT",e);
			}   
		}  		
  		action = "Organize";
  	}
  	
  	
  	else if (action.equals("UpdateTitle"))
  	{
  		String titleId = (String) request.getParameter("TitleId");
  		String title = (String) request.getParameter("Title");
  		String description = (String) request.getParameter("Description");  		
  		news.updateTitle(titleId, title, description);
  		action = "Organize";
  	}
  	else if (action.equals("SelectArchive")) {
    	String archiveId = (String) request.getParameter("ArchiveId");
    	news.setArchiveId(archiveId);
    	action = "Organize";
  	}


%>
<%@ include file="init.jsp.inc" %>

<%
	Window window = gef.getWindow();
	String bodyPart="";
	
	// La barre de navigation
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(news.getComponentLabel(),"newsEdito.jsp");
	browseBar.setDomainName(news.getSpaceLabel());    	
	if (!navigationString.equals(""))
		browseBar.setPath(organiseBB+" > "+navigationString);
	else
		browseBar.setPath(organiseBB);
	

	//Les opérations
	OperationPane operationPane = window.getOperationPane();
	
	switch(detailLevel)
	{	case 0 :
		{
			if (flag.equals("admin"))
			{
				operationPane.addOperation(settings.getString("addJournalIcon"), news.getString("creerJournal") , "javascript:onClick=addArchive()");
				operationPane.addOperation(belpou,deleteArchivesOP ,"javascript:archiveDeleteGroup()");		
			}
		}break;
		case 1 :
		{
			if (flag.equals("admin"))
			{
				operationPane.addOperation(settings.getString("addTitleIcon"), 
					news.getString("ajouterTitreDansJournalX") + " " + Encode.javaStringToHtmlString(archiveDetail.getName()), 
					"javascript:onClick=addTitle('" + archiveDetail.getNodePK().getId() + "')");
		
				operationPane.addOperation(settings.getString("deleteTitleIcon"),deleteArchivesOP ,"javascript:titleDeleteGroup()");
			}
		}break;
		case 2 :
		{
			
		}break;
		case 3 :
		{
		
		
		}break;
	}


	
	//Les onglets
    TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(consultationTP, "javaScript:doConsult()", false);
    
    if (flag.equals("publisher") || flag.equals("admin"))
    	tabbedPane.addTab(organisationTP, "javaScript:doOrganize()", true);
	
	if (flag.equals("admin"))
		tabbedPane.addTab(inLineSettingTP, "javaScript:doSetInLine()", false);

	//Le cadre
	Frame frame = gef.getFrame();	

	switch(detailLevel)
	{	
		// gestion des listes d'archives
		case 0 :
		{
			//Le tableau de tri contenant tous les journaux
			ArrayPane arrayPane = gef.getArrayPane("newsList", "manageNews.jsp", request, session);
			//Définition des colonnes du tableau
		    arrayPane.addArrayColumn(archivesAP);
		    arrayPane.addArrayColumn(creationDateAP);
			ArrayColumn arrayColumnState = arrayPane.addArrayColumn("&nbsp;");
			arrayColumnState.setSortable(false);
		    if (flag.equals("admin"))
		    {
		    	ArrayColumn arrayColumnDel = arrayPane.addArrayColumn("&nbsp;");
				arrayColumnDel.setSortable(false);
			}
			
			Iterator i = archives.iterator();
			while (i.hasNext()) 
			{
				ArrayLine arrayLine = arrayPane.addArrayLine();	
				NodeDetail archive = (NodeDetail) i.next();
				arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(archive.getName()),"javaScript:selectArchive('"+archive.getNodePK().getId()+"')");
				arrayLine.addArrayCellText(resources.getOutputDate(archive.getCreationDate()));
				IconPane iconPane = gef.getIconPane();
				Icon onlineIcon = iconPane.addIcon();
			    onlineIcon.setProperties(online,onlineAL);
				if (archive.getStatus().equals("onLine"))
					arrayLine.addArrayCellIconPane(iconPane);
				else
					arrayLine.addArrayCellText("");
				
				if (flag.equals("admin"))
					arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"checkbox"+archive.getNodePK().getId()+"\" >");
				
			}
			//On range tout...
			frame.addTop(arrayPane.print());
			//Debut du code HTML
			bodyPart+=tabbedPane.print();
			bodyPart+=frame.print();	
		
			window.addBody(bodyPart);
			out.println(window.print());		
		}break;
		// gestion des listes de titre de l'achive sélectionnée
		case 1 :
		{
		
			//La description du journal
			//Si admin => descriptions modifiables, Si publieur => non modifiable
			String top ="";
			if (flag.equals("admin")) 
			{				
				//Bouton valider
				ButtonPane lebouton = gef.getButtonPane();
				Button validerButton = gef.getFormButton(generalMessage.getString("GML.validate"), "javascript:onClick=updateTitle('"+news.getArchiveId()+"')", false);
				lebouton.addButton(validerButton);
				lebouton.setHorizontalPosition();

				top+="<center>";
				top+="<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor>\n";
				top+="\t<tr>\n";
				top+="\t\t<td CLASS=intfdcolor4 NOWRAP>\n";
				top+="\t\t\t<table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH=\"100%\">\n";
				top+="\t\t\t\t<tr>\n";
			  top+="\t\t\t\t<td width=\"200\" class=\"txtlibform\">";
			  top+=Encode.javaStringToHtmlString(news.getString("nodeName"))+" :</td>\n";
				top+="\t\t\t\t<td>\n"; 
				//Le titre du journal
			  top+="\t\t\t\t<input type=\"text\" name=\"Title\" size=\"30\" maxlength=\"30\" value=\""+Encode.javaStringToHtmlString(archiveDetail.getName())+"\">&nbsp;\n";
			  top+="\t\t\t\t<img border=\"0\" src=\""+mandatoryField+"\" width=\"5\" height=\"5\">";
				top+="\t\t\t\t</td></tr>\n";
			 	top+="\t\t\t\t<tr valign=\"top\">\n"; 
			  top+="\t\t\t\t<td class=\"txtlibform\">";
			  top+=Encode.javaStringToHtmlString(news.getString("nodeDescription"))+" :</td>\n";
			  top+="\t\t\t\t<td>\n"; 
			  top+="\t\t\t\t<textarea name=\"Description\" cols=\"40\" rows=\"7\" wrap=\"VIRTUAL\">";
				//la description du journal
				top+=Encode.javaStringToHtmlString(archiveDetail.getDescription());
				//
				top+=" </textarea>\n";
			  top+="\t\t\t\t</td>\n";
				top+="\t\t\t\t</tr>\n";
				top+="\t\t\t\t<tr>\n";
			  top+="\t\t\t\t<td colspan=\"2\">";
				top+="( <img border=\"0\" src=\""+mandatoryField+"\" width=\"5\" height=\"5\"> = "+generalMessage.getString("GML.requiredField")+" )";
				top+="\t\t\t\t</td>\n";
				top+="\t\t\t\t</tr>\n";
				top+="\t\t\t</table>\n";
				top+="\t\t</td>\n";
				top+="\t</tr>\n";
				top+="</table>\n";
				top+="<br>";
				top+=lebouton.print();
				top+="<br>";
				top+="</center>";

			}else
			{
				top+="<table width=\"100%\" border=\"0\" cellspacing=\"5\" cellpadding=\"0\">\n";
				top+="<tr valign=\"top\"\n>"; 
				top+="<tr><td class=\"headline\">"+Encode.javaStringToHtmlString(archiveDetail.getName())+"</td></tr>"; 
				top+="<tr><td class=\"headlinebody\">"+Encode.javaStringToHtmlString(archiveDetail.getDescription())+"</td></tr>"; 
				top+="</tr></table>\n";
			}		
			//Le tableau de tri contenant tous les journaux
			ArrayPane arrayPane = gef.getArrayPane("titleList", "manageNews.jsp?Action=SelectArchive&ArchiveId="+news.getArchiveId(), request, session);
			//Définition des colonnes du tableau
		    arrayPane.addArrayColumn(titreAP);
		    if (flag.equals("admin"))
		    {
		    	ArrayColumn arrayColumnDel = arrayPane.addArrayColumn("&nbsp;");
				arrayColumnDel.setSortable(false);
			}
			Iterator i = archiveDetail.getChildrenDetails().iterator();
			while (i.hasNext()) 
			{
				ArrayLine arrayLine = arrayPane.addArrayLine();	
				NodeDetail archive = (NodeDetail) i.next();
				
				arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(archive.getName()),"javaScript:selectTitle('"+archive.getNodePK().getId()+"')");
				//arrayLine.addArrayCellText(archive.getCreationDate());
				//IconPane iconPane = gef.getIconPane();
				//arrayLine.addArrayCellIconPane(iconPane);
				if (flag.equals("admin"))								
					arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"checkbox"+archive.getNodePK().getId()+"\" >");
			}
		
			//On range tout...
			frame.addTop(top);
			frame.addBottom(arrayPane.print());

			//Debut du code HTML
			bodyPart+=tabbedPane.print();
			bodyPart+=frame.print();	
		
			window.addBody(bodyPart);
			out.println(window.print());
		}break;
		case 2 :
		{
			// noting to do
		
		}break;
		case 3 :
		{
			//nothing to do
		
		}break;
	}	







%>



  <input type="hidden" name="Action">
  <input type="hidden" name="TitleId">

  <input type="hidden" name="FavoritId">
  <input type="hidden" name="ArchiveId">
  <input type="hidden" name="Language">
</FORM>

<FORM NAME="newsForm" ACTION="newsEdito.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>

<FORM NAME="manageArticlesForm" ACTION="manageArticles.jsp" METHOD=POST >
  <input type="hidden" name="Action">
  <input type="hidden" name="TitleId">
</FORM>

<FORM NAME="publicationEditForm" ACTION="publicationEdit.jsp" METHOD=POST ENCTYPE="multipart/form-data">
  <input type="hidden" name="PublicationId">
  <input type="hidden" name="Action">
</FORM>
 <FORM NAME="publishForm" ACTION="publishNews.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>

</BODY>
</HTML>