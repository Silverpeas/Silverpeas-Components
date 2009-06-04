<html>
<% if (dMode != Conf.BUFFEREDFRAMED) { %><meta http-equiv=Refresh content="<%= refreshValue %>;URL=<%= response.encodeURL("content.jsp?todo=refresh") %>"><%}%>
<head>
<script language="JavaScript">
function SP_openWindow(page,nom,largeur,hauteur,options) {
var top=(screen.height-hauteur)/2;
var left=(screen.width-largeur)/2;
	if (window.fenetre != null){
  	window.fenetre.close();}
fenetre=window.open(page,nom,"top="+top+",left="+left+",width="+largeur+",height="+hauteur+","+options);
return fenetre;
}

function scrollFrame()
{
<% if (dMode == Conf.BUFFEREDFRAMED) { %>parent.frames[1].scrollBy(0,-10000);<% } else { %>parent.content.scrollTo(0,1000);<% } %>
}
function privatemsg(user)
{
 SP_openWindow('<%= response.encodeURL("privatetext.jsp?to=") %>'+escape(user)+'','PrivateMessage','600','150','resizable=yes');
}

function goBottom(ancre)
{
self.location.hash=ancre;
}
</script>

</head>
<body bgcolor="#FFFFFF" leftmargin="1" topmargin="1" marginwidth="1" marginheight="1" onLoad="goBottom('bas')">
<center>
<table width=99% height=100% border=0 cellspacing=1 cellpadding=0>
<tr>
<td align=center valign=bottom>
	<%
       /** Display chat content. Top - Bottom */
       StringBuffer sb = new StringBuffer();
	   if (chatroom != null)
	   {
         Vector v = chatroom.getChat(cUser.getName());
         Vector filters = chatroom.getParams().getFilters();
         jChatBox.Chat.Filter.Filter filter = null;
         if (v != null)
         {
            Msg message = null;
            Msg nextMessage = null;
            String messageStr = null;
            String fromStr = null;
            int i = v.size() - (chatroom.getParams().getHistory());
            if (i<0) i = 0;
            int lines = 0;
      		sb.append("<table width=100% border=0 cellspacing=0 cellpadding=0>\n");
            while ( (i<v.size()) && (lines<chatroom.getParams().getHistory()) )
            {
               message = (Msg) v.elementAt(i);
               messageStr = message.getMsg();
               fromStr = message.getFrom();
               /** Filter From and Msg fields for non-SYSTEM users */
               if (!(message.getFrom()).equals(XMLConfig.SYSTEMLOGIN))
               {
               		for (int f=0;f<filters.size();f++)
               		{
						filter = (jChatBox.Chat.Filter.Filter) filters.elementAt(f);
               			fromStr = filter.process(fromStr);
               			messageStr = filter.process(messageStr);
               		}
               }
               i++;
               
               //Très vilain fix pour résoudre le problème des messages doublés sous Firefox
			   //Pas trouvé d'autres solutions !
			   if (i <v.size())
			   {
					nextMessage = (Msg) v.elementAt(i);
                	if (nextMessage.getFrom().equals(fromStr) && nextMessage.getMsg().equals(messageStr) && nextMessage.getDate().getTime() - message.getDate().getTime() < 500)
                		continue;
               }
               
               lines++;
			   sb.append("<tr>\n");
               // System Administrator message.
               if ( message.getType() == Msg.SYSTEM )
               {
                  sb.append("<td valign=top><font color=\"#ff0000\" size=\"2\" face=\"Courier New, Courier, mono\">### "+message.getMsg()+" ###</font></td>\n");
			   }
               // Join message.
               if ( message.getType() == Msg.JOIN )
               {
                  sb.append("<td valign=top><font color=\"#009900\" size=\"2\" face=\"Courier New, Courier, mono\">"+messageStr+" a rejoint le salon.</font></td>\n");
               }
               // Quit message.
                else if ( message.getType() == Msg.LEFT )
               {
                  sb.append("<td valign=top><font color=\"#cc6600\" size=\"2\" face=\"Courier New, Courier, mono\">"+messageStr+" a quitté le salon.</font></td>\n");
               }
               // Private message.
               else if ( message.getType() == Msg.PRIVATE )
               {
                  sb.append("<td valign=top><font color=\"#333399\" size=\"2\" face=\"Courier New, Courier, mono\">&lt;"+fromStr+"&gt;&nbsp;"+messageStr+"</font></td>\n");
               }
               // Message for all users in the chatroom.
               else
               {
                  sb.append("<td valign=top><font color=\"#000000\" size=\"2\" face=\"Courier New, Courier, mono\">&lt;"+fromStr+"&gt;&nbsp;"+messageStr+"</font></td>\n");
               }
               sb.append("</tr>\n");
            }
      		sb.append("</table>\n");
         }
	 	 out.print(new String(sb));
	   }
	%>
  </td>
	<td valign=bottom align=right>
      <table border=0 cellspacing=0 cellpadding=0>
        <tr class="intfdcolor">
          <td>
            <table width=100% border=0 cellspacing=1 cellpadding=5 bgcolor="#666666">
              <tr align=center>
                <td nowrap bgcolor="#666666"><font color="#ffffff" size="1" face="Verdana, Arial, Helvetica, sans-serif"><b>Utilisateurs : <%= chatroomTotalUsers %>/<%= chatroomMaxUsers %></b></font></td>
              </tr>
              <tr bgcolor="#ffffff">
                <td nowrap><font color="#000000" size="1" face="Verdana, Arial, Helvetica, sans-serif">
				<%
				  /** Display users list. Links to privatemsg could be enabled. Head depends on user Type.*/
				  if (chatroom != null)
				  {
					String priv = null;
					sb = new StringBuffer();
				  	Hashtable table = chatroom.getUsers();
				    Enumeration e = table.keys();
			        String name = null;
			        String username = null;
      				ChatroomUser user = null;
         			Vector filters = chatroom.getParams().getFilters();
         			jChatBox.Chat.Filter.Filter filter = null;
      				while (e.hasMoreElements())
      				{
              			name = (String) e.nextElement();
              			user = (ChatroomUser) table.get(name);
              			username = user.getName();
              			/** Filter username for non-SYSTEM users */
						if (user.getType() != User.SYSTEM)
						{
							for (int f=0;f<filters.size();f++)
							{
								filter = (jChatBox.Chat.Filter.Filter) filters.elementAt(f);
		               			username = filter.process(username);
		               		}
		                }
              			if (chatroom.getParams().getPrivateStatus()==true) priv = "<a href=\"javascript:privatemsg('"+user.getName()+"')\"><font color=\"#000000\" size=\"1\" face=\"Verdana, Arial, Helvetica, sans-serif\">"+username+"</font></a><br>";
						else priv = username+"<br>";
              			if (user.getType() == User.SYSTEM) priv="@"+priv;
              			else if (user.getType() == User.MODERATOR) priv="#"+priv;
              			sb.append(priv);
				  	}
      				sb.append("");
      				out.print(new String(sb));
      			  }
				%>
                </font></td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
      <br>
    </td>
  </tr>
</table>
<a name="bas"></a>
</center>
</body>
</html>