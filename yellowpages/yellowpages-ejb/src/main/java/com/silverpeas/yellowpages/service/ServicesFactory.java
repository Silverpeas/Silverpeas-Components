/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

package com.silverpeas.yellowpages.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class ServicesFactory implements ApplicationContextAware {

    public static final String COMPANY_SERVICE = "companyService";
    //public static final String GENERICCONTACT_SERVICE = "genericContactService";
    //public static final String GENERICCONTACT_RELATION_SERVICE = "genericContactRelationService";

    private ApplicationContext context;
    private static ServicesFactory instance;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.context = ctx;
    }

    private ServicesFactory() {
    }

    protected static ServicesFactory getInstance() {
        synchronized (ServicesFactory.class) {
            if (ServicesFactory.instance == null) {
                ServicesFactory.instance = new ServicesFactory();
            }
        }
        return ServicesFactory.instance;
    }

    public static CompanyService getCompanyService() {
        return (CompanyService) getInstance().context.getBean(COMPANY_SERVICE);
    }

/*    public static CompanyService getGenericContactService() {
        return (CompanyService) getInstance().context.getBean(GENERICCONTACT_SERVICE);
    }

    public static CompanyService getGenericContactRelationService() {
        return (CompanyService) getInstance().context.getBean(GENERICCONTACT_RELATION_SERVICE);
    }*/
}
