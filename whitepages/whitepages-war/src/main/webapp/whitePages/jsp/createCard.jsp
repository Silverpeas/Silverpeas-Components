<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="java.util.*"%>
<%@ page import="com.silverpeas.whitePages.model.*"%>
<%@ page import="com.silverpeas.form.*"%>

<%@ include file="checkWhitePages.jsp" %>

<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "javascript:goToMain();");
	browseBar.setPath(resource.getString("whitePages.usersList") + " > "+ resource.getString("whitePages.createCard"));
	
	tabbedPane.addTab(resource.getString("whitePages.id"), routerUrl+"createIdentity", false, true);
	tabbedPane.addTab(resource.getString("whitePages.fiche"), routerUrl+"createCard", true, false);
	
	Card card = (Card) request.getAttribute("card");
	Collection whitePagesCards = (Collection) request.getAttribute("whitePagesCards");
	Form updateForm = (Form) request.getAttribute("Form");
	PagesContext context = (PagesContext) request.getAttribute("context"); 
	DataRecord data = (DataRecord) request.getAttribute("data"); 
%>


<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
   updateForm.displayScripts(out, context);
%>

<script language="JavaScript">
	function goToMain()
	{
		if (window.confirm("<%=resource.getString("whitePages.messageCancelCreate")%>"))
		{
			<% if (containerContext == null) { %>
			   location.href = "Main";
			<% } else { %>
			   location.href = "<%= m_context + containerContext.getReturnURL()%>"; 
			<% } %>
		}
	}
	function B_VALIDER_ONCLICK()
	{
		if (isCorrectForm())
		{
			document.myForm.submit();
		}
	}
	
	function B_ANNULER_ONCLICK() {
		<% if (containerContext == null) { %>
		   location.href = "Main";
		<% } else { %>
		   location.href = "<%=m_context+containerContext.getReturnURL()%>"; 
		<% } %>
	}
	
	function changerChoice() {
        indexWhitePages = document.choixFiche.selectionFiche.selectedIndex;
        document.choixFiche.userCardId.value = document.choixFiche.selectionFiche.options[indexWhitePages].value;
        document.choixFiche.submit();	
	}
	
</script>

</HEAD>

<BODY class="yui-skin-sam" marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<FORM NAME="choixFiche" METHOD="POST" ACTION="<%=routerUrl%>consultCard">
	<input type="hidden" name="userCardId">
<%
out.println(window.printBefore());
out.println(tabbedPane.print());
out.println(frame.printBefore());
%>
<center>

<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr> 
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
				<tr align=center> 

					<td  class="intfdcolor4" valign="baseline" align=left>
						<span class="txtlibform"><%=resource.getString("whitePages.autreFiches")%> :</span>
					</td>
					<td  class="intfdcolor4" valign="baseline" align=left>
                        <span class=selectNS>
                        <select size="1" name="selectionFiche"
								OnChange="changerChoice()">
        <%
        if (whitePagesCards != null) {
					Iterator i = whitePagesCards.iterator();
					while (i.hasNext()) {
						WhitePagesCard whitePagesCard = (WhitePagesCard) i.next();
						long userCardId = whitePagesCard.getUserCardId();
						String label = whitePagesCard.readInstanceLabel();
						
      					if (userCardId == 0) //fiche en creation
           	  				out.println("<option selected value=\""+userCardId+"\">"+label); 
          				
          				else 
          					out.println("<option value=\""+userCardId+"\">"+label); 
      				}
         }
         %>
                		</select></span>

					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
</FORM>



<FORM NAME="myForm" METHOD="POST" ACTION="<%=routerUrl%>effectiveCreate" ENCTYPE="multipart/form-data">
<br>
<%
	updateForm.display(out, context, data);
%>
<br>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false));
	out.println(buttonPane.print());
%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</FORM>
</BODY>
</HTML>
