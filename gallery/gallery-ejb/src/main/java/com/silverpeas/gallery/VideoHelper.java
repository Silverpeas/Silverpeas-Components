package com.silverpeas.gallery;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.io.file.HandledFile;

import com.silverpeas.gallery.constant.VideoFormat;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.Video;
import com.silverpeas.util.FileUtil;

public class VideoHelper {

  /**
   * Save uploaded video file on file system
   * @param fileHandler
   * @param fileItem the current uploaded video
   * @param video the current video media
   * @throws Exception
   */
  public static void processVideoFile(final FileHandler fileHandler, final FileItem fileItem,
      Video video) throws Exception {
    if (fileItem != null && fileItem.getName() != null) {
      String name = FileUtil.getFilename(fileItem.getName());
      if (VideoFormat.isVideo(name)) {
        final String subDirectory = video.getType().getTechnicalFolder();
        final HandledFile handledVideoFile = fileHandler
            .getHandledFile(Media.BASE_PATH, video.getMediaPK().getInstanceId(),
                subDirectory + video.getMediaPK().getId(), name);
        handledVideoFile.writeByteArrayToFile(fileItem.get());
        video.setFileName(name);
        video.setFileMimeType(fileItem.getContentType());
        video.setFileSize(fileItem.getSize());
      }
    }
  }
}
