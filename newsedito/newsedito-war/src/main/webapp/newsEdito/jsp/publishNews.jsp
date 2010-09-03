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
<%@ include file="imports.jsp" %>
<%@ include file="declarations.jsp.inc" %>
<%@ include file="titleUtils.jsp.inc" %>
<%@ include file="publicationUtils.jsp.inc" %>
<%!

  Hashtable selectedPublications ;
  Vector selectedIds;//to maitain order
  Collection availablePublications;

%>

<%
  	if (action == null)
    	action = "Publish";
  	  	
  	if (action.equals("Publish"))
	{  	
  		news.selectFirstArchive();
		selectedPublications = new Hashtable();	
		selectedIds = new Vector();
		  	
		Collection editoList = null;
	  	try 	{
			editoList = news.getArchivePublicationDetails();
		}
	   	catch	(NewsEditoException e) {
			SilverTrace.error("NewsEdito", "publishNews_JSP", "NewsEdito.EX_PROBLEM_TO_GET_ARCHIVE",e);
		}   
	  
	  	if (editoList!= null)
	  	{
		  	Iterator i = editoList.iterator();
		
		  	while	(i.hasNext()) {
		  		Object element = i.next();
				if (element instanceof PublicationDetail)
				{
					PublicationDetail pub = (PublicationDetail) element;
					
					selectedPublications.put(pub.getPK().getId(),pub);
					selectedIds.addElement(pub.getPK().getId());
					
				}
			}
		}		
		
		availablePublications = null;
		action = "Publish";
  	
  	}else if (action.equals("SelectArchive")) {
    	String archiveId = (String) request.getParameter("ArchiveId");
    	news.setArchiveId(archiveId);
    	
    	
    	selectedPublications = new Hashtable();	
		selectedIds = new Vector();
		  	
		Collection editoList = null;
	  	try 	{editoList = news.getArchivePublicationDetails();}
	   	catch	(NewsEditoException e) {
			SilverTrace.error("NewsEdito", "publishNews_JSP", "NewsEdito.EX_PROBLEM_TO_GET_ARCHIVE",e);
		}   
	  
	  	Iterator i = editoList.iterator();
	
	  	while	(i.hasNext()) {
	  		Object element = i.next();
			if (element instanceof PublicationDetail)
			{
				PublicationDetail pub = (PublicationDetail) element;
				
				selectedPublications.put(pub.getPK().getId(),pub);
				selectedIds.addElement(pub.getPK().getId());
				
			}
		}
		
		
		availablePublications = null;
    	
    	action = "Publish";
  	}else if (action.equals("SelectTitle"))
    {
		String titleId = (String) request.getParameter("TitleId");
		news.setTitleId(titleId);
		availablePublications = news.getTitlePublicationDetails();
		action = "Publish";
    }
    else if (action.equals("RemovePublication"))
    {
  		String publicationId=request.getParameter("PubId");
  		selectedPublications.remove(publicationId);
    	int i = 0;
    	while (! ((String) selectedIds.elementAt(i)).equals(publicationId))
    		i++;
    	selectedIds.removeElementAt(i);
  		news.removePublicationFromEditorial(publicationId);
  		action = "Publish";
    }
    else if (action.equals("AddPublication"))
    {
		String publicationId=request.getParameter("PubId");
		boolean alreadyIn = (selectedPublications.get(publicationId)!=null );
		
		if (!alreadyIn) 
		{
        	PublicationDetail publicationToAdd = news.getPublicationDetail(publicationId);			
			selectedPublications.put(publicationId,publicationToAdd);
        	selectedIds.addElement(publicationId);
        
        	//int max = (new Integer(settings.getString("pdfDescriptionMaxLength"))).intValue();
			//if (pubDetail.getDescription().length() < max )
			//	publicationToAdd.getDescription();
			//else
			//	newDescriptions[pubNumber] = pubDetail.getDescription().substring(0, max) + "...";
			news.addPublicationToEditorial(publicationId);
		}
  		
  		
  		action = "Publish";
    }
    
    else if (action.equals("RemovePublications"))
  	{
      	Collection pubList =null;
      	try {pubList = news.getArchivePublicationDetails();}
	   	catch	(NewsEditoException e) {
			SilverTrace.error("NewsEdito", "publishNews_JSP", "NewsEdito.EX_PROBLEM_TO_GET_ARCHIVE",e);
		}   
      	
      	if (pubList!=null)
      	{
	        Iterator i = pubList.iterator();
	        while (i.hasNext()) 
	        {
	        	PublicationDetail publication = (PublicationDetail) i.next();
	            if (request.getParameter("checkbox"+publication.getPK().getId())!=null && request.getParameter("checkbox"+publication.getPK().getId()).equals("on"))
	            	news.removePublicationFromEditorial(publication.getPK().getId());
	  		}
		}  		
  		action = "Publish";
  	}else if (action.equals("SetOnLine"))
	{	
		news.updateTitle(news.getArchiveId(),archiveDetail.getName(),archiveDetail.getDescription(),archiveDetail.getModelId(),"onLine");
		action = "Publish";
	}
	else if (action.equals("SetOffLine"))
	{
		news.updateTitle(news.getArchiveId(),archiveDetail.getName(),archiveDetail.getDescription(),archiveDetail.getModelId(),"offLine");
		action = "Publish";
	}
	else if (action.equals("SetModelId"))
	{
		String modelId = request.getParameter("ModelId");
		news.updateTitle(news.getArchiveId(),archiveDetail.getName(),archiveDetail.getDescription(),modelId,archiveDetail.getStatus());
		action = "Publish";
	}



%>
<%@ include file="init.jsp.inc" %>

<%

GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
String titleAdd=iconsPath+"/util/icons/publicationAdd.gif";
String belpou=iconsPath+"/util/icons/basket.gif";
String up=iconsPath+"/util/icons/arrow/smallup.gif";
String down=iconsPath+"/util/icons/arrow/smalldown.gif";
String del=iconsPath+"/util/icons/smalldel.gif";

%>

<HTML>
<HEAD>

<% out.println(gef.getLookStyleSheet()); %>

<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<SCRIPT language="javaScript">

function doConsult(){
    document.newsForm.Action.value = "Consult";
    document.newsForm.submit();	

}
function doOrganize(){
    document.manageForm.Action.value = "Organize";
    document.manageForm.submit();
}
function doPublish(){
    document.publishForm.Action.value = "Publish";
    document.publishForm.submit();
}
function doSetOnLine(b){
    if (b)
    	document.publishForm.Action.value = "SetOnLine";
    else 
    	document.publishForm.Action.value = "SetOffLine";
    document.publishForm.submit();
}
function doSetModelId(modelId){

    document.publishForm.Action.value = "SetModelId";
    document.publishForm.ModelId.value = modelId;
    document.publishForm.submit();
}
function selectArchive(archiveId)
{
    document.publishForm.Action.value = "SelectArchive";
    document.publishForm.ArchiveId.value = archiveId;
    document.publishForm.submit();
}
function selectTitle(titleId)
{
    document.publishForm.Action.value = "SelectTitle";
    document.publishForm.TitleId.value = titleId;
    document.publishForm.submit();
}

function addPublication(publicationId)
{
    document.publishForm.Action.value = "AddPublication";
    document.publishForm.PubId.value = publicationId;
    document.publishForm.submit();
}
function removePublication(publicationId)
{
    document.publishForm.Action.value = "RemovePublication";
    document.publishForm.PubId.value = publicationId;
    document.publishForm.submit();
}
function publicationDeleteGroup(){

   document.publishForm.Action.value = "RemovePublications";
   document.publishForm.submit();
}
</SCRIPT>
</HEAD>
<BODY>
<%
	Window window = gef.getWindow();
	String bodyPart="";

	// La barre de naviagtion
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(news.getComponentLabel(),"newsEdito.jsp");
	browseBar.setDomainName(news.getSpaceLabel());		  
	if (!navigationString.equals(""))
		browseBar.setPath(setInLineBB+" > "+navigationString);
	else
		browseBar.setPath(setInLineBB);


	//Les onglets
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(consultationTP, "javaScript:doConsult()", false);
    
    if (flag.equals("publisher") || flag.equals("admin"))
    	tabbedPane.addTab(organisationTP, "javaScript:doOrganize()",false );
	
	if (flag.equals("admin"))
		tabbedPane.addTab(inLineSettingTP, "javaScript:doPublish()", true);


	//Le cadre
	Frame frame = gef.getFrame();

	//debut du code HTML
	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
%>
<FORM NAME="publishForm" ACTION="publishNews.jsp" METHOD=POST >
<center>
<TABLE CELLPADDING=1 CELLSPACING=0 BORDER=0 WIDTH="98%">
	<TR>
		<TD><!--Container-->
			<table cellpadding=2 cellspacing=1 border=0 width="100%"  bgcolor=000000>
				<tr> 
					<td class=intfdcolor align=center nowrap width="100%" height="24"> 
						<%
						if (detailLevel >0 ){
							displayArchiveSelect(out, archiveDetail.getNodePK().getId(),archives, true,news,settings);
						%>
					</td>
				</tr>
			</table>
		</td>
		<td width="100%">&nbsp;</td>
		</td>
	</tr>
</table>
<br>
<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr>
		<td CLASS=intfdcolor4 NOWRAP>
			<table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
				<tr>			
					<td valign="baseline" align=left  class="txtlibform">
						<%=stateRBT%>
					</td>
					<td align=left valign="baseline">
						<input type="radio" name="online" value="radiobutton" onclick="javaScript:doSetOnLine(false)" <%if (!archiveDetail.getStatus().equals("onLine")) out.print("checked");%>> <%=notOnLineRB%>
						<br>
						<input type="radio" name="online" value="radiobutton" onclick="javaScript:doSetOnLine(true)" <%if (archiveDetail.getStatus().equals("onLine")) out.print("checked");%>> <%=onLineRB%>
					</td>
					<td valign="baseline" align=left  class="txtlibform">
						<%=modelRBT%>
					</td>
					<td align=left valign="baseline">
						<input type="radio" name="model" value="radiobutton" onclick="javaScript:doSetModelId('1')" <%if (archiveDetail.getModelId().equals("1")) out.print("checked");%> > <%=model1RB%>
						<br>
						<input type="radio" name="model" value="radiobutton" onclick="javaScript:doSetModelId('2')" <%if (archiveDetail.getModelId().equals("2")) out.print("checked");%>>	<%=model2RB%>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
<br><br>
<table CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="98%" cellpadding="0">
  <tr> 
    <td colspan="5">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
	<tr>
    <td width="70%" valign="top"> 
				<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="100%" CLASS=intfdcolor>
					<tr>
						<td CLASS=intfdcolor4 NOWRAP>
            <table width="100%" border="0" cellspacing="1" cellpadding="3">
              <tr> 
                <td width="25%" valign="top"> 

			      <table width="100%" border="0" cellspacing="1" cellpadding="3" class="intfdcolor1">
			        <tr> 
			
			
					<td class="intfdcolor"><span class="txtpetitblanc"> 
			        <%
			        	out.print(archiveDetail.getName());
			        %>
			       </span></td>
			 
			        </tr>
			
				    <%				        
				    	displayTitleList(out, news.getTitleId(),archiveDetail,"txtnote","txtnote", news, settings);
				
					%>
			
			        <tr> 
			          <td class="intfdcolor4"> 
			            <p>&nbsp;</p>
			            <p>&nbsp;</p>
			          </td>
			        </tr>

				</table>

                
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
					// nothing to do
				}
				break;
				case 2 :
				{
		
					if (availablePublications!=null)
						displayPublicationList(out,availablePublications,news,false,"addPublication","textePetitBold","txtnote");			
		
				}
				break;
			}

%>
                  <p>&nbsp;</p>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
    <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td valign="top"> 
      <table width="100%" border="0" cellspacing="0" cellpadding="0" height="250">
        <tr> 
          <td height="245" valign="top">
            <table width="100%" border="0" cellspacing="1" cellpadding="3" class="intfdcolor1" height="250">
              <tr class="intfdcolor1" valign="top"> 
                <td height="5" class="txtGrandBlanc"><%=news.getString("editorial")%></td>
              </tr>
              <tr class="intfdcolor4" valign="top"> 
                <td>

<%             

	{
		Vector publist = new Vector();
		Iterator i = selectedIds.iterator();
		while (i.hasNext())
		{
			publist.addElement(selectedPublications.get(i.next()));
		}
		if (publist!=null)
			displayPublicationList(out,publist,news,false,"removePublication","textePetitBold","txtnote");			
	}

%>
				</td>
              </tr>
            </table>
          </td>
        </tr>
        <tr height="5"> 
          <td valign="bottom" class="txtnote"><br>
            <br>
			<%=emptyEditorialME%></td>
        </tr>
      </table>
    </td>
  </tr>
</table>

<%
	}
%>

</td></tr></table>
</CENTER>

<%
	
	out.println(frame.printAfter());
	//out.println(window.printAfter());
%>
 
  <input type="hidden" name="Action">
  <input type="hidden" name="ArchiveId">
   <input type="hidden" name="ModelId">
  <input type="hidden" name="TitleId">
  <input type="hidden" name="PubId">
</FORM> 

<FORM NAME="manageForm" ACTION="manageNews.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>
</FORM>

<FORM NAME="newsForm" ACTION="newsEdito.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>

</BODY>
</HTML>