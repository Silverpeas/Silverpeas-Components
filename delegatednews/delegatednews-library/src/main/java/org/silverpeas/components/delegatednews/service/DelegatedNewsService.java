/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.delegatednews.service;

import org.silverpeas.components.delegatednews.model.DelegatedNews;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.List;

public interface DelegatedNewsService {

  class Constants {
    /**
     * The name of the component instance configuration parameter to use to indicate the
     * contributions in the given component instance can be subject to be submitted as a news to
     * publish in the home page.
     */
    public static final String DELEGATED_COMPONENT_PARAM = "delegatedNews";

    private Constants() {
    }
  }

  /**
   * Gets the instance of the actual implementation of the {@link DelegatedNewsService} interface.
   * @return a {@link DelegatedNewsService} instance.
   */
  static DelegatedNewsService get() {
    return ServiceProvider.getService(DelegatedNewsService.class);
  }

  /**
   * Submits the specified contribution as a delegated news. The submitted news can then be
   * identified by the contribution identifier from which it is spawned.
   * @param contribution the contribution to take as a news to publish.
   * @param visibilityPeriod the period during which the news has to be visible. If null, the news
   * is always visible.
   * @param userId the unique identifier of the user that has submitted the contribution as a news.
   */
  void submitNews(Contribution contribution, Period visibilityPeriod, String userId);

  /**
   * Gets the delegated news matching the specified contribution.
   * @param contributionId the unique identifier of the delegated news. It is the unique identifier of the
   * contribution from which it was spawned.
   * @return
   */
  DelegatedNews getDelegatedNews(String contributionId);

  /**
   * Gets the delegated news matching the specified contribution.
   * @param contributionIds a collection of unique identifiers, each of them referring a contribution
   * from which a delegated news was spawned.
   * @return a list of delegated news.
   */
  List<DelegatedNews> getDelegatedNews(Collection<String> contributionIds);

  /**
   * Gets all the delegated news.
   * @return a list of all of the delegated news.
   */
  List<DelegatedNews> getAllDelegatedNews();

  /**
   * Gets all the validated delegated news.
   * @return a list of all of the delegated news that are validated.
   */
  List<DelegatedNews> getAllValidDelegatedNews();

  /**
   * Validates the specified delegated news.
   * @param contributionId the unique identifier of the contribution from which the delegated news
   * was spawned.
   * @param validatorId the unique identifier of the validator.
   */
  void validateDelegatedNews(String contributionId, String validatorId);

  /**
   * Refuses the specified delegated news with the given motive.
   * @param contributionId the unique identifier of the contribution from which the delegated news
   * was spawned.
   * @param validatorId the unique identifier of the validator.
   * @param refusalMotive the motive of the refusal.
   */
  void refuseDelegatedNews(String contributionId, String validatorId, String refusalMotive);

  /**
   * Updates the period of visibility of the specified delegated news.
   * @param contributionId the unique identifier of the contribution from which the delegated news
   * was spawned.
   * @param visibilityPeriod the new visibility period. If null then the news will be always
   * visible.
   */
  void updateDateDelegatedNews(String contributionId, Period visibilityPeriod);

  /**
   * Updates the news matching the specified contribution and from the update information. This
   * method should be invoked when the matching contribution is updated.
   * @param id the unique identifier of the updated contribution and from which the news has to be
   * updated.
   * @param updaterId the unique identifier of the contribution updater.
   * @param visibilityPeriod the new period of visibility of the news. If null, then the news is
   * always visible.
   */
  void updateDelegatedNews(ContributionIdentifier id, String updaterId, Period visibilityPeriod);

  /**
   * Deletes the specified delegated news.
   * @param contributionId the unique identifier of the contribution from which the news was
   * spawned.
   */
  void deleteDelegatedNews(String contributionId);

  /**
   * Updates the order of the specified delegated news among the other news.
   * @param contributionId the unique identifier of the contribution from which the deletaged news
   * was spawned.
   * @param newsOrder the new order of the news.
   * @return the updated delegated news.
   */
  DelegatedNews updateOrderDelegatedNews(String contributionId, int newsOrder);
}
