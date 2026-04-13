dependencyResolutionManagement {
    addVersionCatalog(this, "neoforged")
    addVersionCatalog(this, "mojang")
    addVersionCatalog(this, "compactmods")
    addVersionCatalog(this, "mods")
}

fun addVersionCatalog(dependencyResolutionManagement: DependencyResolutionManagement, name: String) {
    dependencyResolutionManagement.versionCatalogs.create(name) {
        from(files("../gradle/$name.versions.toml"))
    }
}