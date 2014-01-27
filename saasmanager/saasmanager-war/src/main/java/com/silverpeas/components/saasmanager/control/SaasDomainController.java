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

package com.silverpeas.components.saasmanager.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import com.silverpeas.components.saasmanager.exception.SaasManagerException;
import com.silverpeas.jobDomainPeas.JobDomainPeasDAO;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.authentication.Authentication;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Controller which creates SAAS domains.
 * @author ahedin
 */
public class SaasDomainController {

  // Prefix used to build a domain name.
  private static final String DOMAIN_NAME_PREFIX = "Domain";

  // Admin bean.
  private Admin admin = null;
  // Silverpeas properties files root path.
  private String silverpeasRootPath = null;
  // Domains file path.
  private String domainFilePath = null;
  // Domains authentication file path.
  private String autDomainFilePath = null;

  public SaasDomainController() {
    admin = new Admin();
    initSilverpeasRootPath();
    initDomainFilePath();
    initAutDomainFilePath();
  }

  /**
   * Initializes Silverpeas properties files root path.
   */
  private void initSilverpeasRootPath() {
    ResourceLocator propInitialize = new ResourceLocator(
      "com.stratelia.silverpeas._silverpeasinitialize.settings._silverpeasinitializeSettings", "");
    String pathInitialize = propInitialize.getString("pathInitialize");
    int initializeIndex = pathInitialize.indexOf("initialize");
    silverpeasRootPath = pathInitialize.substring(0, initializeIndex);
  }

  /**
   * Initializes domains files path.
   */
  private void initDomainFilePath() {
    domainFilePath = new StringBuilder(silverpeasRootPath)
      .append("properties").append(File.separator).append("com").append(File.separator)
      .append("stratelia").append(File.separator).append("silverpeas").append(File.separator)
      .append("domains").toString();
  }

  /**
   * Initializes domains authentication files path.
   */
  private void initAutDomainFilePath() {
    autDomainFilePath = new StringBuilder(silverpeasRootPath)
      .append("properties").append(File.separator).append("com").append(File.separator)
      .append("stratelia").append(File.separator).append("silverpeas").append(File.separator)
      .append("authentication").toString();
  }

  /**
   * Creates a Silverpeas SQL domain.
   * @return The id of the new domain.
   * @throws SaasManagerException
   */
  public String createDomain()
  throws SaasManagerException {
    String domainName = getAvailableDomainName();

    // Creation of files com.stratelia.silverpeas.domains.domain<domainName> and
    // com.stratelia.silverpeas.authentication.autDomain<domainName>
    String domainFileName = "domain" + domainName + ".properties";
    File domainDirectory = new File(domainFilePath);
    File domainFile = new File(domainDirectory, domainFileName);

    String autDomainFileName = "autDomain" + domainName + ".properties";
    File autDomainDirectory = new File(autDomainFilePath);
    File autDomainFile = new File(autDomainDirectory, autDomainFileName);
    try {
      fillDomainFile(domainFile, domainName);
      fillAutDomainFile(autDomainFile, domainName);
    } catch (IOException e) {
      // Delete domain files
      domainFile.delete();
      autDomainFile.delete();
      throw new SaasManagerException("SaasDomainController.createDomain()",
        SilverpeasException.ERROR, "saasmanager.EX_CREATE_DOMAIN_IO", "domainName=" + domainName, e);
    }

    // Creation of database tables Domain<domainName>_Group, Domain<domainName>_Group_User_Rel and
    // Domain<domainName>_User
    int dbStatus = createTables(domainName);
    if (dbStatus > 0) {
      // Delete domain files
      domainFile.delete();
      autDomainFile.delete();
      // Delete tables
      deleteTables(domainName, dbStatus);
      throw new SaasManagerException("SaasDomainController.createDomain()",
        SilverpeasException.ERROR, "saasmanager.EX_CREATE_DOMAIN_DB", "domainName=" + domainName);
    }

    // Store domain in table ST_Domain
    String domainId = getDomainId(domainName);
    if (!StringUtil.isDefined(domainId)) {
      // Delete domain files
      domainFile.delete();
      autDomainFile.delete();
      // Delete tables
      deleteTables(domainName, 3);
      throw new SaasManagerException("SaasDomainController.createDomain()",
        SilverpeasException.ERROR, "saasmanager.EX_CREATE_DOMAIN", "domainName=" + domainName);
    }
    return domainId;
  }

  /**
   * Builds a new domain's name by adding a not yet used domain index to the suffix
   * DOMAIN_NAME_PREFIX.
   * @return An available domain name.
   * @throws SaasManagerException
   */
  private String getAvailableDomainName()
  throws SaasManagerException {
    // Available domain index
    Domain[] domains;
    try {
      domains = admin.getAllDomains();
    } catch (AdminException e) {
      throw new SaasManagerException("SaasDomainController.getAvailableDomainName()",
        SilverpeasException.ERROR, "admin.MSG_ERR_ADD_DOMAIN");
    }
    int domainMaxIndex = 0;
    for (Domain domain : domains) {
      domainMaxIndex = Math.max(domainMaxIndex, getDomainIndex(domain.getName()));
    }
    domainMaxIndex++;

    // Check availability of the files
    // com.stratelia.silverpeas.domains.domain<domainName>.properties
    // and com.stratelia.silverpeas.authentication.autDomain<domainName>.properties on the file
    // system.
    File domainDirectory = new File(domainFilePath);
    File autDomainDirectory = new File(autDomainFilePath);

    String domainName = DOMAIN_NAME_PREFIX + domainMaxIndex;
    File fileDomain = new File(domainDirectory, "domain" + domainName + ".properties");
    File fileAutDomain = new File(autDomainDirectory, "autDomain" + domainName + ".properties");
    while (fileDomain.exists() || fileAutDomain.exists()) {
      domainMaxIndex++;
      domainName = DOMAIN_NAME_PREFIX + domainMaxIndex;
      fileDomain = new File(domainDirectory, "domain" + domainName + ".properties");
      fileAutDomain = new File(autDomainDirectory, "autDomain" + domainName + ".properties");
    }

    return domainName;
  }

  /**
   * @param domainName The domain's name to analyze.
   * @return The index extracted from the domain's name.
   */
  private int getDomainIndex(String domainName) {
    String index = "";
    boolean stop = false;
    int i = domainName.length() - 1;
    while (!stop && i >= 0) {
      if (domainName.charAt(i) >= 0 && domainName.charAt(i) <= 9) {
        index = domainName.charAt(i) + index;
        i--;
      } else {
        stop = true;
      }
    }
    while (index.length() > 0 && index.charAt(0) == '0') {
      index = index.substring(1);
    }
    return (index.length() > 0 ? Integer.parseInt(index) : -1);
  }

  /**
   * Fills and writes the domain's settings file.
   * @param fileDomain The domain's settings file.
   * @param domainName The domain's name.
   * @throws IOException
   */
  private void fillDomainFile(File fileDomain, String domainName)
  throws IOException {
    ResourceLocator propAdmin = new ResourceLocator("com.stratelia.webactiv.beans.admin.admin", "");

    PrintWriter writer = new PrintWriter(new FileWriter(fileDomain));
    writer.println("# SQL driver");
    writer.println("");
    writer.println("# DataBase Access");
    writer.println("# ----------------");
    writer.println("");
    writer.println("database.SQLClassName = " + propAdmin.getString("AdminDBDriver"));
    writer.println("database.SQLJDBCUrl = " + propAdmin.getString("WaProductionDb"));
    writer.println("database.SQLAccessLogin = " + propAdmin.getString("WaProductionUser"));
    writer.println("database.SQLAccessPasswd = " + propAdmin.getString("WaProductionPswd"));
    writer.println("");
    writer.println("database.SQLUserTableName = Domain" + domainName + "_User");
    writer.println("database.SQLGroupTableName = Domain" + domainName + "_Group");
    writer.println("database.SQLUserGroupTableName = Domain" + domainName + "_Group_User_Rel");
    writer.println("");
    writer.println("# Generic Properties");
    writer.println("# ----------------");
    writer.println("");
    writer.println("# For Users");
    writer.println("database.SQLUserSpecificIdColumnName = id");
    writer.println("database.SQLUserLoginColumnName = login");
    writer.println("database.SQLUserFirstNameColumnName = firstName");
    writer.println("database.SQLUserLastNameColumnName = lastName");
    writer.println("database.SQLUserEMailColumnName = email");
    writer.println("database.SQLUserPasswordColumnName = password");
    writer.println("database.SQLUserPasswordValidColumnName = passwordValid");
    writer.println("");
    writer.println("# For Groups");
    writer.println("database.SQLGroupSpecificIdColumnName = id");
    writer.println("database.SQLGroupNameColumnName = name");
    writer.println("database.SQLGroupDescriptionColumnName = description");
    writer.println("database.SQLGroupParentIdColumnName = superGroupId");
    writer.println("");
    writer.println("# For Users-Groups relations");
    writer.println("database.SQLUserGroupUIDColumnName = userId");
    writer.println("database.SQLUserGroupGIDColumnName = groupId");
    writer.println("");

    File fileTemplateDomainSQL = new File(
      domainFilePath + File.separator + "templateDomainSQL.properties");
    BufferedReader templateDomainSQLReader = new BufferedReader(
      new FileReader(fileTemplateDomainSQL));
    String currentLine;
    while ((currentLine = templateDomainSQLReader.readLine()) != null) {
      if (!currentLine.startsWith("allowPasswordChange")) {
        writer.println(currentLine);
      }
    }

    writer.close();
  }

  /**
   * Fills and writes the domain's authentication settings file.
   * @param fileAutDomain The domain's authentication settings file.
   * @param domainName The domain's name.
   * @throws IOException
   */
  private void fillAutDomainFile(File fileAutDomain, String domainName)
  throws IOException {
    ResourceLocator propAdmin = new ResourceLocator("com.stratelia.webactiv.beans.admin.admin", "");

    ResourceLocator templateDomainSql = new ResourceLocator(
      "com.stratelia.silverpeas.domains.templateDomainSQL", "");
    String cryptMethod = templateDomainSql.getString(
      "database.SQLPasswordEncryption", Authentication.ENC_TYPE_MD5);
    boolean allowPasswordChange = "true".equals(
      templateDomainSql.getString("allowPasswordChange", "true"));

    PrintWriter writer = new PrintWriter(new FileWriter(fileAutDomain));
    writer.println("# Silverpeas default driver authentication");
    writer.println("# ----------------------------------------");
    writer.println("");
    writer.println("# Fallback type : could be one of the following values : none, ifNotRejected, always");
    writer.println("fallbackType = none");
    writer.println("");
    writer.println("allowPasswordChange = " + allowPasswordChange);
    writer.println("");
    writer.println("# Authentication servers");
    writer.println("# Available types are : com.stratelia.silverpeas.authentication.AuthenticationNT, "
      + "com.stratelia.silverpeas.authentication.AuthenticationSQL and "
      + "com.stratelia.silverpeas.authentication.AuthenticationLDAP");
    writer.println("");
    writer.println("autServersCount = 1");
    writer.println("");
    writer.println("autServer0.type = com.stratelia.silverpeas.authentication.AuthenticationSQL");
    writer.println("autServer0.enabled = true");
    writer.println("autServer0.SQLJDBCUrl = " + propAdmin.getString("WaProductionDb"));
    writer.println("autServer0.SQLAccessLogin = " + propAdmin.getString("WaProductionUser"));
    writer.println("autServer0.SQLAccessPasswd = " + propAdmin.getString("WaProductionPswd"));
    writer.println("autServer0.SQLDriverClass = " + propAdmin.getString("AdminDBDriver"));
    writer.println("autServer0.SQLUserTableName = Domain" + domainName + "_User");
    writer.println("autServer0.SQLUserLoginColumnName = login");
    writer.println("autServer0.SQLUserPasswordColumnName = password");
    writer.println("autServer0.SQLUserPasswordAvailableColumnName = passwordValid");
    writer.println("autServer0.SQLPasswordEncryption = " + cryptMethod);
    writer.close();
  }

  /**
   * Creates the domain's database tables.
   * @param domainName The domain's name.
   * @return The tables creation status, 0 if all tables have been created, otherwise the index of
   *         the table which a problem has occurred for.
   */
  private int createTables(String domainName) {
    int dbStatus = 0;
    try {
      dbStatus++;
      JobDomainPeasDAO.createTableDomain_Group(domainName);

      dbStatus++;
      JobDomainPeasDAO.createTableDomain_User(domainName);

      dbStatus++;
      JobDomainPeasDAO.createTableDomain_Group_User_Rel(domainName);
    } catch (SQLException e) {
      SilverTrace.error("saasmanager", "SaasDomainController.createTables()",
        "saasmanager.EX_CREATE_DOMAIN_DB_TABLES_CREATION", "domainName=" + domainName
        + " ; tableName=" + getDomainTableName(domainName, dbStatus), e);
      return dbStatus;
    }
    return 0;
  }

  /**
   * Deletes the domain's database tables using the DB status to know which tables have been created
   * and must be removed.
   * @param domainName The domain's name.
   * @param dbStatus The tables creation status, 0 if all tables have been created, otherwise the
   *        index of the table which a problem has occurred for.
   */
  private void deleteTables(String domainName, int dbStatus) {
    int dbRemoveStatus = 0;
    try {
      if (dbStatus > 0) {
        dbRemoveStatus++;
        JobDomainPeasDAO.dropTableDomain_Group(domainName);
      }
      if (dbStatus > 1) {
        dbRemoveStatus++;
        JobDomainPeasDAO.dropTableDomain_User(domainName);
      }
      if (dbStatus > 2) {
        dbRemoveStatus++;
        JobDomainPeasDAO.dropTableDomain_Group_User_Rel(domainName);
      }
    } catch (SQLException e) {
      SilverTrace.error("saasmanager", "SaasDomainController.deleteTables()",
        "saasmanager.EX_CREATE_DOMAIN_DB_TABLES_DELETION", "domainName=" + domainName
        + " ; tableName=" + getDomainTableName(domainName, dbRemoveStatus), e);
    }
  }

  /**
   * Finalizes the domain creation by calling Silverpeas admin bean to obtain its id.
   * @param domainName The domain's name.
   * @return The id of the new domain.
   */
  private String getDomainId(String domainName) {
    ResourceLocator managerSettings = new ResourceLocator(
      "com.silverpeas.saasmanager.settings.SaasManagerSettings", "");
    String silverpeasServerURL = managerSettings.getString("silverpeasUrl", "");
    Domain domain = new Domain();
    domain.setId("-1");
    domain.setName(domainName);
    domain.setDescription(null);
    domain.setDriverClassName("com.stratelia.silverpeas.domains.sqldriver.SQLDriver");
    domain.setPropFileName("com.stratelia.silverpeas.domains.domain" + domainName);
    domain.setAuthenticationServer("autDomain" + domainName);
    domain.setSilverpeasServerURL(silverpeasServerURL);
    domain.setTheTimeStamp("0");
    try {
      return admin.addDomain(domain);
    } catch (AdminException e) {
      SilverTrace.error("saasmanager", "SaasDomainController.getDomainId()",
        "saasmanager.EX_CREATE_DOMAIN", "domainName=" + domainName, e);
      return null;
    }
  }
  
  /**
   * @param domainName The domain's name.
   * @param dbStatus The tables creation status, 0 if all tables have been created, otherwise the
   *        index of the table which a problem has occurred for.
   * @return The database table's name corresponding to the DB status.
   */
  private String getDomainTableName(String domainName, int dbStatus) {
    switch (dbStatus) {
      case 1:
        return "Domain" + domainName + "_Group";
      case 2:
        return "Domain" + domainName + "_User";
      case 3:
        return "Domain" + domainName + "_Group_User_Rel";
      default:
        return null;
    }
  }

}
