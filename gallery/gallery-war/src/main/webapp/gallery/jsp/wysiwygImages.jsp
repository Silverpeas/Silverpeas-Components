<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="com.silverpeas.gallery.model.Media" %>
<%@ page import="com.silverpeas.gallery.model.Photo" %>
<%@ page import="org.silverpeas.util.EncodeHelper" %>
<%@ page import="org.silverpeas.util.LocalizationBundle" %>
<%@ page import="org.silverpeas.util.ResourceLocator" %>
<%@ page import="org.silverpeas.util.StringUtil" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Set resource bundle --%>
<c:set var="language" value="${requestScope.Language}"/>

<fmt:setLocale value="${language}" />
<view:setBundle basename="com.silverpeas.gallery.multilang.galleryBundle"/>

<c:set var="instanceId" value="${requestScope.ComponentId}"/>
<c:set var="medias" value="${requestScope.MediaList}" />


<%
String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

List<Media> mediaList = (List) request.getAttribute("MediaList");
String language = (String) request.getAttribute("Language");

// paramétrage pour l'affichage des photos
int nbAffiche = 0;
int nbParLigne = 4;

  LocalizationBundle multilang =
      ResourceLocator.getLocalizationBundle("org.silverpeas.gallery.multilang.galleryBundle",
          language);
%>

<html>
<head>
<view:looknfeel/>
<link type="text/css" rel="stylesheet" href="<c:url value="/util/styleSheets/treeview.css"/>">
<script type="text/javascript">
function selectImage(url, idP) {
	window.parent.selectImage(url+"&UseOriginal="+$('#useOriginalId' + idP).prop('checked'));
}
</script>
</head>
<body id="${instanceId}" class="gallery gallery-wysiwyg">

<form name="frmVignette">

<table class="Treeview" width="100%" height="100%">
<tr>
<td valign="top">
<table width="100%">

<%
if (mediaList != null) {
    String vignette_url = null;

    if (mediaList.size() > 0) {
      String idP;
      Iterator<Media> itP = mediaList.iterator();

      while (itP.hasNext()) {

        // affichage de la photo
        out.println("<tr>");
        while (itP.hasNext() && nbAffiche < nbParLigne) {
          Photo photo = itP.next().getPhoto();
          if (photo == null) {
            continue;
          }
          idP = photo.getMediaPK().getId();
          String name = "";
          String url = "";
          if (photo.getFileName() != null && !photo.getFileName().equals("")) {
            url = m_context + "/GalleryInWysiwyg/dummy?ImageId=" + idP + "&ComponentId=" + photo.
                getMediaPK().getInstanceId();
            name = photo.getId() + "_133x100.jpg";
            vignette_url = m_context + "/GalleryInWysiwyg/dummy?ImageId=" + idP + "&ComponentId=" + photo.
                getMediaPK().getInstanceId() + "&Size=133x100";
            if (!photo.isPreviewable()) {
              vignette_url = m_context + "/gallery/jsp/icons/notAvailable_" + "fr" + "_133x100.jpg";
            }
          }

          nbAffiche = nbAffiche + 1;

          String altTitle = EncodeHelper.javaStringToHtmlString(photo.getTitle());
          if (StringUtil.isDefined(photo.getDescription())) {
            altTitle += " : " + EncodeHelper.javaStringToHtmlString(photo.getDescription());
          }

%>
          <td valign="middle" align="center">
            <table border="0" align="center" width="10" cellspacing="1" cellpadding="0" class="fondPhoto">
              <tr><td align="center" colspan="2">
                <table cellspacing="1" cellpadding="3" border="0" class="cadrePhoto"><tr><td bgcolor="#FFFFFF">
                  <div style="text-align:right" class="imagename"><%=photo.getFileName() %></div>
                  <a href="javaScript:selectImage('<%=url%>','<%=idP%>');"><img src="<%=vignette_url%>" border="0" alt="<%=altTitle%>" title="<%=altTitle%>"/></a>
                    <input id="useOriginalId<%=idP%>" type="checkbox" name="UseOriginal<%=idP%>" value="true"><font style="font-size: 9px"><%=photo.getDefinition().getWidth()%>x<%=photo.getDefinition().getHeight()%></font><br>
                </td></tr></table>
              </td></tr>
            </table>
          </td>
                    <%
                        }
                        nbAffiche = 0;
                        out.println("</tr>");
                        if (itP.hasNext()) {
                          out.println("<tr><td colspan=\"" + nbParLigne + "\">&nbsp;</td></tr>");
                        }
                      }
                    } else {
                    %>
      <tr>
        <td colspan="5" valign="middle" align="center" width="100%">
          <br>
          <%
            out.println(multilang.getString("gallery.empty.data"));
          %>
          <br>
        </td>
      </tr>
    <%
  }
}
%>



</table>
</td></tr></table>
</form>
</body>
</html>