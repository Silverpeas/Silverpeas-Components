package com.silverpeas.blog.control.ejb;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;

import javax.ejb.EJBObject;

import com.silverpeas.blog.model.Archive;
import com.silverpeas.blog.model.Category;
import com.silverpeas.blog.model.PostDetail;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/** 
 * @author
 */
public interface BlogBm extends EJBObject 
{
	public String createPost(PostDetail post) throws RemoteException;
	public void updatePost(PostDetail post) throws RemoteException;
	public void deletePost(String postId, String instanceId) throws RemoteException;
	public PostDetail getPost(PublicationPK pubPK) throws RemoteException;
	public Collection<PostDetail> getAllPosts(String instanceId, int nbReturned) throws RemoteException;
	public Collection<PostDetail> getLastPosts(String instanceId) throws RemoteException;
	
	public Date getDateEvent(String pubId) throws RemoteException;
	
	public Collection<PostDetail> getPostsByCategory(String categoryId, String instanceId) throws RemoteException;
	public Collection<PostDetail> getPostsByArchive(String beginDate, String endDate, String instanceId) throws RemoteException;
	public Collection<PostDetail> getPostsByDate(String date, String instanceId) throws RemoteException;
	public Collection<PostDetail> getResultSearch(String word, String userId, String spaceId, String instanceId) throws RemoteException;
	
	public String createCategory(Category category) throws RemoteException;
	public void deleteCategory(String categoryId, String instanceId) throws RemoteException;
	public void updateCategory(Category category) throws RemoteException;
	public Category getCategory(NodePK nodePK) throws RemoteException;
	public Collection<NodeDetail> getAllCategories(String instanceId)throws RemoteException;
	
	public Collection<Archive> getAllArchives(String instanceId)throws RemoteException;
	
	public int getSilverObjectId(PublicationPK pubPK)throws RemoteException;
	
	public void indexBlog(String componentId) throws RemoteException;
	
	public void externalElementsOfPublicationHaveChanged(PublicationPK pubPK, String userId) throws RemoteException;
	
	public void addSubscription(NodePK topicPK, String userId) throws RemoteException;
	public void sendSubscriptionsNotification(NodePK fatherPK, PublicationDetail pubDetail, String type) throws RemoteException;
}
