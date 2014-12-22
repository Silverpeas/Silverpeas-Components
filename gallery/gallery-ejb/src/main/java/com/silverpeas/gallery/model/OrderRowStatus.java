/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package com.silverpeas.gallery.model;

/**
 *
 * @author ehugonnet
 */
public enum OrderRowStatus {

  DOWNLOAD_REFUSED("R"), DOWNLOAD_DONE("T"), DOWNLOAD_ALLOWED("D"), DOWNLOAD_WITH_WATERMARK("DW"),
  TO_BE_DECIDED("");
  private final String code;

  private OrderRowStatus(String code) {
    this.code = code;
  }

  public static OrderRowStatus getStatusFromDownloadDecision(String downloadDecision) {
    if (DOWNLOAD_REFUSED.code.equals(downloadDecision)) {
      return DOWNLOAD_REFUSED;
    }
    if (DOWNLOAD_DONE.code.equals(downloadDecision)) {
      return DOWNLOAD_DONE;
    }
    if (DOWNLOAD_ALLOWED.code.equals(downloadDecision)) {
      return DOWNLOAD_ALLOWED;
    }
    if (DOWNLOAD_WITH_WATERMARK.code.equals(downloadDecision)) {
      return DOWNLOAD_WITH_WATERMARK;
    }
    return TO_BE_DECIDED;
  }
}
