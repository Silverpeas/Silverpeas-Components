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
/*
 * Created on 25 janv. 2005
 *
 */
package com.silverpeas.kmelia.importexport;

import java.util.Date;

import com.silverpeas.formTemplate.ejb.FormTemplateBm;
import com.silverpeas.formTemplate.ejb.FormTemplateBmHome;
import com.silverpeas.importExport.control.GEDImportExport;
import com.silverpeas.importExport.report.MassiveReport;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.KmeliaException;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBmHome;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * Classe métier de création d'entités silverpeas utilisée par le moteur d'importExport.
 * @author sDevolder.
 */
public class KmeliaImportExport extends GEDImportExport {

//	Variables
  private static OrganizationController org_Ctrl = new OrganizationController();
  private PublicationBm publicationBm = null;
  private FormTemplateBm formTemplateBm = null;
  private NodeBm nodeBm = null;

  /**
   * Constructeur public de la classe
   * @param userDetail - informations sur l'utilisateur faisant appel au moteur d'importExport
   * @param targetComponentId - composant silverpeas cible
   * @param topicId - topic cible du composant targetComponentId
   */
  public KmeliaImportExport(UserDetail curentUserDetail, String currentComponentId) {
    super(curentUserDetail, currentComponentId);
  }

  /**
   * @return l'EJB PublicationBM
   * @throws ImportExportException
   */
  private PublicationBm getPublicationBm() throws KmeliaException {

    if (publicationBm == null) {
      try {
        PublicationBmHome publicationBmHome =
            (PublicationBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class);
        publicationBm = publicationBmHome.create();
      } catch (Exception e) {
        throw new KmeliaException("KmeliaImportExport.getPublicationBm()", KmeliaException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return publicationBm;
  }

  private FormTemplateBm getFormTemplateBm() throws KmeliaException {

    if (formTemplateBm == null) {
      try {
        FormTemplateBmHome formTemplateBmHome =
            (FormTemplateBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.FORMTEMPLATEBM_EJBHOME, FormTemplateBmHome.class);
        formTemplateBm = formTemplateBmHome.create();
      } catch (Exception e) {
        throw new KmeliaException("GEDImportExport.getPublicationBm()", KmeliaException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return formTemplateBm;
  }

  /**
   * @return l'EJB KmeliaBm
   * @throws ImportExportException
   */
  private KmeliaBm getKmeliaBm() throws Exception {
    KmeliaBm kmeliaBm = null;
    KmeliaBmHome ejbHome = (KmeliaBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME, KmeliaBmHome.class);
    kmeliaBm = ejbHome.create();
    return kmeliaBm;
  }

  /**
   * @return l'EJB NodeBM
   * @throws ImportExportException
   */
  private NodeBm getNodeBm() throws KmeliaException {

    if (nodeBm == null) {
      try {
        NodeBmHome kscEjbHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
        nodeBm = kscEjbHome.create();
      } catch (Exception e) {
        throw new KmeliaException("GEDImportExport.getNodeBm()", KmeliaException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return nodeBm;
  }

  @Override
  protected void updatePublication(PublicationDetail pubDet_temp, PublicationDetail pubDetailToCreate, UserDetail userDetail)
      throws Exception {
    //Ces tests sont utiles dans le cas d'une publication à mettre à jour par id
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
    if (StringUtil.isDefined(pubDetailToCreate.getImage())) {
      pubDet_temp.setImage(pubDetailToCreate.getImage());
    }

    pubDet_temp.setUpdateDate(new Date());
    pubDet_temp.setUpdaterId(userDetail.getId());
    getKmeliaBm().updatePublication(pubDet_temp);
  }

  @Override
  protected String createPublicationIntoTopic(PublicationDetail pubDet_temp, NodePK topicPK, UserDetail userDetail) throws Exception {

    if (pubDet_temp.isStatusMustBeChecked()) {
      String profile = "writer";
      if ("yes".equalsIgnoreCase(org_Ctrl.getComponentParameterValue(topicPK.getInstanceId(), "rightsOnTopics"))) {
        NodeDetail topic = getNodeBm().getHeader(topicPK);
        if (topic.haveRights()) {
          profile = KmeliaHelper.getProfile(org_Ctrl.getUserProfiles(userDetail.getId(), topicPK.getInstanceId(), topic.getRightsDependsOn(), ObjectType.NODE));
        } else {
          profile = KmeliaHelper.getProfile(org_Ctrl.getUserProfiles(userDetail.getId(), topicPK.getInstanceId()));
        }
      } else {
        profile = KmeliaHelper.getProfile(org_Ctrl.getUserProfiles(userDetail.getId(), topicPK.getInstanceId()));
      }
      if ("publisher".equals(profile) || "admin".equals(profile)) {
        pubDet_temp.setStatus(PublicationDetail.VALID);
      } else {
        pubDet_temp.setStatus(PublicationDetail.TO_VALIDATE);
      }
    }
    return getKmeliaBm().createPublicationIntoTopic(pubDet_temp, topicPK);
  }

  @Override
  protected void addPublicationToTopic(PublicationPK pubPK, NodePK topicPK) throws Exception {
    getKmeliaBm().addPublicationToTopic(pubPK, topicPK, false);
  }

  /**
   * Méthode ajoutant un thème à un thème déja existant s'il n'existe pas déjà
   * @param nodeDetail - objet node correspondant au thème à créer.
   * @param topicId - id du thème dans lequel créer le nouveau thème
   * @return un objet clef primaire du nouveau thème créé
   * @throws ImportExportException
   */
  @Override
  protected NodePK addSubTopicToTopic(NodeDetail nodeDetail, int topicId, MassiveReport massiveReport) throws Exception {
    NodePK nodePK = null;
    try {
      try {
        //On renvoie le topic déjà existant si c'est le cas
        nodePK = getNodeBm().getDetailByNameAndFatherId(new NodePK("unKnown", null, getCurrentComponentId()), nodeDetail.getName(), topicId).getNodePK();
      } catch (Exception ex) {
        SilverTrace.info("importExport", "GEDImportExport.addSubTopicToTopic()", "root.EX_NO_MESSAGE", ex);
      }
      if (nodePK == null) {
        //Il n'y a pas de topic, on le crée
        NodePK topicPK = new NodePK(Integer.toString(topicId), getCurrentComponentId());
        nodeDetail.getNodePK().setComponentName(getCurrentComponentId());
        nodeDetail.setCreatorId(getCurentUserDetail().getId());
        nodePK = getKmeliaBm().addSubTopic(topicPK, nodeDetail, "None");
        massiveReport.addOneTopicCreated();
      }
    } catch (Exception ex) {
      throw new KmeliaException("GEDImportExport.addSubTopicToTopic", KmeliaException.ERROR, "importExport.EX_NODE_CREATE", ex);
    }
    return nodePK;
  }

  /**
   * Méthode récupérant le silverObjectId d'un objet d'id id
   * @param id - id de la publication
   * @return le silverObjectId de l'objet d'id id
   * @throws ImportExportException
   */
  @Override
  public int getSilverObjectId(String id) throws Exception {
    int silverObjectId = -1;
    silverObjectId = getKmeliaBm().getSilverObjectId(new PublicationPK(id, getCurrentComponentId()));
    return silverObjectId;
  }

  @Override
  protected CompletePublication getCompletePublication(PublicationPK pk) throws Exception {
    return getKmeliaBm().getCompletePublication(pk);
  }

  @Override
  public void publicationNotClassifiedOnPDC(String pubId) throws Exception {
    try {
      PublicationDetail publication = getKmeliaBm().getPublicationDetail(new PublicationPK(pubId, getCurrentComponentId()));
      publication.setStatus("Draft");
      getKmeliaBm().updatePublication(publication);
    } catch (Exception e) {
      throw new KmeliaException("GEDImportExport.publicationNotClassifiedOnPDC(String)", KmeliaException.ERROR, "importExport.EX_GET_SILVERPEASOBJECTID", "pubId = " + pubId, e);
    }
  }

  /**
   * Specific Kmax: Create publication with no nodeFather
   * @param pubDetail
   * @return pubDetail
   */
  @Override
  protected PublicationDetail createPublication(PublicationDetail pubDetail) throws Exception {
    try {
      pubDetail.setStatus("Valid");
      String pubId = getKmeliaBm().createKmaxPublication(pubDetail);
      pubDetail.getPK().setId(pubId);
      return pubDetail;
    } catch (Exception re) {
      throw new KmeliaException("GEDImportExport.createPublication()", KmeliaException.ERROR, "importExport.EX_PUBLICATION_CREATE", re);
    }
  }
}
