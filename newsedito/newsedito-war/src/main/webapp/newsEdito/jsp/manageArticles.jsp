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
    "http://repository.silverpeas.com/legal/licensing"

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
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ include file="publicationUtils.jsp.inc" %>

<%
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL"); 
%>

<HTML>
<HEAD>
<%out.println(gef.getLookStyleSheet()); %>

<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<script type="text/javascript" src="../../util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="../../util/javaScript/animation.js"></script>
<Script language="JavaScript">

//onglets
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

//operations
function addPublication(){
    document.publicationEditForm.Action.value = "AddPublication";
    document.publicationEditForm.submit();
}

function publicationDeleteGroup(nbObj){
    listeObj = "";

    if (nbObj > 0) {
		if (nbObj == 1) {
			if (document.manageArticlesForm.publicationIds.checked)
				listeObj += document.manageArticlesForm.publicationIds.value + ",";
		}

		else {
        	for (i=0; i<nbObj; i++) {
            	if (document.manageArticlesForm.publicationIds[i] != null) {
                	if (document.manageArticlesForm.publicationIds[i].checked)
                    	listeObj += document.manageArticlesForm.publicationIds[i].value + ",";
            	}
            	else break;
        	}
      	}

		if (listeObj != "") {   //at least one box has been checked
			if (window.confirm("<%=news.getString("supprimerPubsSelectionneesConfirmation")%>")){
	  			document.manageArticlesForm.Action.value = "RemovePublications";
  	  			document.manageArticlesForm.submit();
			}
    	}
    }
}

function copyPublications(nbObj) {
    listeObj = "";

    if (nbObj > 0) {
		if (nbObj == 1) {
			if (document.manageArticlesForm.publicationIds.checked)
				listeObj += document.manageArticlesForm.publicationIds.value + ",";
		}

		else {
        	for (i=0; i<nbObj; i++) {
            	if (document.manageArticlesForm.publicationIds[i] != null) {
                	if (document.manageArticlesForm.publicationIds[i].checked) 
                    	listeObj += document.manageArticlesForm.publicationIds[i].value + ",";
            	}
            	else break;
        	}
      	}

		if (listeObj != "") {   //at least one box has been checked
			document.manageArticlesForm.action = "multicopy.jsp";
			document.manageArticlesForm.target = "IdleFrame";
			document.manageArticlesForm.submit();
    	}
    }
}

function pasteClipboard() {
	<%
	String URLencoded = URLEncoder.encode("manageArticles.jsp?Action=View");
	String responseEncoded = response.encodeURL(URLencoded);
	%>
	top.IdleFrame.document.location.replace('<%=m_context%><%=URLManager.getURL(URLManager.CMP_CLIPBOARD)%>paste?compR=RnewsEdito&SpaceFrom=<%=news.getSpaceId()%>&ComponentFrom=<%=news.getComponentId()%>&JSPPage=<%=responseEncoded%>&TargetFrame=MyMain&message=REFRESH');

}

//boutons
function updateTitle(titleId){

    if (checkString(document.manageArticlesForm.Title,"<%=news.getString("champsObligatoireNonRenseigne")+" "+news.getString("nodeName") %>") )
    {
	    if (!isValidTextArea(document.manageArticlesForm.Description)) {
			  window.alert("<%=news.getString("champsDescriptionTropLong")%>");
		}
		else 
		{    
		    document.manageArticlesForm.Action.value = "UpdateTitle";
    		document.manageArticlesForm.TitleId.value = titleId;
    		document.manageArticlesForm.submit();
		}
	}
}

//appele dans le init.jsp
function selectArchive(archiveId){
    document.manageArticlesForm.Action.value = "SelectArchive";
    document.manageArticlesForm.ArchiveId.value = archiveId;
    document.manageArticlesForm.submit();
}

function selectTitle(titleId){

    document.manageArticlesForm.Action.value = "SelectTitle";
    document.manageArticlesForm.TitleId.value = titleId;
    document.manageArticlesForm.submit();
	
	
}

//appele dans le publicationUtils.jsp
function selectPublication(publicationId){
    document.publicationForm.Action.value = "SelectPublication";
    document.publicationForm.action = "publication";
    document.publicationForm.PublicationId.value = publicationId;
    document.publicationForm.submit();

}


</Script>


</HEAD>

<BODY>


<%
	if (action == null)
    	action = "ManageArticles";
  	
  	if (action.equals("ManageArticles")) {
  		news.selectFirstArchive();
  	}
  	else if (action.equals("SelectArchive")) {
    	String archiveId = (String) request.getParameter("ArchiveId");
    	news.setArchiveId(archiveId);
    	action = "ManageArticles";
  	}
  	else if (action.equals("SelectTitle")) {
    	String titleId = (String) request.getParameter("TitleId");
    	news.initNavigationForNode(titleId);
    	action = "ManageArticles";

  	}
  	else if (action.equals("RemovePublications")) {		
		String Ids[] = request.getParameterValues("publicationIds");
		CompletePublication pub = null;
		
        for (int i = 0; i < Ids.length; i++)
        {
            if (Ids[i] != null) {
                news.removePublication(Ids[i]);
            }
        }
                	
  		action = "ManageArticles";
  		
  	} 
  	else if (action.equals("UpdateTitle")) {
  		String titleId = (String) request.getParameter("TitleId");
  		String title = (String) request.getParameter("Title");
  		String description = (String) request.getParameter("Description");  		
  		news.updateTitle(titleId, title, description);
  		action = "ManageArticles";
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
	
	if (detailLevel > 1 )
	{	
		// l'operationpane
		OperationPane operationPane = window.getOperationPane();
		
		if ((flag.equals("publisher") && !archiveDetail.getStatus().equals("onLine")) 
			|| flag.equals("admin")) {
			
			Collection pubList = news.getTitlePublicationDetails();
			Iterator i = pubList.iterator();
			Object element = null;
			PublicationDetail pub = null;
			int nbObj = 0;
			
			while	(i.hasNext()) {
				element = i.next();
				if (element instanceof PublicationDetail) {	
					pub = (PublicationDetail) element;
					
					if (flag.equals("publisher") && news.getUserId().equals(pub.getCreatorId()) || flag.equals("admin")) 
						nbObj++;
				}
			}			
			
			operationPane.addOperation(settings.getString("addPublicationIcon"),news.getString("ajouterPub"),"javascript:onClick=addPublication()" );									
			operationPane.addOperation(settings.getString("deletePublicationIcon"),deletePublicationsOP ,"javascript:publicationDeleteGroup('"+nbObj+"')");		
			operationPane.addOperation(settings.getString("copyIcon"),news.getString("copierPublications"),"javascript:onClick=copyPublications('"+nbObj+"')");
			operationPane.addOperation(settings.getString("pasteIcon"),news.getString("collerClipboard"),"javascript:onClick=pasteClipboard()" );									
		}

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

	//debut du code HTML
	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
%>
<FORM NAME="manageArticlesForm" ACTION="manageArticles.jsp" METHOD=POST >

<table width="100%" border="0" cellspacing="1" cellpadding="3">

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
		}
		break;
		case 1 :
		{

		}
		break;
		case 2 :
		{

      		//La description du titre
			//Si admin => descriptions modifiables, Si publieur => non modifiable
			
			String top ="";
			if (flag.equals("admin")) 
			{				
				//Bouton valider
				ButtonPane lebouton = gef.getButtonPane();
				Button validerButton = gef.getFormButton(generalMessage.getString("GML.validate"), "javascript:onClick=updateTitle('"+news.getTitleId()+"')", false);
				lebouton.addButton(validerButton);
				lebouton.setHorizontalPosition();

				top+="<center>";
				top+="<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor>\n";
				top+="\t<tr>\n";
				top+="\t\t<td CLASS=intfdcolor4 NOWRAP>\n";
				top+="\t\t\t<table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH=\"100%\">\n";
				top+="\t\t\t\t<tr>\n";
			  top+="\t\t\t\t<td width=\"200\" class=\"txtlibform\">";
			  top+=news.getString("nodeName")+" :</td>\n";
				top+="<td>\n"; 
			  top+="<input type=\"text\" name=\"Title\" size=\"30\" maxlength=\"30\" value=\""+Encode.javaStringToHtmlString(titleDetail.getName())+"\">\n";
				top+="<img border=\"0\" src=\""+mandatoryField+"\" width=\"5\" height=\"5\">";
				top+="</td><td>&nbsp;</td></tr>\n";
			 	top+="<tr valign=\"top\">\n"; 
			  top+="<td class=\"txtlibform\">"+Encode.javaStringToHtmlString(news.getString("nodeDescription"))+" :</td>\n";
			  top+="<td>\n"; 
			  top+="<textarea name=\"Description\" cols=\"40\" rows=\"3\" wrap=\"VIRTUAL\">";
				//la description du journal
				top+=Encode.javaStringToHtmlString(titleDetail.getDescription());
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
			} 
			else {//publisher
				top+="<table width=\"100%\" border=\"0\" cellspacing=\"5\" cellpadding=\"0\">\n";
				top+="<tr valign=\"top\"\n>"; 
				top+="<tr><td class=\"headline\">"+Encode.javaStringToHtmlString(titleDetail.getName())+"</td></tr>"; 
				top+="<tr><td class=\"headlinebody\">"+Encode.javaStringToHtmlString(titleDetail.getDescription())+"</td></tr>"; 
				top+="</tr><tr><td></td></tr></table>\n";
			}
            out.println(top);
            
    		try {
				Collection pubList = news.getTitlePublicationDetails();
				if (pubList!=null)
       				displayPublicationArrayPane(out,pubList,news,request,session);			
		
   			}
	   		catch	(NewsEditoException e) {
				SilverTrace.error("NewsEdito", "manageArticles_JSP", "NewsEdito.EX_PROBLEM_TO_GET_PUBLI_TITLE",e);
			}   
		}
		break;
		case 3 :
		{
			try{
				displayPublication(out, news, news.getCompletePublication(), settings) ;
			}
	   		catch	(NewsEditoException e) {
				SilverTrace.error("NewsEdito", "manageArticles_JSP", "NewsEdito.EX_PROBLEM_TO_GET_PUBLI_TITLE",e);
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
  <input type="hidden" name="PublicationId">
  <input type="hidden" name="FavoritId">
  <input type="hidden" name="ArchiveId">
  <input type="hidden" name="Language">
</FORM>

<FORM NAME="newsForm" ACTION="newsEdito.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>

<FORM NAME="manageForm" ACTION="<%if (flag.equals("publisher")) out.print("manageArticles.jsp"); else out.print("manageNews.jsp");%>" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>

<FORM NAME="publicationEditForm" ACTION="publicationEdit.jsp" METHOD=POST ENCTYPE="multipart/form-data">
  <input type="hidden" name="PublicationId">
  <input type="hidden" name="Action">
</FORM>

<FORM NAME="publicationForm" ACTION="publication.jsp" METHOD=POST >
  <input type="hidden" name="Action">
  <input type="hidden" name="PublicationId">
</FORM>

 <FORM NAME="publishForm" ACTION="publishNews.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>

</BODY>
</HTML>