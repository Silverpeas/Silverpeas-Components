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
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>
<%@ page import="com.silverpeas.gallery.model.PhotoDetail"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.silverpeas.util.EncodeHelper"%>
<%
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

List photos = (List) request.getAttribute("Photos");
String language = (String) request.getAttribute("Language");
    
// param�trage pour l'affichage des photos 
int nbAffiche = 0;
int nbParLigne = 4;
    
ResourceLocator multilang = new ResourceLocator("com.silverpeas.gallery.multilang.galleryBundle", language);
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/treeview.css">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
function selectImage(url, idP)
{
	window.parent.selectImage(url+"&UseOriginal="+eval("document.frmVignette.UseOriginal"+idP+".checked"));
}
</script>
</head>
<body leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

<table class="Treeview" width="100%" height="100%"><tr><td valign="top">
<form name="frmVignette">
<table width="100%">
<%	
if (photos != null) {
    String vignette_url = null;
      
    if (photos.size() > 0) {
      PhotoDetail photo;
      String idP;
      Iterator itP = photos.iterator();
        
      while (itP.hasNext()) {
        // affichage de la photo
        out.println("<tr>");
        while (itP.hasNext() && nbAffiche < nbParLigne) {
          photo = (PhotoDetail) itP.next();
          idP = photo.getPhotoPK().getId();
          String name = "";
          String url = "";
          if (photo.getImageName() != null && !photo.getImageName().equals("")) {
            url = m_context + "/GalleryInWysiwyg/dummy?ImageId=" + idP + "&ComponentId=" + photo.
                getPhotoPK().getInstanceId();
            name = photo.getId() + "_133x100.jpg";
            vignette_url = m_context + "/GalleryInWysiwyg/dummy?ImageId=" + idP + "&ComponentId=" + photo.
                getPhotoPK().getInstanceId() + "&Size=133x100";
            if (!photo.isPreviewable()) {
              vignette_url = m_context + "/gallery/jsp/icons/notAvailable_" + "fr" + "_133x100.jpg";
            }
          }
            
          nbAffiche = nbAffiche + 1;
            
          String altTitle = EncodeHelper.javaStringToHtmlString(photo.getTitle());
          if (StringUtil.isDefined(photo.getDescription())) {
            altTitle += " : " + EncodeHelper.javaStringToHtmlString(photo.getDescription());
          }
            
          // on affiche encore sur la m�me ligne
%>
					<td valign="middle" align="center">
						<table border="0" align="center" width="10" cellspacing="1" cellpadding="0" class="fondPhoto">
							<tr><td align="center" colspan="2">
								<table cellspacing="1" cellpadding="3" border="0" class="cadrePhoto"><tr><td bgcolor="#FFFFFF">
									<div style="text-align:right" class="imagename"><%=photo.getImageName() %></div>
									<a href="javaScript:selectImage('<%=url%>','<%=idP%>');"><img src="<%=vignette_url%>" border="0" alt="<%=altTitle%>" title="<%=altTitle%>"/></a>
										<input type="checkbox" name="UseOriginal<%=idP%>" value="true"><font style="font-size: 9px"><%=photo.getSizeL()%>x<%=photo.getSizeH()%></font><br>
								</td></tr></table>
							</td></tr>
						</table>
					</td>
                    <%
                        }
                        // on passe � la ligne suivante
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
						out.println(multilang.getString("gallery.pasPhoto"));
					%>
					<br>
				</td>
			</tr>
		<%
	}
}
%>
</table>
</form>
</td></tr></table>
</body>
</html>