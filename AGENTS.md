# AGENTS.md

This file provides guidance to AI coding agents (Claude Code, Codex, Cursor, Gemini CLI, Copilot…)
when working with code in this repository. Claude Code loads it through `CLAUDE.md`, which only
imports this file.

## What this repository is

The 32 applications ("components") shipped in standard with Silverpeas. It is **not** a standalone
application: every module compiles against Silverpeas Core (`provided` scope) and is deployed as a
WAR into a Silverpeas instance running on WildFly. CDI, Maven multi-module.

`${core.version}` in the root `pom.xml` pins the Silverpeas Core version; the CI rewrites it to
match the Core build being tested. When working against a locally built Core, that property must
match the Core version installed in `~/.m2`.

## Common rules

These rules are shared, word for word, by the `AGENTS.md` of Silverpeas-Core, Silverpeas-Components
and Silverpeas-Looks. Change them in the three repositories at once.

### Toolchain & build environment

- The build inherits almost everything (Java release, dependency versions, surefire/failsafe wiring,
  integration-test source dirs, profiles) from the external parent POM
  `org.silverpeas:silverpeas-project`, not from this repository. Read it (in `~/.m2`) when a build
  behaviour is not explained by the POMs in this repository.
- Java 21 (`maven.compiler.release` of the parent POM) and Maven 3.9.x. The platform is Jakarta EE 10
  (`jakarta.*` namespaces everywhere) deployed on WildFly.
- Build and test in the devcontainer (`.devcontainer/`, built on the `silverpeas/silverdev:latest`
  image) whenever possible. Otherwise, if a container of that image is available on the host, start
  it if needed and run the Maven commands inside it. The image provides Java, Maven, a WildFly under
  `/opt/wildfly-for-tests/` with a `wildfly start|stop|status` helper, and the native tools some
  tests need (ffmpeg, imagemagick, ghostscript, libreoffice, swftools, pdf2json). Don't expect the
  tests to run in a bare checkout.
- Profiles and switches from the parent POM: `-DskipTests`, `-PskipMinify` (skips the JS/CSS
  minification, much faster when iterating on web assets), `-Pcoverage` (JaCoCo), `-Pdeployment`
  (attaches sources and javadoc jars), `-Plicense` (rewrites the license header of every source file).

### Tests come with any code change

Any code that is modified or added has to be covered by unit or integration tests, written
preferably **before** the code itself: either to guard the modified code against regressions, or to
validate the new code and to help to design it (its call must be simple; any new code follows the
clean code principles). This is true even for a module or a repository without any test yet: set up
its test resources instead of skipping the tests. Code that is hard to test is a design signal: fix
the design rather than giving up the test.

**Unit tests** (surefire, `src/test/`, `**/*Test.java`): JUnit 5 with
`@EnableSilverTestEnv(context = JEETestContext.class)`.
- A bean under test declared with `@TestedBean` gets its `@Inject` dependencies resolved from the
  test bean container, the missing ones being automatically mocked; declare with `@TestManagedMock`
  only the collaborators to stub.
- A module without tests yet needs `silverpeas-core-test` as a test dependency and the
  `src/test/resources/META-INF/services/org.silverpeas.kernel.BeanContainer` file (plus
  `org.silverpeas.kernel.util.SystemWrapper` when system properties are read, and
  `org/silverpeas/util/stringtemplate.properties` when templates are involved); otherwise the CDI
  bean container is loaded instead of the test one.
- To check the user notifications asked by a service without rendering any template, send them
  through `UserNotificationHelper.buildAndSend(...)`, capture the builders with
  `mockStatic(UserNotificationHelper.class)` and put the test in the package of the builders so
  that their protected properties are reachable.
- The parent POM forces the `fr`/`FR` locale and the `Europe/Paris` timezone: date and number
  assertions are locale-sensitive.

**Integration tests** (failsafe, `src/integration-test/`, `**/*IT.java`): JUnit **4** with Arquillian.
- They run only with the `integration-test` profile, activated by `-Dcontext=ci`, against an
  **already running** WildFly started with `standalone-full.xml` (Arquillian uses the
  `wildfly-remote` container). The full CI command is
  `mvn clean install -Pdeployment -Djava.awt.headless=true -Dcontext=ci`.
- Each test deploys a purpose-built WAR assembled by a `WarBuilder*` class that declares exactly
  which classes and resources go into the archive. Any type in the signature of a managed bean
  (fields, parameters, returned and thrown types) has to be embedded: otherwise Weld silently
  ignores the bean instead of failing the deployment.
- Inside an integration test, beans are looked up with `ServiceProvider.getService(...)`, not
  injected.

### Dependency injection

Silverpeas deliberately wraps the CDI/Jakarta-EE container behind its own annotations so the IoC
implementation could be swapped without touching business code. **Prefer these over raw CDI
annotations** when writing beans (they are defined in `org.silverpeas.core.annotation`):

- `@Service` — a transactional, `@ApplicationScoped` business service (a CDI stereotype).
- `@Repository` — a persistence/data-access bean.
- `@Provider`, `@Bean`, `@WebService` — other managed-bean stereotypes.

Managed beans get their collaborators via injection points. **Unmanaged objects** (e.g. entities
loaded from a datasource, JSP-side code) cannot inject, so they obtain services through
`org.silverpeas.core.util.ServiceProvider` (`ServiceProvider.getService(Type.class)` /
`getService("name")`), a thin delegator over the kernel's `ManagedBeanProvider`. For generic
(parameterized) service types, `ServiceProvider` won't resolve them — use
`jakarta.enterprise.inject.Instance` in a managed bean instead.

**In a managed bean, never get another managed bean through `ServiceProvider`**, neither directly
nor through a static accessor delegating to it (`PdcManager.get()`, `OrganizationController.get()`,
…): `ServiceProvider` is first intended for the objects that aren't managed by CDI, and a
programmatic lookup costs more than an injection. When a dependency has to be resolved lazily
(it is used only in some cases, or it isn't always deployed), inject it with
`jakarta.enterprise.inject.Instance<T>` and call `get()` where it is needed, as
`ICalendarEventSynchronization` does with its `Scheduler` in Silverpeas Core.

Beans needing startup logic implement `org.silverpeas.core.initialization.Initialization`.

### Code conventions

- Follow the clean code principles. A constructor that would take more than four parameters is
  replaced by a builder.
- Every source file carries the AGPL v3 + Silverpeas FLOSS-exception header (`license.txt` and
  `exceptions.txt` at the repository root); copy it into new files with the current year as upper
  bound, or run `mvn generate-sources -Plicense`.
- Logging goes through `SilverLogger.getLogger(this)`; each module declares its own logger
  namespace in `properties/org/silverpeas/util/logging/<name>Logging.properties`.
- Javadoc must satisfy the Java 21 doclint.
- LF line endings for all text and source files (enforced by `.gitattributes`).

### Git, CI & versioning

- Commit messages reference the Redmine tracker: `Feature #<n> ...`, `Fix bug #<n> ...`,
  `Fix vulnerability #<n> ...`. PR titles must start with `Bug #<n>`, `Feature #<n>`, `Support #<n>`
  or `[<label>]`: the CI derives the snapshot version from it.
- CI is Jenkins (`Jenkinsfile`) in the `silverpeas/silverbuild` image. It rewrites the project
  version (`versions:set`) and the parent-POM version per branch/PR before building, then runs a
  SonarCloud quality gate on PRs. Don't hand-edit versions to match the CI behaviour.

## Build & test commands

```bash
mvn clean install                       # build everything (unit tests only)
mvn clean install -PskipMinify          # skip JS/CSS minification
mvn install -pl blog/blog-library -am   # build one module and its prerequisites
cd kmelia && mvn install                # build one whole component
mvn test -pl kmelia/kmelia-library -Dtest=KmeliaValidationTest   # one unit-test class
```

Integration tests of one module, against a running WildFly:

```bash
$JBOSS_HOME/bin/standalone.sh -c standalone-full.xml &
mvn verify -Dcontext=ci -pl kmelia/kmelia-library
mvn verify -Dcontext=ci -pl kmelia/kmelia-library -Dit.test=TopicSearchDaoIT
$JBOSS_HOME/bin/jboss-cli.sh --connect :shutdown
```

`JBOSS_HOME` is set by failsafe to `${temp.directory}/wildfly-${wildfly.version}`; `temp.directory`
comes from an active profile in `~/.m2/settings.xml`. In the `silverpeas/silverbuild` CI image and
the devcontainer the server lives under `/opt/wildfly-for-tests/`.

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

## Writing tests

Unit-test examples to start from: `KmeliaSubscribersNotificationTest` (`@TestedBean` and
`@TestManagedMock`) and `QuickInfoSubscribersNotificationTest` (capture of the user notifications
through `mockStatic(UserNotificationHelper.class)`). A component without tests yet gets its
`src/test/resources/META-INF/services/` files copied from one of these components.

Integration tests:
- Each module provides a `WarBuilder4<Name>` (in `src/integration-test/java/.../test/`) extending
  `BasicWarBuilder`, listing the Core maven artifacts to embed. The test's `@Deployment` method calls
  `WarBuilder4<Name>.onWarForTestClass(X.class).testFocusedOn(...).build()`.
- `src/integration-test/resources/META-INF/test-MANIFEST.MF` declares the WildFly modules the test
  WAR depends on (e.g. `org.mnode.ical4j services`) — dependencies provided as server modules must
  be added there, not embedded in the archive.
- Database fixtures: `@Rule DbUnitLoadingRule("create-database.sql", "<name>-dataset.xml")`, with
  the files in the test class' resource package.
