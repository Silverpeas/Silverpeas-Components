CREATE TABLE SC_FormsOnline_Forms
(
	id int NOT NULL,
	xmlFormName varchar(80) NOT NULL,
	name varchar(80) NOT NULL,
	description varchar(200),
	creationDate date NOT NULL,
	state int NOT NULL,
	instanceId varchar(80) NOT NULL,
	alreadyUsed smallint NOT NULL DEFAULT 0,
	creatorId varchar(20) NOT NULL,
	title varchar(200) NOT NULL DEFAULT ''
);

CREATE TABLE SC_FormsOnline_FormInstances
(
	id int NOT NULL,
	formId int NOT NULL,
	state int NOT NULL,
	creatorId varchar(20) NOT NULL,
	creationDate date NOT NULL,
	validatorId varchar(20),
	validationDate date,
	comments varchar(1000),
	instanceId varchar(50) NOT NULL
);

CREATE TABLE SC_FormsOnline_GroupRights
(
	formId int NOT NULL,
	instanceId varchar(80) NOT NULL,
	groupId varchar(20) NOT NULL,
	rightType varchar(1) NOT NULL
);

CREATE TABLE SC_FormsOnline_UserRights
(
	formId int NOT NULL,
	instanceId varchar(80) NOT NULL,
	userId varchar(20) NOT NULL,
	rightType varchar(1) NOT NULL
);