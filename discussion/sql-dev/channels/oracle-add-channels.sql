ALTER TABLE sc_discussion_conversation ADD channelName VARCHAR2(80);
ALTER TABLE sc_discussion_conversation ADD channelDescription VARCHAR2(255);
ALTER TABLE sc_discussion_conversation ADD archived NUMBER(1, 0) DEFAULT 0 NOT NULL;
ALTER TABLE sc_discussion_read ADD notificationMode VARCHAR2(10) DEFAULT 'ALL' NOT NULL;
