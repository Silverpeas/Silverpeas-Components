<%@ include file="checkKmelia.jsp" %>
<%@ include file="tabManager.jsp.inc" %>

<%!
	// Fonction de decodage html
  	String HTMLUncode(String pstr) {
    	String res="";
    	int begin = 0;
    	int end = 0;
    	if (pstr==null) return res;
    	end = pstr.indexOf("<BR>", begin);
    	while(end != -1) {
        	res += pstr.substring(begin, end) + '\n';
        	begin = end + 4;
        	end = pstr.indexOf("<BR>", begin);
    	}
    	res += pstr.substring(begin, pstr.length());
    	return res;
	}
%>

<%


boolean isOwner = false;
if (kmeliaScc.getSessionOwner())
	isOwner = true;

String linkedPathString = kmeliaScc.getSessionPath();

ModelDetail 		modelDetail 		= (ModelDetail) request.getAttribute("ModelDetail");
CompletePublication	completePublication	= (CompletePublication) request.getAttribute("CompletePublication");
Boolean				bImageTrouble		= (Boolean) request.getAttribute("ImageTrouble");
String				wizardLast			= (String) request.getAttribute("WizardLast");
String 				wizard				= (String) request.getAttribute("Wizard");
String	 			wizardRow			= (String) request.getAttribute("WizardRow");
String				currentLang 		= (String) request.getAttribute("Language");

boolean imageTrouble = false;
if (bImageTrouble != null)
	imageTrouble = bImageTrouble.booleanValue();

if (modelDetail == null)
	modelDetail = completePublication.getModelDetail();

if (wizardRow == null)
	wizardRow = "2";

boolean isEnd = false;
if ("2".equals(wizardLast))
	isEnd = true;

InfoDetail 	infos 	= completePublication.getInfoDetail();
String 		pubId 	= completePublication.getPublicationDetail().getPK().getId();
String 		pubName = completePublication.getPublicationDetail().getName(currentLang);

//Icons
String alertSrc = m_context + "/util/icons/alert.gif";

Button cancelButton 	= (Button) gef.getFormButton(resources.getString("GML.cancel"), "ViewPublication", false);
Button sendUpdateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendModelData()", false);
%>
<HTML>
<HEAD>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javascript">
function sendModelData() {
    ok = 1;
    for (var i=0; i<document.modelForm.length; i++) {
        if (document.modelForm.elements[i].name.substring(0, 8) == "WATXTVAR") {
            if (!isValidTextMaxi(document.modelForm.elements[i])) {
                  document.modelForm.elements[i].select();
                  window.alert("<%=resources.getString("TheSelectedField")%> <%=resources.getString("ContainsTooLargeText")%> <%=DBUtil.TextMaxiLength%> <%=resources.getString("Characters")%>");
                  ok = 0;
                  document.modelForm.elements[i].select();
            }
        }
    }
    if (ok == 1) {
        document.modelForm.submit();
    }
}

function topicGoTo(id) {
	closeWindows();
	location.href="GoToTopic?Id="+id;
}

function closeWindows() {
    if (window.publicationWindow != null)
        window.publicationWindow.close();
}
</script>
</HEAD>
<BODY onUnload="closeWindows()">
<%
      Window 	window 	= gef.getWindow();
      Frame 	frame 	= gef.getFrame();
      Board		board	= gef.getBoard();
      Board boardHelp = gef.getBoard();

      BrowseBar browseBar = window.getBrowseBar();
      browseBar.setDomainName(spaceLabel);
      browseBar.setComponentName(componentLabel, "Main");
      browseBar.setPath(linkedPathString);
	  browseBar.setExtraInformation(pubName);

      OperationPane operationPane = window.getOperationPane();
      operationPane.addOperation(alertSrc, resources.getString("GML.notify"), "javaScript:onClick=goToOperationInAnotherWindow('ToAlertUser', '"+pubId+"', 'ViewAlert')");

      // définition des boutons du wizard
   	  	Button cancelWButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "ToPubliContent?WizardRow="+wizardRow, false);
    	Button nextButton;
    	if (isEnd)
    		nextButton = (Button) gef.getFormButton(resources.getString("kmelia.End"), "javascript:onClick=sendModelData()", false);
    	else
    		nextButton = (Button) gef.getFormButton(resources.getString("GML.next"), "javascript:onClick=sendModelData()", false);
    	  

      out.println(window.printBefore());

      if ("progress".equals(wizard))
  		displayWizardOperations(wizardRow, pubId, kmeliaScc, gef, "ModelUpdateView", resources, out, kmaxMode);
  	  else
  	  {
	      if (isOwner)
	          displayAllOperations(pubId, kmeliaScc, gef, "ModelUpdateView", resources, out, kmaxMode);
	      else
	          displayUserOperations(pubId, kmeliaScc, gef, "ModelUpdateView", resources, out, kmaxMode);
  	  }
      
      out.println(frame.printBefore());
      if (("finish".equals(wizard)) || ("progress".equals(wizard)))
  		{
  			//  cadre d'aide
    	    out.println(boardHelp.printBefore());
    		out.println("<table border=\"0\"><tr>");
    		out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resources.getIcon("kmelia.info")+"\"></td>");
    		out.println("<td>"+kmeliaScc.getString("kmelia.HelpContentModel")+"</td>");
    		out.println("</tr></table>");
    	    out.println(boardHelp.printAfter());
    	    out.println("<BR>");
  		}
      if (imageTrouble)
          out.println("<Font color=\"red\" size=2>"+kmeliaScc.getString("ImageTrouble")+"</font><BR><BR>");
          
      out.println(board.printBefore());
          
	/***************************************************************************************************************************/
	/** Affichage du modèle BdD																								  **/
	/***************************************************************************************************************************/
	ResourceLocator publicationSettings = new ResourceLocator("com.stratelia.webactiv.util.publication.publicationSettings", resources.getLanguage());
	
	String 		modelId 	= modelDetail.getId();
  	String 		toParse 	= modelDetail.getHtmlEditor();
  	Collection 	textList 	= null;
  	Collection 	imageList 	= null;

  	if (infos != null) {
    	textList 	= infos.getInfoTextList();
        imageList 	= infos.getInfoImageList();
  	} else {
    	textList 	= new ArrayList();
        imageList 	= new ArrayList();
  	}
  	Iterator textIterator 	= textList.iterator();
  	Iterator imageIterator 	= imageList.iterator();
  	
  	int infoTextCount 	= 0;
  	int infoImageCount 	= 0;
  	int posit  			= toParse.indexOf("%WA");
%>

  	<TABLE CELLPADDING=5><TR><TD valign=top>
  	<FORM Name="modelForm" ACTION="UpdateDBModelContent" METHOD="POST" ENCTYPE="multipart/form-data">
<%
	while (posit != -1) 
	{
    	if (posit > 0) {
      		out.print(toParse.substring(0, posit));
      		toParse = toParse.substring(posit);
    	}

    	if (toParse.startsWith("%WATXTDATA%")) 
    	{
      		if (textIterator.hasNext()) {
        		InfoTextDetail textDetail = (InfoTextDetail)textIterator.next();
				out.print(Encode.encodeSpecialChar(HTMLUncode(textDetail.getContent())));
      		}
      		toParse = toParse.substring(11);
    	}
    	else if (toParse.startsWith("%WATXTVAR%")) 
    	{
      		out.print("WATXTVAR" + String.valueOf(infoTextCount));
      		infoTextCount++;
      		toParse = toParse.substring(10);
    	}
    	else if (toParse.startsWith("%WAIMGDATA%")) 
    	{
      		if (imageIterator.hasNext()) 
      		{
        		InfoImageDetail imageDetail = (InfoImageDetail)imageIterator.next();
        		String url = FileServer.getUrl(spaceId, componentId, imageDetail.getLogicalName(), imageDetail.getPhysicalName(), imageDetail.getType(), publicationSettings.getString("imagesSubDirectory"));
        		out.println("<IMG BORDER=\"0\" SRC=\""+url+"\">");
      		}
      		toParse = toParse.substring(11);
    	}
    	else if (toParse.startsWith("%WAIMGVAR%")) 
    	{
      		out.print("<INPUT TYPE=\"file\" name=\"" + "WAIMGVAR" + String.valueOf(infoImageCount) + "\">");
      		infoImageCount++;
      		toParse = toParse.substring(10);
    	}

    	// et on recommence
    	posit  = toParse.indexOf("%WA");
	}
  	out.println(toParse);
  %>
  		<input type="hidden" name="PubId" value="<%=pubId%>">
  		<input type="hidden" name="ModelId" value="<%=modelId%>">
  	</FORM>
  	</TD></TR></TABLE>
<%
	out.println(board.printAfter());
    out.println(frame.printMiddle());
    
    ButtonPane buttonPane = gef.getButtonPane();
    if ("progress".equals(wizard))
	{
		buttonPane.addButton(nextButton);
		buttonPane.addButton(cancelWButton);
	}
    else
    {
        buttonPane.addButton(sendUpdateButton);
        buttonPane.addButton(cancelButton);
    }
    buttonPane.setHorizontalPosition();
    out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
    
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</BODY>
</HTML>