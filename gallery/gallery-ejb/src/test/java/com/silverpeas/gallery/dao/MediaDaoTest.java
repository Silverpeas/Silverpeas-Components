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
package com.silverpeas.gallery.dao;

import com.silverpeas.gallery.BaseGalleryTest;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.Test;

import java.sql.Connection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * This class of unit tests has been written during Entity and SGBD model migration.
 */
public class MediaDaoTest extends BaseGalleryTest {

  private final static int MEDIA_ROW_COUNT = 12;
  private final static int MEDIA_INTERNAL_ROW_COUNT = 9;
  private final static int MEDIA_PHOTO_ROW_COUNT = 3;
  private final static int MEDIA_VIDEO_ROW_COUNT = 3;
  private final static int MEDIA_SOUND_ROW_COUNT = 3;
  private final static int MEDIA_STREAMING_ROW_COUNT = 3;

  @Override
  public String[] getApplicationContextPath() {
    return new String[]{"/spring-media-embbed-datasource.xml"};
  }

  @Override
  public String getDataSetPath() {
    return "com/silverpeas/gallery/dao/media_dataset.xml";
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    verifyDataBeforeTest();
  }

  @Test
  public void getAllMedia() throws Exception {
    performDAOTest(new DAOTest() {
      @Override
      public void test(final Connection connection) {

      }
    });
  }

  /**
   * Verifying the data before a test.
   */
  private void verifyDataBeforeTest() throws Exception {
    IDataSet actualDataSet = getActualDataSet();
    ITable mediaTable = actualDataSet.getTable("SC_Gallery_Media");
    assertThat(mediaTable.getRowCount(), is(MEDIA_ROW_COUNT));
    ITable internalTable = actualDataSet.getTable("SC_Gallery_Internal");
    assertThat(internalTable.getRowCount(), is(MEDIA_INTERNAL_ROW_COUNT));
    ITable photoTable = actualDataSet.getTable("SC_Gallery_Photo");
    assertThat(photoTable.getRowCount(), is(MEDIA_PHOTO_ROW_COUNT));
    ITable videoTable = actualDataSet.getTable("SC_Gallery_Video");
    assertThat(videoTable.getRowCount(), is(MEDIA_VIDEO_ROW_COUNT));
    ITable soundTable = actualDataSet.getTable("SC_Gallery_Sound");
    assertThat(soundTable.getRowCount(), is(MEDIA_SOUND_ROW_COUNT));
    ITable streamingTable = actualDataSet.getTable("SC_Gallery_Streaming");
    assertThat(streamingTable.getRowCount(), is(MEDIA_STREAMING_ROW_COUNT));
  }
}
