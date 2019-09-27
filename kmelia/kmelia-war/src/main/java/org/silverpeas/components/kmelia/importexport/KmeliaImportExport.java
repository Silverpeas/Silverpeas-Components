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
package org.silverpeas.components.kmelia.importexport;

import org.silverpeas.components.kmelia.KmeliaException;
import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.publication.model.CompletePublication;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.importexport.control.GEDImportExport;
import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.report.MassiveReport;
import org.silverpeas.core.importexport.report.UnitReport;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Date;

import static org.silverpeas.core.contribution.publication.model.PublicationDetail.*;

/**
 * Classe métier de création d'entités silverpeas utilisée par le moteur d'importExport.
 *
 * @author sDevolder.
 */
public class KmeliaImportExport extends GEDImportExport {

  private static final String NODE_ERR_MSG = "importExport.EX_NODE_CREATE";

  /**
   * Constructeur public de la classe
   *
   * @param curentUserDetail informations sur l'utilisateur faisant appel au moteur d'importExport
   * @param currentComponentId - composant silverpeas cible
   */
  public KmeliaImportExport(UserDetail curentUserDetail, String currentComponentId) {
    super(curentUserDetail, currentComponentId);
  }

  /**
   * @return KmeliaService service
   * @throws ImportExportException
   */
  protected KmeliaService getKmeliaService() {
    return ServiceProvider.getService(KmeliaService.class);
  }

  @Override
  protected void updatePublication(PublicationDetail pubDetTemp,
      PublicationDetail pubDetailToCreate, UserDetail userDetail) throws Exception {
    // Ces tests sont utiles dans le cas d'une publication à mettre à jour par id
    if (StringUtil.isDefined(pubDetailToCreate.getName())) {
      pubDetTemp.setName(pubDetailToCreate.getName());
    }
    if (StringUtil.isDefined(pubDetailToCreate.getDescription())) {
      pubDetTemp.setDescription(pubDetailToCreate.getDescription());
    }
    if (StringUtil.isDefined(pubDetailToCreate.getVersion())) {
      pubDetTemp.setVersion(pubDetailToCreate.getVersion());
    }
    if (StringUtil.isDefined(pubDetailToCreate.getKeywords())) {
      pubDetTemp.setKeywords(pubDetailToCreate.getKeywords());
    }
    if (StringUtil.isDefined(pubDetailToCreate.getStatus())) {
      pubDetTemp.setStatusMustBeChecked(false);
      pubDetTemp.setStatus(pubDetailToCreate.getStatus());
    }

    pubDetTemp.setUpdateDate(new Date());
    pubDetTemp.setUpdaterId(userDetail.getId());
    getKmeliaService().updatePublication(pubDetTemp);
  }

  @Override
  protected String createPublicationIntoTopic(PublicationDetail pubDetTemp, NodePK topicPK,
      UserDetail userDetail) throws Exception {
    OrganizationController orgnaisationController = OrganizationControllerProvider
        .getOrganisationController();
    if (pubDetTemp.isStatusMustBeChecked()) {
      final String profile;
      if ("yes".equalsIgnoreCase(orgnaisationController.getComponentParameterValue(topicPK
          .getInstanceId(), "rightsOnTopics"))) {
        NodeDetail topic = getNodeService().getHeader(topicPK);
        if (topic.haveRights()) {
          profile = KmeliaHelper.getProfile(orgnaisationController.getUserProfiles(userDetail.getId(),
              topicPK.getInstanceId(), ProfiledObjectId.fromNode(topic.getRightsDependsOn())));
        } else {
          profile = KmeliaHelper.getProfile(orgnaisationController.getUserProfiles(userDetail
              .getId(), topicPK.getInstanceId()));
        }
      } else {
        profile = KmeliaHelper.getProfile(orgnaisationController.getUserProfiles(userDetail.getId(),
            topicPK.getInstanceId()));
      }
      if ("publisher".equals(profile) || "admin".equals(profile)) {
        pubDetTemp.setStatus(VALID_STATUS);
      } else {
        pubDetTemp.setStatus(TO_VALIDATE_STATUS);
      }
    }
    return getKmeliaService().createPublicationIntoTopic(pubDetTemp, topicPK);
  }

  @Override
  protected void addPublicationToTopic(PublicationPK pubPK, NodePK topicPK) throws Exception {
    getKmeliaService().addPublicationToTopic(pubPK, topicPK, false);
  }

  /**
   * Méthode ajoutant un thème à un thème déja existant. Si le thème à ajouter existe lui aussi (par
   * exemple avec un même ID), il n'est pas modifié et la méthode ne fait rien et ne lève aucune
   * exception.
   *
   * @param nodeDetail le détail du noeud à ajouter.
   * @param topicId l'identifiant du noeud parent, ou 0 pour désigner le noeud racine.
   * @param unitReport le rapport d'import unitaire.
   * @return un objet clé primaire du nouveau thème créé ou du thème déjà existant (thème de même
   * identifiant non modifié).
   * @throws ImportExportException en cas d'anomalie lors de la création du noeud.
   *
   * GEDImportExport#addSubTopicToTopic(NodeDetail,
   * int, UnitReport)
   */
  @Override
  protected NodePK addSubTopicToTopic(NodeDetail nodeDetail, int topicId, UnitReport unitReport)
      throws ImportExportException {
    try {
      final NodePK nodePk = nodeDetail.getNodePK();
      nodePk.setComponentName(getCurrentComponentId());
      nodeDetail.setCreatorId(getCurrentUserDetail().getId());

      // Recherche si le noeud existe déjà, on s'arrête si c'est le cas
      final NodeDetail header = getNodeDetail(nodePk);
      if (header != null) {
        return header.getNodePK();
      }
      // S'il n'existe pas encore, on le crée et on le configure
      final NodePK parentTopicPk = new NodePK(Integer.toString(topicId), getCurrentComponentId());
      final NodePK newTopicPk = getKmeliaService().addSubTopic(parentTopicPk, nodeDetail, "None");
      if (Integer.parseInt(newTopicPk.getId()) < 0) {
        unitReport.setError(UnitReport.ERROR_ERROR);
        SilverLogger.getLogger(this).error("Bad node identifier retrieved");
        throw new ImportExportException("KmeliaImportExport.addSubTopicToTopic", NODE_ERR_MSG);
      }
      unitReport.setStatus(UnitReport.STATUS_TOPIC_CREATED);
      return newTopicPk;
    } catch (Exception ex) {
      unitReport.setError(UnitReport.ERROR_INCORRECT_CLASSIFICATION_ON_COMPONENT);
      unitReport.setStatus(UnitReport.STATUS_PUBLICATION_NOT_CREATED);
      SilverLogger.getLogger(this).error("Node can't be technically created", ex);
      throw new ImportExportException("KmeliaImportExport.addSubTopicToTopic", NODE_ERR_MSG, ex);
    }
  }

  private NodeDetail getNodeDetail(final NodePK nodePk) {
    NodeDetail header = null;
    try {
      header = getNodeService().getHeader(nodePk);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).silent(ex);
    }
    return header;
  }

  /**
   * Méthode ajoutant un thème à un thème déja existant. Si le thème à ajouter existe lui aussi (par
   * exemple avec un même ID), il n'est pas modifié et la méthode ne fait rien et ne lève aucune
   * exception.
   *
   * @param nodeDetail l'objet node correspondant au thème à créer.
   * @param topicId l'ID du thème dans lequel créer le nouveau thème.
   * @return un objet clé primaire du nouveau thème créé.
   * @throws ImportExportException en cas d'anomalie lors de la création du noeud.
   *
   * GEDImportExport#addSubTopicToTopic(NodeDetail,
   * int, MassiveReport)
   */
  @Override
  protected NodePK addSubTopicToTopic(NodeDetail nodeDetail, int topicId,
      MassiveReport massiveReport) throws ImportExportException {
    final NodeDetail existingNodeDetail = getNodeService()
        .getDetailByNameAndFatherId(new NodePK("unKnown", null, getCurrentComponentId()),
            nodeDetail.getName(), topicId);
    final NodePK nodePK;
    if (existingNodeDetail != null) {
      nodePK = existingNodeDetail.getNodePK();
    } else {
      try {
        // Il n'y a pas de topic, on le crée
        final NodePK topicPK = new NodePK(Integer.toString(topicId), getCurrentComponentId());
        nodeDetail.getNodePK().setComponentName(getCurrentComponentId());
        nodeDetail.setCreatorId(getCurrentUserDetail().getId());
        nodePK = getKmeliaService().addSubTopic(topicPK, nodeDetail, "None");
        massiveReport.addOneTopicCreated();
      } catch (Exception ex) {
        throw new ImportExportException("GEDImportExport.addSubTopicToTopic", NODE_ERR_MSG, ex);
      }
    }
    return nodePK;
  }

  /**
   * Méthode récupérant le silverObjectId d'un objet d'id id
   *
   * @param id - id de la publication
   * @return le silverObjectId de l'objet d'id id
   */
  @Override
  public int getSilverObjectId(String id) {
    return getKmeliaService().getSilverObjectId(new PublicationPK(id, getCurrentComponentId()));
  }

  @Override
  protected CompletePublication getCompletePublication(PublicationPK pk) {
    return getKmeliaService().getCompletePublication(pk);
  }

  @Override
  public void publicationNotClassifiedOnPDC(String pubId) throws Exception {
    try {
      PublicationDetail publication = getKmeliaService().getPublicationDetail(
          new PublicationPK(pubId, getCurrentComponentId()));
      publication.setStatus(DRAFT_STATUS);
      getKmeliaService().updatePublication(publication);
    } catch (Exception e) {
      throw new KmeliaException("GEDImportExport.publicationNotClassifiedOnPDC(String)",
          KmeliaException.ERROR, "importExport.EX_GET_SILVERPEASOBJECTID", "pubId = " + pubId, e);
    }
  }

  /**
   * Specific Kmax: Create publication with no nodeFather
   *
   * @param pubDetail
   * @return pubDetail
   */
  @Override
  protected PublicationDetail createPublication(PublicationDetail pubDetail) throws Exception {
    try {
      pubDetail.setStatus(VALID_STATUS);
      String pubId = getKmeliaService().createKmaxPublication(pubDetail);
      pubDetail.getPK().setId(pubId);
      return pubDetail;
    } catch (Exception re) {
      throw new KmeliaException("GEDImportExport.createPublication()", KmeliaException.ERROR,
          "importExport.EX_PUBLICATION_CREATE", re);
    }
  }
}
