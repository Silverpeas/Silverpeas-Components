package com.silverpeas.whitePages.service;

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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ServicesFactory implements ApplicationContextAware {
  public static final String WHITE_PAGES_SERVICE = "whitePagesService";
  public static final String MIXED_SEARCH_SERVICE = "mixedSearchService";

  private ApplicationContext context;
  private static ServicesFactory instance;

  private ServicesFactory() {
  }

  public void setApplicationContext(ApplicationContext context)
      throws BeansException {
    this.context = context;
  }

  public static ServicesFactory getInstance() {
    synchronized (ServicesFactory.class) {
      if (ServicesFactory.instance == null) {
        ServicesFactory.instance = new ServicesFactory();
      }
    }
    return ServicesFactory.instance;
  }

  public static WhitePagesService getWhitePagesService() {
    return (WhitePagesService) getInstance().context
        .getBean(WHITE_PAGES_SERVICE);
  }
  
  public static MixedSearchService getMixedSearchService() {
    return (MixedSearchService) getInstance().context
        .getBean(MIXED_SEARCH_SERVICE);
  }

}
