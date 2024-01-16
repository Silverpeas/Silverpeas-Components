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
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.components.gallery.process.media;

import org.silverpeas.components.gallery.model.InternalMedia;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MetaData;
import org.silverpeas.components.gallery.model.Photo;
import org.silverpeas.components.gallery.process.AbstractGalleryDataProcess;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.session.ProcessSession;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Collection;

public class GalleryIndexMediaDataProcess extends AbstractGalleryDataProcess {

  /**
   * Default hidden conctructor
   * @param media
   */
  protected GalleryIndexMediaDataProcess(final Media media) {
    super(media);
  }

  /**
   * Process to index a media in Database
   * @param media
   * @return
   */
  public static GalleryIndexMediaDataProcess getInstance(final Media media) {
    return new GalleryIndexMediaDataProcess(media);
  }

  /*
   * (non-Javadoc)
   *
   * AbstractGalleryDataProcess#processData(com.silverpeas.gallery
   * .process.GalleryProcessExecutionContext, ProcessSession)
   */
  @Override
  protected void processData(final ProcessExecutionContext context,
      final ProcessSession session) throws Exception {
    // Nothing to do
  }

  /*
   * (non-Javadoc)
   * @see AbstractProcess#onSuccessful()
   */
  @Override
  public void onSuccessful() throws Exception {
    if (getMedia() != null) {
      // Index the Media
      FullIndexEntry indexEntry = setUpIndexEntry();
      if (getMedia().getVisibilityPeriod().getBeginDatable().isDefined()) {
        indexEntry.setStartDate(getMedia().getVisibilityPeriod().getBeginDatable());
      }
      if (getMedia().getVisibilityPeriod().getEndDatable().isDefined()) {
        indexEntry.setEndDate(getMedia().getVisibilityPeriod().getEndDatable());
      }

      if (getMedia() instanceof InternalMedia) {
        setThumbnailData(indexEntry);
      }

      if (getMedia() instanceof Photo) {
        setPhotoMetadata(indexEntry);
      }


      // indexation du contenu du formulaire XML
      final String xmlFormName = getOrganisationController()
          .getComponentParameterValue(getMedia().getInstanceId(), "XMLFormName");

      if (StringUtil.isDefined(xmlFormName)) {
        final String xmlFormShortName =
            xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
        PublicationTemplate pubTemplate;
        try {
          pubTemplate = PublicationTemplateManager.getInstance()
              .getPublicationTemplate(getMedia().getInstanceId() + ":" + xmlFormShortName);
          final RecordSet set = pubTemplate.getRecordSet();
          set.indexRecord(getMedia().getMediaPK().getId(), xmlFormShortName, indexEntry);

        } catch (final Exception e) {
          SilverLogger.getLogger(this).warn(e);
        }
      }

      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  private void setPhotoMetadata(final FullIndexEntry indexEntry) {
    Photo photo = (Photo) getMedia();
    // récupération des méta données pour les indéxer
    StringBuilder metaDataStr = new StringBuilder();
    MetaData metaData;
    final Collection<String> properties = photo.getMetaDataProperties();
    for (final String property : properties) {
      metaData = photo.getMetaData(property);
      final String value = metaData.getValue();
      metaDataStr.append(" ").append(value);
    }
    indexEntry.addTextContent(metaDataStr.toString());
    // indexation des méta données (une donnée par champ d'index)
    for (final String property : properties) {
      metaData = photo.getMetaData(property);
      final String value = metaData.getValue();

      if (metaData.isDate()) {
        indexEntry.addField("IPTC_" + property, metaData.getDateValue());
      } else {
        indexEntry.addField("IPTC_" + property, value);
      }
    }
  }

  private void setThumbnailData(final FullIndexEntry indexEntry) {
    InternalMedia iMedia = (InternalMedia) getMedia();
    if (StringUtil.isDefined(iMedia.getFileName())) {
      indexEntry.setThumbnail(iMedia.getFileName());
      indexEntry.setThumbnailMimeType(iMedia.getFileMimeType().getMimeType());
      indexEntry.setThumbnailDirectory(getMedia().getWorkspaceSubFolderName());
    }
  }

  private FullIndexEntry setUpIndexEntry() {
    FullIndexEntry indexEntry = new FullIndexEntry(getMedia().getMediaPK().getComponentName(),
        getMedia().getContributionType(), getMedia().getMediaPK().getId());
    indexEntry.setTitle(getMedia().getTitle());
    indexEntry.setPreview(getMedia().getDescription());
    indexEntry.setCreationDate(getMedia().getCreationDate());
    indexEntry.setCreationUser(getMedia().getCreatorId());
    indexEntry.setKeywords(getMedia().getKeyWord());
    return indexEntry;
  }
}
