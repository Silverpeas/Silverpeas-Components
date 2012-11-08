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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.kmelia.stats;

import static com.stratelia.webactiv.util.JNDINames.NODEBM_EJBHOME;
import static com.stratelia.webactiv.util.exception.SilverpeasRuntimeException.ERROR;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Named;

import com.silverpeas.kmelia.model.StatisticActivityVO;
import com.silverpeas.kmelia.model.StatsFilterVO;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
import com.stratelia.webactiv.util.statistic.control.StatisticBmHome;
import com.stratelia.webactiv.util.statistic.model.StatisticRuntimeException;

@Named("statisticService")
public class StatisticServiceImpl implements StatisticService {

  /**
   * TODO inject all the statistics manager there like statistic
   */
  private PublicationBm publicationBm;
  private NodeBm nodeBm;
  private StatisticBm statisticBm;

  /*
   * @Inject private KmeliaBm kmeliaBm;
   */

  @Override
  public Integer getNbConsultedPublication(StatsFilterVO statFilter) {
    if (statFilter != null) {
      return getNumberOfConsultedPublications(statFilter);
    }
    return -1;
  }

  @Override
  public Integer getNbStatisticActivityByPeriod(StatsFilterVO statFilter) {
    if (statFilter != null) {
      // Retrieve the list of topic application publications
      List<PublicationDetail> publis = getValidApplicationPublications(statFilter);
      if (publis != null && publis.size() > 0) {
        return countGlobalPublicationActivity(statFilter, publis);
      }
      return 0;
    }
    return -1;
  }

  private Integer getNumberOfConsultedPublications(StatsFilterVO statFilter) {
    List<PublicationDetail> publis = getValidApplicationPublications(statFilter);
    int nbPubli = 0;
    if (publis != null && publis.size() > 0) {
      Integer groupId = statFilter.getGroupId();
      List<WAPrimaryKey> publiPKs = getPrimaryKeysFromPublis(publis);
      if (groupId != null) {
        // Retrieve the list of user identifiers
        List<String> userIds = getListUserIdsFromGroup(groupId);
        try {
          // Retrieve the number of publication
          nbPubli =
              getStatisticBm().getCountByPeriodAndUser(publiPKs, "Publication",
                  statFilter.getStartDate(), statFilter.getEndDate(), userIds);

        } catch (RemoteException e) {
          SilverTrace.error("kmelia", getClass().getSimpleName() + ".getNbConsultedPublication",
              "Error when counting number of access (getCountByPeriodAndUser)", e);
        }

      } else {
        try {
          nbPubli =
              getStatisticBm().getCountByPeriod(publiPKs, 1, "Publication",
                  statFilter.getStartDate(), statFilter.getEndDate());
        } catch (RemoteException e) {
          SilverTrace.error("kmelia", getClass().getSimpleName() + ".getNbConsultedPublication",
              "Error when counting number of access (getCountByPeriod)", e);
        }

      }
    }
    return nbPubli;
  }

  /**
   * @param publis the list of publication detail where we extract WAPrimaryKey
   * @return
   */
  private List<WAPrimaryKey> getPrimaryKeysFromPublis(List<PublicationDetail> publis) {
    List<WAPrimaryKey> publiPKs = new ArrayList<WAPrimaryKey>();
    // Check access for each publication
    for (PublicationDetail publi : publis) {
      publiPKs.add(publi.getPK());
    }
    return publiPKs;
  }

  /**
   * @param groupId the group identifier
   * @return the list of user identifiers which are linked to a group given in parameter, or empty
   * list if an exception occurs
   */
  private List<String> getListUserIdsFromGroup(Integer groupId) {
    List<String> userIds = new ArrayList<String>();
    try {
      Admin admin = AdminReference.getAdminService();
      Group selectedGroup = admin.getGroup(Integer.toString(groupId));
      String[] arrayUserIds = selectedGroup.getUserIds();
      for (String userId : arrayUserIds) {
        userIds.add(userId);
      }
    } catch (AdminException e) {
      SilverTrace.error("kmelia", getClass().getSimpleName() + ".getStatisticActivityByPeriod",
          "Error when loading the list of filtered users", e);
    }
    return userIds;
  }

  /**
   * @param statFilter the stats filter value object
   * @return the list of application publications which respects the stats filter constraint
   */
  private List<PublicationDetail> getValidApplicationPublications(StatsFilterVO statFilter) {
    NodePK fatherPK =
        new NodePK(Integer.toString(statFilter.getTopicId()), statFilter.getInstanceId());
    Collection<PublicationDetail> validPubli = null;
    List<PublicationDetail> publis = new ArrayList<PublicationDetail>();
    try {
      List<NodeDetail> nodes = getNodeBm().getSubTree(fatherPK);
      if (nodes != null) {
        ArrayList<String> fatherIds = new ArrayList<String>();
        for (NodeDetail node : nodes) {
          fatherIds.add(Integer.toString(node.getId()));
        }
        validPubli =
            getPublicationBm().getDetailsByFatherIdsAndStatus(fatherIds,
                new PublicationPK("", statFilter.getInstanceId()), null, "Valid");
        publis.addAll(validPubli);
      }
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", getClass().getSimpleName() + ".getStatisticActivityByPeriod",
          "Error when loading the list of Node publications", e);
    }
    return publis;
  }

  /**
   * @param statFilter the statistic filter object which contains all the statistic constraints
   * @param publis the list of PublicationDetail
   * @param isCreate true if counting create publication activity
   * @param isUpdate true if counting update publication activity
   * @return the number of global (create/modify) activity which happens on the list of publications
   */
  private int countPublicationActivity(StatsFilterVO statFilter,
      Collection<PublicationDetail> publis, boolean isCreate, boolean isUpdate) {
    int nbPubli = 0;
    Date startTime = statFilter.getStartDate();
    Date endTime = statFilter.getEndDate();
    Integer groupId = statFilter.getGroupId();

    if (groupId != null) {
      List<String> userIds = getListUserIdsFromGroup(groupId);
      if (!userIds.isEmpty()) {
        for (PublicationDetail publi : publis) {
          for (String userId : userIds) {
            if (isPubliActivityInsideTimeInterval(startTime, endTime, publi, isCreate, isUpdate) &&
                isUserRelatedWithPubli(publi, userId)) {
              nbPubli++;
            }
          }
        }
      }
    } else {
      // Check activity for each publication
      for (PublicationDetail publi : publis) {
        if (isPubliActivityInsideTimeInterval(startTime, endTime, publi, isCreate, isUpdate)) {
          nbPubli++;
        }
      }
    }
    return nbPubli;
  }

  /**
   * @param publi the publication detail
   * @param userId the user identifier
   * @return true if user has created, modified or validate this publication
   */
  private boolean isUserRelatedWithPubli(PublicationDetail publi, String userId) {
    return (userId.equals(publi.getCreatorId()) || userId.equals(publi.getUpdaterId()) || userId
        .equals(publi.getValidatorId()));
  }

  /**
   * @param startTime the start time interval
   * @param endTime the end time interval
   * @param publi the publication detail
   * @param isCreate true if check Create activity
   * @param isUpdate true if check Update activity
   * @return true if publication creation date and isCreate or modification date and isUpdate is
   * between startTime and endTime
   */
  private boolean isPubliActivityInsideTimeInterval(Date startTime, Date endTime,
      PublicationDetail publi, boolean isCreate, boolean isUpdate) {
    Date createDate = publi.getCreationDate();
    Date updateDate = publi.getUpdateDate();
    return (isCreate &&
        (createDate.after(startTime) || createDate.equals(startTime)) && createDate.before(endTime)) ||
        (isUpdate && (updateDate.after(startTime) || updateDate.equals(startTime)) && updateDate
            .before(endTime));
  }

  private PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        PublicationBmHome publicationBmHome = EJBUtilitaire.getEJBObjectRef(
            JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class);
        this.publicationBm = publicationBmHome.create();
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaSecurity.getPublicationBm()",
            SilverpeasRuntimeException.ERROR,
            "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_PUBLICATIONBM_HOME",
            e);
      }
    }
    return publicationBm;
  }

  private NodeBm getNodeBm() {
    if (nodeBm == null) {
      try {
        NodeBmHome nodeBmHome = EJBUtilitaire.getEJBObjectRef(NODEBM_EJBHOME, NodeBmHome.class);
        this.nodeBm = nodeBmHome.create();
      } catch (Exception e) {
        throw new KmeliaRuntimeException("KmeliaBmEJB.getNodeBm()", ERROR,
            "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
      }
    }
    return nodeBm;
  }

  private StatisticBm getStatisticBm() {
    if (statisticBm == null) {
      try {
        StatisticBmHome statisticHome =
            EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME,
                StatisticBmHome.class);
        this.statisticBm = statisticHome.create();
        return statisticBm;
      } catch (Exception e) {
        throw new StatisticRuntimeException("PdcSearchSessionController.getStatisticBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return statisticBm;
  }

  @Override
  public StatisticActivityVO getStatisticActivity(StatsFilterVO statFilter) {
    List<PublicationDetail> publis = getValidApplicationPublications(statFilter);
    int nbCreate = countCreatePublicationActivity(statFilter, publis);
    int nbUpdate = countUpdatePublicationActivity(statFilter, publis);
    StatisticActivityVO statActivity = new StatisticActivityVO(nbCreate, nbUpdate);
    return statActivity;
  }

  /**
   * @param statFilter the statistic filter object which contains all the statistic constraints
   * @param publis the list of PublicationDetail
   * @return the number of global (create/modify) activity which happens on the list of publications
   */
  private int countCreatePublicationActivity(StatsFilterVO statFilter,
      List<PublicationDetail> publis) {
    return countPublicationActivity(statFilter, publis, true, false);
  }

  /**
   * @param statFilter the statistic filter object which contains all the statistic constraints
   * @param publis the list of PublicationDetail
   * @return the number of global (create/modify) activity which happens on the list of publications
   */
  private int countUpdatePublicationActivity(StatsFilterVO statFilter,
      List<PublicationDetail> publis) {
    return countPublicationActivity(statFilter, publis, false, true);
  }

  /**
   * @param statFilter the statistic filter object which contains all the statistic constraints
   * @param publis the list of PublicationDetail
   * @return the number of global (create/modify) activity which happens on the list of publications
   */
  private int countGlobalPublicationActivity(StatsFilterVO statFilter,
      List<PublicationDetail> publis) {
    return countPublicationActivity(statFilter, publis, true, true);
  }

  @Override
  public Integer getNumberOfDifferentConsultedPublications(StatsFilterVO statFilter) {
    if (statFilter != null) {
      List<PublicationDetail> publis = getValidApplicationPublications(statFilter);
      if (publis != null && publis.size() > 0) {
        return countDistinctConsultedPublications(statFilter, publis);
      }
    }
    return -1;
  }

  /**
   * @param statFilter the statistic filter which contains all the statistics filter parameters
   * @param publis the list of publications
   * @return the number of distinct consulted publications
   */
  private Integer countDistinctConsultedPublications(StatsFilterVO statFilter,
      List<PublicationDetail> publis) {
    int nbPubli = 0;
    List<WAPrimaryKey> publiPKs = getPrimaryKeysFromPublis(publis);
    if (statFilter.getGroupId() != null) {
      // Retrieve the list of user identifiers
      List<String> userIds = getListUserIdsFromGroup(statFilter.getGroupId());
      try {
        return getStatisticBm().getDistinctCountByPeriodUser(publiPKs, 1, "Publication",
            statFilter.getStartDate(), statFilter.getEndDate(), userIds);
      } catch (RemoteException e) {
        SilverTrace.error("kmelia", getClass().getSimpleName() + ".getNumberOfDifferentConsu...",
            "Error when computing distinct access to publication", e);
      }
    } else {
      try {
        return getStatisticBm().getDistinctCountByPeriod(publiPKs, 1, "Publication",
            statFilter.getStartDate(), statFilter.getEndDate());
      } catch (RemoteException e) {
        SilverTrace.error("kmelia", getClass().getSimpleName() + ".getNumberOfDifferentConsu...",
            "Error when computing distinct access to publication", e);
      }
    }
    return nbPubli;
  }

}
