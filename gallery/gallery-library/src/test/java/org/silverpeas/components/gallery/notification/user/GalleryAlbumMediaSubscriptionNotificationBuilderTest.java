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

package org.silverpeas.components.gallery.notification.user;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.subscription.ResourceSubscriptionService;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.extension.LocalizationBundleStub;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestManagedMocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv(context = JEETestContext.class)
@TestManagedMocks({ComponentAccessControl.class})
class GalleryAlbumMediaSubscriptionNotificationBuilderTest {

  private static final String FR = LocalizationBundleStub.LANGUAGE_FR;
  private static final String EN = LocalizationBundleStub.LANGUAGE_EN;
  private static final String DE = LocalizationBundleStub.LANGUAGE_DE;
  private static final ComponentInstLight componentInstance = new ComponentInstLight();
  private static final AlbumDetail albumDetail = new AlbumDetail(new NodeDetail());
  private static final AlbumDetail parentAlbumDetail = new AlbumDetail(new NodeDetail());
  private static final NodePath albumPath = new NodePath();
  private static final User sender = mock(User.class);

  @RegisterExtension
  static LocalizationBundleStub galleryBundle = new LocalizationBundleStub(
      "org.silverpeas.gallery.multilang.galleryBundle", LocalizationBundleStub.LANGUAGE_ALL);

  @TestManagedMock
  private NodeAccessControl nodeAccessControl;
  @TestManagedMock
  private ResourceSubscriptionService subscriptionService;

  private Map<String, ResourceSubscriptionService> componentImplementations;
  private final SubscriptionSubscriberList subscriptionSubscribers = new SubscriptionSubscriberList();

  @BeforeAll
  static void setupStaticData() {
    componentInstance.setLocalId(38);
    componentInstance.setName("gallery");
    albumDetail.setId(26);
    albumDetail.getNodePK().setComponentName(componentInstance.getId());
    albumDetail.setName("B");
    albumPath.add(albumDetail);
    parentAlbumDetail.setId(25);
    parentAlbumDetail.getNodePK().setComponentName(componentInstance.getId());
    parentAlbumDetail.setName("A");
    albumPath.add(parentAlbumDetail);
    when(sender.getId()).thenReturn("1");

    galleryBundle.put(DE, "GML.st.notification.subject", "");
    galleryBundle.put(EN, "GML.st.notification.subject", "");
    galleryBundle.put(FR, "GML.st.notification.subject", "");
    galleryBundle.put(DE, "gallery.media.subscription.subject", "Subscription_DE");
    galleryBundle.put(EN, "gallery.media.subscription.subject", "Subscription_EN");
    galleryBundle.put(FR, "gallery.media.subscription.subject", "Subscription_FR");
    galleryBundle.put("gallery.album.link.label", "AlbumLink");
  }

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setup(@TestManagedMock OrganizationController organizationController,
      @TestManagedMock SilverpeasComponentInstanceProvider componentInstanceProvider,
      @TestManagedMock NodeService nodeService) throws Exception {
    final Optional<SilverpeasComponentInstance> optionalInstance = Optional.of(componentInstance);
    when(organizationController.getComponentInstLight(componentInstance.getId()))
        .thenReturn(componentInstance);
    when(organizationController.getComponentInstance(componentInstance.getId()))
        .thenReturn(optionalInstance);
    when(componentInstanceProvider.getById(componentInstance.getId())).thenReturn(optionalInstance);
    componentImplementations = (Map<String, ResourceSubscriptionService>) FieldUtils
        .readDeclaredStaticField(ResourceSubscriptionProvider.class, "componentImplementations",
            true);
    componentImplementations.put(componentInstance.getName(), subscriptionService);
    when(subscriptionService.getSubscribersOfSubscriptionResource(any(SubscriptionResource.class)))
        .thenReturn(subscriptionSubscribers);
    subscriptionSubscribers.add(UserSubscriptionSubscriber.from("1"));
    when(nodeService.getPath(albumDetail.getNodePK())).thenReturn(albumPath);

    when(nodeAccessControl.isUserAuthorized(anyString(), any(NodeDetail.class))).thenReturn(true);
    when(nodeAccessControl.isGroupAuthorized(anyString(), any(NodePK.class))).thenReturn(true);
  }

  @AfterEach
  void clear() {
    componentImplementations.clear();
    subscriptionSubscribers.clear();
  }

  @Test
  void oneMediaAdded() {
    final GalleryAlbumMediaSubscriptionNotificationBuilder builder =
        new GalleryAlbumMediaSubscriptionNotificationBuilder(
        albumDetail, sender);
    builder.aboutMedia(aMedia());
    final UserNotification userNotification = builder.build();
    final Map<String, String> titles = computeNotificationTitles(userNotification);
    assertThat(titles.get(DE), is("Subscription_DE"));
    assertThat(titles.get(EN), is("Subscription_EN"));
    assertThat(titles.get(FR), is("Subscription_FR"));
    final Map<String, String> contents = computeNotificationContents(userNotification);
    assertThat(contents.get(DE), is("Dem Album <b>A &gt; B</b> wurde ein Medium hinzugefügt."));
    assertThat(contents.get(EN), is("A media has been added into album <b>A &gt; B</b>."));
    assertThat(contents.get(FR), is("Un média a été ajouté dans l'album <b>A &gt; B</b>."));
  }

  @Test
  void severalMediaAdded() {
    final GalleryAlbumMediaSubscriptionNotificationBuilder builder =
        new GalleryAlbumMediaSubscriptionNotificationBuilder(
        albumDetail, sender);
    builder.aboutMedia(aMedia(), aMedia());
    final UserNotification userNotification = builder.build();
    final Map<String, String> titles = computeNotificationTitles(userNotification);
    assertThat(titles.get(DE), is("Subscription_DE"));
    assertThat(titles.get(EN), is("Subscription_EN"));
    assertThat(titles.get(FR), is("Subscription_FR"));
    final Map<String, String> contents = computeNotificationContents(userNotification);
    assertThat(contents.get(DE), is("Dem Album <b>A &gt; B</b> wurde Medien hinzugefügt."));
    assertThat(contents.get(EN), is("Media have been added into album <b>A &gt; B</b>."));
    assertThat(contents.get(FR), is("Des médias ont été ajoutés dans l'album <b>A &gt; B</b>."));
  }

  private Map<String, String> computeNotificationContents(UserNotification userNotification) {
    final Map<String, String> result = new HashMap<>();
    result.put(FR, getContent(userNotification, FR));
    result.put(EN, getContent(userNotification, EN));
    result.put(DE, getContent(userNotification, DE));
    assertThat(result.get(FR), not(is(result.get(EN))));
    assertThat(result.get(EN), not(is(result.get(DE))));
    return result;
  }

  private Map<String, String> computeNotificationTitles(UserNotification userNotification) {
    final Map<String, String> result = new HashMap<>();
    result.put(FR, getTitle(userNotification, FR));
    result.put(EN, getTitle(userNotification, EN));
    result.put(DE, getTitle(userNotification, DE));
    assertThat(result.get(FR), not(is(result.get(EN))));
    assertThat(result.get(EN), not(is(result.get(DE))));
    return result;
  }

  private String getContent(final UserNotification userNotification, final String language) {
    return userNotification.getNotificationMetaData().getContent(language).replaceAll(
        "<!--BEFORE_MESSAGE_FOOTER-->NOTIFIED_USERS_AND_GROUPS_PART_" + language.toUpperCase() +
            "<!--AFTER_MESSAGE_FOOTER-->", "");
  }

  private String getTitle(final UserNotification userNotification, final String language) {
    return userNotification.getNotificationMetaData().getTitle(language);
  }

  private Media aMedia() {
    return mock(Media.class);
  }
}