CREATE TABLE SC_Almanach_Event 
(
	eventId			int		NOT NULL ,
	eventName		varchar (2000)	NOT NULL ,
	eventStartDay		varchar (10)	NOT NULL ,
	eventEndDay		varchar (10)	NULL ,
	eventDelegatorId	varchar (100)	NOT NULL ,
	eventPriority		int		NOT NULL ,
	eventTitle		varchar (2000)	NOT NULL ,
	instanceId		varchar (50)
);