package org.silverpeas.components.gallery;

import org.silverpeas.core.util.StringUtil;

public class Watermark {

  private boolean enabled = false;

  private String propertyIPTCForHD;

  private String propertyIPTCForThumbnails;

  private String textForHD;

  private String textForThumbnails;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public String getIPTCPropertyForHD() {
    return propertyIPTCForHD;
  }

  public void setIPTCPropertyForHD(final String IPTCPropertyForHD) {
    this.propertyIPTCForHD = IPTCPropertyForHD;
  }

  public String getIPTCPropertyForThumbnails() {
    return propertyIPTCForThumbnails;
  }

  public void setIPTCPropertyForThumbnails(final String IPTCPropertyForThumbnails) {
    this.propertyIPTCForThumbnails = IPTCPropertyForThumbnails;
  }

  public String getTextForHD() {
    return textForHD;
  }

  public void setTextForHD(final String textForHD) {
    this.textForHD = textForHD;
  }

  public String getTextForThumbnails() {
    return textForThumbnails;
  }

  public void setTextForThumbnails(final String textForThumbnails) {
    this.textForThumbnails = textForThumbnails;
  }

  public boolean isBasedOnIPTC() {
    return StringUtil.isDefined(getIPTCPropertyForHD()) ||
        StringUtil.isDefined(getIPTCPropertyForThumbnails());
  }

  public boolean isDefinedForThumbnails() {
    return StringUtil.isDefined(getIPTCPropertyForThumbnails()) ||
        StringUtil.isDefined(getTextForThumbnails());
  }
}

