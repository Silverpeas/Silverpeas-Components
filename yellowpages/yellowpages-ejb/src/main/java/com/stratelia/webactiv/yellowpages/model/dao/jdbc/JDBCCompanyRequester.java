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
import com.stratelia.webactiv.yellowpages.model.GenericContactTypeConstant;
import com.stratelia.webactiv.yellowpages.model.beans.Company;
import com.stratelia.webactiv.yellowpages.model.beans.CompanyPK;
import com.stratelia.webactiv.yellowpages.model.beans.GenericContact;
import com.stratelia.webactiv.yellowpages.model.beans.GenericContactPK;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        int newId = 0;
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

        // Trouve le GenericContact associé


        String delete_query = "DELETE sc_contact_company WHERE companyid=?";
        PreparedStatement preStmt = null;
        ResultSet rs = null;
        try {
            preStmt = con.prepareStatement(delete_query);

            // Clause Where
            preStmt.setInt(1, Integer.parseInt(company.getPk().getId()));
            return preStmt.executeUpdate();

        } finally {
            DBUtil.close(preStmt);
        }
    }
/*
    public void addCompanyToContact(Connection con, Company company, ContactPK contactPK) throws SQLException {
        // vérifie l'exitence de la company
        Company company = getCompany(con, companyPK);
        // insert du contact dans la table generic_contact s'il n'y est pas déjà
        GenericContact genericContact = getGenericContact(contactPK);
        if (genericContact == null) {
            genericContact = new GenericContact()
                                    saveGenericContact(con,genericContact)
        }

        // ajout de la company au contact
        String add_company_query = "UPDATE sc_contact_contact"
    }*/


    public GenericContactPK saveGenericContact(Connection con, GenericContact genericContact) throws SQLException {
        String insert_query = "INSERT INTO sc_contact_genericcontact (genericContactId, contactType, contactId, companyId) VALUES (?, ?, ?, ?)";
        PreparedStatement prepStmt = null;

        int newId = 0;
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

    public GenericContact getGenericContactFromContactPk(Connection con, ContactPK contactPk) throws SQLException {

        String select_query = "SELECT genericContactId, contactType, contactId, companyId FROM sc_contact_genericcontact WHERE contactId = ?";
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            prepStmt = con.prepareStatement(select_query);
            prepStmt.setInt(1, Integer.parseInt(contactPk.getId()));
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                contactPk.setComponentName(rs.getString("instanceId"));
                contactPk.setId(String.valueOf(rs.getInt("genericContactId")));
                return resultSet2GenericContact(rs, (WAPrimaryKey) contactPk);
            }
            return null;
        } finally {
            DBUtil.close(rs, prepStmt);
        }

    }

    public GenericContact getGenericContactFromCompanyPk(Connection con, CompanyPK companyPK) throws SQLException {

        String select_query = "SELECT genericContactId, contactType, contactId, companyId FROM sc_contact_genericcontact WHERE companyId = ?";
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            prepStmt = con.prepareStatement(select_query);
            prepStmt.setInt(1, Integer.parseInt(companyPK.getId()));
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                companyPK.setId(String.valueOf(rs.getInt("genericContactId")));
                return resultSet2GenericContact(rs, (WAPrimaryKey) companyPK);
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
/*
    *//**
     * Deletes the company identified by the specified primary key from the data source onto which the given connection is opened.
     *
     * @param con the connection to the data source.
     * @param pk  the unique identifier of the company in the data source.
     * @throws java.sql.SQLException if an error occurs while removing the company from the data source.
     *//*
    public void deleteComment(Connection con, CommentPK pk) throws SQLException {
        String delete_query = "DELETE FROM sb_comment_comment WHERE commentId = ?";
        PreparedStatement prep_stmt = null;
        prep_stmt = con.prepareStatement(delete_query);
        try {
            prep_stmt.setInt(1, Integer.parseInt(pk.getId()));
            prep_stmt.executeUpdate();
        } finally {
            DBUtil.close(prep_stmt);
        }
    }

    *//**
     * Moves company. (Requires more explanation!)
     *
     * @param con    the connection to the data source.
     * @param fromPK the source unique identifier of the company in the data source.
     * @param toPK   the destination unique identifier of another company in the data source.
     * @throws java.sql.SQLException if an error occurs during the operation.
     *//*
    public void moveComments(Connection con, ForeignPK fromPK, ForeignPK toPK)
            throws SQLException {
        String update_query = "UPDATE sb_comment_comment SET foreignId=?, instanceId=? WHERE "
                + "foreignId=? AND instanceId=?";
        PreparedStatement prep_stmt = null;
        try {
            prep_stmt = con.prepareStatement(update_query);
            prep_stmt.setInt(1, Integer.parseInt(toPK.getId()));
            prep_stmt.setString(2, toPK.getInstanceId());
            prep_stmt.setInt(3, Integer.parseInt(fromPK.getId()));
            prep_stmt.setString(4, fromPK.getInstanceId());
            prep_stmt.executeUpdate();
            prep_stmt.close();
        } finally {
            DBUtil.close(prep_stmt);
        }
    }

    *//**
     * Gets the company identified by the specified identifier.
     *
     * @param con the connection to use for getting the company.
     * @param pk  the identifier of the company in the data source.
     * @return the company or null if no such company is found.
     * @throws java.sql.SQLException if an error occurs during the company fetching.
     *//*
    public Comment getComment(Connection con, CommentPK pk) throws SQLException {
        String select_query = "SELECT commentOwnerId, commentCreationDate, commentModificationDate, "
                + "commentComment, foreignId, instanceId FROM sb_comment_comment WHERE commentId = ?";
        PreparedStatement prep_stmt = null;
        ResultSet rs = null;
        try {
            prep_stmt = con.prepareStatement(select_query);
            prep_stmt.setInt(1, Integer.parseInt(pk.getId()));
            rs = prep_stmt.executeQuery();
            if (rs.next()) {
                pk.setComponentName(rs.getString("instanceId"));
                WAPrimaryKey father_id = new CommentPK(String.valueOf(rs.getInt("foreignId")));
                return new Comment(pk, father_id, rs.getInt("commentOwnerId"), "",
                        rs.getString("commentComment"), rs.getString("commentCreationDate"),
                        rs.getString("commentModificationDate"));
            }
            return null;
        } finally {
            DBUtil.close(rs, prep_stmt);
        }
    }

    public List<CommentedPublicationInfo> getMostCommentedAllPublications(Connection con)
            throws SQLException {
        String select_query = "SELECT COUNT(commentId) as nb_comment, foreignId, instanceId FROM "
                + "sb_comment_comment GROUP BY foreignId, instanceId ORDER BY nb_comment desc;";
        Statement prep_stmt = null;
        ResultSet rs = null;
        List<CommentedPublicationInfo> listPublisCommentsCount = new ArrayList<CommentedPublicationInfo>();
        try {
            prep_stmt = con.createStatement();
            rs = prep_stmt.executeQuery(select_query);
            while (rs.next()) {
                Integer countComment = Integer.valueOf(rs.getInt("nb_comment"));
                Integer foreignId = Integer.valueOf(rs.getInt("foreignId"));
                String instanceId = rs.getString("instanceId");
                listPublisCommentsCount.add(new CommentedPublicationInfo(
                        foreignId.toString(), instanceId, countComment.intValue()));
            }
        } finally {
            DBUtil.close(rs, prep_stmt);
        }

        return listPublisCommentsCount;

    }

    public int getCommentsCount(Connection con, WAPrimaryKey foreign_pk)
            throws SQLException {
        String select_query = "SELECT COUNT(commentId) AS nb_comment FROM sb_comment_comment "
                + "WHERE instanceId = ? AND foreignid = ?";
        PreparedStatement prep_stmt = null;
        ResultSet rs = null;
        int commentsCount = 0;
        try {
            prep_stmt = con.prepareStatement(select_query);
            prep_stmt.setString(1, foreign_pk.getComponentName());
            prep_stmt.setInt(2, Integer.parseInt(foreign_pk.getId()));
            rs = prep_stmt.executeQuery();
            while (rs.next()) {
                commentsCount = rs.getInt("nb_comment");
            }
        } catch (Exception e) {
            SilverTrace.error("comment", getClass().getSimpleName() + ".getCommentsCount()",
                    "root.EX_NO_MESSAGE", e);
        } finally {
            DBUtil.close(rs, prep_stmt);
        }

        return commentsCount;
    }

    public List<Comment> getAllComments(Connection con, WAPrimaryKey foreign_pk)
            throws SQLException {
        String select_query =
                "SELECT commentId, commentOwnerId, commentCreationDate, commentModificationDate, "
                        + "commentComment, foreignId, instanceId FROM sb_comment_comment WHERE foreignId = ? "
                        + "AND instanceId = ? ORDER BY commentCreationDate DESC, commentId DESC";
        PreparedStatement prep_stmt = null;
        ResultSet rs = null;
        List<Comment> comments = new ArrayList<Comment>(INITIAL_CAPACITY);
        try {
            prep_stmt = con.prepareStatement(select_query);
            prep_stmt.setInt(1, Integer.parseInt(foreign_pk.getId()));
            prep_stmt.setString(2, foreign_pk.getComponentName());
            rs = prep_stmt.executeQuery();
            CommentPK pk;
            Comment cmt = null;
            while (rs.next()) {
                pk = new CommentPK(String.valueOf(rs.getInt("commentId")));
                pk.setComponentName(rs.getString("instanceId"));
                WAPrimaryKey father_id = (WAPrimaryKey) new CommentPK(String.valueOf(rs.getInt("foreignId")));
                cmt = new Comment(pk, father_id, rs.getInt("commentOwnerId"), "", rs.getString(
                        "commentComment"), rs.getString("commentCreationDate"),
                        rs.getString("commentModificationDate"));
                comments.add(cmt);
            }
        } finally {
            DBUtil.close(rs, prep_stmt);
        }

        return comments;
    }

    public void deleteAllComments(Connection con, ForeignPK pk)
            throws SQLException {
        String delete_query = "DELETE FROM sb_comment_comment WHERE foreignId = ? AND instanceId = ? ";
        PreparedStatement prep_stmt = null;
        try {
            prep_stmt = con.prepareStatement(delete_query);
            prep_stmt.setInt(1, Integer.parseInt(pk.getId()));
            prep_stmt.setString(2, pk.getInstanceId());
            prep_stmt.executeUpdate();
        } finally {
            DBUtil.close(prep_stmt);
        }
    }*/


}