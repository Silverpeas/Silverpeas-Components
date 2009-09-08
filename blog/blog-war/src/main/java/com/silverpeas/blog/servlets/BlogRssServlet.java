package com.silverpeas.blog.servlets;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import com.silverpeas.blog.control.ejb.BlogBm;
import com.silverpeas.blog.control.ejb.BlogBmHome;
import com.silverpeas.blog.model.BlogRuntimeException;
import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.peasUtil.RssServlet;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class BlogRssServlet extends RssServlet
{
    /* (non-Javadoc)
     * @see com.silverpeas.peasUtil.RssServlet#getListElements(java.lang.String, int)
     */
    public Collection getListElements(String instanceId, int nbReturned) throws RemoteException {
    	//récupération de la liste des 10 prochains billets du Blog
    	Collection events = getBlogBm().getAllPosts(instanceId, nbReturned);
    	return events;
    }
    
    /* (non-Javadoc)
     * @see com.silverpeas.peasUtil.RssServlet#getElementTitle(java.lang.Object, java.lang.String)
     */
    public String getElementTitle(Object element, String userId) {
    	PostDetail post = (PostDetail) element;
    	return post.getPublication().getName();
    }
    
    /* (non-Javadoc)
     * @see com.silverpeas.peasUtil.RssServlet#getElementLink(java.lang.Object, java.lang.String)
     */
    public String getElementLink(Object element, String userId) {
    	PostDetail post = (PostDetail) element;
    	return URLManager.getApplicationURL()+"/Publication/"+ post.getPublication().getPK().getId();
    }
    
    /* (non-Javadoc)
     * @see com.silverpeas.peasUtil.RssServlet#getElementDescription(java.lang.Object, java.lang.String)
     */
    public String getElementDescription(Object element, String userId) {
    	PostDetail post = (PostDetail) element;
    	return post.getPublication().getDescription();
    }
    
    /* (non-Javadoc)
     * @see com.silverpeas.peasUtil.RssServlet#getElementDate(java.lang.Object)
     */
    public Date getElementDate(Object element) {
    	PostDetail post = (PostDetail) element;
    	Calendar calElement = GregorianCalendar.getInstance();
    	calElement.setTime(post.getPublication().getCreationDate());
		/*calElement.add(Calendar.HOUR_OF_DAY, -1);  //-1 car bug d'affichage du fil RSS qui affiche toujours 1h en trop*/
    	calElement.add(Calendar.HOUR_OF_DAY, 0);
    	return calElement.getTime();
    }
  
   public String getElementCreatorId(Object element) {
	   PostDetail post = (PostDetail) element;
	   return post.getPublication().getUpdaterId();
	}

private BlogBm getBlogBm()
	{
		BlogBm blogBm = null;
		try 
		{
			BlogBmHome blogBmHome = (BlogBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.BLOGBM_EJBHOME, BlogBmHome.class);
			blogBm = blogBmHome.create();
		} 
		catch (Exception e) 
		{
			throw new BlogRuntimeException("RssServlet.getBlogBm()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
		}
		return blogBm;
	}
}