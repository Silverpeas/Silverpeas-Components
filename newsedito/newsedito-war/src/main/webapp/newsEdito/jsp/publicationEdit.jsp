<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@ include file="newsUtils.jsp.inc" %>
<%@ include file="init.jsp.inc" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page import="com.silverpeas.util.web.servlet.FileUploadUtil" %>
<%!
private ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", "fr");

void displaySelectPublicationModel(JspWriter out, Collection list, NewsEditoSessionController news,
	ResourceLocator settings, ResourceLocator generalMessage, GraphicElementFactory graphicFactory)
  throws NewsEditoException, PublicationTemplateException
{
        SilverTrace.info("NewsEdito", "Publication_JSP.displaySelectPublicationModel", "NewsEdito.MSG_ENTRY_METHOD");
	try{
		  out.println("<TABLE CELLPADDING=5 CELLSPACING=2 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor><TR><TD CLASS=intfdcolor4 NOWRAP align=center>");
		  out.println("<TABLE width=\"400\">");
		  out.println("  <TR>");
		  out.println("    <TD colspan=\"2\">");
		  out.println("			<span class=txtnav>"+news.getString("listeModeles")+"</span>");
		  out.println("    </TD>");
		  out.println("  </TR>");
		  Iterator i = list.iterator();
		  while (i.hasNext()) {
			PublicationTemplate xmlForm = (PublicationTemplate) i.next();
			out.println("  <TR>");
			out.println("    <TD>");
			out.println("		<A HREF=\"javascript:onClick=reallySelectPublicationModel('" +xmlForm.getFileName()+ "')\">");
			out.println(xmlForm.getName());
			out.println("		</A>");
			out.println("    </TD>");
			out.println("    <TD>");
			out.println(xmlForm.getDescription());
			out.println("    </TD>");
			out.println("    <TD>");
			out.println("		<IMG SRC=\""+xmlForm.getThumbnail()+"\">");
			out.println("    </TD>");
			out.println("  </TR>");
		  }

		  out.println("  <TR>");
		  out.println("    <TD colspan=3>");
		  out.println("			<A HREF=\"javascript:onClick=sendToWysiwyg()\">Wysiwyg</A>");
		  out.println("    </TD>");
		  out.println("  </TR>");
			 out.println("</TABLE>");
			out.println("</td></tr></TABLE>");

		  Button button = graphicFactory.getFormButton(news.getString("annuler"),
			  "publication.jsp", false, settings.getString("formButtonIconUrl"));
			out.println("<br><center>");
		  out.println(button.print());
			out.println("</center>");
	}
	catch(Exception e){
		throw new NewsEditoException("publicationEdit_JSP.displaySelectPublicationModel",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_MODEL_SELECTED",e);
	}

}

void showReallySelectPublicationModel(JspWriter out, NewsEditoSessionController news, String modelId, String pubId)

  throws NewsEditoException, IOException
{
        SilverTrace.info("NewsEdito", "Publication_JSP.showReallySelectPublicationModel", "NewsEdito.MSG_ENTRY_METHOD");

	try{
		if (! modelId.equals("wysiwyg") ) {
			// affichage du formulaire XML
			PublicationTemplateImpl pubTemplate = news.setPublicationXmlForm(modelId);
			Form formUpdate = pubTemplate.getUpdateForm();
			RecordSet recordSet = pubTemplate.getRecordSet();
			DataRecord data= recordSet.getEmptyRecord();
			data.setId(pubId);

			PagesContext context = new PagesContext("myForm", "2", news.getLanguage(), false, news.getComponentId(), news.getUserId());

			formUpdate.display(out, context, data);
		}
	}
	catch(Exception e){
		throw new NewsEditoException("publicationEdit_JSP.showReallySelectPublicationModel",NewsEditoException.WARNING,"NewsEdito.EX_PROBLEM_TO_SET_PUBLI",e);
	}
	out.println(news.getString("majModeleEffectuee"));
        SilverTrace.info("NewsEdito", "Publication_JSP.showReallySelectPublicationModel", "NewsEdito.MSG_EXIT_METHOD");
}

void  displayEditPublication(JspWriter out, String fatherId, PublicationDetail pub, String modelId,
          NewsEditoSessionController news, ResourceLocator settings, ResourceLocator generalMessage, GraphicElementFactory graphicFactory, HttpServletRequest request) throws NewsEditoException, IOException
{
 	try{
		out.println("<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor>");
		out.println(" <tr>");
		out.println("  <td CLASS=intfdcolor4 NOWRAP>");

		out.println("  <TABLE WIDTH=\"100%\">");
		out.println("    <TR>");
		out.println("      	<TD class=\"txtlibform\">"+news.getString("pubName")+"</TD>");
		if (pub != null)
			out.println("      <TD><INPUT TYPE=TEXT SIZE=\"50\" NAME=\"Name\" maxlength=" + DBUtil.getTextFieldLength() + " VALUE=\"" +Encode.javaStringToHtmlString(pub.getName())+ "\">");
		else
			out.println("      <TD><INPUT TYPE=TEXT SIZE=\"50\" NAME=\"Name\" maxlength=" + DBUtil.getTextFieldLength() + ">");
		out.println("     		<img border=\"0\" src=\""+settings.getString("mandatoryField")+"\" width=\"5\" height=\"5\">");
		out.println("		</TD>");
		out.println("    </TR>");
		out.println("    <TR valign=\"top\">");
		out.println("      <TD class=\"txtlibform\">"+news.getString("pubDescription")+"</TD>");
		if (pub != null)
			out.println("      <TD><TEXTAREA ROWS=\"6\" COLS=\"50\" NAME=\"Description\" wrap=\"virtual\">" + Encode.javaStringToHtmlString(pub.getDescription()) + "</TEXTAREA>");
		else
			out.println("      <TD><TEXTAREA ROWS=\"6\" COLS=\"50\" NAME=\"Description\" wrap=\"virtual\"></TEXTAREA>");

		out.println("     </TD>");
		out.println("    </TR>");

		if (modelId != null) {
			out.println("    <TR>");
			out.println("      <TD align=\"center\" colspan=\"2\">");
			displayEditXmlModel(out, modelId, pub.getPK().getId(), news, request);
			out.println("     </TD>");
			out.println("    </TR>");
		}

		out.println("	   <TR></TR>");
		out.println("	   <TR><TD colspan=\"2\">( <img border=\"0\" src=\""+mandatoryField+"\" width=\"5\" height=\"5\"> = "+news.getString("champsObligatoire")+" )</TD></TR>");
		out.println("    </TABLE>");
		out.println("</TD></TR></TABLE>");
		out.println("<br>");

		Button button;
		if (pub == null)
		{
			button = graphicFactory.getFormButton(generalMessage.getString("GML.validate"),
			  "javascript:onClick=reallyAddPublication()", false, settings.getString("formButtonIconUrl"));
		}
		else
		{
			button = graphicFactory.getFormButton(generalMessage.getString("GML.validate"),
			  "javascript:onClick=reallyUpdatePublication()", false, settings.getString("formButtonIconUrl"));
		}

		Button button2;
		if (pub == null)
		{
			button2 = graphicFactory.getFormButton(generalMessage.getString("GML.cancel"),
		 	  "manageArticles.jsp?Action=SelectTitle&TitleId="+news.getTitleId(), false, settings.getString("formButtonIconUrl"));
		}
		else
		{
			button2 = graphicFactory.getFormButton(generalMessage.getString("GML.cancel"),
			  "publication.jsp", false, settings.getString("formButtonIconUrl"));
		}

		ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton(button);
		buttonPane.addButton(button2);
		out.println(buttonPane.print());
		out.println("  <input type=\"hidden\" name=\"FatherId\" value=" + fatherId +">");
	}
	catch(Exception e){
		throw new NewsEditoException("publicationEdit_JSP.displayEditPublication",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_PUBLI",e);
	}
    SilverTrace.info("NewsEdito", "Publication_JSP.displayEditPublication", "NewsEdito.MSG_EXIT_METHOD");
}

void displayEditXmlModel(JspWriter out, String modelId, String pubId, NewsEditoSessionController news, HttpServletRequest request)
		  throws NewsEditoException
		{
			SilverTrace.info("NewsEdito", "Publication_JSP.displayEditXmlModel", "NewsEdito.MSG_ENTRY_METHOD");
			try
			{
				// formulaire XML
				if (StringUtil.isDefined(modelId))
				{
					out.flush();
					Form 				formUpdate 	= (Form) request.getAttribute("Form");
					DataRecord 			data 		= (DataRecord) request.getAttribute("Data");
					if (formUpdate != null)
					{
						PagesContext 		context 	= new PagesContext("myForm", "0", news.getLanguage(), false, news.getComponentId(), news.getUserId());
						context.setObjectId(pubId);
						context.setBorderPrinted(false);

						formUpdate.display(out, context, data);
				    }
				}
			}
			catch(Exception e){
				throw new NewsEditoException("publicationEdit_JSP.displayEditXmlModel",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_INFO_MODEL",e);
			}
		}

%>


<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<script type="text/javascript" src="../../util/javaScript/checkForm.js"></script>
<Script language="JavaScript">

function checkModelData() {
    for (var i=0; i<document.publicationEditForm.length; i++) {
        if (document.publicationEditForm.elements[i].name.substring(0, 8) == "WATXTVAR") {
            if (!isValidTextMaxi(document.publicationEditForm.elements[i])) {
                  document.publicationEditForm.elements[i].select();
									return 0;
            }
        }
    }
    return 1;
}

function reallyUpdatePublication()
{
	if (checkString(document.publicationEditForm.Name,"<%=news.getString("champsObligatoireNonRenseigne")+" "+news.getString("pubName") %>") )
    {
		if (!isValidTextArea(document.publicationEditForm.Description)) {
			  window.alert("<%=news.getString("champsDescriptionTropLong")%>");
		} else {
			if (checkModelData() == 0) {
					window.alert("<%=news.getString("champsModelTropLong")%>");
			} else	{
					document.publicationEditForm.action = "ReallyUpdatePublication";
					document.publicationEditForm.submit();
			}
		}
	}
}

function showSelectPublicationModel()
{
	document.publicationEditForm.action = "ListModels";
	document.publicationEditForm.submit();
}

function reallyAddPublication()
{
	if (checkString(document.publicationEditForm.Name,"<%=news.getString("champsObligatoireNonRenseigne")+" "+news.getString("pubName") %>") )
    {
		if (!isValidTextArea(document.publicationEditForm.Description)) {
			  window.alert("<%=news.getString("champsDescriptionTropLong")%>");
		}
	   else {
			if (checkModelData() == 0) {
				window.alert("<%=news.getString("champsModelTropLong")%>");
			} else {
			  document.publicationEditForm.Action.value = "ReallyAddPublication";
				document.publicationEditForm.submit();
			}
		}
	}
}

function reallySelectPublicationModel(modelId)
{
    document.publicationEditForm.Action.value = "ReallySelectPublicationModel";
    document.publicationEditForm.ModelId.value = modelId;
    document.publicationEditForm.submit();
}

function doConsult(){
    document.newsForm.Action.value = "Consult";
    document.newsForm.submit();

}

function doOrganize(){
    document.manageForm.Action.value = "Organize";
    document.manageForm.submit();
}

function doSetInLine(){
    document.publishForm.Action.value = "Publish";
    document.publishForm.submit();
}

function sendToWysiwyg() {
    document.toWysiwyg.submit();
}

function UpdateXMLForm()
{
	document.myForm.submit();
}

</script>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5>


<%
action = "";
String name = "";
String description = "";
String modelId = "";
String modelName = "";
List items = FileUploadUtil.parseRequest(request);

String infoId = null;
int textCount = 0;
int imageCount = 0;
ArrayList textDetails = new ArrayList();
ArrayList imageDetails = new ArrayList();
String theText = null;
int textOrder = 0;
int imageOrder = 0;
String logicalName = "";
String physicalName = "";
long size = 0;
String type = "";
String mimeType = "";
File dir = null;
InfoPK infoPK = null;
String errorFiles = null;
Iterator itemIter = items.iterator();
while (itemIter.hasNext()) {
  FileItem item = (FileItem) itemIter.next();
  if (item.isFormField())
  {
    String mpName = item.getFieldName();
    if ("Action".equals(mpName))
        action = item.getString();
    else if ("Name".equals(mpName))
        name = item.getString();
    else if ("Description".equals(mpName))
        description = item.getString();
    else if ("InfoId".equals(mpName)) {
        infoId = item.getString();
        infoPK = new InfoPK(infoId);
    }
    else if ("ModelId".equals(mpName))
        modelId =item.getString();
    else if ("TextCount".equals(mpName))
        textCount = new Integer(item.getString()).intValue();
    else if ("ImageCount".equals(mpName))
        imageCount = new Integer(item.getString()).intValue();
    else if (mpName.startsWith("WATXTVAR")) {
        theText = item.getString();
        textOrder = new Integer(mpName.substring(8, mpName.length())).intValue();
        textDetails.add(new InfoTextDetail(infoPK, new Integer(textOrder).toString(), "?", theText));
    }
  } else  {
    // it's a file part
    logicalName = item.getName();
    if (logicalName != null) {
			type = logicalName.substring(logicalName.lastIndexOf(".")+1, logicalName.length()).toLowerCase();

	    physicalName = new Long(new Date().getTime()).toString() + "." +type;
	    mimeType = item.getContentType();
	    dir = new File(FileRepositoryManager.getAbsolutePath(news.getComponentId())+settings.getString("imagesSubDirectory")+ File.separator +physicalName);
	    if ("gif".equals(type) || "jpg".equals(type) || "jpeg".equals(type)) {
	      // the part actually contained a file
	      FileUploadUtil.saveToFile(dir, item);
        size = item.getSize();
				if (size > 0) {
		      imageOrder++;
		      imageDetails.add(new InfoImageDetail(infoPK, new Integer(imageOrder).toString(), null, physicalName, logicalName, "", mimeType, size));
				}
				else {
					if (errorFiles != null)
						errorFiles += "<BR>" + news.getString("fichierIntrouvable") + " : " +  logicalName;
					else
						errorFiles = news.getString("fichierIntrouvable") + " : " +  logicalName;
				}

	    } else {
	      // the field did not contain a file
				if (errorFiles != null)
					errorFiles += "<BR> " + news.getString("pasFichierImage") + " : " + logicalName;
				else
					errorFiles = news.getString("pasFichierImage") + " : " + logicalName;
	    }
    }
    //out.flush();
  }
}

if (action.equals("ReallyAddPublication")) {
    try {
	  if (errorFiles != null)
		throw new CreateNewsEditoException("publicationEdit_JSP", NewsEditoException.WARNING, "NewsEdito.EX_ERRORFILE_NOT_EMPTY",errorFiles);

      news.createPublication(name, description);
      out.println("<BODY onLoad=window.location.replace(\"publication.jsp\")>");
      out.println("</BODY>");
    } catch (CreateNewsEditoException e) {
      out.println("<BODY>");
      displayGoBackBanner(out, e.getMessage(),
        "<A HREF=\"javascript:onClick=history.back()\">"+
        news.getString("retour") +
        "</A>");
      out.println("</BODY>");
    }

  }

  else if (action.equals("ReallyUpdatePublication")) {
    try {
		if (errorFiles != null)
				throw new CreateNewsEditoException("publicationEdit_JSP", NewsEditoException.WARNING, "NewsEdito.EX_ERRORFILE_NOT_EMPTY",errorFiles);

      news.updatePublication(name, description);

      out.println("<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 onLoad=window.location.replace(\"publication.jsp\")>");
      out.println("</BODY>");
    }
    catch (CreateNewsEditoException e) {
      out.println("<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5>");
      displayGoBackBanner(out, e.getMessage(),
        "<A HREF=\"javascript:onClick=history.back()\">"+
        news.getString("retour") +
        "</A>");
      out.println("</BODY>");
    }
  }

  else {

	Window window = gef.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(news.getComponentLabel(),"newsEdito.jsp");
	browseBar.setDomainName(news.getSpaceLabel());
	browseBar.setPath(ajoutEditionPublicationBB);

	OperationPane operationPane = window.getOperationPane();
  	if (action.equals("UpdatePublication")) {
		CompletePublication pubComplete = news.getCompletePublication();
		if (WysiwygController.haveGotWysiwyg(pubComplete.getPublicationDetail().getPK().getSpace(), pubComplete.getPublicationDetail().getPK().getComponentName(), pubComplete.getPublicationDetail().getPK().getId())) {
			operationPane.addOperation(settings.getString("updatePublicationIcon"), news.getString("modifierPubContent"), "javascript:onClick=sendToWysiwyg()" );
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

	out.println(window.printBefore());
	out.println(tabbedPane.print());

	out.println(frame.printBefore());


%>

<FORM NAME="publicationEditForm" ACTION="publicationEdit.jsp" METHOD=POST ENCTYPE="multipart/form-data">
<center>

<%

  if (action.equals("SelectPublicationModel")) {
    //showSelectPublicationModel(out, news, settings, generalMessage, gef);
  }
  else if (action.equals("ReallySelectPublicationModel")) {
    showReallySelectPublicationModel(out, news, modelId, news.getCompletePublication().getPublicationDetail().getPK().getId());
    action = "UpdatePublication";
  }
  if (action.equals("UpdatePublication")) {
    CompletePublication complete = news.getCompletePublication();
    displayEditPublication(out, news.getTitleId(), complete.getPublicationDetail(),
              complete.getPublicationDetail().getInfoId(),
	      news, settings, generalMessage, gef, request);
  }
  else
  if (action.equals("AddPublication")) {
    displayEditPublication(out, news.getTitleId(), null, null, news, settings, generalMessage, gef, request);
  }

%>
  <input type="hidden" name="Action">
  <input type="hidden" name="ModelId">

</center>
</FORM>


<FORM NAME="manageForm" ACTION="manageNews.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>

 <FORM NAME="publishForm" ACTION="publishNews.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>


<FORM NAME="newsForm" ACTION="newsEdito.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>


<FORM name="toWysiwyg" Action="../../wysiwyg/jsp/htmlEditor.jsp" method="Post">
  <input type="hidden" name="SpaceId" value="<%=news.getSpaceId()%>">
  <input type="hidden" name="SpaceName" value="<%=news.getSpaceLabel()%>">
  <input type="hidden" name="ComponentId" value="<%=news.getComponentId()%>">
  <input type="hidden" name="ComponentName" value="<%=news.getComponentLabel()%>">
  <input type="hidden" name="BrowseInfo" value="Edition Wysiwyg">
  <input type="hidden" name="ObjectId" value="<%=news.getPublicationId()%>">
  <input type="hidden" name="Language" value="<%=news.getLanguage()%>">
  <input type="hidden" name="ReturnUrl" value="../..<%=news.getComponentUrl()%>publication.jsp?Action=SelectPublication&PublicationId=<%=news.getPublicationId()%>">
</FORM>

<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>


</BODY>
<%
  }//end of actions that needs to display pub
%>
</HTML>