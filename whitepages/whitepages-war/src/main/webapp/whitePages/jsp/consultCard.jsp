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

<%@ page import="java.util.*"%>
<%@ page import="com.silverpeas.whitePages.model.*"%>
<%@ page import="com.silverpeas.form.*"%>

<%@ include file="checkWhitePages.jsp" %>

<%
		
	browseBar.setDomainName(spaceLabel);
	browseBar.setPath(resource.getString("whitePages.usersList") + " > "+ resource.getString("whitePages.consultCard"));

	Card card = (Card) request.getAttribute("card");
	String userCardId = card.getPK().getId();
	
	boolean isAdmin = ( (Boolean) request.getAttribute("isAdmin")).booleanValue();
	
	tabbedPane.addTab(resource.getString("whitePages.id"), routerUrl+"consultIdentity?userCardId="+userCardId, false, true);
	tabbedPane.addTab(resource.getString("whitePages.fiche"), routerUrl+"consultCard", true, false);
	tabbedPane.addTab(resource.getString("whitePages.PdcClassification"), routerUrl+"ViewPdcPositions?userCardId="+userCardId, false, true);
		
	Collection whitePagesCards = (Collection) request.getAttribute("whitePagesCards");
	Form viewForm = (Form) request.getAttribute("Form");
	PagesContext context = (PagesContext) request.getAttribute("context"); 
	DataRecord data = (DataRecord) request.getAttribute("data"); 

	if (! card.readReadOnly()) {
		operationPane.addOperation(resource.getIcon("whitePages.editCard"), resource.getString("whitePages.op.editUser"), "javascript:onClick=B_UPDATE_ONCLICK('"+userCardId+"');");
		operationPane.addLine();

		if (isAdmin) {
			operationPane.addOperation(resource.getIcon("whitePages.delCard"), resource.getString("whitePages.op.deleteUser"), "javascript:onClick=B_DELETE_ONCLICK('"+userCardId+"');");
			operationPane.addLine();
			
			if (card.getHideStatus() == 0) {//Visible
				operationPane.addOperation(resource.getIcon("whitePages.hideCard"), resource.getString("whitePages.op.hideCard"), "javascript:onClick=B_REVERSEHIDE_ONCLICK('"+userCardId+"');");	
			}
			else {//Masque
				operationPane.addOperation(resource.getIcon("whitePages.showCard"), resource.getString("whitePages.op.showCard"), "javascript:onClick=B_REVERSEHIDE_ONCLICK('"+userCardId+"');");
			}
		}

		if (containerContext != null)
		{
			URLIcone classify = containerContext.getClassifyURLIcone();
			String classifyURL = null;
			if (contentId != null)
			{
				classifyURL = containerContext.getClassifyURLWithParameters(
					componentId, contentId);
			}
			if (classifyURL != null)
			{
				operationPane.addOperation(classify.getIconePath(),
													"Classer",
													"javascript:openSPWindow('"+m_context+classifyURL+"','classify')");
			}
		}
	}
	
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.back"), routerUrl+"Main", false));
%>


<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());

%>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

<!-- JAVASCRIPT LANGUAGE -->
<script language="JavaScript">
function openSPWindow(fonction,windowName){
		SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}
</script>


<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">
<!--	
	function changerChoice() {
        indexWhitePages = document.myForm.selectionFiche.selectedIndex;
        idCard = document.myForm.selectionFiche.options[indexWhitePages].value;
        document.choixFiche.userCardId.value = idCard;
        if (idCard == "0") {
        	document.choixFiche.action = "<%=routerUrl%>createCard";
        }
        else 
        	document.choixFiche.action = "<%=routerUrl%>consultCard";
        document.choixFiche.submit();	
	}
	
/*****************************************************************************/
	function B_UPDATE_ONCLICK(idCard) {
		 theURL = "<%=routerUrl%>updateCard?userCardId="+idCard;
         winName = "updateCard";
         larg ="600";
         haut = "400";
         windowParams = "scrollbars=yes, resizable, alwaysRaised";
         dico = SP_openWindow(theURL, winName, larg, haut, windowParams);
	}

/*****************************************************************************/
	function B_DELETE_ONCLICK(idCard) {
		if (window.confirm("<%=resource.getString("whitePages.messageSuppression")%>")) { 
			location.href = "<%=routerUrl%>delete?checkedCard="+idCard;
		}
	}	
	
/*****************************************************************************/
	function B_REVERSEHIDE_ONCLICK(idCard) {
		location.href = "<%=routerUrl%>reverseHide?returnPage=consultCard&userCardId="+idCard;
	}	
	
	
//-->
</script>

</HEAD>

<BODY class="yui-skin-sam" marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<FORM NAME="choixFiche" METHOD="POST">
	<input type="hidden" name="userCardId">
</FORM>

<FORM NAME="myForm" METHOD="POST" ACTION="<%=routerUrl%>effectiveCreate">

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
        long currentUserCardId = new Long(card.getPK().getId()).longValue();
        if (whitePagesCards != null) {
					Iterator i = whitePagesCards.iterator();
					while (i.hasNext()) {
						WhitePagesCard whitePagesCard = (WhitePagesCard) i.next();
						long id = whitePagesCard.getUserCardId();
						String label = whitePagesCard.readInstanceLabel();
						
      					if (id == currentUserCardId) 
           	  				out.println("<option selected value=\""+id+"\">"+label); 
          				
          				else 
          					out.println("<option value=\""+id+"\">"+label); 
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

<br>
<%
	viewForm.display(out, context, data);
%>
<br>
<%=buttonPane.print() %>
</center>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</FORM>
</BODY>
</HTML>
