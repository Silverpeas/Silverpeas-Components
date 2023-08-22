package org.silverpeas.components.formsonline;

import org.silverpeas.components.formsonline.model.FormsOnlineException;
import org.silverpeas.components.formsonline.model.FormsOnlineService;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.ComponentAuthorization;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Named;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Named
public class FormsOnlineComponentAuthorization implements ComponentAuthorization {

  private FormsOnlineComponentAuthorization() {
    // Instantiated by IoC only.
  }

  @Override
  public boolean isRelatedTo(final String instanceId) {
    return instanceId.startsWith("formsOnline");
  }

  @Override
  public <T> Stream<T> filter(final Collection<T> resources,
      final Function<T, ComponentResourceReference> converter, final String userId,
      final AccessControlOperation... operations) {
    Set<String> componentIds = resources.stream()
        .map(r -> converter.apply(r).getInstanceId())
        .collect(Collectors.toSet());
    componentIds = ComponentAccessControl.get()
        .filterAuthorizedByUser(componentIds, userId)
        .collect(Collectors.toSet());
    try {
      String orderBy = "name asc";
      final Set<String> formIds = FormsOnlineService.get()
          .getAvailableFormsToSend(componentIds, userId, orderBy).stream()
          .map(f -> String.valueOf(f.getId()))
          .collect(Collectors.toSet());
      return resources.stream().filter(r -> formIds.contains(converter.apply(r).getLocalId()));
    } catch (FormsOnlineException e) {
      SilverLogger.getLogger(this).error(e);
    }
    return Stream.empty();
  }
}