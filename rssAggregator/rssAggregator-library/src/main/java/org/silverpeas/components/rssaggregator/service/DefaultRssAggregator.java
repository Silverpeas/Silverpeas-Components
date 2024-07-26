/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.rssaggregator.service;

import org.silverpeas.components.rssaggregator.model.RssAgregatorException;
import org.silverpeas.components.rssaggregator.model.SPChannel;
import org.silverpeas.components.rssaggregator.model.SPChannelPK;
import org.silverpeas.core.SilverpeasExceptionMessages;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.persistence.jdbc.bean.BeanCriteria;
import org.silverpeas.core.persistence.jdbc.bean.PersistenceException;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory;

import java.util.List;

/**
 * @author neysseri
 */
@Service
@SuppressWarnings("deprecation")
public class DefaultRssAggregator implements RssAggregator {

  private static final String SYNDICATION_CHANNEL_WITH_ID = "syndication channel with id";
  private SilverpeasBeanDAO<SPChannel> rssDAO;

  public SPChannel addChannel(SPChannel channel) throws RssAgregatorException {
    try {
      WAPrimaryKey pk = getDAO().add(channel);
      channel.setPK(pk);
    } catch (PersistenceException e) {
      throw new RssAgregatorException(
          SilverpeasExceptionMessages.failureOnAdding("syndication channel at", channel.getUrl()),
          e);
    }
    return channel;
  }

  public void deleteChannel(SPChannelPK channelPK) throws RssAgregatorException {
    try {
      getDAO().remove(channelPK);
    } catch (PersistenceException e) {
      throw new RssAgregatorException(
          SilverpeasExceptionMessages.failureOnDeleting(SYNDICATION_CHANNEL_WITH_ID,
              channelPK.getId()), e);
    }
  }

  public List<SPChannel> getChannels(String instanceId) throws RssAgregatorException {
    List<SPChannel> channels;
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion("instanceId", instanceId);
      criteria.setAscOrderBy("id");
      channels = (List<SPChannel>) getDAO().findBy(criteria);
    } catch (PersistenceException e) {
      throw new RssAgregatorException(
          SilverpeasExceptionMessages.failureOnGetting("syndication channels of component instance",
              instanceId), e);
    }
    return channels;
  }

  public void deleteChannels(String instanceId) throws RssAgregatorException {
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion("instanceId", instanceId);
      getDAO().removeBy(criteria);
    } catch (PersistenceException e) {
      throw new RssAgregatorException(SilverpeasExceptionMessages.failureOnDeleting(
          "syndication channels of component instance", instanceId), e);
    }
  }

  public void updateChannel(SPChannel channel) throws RssAgregatorException {
    try {
      getDAO().update(channel);
    } catch (PersistenceException e) {
      throw new RssAgregatorException(
          SilverpeasExceptionMessages.failureOnAdding(SYNDICATION_CHANNEL_WITH_ID,
              channel.getPK().getId()), e);
    }
  }

  private SilverpeasBeanDAO<SPChannel> getDAO() throws RssAgregatorException {
    if (rssDAO == null) {
      try {
        rssDAO = SilverpeasBeanDAOFactory.getDAO(SPChannel.class);
      } catch (PersistenceException e) {
        throw new RssAgregatorException(
            SilverpeasExceptionMessages.failureOnGetting("DAO for syndication channels", ""), e);
      }
    }
    return rssDAO;
  }

  public SPChannel getChannel(SPChannelPK channelPK)
      throws RssAgregatorException {
    SPChannel channel;
    try {
      channel = getDAO().findByPrimaryKey(channelPK);
    } catch (PersistenceException e) {
      throw new RssAgregatorException(
          SilverpeasExceptionMessages.failureOnGetting(SYNDICATION_CHANNEL_WITH_ID,
              channelPK.getId()), e);
    }
    return channel;
  }

}
