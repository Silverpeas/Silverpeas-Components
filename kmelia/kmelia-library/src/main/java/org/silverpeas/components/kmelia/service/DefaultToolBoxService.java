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
 * FLOSS exception. You should have received a copy of the text describing
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

import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.SettingBundle;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

/**
 * Service to handle the contributions in a Toolbox application.
 * @author mmoquillon
 */
@Service
@Named("toolboxService")
public class DefaultToolBoxService implements ApplicationService {

  @Inject
  private KmeliaService kmeliaService;

  @Override
  @SuppressWarnings("unchecked")
  public Optional<KmeliaPublication> getContributionById(
      final ContributionIdentifier contributionId) {
    return kmeliaService.getContributionById(contributionId);
  }

  @Override
  public SettingBundle getComponentSettings() {
    return kmeliaService.getComponentSettings();
  }

  @Override
  public LocalizationBundle getComponentMessages(final String language) {
    return kmeliaService.getComponentMessages(language);
  }

  @Override
  public boolean isRelatedTo(final String instanceId) {
    return instanceId.startsWith("toolbox");
  }
}
