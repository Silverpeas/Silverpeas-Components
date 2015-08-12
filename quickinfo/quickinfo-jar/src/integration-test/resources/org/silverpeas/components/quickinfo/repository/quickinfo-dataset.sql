INSERT INTO sc_quickinfo_news (id, instanceId, foreignId, important, broadcastTicker, broadcastMandatory,
                               createDate, createdBy, lastUpdateDate, lastUpdatedBy, publishDate,
                               publishedBy, version)
    VALUES ('news_1', 'quickinfo1', '123', true, false, false, '2013-11-21 09:57:30.300', '1',
            '2013-11-21 09:57:30.300', '1', '2014-02-01 09:57:30.300', '1', 0);

INSERT INTO sc_quickinfo_news (id, instanceId, foreignId, important, broadcastTicker, broadcastMandatory,
                               createDate, createdBy, lastUpdateDate, lastUpdatedBy, publishDate,
                               publishedBy, version)
    VALUES ('news_2', 'quickinfo1', '128', true, true, true, '2013-11-21 09:57:30.300', '1',
        '2013-11-21 09:57:30.300', '1', '2014-02-15 09:57:30.300', '1', 0);

INSERT INTO sc_quickinfo_news (id, instanceId, foreignId, important, broadcastTicker, broadcastMandatory,
                               createDate, createdBy, lastUpdateDate, lastUpdatedBy, publishDate,
                               publishedBy, version)
VALUES ('news_3', 'quickinfo2', '256', true, true, true, '2013-11-21 09:57:30.300', '1',
        '2013-11-21 09:57:30.300', '1', '2014-11-22 09:57:30.300', '1', 0);

INSERT INTO st_user (id, domainId, specificId, lastName, login, accessLevel, state, stateSaveDate)
    VALUES (1, 0, '1', 'Administrateur', 'SilverAdmin', 'A', 'VALID', '2012-01-01 00:00:00.000');
