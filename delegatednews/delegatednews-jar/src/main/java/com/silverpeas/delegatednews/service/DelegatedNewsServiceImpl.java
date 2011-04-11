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

package com.silverpeas.delegatednews.service;

import com.silverpeas.delegatednews.model.DelegatedNews;
import java.util.Date;
import java.util.List;
import com.silverpeas.delegatednews.dao.DelegatedNewsDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional
public class DelegatedNewsServiceImpl implements DelegatedNewsService {
  
	@Inject
	private DelegatedNewsDao dao;
	
	/**
   * Ajout d'une actualité déléguée
   *
   */
	@Override
	public void addDelegatedNews(int pubId, String instanceId, String contributorId, Date validationDate) {
		DelegatedNews delegatedNews = new DelegatedNews(pubId, instanceId, contributorId, validationDate);
		dao.saveAndFlush(delegatedNews);
	}
	
	/**
   * Récupère une actualité déléguée correspondant à la publication Theme Tracker passée en paramètre
   *
   * @param pubId : l'id de la publication de Theme Tracker
   * @return DelegatedNews : l'objet correspondant à l'actualité déléguée ou null si elle n'existe pas
   */
	@Override
	public DelegatedNews getDelegatedNews(int pubId) {
	  DelegatedNews delegatedNews = dao.readByPrimaryKey(Integer.valueOf(pubId));
	  return delegatedNews;
	}
	
	/**
   * Récupère toutes les actualités déléguées inter Theme Tracker
   *
   * @return List<DelegatedNews> : liste d'actualités déléguées
   */
	@Override
  public List<DelegatedNews> getAllDelegatedNews() {
    List<DelegatedNews> list = dao.readAll(); //TODO utiliser plutot readAll(Sort) sur la date de validation 
    return list;
  }
  
  /**
   * Valide l'actualité déléguée passée en paramètre
   *
   */
	@Override
  public void validateDelegatedNews(int pubId) {
    DelegatedNews delegatedNews = dao.readByPrimaryKey(Integer.valueOf(pubId));
    delegatedNews.setStatus(DelegatedNews.NEWS_VALID);
    dao.saveAndFlush(delegatedNews);
  }

  /**
   * Refuse l'actualité déléguée passée en paramètre
   *
   */
	@Override
  public void refuseDelegatedNews(int pubId) {
    DelegatedNews delegatedNews = dao.readByPrimaryKey(Integer.valueOf(pubId));
    delegatedNews.setStatus(DelegatedNews.NEWS_REFUSED);
    dao.saveAndFlush(delegatedNews);
  }
  
  /**
   * Met à jour les dates de visibilité de l'actualité déléguée passée en paramètre
   *
   */
	@Override
  public void updateDateDelegatedNews(int pubId, Date dateHourBegin, Date dateHourEnd) {
    DelegatedNews delegatedNews = dao.readByPrimaryKey(Integer.valueOf(pubId));
    delegatedNews.setBeginDate(dateHourBegin);
    delegatedNews.setEndDate(dateHourEnd);
    dao.saveAndFlush(delegatedNews);
  }
}
