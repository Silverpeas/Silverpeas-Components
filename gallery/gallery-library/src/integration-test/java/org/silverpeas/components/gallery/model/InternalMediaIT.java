/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.components.gallery.model;

import org.apache.commons.lang3.time.DateUtils;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.gallery.GalleryWarBuilder;
import org.silverpeas.components.gallery.constant.MediaMimeType;
import org.silverpeas.components.gallery.constant.MediaType;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.util.DateUtil;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(Arquillian.class)
public class InternalMediaIT {

  @Deployment
  public static Archive<?> createTestArchive() {
    return GalleryWarBuilder.onWarForTestClass(InternalMediaIT.class).build();
  }

  private Date beginDownloadDate = DateUtils.addDays(DateUtil.getDate(), -50);
  private Date endDownloadDate = DateUtils.addDays(DateUtil.getDate(), 50);

  @Test
  public void justInstancedTest() {
    InternalMedia iMedia = new InternalMediaForTest();
    assertThat(iMedia.getFileName(), nullValue());
    assertThat(iMedia.getFileSize(), is(0L));
    assertThat(iMedia.getFileMimeType(), Matchers.is(MediaMimeType.ERROR));
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
    Date dateOfDay = DateUtil.getDate();
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

    dateOfDay = DateUtils.addSeconds(DateUtil.getDate(), 1);
    iMedia.setDownloadPeriod(Period.from(beginDownloadDate, dateOfDay));

    assertThat(iMedia.isDownloadAuthorized(), is(true));
    assertThat(iMedia.isVisible(dateOfDay), is(true));
    assertThat(iMedia.getDownloadPeriod().getBeginDatable(), comparesEqualTo(beginDownloadDate));
    assertThat(iMedia.getDownloadPeriod().getEndDatable(), comparesEqualTo(dateOfDay));
    assertThat(iMedia.isDownloadable(), is(true));

    dateOfDay = DateUtils.addSeconds(DateUtil.getDate(), -1);
    iMedia.setDownloadPeriod(Period.from(beginDownloadDate, dateOfDay));

    assertThat(iMedia.isDownloadAuthorized(), is(true));
    assertThat(iMedia.isVisible(dateOfDay), is(true));
    assertThat(iMedia.getDownloadPeriod().getBeginDatable(), comparesEqualTo(beginDownloadDate));
    assertThat(iMedia.getDownloadPeriod().getEndDatable(), comparesEqualTo(dateOfDay));
    assertThat(iMedia.isDownloadable(), is(false));

    dateOfDay = DateUtils.addSeconds(DateUtil.getDate(), -1);
    iMedia.setDownloadPeriod(Period.from(dateOfDay, endDownloadDate));

    assertThat(iMedia.isDownloadAuthorized(), is(true));
    assertThat(iMedia.isVisible(dateOfDay), is(true));
    assertThat(iMedia.getDownloadPeriod().getBeginDatable(), comparesEqualTo(dateOfDay));
    assertThat(iMedia.getDownloadPeriod().getEndDatable(), comparesEqualTo(endDownloadDate));
    assertThat(iMedia.isDownloadable(), is(true));

    dateOfDay = DateUtils.addSeconds(DateUtil.getDate(), 1);
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
    iMedia.setFileMimeType(MediaMimeType.JPG);
    iMedia.setDownloadAuthorized(true);
    iMedia.setDownloadPeriod(Period.from(beginDownloadDate, endDownloadDate));
    assertDefaultInternalMedia(iMedia);
    return iMedia;
  }

  private void assertDefaultInternalMedia(InternalMedia iMedia) {
    assertThat(iMedia.getFileName(), is("/FileName"));
    assertThat(iMedia.getFileSize(), is(1024L));
    assertThat(iMedia.getFileMimeType(), is(MediaMimeType.JPG));
    assertThat(iMedia.isDownloadAuthorized(), is(true));
    assertThat(iMedia.getDownloadPeriod(), not(sameInstance(Period.UNDEFINED)));
    assertThat(iMedia.getDownloadPeriod().getBeginDatable(), comparesEqualTo(beginDownloadDate));
    assertThat(iMedia.getDownloadPeriod().getEndDatable(), comparesEqualTo(endDownloadDate));
  }

  private class InternalMediaForTest extends InternalMedia {
    private static final long serialVersionUID = -5052581924414692298L;

    public InternalMediaForTest() {
      super();
    }

    public InternalMediaForTest(final InternalMediaForTest other) {
      super(other);
    }

    @Override
    public MediaType getType() {
      return MediaType.Unknown;
    }

    @Override
    public Media getCopy() {
      return new InternalMediaForTest(this);
    }
  }
}