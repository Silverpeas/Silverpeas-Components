/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.quickinfo.model;

import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.clipboard.SKDException;
import org.silverpeas.core.clipboard.SilverpeasKeyData;
import org.silverpeas.core.index.indexing.model.IndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.annotation.Nonnull;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * Model representing a {@link News} into the context of the clipboard API.
 */
public class NewsSelection extends ClipboardSelection {
  private static final long serialVersionUID = 2873891019350833299L;

  private final News currentNews;
  public static final DataFlavor NewsFlavor = new DataFlavor(News.class, News.getResourceType());

  public NewsSelection(News news) {
    super();
    currentNews = news;
    super.addFlavor(NewsFlavor);
  }

  @Override
  @Nonnull
  public synchronized Object getTransferData(DataFlavor parFlavor)
      throws UnsupportedFlavorException {
    Object transferredData;
    try {
      transferredData = super.getTransferData(parFlavor);
    } catch (UnsupportedFlavorException e) {
      if (NewsFlavor.equals(parFlavor)) {
        transferredData = currentNews;
      } else {
        throw e;
      }
    }
    return transferredData;
  }

  @Override
  public IndexEntry getIndexEntry() {
    final NewsPK newsPK = currentNews.getPK();
    final IndexEntry indexEntry = new IndexEntry(new IndexEntryKey(newsPK.getComponentName(),
        currentNews.getContributionType(), newsPK.getId()));
    indexEntry.setTitle(currentNews.getName());
    return indexEntry;
  }

  @Override
  public SilverpeasKeyData getKeyData() {
    final SilverpeasKeyData keyData = new SilverpeasKeyData(currentNews.getId(),
        currentNews.getComponentInstanceId());
    keyData.setTitle(currentNews.getName());
    keyData.setAuthor(currentNews.getCreatorId());
    keyData.setCreationDate(currentNews.getCreationDate());
    keyData.setDesc(currentNews.getDescription());
    keyData.setType(currentNews.getContributionType());
    keyData.setLink(URLUtil.getSimpleURL(URLUtil.URL_PUBLI, currentNews.getPublicationId(),
        currentNews.getComponentInstanceId()));
    currentNews.getVisibility().getSpecificPeriod().ifPresent(v -> {
      try {
        if (!v.startsAtMinDate()) {
          keyData.setProperty("BEGINDATE", v.getStartDate().toString());
        }
        if (!v.endsAtMaxDate()) {
          keyData.setProperty("ENDDATE", v.getEndDate().toString());
        }
      } catch (SKDException e) {
        SilverLogger.getLogger(this).error(e);
      }
    });
    return keyData;
  }
}
