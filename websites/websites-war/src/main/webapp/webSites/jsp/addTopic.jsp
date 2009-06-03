<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>

<%@ page import="com.stratelia.webactiv.util.exception.*"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>


<%@ include file="checkScc.jsp" %>

<%

SilverTrace.info("websites", "JSPaddTopic", "root.MSG_GEN_ENTER_METHOD");

//Récupération des paramètres
String fatherId = (String) request.getParameter("Id");
String path = (String) request.getParameter("Path");
String action = (String) request.getParameter("Action");

//CBO : REMOVE String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);

%>

<!-- addTopic -->

<%
if (action.equals("View")) {
%>
<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<script LANGUAGE="JavaScript" TYPE="text/javascript">

function isCorrect(nom) {

    if (nom.indexOf("&")>-1 || nom.indexOf(";")>-1 ||
        nom.indexOf(":")>-1 || nom.indexOf("+")>-1 ||
        nom.indexOf("%")>-1 || nom.indexOf("#")>-1) {
        return false;
    }
    return true;

}

/************************************************************************************/

function sendData() {
      if (isCorrectForm()) {
            document.topicDetailForm.Action.value = "Add";
            document.topicDetailForm.Name.value = stripInitialWhitespace(document.topicForm.Name.value);
            document.topicDetailForm.Description.value = stripInitialWhitespace(document.topicForm.Description.value);
            document.topicDetailForm.submit();
      }
}

/************************************************************************************/

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.topicForm.Name.value);
     var description = stripInitialWhitespace(document.topicForm.Description.value);
     if (isWhitespace(title)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++; 
     }
     if (isWhitespace(description)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.description")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++; 
     }
     if (! isCorrect(title)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("MustNotContainSpecialChar")%>\n  <%=resources.getString("Char6")%>\n";
       errorNb++; 
     }          
     switch(errorNb)
     {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}
</script>
</HEAD>

<BODY bgcolor="white" topmargin="15" leftmargin="20" onload="document.topicForm.Name.focus()">
<%
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    //CBO : UPDATE
	//browseBar.setDomainName(scc.getSpaceLabel());
	browseBar.setDomainName(spaceLabel);
    //CBO : UPDATE
	//browseBar.setComponentName(scc.getComponentLabel());
	browseBar.setComponentName(componentLabel);
    browseBar.setPath(resources.getString("FolderCreationTitle"));
    
    //Le cadre
    Frame frame = gef.getFrame();

	//Le board
	Board board = gef.getBoard();

    //Début code
    out.println(window.printBefore());
    out.println(frame.printBefore());
	out.print(board.printBefore());

%>
    <TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
	<FORM NAME="topicForm">
    <TR>
        <TD class="txtlibform"><%=resources.getString("GML.path")%> : </TD>
            <TD valign="top"><%
                           out.println(Encode.javaStringToHtmlString(path));
                            %></TD>
    </TR>
    
    <TR>
        <TD class="txtlibform"><%=resources.getString("GML.name")%> : </TD>
            <TD valign="top">
                <input type="text" name="Name" value="" size="60" maxlength="50">
                &nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></TD>
        </TR>
  
  
    <TR>
        <TD class="txtlibform"><%=resources.getString("GML.description")%> : </TD>
            <TD valign="top">
                <input type="text" name="Description" value="" size="60" maxlength="50">
                &nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></TD>
        </TR>
        
        <TR> 
            <TD colspan="2">(<img border="0" src="<%=mandatoryField%>" width="5" height="5"> 
              : <%=resources.getString("GML.requiredField")%>)</TD>
      </TR>   
	</FORM> 
    </TABLE>  
<%
   //fin du code
	out.print(board.printAfter());
    out.println(frame.printMiddle());

    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
   
    out.println("<br><center>"+buttonPane.print()+"</center><br>");

    out.println(frame.printAfter());
    out.println(window.printAfter());
%>

<!-- CBO : UPDATE -->
<!--<FORM NAME="topicDetailForm" ACTION="addTopic.jsp" METHOD=POST>-->
<FORM NAME="topicDetailForm" ACTION="AddTopic" METHOD=POST>
  <input type="hidden" name="Action">
  <input type="hidden" name="Id" value="<%=fatherId%>">
  <input type="hidden" name="Name">
  <input type="hidden" name="Description">
</FORM>
</BODY>
</HTML>
<% } //End View 

else if (action.equals("Add")) {

    //CBO : REMOVE
	/*String newTopicName = (String) request.getParameter("Name");
    String newTopicDescription = (String) request.getParameter("Description");
    SilverTrace.info("websites", "WebSiteBmEJB.addWebSites()", "root.MSG_GEN_PARAM_VALUE",
                     "name= "+newTopicName+", descrption = "+newTopicDescription);
    
    NodeDetail folder = new NodeDetail("X",newTopicName,newTopicDescription,null,null,null,"0","X");
    NodePK newNodePK = scc.addFolder(folder, "");
	*/
%>
 
	<HTML>
      <HEAD>
      <script language="Javascript">
          function closeAndReplace() {
            window.opener.location.replace("organize.jsp?Action=Search&Id=<%=fatherId%>");
            window.close();
          }
      </script>
      </HEAD>
      <BODY onLoad="closeAndReplace()">
      </BODY>
      </HTML>
<%  }  %>