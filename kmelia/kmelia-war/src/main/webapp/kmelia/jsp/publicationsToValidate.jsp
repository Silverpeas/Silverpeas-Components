<%@ include file="checkKmelia.jsp" %>
<%@ include file="publicationsList.jsp.inc" %>

<%!
  //Icons
  String publicationSrc;
  String fullStarSrc;
  String emptyStarSrc;

// default value for publications sort
  int SORT_DEFAULT_VALUE=6; // sorted by name
%>

<%
//Icons
publicationSrc		= m_context + "/util/icons/publication.gif";
fullStarSrc			= m_context + "/util/icons/starFilled.gif";
emptyStarSrc		= m_context + "/util/icons/starEmpty.gif";
%>

<HTML>
<HEAD>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/ajax/prototype.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/ajax/rico.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/ajax/ricoAjax.js"></script>
<script language="JavaScript1.2">
function sortGoTo(selectedIndex) {
	if (selectedIndex != 0 && selectedIndex != 1) {
		var sort = document.publicationsForm.sortBy[selectedIndex].value;
		ajaxEngine.sendRequest('refreshPubList','ElementId=pubList',"ComponentId=<%=componentId%>",'ToValidate=1',"Index=0","Sort="+sort);
	}
}

function publicationGoTo(id){
    document.pubForm.PubId.value = id;
    document.pubForm.submit();
}

function doPagination(index)
{
	ajaxEngine.sendRequest('refreshPubList','ElementId=pubList',"ComponentId=<%=componentId%>",'ToValidate=1',"Index="+index);
}

function init()
{
	ajaxEngine.registerRequest('refreshPubList', '<%=m_context%>/RAjaxPublicationsListServlet/dummy');
	
	ajaxEngine.registerAjaxElement('pubList');
	
	ajaxEngine.sendRequest('refreshPubList','ElementId=pubList',"ComponentId=<%=componentId%>",'ToValidate=1');
}
</script>
</HEAD>

<BODY onLoad="init()">
<%
    Window window = gef.getWindow();
    Frame frame = gef.getFrame();

	BrowseBar browseBar = window.getBrowseBar();
  	browseBar.setDomainName(kmeliaScc.getSpaceLabel());
  	browseBar.setComponentName(kmeliaScc.getComponentLabel(), "Main");

    out.println(window.printBefore());
    out.println(frame.printBefore());
    
    out.println("<div id=\"pubList\"/>");

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

  <FORM NAME="pubForm" ACTION="ViewPublication" METHOD="POST">
	<input type="hidden" name="PubId">
  </FORM>

</BODY>
</HTML>