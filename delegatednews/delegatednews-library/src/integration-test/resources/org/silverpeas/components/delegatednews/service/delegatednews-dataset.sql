INSERT INTO st_componentinstance (id, spaceId, name, componentName, createdBy, createTime, lang)
VALUES (1, 1, 'Our Documents', 'kmelia', 0, '1464811876258', 'en');

INSERT INTO st_componentinstance (id, spaceId, name, componentName, createdBy, createTime, lang)
  VALUES (2, 1, 'Delegated News', 'delegatednews', 0, '1464811876258', 'en');

INSERT INTO sc_delegatednews_news (pubId, instanceId, status, contributorId, beginDate, newsOrder)
  VALUES (1, 'kmelia1', 'ToValidate', '1', '2012-09-17 00:00:00.000', 2);

INSERT INTO sc_delegatednews_news (pubId, instanceId, status, contributorId, beginDate, newsOrder)
  VALUES (2, 'kmelia1', 'Valid', '1', '2012-09-17 00:00:00.000', 1);

INSERT INTO sc_delegatednews_news (pubId, instanceId, status, contributorId, beginDate, newsOrder)
  VALUES (3, 'kmelia1', 'Valid', '1', '2012-09-17 00:00:00.000', 0);