CREATE TABLE SC_MyDB_ConnectInfo
(
  id             int          NOT NULL,
  dataSource       varchar(250) NULL,
  login          varchar(250) NULL,
  password       varchar(250) NULL,
  tableName      varchar(100) NULL,
  rowLimit       int          NULL,
  instanceId     varchar(50)  NOT NULL,
  CONSTRAINT PK_MyDB_ConnectInfo PRIMARY KEY (id)
);