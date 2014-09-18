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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.delegatednews.web;

import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;
import java.util.Date;

import com.silverpeas.web.TestResources;
import com.silverpeas.delegatednews.model.DelegatedNews;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONMarshaller;
import com.sun.jersey.json.impl.JSONMarshallerImpl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.junit.Test;

/**
 * A wrapper of resources used in the tests of the Delegated news web services.
 */
@Named(TestResources.TEST_RESOURCES_NAME)
public class DelegatedNewsTestResources extends TestResources {

  public static final String JAVA_PACKAGE = "com.silverpeas.delegatednews.web";
  public static final String SPRING_CONTEXT = "spring-delegatednews-webservice.xml";
  public static final String RESOURCE_PATH = "delegatednews/";
  public static final String INVALID_RESOURCE_PATH = "";

  @Inject
  private DelegatedNewsServiceMock delegatedNewsService;

  /**
   * Gets the delegatednews service used in tests.
   * @return the delegatednews service used in tests.
   */
  public DelegatedNewsServiceMock getDelegatedNewsServiceMock() {
    assertNotNull(delegatedNewsService);
    return delegatedNewsService;
  }

  /**
   * @param delegatedNewsToUpdate
   * @return
   */
  public DelegatedNewsEntity[] updateDelegatedNews(final DelegatedNewsEntity[] delegatedNewsToUpdate) {
    DelegatedNewsEntity[] tabResult = new DelegatedNewsEntity[delegatedNewsToUpdate.length];
    for(int i=0; i<delegatedNewsToUpdate.length;i++) {//the tab of DelegatedNewsEntity is in the new order
      DelegatedNewsEntity delegatedNewsEntity = delegatedNewsToUpdate[i];
      DelegatedNews delegatedNews = delegatedNewsEntity.toDelegatedNews();
      DelegatedNews delegatedNewsUpdated = getDelegatedNewsServiceMock().updateOrderDelegatedNews(delegatedNews.getPubId(), i);
      tabResult[i] = DelegatedNewsEntity.fromDelegatedNews(delegatedNewsUpdated);
    }
    return tabResult;
  }
}
