
CREATE TABLE SC_Almanach_Periodicity 
(
	id			int		NOT NULL ,
	eventId			int		NOT NULL ,
	unity			int		NOT NULL ,
	frequency		int		NOT NULL ,
	daysWeekBinary		char(7),
	numWeek int,
	day int,
	untilDatePeriod		varchar (10)		
);

CREATE TABLE SC_Almanach_PeriodicityExcept
(
	id				int		NOT NULL ,
	periodicityId	int		NOT NULL ,
	beginDateException		varchar (10),
	endDateException 		varchar (10)
);
