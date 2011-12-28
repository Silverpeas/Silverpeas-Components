/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.kmelia.control;

import javax.inject.Inject;

/**
 * A factory of BlogService objects. It wraps the access the underlying IoC container from which are
 * get the BlogService objects.
 */
public class KmeliaServiceFactory {
  
  private static final KmeliaServiceFactory instance = new KmeliaServiceFactory();
  
  public static KmeliaServiceFactory getFactory() {
    return instance;
  }
  
  @Inject
  private KmeliaService kmeliaService;
  
  public KmeliaService getKmeliaService() {
    return kmeliaService;
  }
  
  private KmeliaServiceFactory() {}
}
