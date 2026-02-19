plugins {
    id("org.fcitx.fcitx5.android.lib-convention")
}

android {
    namespace = "com.k2fsa.sherpa.onnx"

    // Point to the real Kotlin API source files from the sherpa-onnx repo
    sourceSets {
        getByName("main") {
            kotlin.srcDir("${rootProject.projectDir}/sherpa-onnx-master/sherpa-onnx-master/sherpa-onnx/kotlin-api")
        }
    }
}
