package org.silverpeas.components.webpages.notification;

import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.URLUtil;

import java.util.List;

abstract class AbstractWebPagesNotification extends AbstractTemplateUserNotificationBuilder<NodePK>{

  private final User user;
  private final String pageName;

  public AbstractWebPagesNotification(final NodePK resource, final User user) {
    super(resource);
    this.user = user;
    this.pageName = OrganizationControllerProvider.
        getOrganisationController().getComponentInstLight(getComponentInstanceId()).getLabel();
  }

  @Override
  protected void performTemplateData(final String language, final NodePK resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getTitle(language), "");
    template.setAttribute("path", "");
    template.setAttribute("senderName", user.getDisplayedName());
    template.setAttribute("pageName", pageName);
  }

  @Override
  protected void performNotificationResource(final String language, final NodePK resource,
      final NotificationResourceData notificationResourceData) {
    // The resource name corresponds at the label of the instantiated application
    notificationResourceData.setResourceName(pageName);
    notificationResourceData.setResourceId(resource.getId());
    notificationResourceData.setResourceType(getTemplatePath());
    // Exceptionally the resource location is builded at this level
    // Normally, the location is builded by the delayed notification mechanism
    notificationResourceData.setResourceLocation(buildResourceLocation());
  }

  /**
   * Builds the specific location
   * @return the specific location
   */
  private String buildResourceLocation() {
    final StringBuilder sb = new StringBuilder();
    AdminController adminController = ServiceProvider.getService(AdminController.class);
    final List<SpaceInstLight> spaces =
        adminController.getPathToComponent(getComponentInstanceId());
    for (final SpaceInstLight space : spaces) {
      if (sb.length() > 0) {
        sb.append(NotificationResourceData.LOCATION_SEPARATOR);
      }
      sb.append(space.getName());
    }
    return sb.toString();
  }

  @Override
  protected String getResourceURL(final NodePK resource) {
    return URLUtil.getURL(null, null, resource.getInstanceId()) + "Main";
  }

  @Override
  protected String getSender() {
    return user.getId();
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.webpages.multilang.webPagesBundle";
  }

  @Override
  protected String getTemplatePath() {
    return "webpages";
  }

  @Override
  protected String getComponentInstanceId() {
    return getResource().getInstanceId();
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "webPages.notifWebPageLinkLabel";
  }

}