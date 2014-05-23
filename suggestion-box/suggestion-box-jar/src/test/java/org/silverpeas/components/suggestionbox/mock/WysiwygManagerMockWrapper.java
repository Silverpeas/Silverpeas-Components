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

import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.util.WAPrimaryKey;
import org.mockito.Mockito;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.wysiwyg.WysiwygException;
import org.silverpeas.wysiwyg.control.WysiwygManager;

import java.util.List;
import java.util.Map;

/**
 * @author: Yohann Chastagnier
 */
public class WysiwygManagerMockWrapper extends WysiwygManager {

  private WysiwygManager mock = Mockito.mock(WysiwygManager.class);

  public WysiwygManager getMock() {
    return mock;
  }

  @Override
  public List<SimpleDocument> getImages(final String id, final String componentId) {
    return mock.getImages(id, componentId);
  }

  @Override
  public String getWebsiteRepository() {
    return mock.getWebsiteRepository();
  }

  @Override
  public String[][] getWebsiteImages(final String path, final String componentId)
      throws WysiwygException {
    return mock.getWebsiteImages(path, componentId);
  }

  @Override
  public String[][] getWebsitePages(final String path, final String componentId)
      throws WysiwygException {
    return mock.getWebsitePages(path, componentId);
  }

  @Override
  public String getOldWysiwygFileName(final String objectId) {
    return mock.getOldWysiwygFileName(objectId);
  }

  @Override
  public String getWysiwygFileName(final String objectId, final String currentLanguage) {
    return mock.getWysiwygFileName(objectId, currentLanguage);
  }

  @Override
  public String getImagesFileName(final String objectId) {
    return mock.getImagesFileName(objectId);
  }

  @Override
  public void deleteFileAndAttachment(final String componentId, final String id) {
    mock.deleteFileAndAttachment(componentId, id);
  }

  @Override
  public void deleteFile(final String componentId, final String objectId, final String language) {
    mock.deleteFile(componentId, objectId, language);
  }

  @Override
  public void createFileAndAttachment(final String textHtml, final WAPrimaryKey foreignKey,
      final String context, final String userId, final String contentLanguage) {
    mock.createFileAndAttachment(textHtml, foreignKey, context, userId, contentLanguage);
  }

  @Override
  public void createFileAndAttachment(final String textHtml, final WAPrimaryKey foreignKey,
      final String userId, final String contentLanguage) {
    mock.createFileAndAttachment(textHtml, foreignKey, userId, contentLanguage);
  }

  @Override
  public void createUnindexedFileAndAttachment(final String textHtml, final WAPrimaryKey foreignKey,
      final String userId, final String contentLanguage) {
    mock.createUnindexedFileAndAttachment(textHtml, foreignKey, userId, contentLanguage);
  }

  @Override
  public void addToIndex(final FullIndexEntry indexEntry, final ForeignPK pk,
      final String language) {
    mock.addToIndex(indexEntry, pk, language);
  }

  @Override
  public void updateFileAndAttachment(final String textHtml, final String componentId,
      final String objectId, final String userId, final String language) {
    mock.updateFileAndAttachment(textHtml, componentId, objectId, userId, language);
  }

  @Override
  public void updateFileAndAttachment(final String textHtml, final String componentId,
      final String objectId, final String userId, final String language, final boolean indexIt) {
    mock.updateFileAndAttachment(textHtml, componentId, objectId, userId, language, indexIt);
  }

  @Override
  public void save(final String textHtml, final String componentId, final String objectId,
      final String userId, final String language, final boolean indexIt) {
    mock.save(textHtml, componentId, objectId, userId, language, indexIt);
  }

  @Override
  public void deleteWysiwygAttachments(final String componentId, final String objectId) {
    mock.deleteWysiwygAttachments(componentId, objectId);
  }

  @Override
  public void deleteWysiwygAttachmentsOnly(final String spaceId, final String componentId,
      final String objectId) throws WysiwygException {
    mock.deleteWysiwygAttachmentsOnly(spaceId, componentId, objectId);
  }

  @Override
  public String load(final String componentId, final String objectId, final String language) {
    return mock.load(componentId, objectId, language);
  }

  @Override
  public List<String> getEmbeddedAttachmentIds(final String content) {
    return mock.getEmbeddedAttachmentIds(content);
  }

  @Override
  public String loadFileWebsite(final String path, final String fileName) throws WysiwygException {
    return mock.loadFileWebsite(path, fileName);
  }

  @Override
  public boolean haveGotWysiwygToDisplay(final String componentId, final String objectId,
      final String language) {
    return mock.haveGotWysiwygToDisplay(componentId, objectId, language);
  }

  @Override
  public boolean haveGotWysiwyg(final String componentId, final String objectId,
      final String language) {
    return mock.haveGotWysiwyg(componentId, objectId, language);
  }

  @Override
  public void updateWebsite(final String cheminFichier, final String nomFichier,
      final String contenuFichier) throws WysiwygException {
    mock.updateWebsite(cheminFichier, nomFichier, contenuFichier);
  }

  @Override
  public Map<String, String> copy(final String oldComponentId, final String oldObjectId,
      final String componentId, final String objectId, final String userId) {
    return mock.copy(oldComponentId, oldObjectId, componentId, objectId, userId);
  }

  @Override
  public void move(final String fromComponentId, final String fromObjectId,
      final String componentId, final String objectId) {
    mock.move(fromComponentId, fromObjectId, componentId, objectId);
  }

  @Override
  public void wysiwygPlaceHaveChanged(final String oldComponentId, final String oldObjectId,
      final String newComponentId, final String newObjectId) {
    mock.wysiwygPlaceHaveChanged(oldComponentId, oldObjectId, newComponentId, newObjectId);
  }

  @Override
  public String getWysiwygPath(final String componentId, final String objectId,
      final String language) {
    return mock.getWysiwygPath(componentId, objectId, language);
  }

  @Override
  public String getWysiwygPath(final String componentId, final String objectId) {
    return mock.getWysiwygPath(componentId, objectId);
  }

  @Override
  public List<ComponentInstLight> getGalleries() {
    return mock.getGalleries();
  }

  @Override
  public List<ComponentInstLight> getStorageFile() {
    return mock.getStorageFile();
  }

  @Override
  public void indexEmbeddedLinkedFiles(final FullIndexEntry indexEntry,
      final List<String> embeddedAttachmentIds) {
    mock.indexEmbeddedLinkedFiles(indexEntry, embeddedAttachmentIds);
  }

  @Override
  public String createPath(final String componentId, final String context) {
    return mock.createPath(componentId, context);
  }
}
