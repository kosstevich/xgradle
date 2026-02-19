pipeline {
  agent none

  options {
    timestamps()
    ansiColor('xterm')
    disableConcurrentBuilds()
    skipDefaultCheckout(true)
  }

  stages {
    stage('Checkout') {
      agent any
      steps {
        checkout scm
        stash name: 'src', includes: '**/*', useDefaultExcludes: false
      }
    }

    stage('Build (JDK 11)') {
      agent {
        dockerContainer {
          image 'xenoalt/alt-sisyphus-java:1.0.0-j21'
        }
      }
      steps {
        sh 'apt-get update'
        deleteDir()
        unstash 'src'
        sh './gradlew publishToMavenLocal -Djava11'
      }
    }

    stage('Build (JDK 17+)') {
      agent {
        dockerContainer {
          image 'xenoalt/alt-sisyphus-java:1.0.0-j21'
        }
      }
      steps {
        sh 'apt-get update'
        deleteDir()
        unstash 'src'
        sh './gradlew publishToMavenLocal'
      }
    }

    stage('Tests (JDK 11)') {
      agent {
        dockerContainer {
          image 'xenoalt/alt-sisyphus-java:1.0.0-j21'
        }
      }
      steps {
        sh '''apt-get update && \
            apt-get install -y google-gson \
            maven-lib \
            shadow-gradle-plugin
        '''
        deleteDir()
        unstash 'src'
        sh '''
          ./gradlew build \
            -Djava.library.dir="/usr/share/java" \
            -Dmaven.poms.dir="/usr/share/maven-poms" \
            -Djava11 \
            --info
        '''
      }
    }

    stage('Tests (JDK 17+)') {
      agent {
        dockerContainer {
          image 'xenoalt/alt-sisyphus-java:1.0.0-j21'
        }
      }
      steps {
              sh '''apt-get update && \
                  apt-get install -y google-gson \
                  maven-lib \
                  shadow-gradle-plugin
              '''
        deleteDir()
        unstash 'src'
        sh '''
          ./gradlew build \
            -Djava.library.dir="/usr/share/java" \
            -Dmaven.poms.dir="/usr/share/maven-poms" \
            --info
        '''
      }
    }
  }
}
