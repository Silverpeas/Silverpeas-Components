<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.CompletePublication"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.kmelia.control.KmeliaSessionController"%>
<%!

void displayJavascriptAndFormToOperations(KmeliaSessionController kmeliaScc, JspWriter out) throws IOException {
     out.println("<Form Name=\"operationsForm\" ACTION=\"null\" Method=\"POST\">");
         out.println("<input type=\"hidden\" name=\"PubId\">");
         out.println("<input type=\"hidden\" name=\"Action\">");
     out.println("</Form>");

     out.println("<Form Name=\"pathForm\" ACTION=\"null\" Method=\"POST\">");
         out.println("<input type=\"hidden\" name=\"PubId\">");
         out.println("<input type=\"hidden\" name=\"TopicId\">");
         out.println("<input type=\"hidden\" name=\"Action\">");
     out.println("</Form>");

     out.println("<script language=\"javascript\">");
     out.println("function goToOperation(target, pubId, operation) {");
          out.println("alertMsg = \""+kmeliaScc.getString("PubRemplirFormulaire")+"\";");
          out.println("if (pubId == \"\") {");
	        out.println("window.alert(alertMsg);");
          out.println("} else { ");
                out.println("document.operationsForm.PubId.value = pubId;");
                out.println("document.operationsForm.Action.value = operation;");
                out.println("document.operationsForm.action = target;");
                out.println("document.operationsForm.submit();");
          out.println("}");
     out.println("}");

     out.println("function goToOperationInAnotherWindow(target, pubId, operation) {");
          out.println("alertMsg = \""+kmeliaScc.getString("PubRemplirFormulaire")+"\";");
          out.println("if (pubId == \"\") {");
	        out.println("window.alert(alertMsg);");
          out.println("} else { ");
                out.println("url = target+\"?PubId=\"+pubId+\"&Action=\"+operation;");
                out.println("windowName = \"publicationWindow\";");
                out.println("windowParams = \"directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars\";");
                out.println("larg = \"740\";");
				out.println("haut = \"600\";");
                out.println("publicationWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);");
          out.println("}");
     out.println("}");

     out.println("function goToOperationInAnotherWindow(target, pubId, attachmentOrDocumentId, operation) {");
          out.println("alertMsg = \""+kmeliaScc.getString("PubRemplirFormulaire")+"\";");
          out.println("if (pubId == \"\") {");
	        out.println("window.alert(alertMsg);");
          out.println("} else { ");
                out.println("url = target+\"?PubId=\"+pubId+\"&AttachmentOrDocumentId=\"+attachmentOrDocumentId+\"&Action=\"+operation;");
                out.println("windowName = \"publicationWindow\";");
                out.println("windowParams = \"directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars\";");
                out.println("larg = \"740\";");
				out.println("haut = \"600\";");
                out.println("publicationWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);");
          out.println("}");
     out.println("}");

      out.println("function goToPathOperation(target, pubId, topicId, operation) {");
          out.println("alertMsg = \""+kmeliaScc.getString("PubRemplirFormulaire")+"\";");
          out.println("if (pubId == \"\") {");
	        out.println("window.alert(alertMsg);");
          out.println("} else { ");
              out.println("if(window.confirm(\""+kmeliaScc.getString("ConfirmDeletePath")+"\")){");
                  out.println("document.pathForm.PubId.value = pubId;");
                  out.println("document.pathForm.TopicId.value = topicId;");
                  out.println("document.pathForm.Action.value = operation;");
                  out.println("document.pathForm.action = target;");
                  out.println("document.pathForm.submit();");
              out.println("}");
           out.println("}");
      out.println("}");

     out.println("</script>");
}

public void displayAllOperations(String id, KmeliaSessionController kmeliaScc, GraphicElementFactory gef, String action, ResourcesWrapper resources, JspWriter out) throws IOException {
	boolean kmaxMode = false;
	displayAllOperations(id, kmeliaScc, gef, action, resources, out, kmaxMode);
}

public void displayAllOperations(String id, KmeliaSessionController kmeliaScc, GraphicElementFactory gef, String action, ResourcesWrapper resources, JspWriter out, boolean kmaxMode) throws IOException {
	String routerUrl = URLManager.getApplicationURL() + URLManager.getURL(kmeliaScc.getComponentRootName(), kmeliaScc.getSpaceId(), kmeliaScc.getComponentId());
      displayJavascriptAndFormToOperations(kmeliaScc, out);

      boolean enabled = false;
      if (id.length()>0)
          enabled = true;

      List invisibleTabs = kmeliaScc.getInvisibleTabs();

      int i = 0;
      TabbedPane tabbedPane = gef.getTabbedPane(2);

      PublicationDetail pubDetail = kmeliaScc.getSessionPublication().getPublication().getPublicationDetail();

      PublicationDetail cloneDetail = null;
      if (kmeliaScc.getSessionClone() != null)
	      cloneDetail = kmeliaScc.getSessionClone().getPublication().getPublicationDetail();

      String decoration = "";
      String cloneId = id;
      String previewTabLabel = resources.getString("PublicationPreview");
      if (cloneDetail != null)
      {
      	decoration = " *";
      	id = pubDetail.getPK().getId();
      	previewTabLabel = resources.getString("kmelia.PublicPreview");
      }

	  int row = 2;
      if (invisibleTabs.indexOf(kmeliaScc.TAB_PREVIEW) == -1) {
          i++;
          tabbedPane.addTab(previewTabLabel, routerUrl+"ViewPublication?PubId="+id, action.equals("View") || action.equals("ViewPublication"), enabled, row);
      }
      if (cloneDetail != null)
      {
      		i++;
      		tabbedPane.addTab(resources.getString("kmelia.ClonePreview")+decoration, routerUrl+"ViewClone", action.equals("ViewClone"), enabled, row);
      }
      if (invisibleTabs.indexOf(kmeliaScc.TAB_HEADER) == -1) {
          i++;
          tabbedPane.addTab(kmeliaScc.getString("Header")+decoration, routerUrl+"publicationManager.jsp?Action=UpdateView&PubId="+cloneId, action.equals("UpdateView") || action.equals("New") || action.equals("KmaxModifyPublication"), enabled, row);
      }
      if (invisibleTabs.indexOf(kmeliaScc.TAB_CONTENT) == -1) {
          i++;
       	  tabbedPane.addTab(resources.getString("Model")+decoration, "javaScript:onClick=goToOperation('"+routerUrl+"ToPubliContent', '"+id+"', 'ModelUpdateView')", action.equals("ModelUpdateView") || action.equals("NewModel") || action.equals("ModelChoice"), enabled, row);
      }
      if (invisibleTabs.indexOf(kmeliaScc.TAB_ATTACHMENTS) == -1) {
          if (kmeliaScc.getComponentId().startsWith("toolbox"))
          {
          i++;
          	decoration = "";
          tabbedPane.addTab(resources.getString("GML.attachments")+decoration, "javaScript:onClick=goToOperation('"+routerUrl+"ViewAttachments', '"+id+"', 'ViewAttachments')", action.equals("ViewAttachments"), enabled, row);
          }
      }
      if (invisibleTabs.indexOf(kmeliaScc.TAB_SEE_ALSO) == -1 && !kmaxMode) {
          i++;
		  List seeAlsoList = kmeliaScc.getSessionPublication().getPublication().getLinkList();
		  List authorizedSeeAlsoList = new ArrayList();
          List authorizedAndValidSeeAlsoList = new ArrayList();
            ForeignPK curFPK = null;
            String curComponentId = null;
            for (int cpt=0; cpt < seeAlsoList.size(); cpt++) {
              curFPK = (ForeignPK) seeAlsoList.get(cpt);
              curComponentId = curFPK.getComponentName();
              if (curComponentId != null && kmeliaScc.getOrganizationController().isComponentAvailable(curComponentId, kmeliaScc.getUserId())) {
                authorizedSeeAlsoList.add(curFPK);
              }
            }

            UserPublication   userPub;
            PublicationDetail   pub;
          Collection linkedPublications = kmeliaScc.getPublications(authorizedSeeAlsoList);
          
            for (int cptPub=0; cptPub < linkedPublications.size(); cptPub++) {
              userPub = (UserPublication) linkedPublications.get(cptPub);
              pub = userPub.getPublication();
              if (pub.getStatus() != null && pub.getStatus().equals("Valid")) {
                authorizedAndValidSeeAlsoList.add(pub);
              }
            }
          
              String nbSeeAlso = Integer.toString(authorizedAndValidSeeAlsoList.size());

            
          String nbSeeAlso = Integer.toString(authorizedSeeAlsoList.size());
          tabbedPane.addTab(resources.getString("PubReferenceeParAuteur")+" ("+nbSeeAlso+")", routerUrl+"SeeAlso?PubId="+id, action.equals("LinkAuthorView") || action.equals("SameSubjectView") || action.equals("SameTopicView"), enabled, row);
      }
      if (invisibleTabs.indexOf(kmeliaScc.TAB_COMMENT) == -1) {
          i++;
          String nbComments = Integer.toString(kmeliaScc.getAllComments(id).size());
          tabbedPane.addTab("<span id='comment-tab'>" + resources.getString("Comments")+" ("+nbComments+")</span>", routerUrl+"Comments?PubId="+id, action.equals("ViewComment"), enabled, row);
      }

     if (invisibleTabs.indexOf(kmeliaScc.TAB_ACCESS_PATHS) == -1 && !kmaxMode) {
          i++;
          if (i > 5)
          	row = 1;
          tabbedPane.addTab( resources.getString("PubGererChemins"), routerUrl+"PublicationPaths?PubId="+id, action.equals("ViewPath"), enabled, row);
      }

	  if (invisibleTabs.indexOf(kmeliaScc.TAB_PDC) == -1) {
          i++;
          if (i > 5)
          	row = 1;
          tabbedPane.addTab( resources.getString("GML.PDC"), routerUrl+"ViewPdcPositions?Action=ViewPdcPositions&PubId="+id, action.equals("ViewPdcPositions"), enabled, row);
      }

	  if (kmaxMode)
    	 tabbedPane.addTab(kmeliaScc.getString("PubPositions"), "KmaxViewCombination?PubId="+id+"", action.equals("KmaxViewCombination"), enabled, row);

      if (invisibleTabs.indexOf(kmeliaScc.TAB_READER_LIST) == -1) {
          i++;
          if (i > 5)
          	row = 1;
          tabbedPane.addTab(resources.getString("PubGererControlesLecture"), routerUrl+"ReadingControl", action.equals("ViewReadingControl"), enabled, row);
      }

      if (kmeliaScc.isValidationTabVisible())
      {
      		i++;
          	if (i > 5)
          		row = 1;
          	tabbedPane.addTab(resources.getString("kmelia.validation"), routerUrl+"ViewValidationSteps", action.equals("ViewValidationSteps"), enabled, row);
      }

      out.println(tabbedPane.print());
}

void displayUserOperations(String id, KmeliaSessionController kmeliaScc, GraphicElementFactory gef, String action, ResourcesWrapper resources, JspWriter out) throws IOException {
	displayUserOperations(id, kmeliaScc, gef, action, resources, out, false);
}

void displayUserOperations(String id, KmeliaSessionController kmeliaScc, GraphicElementFactory gef, String action, ResourcesWrapper resources, JspWriter out, boolean kmaxMode) throws IOException {

	  String routerUrl = URLManager.getApplicationURL() + URLManager.getURL(kmeliaScc.getComponentRootName(), kmeliaScc.getSpaceId(), kmeliaScc.getComponentId());

      displayJavascriptAndFormToOperations(kmeliaScc, out);

      int i = 0;

      boolean enabled = false;
      if (id.length()>0)
          enabled = true;

      List invisibleTabs = kmeliaScc.getInvisibleTabs();
      TabbedPane tabbedPane = gef.getTabbedPane();
      tabbedPane.addTab(resources.getString("GML.publication"), routerUrl+"ViewPublication?PubId="+id, action.equals("View") || action.equals("ViewPublication"), enabled);
      if (invisibleTabs.indexOf(kmeliaScc.TAB_SEE_ALSO) == -1 && !kmaxMode) {
    	  List seeAlsoList = kmeliaScc.getSessionPublication().getPublication().getLinkList();
    	  List authorizedSeeAlsoList = new ArrayList();
                ForeignPK curFPK = null;
                String curComponentId = null;
                for (int cpt=0; cpt < seeAlsoList.size(); cpt++) {
                  curFPK = (ForeignPK) seeAlsoList.get(cpt);
                  curComponentId = curFPK.getComponentName();
                  if (curComponentId != null && kmeliaScc.getOrganizationController().isComponentAvailable(curComponentId, kmeliaScc.getUserId())) {
                    authorizedSeeAlsoList.add(curFPK);
                  }
                }

              String nbSeeAlso = Integer.toString(authorizedSeeAlsoList.size());
			tabbedPane.addTab(kmeliaScc.getString("PubReferenceeParAuteur")+" ("+nbSeeAlso+")", routerUrl+"SeeAlso?PubId="+id, action.equals("LinkAuthorView") || action.equals("SameSubjectView") || action.equals("SameTopicView"), enabled);
			i++;
      }
	  if (invisibleTabs.indexOf(kmeliaScc.TAB_COMMENT) == -1 && !kmaxMode) {
	  		String nbComments = Integer.toString(kmeliaScc.getAllComments(id).size());
			tabbedPane.addTab("<span id='comment-tab'>" + kmeliaScc.getString("Comments")+" ("+nbComments+")</span>", routerUrl+"Comments?PubId="+id, action.equals("ViewComment"), enabled);
			i++;
      }

      if (i > 0)
	      out.println(tabbedPane.print());
}

void displayWizardOperations(String wizardRow, String id, KmeliaSessionController kmeliaScc, GraphicElementFactory gef, String action, ResourcesWrapper resources, JspWriter out, boolean kmaxMode) throws IOException {

	  String routerUrl = URLManager.getApplicationURL() + URLManager.getURL(kmeliaScc.getComponentRootName(), kmeliaScc.getSpaceId(), kmeliaScc.getComponentId());

      displayJavascriptAndFormToOperations(kmeliaScc, out);

      boolean enabledHeader = false;
      boolean enabledContent = false;
      boolean enabledAttachment = false;
      boolean enabledPdc = false;
      int numRow = Integer.parseInt(wizardRow);
      if (numRow >= 1)
      {
      	enabledHeader = false;
      	if (id != null)
          enabledHeader = true;
      }
      if (numRow >= 2)
      	enabledContent = true;
      if (numRow >= 3)
      	enabledAttachment = true;
      if (numRow >= 4)
      	enabledPdc = true;

      List invisibleTabs = kmeliaScc.getInvisibleTabs();

      int row = 2;
      int i = 0;
      TabbedPane tabbedPane = gef.getTabbedPane(2);

      if (invisibleTabs.indexOf(kmeliaScc.TAB_HEADER) == -1) {
          i++;
          tabbedPane.addTab(kmeliaScc.getString("Header"), routerUrl+"WizardHeader?PubId="+id, action.equals("Wizard") || action.equals("UpdateWizard"), enabledHeader, row);
      }
      if (invisibleTabs.indexOf(kmeliaScc.TAB_CONTENT) == -1) {
          i++;
          tabbedPane.addTab(resources.getString("Model"), "ToPubliContent?PubId="+id, action.equals("ModelUpdateView") || action.equals("NewModel") || action.equals("ModelChoice"), enabledContent, row);
      }
      if (invisibleTabs.indexOf(kmeliaScc.TAB_ATTACHMENTS) == -1) {
          i++;
          tabbedPane.addTab(resources.getString("GML.attachments"), "ViewAttachments?PubId="+id, action.equals("ViewAttachments"), enabledAttachment, row);
      }
	  if (kmeliaScc.isPDCClassifyingMandatory() && invisibleTabs.indexOf(kmeliaScc.TAB_PDC) == -1) {
          i++;
          tabbedPane.addTab(resources.getString("GML.PDC"), "ViewPdcPositions", action.equals("ViewPdcPositions"), enabledPdc, row);
      }
      if (kmaxMode)
      {
      	tabbedPane.addTab(resources.getString("PubPositions"), "KmaxViewCombination?PubId="+id, action.equals("KmaxViewCombination"), enabledPdc, row);
      }
      out.println(tabbedPane.print());

}

public void displayOnNewOperations(KmeliaSessionController kmeliaScc, GraphicElementFactory gef, String action, JspWriter out) throws IOException {
      displayJavascriptAndFormToOperations(kmeliaScc, out);
      List invisibleTabs = kmeliaScc.getInvisibleTabs();

      TabbedPane tabbedPane = gef.getTabbedPane();
      if (invisibleTabs.indexOf(kmeliaScc.TAB_HEADER) == -1) {
        tabbedPane.addTab(kmeliaScc.getString("Header"), "publicationManager.jsp?Action=View", action.equals("View") || action.equals("UpdateView") || action.equals("New"), false);
      }
      out.println(tabbedPane.print());
}
%>
