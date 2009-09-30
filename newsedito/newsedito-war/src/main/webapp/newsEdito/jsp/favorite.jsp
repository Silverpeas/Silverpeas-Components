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
