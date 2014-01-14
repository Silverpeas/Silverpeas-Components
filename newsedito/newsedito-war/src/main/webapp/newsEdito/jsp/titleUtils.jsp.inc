<%@ page import="com.stratelia.webactiv.newsEdito.NewsEditoException"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%!

void displayTitleList(JspWriter	out, String selectedId, NodeDetail archive, String txtClassOn,String txtClassOff, NewsEditoSessionController news,	ResourceLocator	settings) 
throws NewsEditoException 
{

  try{
	  Collection list =	archive.getChildrenDetails();
	  Iterator i = list.iterator();


	  while	(i.hasNext()) {
	  
		Object element = i.next();
		if (element instanceof NodeDetail)
		{
			NodeDetail title = (NodeDetail)	element;
		
			out.println("<tr valign=center>");
			
			out.print("	 <td align=left class=\"intfdcolor4\">&nbsp;");
			if (title.getNodePK().getId().equals(selectedId))
			{
				out.print("<A HREF=\"javaScript:selectTitle('"+title.getNodePK().getId()+"')\" class=\""+txtClassOff+"\">");
				out.print(Encode.javaStringToHtmlString(title.getName()));
				out.print("</A><IMG SRC=\""+URLManager.getApplicationURL()+"/util/icons/arrow/arrowLeft.gif\">");
			}
			else
			{
				out.print("<A HREF=\"javaScript:selectTitle('"+title.getNodePK().getId()+"')\"	class=\""+txtClassOn+"\">");
				out.print(Encode.javaStringToHtmlString(title.getName()));
				out.println("  </A>");
			}
			out.println("  </td>");		
			out.println("  </tr>");
			
		
		   }

		}
	}
	catch(Exception e){
		throw new NewsEditoException("titleUtils_JSP.displayTitleList",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_TITLE_LIST",e);			
	}
}


void displayArchiveSelect(JspWriter	out, String selectedId, Collection archives,boolean displayAll,NewsEditoSessionController news, ResourceLocator	settings) 
throws NewsEditoException 
{	
	try{
		out.println(" <SELECT name=\"ArchiveSelect\" onChange=\"javascript:selectArchive(this.options(this.selectedIndex).value)\">");	
		if (archives !=null)
		{
			Iterator i = archives.iterator();
			while	(i.hasNext()) {
				
				NodeDetail archive = (NodeDetail) i.next();
				
				
				if (displayAll || archive.getStatus().equals("onLine"))
				{
					if (archive.getNodePK().getId().equals(selectedId))
						out.println("<OPTION SELECTED	VALUE=\""+archive.getNodePK().getId()+"\">");
					else
						out.println("<OPTION VALUE=\""+archive.getNodePK().getId()+"\">");
				
					out.println(Encode.javaStringToHtmlString(archive.getName()));
					out.println("</OPTION>");
					

				}
			}

		
		}
		out.println("</SELECT>");
	}
	catch(Exception e){
		throw new NewsEditoException("titleUtils_JSP.displayArchiveSelect",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_ARCHIVE",e);			
	}
	
}

%>