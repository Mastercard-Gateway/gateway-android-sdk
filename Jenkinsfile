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
    sh "./gradlew gateway-android:androidSourcesJar gateway-android:androidJavadocsJar gateway-android:generatePomFileForAarPublication gateway-android:artifactoryPublish"

    stage 'Archive'
    androidLint canComputeNew: false, canRunOnFailed: true, defaultEncoding: '', failedNewHigh: '0', healthy: '', pattern: 'gateway-android/build/**/lint-*.xml', unHealthy: '', unstableTotalAll: '200'
    step([$class: 'JUnitResultArchiver', testResults: 'gateway-android/build/test-results/**/TEST-*.xml'])
    step([$class: 'ArtifactArchiver', artifacts: 'gateway-android/build/outputs/**/*.aar', fingerprint: true])
    properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactNumToKeepStr: '10', numToKeepStr: '10']]])
}
