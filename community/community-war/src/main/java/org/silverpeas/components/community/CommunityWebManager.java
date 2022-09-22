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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.components.community;

import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;

import java.util.Set;
import java.util.function.Supplier;

import static org.silverpeas.components.community.CommunityComponentSettings.getMessagesIn;


/**
 * WEB manager which allows to centralize code to be used by REST Web Services and Web Component
 * Controller.
 * @author silveryocha
 */
@Service
public class CommunityWebManager {

  private static final String CACHE_KEY_PREFIX = CommunityWebManager.class.getSimpleName() + ":";

  protected CommunityWebManager() {
  }

  /**
   * Gets the singleton instance of the provider.
   */
  public static CommunityWebManager get() {
    return ServiceProvider.getSingleton(CommunityWebManager.class);
  }

  /**
   * Indicates if the current requester is a member.
   * <p>
   *   A member MUST be directly specified into ADMIN, PUBLISHER, WRITER or READER role of direct
   *   parent space.
   * </p>
   * @param community {@link CommunityOfUsers} instance.
   * @return true if member, false otherwise.
   */
  public boolean isMemberOf(final CommunityOfUsers community) {
    return requestCache("isMemberOf", community.getId(), Boolean.class,
        () -> community.isMember(User.getCurrentRequester()));
  }

  /**
   * Gets the roles the current requester has on the given community.
   * @param community {@link CommunityOfUsers} instance.
   * @return a set of {@link SilverpeasRole}.
   */
  @SuppressWarnings("unchecked")
  public Set<SilverpeasRole> getUserRoleOn(final CommunityOfUsers community) {
    return requestCache("userRoleOf", community.getId(), Set.class,
        () -> community.getUserRoleOn(User.getCurrentRequester()));
  }

  private <T> T requestCache(final String type, final String id, Class<T> classType,
      Supplier<T> supplier) {
    return CacheServiceProvider.getRequestCacheService()
        .getCache()
        .computeIfAbsent(CACHE_KEY_PREFIX + type + ":" + id, classType, supplier);
  }

  /**
   * Push a success message to the current user.
   * @param messageKey the key of the message.
   * @param params the message parameters.
   */
  private void successMessage(String messageKey, Object... params) {
    User owner = User.getCurrentRequester();
    String userLanguage = owner.getUserPreferences().getLanguage();
    getMessager().addSuccess(getMessagesIn(userLanguage).getStringWithParams(messageKey, params));
  }

  private WebMessager getMessager() {
    return WebMessager.getInstance();
  }
}
