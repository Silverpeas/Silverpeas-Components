<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="java.lang.*"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>

<%@ page import="com.stratelia.webactiv.util.*"%>
<%//@ page import="com.stratelia.webactiv.beans.admin.*"%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.FolderDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.SiteDetail"%>

<%@ include file="util.jsp" %>
<%@ include file="checkScc.jsp" %>

<%!

    /**
    * isPublishedInTopic
    */
   private boolean isPublishedInTopic(WebSiteSessionController scc, String idSite, String idNode) throws Exception {
        // est ce que l'id Site est publie dans l'id Node
		String idPub = scc.getIdPublication(idSite);
		Collection listFatherPK = scc.getAllFatherPK(idPub);
		Iterator i = listFatherPK.iterator();
		NodePK nodePk = null;
		while(i.hasNext()) {
			nodePk = (NodePK) i.next();
			if (idNode.equals(nodePk.getId())) {
				return true;
			}
        }
		return false;
   }

    /**
    * afficheArbo
    */
   private String afficheArbo(String idSite, ArrayPane arrayPane, String idNode, WebSiteSessionController scc, int nbEsp) throws Exception {
        String resultat;// = arrayPane.print();
        String espace = "";
        int N = nbEsp;

        for (int i=0; i<nbEsp; i++) {
            espace += "&nbsp;";
        }
        N += 4;

        FolderDetail rootFolder = scc.getFolder(idNode);

        ArrayLine arrayLine = arrayPane.addArrayLine();
        arrayLine.addArrayCellText(espace+rootFolder.getNodeDetail().getName());

        if (isPublishedInTopic(scc, idSite, rootFolder.getNodeDetail().getNodePK().getId()))
            arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"topic\" value=\""+rootFolder.getNodeDetail().getNodePK().getId()+"\" checked>");
        else arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"topic\" value=\""+rootFolder.getNodeDetail().getNodePK().getId()+"\">");


        resultat = arrayPane.print();

        Collection subThemes = rootFolder.getNodeDetail().getChildrenDetails();
        if (subThemes != null) {
            Iterator coll = subThemes.iterator();
            while (coll.hasNext()) {
                  NodeDetail theme = (NodeDetail) coll.next();
                  String idTheme = theme.getNodePK().getId();
                  resultat = afficheArbo(idSite, arrayPane, idTheme, scc, N);
            }
       }
       return resultat;
}

    /**
    * nbThemes
    */
    private int nbThemes(String idNode, WebSiteSessionController scc, int nb) throws Exception {
        int N = nb;

        FolderDetail rootFolder = scc.getFolder(idNode);
        N++;

        Collection subThemes = rootFolder.getNodeDetail().getChildrenDetails();
        if (subThemes != null) {
            Iterator coll = subThemes.iterator();
            while (coll.hasNext()) {
                  NodeDetail theme = (NodeDetail) coll.next();
                  String idTheme = theme.getNodePK().getId();
                  N = nbThemes(idTheme, scc, N);
            }
       }
       return N;
}

%>


<%
    String id = (String) request.getParameter("Id");
    String path = (String) request.getParameter("path");
    path = doubleAntiSlash(path);
    SiteDetail site = scc.getWebSite(id);

   Collection collectionRep = affichageChemin(scc, path);
   String infoPath = displayPath(collectionRep, true, 3, "design.jsp?Action=view&path=", site.getName());

%>

<!-- classifySite -->

<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<Script language="JavaScript">

<%

out.println("function B_VALIDER_ONCLICK(nbthemes) {");
                      out.println("document.descriptionSite.Action.value=\"classifySite\";");
                      out.println("f = \"\";");
                      out.println("if (nbthemes == 1) {");
                        out.println("if (document.descriptionSite.topic.checked)");
                            out.println("f += document.descriptionSite.topic.value + \",\";");
                      out.println("}");
                      out.println("else {");
                          out.println("for (i=0; i<nbthemes; i++) {");
                              out.println("if (document.descriptionSite.topic[i].checked)");
                                out.println("f += document.descriptionSite.topic[i].value + \",\";");
                          out.println("}");
                      out.println("}");
                      out.println("document.descriptionSite.ListeTopics.value = f;");
                      out.println("document.descriptionSite.submit();");
out.println("}");

%>


/*******************************************************************************************************************/

    function B_ANNULER_ONCLICK() {
        document.descriptionSite.Action.value="design";
        document.descriptionSite.submit();
    }

</Script>

</HEAD>
<BODY>

<FORM NAME="descriptionSite" ACTION="design.jsp" METHOD="POST">
  <input type="hidden" name="Action">
  <input type="hidden" name="Id" value="<%=id%>">
  <input type="hidden" name="path" value="<%=path%>">
  <input type="hidden" name="ListeTopics">

<%
    Window window = gef.getWindow();

    // La barre de naviagtion
    BrowseBar browseBar = window.getBrowseBar();
	  browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "manage.jsp?Action=view");
    browseBar.setPath("<a href= \"manage.jsp?Action=view\"></a>"+infoPath);

    //Le cadre
    Frame frame = gef.getFrame();

       //Debut code
       out.println(window.printBefore());
       out.println(frame.printBefore());


        //Recupere la liste des themes ou le site est deja publie

    ArrayPane arrayPane = gef.getArrayPane("siteList", "", request, session);
        arrayPane.setVisibleLineNumber(1000);
        arrayPane.setVisibleLineNumber(1000);
        arrayPane.setTitle(resources.getString("ClassificationSite")+" "+site.getName());
    //Definition des colonnes du tableau
        ArrayColumn arrayColumnTopic = arrayPane.addArrayColumn(resources.getString("NomThemes"));
        arrayColumnTopic.setSortable(false);
        ArrayColumn arrayColumnPub = arrayPane.addArrayColumn(resources.getString("GML.publish"));
    arrayColumnPub.setSortable(false);

        String resultat = afficheArbo(site.getSitePK().getId(), arrayPane, "0", scc, 0);


        out.println(resultat);


      //fin du code
      out.println(frame.printMiddle());

    //Boutons
    ButtonPane buttonPane = gef.getButtonPane();
    Button validerButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK("+nbThemes("0", scc, 0)+");", false);
    Button annulerButton = gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false);
    buttonPane.addButton(validerButton);
    buttonPane.addButton(annulerButton);
    buttonPane.setHorizontalPosition();

    out.println("<br><center>"+buttonPane.print()+"</center><br>");

    out.println(frame.printAfter());
    out.println(window.printAfter());

%>
</form>
</BODY>

</HTML>