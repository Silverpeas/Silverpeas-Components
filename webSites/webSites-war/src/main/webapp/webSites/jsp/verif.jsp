<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page import="org.silverpeas.kernel.util.StringUtil"%>
<%@page import="org.apache.commons.io.FilenameUtils"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window" %>
<%@ page import="org.silverpeas.core.web.http.HttpRequest" %>
<%@ page import="org.silverpeas.core.web.token.SynchronizerTokenService" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="util.jsp" %>
<%@ include file="checkScc.jsp" %>

<jsp:useBean id="thePath" scope="session" class="java.lang.String"/>
<jsp:useBean id="prems" scope="session" class="java.lang.String"/>

<%!
    /**
    * debutAffichage
    */
   private void debutAffichage(final HttpServletRequest request, JspWriter out, String rep, String action, String id,
       String currentPath, String name, String newName, String code, GraphicElementFactory gef, MultiSilverpeasBundle resources) throws IOException {
        out.println(

                 "<!-- verif -->"+

                  "<HTML>"+
                  "<TITLE>"+resources.getString("GML.popupTitle")+"</TITLE>"+
           gef.getLookStyleSheet(HttpRequest.decorate(request))+

                    "<HEAD>"+

                    "<Script language=\"JavaScript\">");

                      out.println("function submit_form(test) {");
                            out.println("if (test == \"ok\")");
                              out.println("document.verification.submit();");
                      out.println("}");

                    out.println("</Script>");

                    out.println("</HEAD>"+

                    "<BODY bgcolor=\"white\" topmargin=\"15\" leftmargin=\"20\" onLoad=\"submit_form('"+rep+"')\">"+
                    "<form name=\"verification\" action=\"design.jsp\" method=\"POST\">"+
                    "<input type=\"hidden\" name=\"" + SynchronizerTokenService.SESSION_TOKEN_KEY + "\" value=\""+request.getParameter(SynchronizerTokenService.SESSION_TOKEN_KEY)+"\">"+
                    "<input type=\"hidden\" name=\"" + SynchronizerTokenService.NAVIGATION_TOKEN_KEY + "\" value=\""+request.getParameter(SynchronizerTokenService.NAVIGATION_TOKEN_KEY)+"\">"+
                    "<input type=\"hidden\" name=\"Action\" value=\""+action+"\">"+
                    "<input type=\"hidden\" name=\"Id\" value=\""+id+"\">"+
                    "<input type=\"hidden\" name=\"Path\" value=\""+currentPath+"\">"+
                    "<input type=\"hidden\" name=\"name\" value=\""+name+"\">"+
                    "<input type=\"hidden\" name=\"newName\" value=\""+newName+"\">"+
                    "<input type=\"hidden\" name=\"Code\" value=\""+code+"\">"+
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

        TabbedPane tabbedPane = gef.getTabbedPane();

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

    String action;
    String id;
    String currentPath;
    String name;
    String newName;
    String fullNewName;
    String nomPage = "";//pas besoin du nom de la page
    String code;
    String extension;

    String resultat;

    action = (String) request.getParameter("Action");
    id = (String) request.getParameter("Id");
    currentPath = (String) request.getParameter("Path");
    name = (String) request.getParameter("name");
    newName = (String) request.getParameter("newName");
    code = (String) request.getParameter("Code");

    if ("renamePage".equals(action)) {
      extension = FilenameUtils.getExtension(name);
      fullNewName = (StringUtil.isDefined(extension) ? newName + "." + extension:newName);
    } else {
      fullNewName = newName;
    }

    if (code != null)
      code = WebEncodeHelper.javaStringToHtmlString(code);


    resultat = scc.verif(action, currentPath, name, fullNewName, nomPage);

    String nameSite = scc.getSiteName();
    Collection collectionRep = affichageChemin(scc, currentPath);
    String infoPath = displayPath(collectionRep, true, 3, "design.jsp?Action=view&Path=", nameSite);

    if (resultat.equals("ok")) {
        debutAffichage(request, out, "ok", action, id, currentPath, name, fullNewName, code, gef, resources);
    }

    else if (resultat.equals("pbAjoutFolder")) {
        debutAffichage(request, out, "", action, id, currentPath, name, fullNewName, code, gef, resources);
        affichageErreur(out, scc, infoPath, resources.getString("ErreurPbAjoutRep"), gef, spaceLabel, componentLabel);
    }

    else  if (resultat.equals("pbRenommageFolder")) {
        if (name.equals(fullNewName)) {
            debutAffichage(request, out, "ok", action, id, currentPath, name, fullNewName, code, gef, resources);
        }
        else {
            debutAffichage(request, out, "", action, id, currentPath, name, fullNewName, code, gef, resources);
            affichageErreur(out, scc, infoPath, resources.getString("ErreurPbRenommageRep"), gef, spaceLabel, componentLabel);
        }
    }

    else  if (resultat.equals("pbRenommageFile")) {
        if (name.equals(fullNewName)) {
            debutAffichage(request, out, "ok", action, id, currentPath, name, fullNewName, code, gef, resources);
        }
        else {
            debutAffichage(request, out, "", action, id, currentPath, name, fullNewName, code, gef, resources);
			affichageErreur(out, scc, infoPath, resources.getString("ErreurPbRenommageFichier"), gef, spaceLabel, componentLabel);
        }
    }
%>
