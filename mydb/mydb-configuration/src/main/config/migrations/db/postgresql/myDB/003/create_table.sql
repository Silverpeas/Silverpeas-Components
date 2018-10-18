CREATE TABLE SC_MyDB_ConnectInfo
(
  id             INT           NOT NULL,
  dataSource     VARCHAR(250)  NULL,
  login          VARCHAR(250)  NULL,
  password       VARCHAR(250)  NULL,
  tableName      VARCHAR(100)  NULL,
  rowlimit       INT           NULL,
  instanceId     VARCHAR(50)   NOT NULL,
  CONSTRAINT PK_MyDB_ConnectInfo PRIMARY KEY (id)
);