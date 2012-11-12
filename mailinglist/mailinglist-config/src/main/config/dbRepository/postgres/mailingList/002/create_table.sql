    create table sc_mailinglist_attachment (
        id varchar(255) not null,
        version int4 not null,
        attachmentSize int8,
        attachmentPath varchar(255),
        fileName varchar(255),
        contentType varchar(255),
        md5Signature varchar(255),
        messageId varchar(255)
    );

    create table sc_mailinglist_external_user (
        id varchar(255) not null,
        version int4 not null,
        componentId varchar(255),
        email varchar(255) not null,
        listId varchar(255)
    );

    create table sc_mailinglist_list (
        id varchar(255) not null,
        version int4 not null,
        componentId varchar(255)
    );

    create table sc_mailinglist_message (
        id varchar(255) not null,
        version int4 not null,
        mailId varchar(255) not null,
        componentId varchar(255) not null,
        title varchar(255),
        summary varchar(255),
        sender varchar(255),
        sentDate timestamp,
        referenceId varchar(255),
        moderated bool,
        contentType varchar(255),
        attachmentsSize int8,
        messageYear int4,
        messageMonth int4,
        body text
    );



    create table sc_mailinglist_internal_subscriber (
        id varchar(255) not null,
        version int4 not null,
        subscriber_type varchar(255) not null,
        externalid varchar(255) not null,
        mailinglistid varchar(255) not null
    );

