import java.util.regex.Matcher

node {
  def lockFilePath
  try {
    def version
    docker.image('silverpeas/silverbuild')
        .inside('-u root -v $HOME/.m2:/root/.m2 -v $HOME/.gitconfig:/root/.gitconfig -v $HOME/.ssh:/root/.ssh -v $HOME/.gnupg:/root/.gnupg') {
      stage('Preparation') {
        sh "rm -rf *"
        checkout scm
      }
      stage('Build') {
        version = computeSnapshotVersion()
        lockFilePath = createLockFile(version, 'components')
        waitForDependencyRunningBuildIfAny(version, 'core')
        def pom = readMavenPom()
        def current = pom.version
        boolean coreDependencyExists = existsDependency(version, 'core')
        if (!coreDependencyExists) {
          sh """
sed -i -e "s/<core.version>[\\\${}0-9a-zA-Z.-]\\+/<core.version>${current}/g" pom.xml
"""
        }
        sh """
mvn -U versions:set -DgenerateBackupPoms=false -DnewVersion=${version}
mvn clean install -Pdeployment -Djava.awt.headless=true -Dcontext=ci
"""
        deleteLockFile(lockFilePath)
      }
      stage('Quality Analysis') {
        // quality analyse with our SonarQube service is performed only for PR against our main
        // repository
        if (env.BRANCH_NAME.startsWith('PR') &&
            env.CHANGE_URL?.startsWith('https://github.com/Silverpeas')) {
          withSonarQubeEnv {
            sh """
mvn ${SONAR_MAVEN_GOAL} -Dsonar.projectKey=Silverpeas_Silverpeas-Components \\
    -Dsonar.organization=silverpeas \\
    -Dsonar.pullrequest.branch=${env.BRANCH_NAME} \\
    -Dsonar.pullrequest.key=${env.CHANGE_ID} \\
    -Dsonar.pullrequest.base=master \\
    -Dsonar.pullrequest.provider=github \\
    -Dsonar.host.url=${SONAR_HOST_URL} \\
    -Dsonar.login=${SONAR_AUTH_TOKEN}
"""
          }
        } else {
          echo "It isn't a PR validation for the Silverpeas organization. Nothing to analyse."
        }
      }
    }
  } catch (err) {
    echo "Caught: ${err}"
    currentBuild.result = 'FAILURE'
  } finally {
    deleteLockFile(lockFilePath)
  }
  step([$class                  : 'Mailer',
        notifyEveryUnstableBuild: true,
        recipients              : "miguel.moquillon@silverpeas.org, yohann.chastagnier@silverpeas.org, nicolas.eysseric@silverpeas.org",
        sendToIndividuals       : true])
}

def computeSnapshotVersion() {
  def pom = readMavenPom()
  final String version = pom.version
  final String defaultVersion = env.BRANCH_NAME == 'master' ? version :
      env.BRANCH_NAME.toLowerCase().replaceAll('[# -]', '')
  Matcher m = env.CHANGE_TITLE =~ /^(Bug #\d+|Feature #\d+).*$/
  final String snapshot =
      m.matches() ? m.group(1).toLowerCase().replaceAll(' #', '') : ''
  return snapshot.isEmpty() ? defaultVersion : "${pom.properties['next.release']}-${snapshot}"
}

def existsDependency(version, projectName) {
  def exitCode = sh script: "test -d \$HOME/.m2/repository/org/silverpeas/${projectName}/${version}", returnStatus: true
  return exitCode == 0
}

static def createLockFilePath(version, projectName) {
  final String lockFilePath = "\$HOME/.m2/${version}_${projectName}_build.lock"
  return lockFilePath
}

def createLockFile(version, projectName) {
  final String lockFilePath = createLockFilePath(version, projectName)
  sh "touch ${lockFilePath}"
  return lockFilePath
}

def deleteLockFile(lockFilePath) {
  if (isLockFileExisting(lockFilePath)) {
    sh "rm -f ${lockFilePath}"
  }
}

def isLockFileExisting(lockFilePath) {
  if (lockFilePath?.trim()?.length() > 0) {
    def exitCode = sh script: "test -e ${lockFilePath}", returnStatus: true
    return exitCode == 0
  }
  return false
}

def waitForDependencyRunningBuildIfAny(version, projectName) {
  final String dependencyLockFilePath = createLockFilePath(version, projectName)
  timeout(time: 3, unit: 'HOURS') {
    waitUntil {
      return !isLockFileExisting(dependencyLockFilePath)
    }
  }
  if (isLockFileExisting(dependencyLockFilePath)) {
    error "After timeout dependency lock file ${dependencyLockFilePath} is yet existing!!!!"
  }
}