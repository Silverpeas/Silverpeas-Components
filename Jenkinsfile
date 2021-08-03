import java.util.regex.Matcher

pipeline {
  environment {
    lockFilePath = null
    version = null
    silverpeasCore = '6.1.x'
  }
  agent {
    docker {
      image 'silverpeas/silverbuild:6.1'
      args '-v $HOME/.m2:/home/silverbuild/.m2 -v $HOME/.gitconfig:/home/silverbuild/.gitconfig -v $HOME/.ssh:/home/silverbuild/.ssh -v $HOME/.gnupg:/home/silverbuild/.gnupg'
    }
  }
  stages {
    stage('Waiting for core running build if any') {
      steps {
        script {
          master = env.BRANCH_NAME == 'master'
          version = computeSnapshotVersion()
          lockFilePath = createLockFile(version, 'components')
          waitForDependencyRunningBuildIfAny(version, 'core')
        }
      }
    }
    stage('Build') {
      steps {
        script {
          def pom = readMavenPom()
          boolean coreDependencyExists = existsDependency(version, 'core')
          if (!coreDependencyExists) {
            def coreVersion = getCoreDependencyVersion()
            sh """
sed -i -e "s/<core.version>[\\\${}0-9a-zA-Z.-]\\+/<core.version>${coreVersion}/g" pom.xml
"""
          }
          sh """
mvn -U versions:set -DgenerateBackupPoms=false -DnewVersion=${version}
mvn clean install -Pdeployment -Djava.awt.headless=true -Dcontext=ci
"""
          deleteLockFile(lockFilePath)
        }
      }
    }
    stage('Quality Analysis') {
      steps {
        script {
          echo "No quality analyse for 6.1.x because it is built with Java 8 and SonarQube requires Java 11"
        }
      }
    }
  }
  post {
    always {
      deleteLockFile(lockFilePath)
      step([$class                  : 'Mailer',
            notifyEveryUnstableBuild: true,
            recipients              : "miguel.moquillon@silverpeas.org, yohann.chastagnier@silverpeas.org, nicolas.eysseric@silverpeas.org",
            sendToIndividuals       : true])
    }
  }
}

def computeSnapshotVersion() {
  def pom = readMavenPom()
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
