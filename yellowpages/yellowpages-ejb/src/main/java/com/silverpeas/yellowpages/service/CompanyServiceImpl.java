package com.silverpeas.yellowpages.service;

import com.silverpeas.yellowpages.dao.CompanyDao;
import com.silverpeas.yellowpages.dao.GenericContactDao;
import com.silverpeas.yellowpages.dao.GenericContactRelationDao;
import com.silverpeas.yellowpages.model.Company;
import com.silverpeas.yellowpages.model.GenericContact;
import com.silverpeas.yellowpages.model.GenericContactRelation;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.yellowpages.model.YellowpagesRuntimeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CompanyServiceImpl implements CompanyService {

    @Inject
    private CompanyDao companyDao;

    @Inject
    private GenericContactDao genericContactDao;

    @Inject
    private GenericContactRelationDao genericContactRelationDao;

    @Override
    public Company createCompany(String instanceId, String name, String email, String phone, String fax) {
        Company company = new Company(instanceId, name, email, phone, fax);
        Company savedCompany = null;
        try {
            savedCompany = companyDao.save(company);
            Company companyFromDb = companyDao.findOne(savedCompany.getCompanyId());
            GenericContact gc = new GenericContact(GenericContact.TYPE_COMPANY, null, companyFromDb.getCompanyId());
            genericContactDao.save(gc);

        } catch (Exception e) {
            throw new YellowpagesRuntimeException("CompanyService.createCompany()", SilverpeasRuntimeException.ERROR, "yellowpages.EX_CREATE_COMPANY_FAILED", e);
        }

        return savedCompany;
    }

    @Override
    public Company saveCompany(int id, String name, String email, String phone, String fax) {
        try {
            Company company = companyDao.findOne(id);
            company.setName(name);
            company.setEmail(email);
            company.setPhone(phone);
            company.setFax(fax);
            return companyDao.save(company);
        } catch (Exception e) {
            throw new YellowpagesRuntimeException("CompanyService.saveCompany()", SilverpeasRuntimeException.ERROR, "yellowpages.EX_UPDATE_COMPANY_FAILED", e);
        }
    }

    @Override
    public void deleteCompany(int id) {
        Company companyToDelete = this.getCompany(id);
        try {
            // suppression du generic contact s'il existe
            GenericContact gc = genericContactDao.findGenericContactFromCompanyId(id);
            if (gc != null) {
                // suppression des relations du generic contact s'il en a
                List<GenericContactRelation> relationList = genericContactRelationDao.findByGenericCompanyId(gc.getGenericContactId());
                if (relationList != null && relationList.size() > 0) {
                    genericContactRelationDao.delete(relationList);
                }
                genericContactDao.delete(gc);
            }
            // suppression finale de la company qui n'est plus liée à rien
            companyDao.delete(companyToDelete);

        } catch (Exception e) {
            throw new YellowpagesRuntimeException("CompanyService.deleteCompany()", SilverpeasRuntimeException.ERROR, "yellowpages.EX_DELETE_COMPANY_FAILED", e);
        }
    }


    @Override
    public Company getCompany(int id) {
        return companyDao.findOne(id);
    }

    @Override
    public void addContactToCompany(int companyId, int contactId) {
        // Recuperation de la company dans la table generique (creation si elle n'existe pas encore)
        try {
            GenericContact gcCompany = genericContactDao.findGenericContactFromCompanyId(companyId);
            if (gcCompany == null) {
                GenericContact newGCCompany = new GenericContact(GenericContact.TYPE_COMPANY, null, companyId);
                gcCompany = genericContactDao.save(newGCCompany);
            }

            // Recuperation du contact dans la table generique (creation si il n'existe pas)
            GenericContact gcContact = genericContactDao.findGenericContactFromContactId(contactId);
             if (gcContact == null) {
                GenericContact newGcContact = new GenericContact(GenericContact.TYPE_CONTACT, contactId, null);
                gcContact = genericContactDao.save(newGcContact);
            }

            // Creation de la relation entre les deux
            GenericContactRelation relation = new GenericContactRelation(gcContact.getGenericContactId(), gcCompany.getGenericContactId(), GenericContactRelation.RELATION_TYPE_BELONGS_TO, GenericContactRelation.ENABLE_TRUE);
            genericContactRelationDao.save(relation);
        } catch (Exception e) {
            throw new YellowpagesRuntimeException("CompanyService.addContactToCompany()", SilverpeasRuntimeException.ERROR, "yellowpages.EX_ADD_COMPANY_TO_CONTACT_FAILED", e);
        }
    }

    @Override
    public void removeContactFromCompany(int companyId, int contactId) {
        try {
            GenericContact gcCompany = genericContactDao.findGenericContactFromCompanyId(companyId);
            GenericContact gcContact = genericContactDao.findGenericContactFromContactId(contactId);

            if (gcCompany != null && gcContact != null) {
                // TODO à la place de supprimer, set enabled = false sur la relation ?
                GenericContactRelation relationToDelete = genericContactRelationDao.findByGenericCompanyIdAndGenericContactId(gcCompany.getGenericContactId(), gcContact.getGenericContactId());
                if (relationToDelete != null) {
                    genericContactRelationDao.delete(relationToDelete.getRelationId());
                }
            }
        } catch (Exception e) {
            throw new YellowpagesRuntimeException("CompanyService.removeContactFromCompany()", SilverpeasRuntimeException.ERROR, "yellowpages.EX_REMOVE_COMPANY_FROM_CONTACT_FAILED", e);
        }
    }

    @Override
    public List<Company> findCompanyListByContactId(int contactId) {
        List<Company> returnList = new ArrayList<Company>();
        GenericContact gc = genericContactDao.findGenericContactFromContactId(contactId);
        if (gc != null) {
            List<GenericContactRelation> listeRelations = genericContactRelationDao.findByGenericContactId(gc.getGenericContactId());
            for (GenericContactRelation relation : listeRelations) {
                if (relation.getEnabled() == GenericContactRelation.ENABLE_TRUE) {
                    GenericContact gcCompany = genericContactDao.findOne(relation.getGenericCompanyId());
                    returnList.add(companyDao.findOne(gcCompany.getCompanyId()));
                }
            }
        }
        return returnList;
    }

    @Override
    public List<Company> findAllCompanies() {
        return companyDao.findAll();
    }

    @Override
    public List<Company> findCompaniesByPattern(String pattern) {
        return companyDao.findCompanyListByPattern(pattern);
    }

}
