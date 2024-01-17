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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.service.DefaultOrganizationController;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.test.integration.DataSetTest;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.test.integration.rule.DbSetupRule.getActualDataSet;

/**
 * Base class for tests in the gallery component.
 * It prepares the database to use in tests.
 */
@RunWith(Arquillian.class)
public abstract class BaseGalleryIT extends DataSetTest {

    private static final String TABLE_CREATION_SCRIPT =
            "/org/silverpeas/components/gallery/dao/create-media-database.sql";
    private static final String DATASET_XML_SCRIPT =
            "/org/silverpeas/components/gallery/dao/media_dataset.xml";

    protected static final String GALLERY0 = "gallery25";
    protected static final String GALLERY1 = "gallery26";
    protected static final String GALLERY2 = "gallery27";

    protected final static String INSTANCE_A = "instanceId_A";
    protected final static Date TODAY = java.sql.Date.valueOf("2014-03-01");
    protected final static Date CREATE_DATE = Timestamp.valueOf("2014-06-01 16:38:52.253");
    protected final static Date LAST_UPDATE_DATE = Timestamp.valueOf("2014-06-03 11:20:42.637");

    protected final static int MEDIA_ROW_COUNT = 12;
    protected final static int MEDIA_INTERNAL_ROW_COUNT = 9;
    protected final static int MEDIA_PHOTO_ROW_COUNT = 3;
    protected final static int MEDIA_VIDEO_ROW_COUNT = 3;
    protected final static int MEDIA_SOUND_ROW_COUNT = 3;
    protected final static int MEDIA_STREAMING_ROW_COUNT = 3;
    protected final static int MEDIA_PATH_ROW_COUNT = 13;
    protected final static int MEDIA_ORDER_ROW_COUNT = 3;
    protected final static int MEDIA_ORDER_DETAIL_ROW_COUNT = 6;

    protected UserDetail adminAccessUser;
    protected UserDetail publisherUser;
    protected UserDetail writerUser;
    protected UserDetail userUser;
    private static TimeZone defaultTimeZone;

    @Deployment
    public static Archive<?> createTestArchive() {
        return GalleryWarBuilder.onWarForTestClass(BaseGalleryIT.class)
                .addClasses(StubbedOrganizationController.class)
                .addAsResource(TABLE_CREATION_SCRIPT.substring(1))
                .addAsResource(DATASET_XML_SCRIPT.substring(1)).build();
    }

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    protected StubbedOrganizationController organizationController;

    @Override
    protected String getDbSetupTableCreationSqlScript() {
        return TABLE_CREATION_SCRIPT;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected String getDbSetupInitializations() {
        return DATASET_XML_SCRIPT;
    }

    @BeforeClass
    public static void setUpTimeZone() {
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
    }

    @Before
    public void setUp() throws Exception {
        verifyDataBeforeTest();

        adminAccessUser = new UserDetail();
        adminAccessUser.setId("adminUserId");
        adminAccessUser.setLastName("adminUserName");
        adminAccessUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
        publisherUser = new UserDetail();
        publisherUser.setId("publisherUserId");
        publisherUser.setLastName("publisherUserName");
        writerUser = new UserDetail();
        writerUser.setId("writerUserId");
        writerUser.setLastName("writerUserName");
        userUser = new UserDetail();
        userUser.setId("userUserId");
        userUser.setLastName("userUserName");

        organizationController.addUserDetailForTest(adminAccessUser)
                .addUserDetailForTest(publisherUser, SilverpeasRole.READER, SilverpeasRole.PUBLISHER)
                .addUserDetailForTest(writerUser, SilverpeasRole.READER, SilverpeasRole.WRITER)
                .addUserDetailForTest(userUser, SilverpeasRole.READER);
    }

    @AfterClass
    public static void restoreTimeZone() {
        TimeZone.setDefault(defaultTimeZone);
    }

    @After
    public void tearDown() {
        CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
    }

    /**
     * Verifying the data before a test.
     */
    protected void verifyDataBeforeTest() throws Exception {
        try (Connection connection = getConnection()) {
            IDataSet actualDataSet = getActualDataSet(connection);
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
            ITable pathTable = actualDataSet.getTable("SC_Gallery_Path");
            assertThat(pathTable.getRowCount(), is(MEDIA_PATH_ROW_COUNT));
            ITable orderTable = actualDataSet.getTable("SC_Gallery_Order");
            assertThat(orderTable.getRowCount(), is(MEDIA_ORDER_ROW_COUNT));
            ITable orderDetailTable = actualDataSet.getTable("SC_Gallery_OrderDetail");
            assertThat(orderDetailTable.getRowCount(), is(MEDIA_ORDER_DETAIL_ROW_COUNT));
        }
    }

    /**
     * @author Yohann Chastagnier
     */
    @Service
    @Singleton
    @Alternative
    @Priority(APPLICATION + 10)
    public static class StubbedOrganizationController extends DefaultOrganizationController {

        private final Map<UserDetail, List<String>> users = new HashMap<>();
        protected final List<String> instanceIds = List.of(INSTANCE_A, GALLERY0, GALLERY1, GALLERY2);

        public StubbedOrganizationController addUserDetailForTest(final UserDetail user, final SilverpeasRole... roles) {
            List<String> profiles = this.users.computeIfAbsent(user, k -> new ArrayList<>());
            Stream.of(roles)
                    .map(SilverpeasRole::getName)
                    .forEach(profiles::add);
            return this;
        }

        @Override
        public UserDetail getUserDetail(final String sUserId) {
            return users.keySet().stream().filter(u -> u.getId().equals(sUserId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public String[] getUserProfiles(final String userId, final String componentId) {
            if (instanceIds.contains(componentId)) {
                UserDetail user = getUserDetail(userId);
                return user == null ? new String[0] : users.get(user).toArray(new String[0]);
            }
            return new String[0];
        }
    }
}
