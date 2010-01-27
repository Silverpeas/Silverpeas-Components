CREATE TABLE SC_Almanach_Event 
(
	eventId			number(10)	NOT NULL ,
	eventName		varchar2 (2000)	NOT NULL ,
	eventStartDay		varchar2 (10)	NOT NULL ,
	eventEndDay		varchar2 (10)	NULL ,
	eventDelegatorId	varchar2 (100)	NOT NULL ,
	eventPriority		number(10)	NOT NULL ,
	eventTitle		varchar2 (2000)	NOT NULL ,
	instanceId		varchar2 (50),
	eventStartHour		varchar2 (5),
	eventEndHour		varchar2 (5),
	eventPlace		varchar2 (200),
	eventUrl		varchar2 (200)
);