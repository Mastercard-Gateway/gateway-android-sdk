node {
    stage 'Checkout'
    checkout scm

    stage 'Clean'
    sh "./gradlew clean --refresh-dependencies"

    stage 'Build'
    sh "./gradlew assembleRelease"

    stage 'Test'
    sh "./gradlew lint test"

    stage 'Deploy'
    sh "./gradlew simplify-android:generatePomFileForAarPublication simplify-android:artifactoryPublish"

    stage 'Archive'
    androidLint canComputeNew: false, canRunOnFailed: true, defaultEncoding: '', failedNewHigh: '0', healthy: '', pattern: 'app/build/**/lint-*.xml', unHealthy: '', unstableTotalAll: '200'
    step([$class: 'JUnitResultArchiver', testResults: 'simplify-android/build/test-results/**/TEST-*.xml'])
    step([$class: 'ArtifactArchiver', artifacts: 'simplify-android/build/outputs/**/*.aar', fingerprint: true])
    properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactNumToKeepStr: '10', numToKeepStr: '10']]])
}
