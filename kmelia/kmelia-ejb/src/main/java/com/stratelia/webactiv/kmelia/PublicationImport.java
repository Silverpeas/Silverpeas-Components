/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.silverpeas.attachment.importExport.AttachmentImportExport;
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
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBm;
import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.IndexManager;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

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

  public void importPublications(ArrayList publiParamsList,
      ArrayList formParamsList, String language, String xmlFormName,
      String discrimatingParameterName, String userProfile)
      throws RemoteException {
    for (int i = 0, n = publiParamsList.size(); i < n; i++) {
      importPublication((Map) publiParamsList.get(i), (Map) formParamsList
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
   * @param discrimatingParameterName The name of the field included in the form which allowes to
   * retrieve the eventually existing publication to update.
   * @param userProfile The user's profile used to draft out the publication.
   * @return True if the publication is created, false if it is updated.
   * @throws RemoteException
   */
  public boolean importPublication(Map publiParams, Map formParams,
      String language, String xmlFormName, String discrimatingParameterName,
      String userProfile) throws RemoteException {
    PublicationDetail pubDetail = null;

    String publicationToUpdateId = null;
    if (discrimatingParameterName != null
        && discrimatingParameterName.length() > 0) {
      String discrimatingParameterValue = (String) formParams
          .get(discrimatingParameterName);
      publicationToUpdateId = getPublicationId(xmlFormName,
          discrimatingParameterName, discrimatingParameterValue);
    }

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
        updatePublication(pubDetail);
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
        updatePublication(pubDetail);

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

      PublicationTemplateManager.addDynamicPublicationTemplate(componentId
          + ":" + xmlFormName, xmlFormName + ".xml");

      PublicationTemplate pub = PublicationTemplateManager
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

      List items = new ArrayList();
      String[] fieldNames = data.getFieldNames();
      String fieldName;
      String fieldValue;
      for (int i = 0, n = fieldNames.length; i < n; i++) {
        fieldName = fieldNames[i];
        fieldValue = (String) formParams.get(fieldName);
        fieldValue = (fieldValue == null ? "" : fieldValue);
        items.add(new InternalFileItem(fieldName, fieldValue));
      }

      form.update(items, data, context);
      set.save(data);

      updatePublication(pubDetail);

      NodePK nodePK = new NodePK(topicId, spaceId, componentId);
      kmeliaBm.draftOutPublication(pubPK, nodePK, userProfile);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("PublicationImport.importPublication()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DIMPORTER_PUBLICATION", e);
    }

    return resultStatus;
  }

  public List getPublicationXmlFields(String publicationId) {
    PublicationPK pubPK = new PublicationPK(publicationId, spaceId, componentId);
    PublicationDetail pubDetail = kmeliaBm.getPublicationDetail(pubPK);
    List fields = pubDetail.getXmlFields();
    return fields;
  }

  /**
   * @param parameters The parameters defining the publication.
   * @param language The language used to create the publication.
   * @return A publication detail containing the parameters given as parameters.
   * @throws Exception
   */
  private PublicationDetail getPublicationDetail(Map parameters, String language)
      throws Exception {
    String id = (String) parameters.get("PubId");
    String status = (String) parameters.get("Status");
    String name = (String) parameters.get("Name");
    String description = (String) parameters.get("Description");
    String keywords = (String) parameters.get("Keywords");
    String beginDate = (String) parameters.get("BeginDate");
    String endDate = (String) parameters.get("EndDate");
    String version = (String) parameters.get("Version");
    String importance = (String) parameters.get("Importance");
    String beginHour = (String) parameters.get("BeginHour");
    String endHour = (String) parameters.get("EndHour");
    String author = (String) parameters.get("Author");
    String validatorId = (String) parameters.get("ValideurId");
    String tempId = (String) parameters.get("TempId");
    String infoId = (String) parameters.get("InfoId");

    Date jBeginDate = null;
    Date jEndDate = null;

    if (beginDate != null && !beginDate.trim().equals("")) {
      jBeginDate = DateUtil.stringToDate(beginDate, language);
    }
    if (endDate != null && !endDate.trim().equals("")) {
      jEndDate = DateUtil.stringToDate(endDate, language);
    }

    String pubId = (StringUtil.isDefined(id) ? id : "X");
    PublicationDetail pubDetail = new PublicationDetail(pubId, name,
        description, null, jBeginDate, jEndDate, null, importance, version,
        keywords, "", null, author);
    pubDetail.setBeginHour(beginHour);
    pubDetail.setEndHour(endHour);
    pubDetail.setStatus(status);

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
    pubDetail.setCreationDate(new Date());

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
  private void updatePublication(PublicationDetail pubDetail)
      throws RemoteException {
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
   * named fieldName and valued to fieldValue. Returns null if no publication is found.
   */
  private String getPublicationId(String xmlFormName, String fieldName,
      String fieldValue) {
    QueryDescription query = new QueryDescription("*");
    query.setSearchingUser(userId);
    query.addSpaceComponentPair(spaceId, componentId);

    Hashtable newXmlQuery = new Hashtable();
    newXmlQuery.put(xmlFormName + "$$" + fieldName, fieldValue);
    query.setXmlQuery(newXmlQuery);

    try {
      SearchEngineBm searchEngineBm = kmeliaBm.getSearchEngineBm();
      searchEngineBm.search(query);
      MatchingIndexEntry[] result = searchEngineBm.getRange(0, searchEngineBm
          .getResultLength());
      MatchingIndexEntry mie;
      for (int i = 0; i < result.length; i++) {
        mie = result[i];
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

  public Collection getPublicationsSpecificValues(String componentId,
      String xmlFormName, String fieldName) throws RemoteException {
    PublicationBm publicationBm = kmeliaBm.getPublicationBm();
    Collection publications = publicationBm
        .getAllPublications(new PublicationPK("useless", componentId));
    ArrayList result = new ArrayList();
    Iterator iter = publications.iterator();
    PublicationDetail publication = null;
    String fieldValue;
    Collection fatherPKs;
    NodePK fatherPK;
    while (iter.hasNext()) {
      publication = (PublicationDetail) iter.next();
      if (publication.getInfoId().equals(xmlFormName)) {
        fatherPKs = publicationBm.getAllFatherPK(publication.getPK());
        if (!fatherPKs.isEmpty()) {
          fatherPK = (NodePK) fatherPKs.iterator().next();
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
      updatePublication(pubDetail);
    }
  }

  public void importAttachment(String publicationId, String userId, String filePath, String title,
      String info, Date creationDate)
      throws RemoteException {
    importAttachment(publicationId, userId, filePath, title,
        info, creationDate, null);

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
      updateLogicalName = false;
    }

    attachmentIE.importAttachment(publicationId, componentId, attDetail,
        isIndexable);
  }

  /**
   * @param parameters The parameters defining the publication.
   * @param language The language used to create the publication.
   * @return A publication detail containing the parameters given as parameters.
   * @throws Exception
   */
  private void updatePublicationDetail(PublicationDetail pubDetail, Map parameters, String language)
      throws Exception {
    String status = (String) parameters.get("Status");
    String name = (String) parameters.get("Name");
    String description = (String) parameters.get("Description");
    String keywords = (String) parameters.get("Keywords");
    String beginDate = (String) parameters.get("BeginDate");
    String endDate = (String) parameters.get("EndDate");
    String version = (String) parameters.get("Version");
    String importance = (String) parameters.get("Importance");
    String beginHour = (String) parameters.get("BeginHour");
    String endHour = (String) parameters.get("EndHour");
    String author = (String) parameters.get("Author");
    String validatorId = (String) parameters.get("ValideurId");
    String tempId = (String) parameters.get("TempId");
    String infoId = (String) parameters.get("InfoId");

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