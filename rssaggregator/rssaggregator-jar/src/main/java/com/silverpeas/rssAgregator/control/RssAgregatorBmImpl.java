/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.rssAgregator.control;

import java.util.List;

import javax.inject.Named;

import com.silverpeas.rssAgregator.model.RssAgregatorException;
import com.silverpeas.rssAgregator.model.SPChannel;
import com.silverpeas.rssAgregator.model.SPChannelPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import org.silverpeas.util.WAPrimaryKey;
import org.silverpeas.util.exception.SilverpeasException;

/**
 * @author neysseri
 */
@Named("rssAgregatorBm")
public class RssAgregatorBmImpl implements RssAgregatorBm {

  private static SilverpeasBeanDAO<SPChannel> rssDAO;

  public RssAgregatorBmImpl() {
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.rssAgregator.control.RssAgregatorBm#addChannel(com.silverpeas
   * .rssAgregator.model.SPChannel)
   */
  public SPChannel addChannel(SPChannel channel) throws RssAgregatorException {
    try {
      WAPrimaryKey pk = getDAO().add(channel);
      channel.setPK(pk);
    } catch (PersistenceException pe) {
      throw new RssAgregatorException("RssAgregatorBmImpl.addChannel()", SilverpeasException.ERROR,
          "rssAgregator.ADDING_CHANNEL_FAILED", pe);
    }
    return channel;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.rssAgregator.control.RssAgregatorBm#deleteChannel(com.silverpeas
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
   * @see com.silverpeas.rssAgregator.control.RssAgregatorBm#getChannels(java.lang .String)
   */
  public List<SPChannel> getChannels(String instanceId) throws RssAgregatorException {
    List<SPChannel> channels = null;
    try {
      SPChannelPK pk = new SPChannelPK("useless", instanceId);
      channels = (List<SPChannel>) getDAO().findByWhereClause(pk,
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
          SilverpeasException.ERROR, "rssAgregator.DELETING_CHANNELS_FAILED", pe);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.rssAgregator.control.RssAgregatorBm#updateChannel(com.silverpeas
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

  private SilverpeasBeanDAO<SPChannel> getDAO() throws RssAgregatorException {
    if (rssDAO == null) {
      try {
        rssDAO = SilverpeasBeanDAOFactory.getDAO("com.silverpeas.rssAgregator.model.SPChannel");
      } catch (PersistenceException pe) {
        throw new RssAgregatorException("RssAgregatorBmImpl.getDAO()", SilverpeasException.ERROR,
            "rssAgregator.GETTING_SILVERPEASBEANDAO_FAILED", pe);
      }
    }
    return rssDAO;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.rssAgregator.control.RssAgregatorBm#getChannel(com.silverpeas
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
