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

import org.silverpeas.core.contribution.indicator.ContributionIndicatorRegistry;
import org.silverpeas.core.contribution.indicator.NewContributionIndicator;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * @author silveryocha
 */
public class KmeliaContributionIndicatorInitializer implements Initialization {

  @Override
  public void init() throws Exception {
    ContributionIndicatorRegistry.get()
        .addNewContributionIndicator(new KmeliaNewPublicationIndicator());
  }

  /**
   * Implementation of the {@link NewContributionIndicator}
   */
  static class KmeliaNewPublicationIndicator implements NewContributionIndicator {

    private static final SettingBundle SETTING_BUNDLE = ResourceLocator.getSettingBundle(
        "org.silverpeas.publication.publicationSettings");

    @Override
    public Pair<String, String> relatedToComponentAndResourceType() {
      return Pair.of("kmelia", PublicationDetail.TYPE);
    }

    @Override
    public boolean isNew(final Instant lastUpdateInstant) {
      int days = SETTING_BUNDLE.getInteger("publication.new", 0);
      LocalDate threshold = LocalDate.ofInstant(lastUpdateInstant, ZoneId.systemDefault()).plusDays(days);
      LocalDate today = LocalDate.now();
      return today.isBefore(threshold) || today.isEqual(threshold);
    }
  }
}
