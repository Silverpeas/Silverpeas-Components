/*
 * Copyright (C) 2000 - 2017 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.model;

import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.components.gallery.constant.StreamingProvider;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.ws.rs.WebApplicationException;

/**
 * This class represents a Streaming.
 */
public class Streaming extends Media {
  private static final long serialVersionUID = 5772513957256327862L;

  private String homepageUrl = "";
  private StreamingProvider provider = StreamingProvider.unknown;

  @Override
  public MediaType getType() {
    return MediaType.Streaming;
  }

  /**
   * Gets the provider URL that permits to access to the video play.
   * @return the provider URL that permits to access to the video play.
   */
  public String getHomepageUrl() {
    return homepageUrl;
  }

  /**
   * Sets the provider URL that permits to access to the video play.
   * @param url the provider URL that permits to access to the video play.
   */
  public void setHomepageUrl(final String url) {
    this.homepageUrl = url;
  }

  /**
   * Gets the streaming provider.
   * @return the streaming provider.
   */
  public StreamingProvider getProvider() {
    return provider;
  }

  /**
   * Gets the streaming provider.
   * @param provider the streaming provider.
   */
  public void setProvider(final StreamingProvider provider) {
    this.provider = provider;
  }

  /**
   * The type of this resource
   * @return the same value returned by getContributionType()
   */
  public static String getResourceType() {
    return MediaType.Streaming.name();
  }

  @Override
  public String getApplicationOriginalUrl() {
    if (StringUtil.isNotDefined(getId())) {
      return super.getApplicationOriginalUrl();
    }
    return getApplicationThumbnailUrl(MediaResolution.PREVIEW);
  }

  @Override
  public SilverpeasFile getFile(final MediaResolution mediaResolution) {
    return SilverpeasFile.NO_FILE;
  }

  @Override
  public String getApplicationEmbedUrl(final MediaResolution mediaResolution) {
    return "";
  }

  /**
   * Gets OEMBED data as JSON string.<br/>
   * WARNING: performances can be altered when called from a list treatments as it performs an
   * HTTP request.
   * @return a JSON structure as string that represents oembed data.
   */
  public static String getJsonOembedAsString(String homepageUrl) {
    final String oembedUrl = StreamingProvider.getOembedUrl(homepageUrl);
    final HttpGet httpGet = new HttpGet(oembedUrl);
    httpGet.addHeader("Accept", "application/json");
    try (CloseableHttpClient httpClient = httpClient();
         CloseableHttpResponse response = httpClient.execute(httpGet)) {
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new WebApplicationException(response.getStatusLine().getStatusCode());
      }
      String jsonResponse = EntityUtils.toString(response.getEntity());
      for (StreamingProvider provider : StreamingProvider.values()) {
        jsonResponse = jsonResponse.replaceAll("(?i)" + provider.name(), provider.name());
      }
      return jsonResponse;
    } catch (WebApplicationException wae) {
      SilverLogger.getLogger(Streaming.class)
          .error("{0} -> HTTP ERROR {1}", oembedUrl, wae.getMessage());
      throw wae;
    } catch (Exception e) {
      SilverLogger.getLogger(Streaming.class).error("{0} -> {1}", oembedUrl, e.getMessage());
      throw new WebApplicationException(e);
    } finally {
      httpGet.releaseConnection();
    }
  }

  private static CloseableHttpClient httpClient() {
    HttpClientBuilder builder = HttpClients.custom();
    final String proxyHost = SystemWrapper.get().getProperty("http.proxyHost");
    final String proxyPort = SystemWrapper.get().getProperty("http.proxyPort");
    if (StringUtil.isDefined(proxyHost) && StringUtil.isInteger(proxyPort)) {
      builder.setProxy(new HttpHost(proxyHost, Integer.parseInt(proxyPort)));
    }
    return builder.build();
  }
}