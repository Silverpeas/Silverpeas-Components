<%
  Exception exception = (Exception) request.getAttribute("error");
%>

<%@ include file="checkQuickInfo.jsp" %>

<HTML>
  <HEAD>
<% out.println(gef.getLookStyleSheet()); %>
  </HEAD>
  <body>
<%
	Window window = gef.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(quickinfo.getSpaceLabel());
	browseBar.setComponentName(quickinfo.getComponentLabel() );

	browseBar.setPath(resources.getString("edition"));

	Frame maFrame = gef.getFrame();

	out.println(window.printBefore());
	out.println(maFrame.printBefore());
%>
	<table border="0" cellspacing="0" cellpadding="2" width="100%" class="ArrayColumn">
        <tr>
           <td align="center"> <!--TABLE SAISIE-->
              <table width="100%" border="0" cellspacing="0" cellpadding="5" class="intfdcolor4">
               <tr>
		<td align="center"><span class="txtnote"><%=resources.getString(exception.getMessage())%></span>

	        </td>
               </tr>
	      </TABLE>
           </td>
	</tr>
	</table>
        <table width="100%" border="0" cellspacing="0" cellpadding="5">
         <tr>
	 <td align="center"><%
			Button button = gef.getFormButton(resources.getString("GML.back"), "Main.jsp", false);
			out.print(button.print());
			%>
					</td>
         </tr>
       </TABLE>
			<%
					out.println(maFrame.printAfter());
					out.println(window.printAfter());
			%>
				<BR><BR>
	</body>
</HTML>