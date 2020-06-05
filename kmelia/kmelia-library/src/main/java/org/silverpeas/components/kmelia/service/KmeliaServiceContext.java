/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.components.kmelia.service;

import org.silverpeas.core.contribution.publication.model.PublicationDetail;

import static org.silverpeas.core.cache.service.CacheServiceProvider.getRequestCacheService;

/**
 * This class permits to get the some flags about the Kmelia Service context.
 * @author Yohann Chastagnier
 */
class KmeliaServiceContext {

  /**
   * Marks that the given publication has been created into the current request context.
   * @param publication the publication to indicate created into current request context.
   */
  static void createdIntoRequestContext(final PublicationDetail publication) {
    String cacheKey = buildKey("publicationCreation", publication);
    getRequestCacheService().getCache().put(cacheKey, Boolean.TRUE);
  }

  /**
   * Indicates if the given publication has been created into the context of the current request.
   * @param publication the publication to verify.
   * @return true if yes, false otherwise.
   */
  static boolean hasPublicationBeenCreatedFromRequestContext(PublicationDetail publication) {
    String cacheKey = buildKey("publicationCreation", publication);
    return Boolean.TRUE == getRequestCacheService().getCache().get(cacheKey, Boolean.class);
  }


  /**
   * Marks that the given publication has been updated into the current request context.
   * @param publication the publication to indicate updated into current request context.
   */
  static void updatedIntoRequestContext(final PublicationDetail publication) {
    String cacheKey = buildKey("publicationModification", publication);
    getRequestCacheService().getCache().put(cacheKey, Boolean.TRUE);
  }

  /**
   * Indicates if the given publication has been updated into the context of the current request.
   * @param publication the publication to verify.
   * @return true if yes, false otherwise.
   */
  static boolean hasPublicationBeenUpdatedFromRequestContext(PublicationDetail publication) {
    String cacheKey = buildKey("publicationModification", publication);
    return Boolean.TRUE == getRequestCacheService().getCache().get(cacheKey, Boolean.class);
  }

  /**
   * Common build of cache key from a publication.
   * @param prefix the prefix of the key.
   * @param publication the publication.
   * @return a cache key as string.
   */
  private static String buildKey(final String prefix, final PublicationDetail publication) {
    return prefix + "@" + publication.getInstanceId() + "@" + publication.getId();
  }
}
