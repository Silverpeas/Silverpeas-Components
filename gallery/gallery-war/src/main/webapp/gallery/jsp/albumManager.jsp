<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:includePlugin name="popup"/>

<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<fmt:message var="mandatoryIconPath" key="gallery.obligatoire" bundle="${icons}"/>
<c:url var="mandatoryIcon"      value="${mandatoryIconPath}" />
<c:url var="formCheckingScript" value="/util/javaScript/checkForm.js"/>

<script type="text/javascript" src="${formCheckingScript}"></script>
<script type="text/javascript">
  function openGalleryEditor(gallery)
  {
    var title = "<fmt:message key='gallery.ajoutAlbum'/>";
    if (gallery) {
      title = "<fmt:message key='gallery.updateAlbum'/> " + gallery.name;
      document.galleryForm.action = "UpdateAlbum";
      document.galleryForm.Id.value = gallery.id;
      document.galleryForm.Name.value = gallery.name;
      document.galleryForm.Description.value = gallery.description;
    } else {
      document.galleryForm.action = "CreateAlbum";
      document.galleryForm.Id.value = "";
      document.galleryForm.Name.value = "";
      document.galleryForm.Description.value = "";
    }
    $("#galleryEditor").popup({
      title: title,
      callback: function() {
        var isCorrect = validateGalleryForm();
        if (isCorrect) 
        {
          document.galleryForm.submit();
          
        }
        return isCorrect;
      }
    });
  }
		
  function validateGalleryForm() 
  {
    var errorMsg = "";
    var errorNb = 0;
    var name = stripInitialWhitespace(document.galleryForm.Name.value);
    if (name == "") 
    {
      errorMsg+="  - '<fmt:message key="GML.name"/>' <fmt:message key="GML.MustBeFilled"/>\n";
      errorNb++;
    } 
    switch(errorNb) 
    {
      case 0 :
        result = true;
        break;
      case 1 :
        errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/>: \n" + errorMsg;
        window.alert(errorMsg);
        result = false;
        break;
      default :
        errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb + " <fmt:message key="GML.error"/>:\n" + errorMsg;
        window.alert(errorMsg);
        result = false;
        break;
      } 
      return result;
    }	
</script>

<div id="galleryEditor" style="display: none">
  <form name="galleryForm" method="post" action="">
    <table cellpadding="5" width="100%">
      <tr>
        <td class="txtlibform"><fmt:message key="GML.name"/>&nbsp;:</td>
        <td><input type="text" name="Name" value="" size="60" maxlength="150">
          <img src="${mandatoryIcon}" width="5" height="5" border="0"/>
          <input type="hidden" name="Id" value=""/></td>
      </tr>
      <tr>
        <td class="txtlibform"><fmt:message key="GML.description"/>&nbsp;:</td>
        <td><input type="text" name="Description" value="" size="60" maxlength="150"></td>
      </tr>
      <tr><td colspan="2">(<img border="0" src="${mandatoryIcon}" width="5" height="5"/>&nbsp;: <fmt:message key="GML.requiredField"/>)</td></tr>
    </table>
  </form>
</div>
