/*
 * Created on 20 août 2004
 *
 */
package com.silverpeas.rssAgregator.control;

import java.util.List;

import com.silverpeas.rssAgregator.model.RssAgregatorException;
import com.silverpeas.rssAgregator.model.SPChannel;
import com.silverpeas.rssAgregator.model.SPChannelPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * @author neysseri
 * 
 */
public class RssAgregatorBmImpl implements RssAgregatorBm {

  private static SilverpeasBeanDAO rssDAO;

  public RssAgregatorBmImpl() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.rssAgregator.control.RssAgregatorBm#addChannel(com.silverpeas
   * .rssAgregator.model.SPChannel)
   */
  public SPChannel addChannel(SPChannel channel) throws RssAgregatorException {
    try {
      WAPrimaryKey pk = getDAO().add(channel);
      channel.setPK(pk);
    } catch (PersistenceException pe) {
      throw new RssAgregatorException("RssAgregatorBmImpl.addChannel()",
          SilverpeasException.ERROR, "rssAgregator.ADDING_CHANNEL_FAILED", pe);
    }
    return channel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.rssAgregator.control.RssAgregatorBm#deleteChannel(com.silverpeas
   * .rssAgregator.model.SPChannelPK)
   */
  public void deleteChannel(SPChannelPK channelPK) throws RssAgregatorException {
    try {
      getDAO().remove(channelPK);
    } catch (PersistenceException pe) {
      throw new RssAgregatorException("RssAgregatorBmImpl.addChannel()",
          SilverpeasException.ERROR, "rssAgregator.DELETING_CHANNEL_FAILED", pe);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.rssAgregator.control.RssAgregatorBm#getChannels(java.lang
   * .String)
   */
  public List getChannels(String instanceId) throws RssAgregatorException {
    List channels = null;
    try {
      SPChannelPK pk = new SPChannelPK("useless", instanceId);
      channels = (List) getDAO().findByWhereClause(pk,
          "instanceId = '" + instanceId + "' ORDER BY id");
    } catch (PersistenceException pe) {
      throw new RssAgregatorException("RssAgregatorBmImpl.getChannels()",
          SilverpeasException.ERROR, "rssAgregator.GETTING_CHANNELS_FAILED", pe);
    }
    return channels;
  }

  public void deleteChannels(String instanceId) throws RssAgregatorException {
    try {
      SPChannelPK pk = new SPChannelPK("useless", instanceId);
      getDAO().removeWhere(pk, "instanceId = '" + instanceId + "'");
    } catch (PersistenceException pe) {
      throw new RssAgregatorException("RssAgregatorBmImpl.deleteChannels()",
          SilverpeasException.ERROR, "rssAgregator.DELETING_CHANNELS_FAILED",
          pe);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.rssAgregator.control.RssAgregatorBm#updateChannel(com.silverpeas
   * .rssAgregator.model.SPChannel)
   */
  public void updateChannel(SPChannel channel) throws RssAgregatorException {
    try {
      getDAO().update(channel);
    } catch (PersistenceException pe) {
      throw new RssAgregatorException("RssAgregatorBmImpl.addChannel()",
          SilverpeasException.ERROR, "rssAgregator.UPDATING_CHANNEL_FAILED", pe);
    }
  }

  private SilverpeasBeanDAO getDAO() throws RssAgregatorException {
    if (rssDAO == null) {
      try {
        rssDAO = SilverpeasBeanDAOFactory
            .getDAO("com.silverpeas.rssAgregator.model.SPChannel");
      } catch (PersistenceException pe) {
        throw new RssAgregatorException("RssAgregatorBmImpl.getDAO()",
            SilverpeasException.ERROR,
            "rssAgregator.GETTING_SILVERPEASBEANDAO_FAILED", pe);
      }
    }
    return rssDAO;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.rssAgregator.control.RssAgregatorBm#getChannel(com.silverpeas
   * .rssAgregator.model.SPChannelPK)
   */
  public SPChannel getChannel(SPChannelPK channelPK)
      throws RssAgregatorException {
    SPChannel channel = null;
    try {
      channel = (SPChannel) getDAO().findByPrimaryKey(channelPK);
    } catch (PersistenceException pe) {
      throw new RssAgregatorException("RssAgregatorBmImpl.getChannel()",
          SilverpeasException.ERROR, "rssAgregator.GETTING_CHANNEL_FAILED", pe);
    }
    return channel;
  }

}
