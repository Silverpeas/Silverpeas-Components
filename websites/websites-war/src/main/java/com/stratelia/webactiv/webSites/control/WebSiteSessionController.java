/*
 * webSiteSessionController.java
 *
 * Created on 9 Avril 2001, 11:25
 */

package com.stratelia.webactiv.webSites.control;

/** 
 * This is the webSite manager main interface
 * It contains all of the methods to be accessible to the client
 * @author Cécile BONIN
 * @version 1.0
 */

import java.io.File;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.RemoveException;

import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.webSites.control.ejb.WebSiteBm;
import com.stratelia.webactiv.webSites.control.ejb.WebSiteBmHome;
import com.stratelia.webactiv.webSites.siteManage.model.FolderDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SiteDetail;
import com.stratelia.webactiv.webSites.siteManage.util.Expand;

public class WebSiteSessionController extends AbstractComponentSessionController
{

	/*-------------- Attributs ------------------*/
	private WebSiteBm webSiteEjb = null;
	private WebSiteBmHome webSiteEjbHome = null;

	//Session objects
	private FolderDetail sessionTopic = null;
	//CBO : REMOVE private UserCompletePublication sessionPublication = null;
	//CBO : REMOVE private String sessionPath = null;
	//CBO : REMOVE private FolderDetail sessionTopicToLink = null;
	//CBO : REMOVE private boolean sessionOwner = false;
	private NotificationSender notifSender = null;
	
	//CBO : ADD
	private SiteDetail sessionSite = null;

	private String siteName;

	public final static String TAB_PDC = "tabPdc";

	/*-------------- Methodes de la classe ------------------*/

	/**
	 new WebSiteSessionController
	 */
	public WebSiteSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
	{
		//CBO : UPDATE
		//super(mainSessionCtrl, componentContext, "com.stratelia.webactiv.webSites.multilang.webSiteBundle");
		super(mainSessionCtrl, componentContext, "com.stratelia.webactiv.webSites.multilang.webSiteBundle", null, "com.stratelia.webactiv.webSites.settings.webSiteSettings");
		//CBO : REMOVE checkAuthorsAndDates();
		initEJB();
	}

	/**
	 initEJB
	 */
	private void initEJB()
	{
		//1 - Remove all data store by this SessionController (includes EJB)
		webSiteEjb = null;
		removeSessionTopic();
		
		//CBO : ADD
		removeSessionSite();
		
		//CBO : REMOVE removeSessionTopicToLink();
		//CBO : REMOVE removeSessionPublication();
		//CBO : REMOVE removeSessionPath();
		//CBO : REMOVE removeSessionOwner();

		//2 - Init EJB used by this SessionController
		try
		{
			setWebSiteEJB();
		}
		catch (Exception e)
		{
			throw new EJBException(e);
		}
		try
		{
			setSpaceLabel(getSpaceLabel());
		}
		catch (Exception e)
		{
			throw new EJBException(e);
		}
		try
		{
			setSpaceId(getSpaceId());
		}
		catch (Exception e)
		{
			throw new EJBException(e);
		}
		try
		{
			setActor(getUserDetail());
		}
		catch (Exception e)
		{
			throw new EJBException(e);
		}
		try
		{
			setComponentId(getComponentId());
		}
		catch (Exception e)
		{
			throw new EJBException(e);
		}
	}
	
	public boolean isBookmarkMode() {
		if ("bookmark".equals(getComponentRootName()))
		{
			return true;
		}
		return false;
	}
	
	public boolean isSortedTopicsEnabled() {
		return "yes".equalsIgnoreCase(getComponentParameterValue("sortedTopics"));
    }

	/************************************************************************************************/
	// Current Space operations
	/************************************************************************************************/
	public synchronized void setSpaceLabel(String space) throws RemoteException
	{
		try
		{
			getWebSiteEJB().setSpaceName(space);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			setSpaceLabel(space);
		}
	}

	public synchronized void setSpaceId(String prefixTableName) throws RemoteException
	{
		try
		{
			getWebSiteEJB().setPrefixTableName(prefixTableName);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			setSpaceId(prefixTableName);
		}
	}

	public synchronized void setActor(UserDetail actor) throws RemoteException
	{
		try
		{
			getWebSiteEJB().setActor(actor);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			setActor(actor);
		}
	}

	public synchronized void setComponentId(String compoId) throws RemoteException
	{
		try
		{
			getWebSiteEJB().setComponentId(compoId);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			setComponentId(compoId);
		}
	}

	/**************************************************************************************/
	/* WebSite - Gestion des objets session                                                */
	/**************************************************************************************/
	public synchronized void setSessionTopic(FolderDetail topicDetail)
	{
		this.sessionTopic = topicDetail;
	}
	
	//CBO : ADD
	public synchronized void setSessionSite(SiteDetail siteDetail)
	{
		this.sessionSite = siteDetail;
	}

	//CBO : REMOVE
	/*public synchronized void setSessionTopicToLink(FolderDetail topicDetail)
	{
		this.sessionTopicToLink = topicDetail;
	}
	
	public synchronized void setSessionPublication(UserCompletePublication pubDetail)
	{
		this.sessionPublication = pubDetail;
	}

	public synchronized void setSessionPath(String path)
	{
		this.sessionPath = path;
	}
	
	public synchronized void setSessionOwner(boolean owner)
	{
		this.sessionOwner = owner;
	}*/
	//CBO : FIN REMOVE

	public synchronized FolderDetail getSessionTopic()
	{
		return this.sessionTopic;
	}
	
	//CBO : ADD
	public synchronized SiteDetail getSessionSite()
	{
		return this.sessionSite;
	}

//	CBO : REMOVE
	/*public synchronized FolderDetail getSessionTopicToLink()
	{
		return this.sessionTopicToLink;
	}

	public synchronized UserCompletePublication getSessionPublication()
	{
		return this.sessionPublication;
	}

	public synchronized String getSessionPath()
	{
		return this.sessionPath;
	}
	
	public synchronized boolean getSessionOwner()
	{
		return this.sessionOwner;
	}*/
	//CBO : FIN REMOVE

	public synchronized void removeSessionTopic()
	{
		setSessionTopic(null);
	}
	
	//CBO : ADD
	public synchronized void removeSessionSite()
	{
		setSessionSite(null);
	}

//	CBO : REMOVE
	/*public synchronized void removeSessionTopicToLink()
	{
		setSessionTopicToLink(null);
	}

	public synchronized void removeSessionPublication()
	{
		setSessionPublication(null);
	}

	public synchronized void removeSessionPath()
	{
		setSessionPath(null);
	}

	public synchronized void removeSessionOwner()
	{
		setSessionOwner(false);
	}*/
	//CBO : FIN REMOVE

	public NotificationSender getNotificationSender()
	{
		if (notifSender == null)
			notifSender = new NotificationSender(getComponentId());
		return notifSender;
	}

	/**
	setWebSiteEJB
	*/
	private synchronized void setWebSiteEJB() throws WebSitesException
	{
		if (webSiteEjb == null)
		{
			try
			{
				webSiteEjbHome = (WebSiteBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.WEBSITESBM_EJBHOME, WebSiteBmHome.class);
				webSiteEjb = webSiteEjbHome.create();
			}
			catch (Exception e)
			{
				throw new WebSitesException(
					"WebSiteSessionController.setWebSiteEJB()",
					SilverpeasException.ERROR,
					"webSites.EX_EJB_CREATION_FAIL",
					e);
			}
		}
	}

	/**
	getWebSiteEJB
	*/
	public synchronized WebSiteBm getWebSiteEJB()
	{
		return webSiteEjb;
	}

	public boolean isPdcUsed() {
		String parameterValue = getComponentParameterValue("usepdc");
		if (parameterValue == null || parameterValue.length() <= 0)	{
			return false;
		} else {
			if ("yes".equals(parameterValue.toLowerCase()))
				return true;
			else
				return false;
		}
    }

	/*----------------------------------------------------------------------------------------------------------*/

	/*-------------- Methodes métier de l'interface 
	WebSiteSessionController ------------------*/

	/* ** Gestion des thèmes ** */

	public synchronized FolderDetail getFolder(String id) throws WebSitesException
	{
		try
		{
			return webSiteEjb.goTo(id);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			return getFolder(id);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.getFolder()",
				SilverpeasException.ERROR,
				"webSites.EX_GET_FOLDER_FAILED",
				"id = " + id,
				re);
		}
	}

	public synchronized NodePK updateFolderHeader(NodeDetail nd, String alertType) throws WebSitesException
	{
		try
		{
			return webSiteEjb.updateFolder(nd, alertType);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			return updateFolderHeader(nd, alertType);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.updateFolderHeader()",
				SilverpeasException.ERROR,
				"webSites.EX_UPDATE_FOLDER_HEADER_FAILED",
				re);
		}
	}

	public synchronized NodeDetail getFolderDetail(String id) throws WebSitesException
	{
		try
		{
			return webSiteEjb.getFolderDetail(id);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			return getFolderDetail(id);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.getFolderDetail()",
				SilverpeasException.ERROR,
				"webSites.EX_GET_FOLDER_DETAIL_FAILED",
				"id = " + id,
				re);
		}
	}

	public synchronized NodePK addFolder(NodeDetail nd, String alertType) throws WebSitesException
	{
		try
		{
			return webSiteEjb.addFolder(nd, alertType);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			return addFolder(nd, alertType);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.addFolder()",
				SilverpeasException.ERROR,
				"webSites.EX_ADD_FOLDER_FAILED",
				re);
		}
	}

	public synchronized void deleteFolder(String id) throws WebSitesException
	{
		try
		{
			webSiteEjb.deleteFolder(id);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			deleteFolder(id);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.deleteFolder()",
				SilverpeasException.ERROR,
				"webSites.EX_DELETE_FOLDER_FAILED",
				"id = " + id,
				re);
		}
	}
	
	//CBO : ADD
	/**
	 * @param way
	 * @param topicId
	 * @throws RemoteException
	 */
	public synchronized void changeTopicsOrder(String way, String topicId) throws RemoteException {
		NodePK nodePK = new NodePK(topicId, getSpaceId(), getComponentId());
		webSiteEjb.changeTopicsOrder(way, nodePK, getSessionTopic().getNodePK());
    }
	
	/* ** gestion, des publi ** */

	public synchronized PublicationDetail getPublicationDetail(String pubId) throws WebSitesException
	{
		try
		{
			return webSiteEjb.getPublicationDetail(pubId);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			return getPublicationDetail(pubId);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.getPublicationDetail()",
				SilverpeasException.ERROR,
				"root.EX_GET_PUBLICATION_FAILED",
				"pubId = " + pubId,
				re);
		}
	}

	//CBO : REMOVE
	/*public synchronized String createPublication(PublicationDetail pubDetail) throws WebSitesException
	{
		try
		{
			return webSiteEjb.createPublication(pubDetail);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			return createPublication(pubDetail);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.createPublication()",
				SilverpeasException.ERROR,
				"root.EX_CREATE_PUBLICATION_FAILED",
				"pubDetail = " + pubDetail.toString(),
				re);
		}
	}

	public synchronized void updatePublication(PublicationDetail pubDetail) throws WebSitesException
	{
		try
		{
			webSiteEjb.updatePublication(pubDetail);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			updatePublication(pubDetail);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.updatePublication()",
				SilverpeasException.ERROR,
				"root.EX_UPDATE_PUBLICATION_FAILED",
				"pubDetail = " + pubDetail.toString(),
				re);
		}
	}*/

	public synchronized void deletePublication(String pubId) throws WebSitesException
	{
		try
		{
			webSiteEjb.deletePublication(pubId);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			deletePublication(pubId);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.deletePublication()",
				SilverpeasException.ERROR,
				"root.EX_DELETE_PUBLICATION_FAILED",
				"pubId = " + pubId,
				re);
		}
	}

	//CBO : REMOVE
	/*public synchronized void updateInfoDetail(String pubId, InfoDetail infos) throws WebSitesException
	{
		try
		{
			webSiteEjb.updateInfoDetail(pubId, infos);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			updateInfoDetail(pubId, infos);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.updateInfoDetail()",
				SilverpeasException.ERROR,
				"webSites.EX_PUBLICATION_INFOS_UPDATE_FAILED",
				"pubId = " + pubId + " , infos = " + infos.toString(),
				re);
		}
	}*/

	//CBO : UPDATE
	/*public synchronized FolderDetail getPublicationFather(String pubId) throws WebSitesException
	{
		try
		{
			return webSiteEjb.getPublicationFather(pubId);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			return getPublicationFather(pubId);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.getPublicationFather()",
				SilverpeasException.ERROR,
				"webSites.EX_GET_PUBLICATION_FATHER_FAILED",
				"pubId = " + pubId,
				re);
		}
	}*/
	public synchronized Collection getAllFatherPK(String pubId) throws WebSitesException
	{
		try
		{
			return webSiteEjb.getAllFatherPK(pubId);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			return getAllFatherPK(pubId);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.getPublicationFather()",
				SilverpeasException.ERROR,
				"webSites.EX_GET_PUBLICATION_FATHER_FAILED",
				"pubId = " + pubId,
				re);
		}
	}
	

	//CBO : ADD
	public synchronized void addPublicationToFolder(String pubId, String folderId) throws WebSitesException
	{
		try
		{
			webSiteEjb.addPublicationToTopic(pubId, folderId);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			addPublicationToFolder(pubId, folderId);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.addPublicationToFolder()",
				SilverpeasException.ERROR,
				"webSites.EX_PUBLICATION_ADD_TO_NODE_FAILED",
				"pubId = " + pubId+", folderId = "+folderId,
				re);
		}
	}
	
	public synchronized void removePublicationToFolder(String pubId, String folderId) throws WebSitesException
	{
		try
		{
			webSiteEjb.removePublicationToTopic(pubId, folderId);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			addPublicationToFolder(pubId, folderId);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.removePublicationToFolder()",
				SilverpeasException.ERROR,
				"webSites.EX_PUBLICATION_DELETE_TO_NODE_FAILED",
				"pubId = " + pubId+", folderId = "+folderId,
				re);
		}
	}
	//CBO : FIN ADD

	
	/**
	 * @param siteId
	 * @return
	 * @throws WebSitesException
	 */
	//CBO : UPDATE
	//public synchronized Collection getAllPublication(String siteId) throws WebSitesException
	public synchronized String getIdPublication(String siteId) throws WebSitesException
	{
		try
		{
			//CBO : UPDATE
			//return webSiteEjb.getAllPublication(siteId);
			return webSiteEjb.getIdPublication(siteId);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			//CBO : UPDATE
			//return getAllPublication(siteId);
			return getIdPublication(siteId);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.getIdPublication()",
				SilverpeasException.ERROR,
				"webSites.EX_GET_PUBLICATION_FAILED",
				"siteId =" + siteId,
				re);
		}
	}
	
	//	CBO : ADD
	
	/**
	 * @param pubId
	 * @param direction
	 * @throws RemoteException
	 */
	public synchronized void changePubsOrder(String pubId, int direction) throws RemoteException {
		webSiteEjb.changePubsOrder(pubId, getSessionTopic().getNodePK(), direction);
    }

	/* ** Gestion des sites ** */
	
	/**
	setSiteName
	*/
	public synchronized void setSiteName(String siteName)
	{
		this.siteName = siteName;
	}

	/**
	getSiteName
	*/
	public synchronized String getSiteName()
	{
		return siteName;
	}

	/**
	getAllWebSite 
	*/
	public synchronized Collection getAllWebSite() throws WebSitesException
	{
		try
		{
			return webSiteEjb.getAllWebSite();
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			return getAllWebSite();
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.getAllWebSite()",
				SilverpeasException.ERROR,
				"webSites.EX_GET_ALL_WEBSITES_FAILED",
				"",
				re);
		}
	}

	/**
	getWebSite 
	*/
	public synchronized SiteDetail getWebSite(String id) throws WebSitesException
	{
		try
		{
			return webSiteEjb.getWebSite(id);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			return getWebSite(id);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.getWebSite",
				SilverpeasException.ERROR,
				"webSites.EX_GET_WEBSITE_FAILED",
				"siteId =" + id,
				re);
		}
	}

	/**
	getIcons 
	*/
	public synchronized Collection getIcons(String id) throws WebSitesException
	{
		try
		{
			return webSiteEjb.getIcons(id);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			return getIcons(id);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.getIcons(id)",
				SilverpeasException.ERROR,
				"webSites.EX_GET_ICONS_FAILED",
				"siteId =" + id,
				re);
		}
	}

	/**
	getNextId
	*/
	public synchronized String getNextId() throws WebSitesException
	{
		try
		{
			return webSiteEjb.getNextId();
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			return getNextId();
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.getNextId()",
				SilverpeasException.ERROR,
				"root.EX_GET_NEXTID_FAILED",
				"",
				re);
		}
	}

	/**
	getAllIcons
	*/
	public synchronized Collection getAllIcons() throws WebSitesException
	{
		try
		{
			return webSiteEjb.getAllIcons();
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			return getAllIcons();
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.getAllIcons()",
				SilverpeasException.ERROR,
				"webSites.EX_GET_ALL_ICONS_FAILED",
				"",
				re);
		}
	}

	/**
	getAllSubFolder
	*/
	public synchronized Collection getAllSubFolder(String chemin) throws WebSitesException
	{
		/* chemin du repertoire = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite */

		try
		{
			return FileFolderManager.getAllSubFolder(chemin);
		}
		catch (Exception e)
		{
			throw new WebSitesException(
				"WebSiteSessionController.getAllSubFolder()",
				SilverpeasException.ERROR,
				"webSites.EX_GET_ALL_SUB_FOLDERS_FAIL",
				e);
		}
	}

	/**
	getAllFile
	*/
	public synchronized Collection getAllFile(String chemin) throws WebSitesException
	{
		/* chemin du repertoire = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite */

		try
		{
			return FileFolderManager.getAllFile(chemin);
		}
		catch (Exception e)
		{
			throw new WebSitesException(
				"WebSiteSessionController.getAllFile()",
				SilverpeasException.ERROR,
				"webSites.EX_GET_ALL_FILES_FAIL",
				e);
		}
	}

	/**
	getAllImages
	*/
	public synchronized Collection getAllImages(String chemin) throws WebSitesException
	{
		/* chemin du repertoire = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\rep */

		try
		{
			return FileFolderManager.getAllImages(chemin);
		}
		catch (Exception e)
		{
			throw new WebSitesException(
				"WebSiteSessionController.getAllImages()",
				SilverpeasException.ERROR,
				"webSites.EX_GET_ALL_IMAGES_FAIL",
				e);
		}
	}

	/**
	getAllWebPages2
	*/
	public synchronized Collection getAllWebPages2(String chemin) throws WebSitesException
	{
		/* chemin du repertoire = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\rep */

		try
		{
			return FileFolderManager.getAllWebPages2(chemin);
		}
		catch (Exception e)
		{
			throw new WebSitesException(
				"WebSiteSessionController.getAllWebPages2()",
				SilverpeasException.ERROR,
				"webSites.EX_GET_ALL_WEB_PAGES_FAIL",
				e);
		}
	}

	/**
	createWebSite 
	*/
	//CBO : UPDATE
	//public synchronized void createWebSite(SiteDetail description) throws WebSitesException
	public synchronized String createWebSite(SiteDetail description) throws WebSitesException
	{
		try
		{
			//CBO : UPDATE
			//description.setAuthor(getUserId());
			description.setCreatorId(getUserId());
			
			//CBO : UPDATE
			//description.setDate(DateUtil.today2SQLDate());
			description.setCreationDate(new Date());
			
			return webSiteEjb.createWebSite(description);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			return createWebSite(description);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.createWebSite(description)",
				SilverpeasException.ERROR,
				"webSites.EX_CREATE_WEBSITE_FAILED",
				"siteDetail =" + description.toString(),
				re);
		}
	}

	/**
	AssociateIcons
	*/
	public synchronized void associateIcons(String id, Collection listeIcones) throws WebSitesException
	{
		try
		{
			webSiteEjb.associateIcons(id, listeIcones);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			associateIcons(id, listeIcones);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.associateIcons(id, listeIcones)",
				SilverpeasException.ERROR,
				"webSites.EX_ASSOCIATE_ICONS_FAILED",
				"siteId =" + id,
				re);
		}
	}

	/**
	publish
	*/
	public synchronized void publish(Collection listeSite) throws WebSitesException
	{
		/* Collection d'id de site */
		try
		{
			webSiteEjb.publish(listeSite);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			publish(listeSite);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.publish(listeSite)",
				SilverpeasException.ERROR,
				"webSites.EX_PUBLISH_SELECTED_FAILED",
				"listeSite =" + listeSite.toString(),
				re);
		}
	}

	/**
	dePublish
	*/
	public synchronized void dePublish(Collection listeSite) throws WebSitesException
	{
		/* Collection d'id de site */
		try
		{
			webSiteEjb.dePublish(listeSite);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			dePublish(listeSite);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.dePublish(listeSite)",
				SilverpeasException.ERROR,
				"webSites.EX_DEPUBLISH_SELECTED_FAILED",
				"listeSite =" + listeSite.toString(),
				re);
		}
	}

	/**
	createFolder
	*/
	public synchronized void createFolder(String chemin) throws WebSitesException
	{
		/* chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite */
		try
		{
			FileFolderManager.createFolder(chemin);
		}
		catch (Exception e)
		{
			throw new WebSitesException(
				"WebSiteSessionController.createFolder()",
				SilverpeasException.ERROR,
				"webSites.EX_CREATE_FOLDER_FAIL",
				e);
		}
	}

	/**
	renameFolder
	*/
	public synchronized void renameFolder(String cheminRep, String newCheminRep) throws WebSitesException
	{
		/* chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\Folder */
		try
		{
			FileFolderManager.renameFolder(cheminRep, newCheminRep);
		}
		catch (Exception e)
		{
			throw new WebSitesException(
				"WebSiteSessionController.renameFolder()",
				SilverpeasException.ERROR,
				"webSites.EX_RENAME_FOLDER_FAIL",
				e);
		}
	}

	/**
	delFolder
	*/
	public synchronized void delFolder(String chemin) throws WebSitesException
	{
		/* chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\Folder */
		try
		{
			FileFolderManager.deleteFolder(chemin);
		}
		catch (Exception e)
		{
			throw new WebSitesException(
				"WebSiteSessionController.delFolder()",
				SilverpeasException.ERROR,
				"webSites.EX_DELETE_FOLDER_FAIL",
				e);
		}
	}

	/**
	createPage 
	*/
	public synchronized void createFile(String cheminFichier, String nomFichier, String contenuFichier) throws WebSitesException
	{
		/* cheminFichier = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\rep1\\rep2 */
		/* nomFichier = index.html */
		/* contenuFichier = code du fichier : "<HTML><TITLE>...." */
		String nouveauNom = nomFichier;
		try
		{
			FileFolderManager.createFile(cheminFichier, nouveauNom, contenuFichier);
		}
		catch (UtilException e)
		{
			throw new WebSitesException(
				"WebSiteSessionController.createFile()",
				SilverpeasException.ERROR,
				"webSites.EX_CREATE_FILE_FAIL",
				e);
		}
	}

	/**
	unzip 
	*/
	public synchronized void unzip(String cheminDirResultat, String cheminFichierZip) throws WebSitesException
	{
		SilverTrace.debug("webSites", "WebSiteSessionController.unzip", "root.MSG_GEN_ENTER_METHOD", "cheminDirResultat = "+cheminDirResultat+", cheminFichierZip = "+cheminFichierZip);
		/* cheminDirResultat = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite */
		/* cheminFichierZip = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\toto.zip */
		try
		{
			Expand exp = new Expand();
			File zip = new File(cheminFichierZip);
			File dest = new File(cheminDirResultat);
			exp.setSrc(zip);
			exp.setDest(dest);
			exp.execute();
		}
		catch (Exception e)
		{
			throw new WebSitesException("WebSiteSessionController.unzip()", SilverpeasException.ERROR, "webSites.EX_UNZIP_FILE_FAIL", e);
		}
	}

	/**
	deleteWebSites
	*/
	public synchronized void deleteWebSites(Collection liste) throws WebSitesException
	{
		try
		{
			webSiteEjb.deleteWebSites(liste);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			deleteWebSites(liste);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.deleteWebSites(liste)",
				SilverpeasException.ERROR,
				"webSites.EX_DELETE_WEBSITES_FAILED",
				"listeSite =" + liste.toString(),
				re);
		}
	}
	
	//CBO : REMOVE
	/*public synchronized void deleteWebSitesFromUpdate(Collection liste) throws WebSitesException 
	{
        try {
        	webSiteEjb.deleteWebSitesFromUpdate(liste);
        } catch (NoSuchObjectException nsoe) {
			initEJB();
			deleteWebSitesFromUpdate(liste);
        } catch (RemoteException re) {
            throw new WebSitesException("WebSiteSessionController.deleteWebSitesFromUpdate()",SilverpeasException.ERROR,"webSites.EX_DELETE_WEBSITES_FAILED",re);
        }
   }*/
	
	/**
	 * @param description
	 * @throws WebSitesException
	 */
	//CBO : ADD
	public synchronized void updateWebSite(SiteDetail description) throws WebSitesException 
	{	
		try
		{
			description.setCreatorId(getUserId());
			description.setCreationDate(new Date());
			
			webSiteEjb.updateWebSite(description);
			
			//CBO : UPDATE
			//Collection publi = webSiteEjb.getAllPublication(description.getSitePK().getId());
			String pubId = webSiteEjb.getIdPublication(description.getSitePK().getId());
			
//			CBO : REMOVE String pubId = (String) publi.iterator().next();
			PublicationPK pubPk = new PublicationPK(pubId, getSpaceId(), getComponentId());
			description.setPk(pubPk);
			webSiteEjb.updatePublication(description);
		}
		catch (NoSuchObjectException nsoe)
		{
			initEJB();
			updateWebSite(description);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.updateWebSite(description)",
				SilverpeasException.ERROR,
				"webSites.EX_UPDATE_WEBSITE_FAILED",
				"siteDetail =" + description.toString(),
				re);
		}
   }

	/**
	deleteDirectory 
	*/
	public synchronized void deleteDirectory(String chemin) throws WebSitesException
	{
		/* cheminDirResultat = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite */
		boolean result = false;

		File directory = new File(chemin);
		/* recupere la liste des fichiers et directory du chemin */
		try
		{
			File[] dirFiles = directory.listFiles();

			for (int i = 0; i < dirFiles.length; i++)
			{
				delDir(dirFiles[i]);
			}
		}
		catch (Exception e)
		{
			SilverTrace.warn(
				"webSites",
				"WebSiteSessionController.deleteDirectory()",
				"webSites.EXE_LIST_FILES_FAIL",
				"path = " + chemin,
				e);
		}
		result = directory.delete();
		if (!result)
			throw new WebSitesException(
				"WebSiteSessionClientController.deleteDirectory()",
				SilverpeasException.ERROR,
				"webSites.EXE_DELETE_DIRECTORY_FAIL",
				"path = " + chemin);

	}

	/**
	delDir : procedure privee recursive
	*/
	private synchronized void delDir(File dir)
	{
		try
		{
			if (dir.isDirectory())
			{
				File[] dirFiles = dir.listFiles();
				for (int i = 0; i < dirFiles.length; i++)
				{
					delDir(dirFiles[i]);
				}
			}
			dir.delete();
		}
		catch (Exception e)
		{
			SilverTrace.warn(
				"webSites",
				"WebSiteSessionController.delDir()",
				"webSites.EXE_DELETE_DIRECTORY_FAIL",
				"path = " + dir.getPath(),
				e);
		}
	}

	/**
	renameFile
	*/
	public synchronized void renameFile(String rep, String name, String newName) throws WebSitesException
	{
		/* chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\Folder */
		try
		{
			FileFolderManager.renameFile(rep, name, newName);
		}
		catch (Exception e)
		{
			throw new WebSitesException(
				"WebSiteSessionController.renameFile()",
				SilverpeasException.ERROR,
				"webSites.EX_RENAME_FILE_FAIL",
				e);
		}
	}

	/**
	deleteFile
	*/
	public synchronized void deleteFile(String chemin) throws WebSitesException
	{
		/* chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\Folder\\File.html */
		try
		{
			FileFolderManager.deleteFile(chemin);
		}
		catch (Exception e)
		{
			throw new WebSitesException(
				"WebSiteSessionController.deleteFile()",
				SilverpeasException.ERROR,
				"webSites.EX_DELETE_FILE_FAIL",
				e);
		}
	}

	/**
	getCode
	*/
	public synchronized String getCode(String cheminFichier, String nomFichier) throws WebSitesException
	{
		/* cheminFichier = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\Folder 
		   nomFichier = File.html */
		try
		{
			return FileFolderManager.getCode(cheminFichier, nomFichier);
		}
		catch (Exception e)
		{
			throw new WebSitesException("WebSiteSessionController.getCode()", SilverpeasException.ERROR, "webSites.EX_GET_CODE_FAIL", e);
		}
	}

	/**
	verif
	*/
	public synchronized String verif(String action, String currentPath, String name, String newName, String nomPage)
		throws WebSitesException
	{
		String res = "";

		try
		{
			if (action.equals("addFolder"))
			{ //creation de rep
				File folder = new File(currentPath, name);
				if (folder.exists())
					res = "pbAjoutFolder";
				else
					res = "ok";
			}
			else
				if (action.equals("renameFolder"))
				{ //modification de nom de rep
					File folder = new File(currentPath, newName);
					if (folder.exists())
						res = "pbRenommageFolder";
					else
						res = "ok";
				}
				else
					if (action.equals("addPage"))
					{ //creation de fichier
						File fichier = new File(currentPath, nomPage);
						if (fichier.exists())
							res = "pbAjoutFile";
						else
							res = "ok";
					}
					else
						if (action.equals("renamePage"))
						{ //modification de nom de fichier
							File fichier = new File(currentPath, newName);
							if (fichier.exists())
								res = "pbRenommageFile";
							else
								res = "ok";
						}
						else
							res = "ok";
		}
		catch (Exception e)
		{
			throw new WebSitesException("WebSiteSessionController.verif()", SilverpeasException.ERROR, "webSites.EX_VERIF_FAIL", e);
		}
		return res;
	}

	public void index() throws RemoteException
	{
		getWebSiteEJB().index();
	}

	/**
	notifyPublishers
	*/
	public void notifyPublishers(String auteur, String nomSite, String description, String nomPage, String listeMessage, String date)
	{
		String subject = getString("SuggestionLink");
		String messageText = auteur + " " + getString("PropositionLien") + "  \n \n" +
		getString("VoiciDescriptionLien")
		+ "\n \n"
		+ getString("GML.name")
		+ " : "
		+ nomSite
		+ "\n"
		+ getString("GML.description")
		+ " : "
		+ description
		+ "\n"
		+ getString("URL")
		+ " : "
		+ nomPage
		+ "\n"
		+ getString("ListeIcones")
		+ " : \n"
		+ listeMessage
		+ "\n "
		+ getString("GML.creationDate")
		+" : \n"
		+date;

		try
		{
			List profileNames = new ArrayList();
			profileNames.add("Admin");
			profileNames.add("Publisher");
			String[] users = getOrganizationController().getUsersIdsByRoleNames(getComponentId(), profileNames);
			
			NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL, subject, messageText);
			notifMetaData.setSender(getUserId());
			notifMetaData.addUserRecipients(users);
			notifMetaData.setSource(getSpaceLabel() + " - " + getComponentLabel());
			getNotificationSender().notifyUser(notifMetaData);
		}
		catch (Exception e)
		{
			SilverTrace.warn("webSites", "WebSiteSessionController.notifyPublishers()", "webSites.MSG_NOTIFY_PUBLISHERS_FAIL", null, e);
		}
	}

	public int getSilverObjectId(String objectId) {
		int silverObjectId = -1;
		try
		{
			silverObjectId = webSiteEjb.getSilverObjectId(objectId);
		} catch (NoSuchObjectException nsoe) {
			initEJB();
			getSilverObjectId(objectId);
		} catch (Exception e)
		{
			SilverTrace.error("webSites", "WebSiteSessionController.getSilverObjectId()", "root.EX_CANT_GET_LANGUAGE_RESOURCE", "objectId=" + objectId, e);
		} 
		return silverObjectId;
	}

	public void close()
	{
		try
		{
			if (webSiteEjb != null) webSiteEjb.remove();
		}
		catch (RemoteException e)
		{
			SilverTrace.error("webSiteSession", "WebSiteSessionController.close", "", e);
		}
		catch (RemoveException e)
		{
			SilverTrace.error("webSiteSession", "WebSiteSessionController.close", "", e);
		}
	}
	
	/**
	 * @param idSite
	 * @param arrayTopic
	 * @throws WebSitesException
	 */
//	CBO : ADD
	public void updateClassification(String idSite, ArrayList arrayTopic) throws WebSitesException {
		try
		{
			//CBO : UPDATE
			/*
			 Collection listPubli = webSiteEjb.getAllPublication(idSite);//un seul résultat
			 if(listPubli != null) {
				webSiteEjb.updateClassification((String) listPubli.iterator().next(), arrayTopic);
			 }
			 */
			String idPub = webSiteEjb.getIdPublication(idSite);
			webSiteEjb.updateClassification(idPub, arrayTopic);
			
			//CBO : FIN UPDATE
		} catch (NoSuchObjectException nsoe)
		{
			initEJB();
			updateClassification(idSite, arrayTopic);
		}
		catch (RemoteException re)
		{
			throw new WebSitesException(
				"WebSiteSessionController.updateClassification",
				SilverpeasException.ERROR,
				"webSites.EX_PUBLICATION_UPDATE_FAILED",
				"site id =" + idSite,
				re);
		}
	}

	
	
	/********************************************************************************************************/
	/** Grosse rustine à enlever... un jour !
	 *  Pour reformatter le champ auteur et date de la table Site
	 */
//	CBO : REMOVE
	/*private void checkAuthorsAndDates()
	{
		Collection sites = null;
		try
		{
			sites = getWebSites();
		}
		catch (Exception e)
		{
			throw new WebSitesRuntimeException("WebSiteSessionController.checkAuthorsAndDates()", SilverpeasRuntimeException.ERROR, "webSites.EX_GET_WEBSITES_FAILED", e);
		}
		
		Iterator iSites = sites.iterator();
		SiteDetail site = null;
		while (iSites.hasNext())
		{
			site = (SiteDetail) iSites.next();
			try
			{
				Integer.parseInt(site.getAuthor());
			} 
			catch (NumberFormatException nfe)
			{
				//Le format n'est pas le bon (ancien)
				updateSite(site);
			}
		}
	}
	
	
	private Collection getWebSites() throws SQLException, UtilException
	{
		Connection con = null;
		ArrayList theSiteList = new ArrayList();

		Statement stmt = null;
		ResultSet rs1 = null;
		String queryStr1 = "SELECT * FROM SC_WEBSITES_SITE";

		try
		{
			con = DBUtil.makeConnection(JNDINames.SILVERPEAS_DATASOURCE);
			stmt = con.createStatement();
			rs1 = stmt.executeQuery(queryStr1);

			String idSite = "";
			String name = "";
			String description = "";
			String page = "";
			String author = "";
			String date = "";
			int type;
			int state;
			int popup;
			while (rs1.next())
			{
				idSite = new Integer(rs1.getInt(1)).toString();
				name = rs1.getString(2);
				description = rs1.getString(3);
				page = rs1.getString(4);
				type = rs1.getInt(5);
				author = rs1.getString(6);
				date = rs1.getString(7);
				state = rs1.getInt(8);
				popup = rs1.getInt(10);
				SiteDetail sitedetail = new SiteDetail(idSite, name, description, page, type, author, date, state, popup);

				theSiteList.add(sitedetail);
			}
		}
		finally
		{
			DBUtil.close(rs1, stmt);
			freeConnection(con);
		}

		return theSiteList;
	}
	
	private void updateSite(SiteDetail site)
	{
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		//Récupère l'id de l'auteur
		
		//CBO : UPDATE
		//int userId = getUserByFullName(site.getAuthor());
		int userId = getUserByFullName(site.getCreatorId());
		
		//Formatte correctement la date !
		String date = null;
		try
		{
			//CBO : UPDATE
			//date = DateUtil.date2SQLDate(site.getDate(), "fr");
			date = DateUtil.date2SQLDate(site.getCreationDate());
		}
		catch (Exception e)
		{
			date = DateUtil.today2SQLDate();
		}
		
		String query = null;
		try
		{
			con = DBUtil.makeConnection(JNDINames.SILVERPEAS_DATASOURCE);
			
			query = "UPDATE SC_WEBSITES_SITE SET siteAuthor = '"+userId+"', siteDate = '"+date+"' WHERE siteId = "+site.getId();
			SilverTrace.info("webSites", "WebSitesSessionController.updateSite", "root.MSG_GEN_PARAM_VALUE", "query = "+query);
			
			stmt = con.createStatement();
			stmt.executeUpdate(query);
		}
		catch (Exception e)
		{
			SilverTrace.error("webSites", "WebSitesSessionController.updateSite", "root.EX_SQL_QUERY_FAILED", "query = "+query, e);
		}
		finally
		{
			DBUtil.close(rs, stmt);
			freeConnection(con);
		}
	}
	
	
	/**
	 * @param userFullName = Nom Prénom
	 * @return
	 */
	/*private int getUserByFullName(String userFullName)
	{
		SilverTrace.info("webSites", "WebSitesSessionController.getUserByFullName", "root.MSG_GEN_PARAM_VALUE", "userFullName = "+userFullName);
		Connection 	con 	= null;
		Statement 	stmt 	= null;
		ResultSet 	rs 		= null;
		
		String nameBegin = userFullName;
		int pos = userFullName.indexOf(" ");
		if (pos != -1)
			nameBegin = userFullName.substring(0, pos);
		
		nameBegin = nameBegin.substring(0, nameBegin.length()-1);
		
		String query = null;
		try
		{
			con = DBUtil.makeConnection(JNDINames.SILVERPEAS_DATASOURCE);
			
			query = "SELECT id, lastName, firstName FROM ST_USER WHERE lastName like '"+nameBegin+"%' ORDER BY domainId desc";
			
			SilverTrace.info("webSites", "WebSitesSessionController.getUserByFullName", "root.MSG_GEN_PARAM_VALUE", "query = "+query);
			
			stmt 	= con.createStatement();
			rs 		= stmt.executeQuery(query);
			int id = -1;
			String lastName = null;
			String firstName = null;
			String currentFullName = null;
			while (rs.next())
			{
				id = rs.getInt(1);
				lastName = rs.getString(2);
				firstName = rs.getString(3);
				currentFullName = lastName;
				if (firstName != null)
					currentFullName += " " + firstName;
				
				if (userFullName.trim().equalsIgnoreCase(currentFullName.trim()))
					return id;
			}
			return -1;
		}
		catch (Exception e)
		{
			SilverTrace.error("webSites", "WebSitesSessionController.getUserByFullName", "root.EX_SQL_QUERY_FAILED", "query = "+query, e);
			return -1;
		}
		finally
		{
			DBUtil.close(rs, stmt);
			freeConnection(con);
		}
	}
	
	private void freeConnection(Connection con) {
		if (con != null)
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
				SilverTrace.error("webSites", "WebSiteBmEJB.freeConnection()", "root.EX_CONNECTION_CLOSE_FAILED", "", e);
			}
		}
	}*/
//	CBO : FIN REMOVE
}