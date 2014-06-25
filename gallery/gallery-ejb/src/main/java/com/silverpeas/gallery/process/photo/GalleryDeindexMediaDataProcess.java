package com.silverpeas.gallery.process.photo;

import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.process.AbstractGalleryDataProcess;
import com.silverpeas.gallery.process.GalleryProcessExecutionContext;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.process.session.ProcessSession;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;

public class GalleryDeindexMediaDataProcess extends AbstractGalleryDataProcess {

  /**
   * Process to deindex a media in Database
   * @param media
   * @return
   */
  public static GalleryDeindexMediaDataProcess getInstance(final Media media) {
    return new GalleryDeindexMediaDataProcess(media);
  }

  /**
   * Default hidden conctructor
   * @param media
   */
  protected GalleryDeindexMediaDataProcess(final Media media) {
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
    super.onSuccessful();

    SilverTrace.info("gallery", "GalleryDeindexMediaDataProcess.onSuccessful()",
        "root.MSG_GEN_ENTER_METHOD", "MediaPK = " + getMedia().getMediaPK().toString());

    final IndexEntryPK indexEntry = new IndexEntryPK(getMedia().getMediaPK().getComponentName(),
        getMedia().getContributionType(), getMedia().getMediaPK().getId());

    IndexEngineProxy.removeIndexEntry(indexEntry);
  }
}
