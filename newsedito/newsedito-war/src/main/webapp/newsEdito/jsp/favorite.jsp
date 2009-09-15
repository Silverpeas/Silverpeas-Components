<%@ include file="imports.jsp" %>
<%@ include file="declarations.jsp.inc" %>

<%@ include file="newsUtils.jsp.inc" %>
<%@ include file="favoritUtils.jsp.inc" %>
<%@ include file="titleUtils.jsp.inc" %>


<%

  if (action == null)
    action = "ViewFavorits";


  if (action.equals("RemoveFavorit")) {
    String favoritId = (String) request.getParameter("FavoritId");
    news.removeFavorit(favoritId);
    action = "ViewFavorits";
  }

%>

<HTML>
<HEAD>
<%
	String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	
	out.println(gef.getLookStyleSheet());
%>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<Script language="JavaScript">

function viewFavorits()
{
    document.newsForm.Action.value = "ViewFavorits";
    document.newsForm.submit();
}

function favoritDeleteConfirm(favoritId, name)
{
    if(window.confirm("<%=news.getString("supprimerFavoriConfirmation")%> '" + name + "' ?")){
          document.favoritForm.Action.value = "RemoveFavorit";
          document.favoritForm.FavoritId.value = favoritId;
          document.favoritForm.submit();
    }
}

function viewTitle(titleId)
{
    window.opener.location="newsEdito.jsp?Action=SelectTitle&TitleId="+titleId;
    window.close();
}


</script>
</HEAD>
<BODY>

<%
    Collection archives = null;
    NodeDetail archiveDetail = null;

%>

<%
	Window window = gef.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(news.getComponentLabel());
	browseBar.setDomainName(news.getSpaceLabel());

	browseBar.setPath(news.getString("mesTitresFavoris"));

	out.println(window.printBefore());
	
	Frame frame = gef.getFrame();

	out.println(frame.printBefore());

%>


<%
    if (action.equals("ViewFavorits")) {
      %>

      <%
      displayFavoritList(out, news, settings, generalMessage);
      %>

      <%
    }
    else{
	 %>
	 <table CELLPADDING=5 CELLSPACING=2 BORDER=0 WIDTH="98%">
	 	<tr>
    	<td>
			<% out.println("Error : unknown action = " + action); %>
	  	</td>
    </tr>
	 </table>
	 	<%
		}
		%>


	 <table CELLPADDING=5 CELLSPACING=2 BORDER=0 WIDTH="98%">
    <tr> <td align="center">
        <%
        Button button = gef.getFormButton(generalMessage.getString("GML.close"), 
          "javascript:onClick=window.close()", 
          false, settings.getString("formButtonIconUrl"));
 
        out.println(button.print());
        %>
    </td></tr></table>


<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

    
<FORM NAME="favoritForm" ACTION="favorite.jsp" METHOD=POST >
  <input type="hidden" name="Action">
  <input type="hidden" name="FavoritId">
</FORM>

</BODY>
</HTML>
