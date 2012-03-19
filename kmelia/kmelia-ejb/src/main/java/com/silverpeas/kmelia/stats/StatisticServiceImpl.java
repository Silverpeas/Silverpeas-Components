package com.silverpeas.kmelia.stats;

import static com.stratelia.webactiv.util.JNDINames.NODEBM_EJBHOME;
import static com.stratelia.webactiv.util.exception.SilverpeasRuntimeException.ERROR;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;

import com.silverpeas.kmelia.model.StatsFilterVO;
import com.silverpeas.kmelia.model.TopicSearchStatsVO;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

@Named("statisticService")
public class StatisticServiceImpl implements StatisticService {

  /**
   * TODO inject all the statistics manager there like statistics
   */
  private PublicationBm publicationBm;
  private NodeBm nodeBm;

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
      SilverTrace.error("kmelia", getClass().getSimpleName(),
          "Error when loading the list of Node publications", e);
    }
    if (publis != null && publis.size() > 0) {
      // Check access for each publication
      
    }

    return publis.size();
  }

  @Override
  public TopicSearchStatsVO getStatisticActivityByPeriod(StatsFilterVO statFilter) {
    // TODO Auto-generated method stub
    return null;
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

}
