/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.components.mydb.web;

import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.util.Pair;

import static org.silverpeas.core.util.StringUtil.EMPTY;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * This class permits to handle messages into context of MyDB component.
 * <p>
 * It is particularly useful for UI error management.
 * </p>
 * @author silveryocha
 */
public class MyDBMessageManager {

  private Pair<String, String> error = null;

  public static MyDBMessageManager get() {
    final Class<MyDBMessageManager> ownClass = MyDBMessageManager.class;
    return CacheAccessorProvider.getThreadCacheAccessor()
        .getCache()
        .computeIfAbsent(ownClass.getName(), ownClass, MyDBMessageManager::new);
  }

  private MyDBMessageManager() {
    // internal initialization
  }

  public boolean isError() {
    return error != null && isDefined(error.getFirst());
  }

  public Pair<String, String> getError() {
    return error;
  }

  public void setError(final String functionalError) {
    this.error = Pair.of(functionalError, EMPTY);
  }

  public void setError(final String functionalError, final Exception e) {
    if (e == null) {
      setError(functionalError);
    } else {
      this.error = Pair.of(functionalError, e.getLocalizedMessage());
    }
  }
}
