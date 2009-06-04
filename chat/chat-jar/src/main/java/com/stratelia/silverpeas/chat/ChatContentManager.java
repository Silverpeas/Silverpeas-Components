package com.stratelia.silverpeas.chat;

import jChatBox.Chat.Chatroom;
import jChatBox.Chat.ChatroomManager;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * The chat implementation of ContentInterface.
 */
public class ChatContentManager implements ContentInterface
{
   /**
	* Find all the SilverContent with the given list of SilverContentId
	* @param ids list of silverContentId to retrieve
	* @param peasId the id of the instance
	* @param userId the id of the user who wants to retrieve silverContent
	* @param userRoles the roles of the user
	* @return a List of SilverContent
	*/
   public List getSilverContentById(List ids, String peasId, String userId, List userRoles)
   {
	   if (getContentManager() == null) return new ArrayList();

	   return getHeaders( makePKArray( ids ), peasId );
   }

   public int getSilverObjectId(String chatRoomId, String peasId) throws ChatException {
		SilverTrace.info("chat","ChatContentManager.getSilverObjectId()", "root.MSG_GEN_ENTER_METHOD", "chatRoomId = "+chatRoomId);
		try {
			return getContentManager().getSilverContentId(chatRoomId, peasId);
		} catch (Exception e) {
			throw new ChatException("ChatContentManager.getSilverObjectId()",SilverpeasException.ERROR,"chat.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
		}
	}

	/**
	* add a new content. It is registered to contentManager service
	* @param con a Connection
	* @param chatRoom the content to register
	* @return the unique silverObjectId which identified the new content
	*/
   public int createSilverContent(Connection con, ChatRoomDetail chatRoom) throws ContentManagerException
   {
	  SilverContentVisibility scv = new SilverContentVisibility();
	  SilverTrace.info("chat","ChatContentManager.createSilverContent()", "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "+scv.toString());
	  return getContentManager().addSilverContent(con, chatRoom.getId(), chatRoom.getInstanceId(), chatRoom.getCreatorId(), scv);
   }

   /**
	* delete a content. It is registered to contentManager service
	* @param con a Connection
	* @param chatRoomId the identifiant of the content to unregister
	* @param componentId the identifiant of the component instance where the content to unregister is
	*/
   public void deleteSilverContent(Connection con, String chatRoomId, String componentId) throws ContentManagerException
   {
	  int contentId = getContentManager().getSilverContentId(chatRoomId, componentId);
	  if (contentId != -1)
	  {
		  SilverTrace.info("chat","ChatContentManager.deleteSilverContent()", "root.MSG_GEN_ENTER_METHOD", "chatRoomId = "+chatRoomId+", contentId = "+contentId);
		  getContentManager().removeSilverContent(con, contentId, componentId);
	  }
   }

   /**
	* return a list of room ids according to a list of silverContentId
	* @param idList a list of silverContentId
	* @return a list of String representing room ids
	*/
   private ArrayList makePKArray(List idList)
   {
      ArrayList		roomIds		= new ArrayList();
      Iterator		iter	= idList.iterator();
	  String		id		= null;

	  //for each silverContentId, we get the corresponding roomId
      while (iter.hasNext())
      {
		    int contentId = ((Integer) iter.next()).intValue();
			try
			{
				id = getContentManager().getInternalContentId(contentId);
				roomIds.add(id);
			}
			catch (ClassCastException ignored)
			{
			   // ignore unknown item
			}
			catch (ContentManagerException ignored)
			{
			   // ignore unknown item
			}
      }
	  return roomIds;
   }   

	/**
	 * return a list of silverContent according to a list of publicationPK
	 * @param ids a list of room ids
	 * @param peasId the component Id
	 * @return a list of ChatRoomDetail
	 */
	private List getHeaders(List ids, String peasId)
	{
		Iterator iter					= ids.iterator();
		ArrayList headers				= new ArrayList();
		int roomId						= -1;
		ChatroomManager	chatroomManager = ChatroomManager.getInstance();
		Chatroom aChatroom				= null;
		ChatRoomDetail roomDetail		= null;
		
		while (iter.hasNext())
		{
			roomId = Integer.parseInt((String) iter.next());

			try
			{
				aChatroom = chatroomManager.getChatroom(roomId);
				if (aChatroom != null)
				{ 
					roomDetail = new ChatRoomDetail(peasId, 
													String.valueOf(roomId), 
													aChatroom.getParams().getName(),
													aChatroom.getParams().getSubject(),
													null
													);
					headers.add(roomDetail);
				}
			}
			catch (jChatBox.Chat.ChatException ex)
			{
				SilverTrace.error("chat", "ChatContentManager.getHeaders()", "chat.MSG_ERR_GENERAL", null, ex);
			}
		}
		
		return headers;
	}

	private ContentManager getContentManager()
	{
	   if (contentManager == null)
		{
			try
			{
		      contentManager = new ContentManager();
			}
			catch (Exception e)
			{
			    SilverTrace.fatal("chat", "ChatContentManager", "root.EX_UNKNOWN_CONTENT_MANAGER", e);
			}
		}
		return contentManager;
	}

	private ContentManager	contentManager			= null;
}