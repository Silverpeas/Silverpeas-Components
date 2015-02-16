CREATE TABLE SC_FormsOnline_Forms
(
	id integer NOT NULL,
	xmlFormName character varying(80) NOT NULL,
	name character varying(80) NOT NULL,
	description character varying(200),
	creationDate date NOT NULL,
	state integer NOT NULL,
	instanceId character varying(80) NOT NULL,
	alreadyUsed smallint NOT NULL DEFAULT 0,
	creatorId character varying(20) NOT NULL,
	title character varying(200) NOT NULL DEFAULT ''::character varying
);

CREATE TABLE SC_FormsOnline_FormInstances
(
	id integer NOT NULL,
	formId integer NOT NULL,
	state integer NOT NULL,
	creatorId character varying(20) NOT NULL,
	creationDate date NOT NULL,
	validatorId character varying(20),
	validationDate date,
	comments character varying(1000),
	instanceId character varying(50) NOT NULL
);

CREATE TABLE SC_FormsOnline_GroupRights
(
	formId integer NOT NULL,
	instanceId character varying(80) NOT NULL,
	groupId character varying(20) NOT NULL,
	rightType character varying(1) NOT NULL
);

CREATE TABLE SC_FormsOnline_UserRights
(
	formId integer NOT NULL,
	instanceId character varying(80) NOT NULL,
	userId character varying(20) NOT NULL,
	rightType character varying(1) NOT NULL
);


