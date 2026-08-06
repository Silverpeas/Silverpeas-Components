# Silverpeas Components

**The applications shipped in standard with [Silverpeas](https://www.silverpeas.org).**

Silverpeas is a collaborative and social-networking portal. Its *Core* provides the platform
services — spaces, users and roles, contributions, indexing and search, notifications, workflow
engine, taxonomy (the *classification scheme*), attachments, etc. This repository provides the
**applications** (historically called *components*) that a Silverpeas administrator instantiates
inside a collaborative space to actually do something with those services: manage documents,
publish news, book resources, run a forum, expose an external database, and so on.

* Project: <https://www.silverpeas.org/docs/components>
* Source: <https://github.com/Silverpeas/Silverpeas-Components>
* Licence: GNU AGPL v3 with the [Silverpeas FLOSS exception](exceptions.txt)

| | |
|---|---|
| Current version | `6.5-SNAPSHOT` |
| Requires Silverpeas Core | `${core.version}` in the root POM (`6.5-SNAPSHOT`) |
| Java | 17 |
| Platform | Jakarta EE 10 on WildFly 34 |
| Build | Maven, parent POM `org.silverpeas:silverpeas-project` |
| Databases | PostgreSQL, MS-SQL Server, Oracle, H2 |

This repository is **not** a standalone application. Every module compiles against Silverpeas Core
in `provided` scope and is packaged as a WAR deployed into a running Silverpeas instance.

---

## Table of contents

1. [The applications](#the-applications)
2. [Repository layout](#repository-layout)
3. [Anatomy of an application](#anatomy-of-an-application)
4. [How an application plugs into Silverpeas Core](#how-an-application-plugs-into-silverpeas-core)
5. [Building](#building)
6. [Testing](#testing)
7. [Technical map of the applications](#technical-map-of-the-applications)
8. [Contributing](#contributing)
9. [Licence](#licence)

---

## The applications

The repository holds **32 Maven modules** producing **35 instantiable applications** (a few modules
declare several application descriptors), plus one sample workflow process model and one personal
tool. Silverpeas groups applications into functional *suites*, shown below.

Legend for the *Roles* column: the user profiles declared by the application descriptor. They are
mapped onto the space roles (`admin`, `publisher`, `writer`, `reader`) so that rights can be
inherited from the enclosing space.

### 01 — Document Management

| Application | Module | Roles | Description |
|---|---|---|---|
| **kmelia** — Document management (*Gestion documentaire I*) | `kmelia` | admin, publisher, writer, user | The flagship application of Silverpeas, a full EDM. Publications gather metadata, versioned or non-versioned attachments and a free (WYSIWYG) or structured (XML form) content, organised in a folder tree. Multi-step validation workflows, classification on the classification scheme, readers list, comments, subscriptions to a folder / a publication / a file. Interfaced with the platform workflow engine. |
| **toolbox** — Filebox (*Gestion documentaire II*) | `kmelia` | admin, publisher, user | A lighter face of Kmelia: federate and structure a simple set of files with basic metadata (title, author, date), manual classification on the classification scheme and folder subscriptions. |
| **kmax** — Multi-level Tracker (*Gestion de contenu multi-axes*) | `kmelia` | admin, publisher, writer, user | Also manages publications, but they are classified along several independent axes instead of a folder tree, so that users can cross-search by selecting values on each axis. |

### 02 — Collaborative Management

| Application | Module | Roles | Description |
|---|---|---|---|
| **almanach** — Calendar | `almanach` | admin, publisher, user | Creates and aggregates calendars from spaces and subspaces. Punctual or recurring events, several time scales, attachments, quick access to other calendars, iCal export. |
| **community** — Community | `community` | user | Automates users' access to a space: users request membership, managers approve, and the members group is maintained accordingly. The application instance must be set as the home page of the parent space. Not visible in the standard application catalogue (`visible=false`). |
| **formsOnline** — Online Forms | `formsOnline` | Administrator, SenderReceiver | Users submit structured requests built on fully customisable XML forms; recipients then validate or reject them. Notifications provide the follow-up of each request. |
| **forums** — Forum | `forums` | admin, user, reader | Classic forum features: threads on any theme, replies at any point of a thread, subscriptions, new-message indicators, highlighted users, last-message date, view counters, classification on the classification scheme. |
| **mailinglist** — Email archiver | `mailinglist` | admin, moderator, reader | Archives every email received at a given address, with optional moderation. Turning on the diffusion-list parameter transforms it into a real mailing list. |
| **projectManager** — Project Manager | `projectManager` | admin, responsable, lecteur | Hierarchies of tasks and sub-tasks, each with a person in charge and allocated resources. Allocation is computed automatically to reveal over-allocation; a Gantt chart gives the overview. |
| **quizz** — Quizz | `quizz` | admin, publisher, user | Series of closed questions with per-question scoring and optional hints (which lower the score). Right/wrong answers and a final mark build a podium. Useful for knowledge checks and e-learning. |
| **resourcesManager** — Resources manager | `resourcesManager` | admin, responsable, publisher | Manages bookable resources (rooms, vehicles, equipment…). Users book one or several resources; only those free on the requested dates are offered. Optional validation circuit. |
| **spaceMembers** — Space members | `spaceMembers` | user | Displays the directory of the users having rights declared in the enclosing space. |
| **suggestionBox** — Suggestion Box | `suggestionBox` | admin, publisher, writer, user | Everyone proposes suggestions, comments them and rates them. |
| **yellowpages** — Yellow pages directory (*Contacts*) | `yellowpages` | admin, publisher, user | Directory of contacts and companies, whether or not they are platform users. Each contact is described by a customisable XML form and organised in a tree of categories; CSV import/export; contacts can be attached to a company. |
| **survey** — Survey (*Enquête*) | `survey` | admin, publisher, userMultiple, user | Surveys made of questions, opened and closed on given dates, with participation and abstention rates. The `userMultiple` role allows a user to answer several times. |
| **pollingStation** — Polling station (*Consultation*) | `survey` | admin, publisher, user | The voting flavour of the same engine: a consultation where users cast a vote, with a count of voters. |

### 03 — Knowledge Management

| Application | Module | Roles | Description |
|---|---|---|---|
| **questionReply** — Questions/Answers (to experts) | `questionReply` | admin, publisher, writer, user | The FAQ principle extended: any user asks a question directly to the experts, who manage the diffusion of the answers. The knowledge base grows with each answer. |
| **whitePages** — Expert directory (*Annuaire d'experts*) | `whitePages` | admin, user | Find the users who are experts in a domain and get in touch with them. Experts are qualified through a customisable form and the classification scheme. |

### 04 — Content Management

| Application | Module | Roles | Description |
|---|---|---|---|
| **blog** — Blog | `blog` | admin, publisher, user | Posts organised by category or by date, with comments, subscriptions and an RSS feed. |
| **classifieds** — Classifieds (*Petites annonces*) | `classifieds` | admin, publisher, reader | Classified ads organised by category and type, with a customisable ad structure, optional moderation, comments and subscriptions per theme. |
| **delegatednews** — Delegated news | `delegatednews` | admin, user | Validates and publishes on the portal home page the news submitted from *quickinfo* instances whose news-management option is on. |
| **gallery** — Multimedia library (*Médiathèque*) | `gallery` | admin, publisher, writer, privilegedUser, user | Images, videos, sounds and streaming (YouTube, Vimeo) organised in albums, added by drag & drop. Advanced media management: customisable form, EXIF/IPTC data, batch processing. |
| **hyperlink** — Hyperlink | `hyperlink` | user | A link to a document or a web site — and, beyond the basic need, a client-side SSO mechanism towards web sites requiring an authentication. |
| **infoLetter** — Newsletter (*Lettre d'information*) | `infoLetter` | admin, publisher, user | Sends a newsletter to platform users or to external contacts, with CSV import/export of the contacts and a reusable publication template. |
| **quickinfo** — QuickInfo (*Actualités*) | `quickinfo` | admin, publisher, user | News made of a title and a rich description, automatically pushed to the home page and classifiable on the classification scheme. |
| **webPages** — Webpage | `webPages` | admin, publisher, user | Content pages (home page, presentation page…) with free WYSIWYG or structured content and a customisable layout. |
| **webSites** — WebSite Designer | `webSites` | Admin, Publisher, Reader | Create web pages and directories, upload images, and publish small web sites integrated into the platform. |
| **bookmark** — Bookmarks (*Annuaire de sites*) | `webSites` | Admin, Publisher, Reader | List and share the web sites useful to a project, a department or a team, organised in folders. |

### 05 — Workflow

| Application | Module | Roles | Description |
|---|---|---|---|
| **processManager** | `processManager` | per process model | The workflow engine front-end. Unlike the other applications it does not declare a single descriptor: **each workflow process model is itself an instantiable application** carrying the `workflow` behaviour. The repository ships one sample model, `demandeCongesSimple` (a simplified vacation-leave request, roles `Employe`, `Responsable`, `supervisor`), in `processManager-configuration/src/main/config/xmlcomponents/workflows/`. |

### 06 — Connectors

| Application | Module | Roles | Description |
|---|---|---|---|
| **connecteurJDBC** — JDBC Connector | `jdbcConnector` | admin, publisher, user | Displays the content of an existing database through an SQL request defined by the manager, who can also translate the request into natural language for the end users. |
| **myDB** — My DB | `mydb` | admin, publisher, user | Goes further than the JDBC connector: consult, create and update the business data of an existing relational table. Contributors edit, readers consult. |
| **dataWarning** — DataWarning | `dataWarning` | admin, publisher, user | Watches a data source and warns the users when the values go beyond a specified threshold. |
| **organizationchart** — LDAP-based organization chart | `organizationchart` | user | Renders an organisation chart from the data of an LDAP directory. |
| **orgchartGroup** — Group-based organization chart | `organizationchart` | user | Renders an organisation chart from the Silverpeas user groups. |
| **rssAgregator** — RSS aggregator | `rssAggregator` | admin, user | Aggregates several external RSS sources on a single page. |
| **silverCrawler** — Silvercrawler | `silverCrawler` | admin, publisher, user | Exposes any directory of the information system through the platform web interface: browsing, indexing and full-text search over the files. Read-only by default, write access can be granted. |

### Not instantiable in a space

| Feature | Module | Description |
|---|---|---|
| **scheduleEvent** | `scheduleEvent` | Date-scheduling poll (propose several dates, participants declare their availabilities, the best slot emerges). It declares no `WAComponent` descriptor: it is not instantiated in a space but reached as a personal tool, through the `/Rscheduleevent/` and `/ScheduleEvent/` routes. |

---

## Repository layout

```
Silverpeas-Components/
├── pom.xml                  # aggregator: the 32 component modules + shared build config
├── Jenkinsfile              # CI pipeline (build in the silverpeas/silverbuild image + SonarQube)
├── .devcontainer/           # dev container based on the silverpeas/silverdev image
├── src/site/                # the Maven site published on silverpeas.org
├── license.txt              # GNU AGPL v3
├── exceptions.txt           # Silverpeas FLOSS exception
└── <component>/             # one directory per application, e.g. kmelia/, blog/, gallery/
    ├── pom.xml
    ├── <component>-configuration/
    ├── <component>-library/
    └── <component>-war/
```

The 32 component directories are: `almanach`, `blog`, `classifieds`, `community`, `dataWarning`,
`delegatednews`, `formsOnline`, `forums`, `gallery`, `hyperlink`, `infoLetter`, `jdbcConnector`,
`kmelia`, `mailinglist`, `mydb`, `organizationchart`, `processManager`, `projectManager`,
`questionReply`, `quickinfo`, `quizz`, `resourcesManager`, `rssAggregator`, `scheduleEvent`,
`silverCrawler`, `spaceMembers`, `suggestionBox`, `survey`, `webPages`, `webSites`, `whitePages`,
`yellowpages`.

---

## Anatomy of an application

Every application is made of exactly three Maven modules. Taking `blog` as the example:

### `blog-configuration` — artifact `silverpeas-blog-configuration` (jar)

No Java: it packages `src/main/config` as resources, which Silverpeas Setup unpacks into the
Silverpeas home directory at installation time.

| Path | Purpose |
|---|---|
| `xmlcomponents/blog.xml` | The `WAComponent` descriptor: labels/descriptions/suite per language, user profiles and their mapping onto space roles, and the instance parameters offered to the administrator. This is what makes the application instantiable. |
| `properties/org/silverpeas/blog/multilang/blogBundle[_xx].properties` | UI localisation (fr, en, de). |
| `properties/org/silverpeas/blog/settings/blogSettings.properties` | Runtime settings. |
| `properties/org/silverpeas/blog/settings/blogIcons.properties` | Icon paths used by the UI. |
| `properties/org/silverpeas/util/logging/blogLogging.properties` | The logger namespace of the application. |
| `migrations/modules/blog-migration.xml` | Declares the current database schema version of the application. |
| `migrations/db/{h2,postgresql,mssql,oracle}/blog/<version>/*.sql` | The SQL scripts for each schema version — **all four databases must be kept in sync**. |
| `resources/StringTemplates/components/blog/*.st` | StringTemplate bodies of the user notifications, per language. |

### `blog-library` — artifact `silverpeas-blog` (jar)

The business layer, entirely CDI-managed (`META-INF/beans.xml` is required for bean discovery).

* `service/` — `@Service` beans (`org.silverpeas.core.annotation.Service`), usually a
  `<Name>Service` interface with a `Default<Name>Service` implementation, `@Transactional` where
  transactions are needed.
* `model/` — the domain objects. Newer applications use JPA entities, older ones plain value objects.
* `dao/` (legacy JDBC data access over `DBUtil` connections) **or** `repository/` (a `@Repository`
  interface with a `*JpaRepository` implementation). Both idioms are alive in the code base.
* `notification/` — the user-notification builders, bound to the StringTemplates of the
  configuration module.
* `<Name>InstancePostConstruction` / `<Name>InstancePreDestruction` — callbacks invoked by Core when
  an instance of the application is created in a space or deleted, to set up or clean per-instance data.
* Optionally: content managers (indexing/classification), statistics providers, event listeners,
  access-control extensions.

### `blog-war` — artifact `silverpeas-blog-war` (war)

The web layer. It bundles the library (`compile` scope); everything coming from Core is `provided`
since Core is deployed on the server.

* **Web controllers.** Two generations coexist:
  * *Legacy* — a `ComponentRequestRouter<XxxSessionController>` servlet declared in `WEB-INF/web.xml`
    under `/R<name>/*`, dispatching on a "function" name and forwarding to a JSP. The session-scoped
    `<Name>SessionController` holds the user's state.
  * *Modern* — a `WebComponentController<XxxWebRequestContext>` annotated
    `@WebComponentController(<name>)` and routed with JAX-RS-style annotations (`@Path`, `@GET`,
    `@POST`) combined with the Silverpeas navigation annotations `@Homepage`, `@LowestRoleAccess`,
    `@NavigationStep`, `@RedirectTo*`. Used by `almanach`, `community`, `jdbcConnector`, `mydb` and
    `suggestionBox`, and the way to go for new developments.
* **REST services** — `web/` holds `@WebService`-annotated JAX-RS resources exposing `*Entity` DTOs,
  consumed by the front-end.
* **Views** — JSPs in `src/main/webapp/<name>/jsp/`, JavaScript and CSS in
  `<name>/jsp/javaScript/`, including Vue.js components under `javaScript/vuejs/components/`.
* **Icons** — `webapp/util/icons/component/<name>{Small,Big}.{gif,png}`, looked up by the Silverpeas
  UI from the application name.
* **Other servlets** — permalink handlers (`GoToXxx`), RSS servlets, indexers.

---

## How an application plugs into Silverpeas Core

1. **Declaration.** The `WAComponent` XML descriptor is read by the Silverpeas administration, which
   offers the application in the catalogue of a space and renders its parameters and roles.
2. **Instantiation.** Creating an instance produces an identifier such as `blog42`
   (`<component name><number>`) used everywhere as the *component instance id*. The
   `<Name>InstancePostConstruction` hook is then called.
3. **Rights.** The declared profiles are mapped onto the space roles; the application may refine
   authorisation with its own `AccessController` extensions.
4. **Persistence.** Schema creation and upgrade are driven by the migration descriptor and its SQL
   scripts, replayed by Silverpeas Setup on installation and upgrade.
5. **Services.** Applications consume Core services by CDI injection or through
   `ServiceProvider`: publications, nodes, attachments, comments, subscriptions, user notifications,
   the classification scheme (PdC), indexing and search, statistics, the workflow engine.
6. **Web access.** Silverpeas routes the user to `/silverpeas/R<name>/<instanceId>/<function>` (or
   to the routes of the modern web controller) once the session and the component context are set up.

---

## Building

The supported build environments are the **dev container** shipped in `.devcontainer/` (based on the
`silverpeas/silverdev` image) and the `silverpeas/silverbuild` image used by the CI. Both provide the
right JDK, Maven configuration and a WildFly instance for the integration tests. Building on the host
is possible but requires the same setup by hand.

```bash
mvn clean install                       # build everything (unit tests only)
mvn clean install -PskipMinify          # skip JS/CSS minification — faster when iterating on assets
mvn install -pl blog/blog-library -am   # one module and its prerequisites
cd kmelia && mvn install                # one whole application
```

The build resolves Silverpeas Core in the version given by the `core.version` property of the root
POM. When working against a locally built Core, that property must match the version installed in
`~/.m2`. The CI rewrites it to the version of the Core build under test.

Useful profiles (most of them are defined by the parent POM):

| Profile | Effect |
|---|---|
| `skipMinify` | Disables the Silverpeas UI compressor (JS/CSS minification). |
| `deployment` | Attaches the sources and Javadoc jars. |
| `coverage` | Enables the JaCoCo agent and report. |
| `license` | Rewrites the AGPL header in every source file (`mvn generate-sources -Plicense`). |
| `integration-test` | Activated by `-Dcontext=ci`; see below. |
| `restapi` | Generates the REST API documentation with Miredot. |

---

## Testing

### Unit tests

Run by Surefire on `**/*Test.java` and `**/*TestSuite.java`, sources in `src/test/java`. They use
JUnit 5 and the Silverpeas `silverpeas-core-test` harness. The parent POM forces the `fr`/`FR`
locale and the `Europe/Paris` timezone, so date and number assertions are locale-sensitive.

```bash
mvn test -pl kmelia/kmelia-library
mvn test -pl kmelia/kmelia-library -Dtest=KmeliaValidationTest
```

### Integration tests

Run by Failsafe on `**/*IT.java` and `**/*ITSuite.java`, sources in `src/integration-test/java`.
They execute **inside WildFly** through Arquillian (JUnit 4 here, not JUnit 5), against an
**already running** server started with `standalone-full.xml` — the Arquillian container is
`wildfly-remote`. They only run when the `integration-test` profile is activated by `-Dcontext=ci`.

```bash
$JBOSS_HOME/bin/standalone.sh -c standalone-full.xml &
mvn verify -Dcontext=ci -pl kmelia/kmelia-library
mvn verify -Dcontext=ci -pl kmelia/kmelia-library -Dit.test=TopicSearchDaoIT
$JBOSS_HOME/bin/jboss-cli.sh --connect :shutdown
```

`JBOSS_HOME` is set by Failsafe to `${temp.directory}/wildfly-${wildfly.version}`, `temp.directory`
coming from an active profile of `~/.m2/settings.xml`. In the `silverpeas/silverbuild` and
`silverpeas/silverdev` images the server lives under `/opt/wildfly-for-tests/`.

Writing an integration test:

* Each module provides a `WarBuilder4<Name>` (in `src/integration-test/java/.../test/`) extending
  `BasicWarBuilder` and listing the Core artifacts to embed; the `@Deployment` method calls
  `WarBuilder4<Name>.onWarForTestClass(X.class).testFocusedOn(…).build()`.
* `src/integration-test/resources/META-INF/test-MANIFEST.MF` declares the WildFly modules the test
  WAR depends on (for instance `org.mnode.ical4j services`) — such dependencies are provided by the
  server and must not be embedded in the archive.
* Database fixtures come from `@Rule DbUnitLoadingRule("create-database.sql", "<name>-dataset.xml")`.
* Beans are obtained with `ServiceProvider.getService(…)` rather than injected.

### Continuous integration

`Jenkinsfile` builds the project in the `silverpeas/silverbuild` container, after waiting for any
running build of Silverpeas Core on the same version; it starts WildFly, runs
`mvn clean install -Pdeployment -Dcontext=ci` (so unit **and** integration tests), and, for pull
requests against the Silverpeas organisation, submits the result to SonarCloud and enforces the
quality gate.

---

## Technical map of the applications

A quick guide to which idioms each module uses — handy before touching an unfamiliar application.

| Module | Web layer | REST | JPA | Unit tests | Integration tests |
|---|---|---|---|---|---|
| almanach | web controller | ✔ | | | |
| blog | request router | | | | |
| classifieds | request router | | | | |
| community | web controller | ✔ | ✔ | ✔ | ✔ |
| dataWarning | request router | | | | |
| delegatednews | request router | ✔ | ✔ | | ✔ |
| formsOnline | request router | | | ✔ | ✔ |
| forums | request router | | | | ✔ |
| gallery | request router | ✔ | | ✔ | ✔ |
| hyperlink | request router | | | | |
| infoLetter | request router | | | | ✔ |
| jdbcConnector | web controller | | ✔ | | |
| kmelia | request router | ✔ | ✔ | ✔ | ✔ |
| mailinglist | request router | | ✔ | ✔ | ✔ |
| mydb | web controller | | ✔ | ✔ | |
| organizationchart | request router | | | ✔ | |
| processManager | request router | | | | |
| projectManager | request router | | | | |
| questionReply | request router | ✔ | | ✔ | |
| quickinfo | request router | ✔ | ✔ | | ✔ |
| quizz | request router | | | | |
| resourcesManager | request router | ✔ | ✔ | | ✔ |
| rssAggregator | request router | ✔ | | | |
| scheduleEvent | request router | | ✔ | | |
| silverCrawler | request router | | | | |
| spaceMembers | request router | | | | |
| suggestionBox | web controller | ✔ | ✔ | | |
| survey | request router | | | ✔ | |
| webPages | request router | | | | |
| webSites | request router | | | | |
| whitePages | request router | | ✔ | | ✔ |
| yellowpages | request router | | | | |

---

## Contributing

Contributions are welcome. Please have a look at the
[collaboration rules](https://www.silverpeas.org/dev/collaboration.html) and at the
[technical documentation](https://www.silverpeas.org/docs/components/index.html) before 
submitting a pull
request, and:

* Every source file carries the GNU AGPL v3 header with the Silverpeas FLOSS exception;
  `mvn generate-sources -Plicense` regenerates it.
* Line endings are LF for all text and source files, enforced by `.gitattributes`.
* Javadoc must satisfy the stricter Java 17 doclint.
* Commit messages reference the Silverpeas tracker, for example `Fix bug #15261 …` or
  `Feature #13140 …`.
* Pull requests against the Silverpeas repository are analysed by SonarCloud
  (project `Silverpeas_Silverpeas-Components`) and must pass its quality gate.
* When changing the database schema of an application, add the migration step for the **four**
  supported databases and bump the version in the migration descriptor.
* When adding a parameter, a role or a label to an application, start with its `WAComponent`
  descriptor and mirror the labels in the `fr`, `en` and `de` bundles.
* respect the coding conventions and the architectural conventions described above (in particular
  the use of the Silverpeas stereotypes instead of the raw CDI annotations),
* add the license header to any new source file,
* cover your change with unit tests and, when the change involves the container (persistence,
  transactions, CDI wiring, web resources), with integration tests,
* make sure the whole build passes before opening the pull request.

Bugs and improvement requests are tracked in the
[Silverpeas tracker](https://tracker.silverpeas.org/projects/silverpeas).
Pull requests are welcome; their title has to start with `Bug #<n>`, `Feature #<n>` or `Support 
#<n>`, referring to the tracked issue, as the continuous integration relies on it.

---

## Licence

Copyright © 2000 – 2026 Silverpeas.

This program is free software: you can redistribute it and/or modify it under the terms of the GNU
Affero General Public License as published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version — see [license.txt](license.txt) — with the special
FLOSS exception described in [exceptions.txt](exceptions.txt).
