CREATE TABLE sc_forums_forum
(
  forumid integer NOT NULL,
  forumname character varying(1000) NOT NULL,
  forumdescription character varying(2000),
  forumcreationdate character varying(50) NOT NULL,
  forumclosedate character varying(50),
  forumcreator character varying(255) NOT NULL,
  forumactive integer NOT NULL,
  forumparent integer NOT NULL DEFAULT 0,
  forummodes character varying(50),
  forumlocklevel integer,
  instanceid character varying(50) NOT NULL,
  categoryid character varying(50),
  CONSTRAINT pk_forums_forum PRIMARY KEY (forumid)
);

CREATE TABLE sc_forums_historyuser
(
  userid character varying(255) NOT NULL,
  messageid integer NOT NULL,
  lastaccess character varying(50) NOT NULL,
  CONSTRAINT pk_forums_historyuser PRIMARY KEY (userid, messageid)
);

CREATE TABLE sc_forums_message
(
  messageid integer NOT NULL,
  messagetitle character varying(1000) NOT NULL,
  messageauthor character varying(255) NOT NULL,
  forumid integer NOT NULL,
  messageparentid integer,
  messagedate timestamp without time zone,
  CONSTRAINT pk_forums_message PRIMARY KEY (messageid)
);

CREATE TABLE sc_forums_rights
(
  userid character varying(255) NOT NULL,
  forumid character varying(255) NOT NULL,
  CONSTRAINT pk_forums_rights PRIMARY KEY (userid, forumid)
);

CREATE TABLE sc_forums_subscription
(
  userid character varying(255) NOT NULL,
  messageid character varying(255) NOT NULL,
  CONSTRAINT pk_forums_subscription PRIMARY KEY (userid, messageid)
);