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

package org.silverpeas.components.kmelia.servlets.renderers;

import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.UserNameGenerator;
import org.silverpeas.kernel.util.StringUtil;

import java.util.List;

/**
 * Context of a web page rendering.
 * @author mmoquillon
 */
public class RenderingContext {
  private final GraphicElementFactory gef;
  private boolean sortAllowed = false;
  private boolean linksAllowed = false;
  private boolean seeAlso = false;
  private boolean searchInProgress = false;
  private boolean toLink;
  private boolean attachmentToLink;
  private final KmeliaSessionController kmeliaSC;
  private List<PublicationPK> selectedPublications;
  private List<KmeliaPublication> publications;
  private String role;
  private final MultiSilverpeasBundle resources;
  private String pubToHighlight;

  public RenderingContext(KmeliaSessionController kmeliaSC, GraphicElementFactory gef) {
    this.kmeliaSC = kmeliaSC;
    this.gef = gef;
    this.resources =
        new MultiSilverpeasBundle(kmeliaSC.getMultilang(), kmeliaSC.getIcon(),
            kmeliaSC.getSettings(),
            kmeliaSC.getLanguage());
  }

  public boolean isSortAllowed() {
    return sortAllowed;
  }

  public RenderingContext setSortAllowed(boolean sortAllowed) {
    this.sortAllowed = sortAllowed;
    return this;
  }

  public boolean isLinksAllowed() {
    return linksAllowed;
  }

  public RenderingContext setLinksAllowed(boolean linksAllowed) {
    this.linksAllowed = linksAllowed;
    return this;
  }

  public boolean isSeeAlso() {
    return seeAlso;
  }

  public RenderingContext setSeeAlso(boolean seeAlso) {
    this.seeAlso = seeAlso;
    return this;
  }

  public boolean isSearchInProgress() {
    return searchInProgress;
  }

  public RenderingContext setSearchInProgress(boolean searchInProgress) {
    this.searchInProgress = searchInProgress;
    return this;
  }

  public List<KmeliaPublication> getPublications() {
    return publications;
  }

  public RenderingContext setPublications(List<KmeliaPublication> publicationDetails) {
    this.publications = publicationDetails;
    return this;
  }

  public boolean displayPublications() {
    return publications != null;
  }

  public KmeliaSessionController getSessionController() {
    return kmeliaSC;
  }

  public String getRole() {
    return role;
  }

  public RenderingContext setRole(String role) {
    this.role = role;
    return this;
  }

  public List<PublicationPK> getSelectedPublications() {
    return selectedPublications;
  }

  public RenderingContext setSelectedPublications(List<PublicationPK> selectedPublications) {
    this.selectedPublications = selectedPublications;
    return this;
  }

  public MultiSilverpeasBundle getResources() {
    return resources;
  }

  public GraphicElementFactory getGraphicElementFactory() {
    return gef;
  }

  public boolean isToLink() {
    return toLink;
  }

  public RenderingContext setToLink(boolean toLink) {
    this.toLink = toLink;
    return this;
  }

  public boolean isAttachmentToLink() {
    return attachmentToLink;
  }

  public RenderingContext setAttachmentToLink(boolean attachmentToLink) {
    this.attachmentToLink = attachmentToLink;
    return this;
  }

  public String getPublicationToHighlight() {
    return pubToHighlight;
  }

  public RenderingContext setPublicationToHighlight(String pubToHighlight) {
    this.pubToHighlight = pubToHighlight;
    return this;
  }

  /**
   * Gets the last user authoring the specified publication.
   * @param publication a publication.
   * @return the name of the last publication author.
   */
  public String getLastAuthor(KmeliaPublication publication) {
    User creator = publication.getCreator();
    PublicationDetail pub = publication.getDetail();
    String updaterId = pub.getUpdaterId();
    User updater;
    if (StringUtil.isDefined(updaterId)) {
      updater = kmeliaSC.getUserDetail(updaterId);
    } else {
      updater = creator;
    }

    if (updater != null && (StringUtil.isDefined(updater.getFirstName()) ||
        StringUtil.isDefined(updater.getLastName()))) {
      return UserNameGenerator.toString(updater, kmeliaSC.getUserId());
    }
    return kmeliaSC.getString("kmelia.UnknownUser");
  }
}
  