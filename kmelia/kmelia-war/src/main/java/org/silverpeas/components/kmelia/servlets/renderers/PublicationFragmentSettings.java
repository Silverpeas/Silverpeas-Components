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

public class PublicationFragmentSettings {
  private String pubColor;
  private String highlightClass;
  private String pubState;
  private boolean linksAllowed;
  private boolean seeAlso;
  private boolean showImportance;
  private boolean linkAttachment;
  private boolean toSearch;
  private boolean showTopicPathNameinSearchResult;
  private boolean fileStorageShowExtraInfoPub;
  private boolean displayLinks;
  private boolean draggable;
  private boolean rateable;

  public String getPubColor() {
    return pubColor;
  }

  public PublicationFragmentSettings setPubColor(String pubColor) {
    this.pubColor = pubColor;
    return this;
  }

  public String getHighlightClass() {
    return highlightClass;
  }

  public PublicationFragmentSettings setHighlightClass(String highlightClass) {
    this.highlightClass = highlightClass;
    return this;
  }

  public String getPubState() {
    return pubState;
  }

  public PublicationFragmentSettings setPubState(String pubState) {
    this.pubState = pubState;
    return this;
  }

  public boolean isLinksAllowed() {
    return linksAllowed;
  }

  public PublicationFragmentSettings setLinksAllowed(boolean linksAllowed) {
    this.linksAllowed = linksAllowed;
    return this;
  }

  public boolean isSeeAlso() {
    return seeAlso;
  }

  public PublicationFragmentSettings setSeeAlso(boolean seeAlso) {
    this.seeAlso = seeAlso;
    return this;
  }

  public boolean isShowImportance() {
    return showImportance;
  }

  public PublicationFragmentSettings setShowImportance(boolean showImportance) {
    this.showImportance = showImportance;
    return this;
  }

  public boolean isLinkAttachment() {
    return linkAttachment;
  }

  public PublicationFragmentSettings setLinkAttachment(boolean linkAttachment) {
    this.linkAttachment = linkAttachment;
    return this;
  }

  public boolean isToSearch() {
    return toSearch;
  }

  public PublicationFragmentSettings setToSearch(boolean toSearch) {
    this.toSearch = toSearch;
    return this;
  }

  public boolean isShowTopicPathNameinSearchResult() {
    return showTopicPathNameinSearchResult;
  }

  public PublicationFragmentSettings setShowTopicPathNameinSearchResult(boolean showTopicPathNameinSearchResult) {
    this.showTopicPathNameinSearchResult = showTopicPathNameinSearchResult;
    return this;
  }

  public boolean isFileStorageShowExtraInfoPub() {
    return fileStorageShowExtraInfoPub;
  }

  public PublicationFragmentSettings setFileStorageShowExtraInfoPub(boolean fileStorageShowExtraInfoPub) {
    this.fileStorageShowExtraInfoPub = fileStorageShowExtraInfoPub;
    return this;
  }

  public boolean isDisplayLinks() {
    return displayLinks;
  }

  public PublicationFragmentSettings setDisplayLinks(boolean displayLinks) {
    this.displayLinks = displayLinks;
    return this;
  }

  public boolean isDraggable() {
    return draggable;
  }

  public PublicationFragmentSettings setDraggable(boolean draggable) {
    this.draggable = draggable;
    return this;
  }

  public boolean isRateable() {
    return rateable;
  }

  public PublicationFragmentSettings setRateable(boolean rateable) {
    this.rateable = rateable;
    return this;
  }
}
