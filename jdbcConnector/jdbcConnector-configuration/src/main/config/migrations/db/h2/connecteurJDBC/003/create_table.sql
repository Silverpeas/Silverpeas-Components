CREATE TABLE SC_ConnecteurJDBC_ConnectInfo
(
  id             INT           NOT NULL,
  dataSource     VARCHAR(250)  NULL,
  login          VARCHAR(250)  NULL,
  password       VARCHAR(250)  NULL,
  SQLreq         VARCHAR(4000) NULL,
  rowlimit       INT           NULL,
  instanceId     VARCHAR(50)   NOT NULL
);