<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.CompletePublication"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.kmelia.control.KmeliaSessionController"%>
<%@ page import="java.util.Iterator"%>
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
      PublicationDetail pubDetail = kmeliaScc.getSessionPublication().getDetail();
      PublicationDetail cloneDetail = null;
      if (kmeliaScc.getSessionClone() != null) {
	      cloneDetail = kmeliaScc.getSessionClone().getDetail();
	  }

      String decoration = "";
      String pubId = pubDetail.getPK().getId();
      String previewTabLabel = resources.getString("PublicationPreview");
      String sureId = pubId;
      if (cloneDetail != null) {
      	decoration = " *";
      	sureId = cloneDetail.getId();
      	previewTabLabel = resources.getString("kmelia.PublicPreview");
      }

	  int row = 2;
      if (invisibleTabs.indexOf(kmeliaScc.TAB_PREVIEW) == -1) {
          i++;
          tabbedPane.addTab(previewTabLabel, routerUrl+"ViewPublication?PubId=" + pubId, "View".equals(
          action) || "ViewPublication".equals(action), enabled, row);
      }
      if (cloneDetail != null) {
      		i++;
      		tabbedPane.addTab(resources.getString("kmelia.ClonePreview") + decoration, 
      			routerUrl+"ViewClone", "ViewClone".equals(action), enabled, row);
      }
      if (invisibleTabs.indexOf(kmeliaScc.TAB_HEADER) == -1) {
          i++;
          tabbedPane.addTab(kmeliaScc.getString("Header")+decoration, routerUrl
          + "publicationManager.jsp?Action=UpdateView&PubId=" + sureId, "UpdateView".equals(action)
          || "New".equals(action) || "KmaxModifyPublication".equals(action), enabled, row);
      }
      if (invisibleTabs.indexOf(kmeliaScc.TAB_CONTENT) == -1) {
          i++;
       	  tabbedPane.addTab(resources.getString("Model") + decoration, 
       	  "javaScript:onClick=goToOperation('" + routerUrl + "ToPubliContent', '" + sureId
       	  + "', 'ModelUpdateView')", "ModelUpdateView".equals(action) || "NewModel".equals(action)
       	  || "ModelChoice".equals(action), enabled, row);
      }
      if (invisibleTabs.indexOf(kmeliaScc.TAB_ATTACHMENTS) == -1) {
		if (kmeliaScc.getComponentId().startsWith("toolbox")) {
        	i++;
          	decoration = "";
          	tabbedPane.addTab(resources.getString("GML.attachments") + decoration, 
          		"javaScript:onClick=goToOperation('" + routerUrl + "ViewAttachments', '" + pubId
          		+ "', 'ViewAttachments')", "ViewAttachments".equals(action), enabled, row);
		}
      }
      if (invisibleTabs.indexOf(kmeliaScc.TAB_SEE_ALSO) == -1 && !kmaxMode) {
          i++;
		  List authorizedAndValidSeeAlsoList = kmeliaScc
		  	.getLinkedVisiblePublications();
          String nbSeeAlso = Integer.toString(authorizedAndValidSeeAlsoList.size());
          tabbedPane.addTab(resources.getString("PubReferenceeParAuteur") + " ("
          	+nbSeeAlso + ")", routerUrl + "SeeAlso", 
          	"LinkAuthorView".equals(action) || "SameSubjectView".equals(action) 
          	|| "SameTopicView".equals(action), enabled, row);
      }

     if (invisibleTabs.indexOf(kmeliaScc.TAB_ACCESS_PATHS) == -1 && !kmaxMode) {
          i++;
          if (i > 5) {
          	row = 1;
		  }
          tabbedPane.addTab( resources.getString("PubGererChemins"), routerUrl
          	+ "PublicationPaths?PubId=" + pubId, "ViewPath".equals(action), enabled, row);
      }

	  if (kmaxMode) {
    	 tabbedPane.addTab(kmeliaScc.getString("PubPositions"), "KmaxViewCombination?PubId=" + pubId, 
    	 	"KmaxViewCombination".equals(action), enabled, row);
      }

      if (invisibleTabs.indexOf(kmeliaScc.TAB_READER_LIST) == -1) {
          i++;
          if (i > 5) {
          	row = 1;
          }
          tabbedPane.addTab(resources.getString("PubGererControlesLecture"), routerUrl
          	+"ReadingControl", "ViewReadingControl".equals(action), enabled, row);
      }

      if (kmeliaScc.isValidationTabVisible()) {
      		i++;
          	if (i > 5) {
          		row = 1;
			}
          	tabbedPane.addTab(resources.getString("kmelia.validation"), routerUrl+"ViewValidationSteps", 
          		"ViewValidationSteps".equals(action), enabled, row);
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
		List authorizedAndValidSeeAlsoList = kmeliaScc.getLinkedVisiblePublications();
        String nbSeeAlso = Integer.toString(authorizedAndValidSeeAlsoList.size());
		tabbedPane.addTab(kmeliaScc.getString("PubReferenceeParAuteur")+" ("+nbSeeAlso+")", routerUrl+"SeeAlso?PubId="+id, action.equals("LinkAuthorView") || action.equals("SameSubjectView") || action.equals("SameTopicView"), enabled);
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
      boolean enabledClassification = false;
      int numRow = Integer.parseInt(wizardRow);
      if (numRow >= 1) {
      	enabledHeader = false;
      	if (id != null) {
          enabledHeader = true;
        }
      }
      if (numRow >= 2) {
      	enabledContent = true;
      }
      if (numRow >= 3) {
      	enabledAttachment = true;
      }
      if (numRow >= 4) {
      	enabledClassification = true;
      }

      List invisibleTabs = kmeliaScc.getInvisibleTabs();

      TabbedPane tabbedPane = gef.getTabbedPane();

      if (invisibleTabs.indexOf(kmeliaScc.TAB_HEADER) == -1) {
          tabbedPane.addTab(kmeliaScc.getString("Header"), routerUrl+"WizardHeader?PubId="+id, action.equals("Wizard") || action.equals("UpdateWizard"), enabledHeader);
      }
      if (invisibleTabs.indexOf(kmeliaScc.TAB_CONTENT) == -1) {
          tabbedPane.addTab(resources.getString("Model"), "ToPubliContent?PubId="+id, action.equals("ModelUpdateView") || action.equals("NewModel") || action.equals("ModelChoice"), enabledContent);
      }
      if (invisibleTabs.indexOf(kmeliaScc.TAB_ATTACHMENTS) == -1) {
          tabbedPane.addTab(resources.getString("GML.attachments"), "ViewAttachments?PubId="+id, action.equals("ViewAttachments"), enabledAttachment);
      }
      if (kmaxMode) {
      	tabbedPane.addTab(resources.getString("PubPositions"), "KmaxViewCombination?PubId="+id, action.equals("KmaxViewCombination"), enabledClassification);
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