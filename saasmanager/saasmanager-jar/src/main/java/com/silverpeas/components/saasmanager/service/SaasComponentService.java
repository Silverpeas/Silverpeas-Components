/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.components.saasmanager.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.silverpeas.admin.components.Parameter;
import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.components.saasmanager.exception.SaasManagerException;
import com.silverpeas.components.saasmanager.model.SaasAccess;
import com.silverpeas.components.saasmanager.vo.ComponentListVO;
import com.silverpeas.components.saasmanager.vo.ComponentVO;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * SAAS component service
 * @author ahedin
 */
public class SaasComponentService {

  private static final String COMPONENTS_SETTINGS =
    "com.silverpeas.saasmanager.settings.SaasManagerComponents";

  private Admin admin;
  private ResourceLocator componentsSettings;
  private Map<String, WAComponent> allComponents;

  public SaasComponentService() {
    admin = new Admin();
    componentsSettings = new ResourceLocator(COMPONENTS_SETTINGS, "");
  }

  /**
   * 
   * @param language The language required to display the components.
   * @return A list of available Silverpeas components, sorted by their suites and their names.
   */
  public List<ComponentListVO> getComponentLists(String language) {
    Map<String, WAComponent> componentsMap = getAllComponents();

    // Components are stored into a map depending on the suite they belong to.
    HashMap<String, ComponentListVO> componentListMap = new HashMap<String, ComponentListVO>();
    String suite;
    String name;
    String label;
    for (WAComponent component : componentsMap.values()) {
      if (component.isVisible()) {
        name = component.getName();
        label = component.getLabel().get(language);
        if (StringUtil.isDefined(name) && StringUtil.isDefined(label)) {
          suite = component.getSuite().get(language);
          if (!componentListMap.containsKey(suite)) {
            componentListMap.put(suite, new ComponentListVO(suite));
          }
          componentListMap.get(suite).addComponent(new ComponentVO(name, label));
        }
      }
    }

    // All components lists are stored into an array in order to be sorted.
    ArrayList<ComponentListVO> componentLists = new ArrayList<ComponentListVO>();
    for (ComponentListVO componentList : componentListMap.values()) {
      componentLists.add(componentList);
    }

    // Suites of components are sorted.
    Collections.sort(componentLists, new Comparator<ComponentListVO>() {
      @Override
      public int compare(ComponentListVO o1, ComponentListVO o2) {
        return o1.getLabel().toLowerCase().compareTo(o2.getLabel().toLowerCase());
      }
    });

    // Suites are renamed (indexes included in names are removed) and components are sorted in every
    // suite.
    String suiteIndex;
    for (ComponentListVO componentList : componentLists) {
      suite = componentList.getLabel().trim();
      suiteIndex = "";
      while (suite.length() > 0 && suite.charAt(0) >= '0' && suite.charAt(0) <= '9') {
        suiteIndex += suite.charAt(0);
        suite = suite.substring(1);
      }
      componentList.setLabel(suite.trim());
      componentList.setSuiteIndex(suiteIndex);

      Collections.sort(componentList.getComponents(), new Comparator<ComponentVO>() {
      @Override
        public int compare(ComponentVO o1, ComponentVO o2) {
          return o1.getLabel().toLowerCase().compareTo(o2.getLabel().toLowerCase());
        }
      });
    }

    return componentLists;
  }

  /**
   * Creates the components corresponding to the services of the SAAS access.
   * @param access The SAAS access.
   * @return The list of the ids of the new components.
   * @throws SaasManagerException
   */
  public ArrayList<String> createComponents(SaasAccess access)
  throws SaasManagerException {
    ArrayList<String> services = access.getServicesList();
    ArrayList<String> componentIds = new ArrayList<String>();
    for (String service : services) {
      componentIds.add(createComponent(access, service, null, false, null));
    }
    return componentIds;
  }

  /**
   * Creates the component which defines the home of the SAAS space.
   * @param access The SAAS access.
   * @return The id of the home component.
   * @throws SaasManagerException
   */
  public String createHomeComponent(SaasAccess access)
  throws SaasManagerException {
    String label = componentsSettings.getString("home.label." + access.getLang());
    return createComponent(access, "webPages", label, true, null);
  }

  /**
   * Fills the home component with a default welcome content.
   * @param access The SAAS access.
   * @throws SaasManagerException
   */
  public void fillHomeComponent(SaasAccess access)
  throws SaasManagerException {
    String content = componentsSettings.getString("home.content." + access.getLang());
    try {
      WysiwygController.updateFileAndAttachment(content, access.getSpaceId(),
        access.getHomeComponentId(), access.getHomeComponentId(), access.getUserId(), false);
    } catch (WysiwygException e) {
      throw new SaasManagerException("SaasComponentService.fillHomeComponent()",
        SilverpeasException.ERROR, "saasmanager.EX_FILL_HOME_COMPONENT", e);
    }
  }

  /**
   * Creates the management component of the SAAS access in order, for instance, to manage users of
   * the SAAS domain.
   * @param access The SAAS access.
   * @return The id of the management component.
   * @throws SaasManagerException
   */
  public String createManagementComponent(SaasAccess access)
  throws SaasManagerException {
    HashMap<String, String> specificParameters = new HashMap<String, String>();
    specificParameters.put("URL", new StringBuilder()
      .append(URLManager.getApplicationURL()).append("/Rsaasmanager")
      .append("?userId=%ST_USER_ID%")
      .append("&action=management")
      .append("&uid=").append(access.getUid()).toString());
    specificParameters.put("isInternalLink", "yes");
    String label = componentsSettings.getString("management.label." + access.getLang());
    return createComponent(access, "hyperlink", label, false, specificParameters);
  }

  /**
   * Creates a component of the SAAS access.
   * @param access The SAAS access.
   * @param name The component's name
   * @param label The component's label.
   * @param hidden Indicates if the component is hidden or not in the components menu of the SAAS
   *        space.
   * @param specificParameters Specific parameters linked to the component's type.
   * @return The id of the new component.
   * @throws SaasManagerException
   */
  private String createComponent(SaasAccess access, String name, String label, boolean hidden,
    HashMap<String, String> specificParameters)
  throws SaasManagerException {
    WAComponent component = getAllComponents().get(name);
    ComponentInst componentInst = new ComponentInst();
    if (StringUtil.isDefined(label)) {
      componentInst.setLabel(label);
    } else {
      componentInst.setLabel(component.getLabel().get(access.getLang()));
    }
    componentInst.setDescription("");
    componentInst.setPublic(false);
    componentInst.setHidden(hidden);
    componentInst.setLanguage(access.getLang());
    componentInst.setTranslationId("-1");
    componentInst.setRemoveTranslation(false);
    componentInst.setName(component.getName());
    componentInst.setDomainFatherId(access.getSpaceId());
    componentInst.setCreatorUserId(access.getUserId());

    List<Parameter> parameters = component.cloneParameters();
    String value;
    for (Parameter parameter : parameters) {
      if (specificParameters != null && specificParameters.containsKey(parameter.getName())) {
        value = specificParameters.get(parameter.getName());
      } else {
        value = componentsSettings.getString(component.getName() + "." + parameter.getName());
      }
      if (!StringUtil.isDefined(value)) {
        value = parameter.getValue();
      }
      if (parameter.isCheckbox() && !StringUtil.isDefined(value)) {
        value = "no";
      }
      parameter.setValue(value);
    }
    componentInst.setParameters(parameters);

    try {
      return admin.addComponentInst(access.getUserId(), componentInst);
    } catch (AdminException e) {
      throw new SaasManagerException("SaasComponentService.createComponent()",
        SilverpeasException.ERROR, "saasmanager.EX_CREATE_COMPONENT", "name=" + name, e);
    }
  }

  /**
   * Order all components of the SAAS access : first home component, then management component and
   * finally all others.
   * @param access The SAAS access.
   * @throws SaasManagerException
   */
  public void orderComponents(SaasAccess access)
  throws SaasManagerException {
    try {
      int order = 0;
      admin.updateComponentOrderNum(access.getHomeComponentId(), order++);
      admin.updateComponentOrderNum(access.getManagementComponentId(), order++);
      ArrayList<String> componentIds = access.getComponentIdsList();
      for (String componentId : componentIds) {
        admin.updateComponentOrderNum(componentId, order++);
      }
    } catch (AdminException e) {
      throw new SaasManagerException("SaasSpaceService.updateOrders()",
        SilverpeasException.ERROR, "saasmanager.EX_ORDER_COMPONENTS", e);
    }
  }

  /**
   * @return Silverpeas components descriptions.
   */
  private Map<String, WAComponent> getAllComponents() {
    if (allComponents == null) {
      allComponents = admin.getAllComponents();
    }
    return allComponents;
  }

}
