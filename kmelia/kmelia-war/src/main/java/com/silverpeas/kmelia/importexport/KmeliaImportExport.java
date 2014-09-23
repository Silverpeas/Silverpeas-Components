/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.kmelia.importexport;

import java.util.Date;

import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;

import com.silverpeas.importExport.control.GEDImportExport;
import com.silverpeas.importExport.model.ImportExportException;
import com.silverpeas.importExport.report.MassiveReport;
import com.silverpeas.importExport.report.UnitReport;
import com.silverpeas.util.StringUtil;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.KmeliaException;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.publication.model.CompletePublication;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;

import static com.stratelia.webactiv.publication.model.PublicationDetail.*;

/**
 * Classe métier de création d'entités silverpeas utilisée par le moteur d'importExport.
 *
 * @author sDevolder.
 */
public class KmeliaImportExport extends GEDImportExport {

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
   * @return l'EJB KmeliaBm
   * @throws ImportExportException
   */
  protected KmeliaBm getKmeliaBm() throws ImportExportException {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME, KmeliaBm.class);
  }

  @Override
  protected void updatePublication(PublicationDetail pubDet_temp,
      PublicationDetail pubDetailToCreate, UserDetail userDetail) throws Exception {
    // Ces tests sont utiles dans le cas d'une publication à mettre à jour par id
    if (StringUtil.isDefined(pubDetailToCreate.getName())) {
      pubDet_temp.setName(pubDetailToCreate.getName());
    }
    if (StringUtil.isDefined(pubDetailToCreate.getDescription())) {
      pubDet_temp.setDescription(pubDetailToCreate.getDescription());
    }
    if (StringUtil.isDefined(pubDetailToCreate.getVersion())) {
      pubDet_temp.setVersion(pubDetailToCreate.getVersion());
    }
    if (StringUtil.isDefined(pubDetailToCreate.getKeywords())) {
      pubDet_temp.setKeywords(pubDetailToCreate.getKeywords());
    }
    if (StringUtil.isDefined(pubDetailToCreate.getStatus())) {
      pubDet_temp.setStatusMustBeChecked(false);
      pubDet_temp.setStatus(pubDetailToCreate.getStatus());
    }

    pubDet_temp.setUpdateDate(new Date());
    pubDet_temp.setUpdaterId(userDetail.getId());
    getKmeliaBm().updatePublication(pubDet_temp);
  }

  @Override
  protected String createPublicationIntoTopic(PublicationDetail pubDet_temp, NodePK topicPK,
      UserDetail userDetail) throws Exception {
    OrganisationController orgnaisationController = OrganisationControllerFactory
        .getOrganisationController();
    if (pubDet_temp.isStatusMustBeChecked()) {
      String profile = "writer";
      if ("yes".equalsIgnoreCase(orgnaisationController.getComponentParameterValue(topicPK
          .getInstanceId(), "rightsOnTopics"))) {
        NodeDetail topic = getNodeBm().getHeader(topicPK);
        if (topic.haveRights()) {
          profile = KmeliaHelper.getProfile(orgnaisationController.getUserProfiles(userDetail
              .getId(), topicPK.getInstanceId(), topic.getRightsDependsOn(), ObjectType.NODE));
        } else {
          profile = KmeliaHelper.getProfile(orgnaisationController.getUserProfiles(userDetail
              .getId(), topicPK.getInstanceId()));
        }
      } else {
        profile = KmeliaHelper.getProfile(orgnaisationController.getUserProfiles(userDetail.getId(),
            topicPK.getInstanceId()));
      }
      if ("publisher".equals(profile) || "admin".equals(profile)) {
        pubDet_temp.setStatus(VALID);
      } else {
        pubDet_temp.setStatus(TO_VALIDATE);
      }
    }
    return getKmeliaBm().createPublicationIntoTopic(pubDet_temp, topicPK);
  }

  @Override
  protected void addPublicationToTopic(PublicationPK pubPK, NodePK topicPK) throws Exception {
    getKmeliaBm().addPublicationToTopic(pubPK, topicPK, false);
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
   * @see
   * com.silverpeas.importExport.control.GEDImportExport#addSubTopicToTopic(com.stratelia.webactiv.node.model.NodeDetail,
   * int, com.silverpeas.importExport.report.UnitReport)
   */
  @Override
  protected NodePK addSubTopicToTopic(NodeDetail nodeDetail, int topicId, UnitReport unitReport)
      throws ImportExportException {
    try {
      NodePK nodePk = nodeDetail.getNodePK();
      nodePk.setComponentName(getCurrentComponentId());
      nodeDetail.setCreatorId(getCurentUserDetail().getId());

      // Recherche si le noeud existe déjà, on s'arrête si c'est le cas
      try {
        NodeDetail header = getNodeBm().getHeader(nodePk);
        if (header != null) {
          return header.getNodePK();
        }
      } catch (Exception ex) {
        SilverTrace.debug("importExport", "KmeliaImportExport.addSubTopicToTopic()",
            "root.EX_NO_MESSAGE", ex);
      }
      // S'il n'existe pas encore, on le crée et on le configure
      NodePK parentTopicPk = new NodePK(Integer.toString(topicId), getCurrentComponentId());
      NodePK newTopicPk = getKmeliaBm().addSubTopic(parentTopicPk, nodeDetail, "None");
      if (Integer.parseInt(newTopicPk.getId()) < 0) {
        unitReport.setError(UnitReport.ERROR_ERROR);
        SilverTrace.error("importExport", "KmeliaImportExport.addSubTopicToTopic()",
            "root.EX_NO_MESSAGE");
        throw new ImportExportException("KmeliaImportExport.addSubTopicToTopic",
            "importExport.EX_NODE_CREATE");
      }
      unitReport.setStatus(UnitReport.STATUS_TOPIC_CREATED);
      return newTopicPk;
    } catch (Exception ex) {
      unitReport.setError(UnitReport.ERROR_INCORRECT_CLASSIFICATION_ON_COMPONENT);
      unitReport.setStatus(UnitReport.STATUS_PUBLICATION_NOT_CREATED);
      SilverTrace.error("importExport", "KmeliaImportExport.addSubTopicToTopic()",
          "root.EX_NO_MESSAGE", ex);
      throw new ImportExportException("KmeliaImportExport.addSubTopicToTopic",
          "importExport.EX_NODE_CREATE", ex);
    }
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
   * @see
   * com.silverpeas.importExport.control.GEDImportExport#addSubTopicToTopic(com.stratelia.webactiv.node.model.NodeDetail,
   * int, com.silverpeas.importExport.report.MassiveReport)
   */
  @Override
  protected NodePK addSubTopicToTopic(NodeDetail nodeDetail, int topicId,
      MassiveReport massiveReport) throws ImportExportException {
    NodePK nodePK = null;
    try {
      // On renvoie le topic déjà existant si c'est le cas
      nodePK = getNodeBm().getDetailByNameAndFatherId(new NodePK("unKnown", null,
          getCurrentComponentId()), nodeDetail.getName(), topicId).getNodePK();
    } catch (Exception ex) {
      SilverTrace.info("importExport", "GEDImportExport.addSubTopicToTopic()",
          "root.EX_NO_MESSAGE", ex);
    }
    if (nodePK == null) {
      try {
        // Il n'y a pas de topic, on le crée
        NodePK topicPK = new NodePK(Integer.toString(topicId), getCurrentComponentId());
        nodeDetail.getNodePK().setComponentName(getCurrentComponentId());
        nodeDetail.setCreatorId(getCurentUserDetail().getId());
        nodePK = getKmeliaBm().addSubTopic(topicPK, nodeDetail, "None");
        massiveReport.addOneTopicCreated();

      } catch (Exception ex) {
        throw new ImportExportException("GEDImportExport.addSubTopicToTopic",
            "importExport.EX_NODE_CREATE", ex);
      }
    }

    return nodePK;
  }

  /**
   * Méthode récupérant le silverObjectId d'un objet d'id id
   *
   * @param id - id de la publication
   * @return le silverObjectId de l'objet d'id id
   * @throws ImportExportException
   */
  @Override
  public int getSilverObjectId(String id) throws Exception {
    return getKmeliaBm().getSilverObjectId(new PublicationPK(id, getCurrentComponentId()));
  }

  @Override
  protected CompletePublication getCompletePublication(PublicationPK pk) throws Exception {
    return getKmeliaBm().getCompletePublication(pk);
  }

  @Override
  public void publicationNotClassifiedOnPDC(String pubId) throws Exception {
    try {
      PublicationDetail publication = getKmeliaBm().getPublicationDetail(
          new PublicationPK(pubId, getCurrentComponentId()));
      publication.setStatus(DRAFT);
      getKmeliaBm().updatePublication(publication);
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
      pubDetail.setStatus(VALID);
      String pubId = getKmeliaBm().createKmaxPublication(pubDetail);
      pubDetail.getPK().setId(pubId);
      return pubDetail;
    } catch (Exception re) {
      throw new KmeliaException("GEDImportExport.createPublication()", KmeliaException.ERROR,
          "importExport.EX_PUBLICATION_CREATE", re);
    }
  }
}
