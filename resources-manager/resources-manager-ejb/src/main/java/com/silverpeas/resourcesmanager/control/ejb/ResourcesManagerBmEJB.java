package com.silverpeas.resourcesmanager.control.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.resourcesmanager.model.CategoryDetail;
import com.silverpeas.resourcesmanager.model.ReservationDetail;
import com.silverpeas.resourcesmanager.model.ResourceDetail;
import com.silverpeas.resourcesmanager.model.ResourcesManagerDAO;
import com.silverpeas.resourcesmanager.model.ResourcesManagerRuntimeException;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

/** 
 * @author
 */
public class ResourcesManagerBmEJB implements SessionBean {

	public void ejbCreate()
	{
		// not implemented
	}

	public void setSessionContext(SessionContext context)
	{
		// not implemented
	}

	public void ejbRemove()
	{
		// not implemented
	}

	public void ejbActivate()
	{
		// not implemented
	}

	public void ejbPassivate()
	{
		// not implemented
	}

	/***Gestion des catégories ***/
	public void createCategory(CategoryDetail category){
		Connection con = initCon();
		try
		{
			int id = ResourcesManagerDAO.createCategory(con, category);
			
			category.setId(Integer.toString(id));
			createIndex_Category(category);
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.createCategory()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_CREATE_CATEGORY", e);
		}
		finally
		{
			// fermer la connexion
			fermerCon(con);
		}
	}

	public List getCategories(String instanceId){
		Connection con = initCon();
		try
		{
			List list = ResourcesManagerDAO.getCategories(con, instanceId);
			return list;
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getCategories()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_CATEGORIES", e);
		}
		finally
		{
			// fermer la connexion
			fermerCon(con);
		}
	}

	public CategoryDetail getCategory(String id){
		Connection con = initCon();
		try
		{
			CategoryDetail category =ResourcesManagerDAO.getCategory(con, id);
			return category;		
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getCategory()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_CATEGORY", e);
		}
		finally
		{			
			fermerCon(con);
		}
	}

	public void updateCategory(CategoryDetail category){
		Connection con = initCon();
		try
		{
			ResourcesManagerDAO.updateCategory(con, category);
			createIndex_Category(category);
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.updateCategory()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_UPDATE_CATEGORY", e);
		}
		finally
		{			
			fermerCon(con);
		}
	}

	public void deleteCategory(String id, String componentId) {
		Connection con = initCon();
		try
		{
			//First delete all resources of category 
			List resources = getResourcesByCategory(id);
			ResourceDetail resource;
			for (int r=0; r<resources.size(); r++)
			{
				resource = (ResourceDetail) resources.get(r);
				
				ResourcesManagerDAO.deleteResource(con, resource.getId());
				deleteIndex("Resource", resource.getId(), componentId);
			}
			
			//Then delete category itself
			ResourcesManagerDAO.deleteCategory(con, id);
			deleteIndex(id, "Category", componentId);
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.deleteCategory()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_DELETE_CATEGORY", e);
		}
		finally
		{		
			fermerCon(con);
		}
	}

	/****Gestion des ressources ***/
	public String createResource(ResourceDetail resource){
		Connection con = initCon();
		try
		{
			String id = ResourcesManagerDAO.createResource(con, resource);
			
			resource.setId(id);
			createIndex_Resource(resource);
			
			return id;
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.createResource()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_CREATE_RESOURCE", e);
		}
		finally
		{			
			fermerCon(con);
		}
	}

	public void updateResource(ResourceDetail resource){
		Connection con = initCon();
		try
		{
			ResourcesManagerDAO.updateResource(con, resource);
			createIndex_Resource(resource);
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.updateResource()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_UPDATE_RESOURCE", e);
		}
		finally
		{			
			fermerCon(con);
		}
	}

	public ResourceDetail getResource(String id){
		Connection con = initCon();
		try
		{
			ResourceDetail resource =ResourcesManagerDAO.getResource(con, id);
			return resource;
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getResource()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_RESOURCE", e);
		}
		finally
		{			
			fermerCon(con);
		}
	}

	public List getResourcesByCategory(String categoryId){
		Connection con = initCon();
		try
		{
			List list = ResourcesManagerDAO.getResourcesByCategory(con, categoryId);
			return list;
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getResourcesByCategory()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_RESOURCES_BY_CATEGORY", e);
		}
		finally
		{		
			fermerCon(con);
		}
	}


	public void deleteResource(String id, String componentId) {
		Connection con = initCon();
		try
		{
			ResourcesManagerDAO.deleteResource(con, id);
			deleteIndex(id, "Resource", componentId);
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.deleteResource()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_DELETE_RESOURCE", e);
		}
		finally
		{		
			fermerCon(con);
		}
	}

	public List getResourcesReservable(String instanceId, Date startDate, Date endDate){
		Connection con = initCon();
		try
		{
			List list = ResourcesManagerDAO.getResourcesReservable(con, instanceId, startDate, endDate);
			return list;
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getResourcesReservable()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_RESOURCES_RESERVABLE", e);
		}
		finally
		{		
			fermerCon(con);
		}
	}

	public List getResourcesofReservation(String instanceId, String reservationId){
		Connection con = initCon();
		try
		{
			List list = ResourcesManagerDAO.getResourcesofReservation(con, instanceId, reservationId);
			return list;
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getResourcesofReservation()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_RESOURCES_RESERVATION", e);
		}
		finally
		{
			fermerCon(con);
		}
	}


	/***Gestion des réservations **/
	public void saveReservation(ReservationDetail reservation, String listReservationCurrent){
		Connection con = initCon();
		try
		{
			String idReservation = ResourcesManagerDAO.saveReservation(con, reservation,listReservationCurrent);
			reservation.setId(idReservation);
			createIndex(reservation);
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.saveReservation()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_SAVE_RESERVATION", e);
		}
		finally
		{		
			fermerCon(con);
		}
	}

	public void updateReservation(String listReservation, ReservationDetail reservationCourante){
		Connection con = initCon();
		try
		{
			ResourcesManagerDAO.updateReservation(con, listReservation, reservationCourante);
			createIndex(reservationCourante);
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.updateReservation()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_UPDATE_RESERVATION", e);
		}
		finally
		{		
			fermerCon(con);
		}
	}

	public List verificationReservation(String instanceId, String listeReservation, Date startDate, Date endDate){
		Connection con = initCon();
		try
		{
			List list = ResourcesManagerDAO.verificationReservation(con, instanceId, listeReservation, startDate, endDate);
			return list;
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.verificationReservation()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_CHECK_RESERVATIONS", e);
		}
		finally
		{		
			fermerCon(con);
		}
	}

	public List verificationNewDateReservation(String instanceId, String listeReservation, Date startDate, Date endDate, String reservationId){
		Connection con = initCon();
		try
		{
			List list = ResourcesManagerDAO.verificationNewDateReservation(con, instanceId, listeReservation, startDate, endDate, reservationId);
			return list;
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.verificationNewDateReservation()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_CHECK_DATE_RESERVATION", e);
		}
		finally
		{		
			fermerCon(con);
		}
	}

	public List getReservationUser(String instanceId, String userId){
		Connection con = initCon();
		try
		{		
			return ResourcesManagerDAO.getReservationUser(con, instanceId, userId);
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getReservationUser()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_RESERVATIONS_USER", e);
		}
		finally
		{		
			fermerCon(con);
		}
	}

	public List getReservations(String instanceId){
		Connection con = initCon();
		try
		{		
			return ResourcesManagerDAO.getReservations(con, instanceId);
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getReservations()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_RESERVATIONS", e);
		}
		finally
		{		
			fermerCon(con);
		}
	}

	public ReservationDetail getReservation(String instanceId, String reservationId){
		Connection con = initCon();
		try
		{
			return ResourcesManagerDAO.getReservation(con, instanceId, reservationId);

		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getReservation()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_RESERVATION", e);
		}
		finally
		{		
			fermerCon(con);
		}
	}

	public void deleteReservation(String id, String componentId) {
		Connection con = initCon();
		try
		{
			deleteIndex(id, "Reservation", componentId);
			ResourcesManagerDAO.deleteReservation(con, id);	
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.deleteReservation()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_DELETE_RESERVATION", e);
		}
		finally
		{		
			fermerCon(con);
		}
	}

	public List getMonthReservation(String instanceId, Date monthDate,String userId, String language) {
		Connection con = initCon();
		try
		{
			return ResourcesManagerDAO.getMonthReservation(con,instanceId, monthDate, userId,language);	
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getMonthReservation()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_MONTHLY_RESERVATIONS", e);
		}
		finally
		{		
			fermerCon(con);
		}
	}

	public List getMonthReservationOfCategory(String instanceId, Date monthDate,String userId, String language,String idCategory) {
		Connection con = initCon();
		try
		{
			return ResourcesManagerDAO.getMonthReservationOfCategory(con,instanceId, monthDate, userId,language, idCategory);	
		}
		catch (Exception e)
		{
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.getMonthReservationOfCategory()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_GET_MONTHLY_RESERVATIONS", e);
		}
		finally
		{		
			fermerCon(con);
		}
	}

	private Connection initCon()
	{
		Connection con;
		// initialisation de la connexion
		try
		{
			con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
		}
		catch (UtilException e)
		{
			// traitement des exceptions
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.initCon()", SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
		}
		return con;
	}
	private void fermerCon(Connection con)
	{
		try
		{
			con.close();
		}
		catch (SQLException e)
		{
			// traitement des exceptions
			throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.fermerCon()", SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", e);
		}
	}
	
	private void createIndex_Category(CategoryDetail category)
	{
		SilverTrace.info("resourceManager", "resourceManagerBmEJB.createIndex_Category()", "root.MSG_GEN_ENTER_METHOD", "category = " + category.toString());
		FullIndexEntry indexEntry = null;

		if (category != null)
		{
			indexEntry = new FullIndexEntry(category.getInstanceId(), "Category", category.getId());
			indexEntry.setTitle(category.getName());
			indexEntry.setPreView(category.getDescription());
			if (category.getUpdateDate() != null)
				indexEntry.setCreationDate(category.getUpdateDate());
			else
				indexEntry.setCreationDate(category.getCreationDate());
			indexEntry.setCreationUser(category.getCreaterId());
			IndexEngineProxy.addIndexEntry(indexEntry);
		}
	}
	
	private void createIndex_Resource(ResourceDetail resource)
	{
		SilverTrace.info("resourceManager", "resourceManagerBmEJB.createIndex_Resource()", "root.MSG_GEN_ENTER_METHOD", "resource = " + resource.toString());
		FullIndexEntry indexEntry = null;

		if (resource != null)
		{
			// Index the Reservation
			indexEntry = new FullIndexEntry(resource.getInstanceId(), "Resource", resource.getId());
			indexEntry.setTitle(resource.getName());
			indexEntry.setPreView(resource.getDescription());
			if (resource.getUpdateDate() != null)
				indexEntry.setCreationDate(resource.getUpdateDate());
			else
				indexEntry.setCreationDate(resource.getCreationDate());
			indexEntry.setCreationUser(resource.getCreaterId());
			
			String categoryId = resource.getCategoryId();
			if (StringUtil.isDefined(categoryId))
			{
				CategoryDetail category = getCategory(categoryId);
				if (category != null)
				{
					String xmlFormName = category.getForm();
					if (StringUtil.isDefined(xmlFormName))
					{
						// indéxation du contenu du formulaire XML
						String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf("/")+1, xmlFormName.indexOf("."));
						PublicationTemplate pubTemplate;
						try
						{
							pubTemplate = PublicationTemplateManager.getPublicationTemplate(resource.getInstanceId()+":"+xmlFormShortName);
							RecordSet set = pubTemplate.getRecordSet();
							set.indexRecord(resource.getId(), xmlFormName, indexEntry);					
						}
						catch (Exception e)
						{
							throw new ResourcesManagerRuntimeException("ResourceManagerBmEJB.createIndex_Resource()", SilverpeasRuntimeException.ERROR, "resourcesManager.EX_CREATE_INDEX_FAILED", e);
						}											
					}
				}
			}
			
			IndexEngineProxy.addIndexEntry(indexEntry);
		}
	}

	private void createIndex(ReservationDetail reservation)
	{
		SilverTrace.info("resourceManager", "resourceManagerBmEJB.createIndex()", "root.MSG_GEN_ENTER_METHOD", "reservation = " + reservation);
		FullIndexEntry indexEntry = null;

		if (reservation != null)
		{
			// Index the Reservation
			indexEntry = new FullIndexEntry(reservation.getInstanceId(), "Reservation", reservation.getId());
			indexEntry.setTitle(reservation.getEvent());
			indexEntry.setPreView(reservation.getReason());
			indexEntry.setCreationDate(reservation.getCreationDate());
			indexEntry.setCreationUser(reservation.getUserId());
			indexEntry.setKeyWords(reservation.getPlace());
			IndexEngineProxy.addIndexEntry(indexEntry);
		}
	}
	
	private void deleteIndex(String objectId, String objectType, String componentId)
	{		
		IndexEntryPK indexEntry = new IndexEntryPK(componentId, objectType, objectId);
		IndexEngineProxy.removeIndexEntry(indexEntry);
	}

	public void indexResourceManager(String instanceId)
	{		
		List listOfReservation = getReservations(instanceId);
		if (listOfReservation != null)
		{
			Iterator it = listOfReservation.iterator();
			while (it.hasNext())
			{
				// on récupère chaque réservation
				ReservationDetail reservation = (ReservationDetail) it.next();				
				try
				{
					createIndex(reservation);
				}
				catch (Exception e)
				{
					throw new ResourcesManagerRuntimeException("ResourcesManagerBmEJB.indexResourceManager()", SilverpeasRuntimeException.ERROR, "resourcesManager.MSG_INDEXRESERVATIONS", e);
				}				
			}
		}
	}

}