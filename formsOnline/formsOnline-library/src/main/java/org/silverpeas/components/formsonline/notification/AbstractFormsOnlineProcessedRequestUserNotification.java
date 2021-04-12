package org.silverpeas.components.formsonline.notification;

import org.silverpeas.components.formsonline.model.FormInstance;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.template.SilverpeasTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;

public abstract class AbstractFormsOnlineProcessedRequestUserNotification
    extends FormsOnlineValidationRequestUserNotification {

  private List<String> usersToBeNotified = new ArrayList<>();
  private List<String> groupsToBeNotified = new ArrayList<>();

  protected AbstractFormsOnlineProcessedRequestUserNotification(final FormInstance resource,
      final NotifAction action) {
    super(resource, action);
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return usersToBeNotified;
  }

  @Override
  protected Collection<String> getGroupIdsToNotify() {
    return groupsToBeNotified;
  }

  @Override
  protected void performTemplateData(final String language, final FormInstance resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);
    getResource().getValidations().getLatestValidation()
        .ifPresent(v -> template.setAttribute("validation", v));

  }

  protected void setUserIdsToNotify(List<User> users) {
    if (users == null) {
      this.usersToBeNotified = emptyList();
    } else {
      this.usersToBeNotified = extractUserIds(users);
    }
  }

  protected void setGroupIdsToNotify(List<Group> groups) {
    if (groups == null) {
      this.groupsToBeNotified = emptyList();
    } else {
      this.groupsToBeNotified = extractGroupIds(groups);
    }
  }

}
