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
package com.stratelia.webactiv.yellowpages.model.dao.jdbc;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.contact.model.ContactPK;
import com.stratelia.webactiv.yellowpages.model.Constants.GenericContactTypeConstant;
import com.stratelia.webactiv.yellowpages.model.Constants.RelationTypeConstant;
import com.stratelia.webactiv.yellowpages.model.beans.Company;
import com.stratelia.webactiv.yellowpages.model.beans.CompanyPK;
import com.stratelia.webactiv.yellowpages.model.beans.GenericContact;
import com.stratelia.webactiv.yellowpages.model.beans.GenericContactPK;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A specific JDBC requester dedicated on the company persisted in the underlying data source.
 */
public class JDBCCompanyRequester {

    private static final int INITIAL_CAPACITY = 1000;

    /**
     * Constructs a new JDBCCompanyRequester instance.
     */
    public JDBCCompanyRequester() {
    }

    public CompanyPK saveCompany(Connection con, Company company) throws SQLException {
        String insert_query_company = "INSERT INTO sc_contact_company (companyid, companyname, companyemail, companyphone, companyfax, companycreationdate, companycreatorid, instanceid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement prepStmt = null;
        int newId;
        try {
            newId = DBUtil.getNextId(company.getPk().getTableName(), "companyId");
        } catch (Exception e) {
            SilverTrace.warn("company", getClass().getSimpleName() + ".saveCompany", "yellowpages.EX_CREATE_COMPANY_FAILED", e);
            return null;
        }
        try {
            prepStmt = con.prepareStatement(insert_query_company);
            prepStmt.setInt(1, newId);
            prepStmt.setString(2, company.getName());
            prepStmt.setString(3, company.getEmail());
            prepStmt.setString(4, company.getPhone());
            prepStmt.setString(5, company.getFax());
            prepStmt.setString(6, company.getCreationDate());
            prepStmt.setString(7, company.getCreatorId());
            prepStmt.setString(8, company.getPk().getComponentName());
            prepStmt.executeUpdate();
        } finally {
            DBUtil.close(prepStmt);
        }
        company.getPk().setId(String.valueOf(newId));

        // Enregistrement dans la table GenericContact
        GenericContactPK newGenericContactPK = new GenericContactPK();
        GenericContact newGenericContact = new GenericContact(newGenericContactPK, GenericContactTypeConstant.COMPANY, null, Integer.parseInt(company.getPk().getId()));
        this.saveGenericContact(con, newGenericContact);

        return company.getPk();
    }

    public Company getCompany(Connection con, CompanyPK pk) throws SQLException {
        String select_query = "SELECT companyid, companyname, companyemail, companyphone, companyfax, companycreationdate, companycreatorid, instanceid FROM sc_contact_company WHERE companyId = ?";
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            prepStmt = con.prepareStatement(select_query);
            prepStmt.setInt(1, Integer.parseInt(pk.getId()));
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                pk.setComponentName(rs.getString("instanceId"));
                pk.setId(String.valueOf(rs.getInt("companyId")));
                return resultSet2Company(rs, pk);
            }
            return null;
        } finally {
            DBUtil.close(rs, prepStmt);
        }
    }

    public int updateCompany(Connection con, Company company) throws SQLException {
        String update_query = "UPDATE sc_contact_company SET companyname=?, companyemail=?, companyphone=?, companyfax=?, companycreationdate=?, companycreatorid=?, instanceId=? WHERE companyid=?";
        PreparedStatement preStmt = null;
        ResultSet rs = null;
        try {
            preStmt = con.prepareStatement(update_query);
            preStmt.setString(1, company.getName());
            preStmt.setString(2, company.getEmail());
            preStmt.setString(3, company.getPhone());
            preStmt.setString(4, company.getFax());
            preStmt.setString(5, company.getCreationDate());
            preStmt.setString(6, company.getCreatorId());
            preStmt.setString(7, company.getPk().getComponentName());

            // Clause Where
            preStmt.setInt(8, Integer.parseInt(company.getPk().getId()));

            return preStmt.executeUpdate();

        } finally {
            DBUtil.close(preStmt);
        }
    }

    public int deleteCompany(Connection con, Company company) throws SQLException {

        int companyId = Integer.parseInt(company.getPk().getId());

        // Trouve le GenericContact associé
        GenericContact genericContact = getGenericContactFromCompanyId(con, companyId);
        if (genericContact != null) {
            deleteGenericContact(con, genericContact);
        }

        String delete_query = "DELETE sc_contact_company WHERE companyid=?";
        PreparedStatement preStmt = null;
        ResultSet rs = null;
        try {
            preStmt = con.prepareStatement(delete_query);

            // Clause Where
            preStmt.setInt(1, companyId);
            return preStmt.executeUpdate();

        } finally {
            DBUtil.close(preStmt);
        }
    }

    public boolean isAlreadyInContactList(Connection con, Company company, int contactId) throws SQLException {
        String select_query = "SELECT GCCOMPANY.companyId " +
                "FROM sc_contact_genericcontact GCCONTACT, sc_contact_genericcontact GCCOMPANY, sc_contact_genericcontact_rel REL " +
                "WHERE GCCONTACT.genericcontactid = REL.genericcontactid AND " +
                "REL.genericcompanyid = GCCOMPANY.genericcontactid AND " +
                "GCCOMPANY.contactType = ? AND " +
                "GCCONTACT.contactId = ? AND " +
                "GCCOMPANY.companyId = ?";

        PreparedStatement prepStmt = null;
        ContactPK contactPkSearch;
        ResultSet rs = null;


        List<Company> resultList = new ArrayList<Company>();
        try {
            prepStmt = con.prepareStatement(select_query);
            prepStmt.setInt(1, GenericContactTypeConstant.COMPANY);
            prepStmt.setInt(2, contactId);
            prepStmt.setInt(3, Integer.parseInt(company.getPk().getId()));
            rs = prepStmt.executeQuery();

            return (rs.next());

        } finally {
            DBUtil.close(rs, prepStmt);
        }

    }

    public List<Company> findCompanyListByContactId(Connection con, int contactId) throws SQLException {
        // Recupère la liste des companies associées
        String select_query = "SELECT COMP.companyid, COMP.companyname, COMP.companyemail, COMP.companyphone, COMP.companyfax, COMP.companycreationdate, COMP.companycreatorid, COMP.instanceid " +
                "FROM sc_contact_genericcontact GCCONTACT, sc_contact_genericcontact GCCOMPANY, sc_contact_genericcontact_rel REL, sc_contact_company COMP " +
                "WHERE GCCONTACT.genericcontactid = REL.genericcontactid AND " +
                "REL.genericcompanyid = GCCOMPANY.genericcontactid AND " +
                "GCCOMPANY.companyId = COMP.companyId AND " +
                "GCCOMPANY.contactType = ? AND " +
                "GCCONTACT.contactId = ?";

        PreparedStatement prepStmt = null;
        ContactPK contactPkSearch;
        ResultSet rs = null;
        List<Company> resultList = new ArrayList<Company>();
        try {
            prepStmt = con.prepareStatement(select_query);
            prepStmt.setInt(1, GenericContactTypeConstant.COMPANY);
            prepStmt.setInt(2, contactId);
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("companyId");
                CompanyPK pk = new CompanyPK(String.valueOf(id));
                resultList.add(resultSet2Company(rs, pk));
            }
            return resultList;
        } finally {
            DBUtil.close(rs, prepStmt);
        }
    }

    public void addCompanyToContact(Connection con, Company company, int contactId) throws SQLException {
        // Get generic company
        GenericContact genericCompany = getGenericContactFromCompanyId(con, Integer.parseInt(company.getPk().getId()));
        // Get generic contact
        GenericContact genericContact = getGenericContactFromContactId(con, contactId);

        String insert_query = "INSERT INTO sc_contact_genericcontact_rel (genericContactId, genericCompanyId, relationType) VALUES (?, ?, ?)";
        PreparedStatement prepStmt = null;

        try {
            prepStmt = con.prepareStatement(insert_query);
            prepStmt.setInt(1, Integer.parseInt(genericContact.getPk().getId()));
            prepStmt.setInt(2, Integer.parseInt(genericCompany.getPk().getId()));
            prepStmt.setInt(3, RelationTypeConstant.BELONGS_TO);
            prepStmt.executeUpdate();

        } finally {
            DBUtil.close(prepStmt);
        }
    }

    public void removeCompanyFromContact(Connection con, Company company, int contactId) throws SQLException {
        // Get generic company
        GenericContact genericCompany = getGenericContactFromCompanyId(con, Integer.parseInt(company.getPk().getId()));
        // Get generic contact
        GenericContact genericContact = getGenericContactFromContactId(con, contactId);

        String delete_query = "DELETE sc_contact_genericcontact_rel WHERE genericContactId=? AND genericcompanyid=?";
        PreparedStatement preStmt = null;
        ResultSet rs = null;
        try {
            preStmt = con.prepareStatement(delete_query);

            // Clause Where
            preStmt.setInt(1, Integer.parseInt(genericContact.getPk().getId()));
            preStmt.setInt(2, Integer.parseInt(genericCompany.getPk().getId()));
            preStmt.executeUpdate();

        } finally {
            DBUtil.close(preStmt);
        }
    }

    public GenericContactPK saveGenericContact(Connection con, GenericContact genericContact) throws SQLException {
        String insert_query = "INSERT INTO sc_contact_genericcontact (genericContactId, contactType, contactId, companyId) VALUES (?, ?, ?, ?)";
        PreparedStatement prepStmt = null;

        int newId;
        try {
            newId = DBUtil.getNextId(genericContact.getPk().getTableName(), "genericContactId");
        } catch (Exception e) {
            SilverTrace.warn("genericContact", getClass().getSimpleName() + ".saveGenericContact", "yellowpages.EX_CREATE_GENERIC_CONTACT_FAILED", e);
            return null;
        }
        try {
            prepStmt = con.prepareStatement(insert_query);
            prepStmt.setInt(1, newId);
            prepStmt.setInt(2, genericContact.getType());
            if (genericContact.getContactId() == null) {
                prepStmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                prepStmt.setInt(3, genericContact.getContactId());
            }
            if (genericContact.getCompanyId() == null) {
                prepStmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                prepStmt.setInt(4, genericContact.getCompanyId());
            }
            prepStmt.executeUpdate();
        } finally {
            DBUtil.close(prepStmt);
        }

        // Enregistrement dans la table GenericContact
        genericContact.getPk().setId(String.valueOf(newId));
        return genericContact.getPk();
    }

    public int deleteGenericContact(Connection con, GenericContact genericContact) throws SQLException {
        String delete_query = "DELETE sc_contact_genericcontact WHERE genericContactId=?";
        PreparedStatement preStmt = null;
        ResultSet rs = null;
        try {
            preStmt = con.prepareStatement(delete_query);

            // Clause Where
            preStmt.setInt(1, Integer.parseInt(genericContact.getPk().getId()));
            return preStmt.executeUpdate();

        } finally {
            DBUtil.close(preStmt);
        }
    }

    public GenericContact getGenericContactFromContactId(Connection con, int contactId) throws SQLException {

        // TODO rajouter contactType dans la clause where
        String select_query = "SELECT genericContactId, contactType, contactId, companyId FROM sc_contact_genericcontact WHERE contactId = ?";
        PreparedStatement prepStmt = null;
        ContactPK contactPkSearch;
        ResultSet rs = null;

        try {
            prepStmt = con.prepareStatement(select_query);
            prepStmt.setInt(1, contactId);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                contactPkSearch = new ContactPK(String.valueOf(rs.getInt("genericContactId")));
                return resultSet2GenericContact(rs, contactPkSearch);
            }
            return null;
        } finally {
            DBUtil.close(rs, prepStmt);
        }
    }

    public GenericContact getGenericContactFromCompanyId(Connection con, int companyId) throws SQLException {

        // TODO rajouter contactType dans la clause where
        String select_query = "SELECT genericContactId, contactType, contactId, companyId FROM sc_contact_genericcontact WHERE companyId = ?";
        PreparedStatement prepStmt = null;
        CompanyPK companyPKSearch;
        ResultSet rs = null;
        try {
            prepStmt = con.prepareStatement(select_query);
            prepStmt.setInt(1, companyId);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                companyPKSearch = new CompanyPK(String.valueOf(rs.getInt("genericContactId")));
                return resultSet2GenericContact(rs, companyPKSearch);
            }
            return null;
        } finally {
            DBUtil.close(rs, prepStmt);
        }

    }

    private static GenericContact resultSet2GenericContact(ResultSet rs, WAPrimaryKey pk) throws SQLException {

        int id = rs.getInt("genericContactId");
        GenericContactPK genericPK = new GenericContactPK(String.valueOf(id), pk);
        int type = rs.getInt("contactType");
        Integer contactId = rs.getInt("contactId");
        if (rs.wasNull()) {
            contactId = null;
        }
        Integer companyId = rs.getInt("companyId");
        if (rs.wasNull()) {
            companyId = null;
        }

        return new GenericContact(genericPK, type, contactId, companyId);
    }

    private static Company resultSet2Company(ResultSet rs, CompanyPK pubPK) throws SQLException {
        int id = rs.getInt("companyId");
        CompanyPK pk = new CompanyPK(String.valueOf(id), pubPK);

        String companyname = rs.getString("companyname");
        String companyemail = rs.getString("companyemail");
        String companyphone = rs.getString("companyphone");
        String companyfax = rs.getString("companyfax");
        String creationDate = rs.getString("companycreationdate");
        String companycreatorid = rs.getString("companycreatorid");

        return new Company(pk, companyname, companyemail, companyphone, companyfax, creationDate, companycreatorid);
    }

    public int deleteGenericContactRel(Connection con, GenericContact genericContact) throws SQLException {
        String delete_query = "DELETE sc_contact_genericcontact WHERE genericContactId=?";
        PreparedStatement preStmt = null;
        ResultSet rs = null;
        try {
            preStmt = con.prepareStatement(delete_query);

            // Clause Where
            preStmt.setInt(1, Integer.parseInt(genericContact.getPk().getId()));
            return preStmt.executeUpdate();

        } finally {
            DBUtil.close(preStmt);
        }
    }

}