/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.kmelia;

import com.silverpeas.attachment.importExport.AttachmentImportExport;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.fileitem.InternalFileItem;
import com.silverpeas.form.importExport.XMLField;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBmEJB;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.search.searchEngine.model.MatchingIndexEntry;
import org.silverpeas.search.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import org.silverpeas.search.indexEngine.model.IndexManager;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.silverpeas.search.SearchEngineFactory;

public class PublicationImport {

  private KmeliaBmEJB kmeliaBm;
  private String componentId;
  private String topicId;
  private String spaceId;
  private String userId;

  public PublicationImport(KmeliaBmEJB kmeliaBm, String componentId,
      String topicId, String spaceId, String userId) {
    this.kmeliaBm = kmeliaBm;
    this.componentId = componentId;
    this.topicId = topicId;
    this.spaceId = spaceId;
    this.userId = userId;
  }

  public PublicationImport(KmeliaBmEJB kmeliaBm, String componentId) {
    this.kmeliaBm = kmeliaBm;
    this.componentId = componentId;
  }

  public void importPublications(List<Map<String, String>> publiParamsList,
      List<Map<String, String>> formParamsList, String language, String xmlFormName,
      String discrimatingParameterName, String userProfile)
      throws RemoteException {
    for (int i = 0, n = publiParamsList.size(); i < n; i++) {
      importPublication(publiParamsList.get(i), formParamsList
          .get(i), language, xmlFormName, discrimatingParameterName,
          userProfile);
    }
  }

  /**
   * Creates or updates a publication.
   * @param publiParams The parameters of the publication.
   * @param formParams The parameters of the publication's form.
   * @param language The language of the publication.
   * @param xmlFormName The name of the publication's form.
   * @param discrimatingParameterName The name of the field included in the form which allows to
   * retrieve the eventually existing publication to update.
   * @param userProfile The user's profile used to draft out the publication.
   * @return True if the publication is created, false if it is updated.
   * @throws RemoteException
   */
  public boolean importPublication(Map<String, String> publiParams, Map<String, String> formParams,
      String language, String xmlFormName, String discrimatingParameterName, String userProfile)
      throws RemoteException {
    String publicationToUpdateId = null;
    if (discrimatingParameterName != null && discrimatingParameterName.length() > 0) {
      String discrimatingParameterValue = formParams.get(discrimatingParameterName);
      publicationToUpdateId = getPublicationId(
        xmlFormName, discrimatingParameterName, discrimatingParameterValue);
    }
    
    return importPublication(
      publicationToUpdateId, publiParams, formParams, language, xmlFormName, userProfile);
  }
  
  /**
   * Creates or updates a publication.
   * @param publicationToUpdateId The id of the publication to update.
   * @param publiParams The parameters of the publication.
   * @param formParams The parameters of the publication's form.
   * @param language The language of the publication.
   * @param xmlFormName The name of the publication's form.
   * @param userProfile The user's profile used to draft out the publication.
   * @return True if the publication is created, false if it is updated.
   * @throws RemoteException
   */
  public boolean importPublication(String publicationToUpdateId, Map<String, String> publiParams,
      Map<String, String> formParams, String language, String xmlFormName, String userProfile)
      throws RemoteException {
    PublicationDetail pubDetail = null;
    boolean resultStatus;
    PublicationPK pubPK;
    if (publicationToUpdateId != null) {
      // Update
      // Update
      try {
        resultStatus = false;
        pubPK = new PublicationPK(publicationToUpdateId, spaceId, componentId);
        pubDetail = kmeliaBm.getPublicationDetail(pubPK);
        updatePublicationDetail(pubDetail, publiParams, language);
        updatePublication(pubDetail, true);
      } catch (Exception e) {
        throw new KmeliaRuntimeException(
            "PublicationImport.importPublication()",
            SilverpeasRuntimeException.ERROR,
            "kmelia.EX_IMPOSSIBLE_DIMPORTER_PUBLICATION", e);
      }
    } else {
      // Creation
      try {
        resultStatus = true;
        pubDetail = getPublicationDetail(publiParams, language);
        createPublication(pubDetail);

        pubDetail.setInfoId(xmlFormName);
        updatePublication(pubDetail, true);

        pubPK = pubDetail.getPK();
      } catch (Exception e) {
        throw new KmeliaRuntimeException(
            "PublicationImport.importPublication()",
            SilverpeasRuntimeException.ERROR,
            "kmelia.EX_IMPOSSIBLE_DIMPORTER_PUBLICATION", e);
      }
    }

    // UpdateXMLForm
    try {
      String pubId = pubPK.getId();

      PublicationTemplateManager publicationTemplateManager = 
              PublicationTemplateManager.getInstance();
      publicationTemplateManager.addDynamicPublicationTemplate(componentId
          + ":" + xmlFormName, xmlFormName + ".xml");

      PublicationTemplate pub = publicationTemplateManager
          .getPublicationTemplate(componentId + ":" + xmlFormName);

      RecordSet set = pub.getRecordSet();
      Form form = pub.getUpdateForm();

      DataRecord data = set.getRecord(pubId, language);
      if (data == null) {
        data = set.getEmptyRecord();
        data.setId(pubId);
        data.setLanguage(language);
      }

      PagesContext context = new PagesContext("myForm", "3", language, false,
          componentId, userId);
      context.setNodeId(topicId);
      context.setObjectId(pubId);
      context.setContentLanguage(language);

      List<FileItem> items = new ArrayList<FileItem>();
      String[] fieldNames = data.getFieldNames();
      String fieldName;
      String fieldValue;
      for (int i = 0, n = fieldNames.length; i < n; i++) {
        fieldName = fieldNames[i];
        fieldValue = formParams.get(fieldName);
        fieldValue = (fieldValue == null ? "" : fieldValue);
        items.add(new InternalFileItem(fieldName, fieldValue));
      }

      form.update(items, data, context);
      set.save(data);

      updatePublication(pubDetail, true);

      NodePK nodePK = new NodePK(topicId, spaceId, componentId);
      kmeliaBm.draftOutPublication(pubPK, nodePK, userProfile, true);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("PublicationImport.importPublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DIMPORTER_PUBLICATION", e);
    }

    return resultStatus;
  }

  public List<XMLField> getPublicationXmlFields(String publicationId) {
    return getPublicationXmlFields(publicationId, null);
  }
  
  public List<XMLField> getPublicationXmlFields(String publicationId, String language) {
    PublicationPK pubPK = new PublicationPK(publicationId, spaceId, componentId);
    PublicationDetail pubDetail = kmeliaBm.getPublicationDetail(pubPK);
    return pubDetail.getXmlFields(language);
  }

  /**
   * @param parameters The parameters defining the publication.
   * @param language The language used to create the publication.
   * @return A publication detail containing the parameters given as parameters.
   * @throws Exception
   */
  private PublicationDetail getPublicationDetail(Map<String, String> parameters, String language)
      throws Exception {
    String id = parameters.get("PubId");
    String status = parameters.get("Status");
    String name = parameters.get("Name");
    String description = parameters.get("Description");
    String keywords = parameters.get("Keywords");
    String beginDate = parameters.get("BeginDate");
    String endDate = parameters.get("EndDate");
    String version = parameters.get("Version");
    String importance = parameters.get("Importance");
    String beginHour = parameters.get("BeginHour");
    String endHour = parameters.get("EndHour");
    String author = parameters.get("Author");
    String validatorId = parameters.get("ValideurId");
    String tempId = parameters.get("TempId");
    String infoId = parameters.get("InfoId");
    String creationDate = parameters.get("CreationDate");
    String updateDate = parameters.get("UpdateDate");

    Date jBeginDate = null;
    Date jEndDate = null;
    Date jCreationDate = null;
    Date jUpdateDate = null;

    if (StringUtil.isDefined(beginDate)) {
      jBeginDate = DateUtil.stringToDate(beginDate, language);
    }
    if (StringUtil.isDefined(endDate)) {
      jEndDate = DateUtil.stringToDate(endDate, language);
    }
    if (StringUtil.isDefined(creationDate)) {
      jCreationDate = DateUtil.stringToDate(creationDate, language);
    }
    if (StringUtil.isDefined(updateDate)) {
      jUpdateDate = DateUtil.stringToDate(updateDate, language);
    }

    String pubId = (StringUtil.isDefined(id) ? id : "X");
    PublicationDetail pubDetail = new PublicationDetail(pubId, name,
        description, jCreationDate, jBeginDate, jEndDate, null, importance, version,
        keywords, "", status, "", author);
    pubDetail.setBeginHour(beginHour);
    pubDetail.setEndHour(endHour);
    pubDetail.setUpdateDate(jUpdateDate);

    if (StringUtil.isDefined(validatorId)) {
      pubDetail.setTargetValidatorId(validatorId);
    }

    pubDetail.setCloneId(tempId);

    if (StringUtil.isDefined(infoId)) {
      pubDetail.setInfoId(infoId);
    }

    return pubDetail;
  }

  /**
   * Creates the publication described by the detail given as a parameter.
   * @param pubDetail The publication detail.
   * @return The id of the newly created publication.
   * @throws RemoteException
   */
  private String createPublication(PublicationDetail pubDetail)
      throws RemoteException {
    pubDetail.getPK().setSpace(spaceId);
    pubDetail.getPK().setComponentName(componentId);
    pubDetail.setCreatorId(userId);
    if (pubDetail.getCreationDate() == null) {
      pubDetail.setCreationDate(new Date());
    }

    NodePK nodePK = new NodePK(topicId, spaceId, componentId);
    String result = kmeliaBm.createPublicationIntoTopic(pubDetail, nodePK);
    SilverTrace.info("kmelia", "PublicationImport.createPublication()",
        "Kmelia.MSG_ENTRY_METHOD");
    return result;
  }

  /**
   * Updates the publication detail given as a parameter.
   * @param pubDetail The publication detail.
   * @throws RemoteException
   */
  private void updatePublication(PublicationDetail pubDetail, boolean forceUpdateDate)
      throws RemoteException {
    pubDetail.getPK().setSpace(spaceId);
    pubDetail.getPK().setComponentName(componentId);
    pubDetail.setUpdaterId(userId);
    pubDetail.setIndexOperation(IndexManager.NONE);

    kmeliaBm.updatePublication(pubDetail, forceUpdateDate);
  }

  /**
   * @param xmlFormName The name of the XML form describing the publication.
   * @param fieldName The name of the field searched into the form.
   * @param fieldValue The value of the field searched into the form.
   * @return The id of the publication corresponding to the XML form name and containing a field
   * named fieldName and valued to fieldValue. Returns null if no publication is found.
   */
  public String getPublicationId(String xmlFormName, String fieldName,
      String fieldValue) {
    QueryDescription query = new QueryDescription("*");
    query.setSearchingUser(userId);
    query.addSpaceComponentPair(spaceId, componentId);

    Hashtable<String, String> newXmlQuery = new Hashtable<String, String>();
    newXmlQuery.put(xmlFormName + "$$" + fieldName, fieldValue);
    query.setXmlQuery(newXmlQuery);

    try {
      List<MatchingIndexEntry> result =SearchEngineFactory.getSearchEngine().search(query).getEntries();
      for (MatchingIndexEntry mie : result) {
        if ("Publication".equals(mie.getObjectType())) {
          return mie.getPK().getObjectId();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * @param name The name of the topic.
   * @param description The description of the topic.
   * @return The id of the newly created topic.
   */
  public String createTopic(String name, String description) {
    NodeDetail topic = new NodeDetail("-1", name, description, null, null,
        null, "0", "X");
    topic.getNodePK().setSpace(spaceId);
    topic.getNodePK().setComponentName(componentId);
    topic.setCreatorId(userId);

    NodePK fatherPK = new NodePK(topicId, spaceId, componentId);
    String alertType = "None";
    NodePK nodePK = kmeliaBm.addSubTopic(fatherPK, topic, alertType);

    return nodePK.getId();
  }

  public Collection<String> getPublicationsSpecificValues(String componentId,
      String xmlFormName, String fieldName) throws RemoteException {
    PublicationBm publicationBm = kmeliaBm.getPublicationBm();
    Collection<PublicationDetail> publications = publicationBm
        .getAllPublications(new PublicationPK("useless", componentId));
    List<String> result = new ArrayList<String>();
    Iterator<PublicationDetail> iter = publications.iterator();
    PublicationDetail publication = null;
    String fieldValue;
    Collection<NodePK> fatherPKs;
    NodePK fatherPK;
    while (iter.hasNext()) {
      publication = iter.next();
      if (publication.getInfoId().equals(xmlFormName)) {
        fatherPKs = publicationBm.getAllFatherPK(publication.getPK());
        if (!fatherPKs.isEmpty()) {
          fatherPK = fatherPKs.iterator().next();
          if (!fatherPK.getId().equals("1")) {
            fieldValue = publication.getFieldValue(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
              result.add(fieldValue);
            }
          }
        }
      }
    }
    return result;
  }

  public void draftInPublication(String xmlFormName, String fieldName,
      String fieldValue) throws RemoteException {
    String publicationId = getPublicationId(xmlFormName, fieldName, fieldValue);
    if (publicationId != null) {
      PublicationPK publicationPK = new PublicationPK(publicationId,
          componentId);
      kmeliaBm.draftInPublication(publicationPK);
    }
  }

  public void updatePublicationEndDate(String xmlFormName, String fieldName,
      String fieldValue, Date endDate) throws RemoteException {
    String publicationToUpdateId = getPublicationId(xmlFormName, fieldName,
        fieldValue);
    PublicationPK publicationPK = new PublicationPK(publicationToUpdateId,
        spaceId, componentId);
    PublicationDetail pubDetail = kmeliaBm.getPublicationDetail(publicationPK);
    Date publicationEndDate = pubDetail.getEndDate();
    if (publicationEndDate == null || publicationEndDate.after(endDate)) {
      pubDetail.setEndDate(endDate);
      updatePublication(pubDetail, false);
    }
  }

  public void importAttachment(String publicationId, String userId, String filePath, String title,
      String info, Date creationDate, String logicalName)
      throws RemoteException {
    AttachmentImportExport attachmentIE = new AttachmentImportExport();
    PublicationPK pubPK = new PublicationPK(publicationId, componentId);
    PublicationDetail pubDetail = kmeliaBm.getPublicationBm().getDetail(pubPK);
    boolean isIndexable = KmeliaHelper.isIndexable(pubDetail);

    AttachmentDetail attDetail = new AttachmentDetail();
    AttachmentPK pk = new AttachmentPK("unknown", "useless", componentId);
    attDetail.setPhysicalName(filePath);
    attDetail.setAuthor(userId);
    attDetail.setPK(pk);
    attDetail.setTitle(title);
    attDetail.setInfo(info);
    attDetail.setCreationDate(creationDate);
    boolean updateLogicalName = true;
    if (logicalName != null) {
      // force
      attDetail.setLogicalName(logicalName);
      updateLogicalName=false;
    }
    InputStream in = null;
    try {
      in  = new BufferedInputStream(new FileInputStream(filePath));
      attachmentIE.importAttachment(publicationId, componentId, attDetail, in, isIndexable,
          updateLogicalName);
    } catch (FileNotFoundException e) {
      throw new RemoteException("ImportAttachment", e);
    } finally {
      IOUtils.closeQuietly(in);
    }

  }

  /**
   * @param parameters The parameters defining the publication.
   * @param language The language used to create the publication.
   * @return A publication detail containing the parameters given as parameters.
   * @throws Exception
   */
  private void updatePublicationDetail(PublicationDetail pubDetail, Map<String, String> parameters,
      String language)
      throws Exception {
    String status = parameters.get("Status");
    String name = parameters.get("Name");
    String description = parameters.get("Description");
    String keywords = parameters.get("Keywords");
    String beginDate = parameters.get("BeginDate");
    String endDate = parameters.get("EndDate");
    String version = parameters.get("Version");
    String importance = parameters.get("Importance");
    String beginHour = parameters.get("BeginHour");
    String endHour = parameters.get("EndHour");
    String author = parameters.get("Author");
    String validatorId = parameters.get("ValideurId");
    String tempId = parameters.get("TempId");
    String infoId = parameters.get("InfoId");

    Date jBeginDate = null;
    Date jEndDate = null;

    if (beginDate != null && !beginDate.trim().equals("")) {
      jBeginDate = DateUtil.stringToDate(beginDate, language);
    }
    if (endDate != null && !endDate.trim().equals("")) {
      jEndDate = DateUtil.stringToDate(endDate, language);
    }

    if (name != null) {
      pubDetail.setName(name);
    }

    if (description != null) {
      pubDetail.setDescription(description);
    }

    pubDetail.setBeginDate(jBeginDate);
    pubDetail.setEndDate(jEndDate);
    if ((StringUtil.isInteger(importance))) {
      pubDetail.setImportance(Integer.parseInt(importance));
    }

    if (version != null) {
      pubDetail.setVersion(version);
    }

    if (keywords != null) {
      pubDetail.setKeywords(keywords);
    }

    if (author != null) {
      pubDetail.setAuthor(author);
    }

    pubDetail.setBeginHour(beginHour);
    pubDetail.setEndHour(endHour);

    if (status != null) {
      pubDetail.setStatus(status);
    }

    if (StringUtil.isDefined(validatorId)) {
      pubDetail.setTargetValidatorId(validatorId);
    }

    pubDetail.setCloneId(tempId);

    if (StringUtil.isDefined(infoId)) {
      pubDetail.setInfoId(infoId);
    }
  }

}
