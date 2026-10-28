plugins {
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10" apply false
}

// Permite compilar en una ruta sin tildes cuando el entorno de Windows lo requiere.
providers.gradleProperty("buildOutputDir").orNull?.let { output ->
    layout.buildDirectory.set(file("$output/root"))
    subprojects { layout.buildDirectory.set(file("$output/$name")) }
}
