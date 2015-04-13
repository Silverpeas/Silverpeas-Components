ALTER TABLE sc_connecteurjdbc_connectinfo DROP COLUMN JDBCdriverName;
ALTER TABLE sc_connecteurjdbc_connectinfo DROP COLUMN JDBCurl;
ALTER TABLE sc_connecteurjdbc_connectinfo ADD dataSource VARCHAR(250) NULL;
