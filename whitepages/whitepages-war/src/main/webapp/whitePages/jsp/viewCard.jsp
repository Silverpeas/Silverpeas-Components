<%@ page import="java.util.*"%>
<%@ page import="com.silverpeas.whitePages.model.*"%>
<%@ page import="com.silverpeas.form.*"%>

<%@ include file="checkWhitePages.jsp" %>

<%
	String hostComponentName = (String) request.getAttribute("HostComponentName");
  	String hostUrl = (String) request.getAttribute("HostUrl");
  	String hostSpaceName = (String) request.getAttribute("HostSpaceName");
  	String hostPath = (String)request.getAttribute("HostPath");
  	
  	if (!hostUrl.startsWith(m_context))
  		hostUrl = m_context+hostUrl;
  	
  	browseBar.setDomainName(hostSpaceName);
  	browseBar.setComponentName(hostComponentName, hostUrl);
  	browseBar.setPath(hostPath);
  	
  	Card card = (Card) request.getAttribute("card");
	Collection whitePagesCards = (Collection) request.getAttribute("whitePagesCards");
	Form viewForm = (Form) request.getAttribute("Form");
	PagesContext context = (PagesContext) request.getAttribute("context"); 
	DataRecord data = (DataRecord) request.getAttribute("data"); 
  		
	tabbedPane.addTab(resource.getString("whitePages.id"), routerUrl+"viewIdentity?userCardId="+card.getPK().getId(), false, true);
	tabbedPane.addTab(resource.getString("whitePages.fiche"), routerUrl+"viewCard", true, false);
	
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
<script language="JavaScript">
<!--	
	function changerChoice() {
        indexWhitePages = document.myForm.selectionFiche.selectedIndex;
        idCard = document.myForm.selectionFiche.options[indexWhitePages].value;
        document.choixFiche.userCardId.value = idCard;
        document.choixFiche.submit();	
	}
	
//-->
</script>


</HEAD>

<BODY class="yui-skin-sam" marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<FORM NAME="choixFiche" METHOD="POST" ACTION="<%=routerUrl%>viewCard">
	<input type="hidden" name="userCardId">
</FORM>

<FORM NAME="myForm" METHOD="POST" ACTION="#">
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
<br>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</FORM>
</BODY>
</HTML>
