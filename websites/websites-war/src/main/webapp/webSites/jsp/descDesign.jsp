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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="javax.naming.Context"%>
<%@ page import="javax.naming.InitialContext"%>
<%@ page import="javax.rmi.PortableRemoteObject"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.SiteDetail"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.IconDetail"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
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
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.webSites.control.WebSiteSessionController"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="java.util.Date"%>
<%@ page import="com.stratelia.webactiv.util.DBUtil"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.FolderDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>

<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>

<%@ include file="checkScc.jsp" %>

<%!
    /**
    * afficheArbo
    */
private String afficheArbo(ArrayPane arrayPane, String idNode, WebSiteSessionController scc, int nbEsp) throws Exception {
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
        arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"topic\" value=\""+rootFolder.getNodeDetail().getNodePK().getId()+"\">");

        SilverTrace.info("websites", "JSPdescDesign.afficheArbo()", "root.MSG_GEN_PARAM_VALUE", "name Theme ="+rootFolder.getNodeDetail().getName()+", nbEsp = "+nbEsp);
        resultat = arrayPane.print();

        Collection subThemes = rootFolder.getNodeDetail().getChildrenDetails();
        if (subThemes != null) {
            Iterator coll = subThemes.iterator();
            while (coll.hasNext()) {
                  NodeDetail theme = (NodeDetail) coll.next();
                  String idTheme = theme.getNodePK().getId();
                  SilverTrace.info("websites", "JSPdescDesign.afficheArbo()", "root.MSG_GEN_PARAM_VALUE", "name ss Theme ="+theme.getName()+", id= "+idTheme+", nbEsp = "+N);
                  resultat = afficheArbo(arrayPane, idTheme, scc, N);
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
                  SilverTrace.info("websites", "JSPdescDesign.nbThemes()", "root.MSG_GEN_PARAM_VALUE", "name ss Theme ="+theme.getName()+", N= "+N);
                  N = nbThemes(idTheme, scc, N);
            }
       }
       return N;
}


%>


<%
    //CBO : REMOVE String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    String mandatoryField=m_context+"/util/icons/mandatoryField.gif";

	//CBO : ADD
	Collection allIcons = (Collection) request.getAttribute("AllIcons");
%>

<!-- descDesign -->
<HTML>
<HEAD>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<%
out.println(gef.getLookStyleSheet());
%>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<script language="JavaScript">

function isCorrectNameSite(nom) {

    if (nom.indexOf("\"")>-1) {
        return false;
    }
    return true;

}

/************************************************************************************/

function isCorrectNameFile(nom) {

        if (nom.indexOf(".") == 0) {
        return false;
    }

    return true;

}



/************************************************************************************/

function isCorrect(nom) {

    if (nom.indexOf("\\")>-1 || nom.indexOf("/")>-1 || nom.indexOf(":")>-1 ||
        nom.indexOf("*")>-1 || nom.indexOf("?")>-1 || nom.indexOf("\"")>-1 ||
        nom.indexOf("<")>-1 || nom.indexOf(">")>-1 || nom.indexOf("|")>-1 ||
        nom.indexOf("&")>-1 || nom.indexOf(";")>-1 || nom.indexOf("+")>-1 ||
        nom.indexOf("%")>-1 || nom.indexOf("#")>-1 || nom.indexOf("'")>-1 ||
        nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || nom.indexOf("�")>-1 ||
        nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || nom.indexOf("^")>-1 ||
        nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || /*nom.indexOf("�")>-1 ||*/
        nom.indexOf("�")>-1 || nom.indexOf("�")>-1 || nom.indexOf("�")>-1 ||
        nom.indexOf("�")>-1) {
        return false;
    }

    return true;

}

/************************************************************************************/

function isCorrectExtension(filename){

    var isCorrect = true;
    var indexPoint = filename.lastIndexOf(".");
    // on v�rifie qu'il existe une extension au nom du fichier propos�
    if (indexPoint != -1){
        // le fichier contient une extension. On r�cup�re l'extension
        var ext = filename.substring(indexPoint + 1);
        // on v�rifie que c'est une extension HTML
        if ( (ext != "html") && (ext != "htm") && (ext != "HTML") && (ext != "HTM") ){
            isCorrect = false;
        }
    }

    return isCorrect;
}


/************************************************************************************/

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var nomSite = stripInitialWhitespace(document.descriptionSite.nomSite.value);
     var description = document.descriptionSite.description;
     var nomPage = stripInitialWhitespace(document.descriptionSite.nomPage.value);


     if (isWhitespace(nomSite)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++;
     }

     if (! isCorrectNameSite(nomSite)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("MustNotContainSpecialChar")%>\n<%=Encode.javaStringToJsString(resources.getString("Char4"))%>\n";
       errorNb++;
     }

     if (! isValidTextArea(description)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.description")%>' <%=resources.getString("ContainsTooLargeText")+resources.getString("NbMaxTextArea")+resources.getString("Characters")%>\n";
       errorNb++;
     }

     if (isWhitespace(nomPage)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("NomPagePrincipale")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++;
     }

     if (! isCorrect(nomPage)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("NomPagePrincipale")%>' <%=resources.getString("MustNotContainSpecialChar")%>\n<%=Encode.javaStringToJsString(resources.getString("Char5"))%>\n";
       errorNb++;
     }

    if (! isCorrectNameFile(nomPage)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("NomPagePrincipale")%>' <%=resources.getString("MustContainFileName")%>\n";
       errorNb++;
     }

     // verify the extension
    if ( ! isCorrectExtension(nomPage) ){
        errorMsg += "<%=resources.getString("HTMLExtensionRequired")%>";
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

/******************************************************/

function B_VALIDER_ONCLICK(nbthemes, nbicones) {
    if (isCorrectForm()) {
<%
                  out.println("var indexPoint = document.descriptionSite.nomPage.value.lastIndexOf(\".\");");
                  out.println("var ext = document.descriptionSite.nomPage.value.substring(indexPoint + 1);");
                  out.println("if ( (ext != \"html\") && (ext != \"htm\") && (ext != \"HTML\") && (ext != \"HTM\") )");
                      out.println("document.descriptionSite.nomPage.value = document.descriptionSite.nomPage.value + \".html\"");
                      out.println("f = \"\";");
                      out.println("if (document.descriptionSite.radio[0].checked)");
                        out.println("f += \"0,\";");
                      out.println("for (i=0; i<nbicones; i++) {");
                          out.println("if (document.descriptionSite.icon[i].checked)");
                            out.println("f += document.descriptionSite.icon[i].value + \",\";");
                      out.println("}");
                      out.println("document.descriptionSite.ListeIcones.value = f;");
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
                      out.println("document.descriptionSite.submit();"); /* et on a bien une page html */
%>
    }
}


/*********************************************************************************************************/

    function B_ANNULER_ONCLICK() {
        document.desc.Action.value="view";
        document.desc.submit();
    }

</Script>

</HEAD>

<BODY bgcolor="white" topmargin="15" leftmargin="20" onload="document.descriptionSite.nomSite.focus()">
<%
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
	//CBO : UPDATE
	//browseBar.setDomainName(scc.getSpaceLabel());
	browseBar.setDomainName(spaceLabel);
    //CBO : UPDATE
	//browseBar.setComponentName(scc.getComponentLabel(), "manage.jsp?Action=view");
	browseBar.setComponentName(componentLabel, "manage.jsp?Action=view");
    browseBar.setPath(resources.getString("CreationSite"));

    //Le cadre
    Frame frame = gef.getFrame();

	//Le board
	Board board = gef.getBoard();

    //D�but code
    out.println(window.printBefore());
    out.println(frame.printBefore());
	out.print(board.printBefore());

   //current User
   UserDetail actor = scc.getUserDetail();

   //currentDate
   String creationDate = resources.getOutputDate(new Date());
%>

            <TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
			<FORM NAME="desc" ACTION="manage.jsp" METHOD="POST">
			<input type="hidden" name="Action">
			</FORM>

			<FORM NAME="descriptionSite" ACTION="design.jsp" METHOD="POST">
			<input type="hidden" name="Action" value="newSite">
			<input type="hidden" name="ListeIcones">
			<input type="hidden" name="ListeTopics">

                <tr>
                        <td class="intfdcolor4"><span class="txtlibform"><%=" "+resources.getString("GML.name")%> : </span></td>
                        <td class="intfdcolor4"><input type="text" name="nomSite" size="60" maxlength="<%=DBUtil.getTextFieldLength()%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
                </tr>
                <tr>
                        <td class="intfdcolor4" valign=top><span class="txtlibform"><%=resources.getString("GML.description")%> : </span></td>
                        <td class="intfdcolor4"><textarea rows="6" name="description" cols="60"></textarea></td>
                </tr>
                <tr>
                        <td class="intfdcolor4"><span class="txtlibform"><%=" "+resources.getString("NomPagePrincipale")%> : </span></td>
                        <td class="intfdcolor4"><input type="text" name="nomPage" size="60" maxlength="<%=DBUtil.getTextFieldLength()%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
                </tr>
                <tr>
                        <td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("GML.publisher")%> : </span></td>
                        <td class="intfdcolor4"><%=actor.getLastName()+" "+actor.getFirstName()%></td>
                </tr>
                <tr>
                        <td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("GML.date")%> : </span></td>
                        <td class="intfdcolor4"><%=creationDate%></td>
                </tr>
                <tr>
            			<td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("Reference")%> : </span></td>
                        <td class="intfdcolor4"><input type="radio" name="radio" value="">&nbsp;<%=resources.getString("GML.yes")%>&nbsp;&nbsp;
                        	<input type="radio" name="radio" value="" checked>&nbsp;<%=resources.getString("GML.no")%>
						</td>
               </tr>
               <tr>
              			<td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("OuvrirPopup")%> : </span></td>
                        <td class="intfdcolor4"><input type="checkbox" name="popup" checked></td>
               </tr>
               <tr>
                        <td class="intfdcolor4" valign=top><span class="txtlibform"><%=resources.getString("IconesAssociees")%> : </span></td>
                        <td class="intfdcolor4">
<%
                                                //CBO : REMOVE Collection c = scc.getAllIcons();
                                                //CBO : UPDATE
												//Iterator i = c.iterator();
												Iterator i = allIcons.iterator();
                                                i.next(); // on saute la premiere icone (site important)

                                                while (i.hasNext()) {
                                                    IconDetail icon = (IconDetail) i.next();
                                                    out.println("<input type=\"checkbox\" name=\"icon\" value = \""+icon.getIconPK().getId()+"\">&nbsp;");
                                                    out.println("<img src=\""+icon.getAddress()+"\" alt=\""+resources.getString(icon.getDescription())+"\" align=absmiddle title=\""+resources.getString(icon.getDescription())+"\">&nbsp;&nbsp;");
                                                    out.println(resources.getString(icon.getName())+"<BR>");
                                                }
%>
                                                </td>

                </tr>

                                <tr>

            <td height="10" colspan="2" class="intfdcolor4">(<img border="0" src="<%=mandatoryField%>" width="5" height="5">
              : <%=resources.getString("GML.requiredField")%>)</td>
                 </tr>
            </table>
<%

	ArrayPane arrayPane = gef.getArrayPane("siteList", "", request, session);
	arrayPane.setVisibleLineNumber(1000);
	//D�finition des colonnes du tableau
	ArrayColumn arrayColumnTopic = arrayPane.addArrayColumn(resources.getString("NomThemes"));
	arrayColumnTopic.setSortable(false);
	ArrayColumn arrayColumnPub = arrayPane.addArrayColumn(resources.getString("GML.publish"));
	arrayColumnPub.setSortable(false);

	String resultat = afficheArbo(arrayPane, "0", scc, 0);

	out.println(resultat);
%>

	</FORM>
<%

	//fin du code
	out.print(board.printAfter());
	out.println(frame.printMiddle());

	ButtonPane buttonPane = gef.getButtonPane();
	Button validerButton = null;
	Button annulerButton = null;

	//CBO : UPDATE
	//int size = c.size() - 1;
	int size = allIcons.size() - 1;

	validerButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK("+nbThemes("0", scc, 0)+", "+size+");", false);
	annulerButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false);
	buttonPane.addButton(validerButton);
	buttonPane.addButton(annulerButton);
	buttonPane.setHorizontalPosition();

	out.println("<br><center>"+buttonPane.print()+"</center><br>");

	out.println(frame.printAfter());
	out.println(window.printAfter());

%>
</BODY>

</HTML>
