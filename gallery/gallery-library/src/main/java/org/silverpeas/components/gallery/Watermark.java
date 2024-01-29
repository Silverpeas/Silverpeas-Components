package org.silverpeas.components.gallery;

import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static org.apache.commons.io.FilenameUtils.*;
import static org.silverpeas.core.util.HttpUtil.httpClient;
import static org.silverpeas.core.util.HttpUtil.toUrl;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

public class Watermark {

  private boolean enabled = false;

  private String propertyIPTCForHD;

  private String propertyIPTCForThumbnails;

  private String textForHD;

  private String textForThumbnails;

  private File imageForHD;

  private File imageForThumbnails;

  boolean isEnabled() {
    return enabled;
  }

  void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  String getIPTCPropertyForHD() {
    return propertyIPTCForHD;
  }

  void setIPTCPropertyForHD(final String IPTCPropertyForHD) {
    this.propertyIPTCForHD = IPTCPropertyForHD;
  }

  String getIPTCPropertyForThumbnails() {
    return propertyIPTCForThumbnails;
  }

  void setIPTCPropertyForThumbnails(final String IPTCPropertyForThumbnails) {
    this.propertyIPTCForThumbnails = IPTCPropertyForThumbnails;
  }

  String getTextForHD() {
    return textForHD;
  }

  void setTextForHD(final String textForHD) {
    this.textForHD = textForHD;
  }

  String getTextForThumbnails() {
    return textForThumbnails;
  }

  void setTextForThumbnails(final String textForThumbnails) {
    this.textForThumbnails = textForThumbnails;
  }

  File getImageForHD() {
    return imageForHD;
  }

  synchronized void setImageUrlForHD(String componentInstanceId, final String imageUrlForHD) {
    this.imageForHD = processImageUrl(componentInstanceId, imageUrlForHD);
  }

  File getImageForThumbnails() {
    return imageForThumbnails;
  }

  synchronized void setImageUrlForThumbnails(String componentInstanceId,
      final String imageUrlForThumbnails) {
    this.imageForThumbnails = processImageUrl(componentInstanceId, imageUrlForThumbnails);
  }

  private synchronized File processImageUrl(String componentInstanceId, final String imageUrl) {
    File cachedFile = null;
    if (isDefined(imageUrl)) {
      final String imageUrlWithoutProtocol =
          imageUrl.replaceAll("(file|https?):/+", "").replaceAll("[&=%!;*?]", "").replaceAll(":([0-9]+)", "$1");
      final String normalizedName = normalize(imageUrlWithoutProtocol.replaceAll("[/\\\\:]", ""));
      final String extension = getExtension(normalizedName);
      final Path cachedPath = Paths.get(FileRepositoryManager.getTemporaryPath(),
          componentInstanceId + "_watermark_" + getBaseName(normalizedName) + "." +
              (isDefined(extension) ? extension : "png"));
      cachedFile = cachedPath.toFile();
      if (!cachedFile.exists()) {
        cachedFile = cacheImage(imageUrl, cachedFile, imageUrlWithoutProtocol, cachedPath);
      }
    }
    return cachedFile;
  }

  private File cacheImage(final String imageUrl, File cachedFile,
      final String imageUrlWithoutProtocol, final Path cachedPath) {
    final Path watermarkSource = Paths.get(imageUrlWithoutProtocol);
    if (watermarkSource.toFile().exists()) {
      try {
        Files.copy(watermarkSource, cachedPath);
      } catch (IOException e) {
        cachedFile = null;
        SilverLogger.getLogger(this).warn(e);
        SilverLogger.getLogger(this).warn("impossible to save image from URL {0}", imageUrl);
      }
    } else {
      try {
        final HttpResponse<InputStream> response =  httpClient().send(toUrl(imageUrl)
            .header("Accept", MediaType.WILDCARD)
            .build(), ofInputStream());
        try (final InputStream body = response.body()) {
          Files.copy(body, cachedPath);
        }
      } catch (Exception e) {
        cachedFile = null;
        SilverLogger.getLogger(this).warn(e);
        SilverLogger.getLogger(this).warn("impossible to save image from URL {0}", imageUrl);
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
      }
    }
    return cachedFile;
  }

  boolean isBasedOnIPTC() {
    return isDefined(getIPTCPropertyForHD()) ||
        isDefined(getIPTCPropertyForThumbnails());
  }

  boolean isDefinedForThumbnails() {
    return isDefined(getIPTCPropertyForThumbnails()) ||
        isDefined(getTextForThumbnails()) || getImageForThumbnails() != null;
  }
}

