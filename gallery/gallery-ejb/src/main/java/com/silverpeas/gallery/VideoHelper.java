package com.silverpeas.gallery;

import com.silverpeas.gallery.constant.VideoFormat;
import com.silverpeas.gallery.model.Video;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.silverpeas.process.io.file.FileBasePath;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.io.file.HandledFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class VideoHelper {
  private final static FileBasePath BASE_PATH = FileBasePath.UPLOAD_PATH;

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
            .getHandledFile(BASE_PATH, video.getMediaPK().getInstanceId(),
                subDirectory + video.getMediaPK().getId(), name);
        handledVideoFile.writeByteArrayToFile(fileItem.get());
        video.setFileName(name);
        video.setFileMimeType(fileItem.getContentType());
        video.setFileSize(fileItem.getSize());
      }
    }
  }

  /**
   * Open an output stream of a video according to given details of a {@link Video}.
   * @param video
   * @return
   * @throws java.io.IOException
   */
  public static InputStream openInputStream(final Video video) {
    final String videoId = video.getMediaPK().getId();
    final String instanceId = video.getMediaPK().getInstanceId();
    if (StringUtil.isDefined(videoId) && StringUtil.isDefined(instanceId)) {
      try {
        return FileUtils.openInputStream(FileUtils
            .getFile(new File(BASE_PATH.getPath()), instanceId, video.getWorkspaceSubFolderName(),
                video.getFileName()));
      } catch (IOException e) {
        SilverTrace.error("gallery", "ImageHelper.getBytes", "gallery.ERR_CANT_GET_IMAGE_BYTES",
            "image = " + video.getTitle() + " (#" + video.getId() + ")");
        return null;
      }
    }
    return null;
  }
}
