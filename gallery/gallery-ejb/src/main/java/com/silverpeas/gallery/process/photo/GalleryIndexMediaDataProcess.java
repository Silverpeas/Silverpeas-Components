package com.silverpeas.gallery.process.photo;

import com.silverpeas.form.RecordSet;
import com.silverpeas.gallery.GalleryComponentSettings;
import com.silverpeas.gallery.model.InternalMedia;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.gallery.model.Photo;
import com.silverpeas.gallery.process.AbstractGalleryDataProcess;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
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
          indexEntry.setThumbnailMimeType(iMedia.getFileMimeType());
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
