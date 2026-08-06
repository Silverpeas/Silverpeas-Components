# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

The 32 applications ("components") shipped in standard with Silverpeas. It is **not** a standalone
application: every module compiles against Silverpeas Core (`provided` scope) and is deployed as a
WAR into a Silverpeas instance running on Wildfly. Java 17, Jakarta EE 10, CDI, Maven multi-module.

Parent POM `org.silverpeas:silverpeas-project` (external, in `~/.m2`) holds most of the build
configuration — plugin versions, test profiles, dependency BOMs. Read it when a build behaviour is
not explained by the POMs in this repo.

`${core.version}` in the root `pom.xml` pins the Silverpeas Core version; the CI rewrites it to
match the Core build being tested. When working against a locally built Core, that property must
match the Core version installed in `~/.m2`.

## Build & test commands

To build and test the whole project or a module, use the devcontainer whenever possible. Otherwise, 
if a container from the `silverpeas/silverdev:latest` Docker image is available on the host, starts 
it (if not already done) and uses it.

```bash
mvn clean install                       # build everything (unit tests only)
mvn clean install -PskipMinify          # skip JS/CSS minification (faster iteration on webapp assets)
mvn install -pl blog/blog-library -am   # build one module and its prerequisites
cd kmelia && mvn install                # build one whole component
```

Unit tests (surefire, `**/*Test.java`, `**/*TestSuite.java`) — forced to `fr`/`FR`/`Europe/Paris`
locale and timezone by the parent POM, so date/number assertions are locale-sensitive:

```bash
mvn test -pl kmelia/kmelia-library
mvn test -pl kmelia/kmelia-library -Dtest=KmeliaValidationTest
```

Integration tests (failsafe, `**/*IT.java`, sources in `src/integration-test/`) only run when the
`integration-test` profile is activated by `-Dcontext=ci`, and require a **already running** Wildfly
with `standalone-full.xml` — Arquillian uses the `wildfly-remote` container:

```bash
$JBOSS_HOME/bin/standalone.sh -c standalone-full.xml &
mvn verify -Dcontext=ci -pl kmelia/kmelia-library
mvn verify -Dcontext=ci -pl kmelia/kmelia-library -Dit.test=TopicSearchDaoIT
$JBOSS_HOME/bin/jboss-cli.sh --connect :shutdown
```

`JBOSS_HOME` is set by failsafe to `${temp.directory}/wildfly-${wildfly.version}`; `temp.directory`
comes from an active profile in `~/.m2/settings.xml`. In the `silverpeas/silverbuild` CI image and
the `.devcontainer` (`silverpeas/silverdev`) the server lives under `/opt/wildfly-for-tests/`.

Other profiles: `-Pcoverage` (JaCoCo), `-Pdeployment` (sources + javadoc jars),
`-Plicense` (rewrites the AGPL header in every source file — every file must carry it).

## Component anatomy

Every component follows the same three-module layout (`<name>` is the component name, e.g. `blog`):

**`<name>-configuration`** — jar packaging `src/main/config` as resources. No Java. Contains what
Silverpeas Core reads at runtime:
- `xmlcomponents/<name>.xml` — the `WAComponent` descriptor (labels/descriptions per language,
  user profiles and their space role mapping, instance parameters). This is what makes the app
  instantiable in a space; adding a parameter or role starts here.
- `properties/org/silverpeas/<name>/multilang/<name>Bundle[_xx].properties` — i18n (fr, en, de).
- `properties/org/silverpeas/<name>/settings/<name>Settings.properties` and `<name>Icons.properties`.
- `properties/org/silverpeas/util/logging/<name>Logging.properties` — logger namespace.
- `migrations/modules/<name>-migration.xml` declares the current schema version; the SQL lives in
  `migrations/db/<h2|postgresql|mssql|oracle>/<name>/<version>/*.sql`. **A schema change must be
  added for all four databases** — a past commit had to fix a migration step forgotten on three of them.
- `resources/StringTemplates/components/<name>/` — StringTemplate (`.st`) bodies for user notifications.

**`<name>-library`** (artifact `silverpeas-<name>`) — the business layer, CDI-managed:
- `service/` — `@Service`-annotated (`org.silverpeas.core.annotation.Service`) beans, usually a
  `<Name>Service` interface + `Default<Name>Service`, `@Transactional` where needed.
- `model/` — domain objects; newer components use JPA entities, older ones plain value objects.
- `dao/` (legacy JDBC DAOs against `DBUtil` connections) **or** `repository/` (`@Repository`
  interface + `*JpaRepository` implementation). Both patterns are live; follow the one already
  used by the component you are editing.
- `notification/` — user-notification builders wired to the StringTemplates above.
- `<Name>InstancePostConstruction` / `<Name>InstancePreDestruction` — hooks invoked by Core when a
  component instance is created or deleted (create/drop per-instance data here).
- `src/main/resources/META-INF/beans.xml` is required for CDI discovery.

**`<name>-war`** (artifact `silverpeas-<name>-war`) — the web layer. It depends on the library with
`compile` scope (bundled in the WAR); everything from Core is `provided`. Two MVC generations coexist:
- **Legacy (26 components)**: a `ComponentRequestRouter<XxxSessionController>` servlet declared in
  `WEB-INF/web.xml` under `/R<name>/*`, dispatching on a "function" string and forwarding to JSPs in
  `src/main/webapp/<name>/jsp/`. Business state is held by the session-scoped `<Name>SessionController`.
- **Modern (almanach, community, jdbcConnector, mydb, suggestionBox)**:
  `WebComponentController<XxxWebRequestContext>` annotated `@WebComponentController(<name>)`, with
  JAX-RS-style routing (`@Path`, `@GET`, `@POST`) plus Silverpeas annotations `@Homepage`,
  `@LowestRoleAccess`, `@NavigationStep`, `@RedirectTo*`. Prefer this for new web navigation.
- `web/` — REST services (`@WebService` + `@Path`) exposing `*Entity` DTOs, consumed by the
  JS/Vue.js front-end in `webapp/<name>/jsp/javaScript/{services,vuejs/components}`.
- `webapp/util/icons/component/<name>{Small,Big}.{gif,png}` — icons the Silverpeas UI looks up by name.
- `access/` — `AccessController` extensions when the component has its own authorization rules.

## Writing integration tests

They run inside Wildfly through Arquillian (JUnit **4**, unlike the JUnit 5 unit tests):
- Each module provides a `WarBuilder4<Name>` (in `src/integration-test/java/.../test/`) extending
  `BasicWarBuilder`, listing the Core maven artifacts to embed. The test's `@Deployment` method calls
  `WarBuilder4<Name>.onWarForTestClass(X.class).testFocusedOn(...).build()`.
- `src/integration-test/resources/META-INF/test-MANIFEST.MF` declares the Wildfly modules the test
  WAR depends on (e.g. `org.mnode.ical4j services`) — dependencies provided as server modules must
  be added there, not embedded in the archive.
- Database fixtures: `@Rule DbUnitLoadingRule("create-database.sql", "<name>-dataset.xml")`, with
  the files in the test class' resource package.
- Beans are looked up with `ServiceProvider.getService(...)`, not injected.

## Conventions

- All source files carry the AGPL + Silverpeas FLOSS-exception header (see `license.txt`,
  `exceptions.txt`); `mvn generate-sources -Plicense` regenerates it.
- LF line endings are enforced by `.gitattributes` for all text/source types.
- Commit messages reference the Redmine tracker: `Fix bug #15261 ...`, `Feature #13140 ...`.
- Javadoc must satisfy the stricter Java 17 doclint.
