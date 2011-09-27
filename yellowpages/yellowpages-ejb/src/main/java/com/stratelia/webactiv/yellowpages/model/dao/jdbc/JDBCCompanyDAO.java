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
 * This program is distributed in the hospe that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.yellowpages.model.dao.jdbc;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.yellowpages.model.YellowpagesRuntimeException;
import com.stratelia.webactiv.yellowpages.model.beans.Company;
import com.stratelia.webactiv.yellowpages.model.beans.CompanyPK;
import com.stratelia.webactiv.yellowpages.model.beans.GenericContact;
import com.stratelia.webactiv.yellowpages.model.dao.CompanyDAO;

import javax.inject.Named;
import java.sql.Connection;
import java.util.List;

public class JDBCCompanyDAO implements CompanyDAO {

    @Override
    public CompanyPK saveCompany(Company company) {
        Connection con = openConnection();
        CompanyPK companyPK;
        try {
            JDBCCompanyRequester companyDAO = getCompanyDAO();
            // save company
            companyPK = companyDAO.saveCompany(con, company);
            if (companyPK == null) {
                throw new YellowpagesRuntimeException(getClass().getSimpleName() + ".saveCompany()",
                        SilverpeasRuntimeException.ERROR, "yellowpages.EX_CREATE_COMPANY_FAILED");
            }

        } catch (Exception re) {
            throw new YellowpagesRuntimeException(getClass().getSimpleName() + ".saveCompany()",
                    SilverpeasRuntimeException.ERROR, "yellowpages.EX_CREATE_COMPANY_FAILED", re);
        } finally {
            closeConnection(con);
        }
        return companyPK;
    }

    @Override
    public void updateCompany(Company company) {
        Connection con = openConnection();
        CompanyPK companyPK;
        try {
            JDBCCompanyRequester companyDAO = getCompanyDAO();
            int nbLinesUpdated = companyDAO.updateCompany(con, company);
            if (nbLinesUpdated <= 0) {
                throw new YellowpagesRuntimeException(getClass().getSimpleName() + ".updateCompany()",
                        SilverpeasRuntimeException.ERROR, "yellowpages.EX_UPDATE_COMPANY_FAILED");
            }
            //return companyPK;
        } catch (Exception re) {
            throw new YellowpagesRuntimeException(getClass().getSimpleName() + ".saveCompany()",
                    SilverpeasRuntimeException.ERROR, "yellowpages.EX_UPDATE_COMPANY_FAILED", re);
        } finally {
            closeConnection(con);
        }
    }

    @Override
    public int deleteCompany(Company company) {
        Connection con = openConnection();
        CompanyPK companyPK;
        int nbLinesDeleted;
        try {
            JDBCCompanyRequester companyDAO = getCompanyDAO();
            nbLinesDeleted = companyDAO.updateCompany(con, company);
            if (nbLinesDeleted <= 0) {
                throw new YellowpagesRuntimeException(getClass().getSimpleName() + ".updateCompany()",
                        SilverpeasRuntimeException.ERROR, "yellowpages.EX_DELETE_COMPANY_FAILED");
            }
            //return companyPK;
        } catch (Exception re) {
            throw new YellowpagesRuntimeException(getClass().getSimpleName() + ".saveCompany()",
                    SilverpeasRuntimeException.ERROR, "yellowpages.EX_DELETE_COMPANY_FAILED", re);
        } finally {
            closeConnection(con);
        }
        return nbLinesDeleted;

    }

    @Override
    public void addCompanyToContact(Company company, int contactId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeCompanyFromContact(int contactId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Company> findCompanyListByContactId(int contactId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private Connection openConnection() {
        try {
            Connection con = DBUtil.makeConnection(JNDINames.NODE_DATASOURCE);
            return con;
        } catch (Exception e) {
            throw new YellowpagesRuntimeException("YellowpagesBmEJB.getConnection()", SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
        }
    }

    private void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
                throw new YellowpagesRuntimeException("YellowpagesBmEJB.closeConnection()", SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", e);
            }
        }
    }

    private JDBCCompanyRequester getCompanyDAO() {
        return new JDBCCompanyRequester();
    }

}
