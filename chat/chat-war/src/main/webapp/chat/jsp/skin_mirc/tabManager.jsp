<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.CompletePublication"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.silverpeas.chat.control.ChatSessionController"%>
<%!
  
void displayJavascriptAndFormToOperations(ChatSessionController chatScc, JspWriter out) throws IOException {
     out.println("<Form Name=\"operationsForm\" ACTION=\"null\" Method=\"POST\">");
         out.println("<input type=\"hidden\" name=\"PubId\">");
         out.println("<input type=\"hidden\" name=\"Action\">");
     out.println("</Form>");

     out.println("<Form Name=\"enctypeForm\" ACTION=\"\" Method=\"POST\" ENCTYPE=\"multipart/form-data\">");
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
          out.println("alertMsg = \""+chatScc.getString("PubRemplirFormulaire")+"\";");
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
          out.println("alertMsg = \""+chatScc.getString("PubRemplirFormulaire")+"\";");
          out.println("if (pubId == \"\") {");
	        out.println("window.alert(alertMsg);");
          out.println("} else { ");
                out.println("url = target+\"?PubId=\"+pubId+\"&Action=\"+operation;");
                out.println("windowName = \"publicationWindow\";");
                out.println("windowParams = \"directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars\";");
                out.println("larg = \"740\";");
				out.println("haut = \"600\";");
                out.println("publicationWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);");
				//out.println("publicationWindow = open(url, windowName, windowParams, false);");
          out.println("}");
     out.println("}");

     out.println("function goToOperationWithEnctypeForm(target, pubId, operation) {");
          out.println("alertMsg = \""+chatScc.getString("PubRemplirFormulaire")+"\";");
          out.println("if (pubId == \"\") {");
	        out.println("window.alert(alertMsg);");
          out.println("} else { ");
                out.println("document.enctypeForm.PubId.value = pubId;");
                out.println("document.enctypeForm.Action.value = operation;");
                out.println("document.enctypeForm.action = target;");
                out.println("document.enctypeForm.submit();");
          out.println("}");
      out.println("}");

      out.println("function goToPathOperation(target, pubId, topicId, operation) {");
          out.println("alertMsg = \""+chatScc.getString("PubRemplirFormulaire")+"\";");
          out.println("if (pubId == \"\") {");
	        out.println("window.alert(alertMsg);");
          out.println("} else { ");
              out.println("if(window.confirm(\""+chatScc.getString("ConfirmDeletePath")+" ?\")){");
                  out.println("document.pathForm.PubId.value = pubId;");
                  out.println("document.pathForm.TopicId.value = topicId;");
                  out.println("document.pathForm.Action.value = operation;");
                  out.println("document.pathForm.action = target;");
                  out.println("document.pathForm.submit();");
              out.println("}");
           out.println("}");
      out.println("}");

     out.println("function goTo(fields) {");
        out.println("if ((fields != 0) && (fields != 1)) {");
          out.println("field = document.operations.operation[fields].value.split(\",\");");
          out.println("target = field[0];");
          out.println("pubId = field[1];");
          out.println("operation = field[2];");
          out.println("type = field[3];");
          out.println("if (type == 0)");
              out.println("goToOperation(target, pubId, operation);");
          out.println("if (type == 1)");
              out.println("goToOperationWithEnctypeForm(target, pubId, operation);");
          out.println("if (type == 2)");
              out.println("goToOperationInAnotherWindow(target, pubId, operation);");
        out.println("}");
     out.println("}");

     out.println("</script>");
}

void displayAllOperations(String id, ChatSessionController chatScc, GraphicElementFactory gef, String action, JspWriter out) throws IOException {
      
      displayJavascriptAndFormToOperations(chatScc, out);

      boolean enabled = false;
      if (id.length()>0)
          enabled = true;

      TabbedPane tabbedPane = gef.getTabbedPane(2);
      //tabbedPane.addTab(chatScc.getString("PubDeMemeSujet"), "publicationManager.jsp?Action=SameSubjectView&PubId="+id+"", action.equals("SameSubjectView"), enabled);
      //tabbedPane.addTab(chatScc.getString("PubDeMemeTheme"), "publicationManager.jsp?Action=SameTopicView&PubId="+id+"", action.equals("SameTopicView"), enabled);
      tabbedPane.addTab(chatScc.getString("PubGererChemins"), "publicationManager.jsp?Action=ViewPath&PubId="+id+"", action.equals("ViewPath"), enabled,1);
      //tabbedPane.addTab(chatScc.getString("PubGererAlertesEmails"), "javaScript:onClick=goToOperation('alertsManager.jsp', '"+id+"', 'ViewAlert')", action.equals("ViewAlert") || action.equals("AlertUsers"), enabled);
      tabbedPane.addTab(chatScc.getString("PubGererControlesLecture"), "javaScript:onClick=goToOperation('readingControlManager.jsp', '"+id+"', 'ViewReadingControl')", action.equals("ViewReadingControl") || action.equals("UpdateViewReadingControl"), enabled,1);
      
      tabbedPane.addTab(chatScc.getString("PublicationPreview"), "publicationManager.jsp?Action=ViewPublication&PubId="+id+"", action.equals("ViewPublication"), enabled,2);
      tabbedPane.addTab(chatScc.getString("Header"), "publicationManager.jsp?Action=UpdateView&PubId="+id+"", action.equals("View") || action.equals("UpdateView") || action.equals("New"), enabled,2);
      tabbedPane.addTab(chatScc.getString("Model"), "javaScript:onClick=goToOperationWithEnctypeForm('modelManager.jsp', '"+id+"', 'ModelUpdateView')", action.equals("ViewModel") || action.equals("ModelUpdateView") || action.equals("NewModel") || action.equals("ModelChoice"), enabled,2);
      tabbedPane.addTab(chatScc.getString("Attachments"), "javaScript:onClick=goToOperationWithEnctypeForm('attachmentManager.jsp', '"+id+"', 'ViewAttachments')", action.equals("ViewAttachments"), enabled,2);
      tabbedPane.addTab(chatScc.getString("PubReferenceeParAuteur"), "publicationManager.jsp?Action=LinkAuthorView&PubId="+id+"", action.equals("LinkAuthorView") || action.equals("SameSubjectView") || action.equals("SameTopicView"), enabled,2);
      out.println(tabbedPane.print());
}

void displayUserOperations(String id, ChatSessionController chatScc, GraphicElementFactory gef, String action, JspWriter out) throws IOException {
      
      displayJavascriptAndFormToOperations(chatScc, out);

      boolean enabled = false;
      if (id.length()>0)
          enabled = true;

      TabbedPane tabbedPane = gef.getTabbedPane();
      tabbedPane.addTab(chatScc.getString("Publication"), "publicationManager.jsp?Action=ViewPublication&PubId="+id+"", action.equals("ViewPublication"), enabled);
      tabbedPane.addTab(chatScc.getString("PubReferenceeParAuteur"), "publicationManager.jsp?Action=LinkAuthorView&PubId="+id+"", action.equals("LinkAuthorView"), enabled);
      out.println(tabbedPane.print());

}

void displayOnNewOperations(String id, ChatSessionController chatScc, GraphicElementFactory gef, String action, JspWriter out) throws IOException {
      
      displayJavascriptAndFormToOperations(chatScc, out);

      boolean enabled = false;
      if (id.length()>0)
          enabled = true;

      TabbedPane tabbedPane = gef.getTabbedPane();
      tabbedPane.addTab(chatScc.getString("Header"), "publicationManager.jsp?Action=View&PubId="+id+"", action.equals("View") || action.equals("UpdateView") || action.equals("New"), enabled);
      out.println(tabbedPane.print());

}


void displayOperations(String id, CompletePublication pubComplete, ChatSessionController chatScc, JspWriter out) throws IOException {
    displayJavascriptAndFormToOperations(chatScc, out);

    out.println("<FORM name=\"operations\">");
    out.println("<select name=\"operation\" onChange=\"javascript:goTo(this.selectedIndex)\">");
    out.println("<option value=\"0,0,0,0\">"+chatScc.getString("Operations")+"</option>");
    out.println("<option value=\"0,0,0,0\">-----------------------------------</option>");
    out.println("<option value=\"pathManager.jsp,"+id+",Search,2\">"+chatScc.getString("PubGererChemins")+"</option>");
    if (pubComplete != null) {
        if (pubComplete.getModelDetail() != null)
            out.println("<option value=\"modelManager.jsp,"+id+",UpdateView,1\">"+chatScc.getString("PubModifierModele")+"</option>");
        else
            out.println("<option value=\"modelManager.jsp,"+id+",ModelChoice,1\">"+chatScc.getString("PubCreerModele")+"</option>");
    } else {
        out.println("<option value=\"modelManager.jsp,"+id+",ModelChoice,1\">"+chatScc.getString("PubCreerModele")+"</option>");
    }
    out.println("<option value=\"attachmentManager.jsp,"+id+",View,1\">"+chatScc.getString("PubGererFichiersJoints")+"</option>");
    out.println("<option value=\"publicationLinksManager.jsp,"+id+",Search,2\">"+chatScc.getString("PubGererLiens")+"</option>");
    out.println("<option value=\"alertsManager.jsp,"+id+",View,0\">"+chatScc.getString("PubGererAlertesEmails")+"</option>");
    out.println("<option value=\"readingControlManager.jsp,"+id+",View,0\">"+chatScc.getString("PubGererControlesLecture")+"</option>");
    out.println("</select>");
    out.println("</Form>");

}

void displayBeginFrame(JspWriter out) throws IOException {
/*out.println("<!-- Cadre ext�rieur -->");
out.println("<table class=\"frame\" cellpadding=\"1\" cellspacing=\"1\" border=\"0\" width=\"600\">");
  out.println("<tr>");
    out.println("<td align=\"center\" width=\"600\">");

    out.println("<!-- Cadre int�rieur -->");
      out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" bgcolor=\"ffffff\">");
        out.println("<tr valign=\"middle\">");
          out.println("<td align=\"center\" width=\"600\">");*/

		out.println("<!-- Cadre ext�rieur -->");
       	out.println("<TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor align=\"center\" valign=\"top\">");
       	 out.println("<tr>");
        	out.println("<td>");

          		out.println("<!-- Cadre int�rieur -->");
           		out.println("<TABLE CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH=\"100%\" CLASS=intfdcolor4 align=\"center\">");
             		out.println("<tr valign=\"middle\">");
             			 out.println("<td align=\"center\">");

}
void displayEndFrame(JspWriter out) throws IOException {
    					 out.println("</TD></TR></TABLE> <!-- Fin cadre int�rieur -->");
   		out.println("</TD></TR></TABLE> <!-- Fin cadre ext�rieur -->");
}

%>