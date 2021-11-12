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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.infoletter.model;

import org.silverpeas.core.util.ContributionPath;

import java.util.Objects;

/**
 * The path of a given {@link InfoLetterPublicationPdC} in an application.
 * @author silveryocha
 */
public class InfoLetterPath extends ContributionPath<InfoLetterPublicationPdC> {
  private static final long serialVersionUID = 5984242196688248627L;

  /**
   * Gets an info letter path without taking care about right accesses.
   * @param infoLetter an info letter contribution instance.
   * @return an initialized {@link InfoLetterPath}.
   */
  public static InfoLetterPath getPath(final InfoLetterPublicationPdC infoLetter) {
    return new InfoLetterPath(infoLetter);
  }

  private final InfoLetterPublicationPdC infoLetter;

  private InfoLetterPath(InfoLetterPublicationPdC infoLetter) {
    this.infoLetter = infoLetter;
    add(infoLetter);
  }

  /**
   * Gets the contribution aimed by the path.
   * @return a {@link InfoLetterPublicationPdC} instance.
   */
  public InfoLetterPublicationPdC getInfoLetter() {
    return this.infoLetter;
  }

  @Override
  protected boolean isRoot(final InfoLetterPublicationPdC node) {
    return false;
  }

  @Override
  protected String getLabel(final InfoLetterPublicationPdC infoLetter, final String language) {
    return infoLetter.getTitle();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final InfoLetterPath that = (InfoLetterPath) o;
    return Objects.equals(infoLetter, that.infoLetter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), infoLetter);
  }
}
  