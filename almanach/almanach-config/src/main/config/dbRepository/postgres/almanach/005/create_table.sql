CREATE TABLE SC_Almanach_Event 
(
	eventId			int		NOT NULL ,
	eventName		varchar (2000),
	eventStartDay		varchar (10)	NOT NULL ,
	eventEndDay		varchar (10)	NULL ,
	eventDelegatorId	varchar (100)	NOT NULL ,
	eventPriority		int		NOT NULL ,
	eventTitle		varchar (2000)	NOT NULL ,
	instanceId		varchar (50),
	eventStartHour		varchar (5),
	eventEndHour		varchar (5),
	eventPlace		varchar (200),
	eventUrl		varchar (200)
);

CREATE TABLE SC_Almanach_Periodicity 
(
	id			int		NOT NULL ,
	eventId			int		NOT NULL ,
	unity			int		NOT NULL ,
	frequency		int		NOT NULL ,
	daysWeekBinary		char(7),
	numWeek			int,
	day			int,
	untilDatePeriod		varchar (10)		
);

CREATE TABLE SC_Almanach_PeriodicityExcept
(
	id				int		NOT NULL ,
	periodicityId			int		NOT NULL ,
	beginDateException		varchar (10),
	endDateException 		varchar (10)
);
