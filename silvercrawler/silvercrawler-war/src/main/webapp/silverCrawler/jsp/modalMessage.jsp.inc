<script type="text/javascript">

messageObj = new DHTML_modalMessage();	// We only create one object of this class
messageObj.setShadowOffset(5);	// Large shadow

<!-- avec le script modal-message.js -->

function displayStaticMessage()
{
	messageObj.setHtmlContent("<center><table><tr><td align=\"center\" class=\"txtnote\"><%=resource.getString("silverCrawler.attente")%></td></tr><tr><td><br/></td></tr><tr><td align=\"center\"><img src=\"<%=resource.getIcon("silverCrawler.wait")%>\"/></td></tr></table></center>");
	messageObj.setSize(300,100);
	messageObj.setCssClassMessageBox(false);
	//messageObj.setSource('inProgress.inc');	// no html source since we want to use a static message here.
	messageObj.setShadowDivVisible(true);	// Disable shadow for these boxes	
	messageObj.display();
}

function closeMessage()
{
	messageObj.close();	
}

</script>

