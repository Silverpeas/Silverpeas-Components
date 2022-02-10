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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.forums.subscription.bean;

import org.silverpeas.components.forums.model.ForumPath;
import org.silverpeas.components.forums.service.ForumService;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.subscription.bean.AbstractSubscriptionBean;

import static org.silverpeas.core.util.URLUtil.getSearchResultURL;

/**
 * @author silveryocha
 */
public class ForumSubscriptionBean extends AbstractSubscriptionBean {

  private final ForumPath path;

  protected ForumSubscriptionBean(final Subscription subscription, final ForumPath path,
      final SilverpeasComponentInstance component, final String language) {
    super(subscription, component, language);
    this.path = path;
  }

  @Override
  public String getPath() {
    return path.format(getLanguage());
  }

  @Override
  public String getLink() {
    return URLUtil.getApplicationURL() +
        getSearchResultURL(ForumService.get().getForumDetail(path.iterator().next().getPk()));
  }
}
