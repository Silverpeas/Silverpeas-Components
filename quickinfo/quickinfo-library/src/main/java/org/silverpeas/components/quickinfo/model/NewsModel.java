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

package org.silverpeas.components.quickinfo.model;

import org.silverpeas.core.contribution.model.ContributionModel;
import org.silverpeas.core.contribution.model.DefaultContributionModel;

import static org.silverpeas.core.contribution.publication.model.PublicationDetail.DELAYED_VISIBILITY_AT_MODEL_PROPERTY;

/**
 * The default implementation of the {@link News} entity which is extending the default one.
 * @author silveryocha
 * @see ContributionModel
 * @see DefaultContributionModel
 */
public class NewsModel extends DefaultContributionModel<News> {

  NewsModel(final News contribution) {
    super(contribution);
  }

  @Override
  public <T> T getProperty(final String property, final Object... parameters) {
    if (DELAYED_VISIBILITY_AT_MODEL_PROPERTY.equals(property)) {
      return getContribution().getPublication().getModel().getProperty(property, parameters);
    }
    return super.getProperty(property, parameters);
  }
}
