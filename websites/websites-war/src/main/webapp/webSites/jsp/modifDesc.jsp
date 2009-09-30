<%--

    Copyright (C) 2000 - 2009 Silverpeas

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
<%@ page import="com.stratelia.webactiv.webSites.control.WebSiteSessionController"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="java.util.Date"%>
<%@ page import="com.stratelia.webactiv.util.DBUtil"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.FolderDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>
<%@ page import="com.stratelia.webactiv.util.DateUtil"%>

<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>

<%@ include file="util.jsp" %>
<%@ include file="checkScc.jsp" %>

<%!

    /**
    * appartient
    */
    private boolean appartient(IconDetail iconDetail, Collection c) {
          boolean ok = false;

          String theId = iconDetail.getIconPK().getId();

          Iterator i = c.iterator();
          while(i.hasNext() && !ok) {
              IconDetail icon = (IconDetail) i.next();
              String id = icon.getIconPK().getId();
              if (theId.equals(id)) 
                ok = true;
          }
          return ok;
    }
    
    /**
    * appartientId
    */
    private boolean appartientId(IconDetail iconDetail, Collection c) {
          boolean ok = false;

          String theId = iconDetail.getIconPK().getId();

          Iterator i = c.iterator();
          while(i.hasNext() && !ok) {
              String id = (String) i.next();
              if (theId.equals(id)) 
                ok = true;
          }
          return ok;
    }
    
    /**
    * isPublishedInTopic
    */
   private boolean isPublishedInTopic(WebSiteSessionController scc, String idSite, String idNode) throws Exception {
        // est ce que l'id Site est publie dans l'id Node

		//CBO : UPDATE
        /*Collection coll = scc.getAllPublication(idSite);
        Iterator i = coll.iterator();

        while(i.hasNext()) {
              String idPub = (String) i.next(); // idPub
              FolderDetail folder = scc.getPublicationFather(idPub);
              if (idNode.equals(folder.getNodeDetail().getNodePK().getId()))
                  return true;
        }
        return false;*/
		String idPub = scc.getIdPublication(idSite);
		//CBO : FIN UPDATE

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
	//CBO : REMOVE String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	String mandatoryField=m_context+"/util/icons/mandatoryField.gif";

	String id = (String) request.getParameter("Id"); 
	String currentPath = (String) request.getParameter("path"); /* = null ou rempli si type= design */
	String type = (String) request.getParameter("type"); // null  ou design

	//CBO : UPDATE
	//SiteDetail site = scc.getWebSite(id);
	SiteDetail site = (SiteDetail) request.getAttribute("Site");

	//CBO : ADD
	Collection listIcons = (Collection) request.getAttribute("ListIcons");
	Collection allIcons = (Collection) request.getAttribute("AllIcons");
	//CBO : FIN ADD

	String recupParam = (String) request.getParameter("RecupParam"); //=null ou oui
	String nom;
	String description;
	String lapage;
	ArrayList icones = new ArrayList();
	boolean refChecked = false;

	if (recupParam != null) {//=oui
		nom = (String) request.getParameter("Nom"); 
		if (nom == null) nom = "";
		description = (String) request.getParameter("Description"); 
		if (description == null) description = "";
		lapage = (String) request.getParameter("Page"); 
		if (lapage == null) lapage = "";
		String listeIcones = (String) request.getParameter("ListeIcones"); 
		int i = 0;
		int begin = 0;
		int end = 0;
		if (listeIcones != null) {
			end = listeIcones.indexOf(',', begin);
			while(end != -1) {
				String num = listeIcones.substring(begin, end);
				if (num.equals("0"))
					refChecked = true;
				icones.add(num);
				begin = end + 1;
				end = listeIcones.indexOf(',', begin);
			}
		}
	} else { //recup BD
		nom = site.getName();
		description = site.getDescription();
		if (description == null)
			description = "";

		//CBO : UPDATE
		//lapage = site.getPage();
		lapage = site.getContent();


		//CBO : REMOVE Collection ic = scc.getIcons(id);

		//CBO : UPDATE
		//icones = new ArrayList(ic);
		icones = new ArrayList(listIcons);

		//site de reference ou pas
		//CBO : UPDATE
		//Iterator i = ic.iterator();
		Iterator i = listIcons.iterator();
		while (i.hasNext()) {
			IconDetail icon = (IconDetail) i.next();
			if (icon.getName().equals("Icon0")) {
				refChecked = true;
				break;
			}
		}
	}
   
	Collection collectionRep = affichageChemin(scc, currentPath);
	String infoPath = displayPath(collectionRep, true, 3, "design.jsp?Action=view&path=", nom);
   

%>
     
<HTML>
<HEAD>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<%
out.println(gef.getLookStyleSheet());
%>

<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<Script language="JavaScript">

function isCorrectName(nom) {

    if (nom.indexOf("\"")>-1) {
        return false;
    }
    return true;

}

/************************************************************************************/

function isCorrectURL(nom) {

    if (nom.indexOf("\"")>-1 || nom.indexOf("'")>-1) {
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
        nom.indexOf("²")>-1 || nom.indexOf("é")>-1 || nom.indexOf("è")>-1 ||
        nom.indexOf("ç")>-1 || nom.indexOf("à")>-1 || nom.indexOf("^")>-1 ||
        nom.indexOf("ù")>-1 || nom.indexOf("°")>-1 || /*nom.indexOf("¨")>-1 ||*/
        nom.indexOf("£")>-1 || nom.indexOf("µ")>-1 || nom.indexOf("§")>-1 ||
        nom.indexOf("¤")>-1) {
        return false;
    }
    
    return true;

}

/************************************************************************************/

function isCorrectForm(type) {
     var errorMsg = "";
     var errorNb = 0;
     var nomSite = stripInitialWhitespace(document.descriptionSite.nomSite.value);
     var description = document.descriptionSite.description;
     var nomPage = stripInitialWhitespace(document.descriptionSite.nomPage.value);
     
          
     if (isWhitespace(nomSite)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++; 
     }    
     
     if (type != 1) { //upload et design
        if (! isCorrectName(nomSite)) {
            errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("MustNotContainSpecialChar")%>\n<%=Encode.javaStringToJsString(resources.getString("Char4"))%>\n";
            errorNb++; 
        }
     }         
     
     if (! isValidTextArea(description)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.description")%>' <%=resources.getString("ContainsTooLargeText")+resources.getString("NbMaxTextArea")+resources.getString("Characters")%>\n";
       errorNb++; 
     } 
     
     if (isWhitespace(nomPage)) {
           if (type == 1) { 
            errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("URL")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
            errorNb++; 
            }
        else {
            errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("NomPagePrincipale")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
            errorNb++; 
            }
     }          
     
     if (type == 1) { 
        if (! isCorrectURL(nomPage)) {
            errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("URL")%>' <%=Encode.javaStringToJsString(resources.getString("MustNotContainSpecialChar"))%>\n<%=Encode.javaStringToJsString(resources.getString("Char1"))%>";
            errorNb++; 
        }
      }
      else {
         if (! isCorrect(nomPage)) {
            errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("NomPagePrincipale")%>' <%=Encode.javaStringToJsString(resources.getString("MustNotContainSpecialChar"))%>\n<%=Encode.javaStringToJsString(resources.getString("Char5"))%>\n";
            errorNb++; 
    }     
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

/**************************************************************************************/

function B_VALIDER_ONCLICK(nbthemes, nbicones, type) {
    if (isCorrectForm(type)) {
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


/************************************************************************************/

    function B_ANNULER_ONCLICK() {
        document.descriptionSite.Action.value="view";
        document.descriptionSite.submit();
    }

</Script>

</HEAD>

<BODY bgcolor="white" topmargin="5" leftmargin="5" onload="document.descriptionSite.nomSite.focus()">

<%
	String theAction = "";
	if (type == null) { //pour sites bookmark
		theAction = "manage.jsp";
	} else {//cas de retour a design pour sites upload et sites design
		theAction = "design.jsp";
	} 

    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    //CBO : UPDATE
	//browseBar.setDomainName(scc.getSpaceLabel());
	browseBar.setDomainName(spaceLabel);
	//CBO : UPDATE
    //browseBar.setComponentName(scc.getComponentLabel(), "manage.jsp?Action=view");
	browseBar.setComponentName(componentLabel, "manage.jsp?Action=view");
    if (site.getType() == 1) { //bookmark
        browseBar.setPath(resources.getString("ModificationSite"));
    }
    
    else { //autres sites
        browseBar.setPath(infoPath+" - "+resources.getString("ModificationSite"));
    }
   
    //Le cadre
    Frame frame = gef.getFrame();

	//Le board
	Board board = gef.getBoard();

   //Début code
    out.println(window.printBefore());

	if (scc.isPdcUsed()) {
		TabbedPane tabbedPane = gef.getTabbedPane();
		tabbedPane.addTab(resources.getString("GML.description"), "#", true, false);
		tabbedPane.addTab(resources.getString("GML.PDC"), "pdcPositions.jsp?Action=ViewPdcPositions&Id="+id, false, true);
		out.println(tabbedPane.print());
	}
    out.println(frame.printBefore());
	out.print(board.printBefore());

	//current User
	String 		creatorName = null;

	//CBO : UPDATE
	//UserDetail 	creator 	= scc.getUserDetail(site.getAuthor());
	UserDetail 	creator = scc.getUserDetail(site.getCreatorId());

	if (creator != null)
		creatorName = creator.getDisplayedName();
	else
		creatorName = "?";

	//currentDate

	//CBO : UPDATE
	//String creationDate = resources.getOutputDate(site.getDate());
	String creationDate = resources.getOutputDate(site.getCreationDate());
	
	int popup = site.getPopup(); 
%>

            <TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
			<FORM NAME="descriptionSite" ACTION="<%=theAction%>" METHOD="POST">
			<input type="hidden" name="Action" value="updateDescription">
			<input type="hidden" name="Id" value="<%=id%>">  
			<input type="hidden" name="path" value="<%=currentPath%>">
			<input type="hidden" name="ListeIcones">
			<input type="hidden" name="ListeTopics">
			<input type="hidden" name="etat" value="<%=new Integer(site.getState()).toString()%>">

                <tr>
                    <td class="intfdcolor4"><span class="txtlibform"><%=" "+resources.getString("GML.name")%> : </span></td>
                    <td class="intfdcolor4"><input type="text" name="nomSite" size="60" maxlength="<%=DBUtil.TextFieldLength%>" value="<%=Encode.javaStringToHtmlString(nom)%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
                </tr>
                <tr>
                    <td class="intfdcolor4" valign=top><span class="txtlibform"><%=resources.getString("GML.description")%> : </span></td>
                    <td class="intfdcolor4"><textarea rows="6" name="description" cols="60"><%=description%></textarea></td>
                </tr>
                <tr>
<% if (site.getType() == 1) { %>
                    <td class="intfdcolor4"><span class="txtlibform"><%=" "+resources.getString("URL")+" (ex : www.lesite.com)"%> : </span></td>
<% } else { %>
                    <td class="intfdcolor4"><span class="txtlibform"><%=" "+resources.getString("NomPagePrincipale")%> : </span></td>
<% } %>
                    <td class="intfdcolor4"><input type="text" name="nomPage" size="60" maxlength="<%=DBUtil.TextFieldLength%>" value="<%=Encode.javaStringToHtmlString(lapage)%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
                </tr>

                <tr>
                	<td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("GML.publisher")%> : </span></td>
                    <td class="intfdcolor4"><%=creatorName%></td>
                </tr>
                <tr>
                    <td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("GML.date")%> : </span></td>
                    <td class="intfdcolor4"><%=creationDate%></td>
                </tr>
                <tr>
                        
            <td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("Reference")%> : 
               </span></td>
              
              <%
               if (refChecked) {%>
                        <td class="intfdcolor4"><input type="radio" name="radio"  
                                             
                                            value="" checked>&nbsp;<%=resources.getString("GML.yes")%>&nbsp;&nbsp;
                                        <input type="radio" name="radio"  
                                             
                                            value="">&nbsp;<%=resources.getString("GML.no")%></td>
          <%}
          
          else {%>
          
                            <td class="intfdcolor4"><input type="radio" name="radio"  
                                             
                                            value="">&nbsp;<%=resources.getString("GML.yes")%>&nbsp;&nbsp;
                                        <input type="radio" name="radio"  
                                             
                                            value="" checked>&nbsp;<%=resources.getString("GML.no")%></td>
          
          
          <%}%>

                </tr> 
                
                <tr>
               			<td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("OuvrirPopup")%> : </span></td>
               			
               			<% 	String openPopup = "";
                        	if (popup == 1)
								openPopup = "checked";
						%>
                        <td class="intfdcolor4"><input type="checkbox" name="popup" <%=openPopup%>></td>
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
                                                    if (recupParam != null) {//=oui
                                                        if (appartientId(icon, icones))
                                                            out.println("<input type=\"checkbox\" name=\"icon\" value = \""+icon.getIconPK().getId()+"\" checked>&nbsp;");
                                                        else out.println("<input type=\"checkbox\" name=\"icon\" value = \""+icon.getIconPK().getId()+"\">&nbsp;");
                                                    }
                                                    else {
                                                        if (appartient(icon, icones))
                                                            out.println("<input type=\"checkbox\" name=\"icon\" value = \""+icon.getIconPK().getId()+"\" checked>&nbsp;");
                                                        else out.println("<input type=\"checkbox\" name=\"icon\" value = \""+icon.getIconPK().getId()+"\">&nbsp;");
                                                    }
                                                    
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
		if (type != null) {
			out.println("</FORM>");
		}
%>
            </table>
<%

if (type == null) {
    //Recupere la liste des themes ou le site est deja publie 

    ArrayPane arrayPane = gef.getArrayPane("siteList", "", request, session);
    arrayPane.setVisibleLineNumber(1000);
    //Définition des colonnes du tableau
	ArrayColumn arrayColumnTopic = arrayPane.addArrayColumn(resources.getString("NomThemes"));
	arrayColumnTopic.setSortable(false);
	ArrayColumn arrayColumnPub = arrayPane.addArrayColumn(resources.getString("GML.publish"));
	arrayColumnPub.setSortable(false);

	String resultat = afficheArbo(site.getSitePK().getId(), arrayPane, "0", scc, 0);

	out.println(resultat);
	out.println("</FORM>");
}
    
	//fin du code
	out.print(board.printAfter());
	out.println(frame.printMiddle());

	//CBO : UPDATE
	//int size = c.size() - 1;
	int size = allIcons.size() - 1;

	ButtonPane buttonPane = gef.getButtonPane();
	Button validerButton = null;
	if (type == null) {
		  validerButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK("+nbThemes("0", scc, 0)+", "+size+", '"+site.getType()+"');", false);
	}
	else 
		validerButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK(0, "+size+", '"+site.getType()+"');", false);

	Button annulerButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false);
	buttonPane.addButton(validerButton);
	buttonPane.addButton(annulerButton);
	buttonPane.setHorizontalPosition(); 

	out.println("<br><center>"+buttonPane.print()+"</center><br>");

	out.println(frame.printAfter());
	out.println(window.printAfter());

%>
</BODY>     
</HTML>