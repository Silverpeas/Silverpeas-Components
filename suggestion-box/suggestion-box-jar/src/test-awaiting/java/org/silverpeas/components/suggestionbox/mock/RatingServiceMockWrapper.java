/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.components.suggestionbox.mock;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.notation.ejb.RatingBm;
import org.mockito.Mockito;
import org.silverpeas.rating.ContributionRating;
import org.silverpeas.rating.ContributionRatingPK;
import org.silverpeas.rating.RaterRatingPK;

import java.util.Map;

/**
 * @author: Yohann Chastagnier
 */
public class RatingServiceMockWrapper implements RatingBm {

  private final RatingBm mock = Mockito.mock(RatingBm.class);

  public RatingBm getMock() {
    return mock;
  }

  @Override
  public void updateRating(final RaterRatingPK pk, final int note) {
    mock.updateRating(pk, note);
  }

  @Override
  public void moveRating(final ContributionRatingPK pk, final String componentInstanceId) {
    mock.moveRating(pk, componentInstanceId);
  }

  @Override
  public void deleteRating(final ContributionRatingPK pk) {
    mock.deleteRating(pk);
  }

  @Override
  public void deleteRaterRating(final RaterRatingPK pk) {
    mock.deleteRaterRating(pk);
  }

  @Override
  public void deleteComponentRatings(final String componentInstanceId) {
    mock.deleteComponentRatings(componentInstanceId);
  }

  @Override
  public Map<String, ContributionRating> getRatings(final SilverpeasContent... contributions) {
    return mock.getRatings(contributions);
  }

  @Override
  public ContributionRating getRating(final SilverpeasContent contribution) {
    return mock.getRating(contribution);
  }

  @Override
  public ContributionRating getRating(final ContributionRatingPK pk) {
    return mock.getRating(pk);
  }

  @Override
  public boolean hasUserRating(final RaterRatingPK pk) {
    return mock.hasUserRating(pk);
  }
}
