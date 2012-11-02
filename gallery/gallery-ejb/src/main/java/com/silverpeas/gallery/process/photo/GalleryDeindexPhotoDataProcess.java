package com.silverpeas.gallery.process.photo;

import org.silverpeas.process.session.ProcessSession;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;

import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.process.AbstractGalleryDataProcess;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class GalleryDeindexPhotoDataProcess extends AbstractGalleryDataProcess {

  /**
   * Process to deindex a photo in Database
   * @param photo
   * @return
   */
  public static GalleryDeindexPhotoDataProcess getInstance(final PhotoDetail photo) {
    return new GalleryDeindexPhotoDataProcess(photo);
  }

  /**
   * Default hidden conctructor
   * @param photo
   */
  protected GalleryDeindexPhotoDataProcess(final PhotoDetail photo) {
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
    super.onSuccessful();

    SilverTrace.info("gallery", "GalleryBmEJB.deleteIndex()", "root.MSG_GEN_ENTER_METHOD",
        "PhotoPK = " + getPhoto().getPhotoPK().toString());

    final IndexEntryPK indexEntry =
        new IndexEntryPK(getPhoto().getPhotoPK().getComponentName(), "Photo", getPhoto()
            .getPhotoPK().getId());

    IndexEngineProxy.removeIndexEntry(indexEntry);
  }
}
