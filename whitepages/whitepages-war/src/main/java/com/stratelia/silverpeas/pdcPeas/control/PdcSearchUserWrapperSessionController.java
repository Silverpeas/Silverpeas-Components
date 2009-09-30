package com.stratelia.silverpeas.pdcPeas.control;

import com.stratelia.silverpeas.pdc.control.Pdc;
import java.util.List;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;

/**
 * A simple wrapper to the userpanel.
 * 
 * @author Didier Wenzek
 */
public class PdcSearchUserWrapperSessionController extends
    AbstractComponentSessionController {

  private Pdc m_pdc = null;

  public Pdc getPdc() {
    if (m_pdc == null) {
      m_pdc = new Pdc();
    }

    return m_pdc;
  }

  /**
   * Standard Session Controller Constructeur
   * 
   * @param mainSessionCtrl
   *          The full work session.
   * @param componentContext
   *          The context of this component session.
   */
  public PdcSearchUserWrapperSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.stratelia.silverpeas.pdcPeas.multilang.pdcBundle",
        "com.stratelia.silverpeas.pdcPeas.settings.pdcPeasIcons");
  }

  /**
   * Returns the HTML form name whose user element must be set.
   */
  public String getFormName() {
    return formName;
  }

  /**
   * Returns the HTML input where the selected user id must be set.
   */
  public String getElementId() {
    return elementId;
  }

  /**
   * Returns the HTML input where the selected user name must be set.
   */
  public String getElementName() {
    return elementName;
  }

  /**
   * Returns the selected user (if any).
   */
  public List getSelectedUsers() {
    return selectedUsers;
  }

  /**
   * Set the HTML form name whose user element must be set.
   */
  public void setFormName(String formName) {
    this.formName = formName;
  }

  /**
   * Set the HTML input where the selected user id must be set.
   */
  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  /**
   * Set the HTML input where the selected user name must be set.
   */
  public void setElementName(String elementName) {
    this.elementName = elementName;
  }

  /**
   * Set the selected user (if any).
   */
  public void setSelectedUserIds(String selectedUserIds) {// parcours de la
                                                          // liste userCardId,
                                                          // userCardId,
                                                          // userCardId ...
    // Construction d'une liste de GlobalSilverContent à partir de ces Id
  }

  /**
   * Init the user panel.
   */
  public void initPdcSearchUser() {
  }

  /**
   * Reads the selection made with the user panel.
   */
  public void getUserSelection() {
    // get selected objects from PDC
    selectedUsers = getPdc().getSelectedSilverContents();
  }

  /**
   * The HTML form name whose user element must be set.
   */
  private String formName = null;
  /**
   * The HTML input where the selected user id must be set.
   */
  private String elementId = null;
  /**
   * The HTML input where the selected user name must be set.
   */
  private String elementName = null;
  /**
   * The selected user (if any).
   */
  private List selectedUsers = null;
}
