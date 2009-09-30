/*
 * Created on 20 août 2004
 *
 */
package com.silverpeas.rssAgregator.control;

import java.util.List;

import com.silverpeas.rssAgregator.model.RssAgregatorException;
import com.silverpeas.rssAgregator.model.SPChannel;
import com.silverpeas.rssAgregator.model.SPChannelPK;

/**
 * @author neysseri
 * 
 */
public interface RssAgregatorBm {

  public SPChannel addChannel(SPChannel channel) throws RssAgregatorException;

  public void updateChannel(SPChannel channel) throws RssAgregatorException;

  public void deleteChannel(SPChannelPK channelPK) throws RssAgregatorException;

  public void deleteChannels(String instanceId) throws RssAgregatorException;

  public List getChannels(String instanceId) throws RssAgregatorException;

  public SPChannel getChannel(SPChannelPK channelPK)
      throws RssAgregatorException;

}
