/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.components.infoletter.model;

import org.silverpeas.core.NotSupportedException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.ResourceLocator;

import java.util.Date;
import java.util.Optional;

/**
 * @author silveryocha
 */
public class InfoLetterTemplateContributionWrapper implements Contribution {
  private static final long serialVersionUID = 2283060569458972864L;

  private final InfoLetter letter;

  public InfoLetterTemplateContributionWrapper(final InfoLetter letter) {
    this.letter = letter;
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return letter.getTemplateIdentifier();
  }

  @Override
  public String getTitle() {
    final String language = Optional.ofNullable(User.getCurrentRequester())
        .map(User::getUserPreferences)
        .map(UserPreferences::getLanguage)
        .orElseGet(DisplayI18NHelper::getDefaultLanguage);
    return ResourceLocator.getLocalizationBundle(
            "org.silverpeas.infoLetter.multilang.infoLetterBundle", language)
        .getString("infoLetter.template");
  }

  @Override
  public String getContributionType() {
    return InfoLetter.TYPE;
  }

  @Override
  public Date getCreationDate() {
    throw new NotSupportedException("creation date is not supported");
  }

  @Override
  public Date getLastUpdateDate() {
    throw new NotSupportedException("last update date is not supported");
  }

  @Override
  public User getCreator() {
    throw new NotSupportedException("creator is not supported");
  }

  @Override
  public User getLastUpdater() {
    throw new NotSupportedException("last updater is not supported");
  }
}
