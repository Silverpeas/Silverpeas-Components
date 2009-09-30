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
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>
<%@ page import="com.silverpeas.gallery.model.PhotoDetail"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%
	String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
	
	ResourceLocator multilang = null;
%>

<% 
List 	photos		= (List) request.getAttribute("Photos");
String 	language	= (String) request.getAttribute("Language");

// paramètrage pour l'affichage des photos 
int nbAffiche 	= 0;
int nbParLigne 	= 4;

multilang = new ResourceLocator("com.silverpeas.gallery.multilang.galleryBundle", language);
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/treeview.css">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
function selectImage(url)
{
	window.parent.selectImage(url);
}
</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

<table class="Treeview" width="100%" height="100%"><tr><td valign="top">
<table width="100%">
<%	
if (photos != null)
{
	String	vignette_url = null;
	
	if (photos.size()>0) 
	{
		PhotoDetail photo;
		String idP;
		Iterator itP = (Iterator) photos.iterator();

		while (itP.hasNext()) 
		{
			// affichage de la photo
			out.println("<tr>");
			while (itP.hasNext() && nbAffiche < nbParLigne)
			{			
				photo = (PhotoDetail) itP.next();
				idP = photo.getPhotoPK().getId();
				String name = "";
				String url = "";
				if (photo.getImageName() != null && !photo.getImageName().equals(""))
				{
					name = photo.getImageName();
					String type = name.substring(name.lastIndexOf(".") + 1, name.length());
					url = m_context+"/GalleryInWysiwyg/dummy?ImageId="+idP+"&ComponentId="+photo.getPhotoPK().getInstanceId();
					name = photo.getId() + "_133x100.jpg";
					//vignette_url = FileServer.getUrl("useless", photo.getPhotoPK().getInstanceId(), name, photo.getImageMimeType(), nomRep);
					vignette_url = m_context+"/GalleryInWysiwyg/dummy?ImageId="+idP+"&ComponentId="+photo.getPhotoPK().getInstanceId()+"&Size=133x100";
					if ("bmp".equalsIgnoreCase(type))
						vignette_url = m_context+"/gallery/jsp/icons/notAvailable_"+"fr"+"_133x100.jpg";
				}

				nbAffiche = nbAffiche + 1;
				
				String altTitle = Encode.javaStringToHtmlString(photo.getTitle());
				if (photo.getDescription() != null && photo.getDescription().length() > 0)
					altTitle += " : "+Encode.javaStringToHtmlString(photo.getDescription());
					
				// on affiche encore sur la même ligne
				%>
					<td valign="middle" align="center">
						<table border="0" align="center" width="10" cellspacing="1" cellpadding="0" class="fondPhoto">
							<tr><td align="center" colspan="2">
								<table cellspacing="1" cellpadding="3" border="0" class="cadrePhoto"><tr><td bgcolor="#FFFFFF">
									<a href="javaScript:selectImage('<%=url%>');"><IMG SRC="<%=vignette_url%>" border="0" alt="<%=altTitle%>" title="<%=altTitle%>"></a>
								</td></tr></table>
							</td></tr>
						</table>
					</td>
				<%
			}
			// on passe à la ligne suivante
			nbAffiche = 0;
			out.println("</tr>");
			if (itP.hasNext())
				out.println("<tr><td colspan=\""+nbParLigne+"\">&nbsp;</td></tr>");
		}
	}
	else
	{
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
</td></tr></table>
</body>
</html>