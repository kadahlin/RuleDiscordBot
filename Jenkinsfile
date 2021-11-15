pipeline {
  agent any

  tools {
    jdk 'jdk1.8'
  }

  options {
    // Stop the build early in case of compile or test failures
    skipStagesAfterUnstable()
  }

  environment {
    HONKBOT_FIREBASE_JSON = credentials('honkbotFirebaseJson')
    HONKBOT_TOKEN = credentials('honkbotToken')
    DOCKER_TOKEN = credentials('dockerHubToken')
    DOCKER_USERNAME = credentials('dockerHubUsername')
  }

  stages {

    stage('Move firebase resources') {
      steps {
        // Compile the app and its dependencies
        sh 'mkdir -p ./myrulebot/src/main/resources'
        sh 'mv $HONKBOT_FIREBASE_JSON ./myrulebot/src/main/resources/serviceAccount.json'
      }
    }

    stage('Build and test') {
      steps {
        sh './gradlew build installDist --stacktrace'
      }
    }

    stage('Create docker image') {
      steps {
        sh 'docker build -t brinkhorizon/honkbot:latest --build-arg HONKBOT_TOKEN=${HONKBOT_TOKEN} .'
      }
    }

    stage('Publish to docker hub') {
      steps {
          sh '''
          docker login -u $DOCKER_USERNAME -p $DOCKER_TOKEN
          docker push brinkhorizon/honkbot:latest
          docker logout
          '''
      }
    }
  }
}