pipeline {
  agent any

  options {
    timestamps()
    ansiColor('xterm')
    disableConcurrentBuilds()
  }

  parameters {
    string(name: 'JAVA_LIBRARY_DIR', defaultValue: '/usr/share/java',
      description: 'Passed to Gradle as -Djava.library.dir (needed by tests/check)')
    string(name: 'MAVEN_POMS_DIR', defaultValue: '/usr/share/maven-poms',
      description: 'Passed to Gradle as -Dmaven.poms.dir (needed by tests/check)')
  }

  environment {
    GRADLE_OPTS = "-Dorg.gradle.daemon=false -Dorg.gradle.console=plain"
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Prepare Gradle') {
      steps {
        script {
          env.GRADLE_CMD = fileExists('gradlew') ? './gradlew' : 'gradle'
          if (env.GRADLE_CMD == './gradlew') sh 'chmod +x gradlew'
          sh "${env.GRADLE_CMD} --version"
        }
      }
    }

    stage('Publication') {
      steps {
        sh "${env.GRADLE_CMD} publishToMavenLocal -Ddisable.ansi.color=true"
      }
    }

    stage('Check') {
      steps {
        sh """
          set -eux
          test -d "${params.JAVA_LIBRARY_DIR}"
          test -d "${params.MAVEN_POMS_DIR}"

          ${env.GRADLE_CMD} check \
            -Djava.library.dir="${params.JAVA_LIBRARY_DIR}" \
            -Dmaven.poms.dir="${params.MAVEN_POMS_DIR}" \
        """
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
        }
      }
    }
  }
}
