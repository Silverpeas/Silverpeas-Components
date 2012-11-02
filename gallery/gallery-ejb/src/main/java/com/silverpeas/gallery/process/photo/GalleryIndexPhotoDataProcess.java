package com.silverpeas.gallery.process.photo;

import java.util.Collection;

import org.silverpeas.process.session.ProcessSession;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;

import com.silverpeas.form.RecordSet;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.process.AbstractGalleryDataProcess;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.ResourceLocator;

public class GalleryIndexPhotoDataProcess extends AbstractGalleryDataProcess {

  /**
   * Process to index a photo in Database
   * @param photo
   * @return
   */
  public static GalleryIndexPhotoDataProcess getInstance(final PhotoDetail photo) {
    return new GalleryIndexPhotoDataProcess(photo);
  }

  /**
   * Default hidden conctructor
   * @param photo
   */
  protected GalleryIndexPhotoDataProcess(final PhotoDetail photo) {
    super(photo);
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
    SilverTrace.info("gallery", "GalleryIndexPhotoDataProcess.onSuccessful()",
        "root.MSG_GEN_ENTER_METHOD", "getPhoto()Detail = " + getPhoto().toString());
    FullIndexEntry indexEntry = null;

    if (getPhoto() != null) {
      // Index the Photo
      indexEntry =
          new FullIndexEntry(getPhoto().getPhotoPK().getComponentName(), "Photo", getPhoto()
              .getPhotoPK().getId());
      indexEntry.setTitle(getPhoto().getTitle());
      indexEntry.setPreView(getPhoto().getDescription());
      indexEntry.setCreationDate(getPhoto().getCreationDate());
      indexEntry.setCreationUser(getPhoto().getCreatorId());
      indexEntry.setKeyWords(getPhoto().getKeyWord());
      if (getPhoto().getBeginDate() != null) {
        indexEntry.setStartDate(DateUtil.date2SQLDate(getPhoto().getBeginDate()));
      }
      if (getPhoto().getEndDate() != null) {
        indexEntry.setEndDate(DateUtil.date2SQLDate(getPhoto().getEndDate()));
      }

      if (getPhoto().getImageName() != null) {
        final ResourceLocator gallerySettings =
            new ResourceLocator("com.silverpeas.gallery.settings.gallerySettings", "");
        indexEntry.setThumbnail(getPhoto().getImageName());
        indexEntry.setThumbnailMimeType(getPhoto().getImageMimeType());
        indexEntry.setThumbnailDirectory(gallerySettings.getString("imagesSubDirectory") +
            getPhoto().getPhotoPK().getId());
      }

      // récupération des méta données pour les indéxer
      String metaDataStr = "";
      MetaData metaData;
      final Collection<String> properties = getPhoto().getMetaDataProperties();
      for (final String property : properties) {
        metaData = getPhoto().getMetaData(property);
        final String value = metaData.getValue();
        metaDataStr = metaDataStr + " " + value;
      }
      indexEntry.addTextContent(metaDataStr);
      SilverTrace.info("gallery", "GalleryIndexPhotoDataProcess.onSuccessful()",
          "root.MSG_GEN_ENTER_METHOD",
          "metaData = " + metaDataStr + " indexEntry = " + indexEntry.toString());
      // indexation des méta données (une donnée par champ d'index)
      for (final String property : properties) {
        metaData = getPhoto().getMetaData(property);
        final String value = metaData.getValue();
        SilverTrace.info("gallery", "GalleryIndexPhotoDataProcess.onSuccessful()",
            "root.MSG_GEN_ENTER_METHOD", "property = " + property + " value = " + value);
        if (metaData.isDate()) {
          indexEntry.addField("IPTC_" + property, metaData.getDateValue());
        } else {
          indexEntry.addField("IPTC_" + property, value);
        }
      }

      // indexation du contenu du formulaire XML
      final String xmlFormName =
          getOrganizationController().getComponentParameterValue(getPhoto().getInstanceId(),
              "XMLFormName");
      SilverTrace.info("gallery", "GalleryIndexPhotoDataProcess.onSuccessful()",
          "root.MSG_GEN_ENTER_METHOD", "xmlFormName = " + xmlFormName);
      if (StringUtil.isDefined(xmlFormName)) {
        final String xmlFormShortName =
            xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
        PublicationTemplate pubTemplate;
        try {
          pubTemplate =
              PublicationTemplateManager.getInstance().getPublicationTemplate(
                  getPhoto().getInstanceId() + ":" + xmlFormShortName);
          final RecordSet set = pubTemplate.getRecordSet();
          set.indexRecord(getPhoto().getPhotoPK().getId(), xmlFormShortName, indexEntry);
          SilverTrace.info("gallery", "GalleryIndexPhotoDataProcess.onSuccessful()",
              "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry.toString());
        } catch (final Exception e) {
          SilverTrace.info("gallery", "GalleryIndexPhotoDataProcess.onSuccessful()",
              "root.MSG_GEN_ENTER_METHOD", "xmlFormName = " + xmlFormName);
        }
      }

      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }
}
