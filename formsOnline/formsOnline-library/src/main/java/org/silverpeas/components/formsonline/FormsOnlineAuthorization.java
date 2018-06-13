package org.silverpeas.components.formsonline;

import org.silverpeas.components.formsonline.model.FormDetail;
import org.silverpeas.components.formsonline.model.FormsOnlineService;
import org.silverpeas.core.security.authorization.ComponentAuthorization;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormsOnlineAuthorization implements ComponentAuthorization {

  private Map<String, List<String>> cache = Collections.synchronizedMap(new HashMap<>());
  private volatile boolean cacheEnabled = false;

  @Override
  public boolean isAccessAuthorized(final String componentId, final String userId,
      final String objectId) {
    return false;
  }

  @Override
  public boolean isAccessAuthorized(final String componentId, final String userId,
      final String objectId, final String objectType) {
    return isObjectAvailable(componentId, userId, objectId, objectType);
  }

  @Override
  public boolean isObjectAvailable(final String componentId, final String userId,
      final String objectId, final String objectType) {

    List<String> formIds = null;
    if (cacheEnabled) {
      formIds = cache.get(componentId);
    }

    if (formIds == null) {
      formIds = new ArrayList<>();
      try {
        List<FormDetail> forms = getService().getAvailableFormsToSend(componentId, userId);
        for (FormDetail form : forms) {
          formIds.add(String.valueOf(form.getId()));
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }

      if (cacheEnabled) {
        cache.put(componentId, formIds);
      }
    }

    return formIds.contains(objectId);
  }

  @Override
  public void enableCache() {
    cache.clear();
    cacheEnabled = true;
  }

  @Override
  public void disableCache() {
    cache.clear();
    cacheEnabled = false;
  }

  private FormsOnlineService getService() {
    return FormsOnlineService.get();
  }
}