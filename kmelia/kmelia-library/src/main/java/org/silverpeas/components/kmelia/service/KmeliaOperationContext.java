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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.kmelia.service;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.persistence.datasource.OperationContext;

import java.util.Optional;

/**
 * Defines the context of a mutable operation on publications that is currently invoked in Kmelia.
 * Useful to maintain the operation state along different invocations, mainly with system
 * notifications that are processed by different transverse services.
 * @author mmoquillon
 */
public class KmeliaOperationContext {

  private static final String cacheKey = "@@@KmeliaOperationContext@@@";
  private final OperationType operation;

  /**
   * Gets the current operation context. None if no such a context has been set.
   * @return optionally the current operation context if any.
   */
  public static Optional<KmeliaOperationContext> current() {
    return Optional.ofNullable(CacheAccessorProvider.getThreadCacheAccessor()
        .getCache()
        .get(cacheKey, KmeliaOperationContext.class));
  }

  /**
   * Spawn a new Kmelia context about the given operation on publications.
   * @param type the type of the operation.
   */
  public static void about(final OperationType type) {
    KmeliaOperationContext context = new KmeliaOperationContext(type);
    CacheAccessorProvider.getThreadCacheAccessor().getCache().put(cacheKey, context);
  }

  private KmeliaOperationContext(final OperationType type) {
    this.operation = type;
  }

  public boolean isAbout(final OperationType type) {
    return this.operation == type;
  }

  public User getInvoker() {
    return OperationContext.fromCurrentRequester().getUser();
  }

  /**
   * Different kinds of operations.
   */
  public enum OperationType {
    /**
     * A publication is being created.
     */
    CREATION,
    /**
     * A publication is being updated.
     */
    UPDATE,
    /**
     * A publication is being removed (that is moved into the trash).
     */
    REMOVING,
    /**
     * A publication is being deleted (id est definitely deleted).
     */
    DELETION
  }
}
  