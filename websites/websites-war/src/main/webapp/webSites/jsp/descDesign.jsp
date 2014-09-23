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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>


<%@ page import="com.stratelia.webactiv.util.DBUtil"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.FolderDetail"%>
<%@ page import="com.stratelia.webactiv.node.model.NodePK"%>

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

  SilverTrace.info("websites", "JSPdescDesign.afficheArbo()", "root.MSG_GEN_PARAM_VALUE", "name Theme ="+rootFolder.getNodeDetail().getName()+", nbEsp = "+nbEsp);
  resultat = arrayPane.print();

  Collection<NodeDetail> subThemes = rootFolder.getNodeDetail().getChildrenDetails();
  if (subThemes != null) {
      Iterator<NodeDetail> coll = subThemes.iterator();
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

        Collection<NodeDetail> subThemes = rootFolder.getNodeDetail().getChildrenDetails();
        if (subThemes != null) {
            Iterator<NodeDetail> coll = subThemes.iterator();
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
  String mandatoryField=m_context+"/util/icons/mandatoryField.gif";
  Collection allIcons = (Collection) request.getAttribute("AllIcons");
%>
<c:set var="allIcons" value="${requestScope.AllIcons}"/>


<!-- descDesign -->
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><%=resources.getString("GML.popupTitle")%></title>

<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<script type="text/javascript">

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
    // on verifie qu'il existe une extension au nom du fichier propos�
    if (indexPoint != -1){
        // le fichier contient une extension. On recupere l'extension
        var ext = filename.substring(indexPoint + 1);
        // on verifie que c'est une extension HTML
        if ( (ext != "html") && (ext != "htm") && (ext != "HTML") && (ext != "HTM") ) {
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
    errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("MustNotContainSpecialChar")%>\n<%=EncodeHelper.javaStringToJsString(resources.getString("Char4"))%>\n";
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
    errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("NomPagePrincipale")%>' <%=resources.getString("MustNotContainSpecialChar")%>\n<%=EncodeHelper.javaStringToJsString(resources.getString("Char5"))%>\n";
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
    var indexPoint = document.descriptionSite.nomPage.value.lastIndexOf(".");
    var ext = document.descriptionSite.nomPage.value.substring(indexPoint + 1);
    if ((ext != "html") && (ext != "htm") && (ext != "HTML") && (ext != "HTM") ) {
      document.descriptionSite.nomPage.value = document.descriptionSite.nomPage.value + ".html"
    }
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
    <view:pdcPositions setIn="document.descriptionSite.Positions.value"/>;
    document.descriptionSite.submit(); /* et on a bien une page html */
  }
}


/*********************************************************************************************************/

function B_ANNULER_ONCLICK() {
  document.desc.Action.value="view";
  document.desc.submit();
}

</script>

</head>

<body class="websites" onload="document.descriptionSite.nomSite.focus()">
<%
  Window window = gef.getWindow();
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(spaceLabel);
  browseBar.setComponentName(componentLabel, "manage.jsp?Action=view");
  browseBar.setPath(resources.getString("CreationSite"));

  Frame frame = gef.getFrame();

  out.println(window.printBefore());
  out.println(frame.printBefore());

  //currentDate
  String creationDate = resources.getOutputDate(new Date());
%>
<form name="desc" action="manage.jsp" method="post">
  <input type="hidden" name="Action" />
</form>

<form name="descriptionSite" action="design.jsp" method="post">
  <input type="hidden" name="Action" value="newSite" />
  <input type="hidden" name="ListeIcones" />
  <input type="hidden" name="ListeTopics" />
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
      <label class="txtlibform" for="nomPage"><fmt:message key="NomPagePrincipale" /> </label>
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
        <input type="checkbox" name="popup" checked/>
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

<view:pdcNewContentClassification componentId="<%=scc.getComponentId()%>" />

<div class="legend">
  <fmt:message key="GML.requiredField" /> : <img src="<%=mandatoryField%>" width="5" height="5" />
</div>


	</form>
<%
	out.println(frame.printMiddle());

	ButtonPane buttonPane = gef.getButtonPane();

	int size = allIcons.size() - 1;

	Button validerButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK("+nbThemes("0", scc, 0)+", "+size+");", false);
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