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

package com.silverpeas.mailinglist.jms;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import com.mockrunner.mock.jms.JMSMockObjectFactory;
import com.mockrunner.mock.jms.MockQueue;

public class MockObjectFactory implements ObjectFactory {

  private static JMSMockObjectFactory factory = new JMSMockObjectFactory();

  private static Map queues = new HashMap(10);

  public Object getObjectInstance(Object obj, Name name, Context nameCtx,
      Hashtable environment) throws Exception {
    // We only know how to deal with <code>javax.naming.Reference</code>s
    // that specify a class name of "javax.sql.DataSource"
    if ((obj == null) || !(obj instanceof Reference)) {
      return null;
    }
    Reference ref = (Reference) obj;
    if ("javax.jms.QueueConnectionFactory".equals(ref.getClassName())) {
      return factory.getMockQueueConnectionFactory();
    }

    if ("javax.jms.TopicConnectionFactory".equals(ref.getClassName())) {
      return factory.getMockTopicConnectionFactory();
    }

    if("javax.jms.Queue".equals(ref.getClassName())) {
      return getQueue(name);
    }
    return null;

  }

  private static Queue getQueue(Name name){
    Queue queue = (Queue) queues.get(name);
    if(queue == null){
      queue = factory.getDestinationManager().createQueue(name.toString());
      queues.put(name, queue);
    }
    return queue;
  }

  public static List getMessages(String name) {
    MockQueue queue = factory.getDestinationManager().getQueue(name);
    if(queue == null){
      queue = (MockQueue) queues.get(name);
    }
    return queue.getCurrentMessageList();
  }

}
