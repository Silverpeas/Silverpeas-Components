/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.components.formsonline.control;

import org.silverpeas.components.formsonline.model.FormInstance;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.util.SimpleContributionUIEntity;

import java.util.Set;
import java.util.function.Function;

/**
 * @author silveryocha
 */
public class RequestUIEntity extends SimpleContributionUIEntity<FormInstance> {

  /**
   * Hidden constructor.
   */
  private RequestUIEntity(final FormInstance data, final Set<String> selectedIds) {
    super(data, selectedIds);
  }

  /**
   * Converts the given data list into a {@link SilverpeasList} of item wrapping the {@link
   * Contribution}.
   * @param requests the list of {@link FormInstance}.
   * @return the {@link SilverpeasList} of {@link RequestUIEntity}.
   */
  public static SilverpeasList<RequestUIEntity> convertList(final SilverpeasList<FormInstance> requests,
      final Set<String> selectedIds) {
    final Function<FormInstance, RequestUIEntity> converter = c -> new RequestUIEntity(c, selectedIds);
    return requests.stream().map(converter).collect(SilverpeasList.collector(requests));
  }

  /**
   * Gets the creator with the use of request cache in order to avoid getting again and again
   * data from persistence about same users occurring into several lines.
   * @return the creator.
   */
  public User getCreator() {
    return getUserByIdFromCache(getData().getCreatorId());
  }

  /**
   * Gets the validator with the use of request cache in order to avoid getting again and again
   * data from persistence about same validators occurring into several lines.
   * @return the creator.
   */
  public User getValidator() {
    return getUserByIdFromCache(getData().getValidatorId());
  }
}
