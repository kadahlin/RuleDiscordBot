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
    AWS_ACCOUNT_ID = credentials('honkbotProdAccountId')
    AWS_ACCESS_KEY_ID = credentials('honkbotProdAwsAccessKey')
    AWS_SECRET_ACCESS_KEY = credentials('honkbotProdAwsSecretKey')
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

    stage('Publish to ECR') {
      steps {
          sh 'aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.us-west-2.amazonaws.com '
          sh 'docker tag brinkhorizon/honkbot:latest $AWS_ACCOUNT_ID.dkr.ecr.us-west-2.amazonaws.com/honkbot-prod:latest'
          sh 'docker push $AWS_ACCOUNT_ID.dkr.ecr.us-west-2.amazonaws.com/honkbot-prod:latest'
          sh 'aws ecs update-service --cluster honkbot-cluser --service honkbot-service --force-new-deployment'
      }
    }
  }
}