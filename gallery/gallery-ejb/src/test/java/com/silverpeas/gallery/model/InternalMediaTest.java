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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.model;

import com.silverpeas.gallery.constant.MediaType;
import com.stratelia.webactiv.util.DateUtil;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.silverpeas.date.Period;

import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class InternalMediaTest {

  private Date beginDownloadDate = DateUtils.addDays(DateUtil.getNow(), -50);
  private Date endDownloadDate = DateUtils.addDays(DateUtil.getNow(), 50);

  @Test
  public void justInstancedTest() {
    InternalMedia iMedia = new InternalMediaForTest();
    assertThat(iMedia.getFileName(), nullValue());
    assertThat(iMedia.getFileSize(), is(0L));
    assertThat(iMedia.getFileMimeType(), nullValue());
    assertThat(iMedia.isDownloadAuthorized(), is(false));
    assertThat(iMedia.getDownloadPeriod(), sameInstance(Period.UNDEFINED));
  }

  @Test
  public void justCreatedTest() {
    InternalMedia iMedia = defaultInternalMedia();
    assertDefaultInternalMedia(iMedia);
  }

  @Test
  public void isDownloadable() {
    Date dateOfDay = DateUtil.getNow();
    InternalMedia iMedia = defaultInternalMedia();

    assertThat(iMedia.isDownloadAuthorized(), is(true));
    assertThat(iMedia.isVisible(dateOfDay), is(true));
    assertThat(iMedia.getDownloadPeriod().getBeginDatable(), comparesEqualTo(beginDownloadDate));
    assertThat(iMedia.getDownloadPeriod().getEndDatable(), comparesEqualTo(endDownloadDate));
    assertThat(iMedia.isDownloadable(), is(true));

    iMedia.setDownloadAuthorized(false);

    assertThat(iMedia.isDownloadAuthorized(), is(false));
    assertThat(iMedia.isVisible(dateOfDay), is(true));
    assertThat(iMedia.getDownloadPeriod().getBeginDatable(), comparesEqualTo(beginDownloadDate));
    assertThat(iMedia.getDownloadPeriod().getEndDatable(), comparesEqualTo(endDownloadDate));
    assertThat(iMedia.isDownloadable(), is(false));

    iMedia.setDownloadAuthorized(true);
    iMedia.setVisibilityPeriod(Period.from(beginDownloadDate, beginDownloadDate));

    assertThat(iMedia.isDownloadAuthorized(), is(true));
    assertThat(iMedia.isVisible(dateOfDay), is(false));
    assertThat(iMedia.getDownloadPeriod().getBeginDatable(), comparesEqualTo(beginDownloadDate));
    assertThat(iMedia.getDownloadPeriod().getEndDatable(), comparesEqualTo(endDownloadDate));
    assertThat(iMedia.isDownloadable(), is(false));

    iMedia.setVisibilityPeriod(Period.UNDEFINED);

    assertThat(iMedia.isDownloadAuthorized(), is(true));
    assertThat(iMedia.isVisible(dateOfDay), is(true));
    assertThat(iMedia.getDownloadPeriod().getBeginDatable(), comparesEqualTo(beginDownloadDate));
    assertThat(iMedia.getDownloadPeriod().getEndDatable(), comparesEqualTo(endDownloadDate));
    assertThat(iMedia.isDownloadable(), is(true));

    dateOfDay = DateUtils.addSeconds(DateUtil.getNow(), 1);
    iMedia.setDownloadPeriod(Period.from(beginDownloadDate, dateOfDay));

    assertThat(iMedia.isDownloadAuthorized(), is(true));
    assertThat(iMedia.isVisible(dateOfDay), is(true));
    assertThat(iMedia.getDownloadPeriod().getBeginDatable(), comparesEqualTo(beginDownloadDate));
    assertThat(iMedia.getDownloadPeriod().getEndDatable(), comparesEqualTo(dateOfDay));
    assertThat(iMedia.isDownloadable(), is(true));

    dateOfDay = DateUtils.addSeconds(DateUtil.getNow(), -1);
    iMedia.setDownloadPeriod(Period.from(beginDownloadDate, dateOfDay));

    assertThat(iMedia.isDownloadAuthorized(), is(true));
    assertThat(iMedia.isVisible(dateOfDay), is(true));
    assertThat(iMedia.getDownloadPeriod().getBeginDatable(), comparesEqualTo(beginDownloadDate));
    assertThat(iMedia.getDownloadPeriod().getEndDatable(), comparesEqualTo(dateOfDay));
    assertThat(iMedia.isDownloadable(), is(false));

    dateOfDay = DateUtils.addSeconds(DateUtil.getNow(), -1);
    iMedia.setDownloadPeriod(Period.from(dateOfDay, endDownloadDate));

    assertThat(iMedia.isDownloadAuthorized(), is(true));
    assertThat(iMedia.isVisible(dateOfDay), is(true));
    assertThat(iMedia.getDownloadPeriod().getBeginDatable(), comparesEqualTo(dateOfDay));
    assertThat(iMedia.getDownloadPeriod().getEndDatable(), comparesEqualTo(endDownloadDate));
    assertThat(iMedia.isDownloadable(), is(true));

    dateOfDay = DateUtils.addSeconds(DateUtil.getNow(), 1);
    iMedia.setDownloadPeriod(Period.from(dateOfDay, endDownloadDate));

    assertThat(iMedia.isDownloadAuthorized(), is(true));
    assertThat(iMedia.isVisible(dateOfDay), is(true));
    assertThat(iMedia.getDownloadPeriod().getBeginDatable(), comparesEqualTo(dateOfDay));
    assertThat(iMedia.getDownloadPeriod().getEndDatable(), comparesEqualTo(endDownloadDate));
    assertThat(iMedia.isDownloadable(), is(false));
  }

  private InternalMedia defaultInternalMedia() {
    InternalMedia iMedia = new InternalMediaForTest();
    iMedia.setFileName("/FileName");
    iMedia.setFileSize(1024);
    iMedia.setFileMimeType("image/jpeg");
    iMedia.setDownloadAuthorized(true);
    iMedia.setDownloadPeriod(Period.from(beginDownloadDate, endDownloadDate));
    assertDefaultInternalMedia(iMedia);
    return iMedia;
  }

  private void assertDefaultInternalMedia(InternalMedia iMedia) {
    assertThat(iMedia.getFileName(), is("/FileName"));
    assertThat(iMedia.getFileSize(), is(1024L));
    assertThat(iMedia.getFileMimeType(), is("image/jpeg"));
    assertThat(iMedia.isDownloadAuthorized(), is(true));
    assertThat(iMedia.getDownloadPeriod(), not(sameInstance(Period.UNDEFINED)));
    assertThat(iMedia.getDownloadPeriod().getBeginDatable(), comparesEqualTo(beginDownloadDate));
    assertThat(iMedia.getDownloadPeriod().getEndDatable(), comparesEqualTo(endDownloadDate));
  }

  private class InternalMediaForTest extends InternalMedia {
    private static final long serialVersionUID = -5052581924414692298L;

    @Override
    public MediaType getType() {
      return MediaType.Unknown;
    }
  }
}