package com.stratelia.webactiv.forums.forumsManager.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.ejb.EJBObject;

import com.stratelia.webactiv.forums.forumEntity.ejb.ForumPK;
import com.stratelia.webactiv.forums.messageEntity.ejb.MessagePK;
import com.stratelia.webactiv.forums.models.Category;
import com.stratelia.webactiv.forums.models.Forum;
import com.stratelia.webactiv.forums.models.Message;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Cette classe est l'interface Remote du Business Manager qui gere les forums
 * @author frageade
 * @since September 2000
 */
public interface ForumsBM extends EJBObject {
	
	public Forum getForum(ForumPK forumPK) throws RemoteException;
	
	public String getForumName(int forumId) throws RemoteException;
	
	public boolean isForumActive(int forumId) throws RemoteException;
	
	public int getForumParentId(int forumId) throws RemoteException;
	
	public String getForumInstanceId(int forumId) throws RemoteException;
	
	public Collection getForums(Collection forumPKs) throws RemoteException;
	
	public Collection getForumsList(Collection forumPKs) throws RemoteException;
	
	public Collection getThreadsList(Collection messagePKs) throws RemoteException;

	public ArrayList getForums(ForumPK forumPK) throws RemoteException;
	
	public ArrayList getForumsByCategory(ForumPK forumPK, String categoryId) throws RemoteException;

	public ArrayList getForumSonsIds(ForumPK forumPK) throws RemoteException;

	public int createForum(ForumPK forumPK, String forumName, String forumDescription,
			String forumCreator, int forumParent, String categoryId, String keywords)
		throws RemoteException;

	public void updateForum(ForumPK forumPK, String forumName, String forumDescription,
			int forumParent, String categoryId, String keywords)
		throws RemoteException;

	public void lockForum(ForumPK forumPK, int level) throws RemoteException;

	public int unlockForum(ForumPK forumPK, int level) throws RemoteException;

	public void deleteForum(ForumPK forumPK) throws RemoteException;

	public Collection getMessages(ForumPK forumPK) throws RemoteException;

	public Message getMessage(MessagePK messagePK) throws RemoteException;
	
	public String getMessageTitle(int messageId) throws RemoteException;
	
	public int getMessageParentId(int messageId) throws RemoteException;

	public Message getLastMessage(ForumPK forumPK) throws RemoteException;

	public Message getLastMessage(ForumPK forumPK, int messageParentId) throws RemoteException;

	public Message getLastMessage(ForumPK forumPK, List messageParentId) throws RemoteException;

	public Collection getLastMessageRSS(String instanceId, int nbReturned) throws RemoteException;

	public int getNbMessages(int forumId, String type) throws RemoteException;

	public int getAuthorNbMessages(String userId) throws RemoteException;

	public int getNbResponses(int forumId, int messageId) throws RemoteException;

	public boolean isNewMessageByForum(String userId, ForumPK forumPK) throws RemoteException;

	public boolean isNewMessage(String userId, ForumPK forumPK, int messageId)
		throws RemoteException;

	public void setLastVisit(String userId, int messageId) throws RemoteException;

  	public int createMessage(MessagePK messagePK, String messageTitle, String messageAuthor,
			Date messageCreationdate, int messageForum, int messageParent, String messageText,
			String keywords)
		throws RemoteException;
	
	public void updateMessage(MessagePK messagePK, String title, String message, String userId)
		throws RemoteException;
	
	public void updateMessageKeywords(MessagePK messagePK, String keywords)
	throws RemoteException;

	public void deleteMessage(MessagePK messagePK) throws RemoteException;

	public boolean isModerator(String userId, ForumPK forumPK) throws RemoteException;

	public void addModerator(ForumPK forumPK, String userId) throws RemoteException;

	public void removeModerator(ForumPK forumPK, String userId) throws RemoteException;

	public void removeAllModerators(ForumPK forumPK) throws RemoteException;

	public void moveMessage(MessagePK messagePK, ForumPK forumPK) throws RemoteException;

	public void subscribeMessage(MessagePK messagePK, String userId) throws RemoteException;

	public void unsubscribeMessage(MessagePK messagePK, String userId) throws RemoteException;

	public void removeAllSubscribers(MessagePK messagePK) throws RemoteException;

	public Vector listAllSubscribers(MessagePK messagePK) throws RemoteException;

	public boolean isSubscriber(MessagePK messagePK, String userId) throws RemoteException;

	public void createIndex(ForumPK forumPK) throws RemoteException;

	public void createIndex(MessagePK messagePK) throws RemoteException;

	public int getSilverObjectId(ForumPK forumPK) throws RemoteException;

	public String createCategory(Category category) throws RemoteException;

	public void deleteCategory(String categoryId, String instanceId) throws RemoteException;

	public void updateCategory(Category category) throws RemoteException;

	public Category getCategory(NodePK nodePK) throws RemoteException;

	public Collection getAllCategories(String instanceId) throws RemoteException;

	public String getForumTags(ForumPK forumPK) throws RemoteException;

	public String getMessageTags(MessagePK messagePK) throws RemoteException;

    public Collection getLastThreads(ForumPK forumPK, int count) throws RemoteException;
    
    public Collection getNotAnsweredLastThreads(ForumPK forumPK, int count) throws RemoteException;
    
}