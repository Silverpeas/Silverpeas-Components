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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.gallery.process.media;

import com.silverpeas.form.RecordSet;
import com.silverpeas.gallery.model.InternalMedia;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.gallery.model.Photo;
import com.silverpeas.gallery.process.AbstractGalleryDataProcess;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import org.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.DateUtil;
import org.silverpeas.process.session.ProcessSession;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;

import java.util.Collection;

public class GalleryIndexMediaDataProcess extends AbstractGalleryDataProcess {

  /**
   * Process to index a media in Database
   * @param media
   * @return
   */
  public static GalleryIndexMediaDataProcess getInstance(final Media media) {
    return new GalleryIndexMediaDataProcess(media);
  }

  /**
   * Default hidden conctructor
   * @param media
   */
  protected GalleryIndexMediaDataProcess(final Media media) {
    super(media);
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.gallery.process.AbstractGalleryDataProcess#processData(com.silverpeas.gallery
   * .process.GalleryProcessExecutionContext, org.silverpeas.process.session.ProcessSession)
   */
  @Override
  protected void processData(final GalleryProcessExecutionContext context,
      final ProcessSession session) throws Exception {
    // Nothing to do
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.process.AbstractProcess#onSuccessful()
   */
  @Override
  public void onSuccessful() throws Exception {
    SilverTrace
        .info("gallery", "GalleryIndexMediaDataProcess.onSuccessful()", "root.MSG_GEN_ENTER_METHOD",
            "getMedia() = " + getMedia().toString());

    if (getMedia() != null) {
      // Index the Media
      FullIndexEntry indexEntry = new FullIndexEntry(getMedia().getMediaPK().getComponentName(),
          getMedia().getContributionType(), getMedia().getMediaPK().getId());
      indexEntry.setTitle(getMedia().getTitle());
      indexEntry.setPreView(getMedia().getDescription());
      indexEntry.setCreationDate(getMedia().getCreationDate());
      indexEntry.setCreationUser(getMedia().getCreatorId());
      indexEntry.setKeyWords(getMedia().getKeyWord());
      if (getMedia().getVisibilityPeriod().getBeginDatable().isDefined()) {
        indexEntry.setStartDate(
            DateUtil.date2SQLDate(getMedia().getVisibilityPeriod().getBeginDatable()));
      }
      if (getMedia().getVisibilityPeriod().getEndDatable().isDefined()) {
        indexEntry
            .setEndDate(DateUtil.date2SQLDate(getMedia().getVisibilityPeriod().getEndDatable()));
      }

      if (getMedia() instanceof InternalMedia) {
        InternalMedia iMedia = (InternalMedia) getMedia();
        if (StringUtil.isDefined(iMedia.getFileName())) {
          indexEntry.setThumbnail(iMedia.getFileName());
          indexEntry.setThumbnailMimeType(iMedia.getFileMimeType().getMimeType());
          indexEntry.setThumbnailDirectory(getMedia().getWorkspaceSubFolderName());
        }
      }

      if (getMedia() instanceof Photo) {
        Photo photo = (Photo) getMedia();
        // récupération des méta données pour les indéxer
        String metaDataStr = "";
        MetaData metaData;
        final Collection<String> properties = photo.getMetaDataProperties();
        for (final String property : properties) {
          metaData = photo.getMetaData(property);
          final String value = metaData.getValue();
          metaDataStr = metaDataStr + " " + value;
        }
        indexEntry.addTextContent(metaDataStr);
        SilverTrace.info("gallery", "GalleryIndexPhotoDataProcess.onSuccessful()",
            "root.MSG_GEN_ENTER_METHOD",
            "metaData = " + metaDataStr + " indexEntry = " + indexEntry.toString());
        // indexation des méta données (une donnée par champ d'index)
        for (final String property : properties) {
          metaData = photo.getMetaData(property);
          final String value = metaData.getValue();
          SilverTrace.info("gallery", "GalleryIndexMediaDataProcess.onSuccessful()",
              "root.MSG_GEN_ENTER_METHOD", "property = " + property + " value = " + value);
          if (metaData.isDate()) {
            indexEntry.addField("IPTC_" + property, metaData.getDateValue());
          } else {
            indexEntry.addField("IPTC_" + property, value);
          }
        }
      }


      // indexation du contenu du formulaire XML
      final String xmlFormName = getOrganisationController()
          .getComponentParameterValue(getMedia().getInstanceId(), "XMLFormName");
      SilverTrace.info("gallery", "GalleryIndexMediaDataProcess.onSuccessful()",
          "root.MSG_GEN_ENTER_METHOD", "xmlFormName = " + xmlFormName);
      if (StringUtil.isDefined(xmlFormName)) {
        final String xmlFormShortName =
            xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
        PublicationTemplate pubTemplate;
        try {
          pubTemplate = PublicationTemplateManager.getInstance()
              .getPublicationTemplate(getMedia().getInstanceId() + ":" + xmlFormShortName);
          final RecordSet set = pubTemplate.getRecordSet();
          set.indexRecord(getMedia().getMediaPK().getId(), xmlFormShortName, indexEntry);
          SilverTrace.info("gallery", "GalleryIndexMediaDataProcess.onSuccessful()",
              "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry.toString());
        } catch (final Exception e) {
          SilverTrace.info("gallery", "GalleryIndexMediaDataProcess.onSuccessful()",
              "root.MSG_GEN_ENTER_METHOD", "xmlFormName = " + xmlFormName);
        }
      }

      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }
}
