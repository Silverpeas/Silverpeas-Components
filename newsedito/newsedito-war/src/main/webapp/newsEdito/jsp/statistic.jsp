<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ include file="imports.jsp" %>
<%@ include file="declarations.jsp.inc" %>

<%@ include file="newsUtils.jsp.inc" %>
<%@ include file="titleUtils.jsp.inc" %>

<%!

void displayStatistic(JspWriter out, NewsEditoSessionController news, String fatherId, ResourceLocator generalMessage) throws NewsEditoException
{
	try{
		Collection stats = news.getArchiveUsage(fatherId);
		//ArrayList liste = new ArrayList(stats);
		out.println("<table CELLPADDING=5 CELLSPACING=2 BORDER=0 WIDTH=98% CLASS=intfdcolor>");
		out.println("\t<tr>");
		out.println("\t\t<td CLASS=intfdcolor4 NOWRAP>");
		
		out.println("\t\t\t<TABLE width=70% align=center border=0 cellPadding=0 cellSpacing=3>");
		out.println("\t\t\t\t<TR>");
		out.println("\t\t\t\t\t<TD colspan=2 align=center class=intfdcolor height=1><img src=\""+settings.getString("1px")+"\"></TD>");
		out.println("\t\t\t\t</TR>");
		out.println("\t\t\t\t<TR>");
		out.println("\t\t\t\t\t<TD align=center class=txttitrecol>");
		out.println("\t\t\t\t\t"+news.getString("titre"));				
		out.println("\t\t\t\t\t</TD>");
		out.println("\t\t\t\t\t<TD align=center class=txttitrecol>");
		out.println("\t\t\t\t\t"+news.getString("nbLecture"));
		out.println("\t\t\t\t\t</TD>");
		out.println("\t\t\t\t</TR>");
		out.println("\t\t\t\t<TR>");
		out.println("\t\t\t\t\t<TD colspan=2 align=center class=intfdcolor height=1><img src=\""+settings.getString("1px")+"\"></TD>");
		out.println("\t\t\t\t</TR>");
		
	  if (stats == null || stats.size() == 0){
		 out.println("\t\t\t\t<TR>");
	   out.println("\t\t\t\t\t<TD colspan=\"2\" align=\"center\">");
	   out.println(generalMessage.getString("GML.noStatistic"));
		 out.println("\t\t\t\t\t</TD>");
		 out.println("\t\t\t\t</TR>");
		}
		
		Iterator i = stats.iterator();
	  while (i.hasNext()) {
		StatisticResultDetail statDetail = (StatisticResultDetail) i.next();
		NodeDetail nodeDetail = null;
		
		if (statDetail.getDetail() != null) {
		  nodeDetail = (NodeDetail) statDetail.getDetail();
		  out.println(" <TR>");
		  out.println("   <TD WIDTH=50% ALIGN=CENTER class=\"field\">");
		  out.println(Encode.javaStringToHtmlString(nodeDetail.getName()));
		  out.println("   </TD>");
		  out.println("   <TD WIDTH=50% ALIGN=CENTER>");
		  out.println(statDetail.getResult());
		  out.println("   </TD>");
		  out.println(" </TR>");
		}
		else {
		  out.println(" <TR>");
		  out.println("   <TD ALIGN=CENTER>");
		  out.println(statDetail.getPK().getId() + ", "+statDetail.getPK().getSpace() + ", "+
			  statDetail.getPK().getComponentName());
		  out.println("   </TD>");
		  out.println("   <TD>");
		  out.println(statDetail.getResult());
		  out.println("   </TD>");
		  out.println(" </TR>");
		}
	  }
		out.println("\t\t\t\t<TR>");
		out.println("\t\t\t\t\t<TD colspan=2 align=center class=intfdcolor height=1><img src=\""+settings.getString("1px")+"\"></TD>");
		out.println("\t\t\t\t</TR>");
		out.println("\t\t\t</TABLE>");
		
		out.println("\t\t</td>");
		out.println("\t</tr>");
		out.println("</table>");
	}
	catch(Exception e){
		throw new NewsEditoException("statistic_JSP.displayStatistic",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_DISPLAY_STATISTIC",e);			
	}

}

%>

<%

  String fatherId;  
  fatherId = (String) request.getParameter("FatherId");
  if (fatherId != null)
    if (fatherId.length() == 0) fatherId = null;

  if (action == null)
    action = "View";

  NodeDetail archiveDetail = news.getArchiveContent();

	String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>

<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet());%>
<TITLE><%=news.getString("statistiques")%></TITLE>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">
function fermer(){
	winClose();
}
</script>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5>

<%
	Window window = gef.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(news.getComponentLabel());
	browseBar.setDomainName(news.getSpaceLabel());

	String navigation = "";
	if (archiveDetail != null)
	    navigation += Encode.javaStringToHtmlString(archiveDetail.getName());

        navigation += " > " + news.getString("statistiques");

	browseBar.setPath(navigation);

	out.println(window.printBefore());

	Frame frame = gef.getFrame();

	out.println(frame.printBefore());
%>
<center>

      <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr> <td>&nbsp;</td></tr>

        <tr>
          <td align="center">
<%
  if (action.equals("View")) {
    displayStatistic(out, news, fatherId, generalMessage);
  }
  else
    out.println("Error : unknown action = " + action);
 
%>
          </td>
        </tr>
        <tr> <td>&nbsp;</td></tr>
        <tr> <td align="center">
        <%
        Button button = gef.getFormButton(generalMessage.getString("GML.close"), 
          "javascript:fermer()", 
          false, settings.getString("formButtonIconUrl"));
 
        out.println(button.print());
        %>
        </td></tr>
        
  </table>
</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>


</BODY>
</HTML>
