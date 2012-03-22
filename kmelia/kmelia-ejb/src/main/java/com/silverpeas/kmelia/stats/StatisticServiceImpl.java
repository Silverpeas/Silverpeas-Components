package com.silverpeas.kmelia.stats;

import static com.stratelia.webactiv.util.JNDINames.NODEBM_EJBHOME;
import static com.stratelia.webactiv.util.exception.SilverpeasRuntimeException.ERROR;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Named;

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
    NodePK fatherPK =
        new NodePK(Integer.toString(statFilter.getTopicId()), statFilter.getInstanceId());
    Collection<PublicationDetail> publis = new ArrayList<PublicationDetail>();
    try {
      List<NodeDetail> nodes = getNodeBm().getSubTree(fatherPK);
      if (nodes != null) {
        ArrayList<String> fatherIds = new ArrayList<String>();
        for (NodeDetail node : nodes) {
          fatherIds.add(Integer.toString(node.getId()));
        }
        publis =
            getPublicationBm().getDetailsByFatherIds(fatherIds,
                new PublicationPK("", statFilter.getInstanceId()));
      }
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", getClass().getSimpleName() + ".getNbConsultedPublication",
          "Error when loading the list of Node publications", e);
    }
    int nbPubli = 0;
    if (publis != null && publis.size() > 0) {
      Integer groupId = statFilter.getGroupId();
      List<WAPrimaryKey> publiPKs = new ArrayList<WAPrimaryKey>();
      // Check access for each publication
      for (PublicationDetail publi : publis) {
        publiPKs.add(publi.getPK());
      }
      if (groupId != null) {
        // Retrieve the group and the list of user identifiers
        try {
          List<String> userIds = getListUserIds(groupId);
          nbPubli =
              getStatisticBm().getCountByPeriodAndUser(publiPKs, "Publication",
                  statFilter.getStartDate(), statFilter.getEndDate(), userIds);

        } catch (AdminException e) {
          SilverTrace.error("kmelia", getClass().getSimpleName() + ".getNbConsultedPublication",
              "Error when getting group before counting access groupId=" + groupId, e);
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
   * @param groupId the group identifier
   * @return the list of user identifiers which are linked to a group given in parameter
   * @throws AdminException
   */
  private List<String> getListUserIds(Integer groupId) throws AdminException {
    Admin admin = AdminReference.getAdminService();
    Group selectedGroup = admin.getGroup(Integer.toString(groupId));
    String[] arrayUserIds = selectedGroup.getUserIds();
    List<String> userIds = new ArrayList<String>();
    for (String userId : arrayUserIds) {
      userIds.add(userId);
    }
    return userIds;
  }

  @Override
  public Integer getStatisticActivityByPeriod(StatsFilterVO statFilter) {
    NodePK fatherPK =
        new NodePK(Integer.toString(statFilter.getTopicId()), statFilter.getInstanceId());
    Collection<PublicationDetail> publis = new ArrayList<PublicationDetail>();
    try {
      List<NodeDetail> nodes = getNodeBm().getSubTree(fatherPK);
      if (nodes != null) {
        ArrayList<String> fatherIds = new ArrayList<String>();
        for (NodeDetail node : nodes) {
          fatherIds.add(Integer.toString(node.getId()));
        }
        publis =
            getPublicationBm().getDetailsByFatherIds(fatherIds,
                new PublicationPK("", statFilter.getInstanceId()));
      }
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", getClass().getSimpleName() + ".getStatisticActivityByPeriod",
          "Error when loading the list of Node publications", e);
    }
    int nbPubli = 0;
    if (publis != null && publis.size() > 0) {
      Date startTime = statFilter.getStartDate();
      Date endTime = statFilter.getEndDate();
      Integer groupId = statFilter.getGroupId();

      if (groupId != null) {
        List<String> userIds = new ArrayList<String>();
        try {
          userIds = getListUserIds(groupId);
        } catch (AdminException e) {
          SilverTrace.error("kmelia", getClass().getSimpleName() + ".getStatisticActivityByPeriod",
              "Error when loading the list of filtered users", e);
        }
        if (!userIds.isEmpty()) {
          for (PublicationDetail publi : publis) {
            for (String userId : userIds) {
              if (isPubliActivityInsideTimeInterval(startTime, endTime, publi) &&
                  isUserRelatedWithPubli(publi, userId)) {
                nbPubli++;
              }
            }
          }
        }
      } else {
        // Check activity for each publication
        for (PublicationDetail publi : publis) {
          if (isPubliActivityInsideTimeInterval(startTime, endTime, publi)) {
            nbPubli++;
          }
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
   * @return true if publication creation date or modification date is between startTime and endTime
   */
  private boolean isPubliActivityInsideTimeInterval(Date startTime, Date endTime,
      PublicationDetail publi) {
    return (publi.getCreationDate().after(startTime) && publi.getCreationDate().before(endTime)) ||
        (publi.getUpdateDate().after(startTime) && publi.getUpdateDate().before(endTime));
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

}
