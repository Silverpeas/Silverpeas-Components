package org.silverpeas.components.gallery;

import org.silverpeas.core.util.StringUtil;

public class Watermark {

  private boolean enabled = false;

  private String propertyIPTCForHD;

  private String propertyIPTCForThumbnails;

  private String textForHD;

  private String textForThumbnails;

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

  boolean isBasedOnIPTC() {
    return StringUtil.isDefined(getIPTCPropertyForHD()) ||
        StringUtil.isDefined(getIPTCPropertyForThumbnails());
  }

  boolean isDefinedForThumbnails() {
    return StringUtil.isDefined(getIPTCPropertyForThumbnails()) ||
        StringUtil.isDefined(getTextForThumbnails());
  }
}

