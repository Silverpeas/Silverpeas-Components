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
<%@ include file="imports.jsp" %>
<%@ include file="declarations.jsp.inc" %>


<%@ include file="publicationUtils.jsp.inc" %>

<HTML>
<HEAD>
<%
	String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

	out.println(gef.getLookStyleSheet());
%>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<Script language="JavaScript">

function doConsult(){
    document.newsForm.Action.value = "Consult";
    document.newsForm.submit();

}
function doOrganize(){
    document.manageForm.Action.value = "Organize";
    document.manageForm.submit();
}
function doSetInLine(){
    document.publishForm.Action.value = "Publish";
    document.publishForm.submit();
}

function selectArchive(archiveId)
{
    document.pdfCompileForm.Action.value = "SelectArchive";
    document.pdfCompileForm.ArchiveId.value = archiveId;
    document.pdfCompileForm.submit();
}

function selectTitle(titleId)
{
    document.pdfCompileForm.Action.value = "SelectTitle";
    document.pdfCompileForm.TitleId.value = titleId;
    document.pdfCompileForm.submit();
}

function addPublication(publicationId)
{
    document.pdfCompileForm.Action.value = "AddPublication";
    document.pdfCompileForm.PubId.value = publicationId;
    document.pdfCompileForm.submit();
}

function removePublication(publicationId)
{
    document.pdfCompileForm.Action.value = "RemovePublication";
    document.pdfCompileForm.PubId.value = publicationId;
    document.pdfCompileForm.submit();
}

function compileArchive()
{
    document.pdfCompileForm.Action.value = "CompileArchive";
    document.pdfCompileForm.submit();
}

function compileListPub()
{
    document.pdfCompileForm.Action.value = "CompileListPub";
    document.pdfCompileForm.submit();
}

function compileResult(fileName) {
    SP_openWindow(fileName, "PdfGeneration","770", "550", "toolbar=no, directories=no, menubar=no, locationbar=no ,resizable, scrollbars");
    window.location.replace("newsEdito.jsp");
}

</script>
</HEAD>


<%!

  Hashtable selectedPublications ;
  Vector selectedIds;//to maitain order
  Collection availablePublications;

  void showPdfGeneration(JspWriter out, NewsEditoSessionController news, String[] pubList)
    throws NewsEditoException, IOException
  {
	String name = "";
	String link = "";
	try{
		name = news.generatePdf(pubList);

	}
	catch(NewsEditoException e){
		throw new NewsEditoException("pdfCompile_JSP.showPdfGeneration",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_SHOW_PDF_GENERATION",e);
	}
		link = FileServerUtils.getUrlToTempDir(name);
		out.println("<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 onLoad=\"compileResult('"+link+"')\">");
		out.println("</BODY>");

  }

%>


<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server

if (action == null)
	action = "CompilePdf";

if (action.equals("CompileArchive"))
{
	showPdfGeneration(out, news, null);
}
else if (action.equals("CompileListPub"))
{
	if (selectedIds.size()!=0) {
		String ids[]= new String[selectedIds.size()];
		selectedIds.toArray(ids);

   		showPdfGeneration(out, news, ids);
	}
}
else {
    // toutes les actions suivantes necessitent l'affichage de la selection.
    %>
    <BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5>

    <%

	if (action.equals("CompilePdf"))
	{
		news.selectFirstOnLineArchive();
		selectedPublications = new Hashtable();
		selectedIds = new Vector();
		availablePublications = null;
		action = "CompilePdf";
	}
	else if (action.equals("SelectArchive"))
    {
		String archiveId = (String) request.getParameter("ArchiveId");
		news.setArchiveId(archiveId);
    	action = "CompilePdf";
    }
    else
    if (action.equals("SelectTitle"))
    {
		String titleId = (String) request.getParameter("TitleId");
		news.setTitleId(titleId);
		availablePublications = news.getTitlePublicationDetails();
		action = "CompilePdf";
    }
    else
    if (action.equals("AddPublication"))
    {
		String publicationId = (String) request.getParameter("PubId");

		boolean alreadyIn = (selectedPublications.get(publicationId)!=null );

		if (!alreadyIn)  {
        	PublicationDetail publicationToAdd = news.getPublicationDetail(publicationId);
			selectedPublications.put(publicationId,publicationToAdd);
        	selectedIds.addElement(publicationId);
    		}
    	action = "CompilePdf";
    }
	else if (action.equals("RemovePublication"))
    {
		String publicationId = (String) request.getParameter("PubId");
		selectedPublications.remove(publicationId);
    	int i = 0;
    	while (! ((String) selectedIds.elementAt(i)).equals(publicationId))
    		i++;
    	selectedIds.removeElementAt(i);
    	action = "CompilePdf";
    }



%>
<%@ include file="init.jsp.inc" %>


    <FORM NAME="pdfCompileForm" ACTION="pdfCompile.jsp" METHOD=POST >

<%
	Window window = gef.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(news.getComponentLabel(),"newsEdito.jsp");
	browseBar.setDomainName(news.getSpaceLabel());
	if (!navigationString.equals(""))
		browseBar.setPath(compilationPdfBB+" > "+navigationString);
	else
		browseBar.setPath(organiseBB);

	//Les onglets
    TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(consultationTP, "javaScript:doConsult()", true);

    if (flag.equals("publisher") || flag.equals("admin"))
    	tabbedPane.addTab(organisationTP, "javaScript:doOrganize()", false);

	if (flag.equals("admin"))
		tabbedPane.addTab(inLineSettingTP, "javaScript:doSetInLine()", false);

	//Le cadre
	Frame frame = gef.getFrame();

	//debut du code HTML
	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
%>
<TABLE CELLPADDING=5 CELLSPACING=2 BORDER=0 WIDTH="98%"><TR><TD NOWRAP>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
		<td colspan="3">
			<div align="right"> <A HREF="javascript:onClick=compileArchive()"><%=news.getString("compilerTouteArchive")%></a></div>
		</td>
	</tr>
	 <tr>
		<td colspan="3">
			<img src="<%=settings.getString("1px") %>" height="2">
		</td>
	</tr>
	<tr>
    <td width="70%" valign="top">
			<TABLE CELLPADDING=5 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
				<TR>
					<TD CLASS=intfdcolor4 NOWRAP>
            <table width="100%" border="0" cellspacing="1" cellpadding="3">
              <tr>
                <td width="25%" valign="top">
				<%
					action = "Consult";
				%>
				<%@ include file="navigationDisplaying.jsp.inc" %>

                </td>
                <td valign="top">
<%
			switch(detailLevel)
			{
				case 0 :
				{
					// nothing to do
				}
				break;
				case 1 :
				{
					// nothing to do
				}
				break;
				case 2 :
				{

					if (availablePublications!=null)
						displayPublicationList(out,availablePublications,news,false,"addPublication","textePetitBold","txtnote");

				}
				break;
			}

%>
                  <p>&nbsp;</p>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
    <td>&nbsp;</td>
    <td valign="top">
      <table width="100%" border="0" cellspacing="0" cellpadding="0" height="250">
        <tr>
          <td height="245" valign="top">
            <table width="100%" border="0" cellspacing="1" cellpadding="3" class="intfdcolor1" height="250">
              <tr class="intfdcolor1" valign="top">
                <td height="5" class="txtGrandBlanc"><%=news.getString("listPubACompiler")%></td>
              </tr>
              <tr class="intfdcolor4" valign="top">
                <td>

<%

		Vector publist = new Vector();
		Iterator i = selectedIds.iterator();
		while (i.hasNext())
		{
			publist.addElement(selectedPublications.get(i.next()));
		}
		if (publist!=null)
			displayPublicationList(out,publist,news,false,"removePublication","textePetitBold","txtnote");



%>
				</td>
              </tr>
              <tr class="intfdcolor4" valign="top">
                <td height="5">

                  <div align="right">
                  <%
                  	String link = "#";
                  	if (selectedIds.size()!=0)
                  		link = "javascript:onClick=compileListPub()";
                  %>
                  	<a href="<%=link%>"><%=news.getString("compilerListePub")%></a>
                  </div>

                </td>
              </tr>
            </table>
          </td>
        </tr>
        <tr height="5">
          <td valign="bottom" class="txtnote"><br>
            <br>
			<%=news.getString("aucunePublicationSelectionne")%></td>
        </tr>
      </table>
    </td>
  </tr>
</table>
</TD></TR></TABLE>

      <input type="hidden" name="Action">
      <input type="hidden" name="TitleId">
      <input type="hidden" name="ArchiveId">
      <input type="hidden" name="PubId">
    </FORM>
<%
	// fin du HTML
	out.println(frame.printAfter());
  	out.println(window.printAfter());
%>





    </BODY>

<FORM NAME="newsForm" ACTION="newsEdito.jsp" METHOD=POST >
  <input type="hidden" name="ArchiveId">
  <input type="hidden" name="Action">
</FORM>
<FORM NAME="manageForm" ACTION="manageNews.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>
 <FORM NAME="publishForm" ACTION="publishNews.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>


<%
}
%>

</HTML>