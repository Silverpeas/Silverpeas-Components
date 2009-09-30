package com.stratelia.webactiv.servlets;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import com.silverpeas.peasUtil.RssServlet;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.forums.forumsException.ForumsRuntimeException;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBM;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBMHome;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ForumsRssServlet extends RssServlet {

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.peasUtil.RssServlet#getListElements(java.lang.String,
   * int)
   */
  public Collection getListElements(String instanceId, int nbReturned)
      throws RemoteException {
    // récupération de la liste des 15 derniers messages des forums
    Collection events = getForumsBM().getLastMessageRSS(instanceId, nbReturned);

    SilverTrace.debug("forums", "ForumsRssServlet.getListElements()",
        "root.MSG_GEN_PARAM_VALUE", "events = " + events);

    return events;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.peasUtil.RssServlet#getElementTitle(java.lang.Object,
   * java.lang.String)
   */
  public String getElementTitle(Object element, String userId) {
    Vector message = (Vector) element;

    SilverTrace.debug("forums", "ForumsRssServlet.getElementTitle()",
        "root.MSG_GEN_PARAM_VALUE", "message.elementAt(1) = "
            + message.elementAt(1));

    return (String) message.elementAt(1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.peasUtil.RssServlet#getElementLink(java.lang.Object,
   * java.lang.String)
   */
  public String getElementLink(Object element, String userId) {
    Vector message = (Vector) element;
    String messageUrl = URLManager.getApplicationURL() + "/ForumsMessage/"
        + (String) message.elementAt(0) + "?ForumId="
        + (String) message.elementAt(4);

    SilverTrace.debug("forums", "ForumsRssServlet.getElementLink()",
        "root.MSG_GEN_PARAM_VALUE", "messageUrl = " + messageUrl);

    return messageUrl;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.peasUtil.RssServlet#getElementDescription(java.lang.Object,
   * java.lang.String)
   */
  public String getElementDescription(Object element, String userId) {
    Vector message = (Vector) element;

    SilverTrace.debug("forums", "ForumsRssServlet.getElementDescription()",
        "root.MSG_GEN_PARAM_VALUE", "message.elementAt(6) = "
            + message.elementAt(1));

    return (String) message.elementAt(6);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.peasUtil.RssServlet#getElementDate(java.lang.Object)
   */
  public Date getElementDate(Object element) {
    Vector message = (Vector) element;
    Date messageCreationDate = new Date(Long.parseLong((String) message
        .elementAt(3)));
    /*
     * Calendar calElement = GregorianCalendar.getInstance();
     * calElement.setTime(messageCreationDate);
     * calElement.add(Calendar.HOUR_OF_DAY, 0); //-1 car bug d'affichage du fil
     * RSS qui affiche toujours 1h en trop
     */

    SilverTrace.debug("forums", "ForumsRssServlet.getElementDate()",
        "root.MSG_GEN_PARAM_VALUE", "messageCreationDate = "
            + messageCreationDate);

    return messageCreationDate;
  }

  public String getElementCreatorId(Object element) {
    Vector message = (Vector) element;
    return (String) message.elementAt(2);
  }

  private ForumsBM getForumsBM() {
    ForumsBM forumsBM = null;
    try {
      ForumsBMHome forumsBMHome = (ForumsBMHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.FORUMSBM_EJBHOME, ForumsBMHome.class);
      forumsBM = forumsBMHome.create();
    } catch (Exception e) {
      throw new ForumsRuntimeException("RssServlet.getForumsBM()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return forumsBM;
  }
}