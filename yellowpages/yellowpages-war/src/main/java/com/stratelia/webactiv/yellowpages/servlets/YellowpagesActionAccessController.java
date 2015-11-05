/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.yellowpages.servlets;

import com.stratelia.webactiv.SilverpeasRole;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ebonnet
 */
public class YellowpagesActionAccessController {

  private Map<String, SilverpeasRole> actionRole = new HashMap<String, SilverpeasRole>();

  public YellowpagesActionAccessController() {
    actionRole.put("Main", SilverpeasRole.reader);
    actionRole.put("DeleteContact", SilverpeasRole.writer);

    /*
      function.equals("GoTo")) {
      function.startsWith("portlet")) {
      function.startsWith("annuaire")) {
      function.startsWith("topicManager")) {
      function.equals("GoToGroup")) {
      function.equals("RemoveGroup")) {
      function.equals("ViewUserFull")) {
      function.startsWith("searchResult")) {
      function.equals("Search")) {
      "ToAddFolder".equals(function)) {
      "AddFolder".equals(function)) {
      "ToUpdateFolder".equals(function)) {
      "UpdateFolder".equals(function)) {
      "DeleteFolder".equals(function)) {
      function.equals("PrintList")) {
      function.startsWith("Contact")) {
      function.startsWith("http")) {
      function.equals("selectUser")) {
      function.startsWith("saveUser")) {
      function.equals("ToChooseGroup")) {
      function.equals("AddGroup")) {
      function.equals("ModelUsed")) {
      function.equals("SelectModel")) {
      function.equals("DeleteBasketContent")) {
      "ExportCSV".equals(function)) {
      "ToImportCSV".equals(function)) {
      "ImportCSV".equals(function)) {
    if ("ContactView".equals(function)) {
    "ContactExternalView".equals(function)) {
    "ContactNew".equals(function)) {
    "ContactNewFromUser".equals(function)) {
    "ContactUpdate".equals(function)) {
    "ContactSave".equals(function)) {
    "ContactSetFolders".equals(function)) {
    }
    */

  }


  /**
   * Check if user role has right access to the given action
   * @param action the checked action
   * @param role the highest user role
   * @return true if given role has right access to the action
   */
  public boolean hasRightAccess(String action, SilverpeasRole role) {
    boolean actionExist = actionRole.containsKey(action);
    if (actionExist && role.isGreaterThanOrEquals(actionRole.get(action))) {
      return true;
    } else if (!actionExist) {
      return true;
    }
    return false;
  }
}
