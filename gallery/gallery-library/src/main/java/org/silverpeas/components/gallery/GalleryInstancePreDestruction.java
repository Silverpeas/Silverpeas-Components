package org.silverpeas.components.gallery;

import com.silverpeas.admin.components.ComponentInstancePreDestruction;
import org.silverpeas.components.gallery.service.MediaServiceProvider;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.node.model.NodePK;
import org.silverpeas.util.FileRepositoryManager;
import org.silverpeas.util.FileUtil;

import javax.inject.Named;
import javax.transaction.Transactional;
import java.io.File;

/**
 * @author Yohann Chastagnier
 */
@Named
public class GalleryInstancePreDestruction implements ComponentInstancePreDestruction {

  @Transactional
  @Override
  public void preDestroy(final String componentInstanceId) {
    MediaServiceProvider.getMediaService()
        .deleteAlbum(UserDetail.getCurrentRequester(), componentInstanceId,
            new NodePK(NodePK.ROOT_NODE_ID, componentInstanceId));
    FileUtil.deleteEmptyDir(new File(FileRepositoryManager.getAbsolutePath(componentInstanceId)));
  }
}
