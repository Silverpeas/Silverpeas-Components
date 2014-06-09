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

<%@ page import="java.util.Collection"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="javax.naming.Context"%>
<%@ page import="javax.naming.InitialContext"%>
<%@ page import="javax.rmi.PortableRemoteObject"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.SiteDetail"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.webSites.control.WebSiteSessionController"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ include file="util.jsp" %>

<%@ include file="checkScc.jsp" %>

<jsp:useBean id="thePath" scope="session" class="java.lang.String"/>
<jsp:useBean id="prems" scope="session" class="java.lang.String"/>

<%!

    /**
    * debutAffichage
    */  
   private void debutAffichage(JspWriter out, String rep, String action, 
                                              String currentPath, String nomPage, String nameSite, GraphicElementFactory gef, String id, ResourcesWrapper resources) throws IOException {
        out.println(
              "<!-- verifAjoutPage -->"+
              
                  "<HTML>"+
                  "<TITLE>"+resources.getString("GML.popupTitle")+"</TITLE>"+
           gef.getLookStyleSheet()+
 
                    "<HEAD>"+
                    "<Script language=\"JavaScript\">");
                      out.println("function submit_form(test) {");
                      out.println("if (test == \"ok\")");
                      out.println("document.verification.submit();");
                      out.println("}");
                    out.println("</Script>");
                    out.println("</HEAD>"+

                    "<BODY bgcolor=\"white\" topmargin=\"15\" leftmargin=\"20\" onLoad=\"submit_form('"+rep+"')\">"+
                    "<form name=\"verification\" action=\"addPage.jsp\" method=\"POST\">"+
                    "<input type=\"hidden\" name=\"Action\" value=\""+action+"\">"+
                    "<input type=\"hidden\" name=\"path\" value=\""+currentPath+"\">"+
                    "<input type=\"hidden\" name=\"nomPage\" value=\""+nomPage+"\">"+
                    "<input type=\"hidden\" name=\"nameSite\" value=\""+nameSite+"\">"+
                    "<input type=\"hidden\" name=\"id\" value=\""+id+"\">"+
                    "</form>");
   }
   
   
    /**
    * affichageErreur
    */
	public void  affichageErreur(JspWriter out, WebSiteSessionController scc, String infoPath, 
                String mess, GraphicElementFactory gef, String spaceLabel, String componentLabel) throws IOException {
   
    Window laFenetre = gef.getWindow(); 
    
    // La barre de naviagtion
    BrowseBar browseBar = laFenetre.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "manage.jsp?Action=view");
    browseBar.setPath("<a href= \"manage.jsp?Action=view\"></a>"+infoPath);
    out.println(laFenetre.printBefore());
    TabbedPane tabbedPane = gef.getTabbedPane(1);
    Frame laFrame = gef.getFrame();
    out.println(tabbedPane.print());
    out.println(laFrame.printBefore());
    out.println(mess+"<BR><BR>");
    out.println(laFrame.printMiddle());
    out.println(laFrame.printAfter());
    out.println(laFenetre.printAfter());
    out.println("</BODY></HTML>");
}

%>

<%
    String action = "addPage";
    String currentPath;
    String nomPage;
    String nameSite;

    String resultat;

    currentPath = (String) request.getParameter("path");
    nomPage = (String) request.getParameter("nomPage");
    nameSite = (String) request.getParameter("nameSite");
		String id = (String) request.getParameter("id");
		
    resultat = scc.verif(action, currentPath, "", "", nomPage);
    
    Collection collectionRep = affichageChemin(scc, currentPath);
    String infoPath = displayPath(collectionRep, true, 3, "design.jsp?Action=view&path=", nameSite);

    if (resultat.equals("ok")) {
        debutAffichage(out, "ok", action, currentPath, nomPage, nameSite, gef, id, resources);
    }
    else if (resultat.equals("pbAjoutFile")) {
        debutAffichage(out, "", action, currentPath, nomPage, nameSite, gef, id, resources);
		affichageErreur(out, scc, infoPath, resources.getString("ErreurPbAjoutFichier"), gef, spaceLabel, componentLabel);
    }
%>
