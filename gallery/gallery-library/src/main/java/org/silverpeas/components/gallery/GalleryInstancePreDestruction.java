package org.silverpeas.components.gallery;

import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.components.gallery.service.MediaServiceProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.kernel.annotation.Technical;

import javax.inject.Named;
import javax.transaction.Transactional;
import java.io.File;

/**
 * @author Yohann Chastagnier
 */
@Technical
@Bean
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
