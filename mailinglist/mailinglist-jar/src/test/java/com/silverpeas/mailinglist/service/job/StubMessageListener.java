/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.silverpeas.mailinglist.service.job;

import com.silverpeas.mailinglist.service.event.MessageEvent;
import com.silverpeas.mailinglist.service.event.MessageListener;

public class StubMessageListener implements MessageListener {

  private MessageEvent event;

  private String componentId;

  public StubMessageListener() {
    this.componentId = "componentId";
  }

  public StubMessageListener(String componentId) {
    this.componentId = componentId;
  }

  public String getComponentId() {
    return this.componentId;
  }

  public void onMessage(MessageEvent event) {
    this.event = event;
  }

  public MessageEvent getMessageEvent() {
    return this.event;
  }

  public boolean checkSender(String email) {
    return !"marge.simpson@silverpeas.com".equals(email);
  }

}
