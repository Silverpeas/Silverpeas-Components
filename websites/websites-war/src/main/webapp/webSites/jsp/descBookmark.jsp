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

        SilverTrace.info("webSites", "JSPdescBookmark.afficheArbo()", "root.MSG_GEN_PARAM_VALUE", "name Theme ="+rootFolder.getNodeDetail().getName()+", nbEsp = "+nbEsp);

        resultat = arrayPane.print();

        Collection subThemes = rootFolder.getNodeDetail().getChildrenDetails();
        if (subThemes != null) {
            Iterator coll = subThemes.iterator();
            while (coll.hasNext()) {
                  NodeDetail theme = (NodeDetail) coll.next();
                  String idTheme = theme.getNodePK().getId();
                  SilverTrace.info("webSites", "JSPdescBookmark.afficheArbo()", "root.MSG_GEN_PARAM_VALUE", "name ss Theme ="+theme.getName()+", id= "+idTheme+", nbEsp = "+N);
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
                  SilverTrace.info("webSites", "JSPdescBookmark.nbThemes()", "root.MSG_GEN_PARAM_VALUE", "name ss Theme ="+theme.getName()+", N= "+N);
                  N = nbThemes(idTheme, scc, N);
            }
       }
       return N;
}


%>


<%  
    //CBO : REMOVE String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    String mandatoryField=m_context+"/util/icons/mandatoryField.gif";

    String action = (String) request.getAttribute("Action");
    if (action == null) // action = desc || suggest
        action = "desc";

	//CBO : ADD
	Collection allIcons = (Collection) request.getAttribute("AllIcons");
%>

<!-- descBookmark -->
           
<HTML>
<HEAD>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<%
out.println(gef.getLookStyleSheet());
%>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<script language="JavaScript">

function isCorrect(nom) {

    if (nom.indexOf("\"")>-1 || nom.indexOf("'")>-1) {
        return false;
    }
    return true;

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
     
     if (! isValidTextArea(description)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.description")%>' <%=resources.getString("ContainsTooLargeText")+resources.getString("NbMaxTextArea")+resources.getString("Characters")%>\n";
       errorNb++; 
     } 
     
     if (isWhitespace(nomPage)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("URL")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++; 
     }          
     
     if (! isCorrect(nomPage)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("URL")%>' <%=Encode.javaStringToJsString(resources.getString("MustNotContainSpecialChar"))%>\n<%=Encode.javaStringToJsString(resources.getString("Char1"))%>\n";
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
		f = "";
		if (document.descriptionSite.radio[0].checked)
			f += "0,";
			
		for (i=0; i<nbicones; i++) {
		  if (document.descriptionSite.icon[i].checked)
			  f += document.descriptionSite.icon[i].value + ",";
		}
		document.descriptionSite.ListeIcones.value = f;

		f = "";
		if (nbthemes == 1) {
			if (document.descriptionSite.topic.checked)
				f += document.descriptionSite.topic.value + ",";
		}
		else {
			for (i=0; i<nbthemes; i++) {
			  if (document.descriptionSite.topic[i].checked)
				  f += document.descriptionSite.topic[i].value + ",";
			}
		}
		document.descriptionSite.ListeTopics.value = f;
		document.descriptionSite.submit();
    }
}


/*********************************************************************************************************/

function B_SUGGERER_ONCLICK(nb, nom) {

    if (isCorrectForm()) {
		f = "";
		if (document.descriptionSite.radio[0].checked)
			f += nom+",";
				
		for (i=0; i<nb; i++) {
		  if (document.descriptionSite.icon[i].checked)
			  f += document.descriptionSite.icon[i].value + ",";
		}

		document.suggestionSite.nomSite.value = document.descriptionSite.nomSite.value;
		document.suggestionSite.description.value = document.descriptionSite.description.value;
		document.suggestionSite.nomPage.value = document.descriptionSite.nomPage.value;
		document.descriptionSite.auteur.disabled = false;
		document.suggestionSite.auteur.value = document.descriptionSite.auteur.value;
		document.descriptionSite.date.disabled = false;
		document.suggestionSite.date.value = document.descriptionSite.date.value;
		document.suggestionSite.ListeIcones.value = f; /* liste des noms d'icones */
		document.suggestionSite.submit();
	}
}

/*********************************************************************************************************/

    function B_ANNULER_ONCLICK() {
        document.descriptionSite.Action.value="view";
        document.descriptionSite.submit();
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
    if (action.equals("suggest")) {
        //CBO : UPDATE
		//browseBar.setComponentName(scc.getComponentLabel(), "listSite_reader.jsp");
		browseBar.setComponentName(componentLabel, "listSite_reader.jsp");
        browseBar.setPath(resources.getString("Suggerer"));
    }
    else {
		//CBO : UPDATE
        //browseBar.setComponentName(scc.getComponentLabel(), "manage.jsp?Action=view");
		browseBar.setComponentName(componentLabel, "manage.jsp?Action=view");
        browseBar.setPath(resources.getString("CreationSite"));
    }

    //Le cadre
    Frame frame = gef.getFrame();

	//Le board
	Board board = gef.getBoard();

    //Début code
    out.println(window.printBefore());
    out.println(frame.printBefore());
	out.print(board.printBefore());

   //current User
   UserDetail actor = scc.getUserDetail();

   //currentDate
   String creationDate = resources.getOutputDate(new Date());
%> 
            <TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
			<FORM NAME="descriptionSite" ACTION="manage.jsp" METHOD="POST">
			<input type="hidden" name="Action" value="addBookmark">
			<input type="hidden" name="ListeIcones">
			<input type="hidden" name="ListeTopics">
			<input type="hidden" name="auteur" value="<%=actor.getLastName()+" "+actor.getFirstName()%>">
			<input type="hidden" name="date" value="<%=creationDate%>">


                <tr>

                        
            <td class="intfdcolor4"><span class="txtlibform"><%=" "+resources.getString("GML.name")%> 
              : </span></td>
                        <td class="intfdcolor4"><input type="text" name="nomSite" size="60" maxlength="<%=DBUtil.TextFieldLength%>"
                                            >
              &nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
                </tr>

                <tr>
                        
            <td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("GML.description")%> 
              : </span></td>
                        <td class="intfdcolor4"><textarea rows="6" name="description" cols="60" 
                                            ></textarea></td>
                </tr>

                <tr>
                        
            <td class="intfdcolor4"><span class="txtlibform"><%=" "+resources.getString("URL")+" (ex : www.lesite.com)"%> 
              : </span></td>
                        <td class="intfdcolor4"><input type="text" name="nomPage" size="60" maxlength="<%=DBUtil.TextFieldLength%>"
                                            >&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>

                </tr>

                <tr>
                	<td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("GML.publisher")%> : </span></td>
                    <td class="intfdcolor4"><%=actor.getLastName()+" "+actor.getFirstName()%></td>
                </tr>
                <tr>
                	<td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("GML.date")%>: </span></td>
                    <td class="intfdcolor4"><%=creationDate%></td>
                </tr>
                <tr>
                        
            <td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("Reference")%> 
              : </span></td>
                        <td class="intfdcolor4"><input type="radio" name="radio"  
                                             
                                            value="">&nbsp;<%=resources.getString("GML.yes")%>&nbsp;&nbsp;
                                        <input type="radio" name="radio"  
                                             
                                            value="" checked>&nbsp;<%=resources.getString("GML.no")%></td>

                </tr>  
                
                
                 <tr>
              			<td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("OuvrirPopup")%> : </span></td>
                        <td class="intfdcolor4"><input type="checkbox" name="popup" checked>
						</td>
               </tr>                

                   <tr>
                        
            <td class="intfdcolor4" valign=top><span class="txtlibform"><%=resources.getString("IconesAssociees")%> 
              : </span></td>
                        <td class="intfdcolor4">
<%
							//CBO : REMOVE Collection c = scc.getAllIcons();
                            //CBO : UPDATE
							//Iterator i = c.iterator();
							Iterator i = allIcons.iterator();

							IconDetail icon = (IconDetail) i.next(); // on saute la premiere icone (site important)
							String nameReference = resources.getString(icon.getName());
							
							while (i.hasNext()) {
								icon = (IconDetail) i.next();
								if (action.equals("suggest")) {
									out.println("<input type=\"checkbox\" name=\"icon\" value = \""+resources.getString(icon.getName())+"\">&nbsp;");
								}
								else 
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
<%
	if(action.equals("suggest")) {
		out.println("</FORM>");
	}
%>
            </table>

<%

    if (! action.equals("suggest")) {
		ArrayPane arrayPane = gef.getArrayPane("siteList", "", request, session);
        arrayPane.setVisibleLineNumber(1000);

		//Définition des colonnes du tableau
        ArrayColumn arrayColumnTopic = arrayPane.addArrayColumn(resources.getString("NomThemes"));
        arrayColumnTopic.setSortable(false);
        ArrayColumn arrayColumnPub = arrayPane.addArrayColumn(resources.getString("GML.publish"));
		arrayColumnPub.setSortable(false);

        String resultat = afficheArbo(arrayPane, "0", scc, 0);

        out.println(resultat);
		out.println("</FORM>");
    }

      //fin du code
	  out.print(board.printAfter());
      out.println(frame.printMiddle());

      ButtonPane buttonPane = gef.getButtonPane();
      Button validerButton = null;
      Button annulerButton = null;

	  //CBO : UPDATE
	  //int size = c.size() - 1;
	  int size = allIcons.size() - 1;
      
      if (action.equals("suggest")) {
          validerButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_SUGGERER_ONCLICK("+size+", '"+nameReference+"');", false);
          annulerButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "listSite_reader.jsp", false);
      }
      else {
          validerButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK("+nbThemes("0", scc, 0)+", "+size+");", false);
          annulerButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false);
      }
      buttonPane.addButton(validerButton);
      buttonPane.addButton(annulerButton);
      buttonPane.setHorizontalPosition(); 

	out.println("<br><center>"+buttonPane.print()+"</center><br>");

    out.println(frame.printAfter());
    out.println(window.printAfter());

%>

<FORM NAME="suggestionSite" ACTION="SuggestLink" METHOD="POST">
  <input type="hidden" name="nomSite">
  <input type="hidden" name="description">
  <input type="hidden" name="nomPage">
  <input type="hidden" name="auteur">
  <input type="hidden" name="date">
  <input type="hidden" name="ListeIcones">
</FORM>

</BODY>     

</HTML>
