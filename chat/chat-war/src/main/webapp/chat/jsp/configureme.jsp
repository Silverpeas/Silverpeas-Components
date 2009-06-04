<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%!
	/**--------------------------------------------------------------*/
	/**        MODIFY THE FILENAME BELOW TO YOUR jchatbox.xml        */
	/**                                                              */
	/** Samples  :                                                   */
	/** For Win32 OS => c:/apache/jchatbox/conf/jchatbox.xml         */
	/** For Un*x OS  => /home/alice/jchatbox/conf/jchatbox.xml       */
	/** For Max OS   => MAC_HD:webserver:jchatbox:conf:jchatbox.xml  */
	/**--------------------------------------------------------------*/
	private static ResourceLocator settings = new ResourceLocator("com.stratelia.silverpeas.chat.settings.chatSettings", "fr");
	// OLD String XMLjChatBox = "/tmp/chat/conf/jchatbox.xml";
	String XMLjChatBox = settings.getString("xmlConfigLocation");

	// Overides jspInit method. Do not modify.
	public void jspInit()
	{
		jChatBox.Util.XMLConfig.init(XMLjChatBox);
	}
%>
