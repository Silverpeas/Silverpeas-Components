<%@ page import="com.stratelia.webactiv.newsEdito.NewsEditoException"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>

<%!

void displayEditorial(JspWriter out, Collection pubList,String modelId,NewsEditoSessionController news , boolean displayTitle,String publicationTitleAction ,String titleClass,String descriptionClass) 
  throws NewsEditoException 
{
	try{
		if (modelId.equals("1"))
			displayEditorialModel1(out,pubList,news ,displayTitle,publicationTitleAction ,titleClass,descriptionClass) ;
		else if (modelId.equals("2"))
			displayEditorialModel2(out,pubList,news ,displayTitle,publicationTitleAction ,titleClass,descriptionClass) ;
	}
	catch(Exception e){
		throw new NewsEditoException("publicationUtils_JSP.displayEditorial",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_EDITO",e);			
	}
}

void displayEditorialModel1(JspWriter out, Collection pubList,NewsEditoSessionController news , boolean displayTitle,String publicationTitleAction ,String titleClass,String descriptionClass) 
  throws NewsEditoException 
{
  try{
	  Iterator i = pubList.iterator();
	  String[] align = {"right","left"};
	  int indiceAlign = 0;
	  while	(i.hasNext()) {
	  
		Object element = i.next();
		if (element instanceof PublicationDetail)
		{	
			String m_context = "../..";	    
			
			PublicationDetail pub = (PublicationDetail) element;
			out.println("<table width='100%' border=0 cellspacing=1 cellpadding=3>");
			out.println(" <TR>");
			out.print("   <TD><br>");
			
			
			if (pub.getImage()!=null)
			{
				//StringTokenizer st = new StringTokenizer(pub.getImage(),";");
				String directory = settings.getString("imagesSubDirectory");//st.nextToken();
				String physicalName =pub.getImage();//st.nextToken();
				String mimeType = pub.getImageMimeType();//st.nextToken();
				
				//String imgSrc=m_context+"/FileServer?SourceFile="+physicalName+"&MimeType="+mimeType+"&Directory="+directory;
				String imgSrc= FileServer.getUrl(news.getSpaceId(), news.getComponentId(), physicalName, physicalName, mimeType,directory);
				out.print("<img src=\""+imgSrc+"\" align=\""+align[indiceAlign]+"\"> ");
				indiceAlign = (indiceAlign +1) % 2;	    	 
			}
			
			out.println("<A class=\""+titleClass+"\" HREF=\"javascript:onClick="+publicationTitleAction+"('"+ pub.getPK().getId() + "')\">");
			out.print(Encode.javaStringToHtmlString(pub.getName()));
			
			if (displayTitle)
			try {
					String title = news.getPublicationTitleDetail( pub.getPK().getId()).getName();
					out.print(" - "+Encode.javaStringToHtmlString(title)+"");
				}
			catch	(NewsEditoException e) {
				SilverTrace.error("NewsEdito", "publishUtils_JSP", "NewsEdito.EX_PROBLEM_TO_GET_PUBLI_TITLE",e);
			}   
			
			out.println("</A>");
			out.println("  </br>");
			out.println("   <span class=\""+descriptionClass+"\">");
			out.println(Encode.javaStringToHtmlString( pub.getDescription() ));
			out.println("   </span></TD>");
			out.println(" </TR>");
			out.println("</TABLE>");
			
		 
		}

	  }
	}
	catch(Exception e){
		throw new NewsEditoException("publicationUtils_JSP.displayEditorialModel",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_MODEL_EDITO",e);			
	}
}

void displayEditorialModel2(JspWriter out, Collection pubList,NewsEditoSessionController news , boolean displayTitle,String publicationTitleAction ,String titleClass,String descriptionClass) 
  throws NewsEditoException 
{
	try{
		out.println("<TABLE width='100%' border=0 cellspacing=1 cellpadding=3>");
		Iterator i = pubList.iterator();
		
		// premiere case
		if (i.hasNext()) 
		{		 
			Object element = i.next();
			if (element instanceof PublicationDetail)
			{
				
				PublicationDetail pub = (PublicationDetail) element;
				out.println(" <TR>");
				out.print("   <TD colspan=2>");
				out.print("<br>");
				String m_context = "../..";
				if (pub.getImage()!=null)
				{
						//StringTokenizer st = new StringTokenizer(pub.getImage(),";");
						String directory = settings.getString("imagesSubDirectory");//st.nextToken();
						String physicalName =pub.getImage();//st.nextToken();
						String mimeType = pub.getImageMimeType();//st.nextToken();
							
						//String imgSrc=m_context+"/FileServer?SourceFile="+physicalName+"&MimeType="+mimeType+"&Directory="+directory;
						String imgSrc= FileServer.getUrl(news.getSpaceId(), news.getComponentId(), physicalName, physicalName, mimeType,directory);
					
						out.print("<img src=\""+imgSrc+"\" align=\"left\"> ");		    		 
				}
				out.println("<A class=\""+titleClass+"\" HREF=\"javascript:onClick="+publicationTitleAction+"('"+ pub.getPK().getId() + "')\">");
				out.print(Encode.javaStringToHtmlString(pub.getName()));
				if (displayTitle)
				try {
						String title = news.getPublicationTitleDetail( pub.getPK().getId()).getName();
						out.print(" ("+Encode.javaStringToHtmlString(title)+")");
					}
				catch	(NewsEditoException e) {
					SilverTrace.error("NewsEdito", "publishUtils_JSP", "NewsEdito.EX_PROBLEM_TO_GET_PUBLI_TITLE",e);
				}
				out.println("</A>");
				out.println("  </br>");
				out.println("   <span class=\""+descriptionClass+"\">");
				out.println(Encode.javaStringToHtmlString( pub.getDescription() ));
				out.println("   </span>");
				out.println("</TD>");
				out.println(" </TR>");		

			
			}
		}
		// deux colonnes du dessous
		if (pubList.size()-1 >0)
		{
			
			out.println("<TR>");
			out.print("<TD width='50%'  VALIGN = 'TOP'>");
			out.println("<TABLE width='100%' border=0 cellspacing=0 cellpadding=0>");	
			int sizeRight = (pubList.size()-1)/2;
			int sizeLeft = (pubList.size()-1)-sizeRight;
			for (int j=0; j< sizeLeft  ;j++ )
			{
				Object element = i.next();
				PublicationDetail pub = (PublicationDetail) element;
				out.print("<TR>");
				out.print("<TD>");
				out.print("<BR>");
				out.print("<A class=\""+titleClass+"\" HREF=\"javascript:onClick="+publicationTitleAction+"('"+ pub.getPK().getId() + "')\">");
				out.print(Encode.javaStringToHtmlString(pub.getName()));
				out.print("</A>");
				out.print("</BR>");
				out.print("<span class=\""+descriptionClass+"\">");
				out.print(Encode.javaStringToHtmlString( pub.getDescription() ));
				out.print("</span>");

				out.print("</TD>");
				out.print("</TR>");
			}
			out.print("</TABLE>");
			out.print("</TD>");
			out.print("<TD width='50%' VALIGN = 'TOP'>");
			
			out.print("<TABLE width='100%' border=0 cellspacing=0 cellpadding=0>");
			
			for (int j=0; j< sizeRight  ;j++ )
			{
				Object element = i.next();
				PublicationDetail pub = (PublicationDetail) element;
				out.print("<TR>");
				out.print("<TD>");
				out.print("<BR>");
				out.print("<A class=\""+titleClass+"\" HREF=\"javascript:onClick="+publicationTitleAction+"('"+ pub.getPK().getId() + "')\">");
				out.print(Encode.javaStringToHtmlString(pub.getName()));
				out.print("</A>");
				out.print("</BR>");
				out.print("<span class=\""+descriptionClass+"\">");
				out.print(Encode.javaStringToHtmlString( pub.getDescription() ));
				out.print("</span>");
				out.print("</TD>");
				out.print("</TR>");
			}
			out.println("</TABLE>");

			out.println("</TD>");
			out.println("</TR>");
		}	
		
		out.println("</TABLE>");
	}
	catch(Exception e){
		throw new NewsEditoException("publicationUtils_JSP.displayEditorialModel2",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_MODEL_EDITO",e);			
	}
			
}

void displayPublicationList(JspWriter out, Collection pubList,NewsEditoSessionController news , 
	boolean displayTitle, String publicationTitleAction ,String titleClass,
	String descriptionClass) 
  throws NewsEditoException 
{
	try{
		displayPublicationList(out, pubList, news , displayTitle,false, publicationTitleAction ,titleClass,
			descriptionClass);
	}
	catch(Exception e){
		throw new NewsEditoException("publicationUtils_JSP.displayPublicationList",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_PUBLI_LIST",e);			
	}
}

void displayPublicationList(JspWriter out, Collection pubList,NewsEditoSessionController news , 
	boolean displayTitle,boolean withCheckBox, String publicationTitleAction ,String titleClass,
	String descriptionClass) 
  throws NewsEditoException 
{
	try{
		Iterator i = pubList.iterator();
		Object element = null;
		PublicationDetail pub = null;
		
		while	(i.hasNext()) {
		
			element = i.next();
			out.println("<table width='100%' border=0 cellspacing=1 cellpadding=3>");
			
			if (element instanceof PublicationDetail)
			{	
				pub = (PublicationDetail) element;
				out.println(" <TR>");
				out.println("   <TD width=\"95%\"><br>");
				out.println("<A class=\""+titleClass+"\" HREF=\"javascript:onClick="+publicationTitleAction+"('"+ 
							pub.getPK().getId() + "')\">");
				out.println(Encode.javaStringToHtmlString(pub.getName()));
				out.println("</A>");
				out.println("  </br>");
				out.println("   <span class=\""+descriptionClass+"\">");
				out.println(Encode.javaStringToHtmlString( pub.getDescription() ));
				out.println("   </span></TD>");
				
				if (withCheckBox) {
					out.println("   <TD width=\"5%\" align=right><input type=\"checkbox\" name=\"publicationIds\" value=\""+pub.getPK().getId()+"\">");
					out.println("   </TD>");
				}
				out.println(" </TR>");
				
			}
			out.println("</TABLE>");	
		}
	}
	catch(Exception e){
		throw new NewsEditoException("publicationUtils_JSP.displayPublicationList",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_PUBLI_LIST",e);			
	}
}


void displayPublicationArrayPane(JspWriter out, Collection pubList,NewsEditoSessionController news,javax.servlet.http.HttpServletRequest request,javax.servlet.http.HttpSession session) 
  throws NewsEditoException 
{
	try{
		//Le tableau de tri contenant tous les journaux
		ArrayPane arrayPane = gef.getArrayPane("articleList", "manageArticles.jsp?Action=SelectTitle&TitleId="+news.getTitleId(), request, session);
		
		//Définition des colonnes du tableau
		arrayPane.addArrayColumn(titreAP);
		arrayPane.addArrayColumn(news.getString("nodeDescription"));
		ArrayColumn arrayColumnDel = arrayPane.addArrayColumn("&nbsp;"); //checkBox to delete or copy articles
		arrayColumnDel.setSortable(false);		

		Iterator i = pubList.iterator();
		Object element = null;
		PublicationDetail pub = null;
		ArrayLine arrayLine = null;
		
		while	(i.hasNext()) {
			element = i.next();
			if (element instanceof PublicationDetail) {	
				pub = (PublicationDetail) element;
			
				arrayLine = arrayPane.addArrayLine();
				arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(pub.getName()),"javascript:onClick=selectPublication('"+pub.getPK().getId() + "')");
				arrayLine.addArrayCellText(Encode.javaStringToHtmlString( pub.getDescription() ));
				
				if ( (flag.equals("publisher") && news.getUserId().equals(pub.getCreatorId()) && !news.getArchiveContent().getStatus().equals("onLine") ) 
		 			|| flag.equals("admin")) 
					arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"publicationIds\" value=\""+pub.getPK().getId()+"\">"); //he can copy or delete the article
			}
		}

		out.println(arrayPane.print());  
	}
	catch(Exception e){
		throw new NewsEditoException("publicationUtils_JSP.displayPublicationArrayPane",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_PUBLI_PANE",e);			
	}

}

// Fonction d'encodage html
String HTMLEncode(String pstr) {
    String res="";

    if (pstr==null) return res;
    for(int i=0; i<pstr.length();i++) {
        char c=pstr.charAt(i);
        switch(c) {
            case '\n':res+="<BR>";break;
            default:res+=c;
        }
    }
    return res;
}

void displayPublication(JspWriter out, NewsEditoSessionController news, CompletePublication pubComplete, ResourceLocator settings) 
  throws NewsEditoException 
{
	try{
		displayPublication(out, news, pubComplete,false, settings);
	}
	catch(Exception e){
		throw new NewsEditoException("publicationUtils_JSP.displayPublication",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_PUBLI",e);			
	}
}


void displayPublication(JspWriter out, NewsEditoSessionController news, CompletePublication pubComplete, boolean displayEditoPicture, ResourceLocator settings) 
  throws NewsEditoException 
{
	try{
		  if (pubComplete == null) {
			out.println(news.getString("aucunePubSelectionnee"));
			return;
		  }
		  PublicationDetail pubDetail = pubComplete.getPublicationDetail();

		  String author=null;
		  try {
				UserDetail userDetail = news.getUserDetail(pubDetail.getCreatorId());
				author= userDetail.getFirstName() + " " +userDetail.getLastName();
			} catch (Exception e) 
			  {author= news.getString("auteurInconnu");}
			out.println("<TABLE><TR>");

			
			if (displayEditoPicture)
				if (pubComplete.getPublicationDetail() != null) 
					if (pubComplete.getPublicationDetail().getImage()!=null)
					{
						String m_context = "../..";
						//StringTokenizer st = new StringTokenizer(pubComplete.getPublicationDetail().getImage(),";");
						String directory = settings.getString("imagesSubDirectory");//st.nextToken();
						String physicalName =pubComplete.getPublicationDetail().getImage();//st.nextToken();
						String mimeType = pubComplete.getPublicationDetail().getImageMimeType();//st.nextToken();
							
						//String imgSrc=m_context+"/FileServer?SourceFile="+physicalName+"&MimeType="+mimeType+"&Directory="+directory;
						String imgSrc= FileServer.getUrl(news.getSpaceId(), news.getComponentId(), physicalName, physicalName, mimeType,directory);
							
						out.print("<TD valign=top ><img src=\""+imgSrc+"\" ></TD> ");
					}

			out.println("<TD>");
			out.println("<p><span class=\"headline\">"+Encode.javaStringToHtmlString(pubDetail.getName())+"</span><span class=\"txtnote\"> ("+author+"</span>)"+"</p>");
			out.println("<p class=\"healineBody\">"+Encode.javaStringToHtmlString(pubDetail.getDescription())+"</p>");
			
				
		  out.println("</TD></TR></TABLE>");
		  out.print("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"5\"> <TR><TD>");
		  out.print("</TD></TR></TABLE>");
	}
	catch(Exception e){
		throw new NewsEditoException("publicationUtils_JSP.displayPublication",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_PUBLI",e);			
	}
}

void displayViewWysiwyg(String id, String spaceId, String componentId, HttpServletRequest request, HttpServletResponse response) throws NewsEditoException {
    try {
        getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+id+"&SpaceId="+spaceId+"&ComponentId="+componentId).include(request, response);
    } catch (Exception e) {
		throw new NewsEditoException("publicationUtils_JSP.displayViewWysiwyg",NewsEditoException.ERROR,"NewsEdito.EX_CANNOT_DISPLAY_WYSIWYG",e);			
    }
}

%>

<%!
private ResourceLocator uploadSettings = new ResourceLocator(
      "com.stratelia.webactiv.util.uploads.uploadSettings", "fr");


%>