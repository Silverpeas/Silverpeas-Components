<%--

    Copyright (C) 2000 - 2011 Silverpeas

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


<%@ page import="com.stratelia.webactiv.util.DBUtil"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.FolderDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>

<%@ include file="checkScc.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

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

  Collection<NodeDetail> subThemes = rootFolder.getNodeDetail().getChildrenDetails();
  if (subThemes != null) {
      Iterator<NodeDetail> coll = subThemes.iterator();
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
  
  Collection<NodeDetail> subThemes = rootFolder.getNodeDetail().getChildrenDetails();
  if (subThemes != null) {
      Iterator<NodeDetail> coll = subThemes.iterator();
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
    String mandatoryField=m_context+"/util/icons/mandatoryField.gif";
    String action = (String) request.getAttribute("Action");
    if (action == null) {// action = desc || suggest
        action = "desc";
    }

	Collection allIcons = (Collection) request.getAttribute("AllIcons");
%>
<c:set var="allIcons" value="${requestScope.AllIcons}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
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
   errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("URL")%>' <%=EncodeHelper.javaStringToJsString(resources.getString("MustNotContainSpecialChar"))%>\n<%=EncodeHelper.javaStringToJsString(resources.getString("Char1"))%>\n";
   errorNb++;
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

/******************************************************/

function B_VALIDER_ONCLICK(nbthemes, nbicones) {
  if (isCorrectForm()) {
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
		}
		else {
			for (i=0; i<nbthemes; i++) {
			  if (document.descriptionSite.topic[i].checked) {
				  f += document.descriptionSite.topic[i].value + ",";
			  }
			}
		}
		document.descriptionSite.ListeTopics.value = f;
    <view:pdcPositions setIn="document.descriptionSite.Positions.value"/>;    
		document.descriptionSite.submit();
  }
}


/*********************************************************************************************************/

function B_SUGGERER_ONCLICK(nb, nom) {

    if (isCorrectForm()) {
		f = "";
		if (document.descriptionSite.radio[0].checked) {
			f += nom+",";
    }

		for (i=0; i<nb; i++) {
		  if (document.descriptionSite.icon[i].checked) {
			  f += document.descriptionSite.icon[i].value + ",";
		  }
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
</script>
</head>
<body class="websites bookmarks" onload="document.descriptionSite.nomSite.focus()">
<%
  Window window = gef.getWindow();
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(spaceLabel);
  if (action.equals("suggest")) {
  	browseBar.setComponentName(componentLabel, "listSite_reader.jsp");
    browseBar.setPath(resources.getString("Suggerer"));
  } else {
	browseBar.setComponentName(componentLabel, "manage.jsp?Action=view");
    browseBar.setPath(resources.getString("CreationSite"));
  }

  //Le cadre
  Frame frame = gef.getFrame();
  
  //Start code
  out.println(window.printBefore());
  out.println(frame.printBefore());

  //current User
  UserDetail actor = scc.getUserDetail();
  
  //currentDate
  String creationDate = resources.getOutputDate(new Date());
%>

<form name="descriptionSite" action="manage.jsp" method="post">
  <input type="hidden" name="Action" value="addBookmark"/>
  <input type="hidden" name="ListeIcones"/>
  <input type="hidden" name="ListeTopics"/>
  <input type="hidden" name="auteur" value="<%=actor.getLastName()+" "+actor.getFirstName()%>"/>
  <input type="hidden" name="date" value="<%=creationDate%>"/>
  <input type="hidden" name="Positions" />


<fieldset id="infoFieldset" class="skinFieldset">
  <legend><fmt:message key="websites.header.fieldset.info" /></legend>
  <!-- Website info form -->
  <div class="fields">
    <!-- Website name -->
    <div class="field" id="nomSiteArea">
      <label class="txtlibform" for="nomSite"><fmt:message key="GML.name" /> </label>
      <div class="champs">
        <input type="text" name="nomSite" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>"/>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
      </div>
    </div>
    <div class="field" id="descriptionArea">
      <label class="txtlibform" for="description"><fmt:message key="GML.description" /> </label>
      <div class="champs">
        <textarea name="description" id="description" rows="4" cols="60"></textarea>
      </div>
    </div>
    <div class="field" id="nomPageArea">
      <label class="txtlibform" for="nomPage"><fmt:message key="URL" />  (ex : www.lesite.com)</label>
      <div class="champs">
        <input type="text" name="nomPage" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>"/>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
      </div>
    </div>
    <div class="field" id="publisherArea">
      <label class="txtlibform" for="publisher"><fmt:message key="GML.publisher" /> </label>
      <div class="champs"><view:username userId="<%=scc.getUserId()%>"/></div>
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
        <input type="radio" name="radio" value=""/>&nbsp;<fmt:message key="GML.yes" />&nbsp;&nbsp;
        <input type="radio" name="radio" value="" checked="checked"/>&nbsp;<fmt:message key="GML.no" />
      </div>
    </div>
    <div class="field" id="popupArea">
      <label class="txtlibform" for="popup"><fmt:message key="OuvrirPopup" /> </label>
      <div class="champs">
        <input type="checkbox" name="popup" checked="checked"/>
      </div>
    </div>
    <div class="field" id="iconArea">
      <label class="txtlibform" for="popup"><fmt:message key="IconesAssociees" /> </label>
      <div class="champs">
        <c:forEach var="icon" items="${allIcons}" varStatus="iconStatus">
          <!-- Dont display the first element (important site) -->
          <c:if test="${not iconStatus.first}">
          	<div class="specification-tag">
		        <input type="checkbox" name="icon" value="${icon.iconPK.id}"/>&nbsp;
		        <fmt:message var="iconTitleMsg" key="${icon.description}" />
	    	    <img src="${icon.address}" alt="${icon.description}" title="${iconTitleMsg}"/>&nbsp;&nbsp;<fmt:message key="${icon.name}" />
	    	</div>
          </c:if>
        </c:forEach>
      </div>
    </div>
  </div>
</fieldset>    


<%

  if (! action.equals("suggest")) {
%>
<fieldset id="foldersFieldset" class="skinFieldset">
  <legend><fmt:message key="websites.header.fieldset.folders" /></legend>

<%      
  		ArrayPane arrayPane = gef.getArrayPane("siteList", "", request, session);
      arrayPane.setVisibleLineNumber(1000);

      //Definition des colonnes du tableau
      ArrayColumn arrayColumnTopic = arrayPane.addArrayColumn(resources.getString("NomThemes"));
      arrayColumnTopic.setSortable(false);
      ArrayColumn arrayColumnPub = arrayPane.addArrayColumn(resources.getString("GML.publish"));
      arrayColumnPub.setSortable(false);
      String resultat = afficheArbo(arrayPane, "0", scc, 0);
      out.println(resultat);
%>
</fieldset>
<%
  }
%>

<view:pdcNewContentClassification componentId="<%=scc.getComponentId()%>" />

<div class="legend">
  <fmt:message key="GML.requiredField" /> : <img src="<%=mandatoryField%>" width="5" height="5" />
</div>

</form>

<%
  //fin du code
  out.println(frame.printMiddle());
  
  ButtonPane buttonPane = gef.getButtonPane();
  Button validerButton = null;
  Button annulerButton = null;
  
  int size = allIcons.size() - 1;
  Iterator i = allIcons.iterator();

  IconDetail icon = (IconDetail) i.next(); // on saute la premiere icone (site important)
  String nameReference = resources.getString(icon.getName());
  
  if (action.equals("suggest")) {
      validerButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_SUGGERER_ONCLICK("+size+", '"+nameReference+"');", false);
      annulerButton = gef.getFormButton(resources.getString("GML.cancel"), "listSite_reader.jsp", false);
  } else {
      validerButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK("+nbThemes("0", scc, 0)+", "+size+");", false);
      annulerButton = gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false);
  }
  buttonPane.addButton(validerButton);
  buttonPane.addButton(annulerButton);
  buttonPane.setHorizontalPosition();
  
  out.println("<br/><center>"+buttonPane.print()+"</center><br/>");
  
  out.println(frame.printAfter());
  out.println(window.printAfter());

%>

<form name="suggestionSite" action="SuggestLink" method="post">
  <input type="hidden" name="nomSite"/>
  <input type="hidden" name="description"/>
  <input type="hidden" name="nomPage"/>
  <input type="hidden" name="auteur"/>
  <input type="hidden" name="date"/>
  <input type="hidden" name="ListeIcones"/>
</form>
</body>
</html>