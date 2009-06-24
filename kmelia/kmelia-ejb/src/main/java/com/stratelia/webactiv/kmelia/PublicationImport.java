package com.stratelia.webactiv.kmelia;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.fileitem.InternalFileItem;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBmEJB;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBm;
import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.IndexManager;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

public class PublicationImport {
	
	private KmeliaBmEJB kmeliaBm;
	private String componentId;
	private String topicId;
	private String spaceId;
	private String userId;
	
	public PublicationImport(KmeliaBmEJB kmeliaBm, String componentId, String topicId,
			String spaceId, String userId) {
		this.kmeliaBm = kmeliaBm;
		this.componentId = componentId;
		this.topicId = topicId;
		this.spaceId = spaceId;
		this.userId = userId;
	}

	/**
	 * Creates or updates a publication.
	 * 
	 * @param publiParams The parameters of the publication.
	 * @param formParams The parameters of the publication's form.
	 * @param language The language of the publication.
	 * @param xmlFormName The name of the publication's form.
	 * @param discrimatingParameterName The name of the field included in the form which allowes to
	 *        retrieve the eventually existing publication to update.
	 * @param userProfile The user's profile used to draft out the publication.
	 * @return True if the publication is created, false if it is updated.
	 * @throws RemoteException
	 */
    public boolean importPublication(Map publiParams, Map formParams, String language,
    		String xmlFormName, String discrimatingParameterName, String userProfile)
		throws RemoteException
	{
    	PublicationDetail pubDetail = null;
    	
    	String discrimatingParameterValue = (String)formParams.get(discrimatingParameterName);
    	String publicationToUpdateId = getPublicationId(
    		xmlFormName, discrimatingParameterName, discrimatingParameterValue);
    	
    	boolean resultStatus;
    	PublicationPK pubPK;
    	if (publicationToUpdateId != null)
    	{
    		// Update
    		resultStatus = false;
    		pubPK = new PublicationPK(publicationToUpdateId, spaceId, componentId);
    		pubDetail = kmeliaBm.getPublicationDetail(pubPK);
    	}
    	else
    	{
        	// Creation
        	try
        	{
        		resultStatus = true;
    	    	pubDetail = getPublicationDetail(publiParams, language);
    	    	createPublication(pubDetail);
    	    	
    	    	pubDetail.setInfoId(xmlFormName);
				updatePublication(pubDetail);
				
				pubPK = pubDetail.getPK();
        	}
        	catch (Exception e)
        	{
        		throw new KmeliaRuntimeException("PublicationImport.importPublication()",
        			SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DIMPORTER_PUBLICATION",
        			e);
        	}
    	}
    	
    	// UpdateXMLForm
    	try
    	{ 
			String pubId = pubPK.getId();
			
			PublicationTemplateManager.addDynamicPublicationTemplate(
				componentId + ":" + xmlFormName, xmlFormName + ".xml");
			
			PublicationTemplate pub = PublicationTemplateManager.getPublicationTemplate(
				componentId + ":" + xmlFormName);
			
			RecordSet set = pub.getRecordSet();
			Form form = pub.getUpdateForm();
			
			DataRecord data = set.getRecord(pubId, language);
			if (data == null)
			{
				data = set.getEmptyRecord();
				data.setId(pubId);
				data.setLanguage(language);
			}
			
			PagesContext context = new PagesContext(
				"myForm", "3", language, false, componentId, userId);
			context.setNodeId(topicId);
			context.setObjectId(pubId);
			context.setContentLanguage(language);
			
    		List items = new ArrayList();
    		String[] fieldNames = data.getFieldNames();
    		String fieldName;
    		String fieldValue;
    		for (int i = 0, n = fieldNames.length; i < n; i++) {
    			fieldName = fieldNames[i];
    			fieldValue = (String)formParams.get(fieldName);
    			fieldValue = (fieldValue == null ? "" : fieldValue);
    			items.add(new InternalFileItem(fieldName, fieldValue));
    		}
			
			form.update(items, data, context);
			set.save(data);
			
			updatePublication(pubDetail);
			
			NodePK nodePK = new NodePK(topicId, spaceId, componentId);
			kmeliaBm.draftOutPublication(pubPK, nodePK, userProfile);
		}
    	catch (Exception e)
    	{
    		throw new KmeliaRuntimeException("PublicationImport.importPublication()",
    			SilverpeasRuntimeException.ERROR, "kmelia.EX_IMPOSSIBLE_DIMPORTER_PUBLICATION", e);
    	}
    	
    	return resultStatus;
	}
    
    /**
     * @param parameters The parameters defining the publication.
     * @param language The language used to create the publication.
     * @return A publication detail containing the parameters given as parameters.
     * @throws Exception
     */
	private PublicationDetail getPublicationDetail(Map parameters, String language)
		throws Exception
	{
		String id = (String)parameters.get("PubId");
	    String status = (String)parameters.get("Status");
		String name = (String)parameters.get("Name");
	    String description = (String)parameters.get("Description");
	    String keywords = (String)parameters.get("Keywords");
	    String beginDate = (String)parameters.get("BeginDate");
	    String endDate = (String)parameters.get("EndDate");
	    String version = (String)parameters.get("Version");
	    String importance = (String)parameters.get("Importance");
	    String beginHour = (String)parameters.get("BeginHour");
	    String endHour = (String)parameters.get("EndHour");
	    String author = (String)parameters.get("Author");
	    String validatorId = (String)parameters.get("ValideurId");
	    String tempId = (String)parameters.get("TempId");
	    String infoId = (String)parameters.get("InfoId");
	    
	    Date jBeginDate = null;
	    Date jEndDate = null;
	    
	    if (beginDate != null && !beginDate.trim().equals(""))
	    {
	    	jBeginDate = DateUtil.stringToDate(beginDate, language);
	    }
	    if (endDate != null && !endDate.trim().equals(""))
	    {
	    	jEndDate = DateUtil.stringToDate(endDate, language);
	    }
	    
	    String pubId = (StringUtil.isDefined(id) ? id : "X");
	    PublicationDetail pubDetail = new PublicationDetail(pubId, name, description, null,
	    	jBeginDate, jEndDate, null, importance, version, keywords, "", null, author);
        pubDetail.setBeginHour(beginHour);
        pubDetail.setEndHour(endHour);
        pubDetail.setStatus(status);
        
        if (StringUtil.isDefined(validatorId))
        {
        	pubDetail.setTargetValidatorId(validatorId);
        }
        
        pubDetail.setCloneId(tempId);
        
        if (StringUtil.isDefined(infoId))
        {
        	pubDetail.setInfoId(infoId);
        }
        
        return pubDetail;
	}
	
	/**
	 * Creates the publication described by the detail given as a parameter.
	 * 
	 * @param pubDetail The publication detail.
	 * @return The id of the newly created publication.
	 * @throws RemoteException
	 */
	private String createPublication(PublicationDetail pubDetail)
    	throws RemoteException
    {	
		pubDetail.getPK().setSpace(spaceId);
	    pubDetail.getPK().setComponentName(componentId);
	    pubDetail.setCreatorId(userId);
	    pubDetail.setCreationDate(new Date());
		    
	    NodePK nodePK = new NodePK(topicId, spaceId, componentId);
	    String result = kmeliaBm.createPublicationIntoTopic(pubDetail, nodePK);
        SilverTrace.info("kmelia", "PublicationImport.createPublication()",
        	"Kmelia.MSG_ENTRY_METHOD");
        return result;
    }
    
	/**
	 * Updates the publication detail given as a parameter.
	 * 
	 * @param pubDetail The publication detail.
	 * @throws RemoteException
	 */
	private void updatePublication(PublicationDetail pubDetail)
		throws RemoteException
	{
		pubDetail.getPK().setSpace(spaceId);
	    pubDetail.getPK().setComponentName(componentId);
	    pubDetail.setUpdaterId(userId);
	    pubDetail.setIndexOperation(IndexManager.NONE);
	    
	    kmeliaBm.updatePublication(pubDetail);
    }
	
	/**
	 * @param xmlFormName The name of the XML form describing the publication.
	 * @param fieldName The name of the field searched into the form.
	 * @param fieldValue The value of the field searched into the form.
	 * @return The id of the publication corresponding to the XML form name and containing a field
	 *         named fieldName and valued to fieldValue. Returns null if no publication is found.
	 */
	private String getPublicationId(String xmlFormName, String fieldName, String fieldValue)
	{
		QueryDescription query = new QueryDescription("*");
		query.setSearchingUser(userId);
		query.addSpaceComponentPair(spaceId, componentId);
		
		Hashtable newXmlQuery = new Hashtable();
		newXmlQuery.put(xmlFormName + "$$" + fieldName, fieldValue);
		query.setXmlQuery(newXmlQuery);
		
		try
		{
			SearchEngineBm searchEngineBm = kmeliaBm.getSearchEngineBm();
			searchEngineBm.search(query);
			MatchingIndexEntry[] result = searchEngineBm.getRange(
				0, searchEngineBm.getResultLength());
			MatchingIndexEntry	mie;
			for (int i = 0; i < result.length; i++)
			{
				mie = result[i];
				if ("Publication".equals(mie.getObjectType()))
				{
					return mie.getPK().getObjectId();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * @param name The name of the topic.
	 * @param description The description of the topic.
	 * @return The id of the newly created topic.
	 */
	public String createTopic(String name, String description)
	{
		NodeDetail topic = new NodeDetail("-1", name, description, null, null, null, "0", "X");
        topic.getNodePK().setSpace(spaceId);
        topic.getNodePK().setComponentName(componentId);
        topic.setCreatorId(userId);
        
        NodePK fatherPK = new NodePK(topicId, spaceId, componentId);
        
        String alertType = "None";
        NodePK nodePK = kmeliaBm.addSubTopic(fatherPK, topic, alertType);
        return nodePK.getId();
	}
	
}