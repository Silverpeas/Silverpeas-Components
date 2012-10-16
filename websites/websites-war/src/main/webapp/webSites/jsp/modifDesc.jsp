<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>


<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>

<%@ page import="com.stratelia.webactiv.util.DBUtil"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.FolderDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>
<%@ page import="com.stratelia.webactiv.util.DateUtil"%>

<%@ include file="util.jsp" %>
<%@ include file="checkScc.jsp" %>


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="recupParam" value="${requestScope['RecupParam']}" />

<c:set var="siteDetail" value="${requestScope['Site']}" />
<c:set var="curWebSiteId" value="${requestScope['Id']}" />
<c:if test="${empty curWebSiteId and not empty siteDetail}">
  <c:set var="curWebSiteId" value="${siteDetail.sitePK.id}" />
</c:if>
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
          if (theId.equals(id)) {
            ok = true;
          }
      }
      return ok;
}

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

        if (isPublishedInTopic(scc, idSite, rootFolder.getNodeDetail().getNodePK().getId())) {
            arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"topic\" value=\""+rootFolder.getNodeDetail().getNodePK().getId()+"\" checked>");
        } else { 
          arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"topic\" value=\""+rootFolder.getNodeDetail().getNodePK().getId()+"\">");
        }

        resultat = arrayPane.print();

        Collection<NodeDetail> subThemes = rootFolder.getNodeDetail().getChildrenDetails();
        if (subThemes != null) {
            Iterator<NodeDetail> coll = subThemes.iterator();
            while (coll.hasNext()) {
                  NodeDetail theme = coll.next();
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

            Collection<NodeDetail> subThemes = rootFolder.getNodeDetail().getChildrenDetails();
            if (subThemes != null) {
                Iterator<NodeDetail> coll = subThemes.iterator();
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
	String mandatoryField=m_context+"/util/icons/mandatoryField.gif";

	String id = request.getParameter("Id");
	String currentPath = request.getParameter("path"); /* = null ou rempli si type= design */
	String type = request.getParameter("type"); // null  ou design

	SiteDetail site = (SiteDetail) request.getAttribute("Site");

	Collection listIcons = (Collection) request.getAttribute("ListIcons");
	Collection allIcons = (Collection) request.getAttribute("AllIcons");
	
	String recupParam = request.getParameter("RecupParam"); //=null ou oui
	String nom;
	String description;
	String lapage;
	ArrayList icones = new ArrayList();
	boolean refChecked = false;

	if (recupParam != null) {//=oui
		nom = request.getParameter("Nom");
		if (nom == null) {
		  nom = "";
		}
		description = request.getParameter("Description");
		if (description == null) {
		  description = "";
		}
		lapage = request.getParameter("Page");
		if (lapage == null) {
		  lapage = "";
		}
		String listeIcones = request.getParameter("ListeIcones");
		int i = 0;
		int begin = 0;
		int end = 0;
		if (listeIcones != null) {
			end = listeIcones.indexOf(',', begin);
			while(end != -1) {
				String num = listeIcones.substring(begin, end);
				if (num.equals("0")) {
					refChecked = true;
				}
				icones.add(num);
				begin = end + 1;
				end = listeIcones.indexOf(',', begin);
			}
		}
	} else { //recup BD
		nom = site.getName();
		description = site.getDescription();
		if (description == null) {
      description = "";
    }

		lapage = site.getContent();
		icones = new ArrayList(listIcons);

		//site de reference ou pas
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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><fmt:message key="GML.popupTitle"/></title>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">

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
        errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("MustNotContainSpecialChar")%>\n<%=EncodeHelper.javaStringToJsString(resources.getString("Char4"))%>\n";
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
    } else {
        errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("NomPagePrincipale")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
        errorNb++;
    }
  }
  
  if (type == 1) {
    if (! isCorrectURL(nomPage)) {
      errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("URL")%>' <%=EncodeHelper.javaStringToJsString(resources.getString("MustNotContainSpecialChar"))%>\n<%=EncodeHelper.javaStringToJsString(resources.getString("Char1"))%>";
      errorNb++;
    }
  } else {
    if (! isCorrect(nomPage)) {
      errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("NomPagePrincipale")%>' <%=EncodeHelper.javaStringToJsString(resources.getString("MustNotContainSpecialChar"))%>\n<%=EncodeHelper.javaStringToJsString(resources.getString("Char5"))%>\n";
      errorNb++;
    }
  }

     <view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>
     
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
		if (document.descriptionSite.radio[0].checked) {
			f += "0,";
		}
		for (i=0; i<nbicones; i++) {
		  if (document.descriptionSite.icon[i].checked) {
			  f += document.descriptionSite.icon[i].value + ",";
		  }
		}
		document.descriptionSite.ListeIcones.value = f;

		f = "";
		if (nbthemes == 1) {
			if (document.descriptionSite.topic.checked) {
				f += document.descriptionSite.topic.value + ",";
			}
		} else {
			for (i=0; i<nbthemes; i++) {
			  if (document.descriptionSite.topic[i].checked) {
				  f += document.descriptionSite.topic[i].value + ",";
			  }
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

</script>
</head>
<body class="websites"  onload="document.descriptionSite.nomSite.focus()">
<%
	String theAction = "";
	if (type == null) { //pour sites bookmark
		theAction = "manage.jsp";
	} else {//cas de retour a design pour sites upload et sites design
		theAction = "design.jsp";
	}

  Window window = gef.getWindow();
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(spaceLabel);
  browseBar.setComponentName(componentLabel, "manage.jsp?Action=view");
  if (site.getType() == 1) { //bookmark
    browseBar.setPath(resources.getString("ModificationSite"));
  } else { //autres sites
    browseBar.setPath(infoPath+" - "+resources.getString("ModificationSite"));
  }

  Frame frame = gef.getFrame();
 
  out.println(window.printBefore());
  out.println(frame.printBefore());

	String creationDate = resources.getOutputDate(site.getCreationDate());

	int popup = site.getPopup();
%>
<form name="descriptionSite" action="<%=theAction%>" method="post">
  <input type="hidden" name="Action" value="updateDescription"/>
  <input type="hidden" name="Id" value="<%=id%>"/>
  <input type="hidden" name="path" value="<%=currentPath%>"/>
  <input type="hidden" name="ListeIcones"/>
  <input type="hidden" name="ListeTopics"/>
  <input type="hidden" name="etat" value="<%=String.valueOf(site.getState())%>" />

<fieldset id="infoFieldset" class="skinFieldset">
  <legend><fmt:message key="websites.header.fieldset.info" /></legend>
  <!-- Website info form -->
  <div class="fields">
    <!-- Website name -->
    <div class="field" id="nomSiteArea">
      <label class="txtlibform" for="nomSite"><fmt:message key="GML.name" /> </label>
      <div class="champs">
        <input type="text" name="nomSite" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>" value="<%=EncodeHelper.javaStringToHtmlString(nom)%>"/>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
      </div>
    </div>
    <div class="field" id="descriptionArea">
      <label class="txtlibform" for="description"><fmt:message key="GML.description" /> </label>
      <div class="champs">
        <textarea name="description" id="description" rows="4" cols="60"><%=description%></textarea>
      </div>
    </div>
    <div class="field" id="nomPageArea">
      <label class="txtlibform" for="nomPage"><fmt:message key="NomPagePrincipale" /> </label>
      <div class="champs">
        <input type="text" name="nomPage" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>" value="<%=EncodeHelper.javaStringToHtmlString(lapage)%>" />&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
      </div>
    </div>
    <div class="field" id="publisherArea">
      <label class="txtlibform" for="publisher"><fmt:message key="GML.publisher" /> </label>
      <div class="champs"><view:username userId="<%=site.getCreatorId()%>"/></div>
    </div>
    <div class="field" id="dateArea">
      <label class="txtlibform" for="date"><fmt:message key="GML.date" /> </label>
      <div class="champs"><%=creationDate%></div>
    </div>
    
  </div>
</fieldset>      

<fieldset id="specificationsFieldset" class="skinFieldset">
  <legend><fmt:message key="websites.header.fieldset.specifications" /></legend>
  <div class="fields">
    <div class="field" id="radioArea">
      <label class="txtlibform" for="radio"><fmt:message key="Reference" /> </label>
      <div class="champs">
        <input type="radio" name="radio" value="" <%=refChecked?"checked='checked'":""%>/>&nbsp;<fmt:message key="GML.yes" />&nbsp;&nbsp;
        <input type="radio" name="radio" value="" <%=refChecked?"":"checked='checked'"%>/>&nbsp;<fmt:message key="GML.no" />
      </div>
    </div>
    <div class="field" id="popupArea">
      <label class="txtlibform" for="popup"><fmt:message key="OuvrirPopup" /> </label>
      <div class="champs">
        <input type="checkbox" name="popup" <%=(popup == 1)?"checked":"" %>/>
      </div>
    </div>
    <div class="field" id="iconArea">
      <label class="txtlibform" for="popup"><fmt:message key="IconesAssociees" /> </label>
      <div class="champs">


<%

Iterator<IconDetail> iconIterator = allIcons.iterator();

iconIterator.next(); // on saute la premiere icone (site important)

while (iconIterator.hasNext()) {
  IconDetail icon = (IconDetail) iconIterator.next();
  out.println("<div class=\"specification-tag\">");
  if (recupParam != null) {//=oui
      if (appartientId(icon, icones)) {
          out.println("<input type=\"checkbox\" name=\"icon\" value = \""+icon.getIconPK().getId()+"\" checked>&nbsp;");
      } else {
        out.println("<input type=\"checkbox\" name=\"icon\" value = \""+icon.getIconPK().getId()+"\">&nbsp;");
      }
  } else {
      if (appartient(icon, icones)) {
        out.println("<input type=\"checkbox\" name=\"icon\" value = \""+icon.getIconPK().getId()+"\" checked>&nbsp;");
      }
      else {
        out.println("<input type=\"checkbox\" name=\"icon\" value = \""+icon.getIconPK().getId()+"\">&nbsp;");
      }
  }
    out.println("<img src=\""+icon.getAddress()+"\" alt=\""+resources.getString(icon.getDescription())+"\" align=absmiddle title=\""+resources.getString(icon.getDescription())+"\">&nbsp;&nbsp;");
    out.println(resources.getString(icon.getName()));
    out.println("</div>");
}
%>
      </div>
    </div>
  </div>
</fieldset>

<% if (type == null) { %>
<fieldset id="foldersFieldset" class="skinFieldset">
  <legend><fmt:message key="websites.header.fieldset.folders" /></legend>

<%
  //Recupere la liste des themes ou le site est deja publie
  ArrayPane arrayPane = gef.getArrayPane("siteList", "", request, session);
  arrayPane.setVisibleLineNumber(1000);
  //Definition des colonnes du tableau
	ArrayColumn arrayColumnTopic = arrayPane.addArrayColumn(resources.getString("NomThemes"));
	arrayColumnTopic.setSortable(false);
	ArrayColumn arrayColumnPub = arrayPane.addArrayColumn(resources.getString("GML.publish"));
	arrayColumnPub.setSortable(false);

	String resultat = afficheArbo(site.getSitePK().getId(), arrayPane, "0", scc, 0);

	out.println(resultat);
%>
</fieldset>
<% } %>

<view:pdcClassification componentId="<%=scc.getComponentId()%>" contentId="${curWebSiteId}" editable="true" />

<div class="legend">
  <fmt:message key="GML.requiredField" /> : <img src="<%=mandatoryField%>" width="5" height="5" />
</div>

</form>

<%
	out.println(frame.printMiddle());

	int size = allIcons.size() - 1;

	ButtonPane buttonPane = gef.getButtonPane();
	Button validerButton = null;
	if (type == null) {
		validerButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK("+nbThemes("0", scc, 0)+", "+size+", '"+site.getType()+"');", false);
	} else {
    	validerButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK(0, "+size+", '"+site.getType()+"');", false);
  	}

	Button annulerButton = gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false);
	buttonPane.addButton(validerButton);
	buttonPane.addButton(annulerButton);
	buttonPane.setHorizontalPosition();

	out.println("<br/><center>"+buttonPane.print()+"</center>");

	out.println(frame.printAfter());
	out.println(window.printAfter());

%>
</body>
</html>