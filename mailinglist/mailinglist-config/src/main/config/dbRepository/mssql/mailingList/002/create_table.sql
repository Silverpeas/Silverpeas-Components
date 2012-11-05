    create table sc_mailinglist_attachment (
        id varchar(255) not null,
        version int not null,
        attachmentSize numeric(19,0) null,
        attachmentPath varchar(255) null,
        fileName varchar(255) null,
        contentType varchar(255) null,
        md5Signature varchar(255) null,
        messageId varchar(255) null
    );

    create table sc_mailinglist_external_user (
        id varchar(255) not null,
        version int not null,
        componentId varchar(255) null,
        email varchar(255) not null,
        listId varchar(255) null
    );

    create table sc_mailinglist_list (
        id varchar(255) not null,
        version int not null,
        componentId varchar(255) null
    );

    create table sc_mailinglist_message (
        id varchar(255) not null,
        version int not null,
        mailId varchar(255) not null,
        componentId varchar(255) not null,
        title varchar(255) null,
        summary varchar(255) null,
        sender varchar(255) null,
        sentDate datetime null,
        referenceId varchar(255) null,
        moderated tinyint null,
        contentType varchar(255) null,
        attachmentsSize numeric(19,0) null,
        messageYear int null,
        messageMonth int null,
        body text null
    );

    create table sc_mailinglist_internal_subscriber (
        id varchar(255) not null,
        version int not null,
        subscriber_type varchar(255) not null,
        externalid varchar(255) not null,
        mailinglistid varchar(255) not null
    );

