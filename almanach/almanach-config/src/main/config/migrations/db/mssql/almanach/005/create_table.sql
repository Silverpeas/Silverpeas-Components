CREATE TABLE SC_Almanach_Event
(
  eventId          INT           NOT NULL,
  eventName        VARCHAR(2000),
  eventStartDay    VARCHAR(10)   NOT NULL,
  eventEndDay      VARCHAR(10)   NULL,
  eventDelegatorId VARCHAR(100)  NOT NULL,
  eventPriority    INT           NOT NULL,
  eventTitle       VARCHAR(2000) NOT NULL,
  instanceId       VARCHAR(50),
  eventStartHour   VARCHAR(5),
  eventEndHour     VARCHAR(5),
  eventPlace       VARCHAR(200),
  eventUrl         VARCHAR(200)
);

CREATE TABLE SC_Almanach_Periodicity
(
  id              INT NOT NULL,
  eventId         INT NOT NULL,
  unity           INT NOT NULL,
  frequency       INT NOT NULL,
  daysWeekBinary  CHAR(7),
  numWeek         INT,
  day             INT,
  untilDatePeriod VARCHAR(10)
);

CREATE TABLE SC_Almanach_PeriodicityExcept
(
  id                 INT NOT NULL,
  periodicityId      INT NOT NULL,
  beginDateException VARCHAR(10),
  endDateException   VARCHAR(10)
);
