<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>

<%!
  
void displayJavascriptAndFormToOperations(JspWriter out) throws IOException {
     out.println("<Form Name=\"enctypeForm2\" ACTION=\"\" Method=\"POST\" ENCTYPE=\"multipart/form-data\">");
         out.println("<input type=\"hidden\" name=\"ContactId\">");
         out.println("<input type=\"hidden\" name=\"Action\">");
     out.println("</Form>");
   out.println("<SCRIPT LANGUAGE=JavaScript>");

     out.println("function goToOperationWithEnctypeForm(target, contactId, operation) {");
                out.println("document.enctypeForm2.ContactId.value = contactId;");
                out.println("document.enctypeForm2.Action.value = operation;");
                out.println("document.enctypeForm2.action = target;");
                out.println("document.enctypeForm2.submit();");
      out.println("}");
out.println("</SCRIPT>");
}

void displayAllOperations(ResourcesWrapper resources, String id, GraphicElementFactory gef, String action, JspWriter out) throws IOException {
    displayJavascriptAndFormToOperations(out);

    boolean enabled = false;
    if ((id !=null)&&(id.length()>0)) {
        enabled = true;
	}

    TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(resources.getString("GML.description"), "updateTopic.jsp?ChildId="+id+"&Path=", action.equals("ViewDesc"),enabled);
    tabbedPane.addTab(resources.getString("Model"), "javaScript:onClick=goToOperationWithEnctypeForm('modelManager.jsp', '"+id+"', 'ModelChoice')", action.equals("ViewModel") || action.equals("NewModel") || action.equals("ModelChoice"), enabled);
    out.println(tabbedPane.print());
}

void displayContactOperations(ResourcesWrapper resources, String id, GraphicElementFactory gef, String action, JspWriter out, boolean useModel) throws IOException {
    displayJavascriptAndFormToOperations(out);
    boolean enabled = false;
    if ((id !=null)&&(id.length()>0)) {
        enabled = true;
    }

    TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(resources.getString("GML.head"), "contactManager.jsp?Action=UpdateView&ContactId="+id+"", action.equals("View") || action.equals("UpdateView") || action.equals("New"), enabled);
    if (useModel)
	    tabbedPane.addTab(resources.getString("Model"), "javaScript:onClick=goToOperationWithEnctypeForm('modelManager.jsp', '"+id+"', 'NewModel')", action.equals("ViewModel") || action.equals("NewModel") || action.equals("ModelChoice"), enabled);
    out.println(tabbedPane.print());
}
%>