/*
 * Copyright (C) 2000 - 2019 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.XMLField;
import org.silverpeas.core.contribution.content.form.fileitem.InternalFileItem;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.index.search.SearchEngineProvider;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.ParseException;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PublicationImport {

  private KmeliaService kmeliaService;
  private String componentId;
  private String topicId;
  private String spaceId;
  private String userId;
  private boolean ignoreMissingFormFields = false;
  private SilverLogger logger = SilverLogger.getLogger(this);

  public PublicationImport(KmeliaService kmeliaService, String componentId,
      String topicId, String spaceId, String userId) {
    this.kmeliaService = kmeliaService;
    this.componentId = componentId;
    this.topicId = topicId;
    this.spaceId = spaceId;
    this.userId = userId;
  }

  public PublicationImport(KmeliaService kmeliaService, String componentId) {
    this.kmeliaService = kmeliaService;
    this.componentId = componentId;
  }

  public void importPublications(List<Map<String, String>> publiParamsList,
      List<Map<String, String>> formParamsList, String language, String xmlFormName,
      String discrimatingParameterName, String userProfile) {
    for (int i = 0; i < publiParamsList.size(); i++) {
      importPublication(publiParamsList.get(i), formParamsList.get(i), language, xmlFormName,
          discrimatingParameterName, userProfile);
    }
  }

  /**
   * Creates or updates a publication.
   *
   * @param publiParams The parameters of the publication.
   * @param formParams The parameters of the publication's form.
   * @param language The language of the publication.
   * @param xmlFormName The name of the publication's form.
   * @param discrimatingParameterName The name of the field included in the form which allows to
   * retrieve the eventually existing publication to update.
   * @param userProfile The user's profile used to draft out the publication.
   * @return True if the publication is created, false if it is updated.
   */
  public boolean importPublication(Map<String, String> publiParams, Map<String, String> formParams,
      String language, String xmlFormName, String discrimatingParameterName, String userProfile) {
    String publicationToUpdateId = null;
    if (discrimatingParameterName != null && discrimatingParameterName.length() > 0) {
      String discrimatingParameterValue = formParams.get(discrimatingParameterName);
      publicationToUpdateId = getPublicationId(xmlFormName, discrimatingParameterName,
          discrimatingParameterValue);
    }
    return importPublication(publicationToUpdateId, publiParams, formParams, language, xmlFormName,
        userProfile);
  }

  /**
   * Creates or updates a publication.
   *
   * @param publicationToUpdateId The id of the publication to update.
   * @param publiParams The parameters of the publication.
   * @param formParams The parameters of the publication's form.
   * @param language The language of the publication.
   * @param xmlFormName The name of the publication's form.
   * @param userProfile The user's profile used to draft out the publication.
   * @return True if the publication is created, false if it is updated.
   */
  public boolean importPublication(String publicationToUpdateId, Map<String, String> publiParams,
      Map<String, String> formParams, String language, String xmlFormName, String userProfile) {
    PublicationDetail pubDetail = null;
    boolean resultStatus;
    PublicationPK pubPK;
    if (publicationToUpdateId != null) {
      // Update
      try {
        resultStatus = false;
        pubPK = new PublicationPK(publicationToUpdateId, spaceId, componentId);
        pubDetail = kmeliaService.getPublicationDetail(pubPK);
        updatePublicationDetail(pubDetail, publiParams, language);
        updatePublication(pubDetail, true);
      } catch (Exception e) {
        throw new KmeliaRuntimeException(e);
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
        throw new KmeliaRuntimeException(e);
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
      if (ignoreMissingFormFields) {
        context.setUpdatePolicy(PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES);
      }
      List< FileItem> items = new ArrayList<>();
      for (String fieldName : data.getFieldNames()) {
        String fieldValue = formParams.get(fieldName);
        fieldValue = (fieldValue == null ? "" : fieldValue);
        items.add(new InternalFileItem(fieldName, fieldValue));
      }
      form.update(items, data, context);
      set.save(data);
      updatePublication(pubDetail, true);
      NodePK nodePK = new NodePK(topicId, spaceId, componentId);
      kmeliaService.draftOutPublication(pubPK, nodePK, userProfile, true);
    } catch (Exception e) {
      throw new KmeliaRuntimeException(e);
    }

    return resultStatus;
  }

  public List<XMLField> getPublicationXmlFields(String publicationId) {
    return getPublicationXmlFields(publicationId, null);
  }

  public List<XMLField> getPublicationXmlFields(String publicationId, String language) {
    PublicationPK pubPK = new PublicationPK(publicationId, spaceId, componentId);
    PublicationDetail pubDetail = kmeliaService.getPublicationDetail(pubPK);
    return pubDetail.getXmlFields(language);
  }

  /**
   * @param parameters The parameters defining the publication.
   * @param language The language used to create the publication.
   * @return A publication detail containing the parameters given as parameters.
   * @throws Exception
   */
  private PublicationDetail getPublicationDetail(Map<String, String> parameters, String language)
      throws java.text.ParseException {
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

    if (!StringUtil.isInteger(importance)) {
      importance = "5";
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
   *
   * @param pubDetail The publication detail.
   * @return The id of the newly created publication.
   */
  private String createPublication(PublicationDetail pubDetail) {
    pubDetail.getPK().setSpace(spaceId);
    pubDetail.getPK().setComponentName(componentId);
    pubDetail.setCreatorId(userId);
    if (pubDetail.getCreationDate() == null) {
      pubDetail.setCreationDate(new Date());
    }

    NodePK nodePK = new NodePK(topicId, spaceId, componentId);
    return kmeliaService.createPublicationIntoTopic(pubDetail, nodePK);
  }

  /**
   * Updates the publication detail given as a parameter.
   *
   * @param pubDetail The publication detail.
   */
  private void updatePublication(PublicationDetail pubDetail, boolean forceUpdateDate) {
    pubDetail.getPK().setSpace(spaceId);
    pubDetail.getPK().setComponentName(componentId);
    pubDetail.setUpdaterId(userId);
    pubDetail.setIndexOperation(IndexManager.NONE);
    kmeliaService.updatePublication(pubDetail, forceUpdateDate);
  }

  /**
   * @param xmlFormName The name of the XML form describing the publication.
   * @param fieldName The name of the field searched into the form.
   * @param fieldValue The value of the field searched into the form.
   * @return The id of the publication corresponding to the XML form name and containing a field
   * named fieldName and valued to fieldValue. Returns null if no publication is found.
   */
  public String getPublicationId(String xmlFormName, String fieldName, String fieldValue) {
    QueryDescription query = new QueryDescription("*");
    query.setSearchingUser(userId);
    query.addComponent(componentId);

    query.addFieldQuery(new FieldDescription(xmlFormName + "$$" + fieldName, fieldValue, null));

    try {
      List<MatchingIndexEntry> result = SearchEngineProvider.getSearchEngine().search(query).
          getEntries();
      for (MatchingIndexEntry mie : result) {
        if ("Publication".equals(mie.getObjectType())) {
          return mie.getPK().getObjectId();
        }
      }
    } catch (ParseException e) {
      logger.error(e.getMessage(), e);
    }
    return null;
  }

  /**
   * @param name The name of the topic.
   * @param description The description of the topic.
   * @return The id of the newly created topic.
   */
  public String createTopic(String name, String description) {
    NodeDetail topic = new NodeDetail("-1", name, description, 0, "X");
    topic.getNodePK().setSpace(spaceId);
    topic.getNodePK().setComponentName(componentId);
    topic.setCreatorId(userId);

    NodePK fatherPK = new NodePK(topicId, spaceId, componentId);
    String alertType = "None";
    NodePK nodePK = kmeliaService.addSubTopic(fatherPK, topic, alertType);

    return nodePK.getId();
  }

  public Collection<String> getPublicationsSpecificValues(String componentId, String xmlFormName,
      String fieldName) {
    PublicationService publicationService = getPublicationService();
    Collection<PublicationDetail> publications = publicationService.getAllPublications(componentId);
    List<String> result = new ArrayList<>();
    Iterator<PublicationDetail> iter = publications.iterator();
    while (iter.hasNext()) {
      PublicationDetail publication = iter.next();
      if (publication.getInfoId().equals(xmlFormName)) {
        Collection<NodePK> fatherPKs = publicationService.getAllFatherPKInSamePublicationComponentInstance(publication.getPK());
        if (!fatherPKs.isEmpty()) {
          NodePK fatherPK = fatherPKs.iterator().next();
          if (!fatherPK.isTrash()) {
            String fieldValue = publication.getFieldValue(fieldName);
            if (StringUtil.isDefined(fieldValue)) {
              result.add(fieldValue);
            }
          }
        }
      }
    }
    return result;
  }

  public void draftInPublication(String xmlFormName, String fieldName, String fieldValue) {
    String publicationId = getPublicationId(xmlFormName, fieldName, fieldValue);
    if (publicationId != null) {
      PublicationPK publicationPK = new PublicationPK(publicationId, componentId);
      kmeliaService.draftInPublication(publicationPK);
    }
  }

  public void updatePublicationEndDate(String xmlFormName, String fieldName, String fieldValue,
      Date endDate) {
    String publicationToUpdateId = getPublicationId(xmlFormName, fieldName, fieldValue);
    PublicationPK publicationPK = new PublicationPK(publicationToUpdateId, spaceId, componentId);
    PublicationDetail pubDetail = kmeliaService.getPublicationDetail(publicationPK);
    Date publicationEndDate = pubDetail.getEndDate();
    if (publicationEndDate == null || publicationEndDate.after(endDate)) {
      pubDetail.setEndDate(endDate);
      updatePublication(pubDetail, false);
    }
  }

  /**
   * @param parameters The parameters defining the publication.
   * @param language The language used to create the publication.
   * @return A publication detail containing the parameters given as parameters.
   * @throws Exception
   */
  private void updatePublicationDetail(PublicationDetail pubDetail, Map<String, String> parameters,
      String language) throws java.text.ParseException {
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
    String updateDate = parameters.get("UpdateDate");

    if (StringUtil.isDefined(updateDate)) {
      pubDetail.setUpdateDate(DateUtil.stringToDate(updateDate, language));
    }

    if (StringUtil.isDefined(beginDate)) {
      pubDetail.setBeginDate(DateUtil.stringToDate(beginDate, language));
    }
    if (StringUtil.isDefined(endDate)) {
      pubDetail.setEndDate(DateUtil.stringToDate(endDate, language));
    }

    if (name != null) {
      pubDetail.setName(name);
    }

    if (description != null) {
      pubDetail.setDescription(description);
    }

    if (StringUtil.isInteger(importance)) {
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

  public void setIgnoreMissingFormFields(boolean ignore) {
    ignoreMissingFormFields = ignore;
  }

  private PublicationService getPublicationService() {
    return ServiceProvider.getService(PublicationService.class);
  }
}
