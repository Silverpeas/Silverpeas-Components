create table sc_scheduleevent_list (
  id varchar(255) not null,
  title varchar(255) not null,
  description varchar(4000),
  creationdate datetime not null,
  status int not null,
  creatorid integer not null
);

create table sc_scheduleevent_options (
  id varchar(255) not null,
  scheduleeventid varchar(255) not null,
  optionday datetime not null,
  optionhour int not null
);

create table sc_scheduleevent_contributor (
  id varchar(255) not null,
  scheduleeventid varchar(255) not null,
  userid int not null,
  lastvalidation datetime,
  lastvisit datetime
);

create table sc_scheduleevent_response (
  id varchar(255) not null,
  scheduleeventid varchar(255) not null,
  userid int not null,
  optionid varchar(255) not null
);