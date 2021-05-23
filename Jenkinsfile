pipeline {
    agent {
        label('docker')
    }

    environment {
        CONTAINER = 'harbor.seamlessme.local/seamlessme/android:build'
        QUOTE = '\\"'
        C4L_SIGNING_KEY_ALIAS = 'upload'
    }

    parameters {
        booleanParam(name: 'ReleaseToInternalTesting', defaultValue: false,
            description: 'Release the bundle to internal testing track on Google Play Store. This only works on master or release/* branches.')
    }

    stages {
        stage('Docker pull') {
            steps {
                cleanWs()
                checkout scm
                sh("docker pull ${CONTAINER}")
            }
        }
        stage('Unit Tests') {
            steps {
                sh '''
                    cd Luca
                    docker run --rm -u $(id -u):$(id -g) -v `pwd`:/Luca ${CONTAINER} bash -c "cd /Luca/app; ./../gradlew :app:test"
                '''
            }
        }
        stage('Build Debug') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: "luca-staging-api-basic-auth-user-pw", usernameVariable: 'STAGING_API_USERNAME', passwordVariable: 'STAGING_API_PASSWORD'),
                ]) {
                    sh '''
                        cd Luca
                        STAGING_PARAMS="-PSTAGING_API_USERNAME=$QUOTE$STAGING_API_USERNAME$QUOTE -PSTAGING_API_PASSWORD=$QUOTE$STAGING_API_PASSWORD$QUOTE"
                        CMD="cd /Luca/app; ./../gradlew :app:assembleDebug $STAGING_PARAMS && mv build/outputs/apk/debug/app-debug.apk build/outputs/apk/debug/app-debug_${BUILD_NUMBER}.apk"
                        docker run --rm -u $(id -u):$(id -g) -v `pwd`:/Luca ${CONTAINER} bash -c "$CMD"
                    '''
                }
            }
        }
        stage('Build Release') {
            when { anyOf { branch 'release/*'; branch 'master' } }
            steps {
                withCredentials([
                    file(credentialsId: 'luca-android-keystore', variable: 'KEYSTORE_FILE'),
                    string(credentialsId: 'luca-android-upload-key-pw', variable: 'C4L_SIGNING_KEY_PASSWORD'),
                    string(credentialsId: 'luca-android-keystore-pw', variable: 'C4L_SIGNING_STORE_PASSWORD'),
                ]) {
                    sh '''
                        cd Luca
                        LOCAL_KEYSTORE_FILE=$(basename $KEYSTORE_FILE)
                        KEYSTORE_PARAMS="-PC4L_SIGNING_STORE_FILE=$LOCAL_KEYSTORE_FILE -PC4L_SIGNING_KEY_PASSWORD=$C4L_SIGNING_KEY_PASSWORD -PC4L_SIGNING_KEY_ALIAS=$C4L_SIGNING_KEY_ALIAS -PC4L_SIGNING_STORE_PASSWORD=$C4L_SIGNING_STORE_PASSWORD"
                        CMD="cd /Luca/app; ./../gradlew :app:assembleRelease $KEYSTORE_PARAMS"
                        CMD="$CMD && mv build/outputs/apk/release/app-release.apk build/outputs/apk/release/app-release_${BUILD_NUMBER}.apk"
                        docker run --rm -u $(id -u):$(id -g) -v `pwd`:/Luca -v $KEYSTORE_FILE:/Luca/app/$LOCAL_KEYSTORE_FILE ${CONTAINER} bash -c "$CMD"
                    '''
                }
            }
        }
        stage('Upload to PlayStore') {
            when {
                allOf {
                    anyOf { branch 'release/*'; branch 'master' }
                    expression { return params.ReleaseToInternalTesting }
                }
            }
            steps {
                withCredentials([
                    file(credentialsId: 'luca-android-keystore', variable: 'KEYSTORE_FILE'),
                    file(credentialsId: 'c4l-play-console-service-account-json', variable: 'SERVICE_ACCOUNT_FILE'),
                    string(credentialsId: 'luca-android-upload-key-pw', variable: 'C4L_SIGNING_KEY_PASSWORD'),
                    string(credentialsId: 'luca-android-keystore-pw', variable: 'C4L_SIGNING_STORE_PASSWORD'),
                ]) {
                    sh '''
                        cd Luca
                        LOCAL_KEYSTORE_FILE=$(basename $KEYSTORE_FILE)
                        KEYSTORE_PARAMS="-PC4L_SIGNING_STORE_FILE=$LOCAL_KEYSTORE_FILE -PC4L_SIGNING_KEY_PASSWORD=$C4L_SIGNING_KEY_PASSWORD -PC4L_SIGNING_KEY_ALIAS=$C4L_SIGNING_KEY_ALIAS -PC4L_SIGNING_STORE_PASSWORD=$C4L_SIGNING_STORE_PASSWORD"
                        CMD="cd /Luca/app; ./../gradlew publishReleaseBundle $KEYSTORE_PARAMS"
                        docker run --rm -u $(id -u):$(id -g) -v `pwd`:/Luca -v $SERVICE_ACCOUNT_FILE:/Luca/app/service-account.json -v $KEYSTORE_FILE:/Luca/app/$LOCAL_KEYSTORE_FILE ${CONTAINER} bash -c "$CMD"
                    '''
                }
            }
        }
        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'Luca/app/build/outputs/apk/**/*.apk, Luca/app/build/outputs/bundle/**/*.aab', excludes: 'Luca/app/build/outputs/apk/**/*-androidTest.apk', fingerprint: true
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
