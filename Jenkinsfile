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

/*
    stage('Publish to ECR') {
      steps {
        dir('stack') {
          sh 'cdk synth'
          sh 'cdk deploy styles-dev-stack'
        }
      }
    }
*/
  }
}