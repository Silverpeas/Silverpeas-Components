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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.infoletter.model;

import org.silverpeas.components.infoletter.InfoLetterContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author lbertin
 */
public class InfoLetterPublicationPdC extends InfoLetterPublication
    implements SilverContentInterface {
  private static final long serialVersionUID = -2174573301215680444L;
  public static final String TYPE = "publication";

  private String silverObjectId;
  private String positions;

  /**
   * Default constructor
   */
  public InfoLetterPublicationPdC() {
    super();
  }

  /**
   * Constructor from InfoLetterPublication
   * @param ilp InfoLetterPublication
   */
  public InfoLetterPublicationPdC(InfoLetterPublication ilp) {
    super(ilp.getPK(), ilp.getInstanceId(), ilp.getTitle(), ilp.getDescription(),
        ilp.getParutionDate(), ilp.getPublicationState(), ilp.getLetterId());
  }

  /**
   * @return the positions
   */
  public String getPositions() {
    return positions;
  }

  /**
   * @param positions the positions to set
   */
  public void setPositions(String positions) {
    this.positions = positions;
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  public String getURL() {
    return "searchResult?Type=Publication&Id=" + getId();
  }

  @Override
  public String getId() {
    return getPK().getId();
  }

  @Override
  public String getDate() {
    return getParutionDate();
  }

  @Override
  public String getCreatorId() {
    return null;
  }

  @Override
  public String getIconUrl() {
    /*
     * publication icon
     */
    return "infoLetterSmall.gif";
  }

  @Override
  public String getSilverCreationDate() {
    return getParutionDate();
  }

  @Override
  public String getDescription(String language) {
    return getDescription();
  }

  @Override
  public String getName(String language) {
    return getName();
  }

  @Override
  public Collection<String> getLanguages() {
    return Collections.emptyList();
  }

  @Override
  public String getSilverpeasContentId() {
    if (this.silverObjectId == null) {
      InfoLetterContentManager contentManager =
          ServiceProvider.getService(InfoLetterContentManager.class);
      int objectId = contentManager.getSilverContentId(getId(), getComponentInstanceId());
      if (objectId >= 0) {
        this.silverObjectId = String.valueOf(objectId);
      }
    }
    return this.silverObjectId;
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return super.getIdentifier();
  }

  @Override
  public String getContributionType() {
    return TYPE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Optional<InfoLetterPath> getResourcePath() {
    return Optional.of(this).map(InfoLetterPath::getPath);
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
