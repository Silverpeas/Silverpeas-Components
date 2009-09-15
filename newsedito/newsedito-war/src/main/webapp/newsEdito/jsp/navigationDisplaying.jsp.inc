<%@ include file="titleUtils.jsp.inc" %>

<%
String titleClass = null;
String txtClassOn = null;
String txtClassOff = null;
if (action.equals("CompilePdf"))
{	
	titleClass="txtpetitblanc";
	txtClassOn="txtnote";
	txtClassOff="txtnote";
}
else
{	
	titleClass="txtGrandBlanc";
	txtClassOn="hrefComponentName";
	txtClassOff="hrefComponentNameOff";	
}

%>

      <table width="100%" border="0" cellspacing="1" cellpadding="3" class="intfdcolor1">
        <tr> 


		<td class="intfdcolor"><span class="<%=titleClass %>"> 


          <% if (detailLevel >0 ) 
          		out.print(archiveDetail.getName());
          	else 
          		out.print(news.getString("aucuneArchiveSelectionnee"));   %></span></td>
 
        </tr>

      <%
        if (detailLevel >0 ) {
          displayTitleList(out, news.getTitleId(),archiveDetail,txtClassOn,txtClassOff, news, settings);

        } 
	%>

        <tr> 
          <td class="intfdcolor4"> 
            <p>&nbsp;</p>
            <p>&nbsp;</p>
          </td>
        </tr>
				<%
				if (archives != null) {
					%>
					<tr> 
						<td class="intfdcolor"><span class="<%=titleClass %>"><%=news.getString("journauxPrecedents")%></span></td>
					</tr>
					<tr> 
						<td class="intfdcolor4">
						<%
							boolean displayAll = ! action.equals("Consult");
							if (detailLevel >0 )
								displayArchiveSelect(out, archiveDetail.getNodePK().getId(),archives,displayAll, news,settings);
							else
								displayArchiveSelect(out, null,archives,displayAll, news,settings);
							%>
						</td>
					</tr>
					<%
				}
				%>
      </table>