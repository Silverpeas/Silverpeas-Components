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
<%@ page import="java.io.File"%>
<%@ page import="com.stratelia.webactiv.util.DBUtil"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.FolderDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.*"%>

<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>


<%@ include file="checkScc.jsp" %>
<%@ include file="util.jsp" %>

<%!

    /**
    * appartient
    */
    private boolean appartient(IconDetail iconDetail, Collection c) {
          SilverTrace.info("websites", "JSPdescUpload", "root.MSG_GEN_PARAM_VALUE", "appartient");
          boolean ok = false;

          String theId = iconDetail.getIconPK().getId();
          SilverTrace.info("websites", "JSPdescUpload", "root.MSG_GEN_PARAM_VALUE", "theId= "+theId);

          Iterator i = c.iterator();
          while(i.hasNext() && !ok) {
              String id = (String) i.next();
              if (theId.equals(id))
                ok = true;
          }
          return ok;
    }

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

        SilverTrace.info("websites", "JSPdescUpload.afficheArbo()", "root.MSG_GEN_PARAM_VALUE", "name Theme ="+rootFolder.getNodeDetail().getName()+", nbEsp = "+nbEsp);

        resultat = arrayPane.print();

        Collection subThemes = rootFolder.getNodeDetail().getChildrenDetails();
        if (subThemes != null) {
            Iterator coll = subThemes.iterator();
            while (coll.hasNext()) {
                  NodeDetail theme = (NodeDetail) coll.next();
                  String idTheme = theme.getNodePK().getId();
                  SilverTrace.info("websites", "JSPdescUpload.afficheArbo()", "root.MSG_GEN_PARAM_VALUE", "name ss Theme ="+theme.getName()+", id= "+idTheme+", nbEsp = "+N);
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
                  SilverTrace.info("websites", "JSPdescUpload.nbThemes()", "root.MSG_GEN_PARAM_VALUE", "name ss Theme ="+theme.getName()+", N= "+N);
                  N = nbThemes(idTheme, scc, N);
            }
       }
       return N;
}

%>


<%
	//CBO : REMOVE ResourceLocator settings;
	String nomSite = "";
	String description = "";
	String nomPage = "";
	String auteur = null;
	String listeIcones = "";
	//CBO : REMOVE String listeTopics = "";
	boolean refChecked = false;
	//CBO : REMOVE boolean uploadOk = true;
	//CBO : REMOVE boolean searchOk = true;

	//CBO : ADD
	int popup = 1;

	//CBO : ADD
	SiteDetail siteDetail = (SiteDetail) request.getAttribute("Site");
	Collection allIcons = (Collection) request.getAttribute("AllIcons");
	listeIcones = (String) request.getAttribute("ListeIcones");
	Boolean uploadOk = (Boolean) request.getAttribute("UploadOk");
	Boolean searchOk = (Boolean) request.getAttribute("SearchOk");

	if(siteDetail != null) {
		nomSite = siteDetail.getName();
		description = siteDetail.getDescription();
		popup = siteDetail.getPopup();
		nomPage = siteDetail.getContent();
	}
	//CBO : FIN ADD

	//CBO : REMOVE settings = new ResourceLocator("com.stratelia.webactiv.webSites.settings.webSiteSettings","fr");

	//CBO : REMOVE String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

	String mandatoryField=m_context+"/util/icons/mandatoryField.gif";

	//current User
	UserDetail actor = scc.getUserDetail();
	auteur = actor.getLastName()+" "+actor.getFirstName();

	//currentDate
	String creationDate = resources.getOutputDate(new Date());

	//CBO : REMOVE
/*	SilverpeasMultipartParser mp = new SilverpeasMultipartParser(request);
	Part part;
    while ((part = mp.readNextPart()) != null) {
		String name = part.getName();

		if (part.isParam()) {
			SilverpeasParamPart paramPart = (SilverpeasParamPart) part;
			if (name.equals("nomSite")) {
				nomSite = paramPart.getStringValue();
				SilverTrace.info("websites", "JSPdescUpload", "root.MSG_GEN_PARAM_VALUE", "nomSite= "+nomSite);
			}
			else if (name.equals("description")) {
				description = paramPart.getStringValue();
				SilverTrace.info("websites", "JSPdescUpload", "root.MSG_GEN_PARAM_VALUE", "desc= "+description);
			}
			else if (name.equals("nomPage")) {
				nomPage = paramPart.getStringValue();
				SilverTrace.info("websites", "JSPdescUpload", "root.MSG_GEN_PARAM_VALUE", "nomPage= "+nomPage);
			}
			else if (name.equals("ListeIcones"))
				listeIcones = paramPart.getStringValue();
			else if (name.equals("ListeTopics"))
				listeTopics = paramPart.getStringValue();
		}

		else if (part.isFile()) {
			FilePart filePart = (FilePart) part;

			//addUpload

			/* recuperation de l'id = nom du directory */
/*			String id = scc.getNextId();

			/* Cr�ation du directory */
			//CBO : UPDATE
			//File directory = new File(settings.getString("uploadsPath")+settings.getString("Context")+"/"+scc.getComponentId()+"/"+id);
/*			File directory = new File(settings.getString("uploadsPath")+settings.getString("Context")+"/"+componentId+"/"+id);
			if (directory.mkdir()) {
				/* creation du zip sur le serveur */
/*				String fichierZipName = filePart.getFileName();
				String type = ".zip";
				//CBO : UPDATE
				//File fichier = new File(settings.getString("uploadsPath")+settings.getString("Context")+"/"+scc.getComponentId()+"/"+id+"/"+fichierZipName);
				File fichier = new File(settings.getString("uploadsPath")+settings.getString("Context")+"/"+componentId+"/"+id+"/"+fichierZipName);
				long size = filePart.writeTo(fichier);
				if (size <= 0)
				uploadOk = false;

				if (uploadOk) {
					/* dezip du fichier.zip sur le serveur */
					//CBO : UPDATE
					//String cheminZip = settings.getString("uploadsPath")+settings.getString("Context")+"/"+scc.getComponentId()+"/"+id;
/*					String cheminZip = settings.getString("uploadsPath")+settings.getString("Context")+"/"+componentId+"/"+id;
					String cheminFichierZip = cheminZip+"/"+fichierZipName;
					scc.unzip(cheminZip, cheminFichierZip);

					/* verif que le nom de la page principale est correcte */
/*					Collection collPages = scc.getAllWebPages2(cheminZip);
					Iterator j = collPages.iterator();
					boolean ok = false;
					while (j.hasNext()) {
						File f = (File) j.next();
						if (f.getName().equals(nomPage)) {
							ok = true;
							break;
						}
					}

					searchOk = ok;

					if (searchOk) {

						/* creation en BD */
/*						ArrayList listIcons = new ArrayList();
						int i = 0;
						int begin = 0;
						int end = 0;
						if (listeIcones != null) {
							end = listeIcones.indexOf(',', begin);
							while(end != -1) {
								listIcons.add(listeIcones.substring(begin, end));
								begin = end + 1;
								end = listeIcones.indexOf(',', begin);
							}
						}

						SiteDetail descriptionSite = new SiteDetail(id, nomSite, description, nomPage, 2, null, null, 0, 1); /* type 2 = site uploade,popup=1 affichage popup par defaut */

						//CBO : UPDATE
/*						//scc.createWebSite(descriptionSite);
						String pubId = scc.createWebSite(descriptionSite);

						if (listIcons.size() > 0)
							scc.associateIcons(id, listIcons);

						/* publications : classer le site dans les themes coch�s */
/*						ArrayList arrayToClassify = new ArrayList();
						boolean publish = false;
						i = 0;
						begin = 0;
						end = 0;
						end = listeTopics.indexOf(',', begin);
						while(end != -1) {
							String idTopic = listeTopics.substring(begin, end);

							begin = end + 1;
							end = listeTopics.indexOf(',', begin);

							// ajout de la publication dans le theme
							//CBO : REMOVE PublicationDetail pubDetail = new PublicationDetail("X", nomSite, description, null, null, null, "", "2", id, "", nomPage);

							//CBO : REMOVE scc.getFolder(idTopic);

							//CBO : UPDATE
							//String newPubId = scc.createPublication(pubDetail);
							scc.addPublicationToFolder(pubId, idTopic);

							publish = true;
						}

						if (publish) {
							arrayToClassify.add(id);
							scc.publish(arrayToClassify); //set etat du site a 1
						}
					}
					else { //le nom de la page principale n'est pas bonne, on supprime ce qu'on a dezipe
						scc.deleteDirectory(cheminZip);
					}


					//site de reference ou pas : reaffichage de la page si pb
					int i = 0;
					int begin = 0;
					int end = 0;
					if (listeIcones != null) {
						end = listeIcones.indexOf(',', begin);
						while(end != -1) {
							String idIcon = listeIcones.substring(begin, end);
							if (idIcon.equals("0")) {
								refChecked = true;
								break;
							}

							begin = end + 1;
							end = listeIcones.indexOf(',', begin);
						}
					}
				}//if searchOk
			}//if uploadOk
		} //partFile
	} //ferme le while
	*/
	//CBO : FIN REMOVE


%>

<!-- descUpload -->

<HTML>

<HEAD>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<%
out.println(gef.getLookStyleSheet());
%>

<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>

<Script language="JavaScript">

<%

//CBO : UPDATE
/*if (! uploadOk) {
    out.println("alert(\""+resources.getString("FileToAttachNotCorrect")+"\");");

}

if (! searchOk) {
    out.println("alert(\""+resources.getString("PrincipalPageNotCorrect")+"\")");
}

if (! nomSite.equals("") && uploadOk && searchOk) {
    out.println("location.replace(\"manage.jsp\");");
}*/
if (uploadOk != null && uploadOk.equals(Boolean.FALSE)) {
	out.println("alert(\""+resources.getString("FileToAttachNotCorrect")+"\");");
}

if (searchOk != null && searchOk.equals(Boolean.FALSE)) {
    out.println("alert(\""+resources.getString("PrincipalPageNotCorrect")+"\")");
}


%>

function isCorrectName(nom) {

    if (nom.indexOf("\"")>-1) {
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

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var nomSite = stripInitialWhitespace(document.descriptionSite.nomSite.value);
     var description = document.descriptionSite.description;
     var nomPage = stripInitialWhitespace(document.descriptionSite.nomPage.value);
     var zip = stripInitialWhitespace(document.descriptionSite.fichierZip.value);


     if (isWhitespace(nomSite)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++;
     }

     if (! isCorrectName(nomSite)) {
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

    if (isWhitespace(zip)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("FichierZip")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++;
     }

     var indexPoint = zip.lastIndexOf(".");
     if (indexPoint > -1) {
        var ext = zip.substring(indexPoint + 1);
        if (ext.toLowerCase() != "zip") {
            errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("FichierZip")%>' <%=resources.getString("ErreurPbZip")%>\n";
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

%>

            <TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
			<FORM NAME="desc" ACTION="manage.jsp" METHOD="POST">
			<input type="hidden" name="Action">
			</FORM>

			<!-- CBO : UPDATE -->
			<!--<FORM NAME="descriptionSite" ACTION="descUpload.jsp" METHOD="POST" ENCTYPE="multipart/form-data">-->
			<FORM NAME="descriptionSite" ACTION="EffectiveUploadSiteZip" METHOD="POST" ENCTYPE="multipart/form-data">
			  <input type="hidden" name="ListeIcones">
			  <input type="hidden" name="ListeTopics">

                <tr>
                        <td class="intfdcolor4"><span class="txtlibform"><%=" "+resources.getString("GML.name")%> : </span></td>
                        <td class="intfdcolor4"><input type="text" name="nomSite" size="60" maxlength="<%=DBUtil.getTextFieldLength()%>" value="<%=nomSite%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
                </tr>

                <tr>
                        <td class="intfdcolor4" valign=top><span class="txtlibform"><%=resources.getString("GML.description")%> : </span></td>
                        <td class="intfdcolor4"><textarea rows="6" name="description" cols="60" maxlength="<%=DBUtil.getTextFieldLength()%>"><%=description%></textarea></td>
                </tr>

                                <tr>
                                            <td class="intfdcolor4"><span class="txtlibform"><%=" "+resources.getString("NomPagePrincipale")%> : </span></td>
                                            <td class="intfdcolor4"><input type="text" name="nomPage" size="60" maxlength="<%=DBUtil.getTextFieldLength()%>" value="<%=nomPage%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
                                </tr>

                                <tr>
                                            <td class="intfdcolor4"><span class="txtlibform"><%=" "+resources.getString("FichierZip")%> : </td>
                                            <td class="intfdcolor4"><input type="file" name="fichierZip" size=60>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
                                </tr>

                <tr>
                        <td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("GML.publisher")%> : </span></td>
                        <td class="intfdcolor4"><%=auteur%></td>
                </tr>
                <tr>
                        <td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("GML.date")%> : </span></td>
                        <td class="intfdcolor4"><%=creationDate%></td>
                </tr>

                    <tr>

            <td class="intfdcolor4"><span class="txtlibform"><%=resources.getString("Reference")%> :
               </span></td>

              <%

				//CBO : ADD
				ArrayList listIcons = new ArrayList();
				int i = 0;
				int begin = 0;
				int end = 0;
				if (listeIcones != null) {
					String idIcon;
					end = listeIcones.indexOf(',', begin);
					while(end != -1) {
						idIcon = listeIcones.substring(begin, end);
						if (idIcon.equals("0")) {
							refChecked = true;
						}

						listIcons.add(idIcon);

						begin = end + 1;
						end = listeIcones.indexOf(',', begin);
					}
				}
				//CBO : FIN ADD

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
						 <!-- CBO : UPDATE -->
                         <!--<td class="intfdcolor4"><input type="checkbox" name="popup" checked>-->
						<% if (popup == 1) {%>
							<td class="intfdcolor4"><input type="checkbox" name="popup" checked>
				        <%}
						else {%>
                            <td class="intfdcolor4"><input type="checkbox" name="popup">
				        <%}%>
		  			    <!-- CBO : FIN UPDATE -->
						</td>
               </tr>

                   <tr>
                        <td class="intfdcolor4" valign=top><span class="txtlibform"><%=resources.getString("IconesAssociees")%> : </span></td>
                        <td class="intfdcolor4">

<%
	//CBO : REMOVE
	/*ArrayList listIcons = new ArrayList();
	int i = 0;
	int begin = 0;
	int end = 0;
	if (listeIcones != null) {
		end = listeIcones.indexOf(',', begin);
		while(end != -1) {
			listIcons.add(listeIcones.substring(begin, end));

			listIcons.add(idIcon);

			begin = end + 1;
			end = listeIcones.indexOf(',', begin);
		}
	}*/
	//CBO : FIN REMOVE


		//CBO : REMOVE Collection c = scc.getAllIcons();

		//CBO : UPDATE
		//Iterator j = c.iterator();
		Iterator j = allIcons.iterator();

		j.next(); // on saute la premiere icone (site important)
		while (j.hasNext()) {
			IconDetail icon = (IconDetail) j.next();
			if (appartient(icon, listIcons))
			  out.println("<input type=\"checkbox\" name=\"icon\" value = \""+icon.getIconPK().getId()+"\" checked>&nbsp;");
			else out.println("<input type=\"checkbox\" name=\"icon\" value = \""+icon.getIconPK().getId()+"\">&nbsp;");
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
