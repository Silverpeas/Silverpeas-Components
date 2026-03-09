package org.silverpeas.components.gallery;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.kernel.annotation.Technical;

import java.io.File;

/**
 * @author Yohann Chastagnier
 */
@Technical
@Bean
@Named
public class GalleryInstancePreDestruction implements ComponentInstancePreDestruction {

  @Inject
  private GalleryService mediaService;

  @Transactional
  @Override
  public void preDestroy(final String componentInstanceId) {
    mediaService.deleteAlbum(UserDetail.getCurrentRequester(), componentInstanceId,
            new NodePK(NodePK.ROOT_NODE_ID, componentInstanceId));
    FileUtil.deleteEmptyDir(new File(FileRepositoryManager.getAbsolutePath(componentInstanceId)));
  }
}
