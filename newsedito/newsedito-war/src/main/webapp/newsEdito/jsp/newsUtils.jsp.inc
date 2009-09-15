
<%!


void displaySearchBar(JspWriter out, NewsEditoSessionController news) 
  throws NewsEditoException
{
	try{
	  out.println("<TABLE BORDER=0 WIDTH=100% >");
	  out.println("  <TR>");
	  out.println("    <TD colspan=2 ALIGN=CENTER>");
	  out.println(news.getString("maRecherche"));
	  out.println("    </TD>");
	  out.println("  </TR>");
	  out.println("  <TR>");
	  out.println("    <TD WIDTH=50% ALIGN=RIGHT>");
	  out.println("<input type=\"text\" name=\"query\">");
	  out.println("    </TD>");
	  out.println("    <TD WIDTH=50% ALIGN=LEFT>");
	  out.println("<A HREF=\"javascript:onClick=searchString()\">");
	  out.println(news.getString("rechercher"));
	  out.println("    </TD>");
	  out.println("  </TR>");
	  out.println("</TABLE>");
	}
	catch(Exception e){
		throw new NewsEditoException("newsUtils_JSP.displaySearchBar",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_SEARCHBAR",e);			
	}
}


void displayTopBanner(JspWriter out, NodeDetail nodeDetail, NewsEditoSessionController news) 
throws NewsEditoException 
{
	try{
	  if (nodeDetail == null) {
		out.println("<TABLE WIDTH=100% BGCOLOR=\"#FFCC99\">");
		out.println(" <TR><TD align=left>");
		out.println(news.getString("aucunJournalSelectionne"));
		out.println(" </TD></TR></TABLE>");
		return;
	  }
	  
	  out.println("<TABLE WIDTH=100% BGCOLOR=\"#FFCC99\">");
	  out.println(" <TR><TD width=25% align=left>");
	  out.println("<font size=+2>"+news.getString("journalEditorial")+"</font>");
	  out.println("   </TD>");
	  out.println("   <TD width=50% align=center>");
	  out.println("<font size=+1>" + Encode.javaStringToHtmlString(nodeDetail.getDescription()) +"</font>");
	  
	  out.println("   </TD>");
	  out.println("   <TD width=25% align=right>");
	  out.println("<A HREF=\"javascript:onClick=archiveDeleteConfirm('"+
		   Encode.javaStringToHtmlString(Encode.javaStringToJsString(nodeDetail.getName())) +"')\" INFO=\""+
		  news.getString("supprimerArchive")+"\">" +
		  "<img border=\"0\" src=\"icons/delete.gif\" width=\"32\" height=\"32\">" +
		  "</A>");

	  out.println(" <A HREF=\"javascript:onClick=pdfGeneration()\" INFO=\""+
		  news.getString("genererPdf") + "\">" +
		  "<img border=\"0\" src=\"icons/pdf.bmp\" width=\"32\" height=\"32\">" +
		  "</A>");
	  out.println("   </TD>");
	  out.println("   <TD> ");
	  out.println("<A HREF=\"javascript:onClick=viewArchiveStatistic('"+nodeDetail.getNodePK().getId()+"')\">");
	  out.println("Statistiques");
	  out.println("</A>");
	  out.println("  </TD> ");
	  out.println("  </TR>");
	  out.println("</TABLE>");
	}
	catch(Exception e){
		throw new NewsEditoException("newsUtils_JSP.displayTopBanner",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_TOP_FRAME",e);			
	}
}

void displayBottomBanner(JspWriter out, boolean withActions, Collection archives, NewsEditoSessionController news) 
throws NewsEditoException 
{
	try{
	  out.println("<TABLE WIDTH=100% BGCOLOR=\"#FFCC99\">");
	  out.println("  <TR>");
	  out.println("      <TD> ");
	  out.println(news.getString("journauxPrecedents") + " : ");
	  out.println("      </TD> ");
	  out.println("      <TD> ");

	  String list = null;
	  if (archives != null) {
		Iterator iterator = archives.iterator();
		while (iterator.hasNext())
		{
		  NodeDetail detail = (NodeDetail) iterator.next();
		  if (list != null) list = list + ", ";
		  else list = "";
		  list = list + 
			  "<A HREF=\"javascript:onClick=viewArchive('"+detail.getNodePK().getId() +
				  "')\">" +
				  Encode.javaStringToHtmlString(detail.getName())+
				  "</A>";
		}
	  }

	  if (withActions) {
		if (list == null) 
		  list = "";
		else
		  list = list + ", ";
		list = list + "<A HREF=\"javascript:onClick=addArchive()\">" +
			  news.getString("ajouterArchive") +
			  "</A>";
	  }
	  if (list != null)
	  out.println(list);
	  out.println("     </TD>");
	  out.println("      <TD> ");
	  displayLanguageSelect(out);
	  out.println("      </TD> ");
	  out.println("      <TD> ");
	  out.println("<A HREF=\"javascript:onClick=viewStatistic()\">");
	  out.println("Statistiques");
	  out.println("</A>");
	  out.println("      </TD> ");
	  out.println("  </TR>");

	  out.println("</TABLE>");
	}
	catch(Exception e){
		throw new NewsEditoException("newsUtils_JSP.displayBottomBanner",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_BOTTOM_FRAME",e);			
	}
}

void displayLanguageSelect(JspWriter out)
throws NewsEditoException
{
	try{
	  out.println("<TABLE WIDTH=100% >");
	  out.println("  <TR>");
	  out.println("    <TD> ");
	  out.println("      <A HREF=\"javascript:onClick=selectLanguage('fr')\">");
	  out.println("        <IMG SRC=\"icons/fr.gif\">");
	  out.println("      </A>");
	  out.println("    </TD> ");
	  out.println("    <TD> ");
	  out.println("      <A HREF=\"javascript:onClick=selectLanguage('en')\">");
	  out.println("        <IMG SRC=\"icons/uk.gif\">");
	  out.println("      </A>");
	  out.println("    </TD> ");
	  out.println("  </TR>");
	  out.println("</TABLE>");
	}
	catch(Exception e){
		throw new NewsEditoException("newsUtils_JSP.displayLanguageSelect",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_LANGUAGE_SELECTION",e);			
	}
}

void displayGoBackBanner(JspWriter out, String actionDone, String goBackLink) 
  throws NewsEditoException, IOException
{
	try{
	  out.println("<TABLE width=100% cellspacing=10>");
	  out.println(" <TR>");
	  out.println("   <TD align=center>");
	  
	  out.println( actionDone );
	  out.println("     </TD>");
	  out.println("  </TR>");
	  out.println(" <TR>");
	  out.println("   <TD align=center>");
	  out.println(goBackLink);
	  out.println("   </TD>");
	  out.println("  </TR>");

	  out.println("</TABLE>");
	}
	catch(Exception e){
		throw new NewsEditoException("newsUtils_JSP.displayGoBackBanner",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_GOBACK_FRAME",e);			
	}
}

%>