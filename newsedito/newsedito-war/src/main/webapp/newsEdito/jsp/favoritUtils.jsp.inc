<%!

void displayFavoritList(JspWriter out, NewsEditoSessionController news, ResourceLocator settings, ResourceLocator generalMessage) 
  throws NewsEditoException
{
	try{
			Collection favorits = news.getFavoritList();
			
			out.println("<table CELLPADDING=5 CELLSPACING=2 BORDER=0 WIDTH=98% CLASS=intfdcolor align=\"center\">");
			out.println("\t<tr>");
			out.println("\t\t<td CLASS=intfdcolor4 NOWRAP>");
			
			out.println("<TABLE width=\"85%\" align=\"center\" border=0 cellPadding=0 cellSpacing=3>");
			out.println("\t<TR>");
			out.println("\t\t<TD colspan=\"2\" align=\"center\" class=\"intfdcolor\" height=\"1\"><img src=\""+settings.getString("1px")+"\"></TD>");
			out.println("\t</TR>");
			out.println("\t<TR>");
			out.println("\t\t<TD align=\"center\" class=\"txttitrecol\">");
			out.println("\t\t\t"+news.getString("mesTitresFavoris")+"");
			out.println("\t\t</TD>\n");
			out.println("\t\t<TD align=\"center\" class=\"txttitrecol\">");
			out.println("\t\t\t"+generalMessage.getString("GML.operation")+"");
			out.println("\t\t</TD>");
			out.println("\t</TR>");
			out.println("\t<TR>");
			out.println("\t\t<TD colspan=\"2\" align=\"center\" class=\"intfdcolor\" height=\"1\"><img src=\""+settings.getString("1px")+"\"></TD>");
			out.println("\t</TR>");

		  if (favorits == null || favorits.size() == 0)
			{
			out.println("\t<TR>");
			out.println("\t\t<TD align=\"center\" colspan=\"2\">");
			out.println(generalMessage.getString("GML.noFavorit"));
			out.println("\t\t</TD>");
			out.println("\t</TR>");
			}
			Iterator i = favorits.iterator();
		  while (i.hasNext())
		  {
			Collection path = (Collection) i.next();
			Iterator j = path.iterator();
			String link = null;
			String removeLink = null;
			while (j.hasNext()) {
			  NodeDetail header = (NodeDetail) j.next();
			  if (link == null) {
				link = "<A HREF=\"javascript:onClick=viewTitle('" + 
					 header.getNodePK().getId() + 
					 "')\">" + 
					 Encode.javaStringToHtmlString(header.getName()) + "</A>";

				removeLink = "<A HREF=\"javascript:onClick=favoritDeleteConfirm('" + 
					 header.getNodePK().getId() + 
					 "','" + Encode.javaStringToHtmlString(Encode.javaStringToJsString(header.getName())) + "')\" TITLE=\""+news.getString("supprimerFavoriX")+" '" + Encode.javaStringToHtmlString(header.getName()) + "'\">" + 
					 "<img border=\"0\" src=\""+ settings.getString("deleteFavoriteIcon") +"\">" +
					 "</A>";
			  }
			  else 
				if (j.hasNext())
				  link = Encode.javaStringToHtmlString(header.getName()) + "&nbsp;>&nbsp;"  + link;
			}

			out.println("\t<TR>");
			out.println("\t\t<TD align=\"center\">");
			out.println(link);
			out.println("\t\t</TD>");
			out.println("\t\t<TD align=\"center\">");
			out.println(removeLink);
			out.println("\t\t</TD>");
			out.println("\t</TR>");
			
		  }
			out.println("\t<TR>");
			out.println("\t\t<TD colspan=\"2\" align=\"center\" class=\"intfdcolor\" height=\"1\"><img src=\""+settings.getString("1px")+"\"></TD>");
			out.println("\t</TR>");
		  out.println("</TABLE>");
			
			out.println("\t\t</td>");
			out.println("\t</tr>");
			out.println("</table>");  
	}
	catch(Exception e){
		throw new NewsEditoException("favoritsUtils_JSP",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_FAVORIT_LIST",e);
	}
}

%>