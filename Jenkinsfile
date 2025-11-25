import java.util.regex.Matcher

pipeline {
  environment {
    lockFilePath = null
    version = null
    silverpeasCore = null
  }
  agent {
    docker {
      image 'silverpeas/silverbuild:6.4'
      args '''
        -v $HOME/.m2:/home/silverbuild/.m2 
        -v $HOME/.gitconfig:/home/silverbuild/.gitconfig 
        -v $HOME/.ssh:/home/silverbuild/.ssh 
        -v $HOME/.gnupg:/home/silverbuild/.gnupg
        '''
    }
  }
  stages {
    stage('Waiting for core running build if any') {
      steps {
        script {
          println "Current branch is ${env.BRANCH_NAME}"
          println "Actual stable branch is ${env.STABLE_BRANCH}"
          def pom = readMavenPom()
          silverpeasCore = getSilverpeasCoreProject(pom)
          version = computeSnapshotVersion(pom)
          lockFilePath = createLockFile(version, 'components')
          waitForDependencyRunningBuildIfAny(version, 'core')
        }
      }
    }
    stage('Build') {
      steps {
        script {
          sh "/opt/wildfly-for-tests/wildfly-*.Final/bin/standalone.sh -c standalone-full.xml &> /dev/null &"
          checkParentPOMVersion(version)
          boolean coreDependencyExists = existsDependency(version, 'core')
          def coreVersion
          if (coreDependencyExists) {
            coreVersion = version
          } else {
            coreVersion = getCoreDependencyVersion()
          }
          sh """
            sed -i -e "s/<core.version>[\\\${}0-9a-zA-Z.-]\\+/<core.version>${coreVersion}/g" pom.xml
            mvn -U versions:set -DgenerateBackupPoms=false -DnewVersion=${version}
            mvn clean install -Pdeployment -Djava.awt.headless=true -Dcontext=ci
            /opt/wildfly-for-tests/wildfly-*.Final/bin/jboss-cli.sh --connect :shutdown
            """
          deleteLockFile(lockFilePath)
        }
      }
    }
    stage('Quality Analysis') {
      // quality analyse with our SonarQube service is performed only for PR against our main
      // repository and for master branch
      when {
        expression {
          env.BRANCH_NAME.startsWith('PR') &&
              env.CHANGE_URL?.startsWith('https://github.com/Silverpeas')
        }
      }
      steps {
        script {
          String jdkHome = sh(script: 'echo ${SONAR_JDK_HOME}', returnStdout: true).trim()
          withSonarQubeEnv('Silverpeas SonarCloud') {
             sh """
                JAVA_HOME=$jdkHome mvn org.codehaus.mojo:sonar-maven-plugin::sonar \\
                  -Dsonar.projectKey=Silverpeas_Silverpeas-Components \\
                  -Dsonar.organization=silverpeas \\
                  -Dsonar.pullrequest.branch=${env.BRANCH_NAME} \\
                  -Dsonar.pullrequest.key=${env.CHANGE_ID} \\
                  -Dsonar.pullrequest.base=master \\
                  -Dsonar.pullrequest.provider=github \\
                  -Dsonar.scanner.force-deprecated-java-version=true
                """
          }
          timeout(time: 30, unit: 'MINUTES') {
            // Just in case something goes wrong, pipeline will be killed after a timeout
            def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
            if (qg.status != 'OK' && qg.status != 'WARNING') {
              error "Pipeline aborted due to quality gate failure: ${qg.status}"
            }
          }
        }
      }
    }
  }
  post {
    always {
      deleteLockFile(lockFilePath)
      step([$class                  : 'Mailer',
            notifyEveryUnstableBuild: true,
            recipients              : "miguel.moquillon@silverpeas.org, david.lesimple@silverpeas.org, silveryocha@chastagnier.com",
            sendToIndividuals       : true])
    }
  }
}

def computeSnapshotVersion(pom) {
  final String version = pom.version
  final String defaultVersion = env.BRANCH_NAME == 'master' ? version :
      env.BRANCH_NAME.toLowerCase().replaceAll('[# -]', '')
  Matcher m = env.CHANGE_TITLE =~ /^(Bug #?\d+|Feature #?\d+).*$/
  String snapshot = m.matches()
      ? m.group(1).toLowerCase().replaceAll(' #?', '')
      : ''
  if (snapshot.isEmpty()) {
    m = env.CHANGE_TITLE =~ /^\[([^\[\]]+)].*$/
    snapshot = m.matches()
        ? m.group(1).toLowerCase().replaceAll('[/><|:&?!;,*%$=}{#~\'"\\\\°)(\\[\\]]', '').trim().replaceAll('[ @]', '-')
        : ''
  }
  return snapshot.isEmpty() ? defaultVersion : "${pom.properties['next.release']}-${snapshot}"
}

def getCoreDependencyVersion() {
  copyArtifacts projectName: "Silverpeas_${silverpeasCore}_AutoDeploy", flatten: true
  def lastBuild = readYaml file: 'build.yaml'
  sh "rm build.yaml"
  return lastBuild.version
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

def checkParentPOMVersion(version) {
  def pom = readMavenPom()
  int idx = pom.parent.version.indexOf('-SNAPSHOT')
  if (idx > 0) {
    String[] snapshot = version.split('-')
    String parentVersion = pom.parent.version.substring(0, idx) + '-' + snapshot[snapshot.length - 1]
    echo "Update parent POM to ${parentVersion}"
    sh """
      mvn versions:update-parent -DgenerateBackupPoms=false -DparentVersion="[${parentVersion}]"
      """
  }
}

def getSilverpeasCoreProject(pom) {
  String silverpeasCoreProject
  switch (env.BRANCH_NAME) {
    case 'master':
      silverpeasCoreProject = 'Master'
      break
    case env.STABLE_BRANCH:
      silverpeasCoreProject = 'Stable'
      break
    default:
      Matcher branchMatcher = env.BRANCH_NAME =~ /\d+.\d+.x/
      if (branchMatcher.matches()) {
        // an old stable project
        silverpeasCoreProject = env.BRANCH_NAME
      } else {
        // this is a PR
        String version = pom.version
        Matcher versionMatcher = version =~ /\d+.\d+.\d+-SNAPSHOT/
        if (versionMatcher.matches()) {
          if (version.startsWith(env.STABLE_BRANCH.replace('.x', ''))) {
            silverpeasCoreProject = 'Stable'
          } else {
            silverpeasCoreProject = version.replaceFirst('.\\d+-SNAPSHOT', '.x')
          }
        } else {
          silverpeasCoreProject = 'Master'
        }
      }
      break
  }
  return silverpeasCoreProject
}