CREATE TABLE SC_DataWarning
(
id 			    int not null,
JDBCDriverName 	varchar(250),
JDBCUrl 		varchar(250),
login 			varchar(250),
pwd		 		varchar(250),
rowLimit 		int,
instanceId 		varchar(50) not null,
analysisType 	int default 0,
description 	varchar(256)
);



CREATE TABLE SC_DataWarning_Rel_Group
(
id		    int not null,
instanceId 	varchar(50) not null,
groupId 	int not null
);



CREATE TABLE SC_DataWarning_Rel_User
(
id		    int not null,
instanceId	varchar(50) not null,
userId		int not null
);



CREATE TABLE SC_DataWarning_Query
(
id			    int not null,
instanceId		varchar(50) not null,
description		varchar(1000),
query			varchar(4000),
queryCondition	int default 0,
type			int default 0,
theTrigger		numeric(18) default 0,
theTriggerCondition	int,
theTriggerPrecedent	numeric(18),
persoUID        varchar(50) NULL,
persoColNB      int NOT NULL DEFAULT (1),
persoValid      int NOT NULL DEFAULT (0)
);




CREATE TABLE SC_DataWarning_Scheduler
(
id			    int not null,
instanceId		varchar(50) not null,
numberOfTimes	int,
numberOfTimesMoment	int,
minits			int,
hours			int,
dayOfWeek		int,
dayOfMonth		int,
theMonth		int,
schedulerState	int default 0, 
wakeUp          numeric(18)
);
