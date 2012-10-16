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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkYellowpages.jsp" %>
<%!

private String afficheArbo(ArrayPane arrayPane, String idNode, YellowpagesSessionController yellowpagesScc, int nbEsp, Collection fathers, String currentTopicId) throws Exception {
	String resultat = "";
    String espace = "";
    int N = nbEsp;
    String checkText = "";
    String fatherId="";

    for (int i=0; i<nbEsp; i++) {
    	espace += "&nbsp;";
    }
    N += 4;
    
    TopicDetail rootFolder = yellowpagesScc.getTopic(idNode);
    ArrayLine arrayLine = arrayPane.addArrayLine();
    String nodeName = rootFolder.getNodeDetail().getName();
    if (idNode.equals("0"))
        nodeName = yellowpagesScc.getComponentLabel();
    arrayLine.addArrayCellText(espace+nodeName);
    checkText = "<input type=\"checkbox\" name=\"topic\" value=\""+rootFolder.getNodeDetail().getNodePK().getId()+"\"";
    if (fathers != null)
    {
        Iterator it = fathers.iterator();
        while (it.hasNext()) {
              fatherId = ((NodePK) it.next()).getId();
              if (fatherId.equals(rootFolder.getNodeDetail().getNodePK().getId()))
            checkText += " checked";
        }
    }
    checkText += " >";
    
	arrayLine.addArrayCellText(checkText);
	resultat = arrayPane.print();

    Collection subThemes = rootFolder.getNodeDetail().getChildrenDetails();
    if (subThemes != null) {
        Iterator coll = subThemes.iterator();
        while (coll.hasNext()) {
        	NodeDetail theme = (NodeDetail) coll.next();
            String idTheme = theme.getNodePK().getId();
      		if (!idTheme.equals("1") && !idTheme.equals("2") && !idTheme.startsWith("group_"))
            	resultat = afficheArbo(arrayPane, idTheme, yellowpagesScc, N, fathers, currentTopicId);
        }
   }

    return resultat;
}
%>
<script>
<!--
function reallyClose()
{
  window.opener.document.topicDetailForm.Action.value = "Search";
  window.opener.document.topicDetailForm.submit();
  window.close();
}
//-->
</script>

<% 
TopicDetail CurrentTopic = yellowpagesScc.getCurrentTopic();
String action = request.getParameter("Action");
String listeTopics = request.getParameter("ListeTopics");
String contactId = request.getParameter("ContactId");

if ((action != null) && (action.equals("SendTopic")))
{
    int i = 0;
    int begin = 0;
    int end = 0;
    yellowpagesScc.deleteContactFathers(contactId);
    end = listeTopics.indexOf(',', begin);
    while(end != -1) {
        String idTopic = listeTopics.substring(begin, end);

        begin = end + 1;
        end = listeTopics.indexOf(',', begin);
        // ajout de la contactlication dans le theme
    yellowpagesScc.addContactToTopic(contactId, idTopic);
    }
    %>
    <BODY onLoad="reallyClose()">
    </BODY>
    <%
}
else
{
String linkedPathString = yellowpagesScc.getPath();
%>
          
<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script>
<!--

function B_VALIDER_ONCLICK() {
    f = "";
    if (String(document.AddTopicLink.topic.length) != "undefined") {
        for (i=0; i<document.AddTopicLink.topic.length; i++) {
            if (document.AddTopicLink.topic[i].checked)
                f += document.AddTopicLink.topic[i].value + ",";
        }
    }
    else {
        if (document.AddTopicLink.topic.checked)
            f += document.AddTopicLink.topic.value + ",";
    }
    if (f != "")
    {
        document.AddTopicLink.ListeTopics.value = f;
        document.AddTopicLink.submit();
    }
    else
        alert('<%=yellowpagesScc.getString("ErrorAddLink")%>');
}
//-->
</script>
</HEAD>

<BODY bgcolor="white" topmargin="15" leftmargin="20">

<FORM NAME="AddTopicLink" ACTION="TopicLink.jsp" METHOD="POST">
<input type=hidden name=Action value=SendTopic>
<input type=hidden name=ContactId value=<%=contactId%>>
<input type=hidden name=ListeTopics>
<%
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	browseBar.setPath(resources.getString("TopicLink"));

    Frame frame = gef.getFrame();
    Board board = gef.getBoard();

    //Debut code
    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());

    ArrayPane arrayPane = gef.getArrayPane("siteList", "", request, session);
    arrayPane.setVisibleLineNumber(1000);
    //Definition des colonnes du tableau
    ArrayColumn arrayColumnTopic = arrayPane.addArrayColumn(yellowpagesScc.getString("NomThemes"));
    arrayColumnTopic.setSortable(false);
    ArrayColumn arrayColumnContact = arrayPane.addArrayColumn(yellowpagesScc.getString("Contactlier"));
    arrayColumnContact.setSortable(false);

    String resultat = afficheArbo(arrayPane, "0", yellowpagesScc, 0, yellowpagesScc.getContactFathers(contactId),CurrentTopic.getNodeDetail().getNodePK().getId());

    out.println(resultat);

    ButtonPane buttonPane = gef.getButtonPane();
    Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=self.close();", false);
    Button validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false);
        
    out.println(board.printAfter());
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
    buttonPane.setHorizontalPosition(); 
    out.println("<br><center>"+buttonPane.print()+"</center><br>");
    out.println(frame.printAfter());
    out.println(window.printAfter());
}
%>
</form>
</BODY>     
</HTML>
