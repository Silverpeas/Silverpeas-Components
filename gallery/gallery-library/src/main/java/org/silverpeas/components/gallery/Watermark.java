package org.silverpeas.components.gallery;

import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.kernel.logging.SilverLogger;

import org.apache.commons.io.input.BoundedInputStream;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static org.apache.commons.io.FilenameUtils.*;
import static org.silverpeas.core.util.HttpUtil.httpClientBuilder;
import static org.silverpeas.core.util.HttpUtil.toUrl;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

public class Watermark {

  private static final Duration FETCH_TIMEOUT = Duration.ofSeconds(10);
  private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;

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
    } else if (!isFetchable(imageUrl)) {
      cachedFile = null;
      SilverLogger.getLogger(this).warn("refused to fetch the watermark image from URL {0}",
          imageUrl);
    } else {
      try {
        final HttpClient client = httpClientBuilder()
            // a redirection would escape the verification performed by isFetchable above
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(FETCH_TIMEOUT)
            .build();
        final HttpResponse<InputStream> response = client.send(toUrl(imageUrl)
            .timeout(FETCH_TIMEOUT)
            .header("Accept", MediaType.WILDCARD)
            .build(), ofInputStream());
        try (final InputStream body = new BoundedInputStream(response.body(), MAX_IMAGE_SIZE)) {
          Files.copy(body, cachedPath);
        }
        if (Files.size(cachedPath) >= MAX_IMAGE_SIZE) {
          Files.deleteIfExists(cachedPath);
          cachedFile = null;
          SilverLogger.getLogger(this)
              .warn("watermark image from URL {0} exceeds {1} bytes", imageUrl, MAX_IMAGE_SIZE);
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

  /**
   * Can the image be fetched from the given URL? Only the HTTP and HTTPS schemes are handled, and
   * the host must resolve to neither a loopback nor a link-local address, so that a watermark URL
   * cannot be used to reach the services the server deliberately keeps for itself nor the metadata
   * endpoint of a cloud provider. The addresses of the private networks of an organization are
   * deliberately allowed: they are where the internal resources of an intranet legitimately live,
   * and no address range can tell them from the internal services one would rather protect. Only
   * an explicit list of allowed hosts could, which is left to a further decision.
   * @param imageUrl the URL set as an instance parameter.
   * @return true if the URL can be requested, false otherwise.
   */
  static boolean isFetchable(final String imageUrl) {
    try {
      final URI uri = URI.create(imageUrl);
      final String scheme = uri.getScheme();
      if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
        return false;
      }
      final String host = uri.getHost();
      if (host == null) {
        return false;
      }
      // every address the host resolves to is verified, otherwise a host resolving to a forbidden
      // address beside an allowed one would go through
      for (final InetAddress address : InetAddress.getAllByName(host)) {
        if (address.isLoopbackAddress() || address.isLinkLocalAddress() ||
            address.isAnyLocalAddress() || address.isMulticastAddress()) {
          return false;
        }
      }
      return true;
    } catch (IllegalArgumentException | UnknownHostException e) {
      SilverLogger.getLogger(Watermark.class).warn(e);
      return false;
    }
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

