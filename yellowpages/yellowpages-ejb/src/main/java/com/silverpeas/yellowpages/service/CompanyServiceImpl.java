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
    public Company saveCompany(String instanceId, String name, String email, String phone, String fax) {
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
    public void deleteCompany(int id) {
        Company companyToDelete = this.getCompany(id);
        try {
            companyDao.delete(companyToDelete);

            // suppression du generic contact s'il existe
            GenericContact gc = genericContactDao.findGenericContactFromCompanyId(id);
            if (gc != null) {
                genericContactDao.delete(gc);
                // suppression des relations du generic contact s'il en a
                List<GenericContactRelation> relationList = genericContactRelationDao.findByGenericCompanyId(gc.getGenericcontactId());
                if (relationList != null && relationList.size() > 0) {
                    genericContactRelationDao.delete(relationList);
                }
            }
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
                GenericContact newGCCompany = new GenericContact(GenericContact.TYPE_COMPANY, 0, companyId);
                gcCompany = genericContactDao.save(newGCCompany);
            }

            // Recuperation du contact dans la table generique
            GenericContact gcContact = genericContactDao.findGenericContactFromContactId(contactId);

            // Creation de la relation entre les deux
            GenericContactRelation relation = new GenericContactRelation(gcContact.getGenericcontactId(), gcCompany.getGenericcontactId(), GenericContactRelation.RELATION_TYPE_BELONGS_TO, GenericContactRelation.ENABLE_TRUE);
            genericContactRelationDao.save(relation);
        } catch (Exception e) {
            throw new YellowpagesRuntimeException("CompanyService.addContactToCompany()", SilverpeasRuntimeException.ERROR, "yellowpages.EX_ADD_COMPANY_TO_CONTACT_FAILED", e);
        }
    }

    @Override
    public void removeContactFromCompany(int companyId, int contactId) {
        try {
            // Recuperation de la company dans la table generique (creation si elle n'existe pas encore)
            GenericContact myGCCompany = genericContactDao.findGenericContactFromCompanyId(companyId);

            // Recuperation de la company dans la table generique (creation si elle n'existe pas encore)
            GenericContact myGCContact = genericContactDao.findGenericContactFromContactId(contactId);

            GenericContactRelation relationToDelete = new GenericContactRelation(myGCContact.getGenericcontactId(), myGCCompany.getGenericcontactId(), GenericContactRelation.RELATION_TYPE_BELONGS_TO, GenericContactRelation.ENABLE_TRUE);
            genericContactRelationDao.delete(relationToDelete);
        } catch (Exception e) {
            throw new YellowpagesRuntimeException("CompanyService.removeContactFromCompany()", SilverpeasRuntimeException.ERROR, "yellowpages.EX_REMOVE_COMPANY_FROM_CONTACT_FAILED", e);
        }
    }

    @Override
    public List<Company> findCompanyListByContactId(int contactId) {
        GenericContact gc = genericContactDao.findGenericContactFromContactId(contactId);
        List<GenericContactRelation> listeRelations = genericContactRelationDao.findByGenericContactId(gc.getGenericcontactId());
        List<Company> returnList = new ArrayList<Company>();
        for (GenericContactRelation relation : listeRelations) {
            if (relation.getEnabled() == GenericContactRelation.ENABLE_TRUE) {
                GenericContact gcCompany = genericContactDao.findOne(relation.getGenericCompanyId());
                returnList.add(companyDao.findOne(gcCompany.getCompanyId()));
            }
        }
        return returnList;
    }

}
