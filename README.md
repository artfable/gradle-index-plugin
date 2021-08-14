# Gradle Resource Plugin
[ ![artifactory](https://img.shields.io/badge/Artifactory-v0.0.3-green) ](https://artfable.jfrog.io/ui/packages/gav:%2F%2Fcom.artfable.gradle:gradle-index-plugin)

## Overview
The plugin that was written on [kotlin](https://kotlinlang.org). 
It gives you opportunity to set `index.html` (it could has another name and another extension) file differently 
for all types of builds. For example, you can set different index file for production build and developing build.

## Install
```kotlin
buildscript {
    repositories {
        maven(url = "https://artfable.jfrog.io/artifactory/default-maven-local")
    }
    dependencies {
        classpath("com.github.artfable.gradle:gradle-index-plugin:0.0.3")
    }
}

apply(plugin = "artfable.index")
```

It'll add a task `resolveIndex`

For use in `plugins {}` see [Gradle resolution strategy](https://docs.gradle.org/current/userguide/custom_plugins.html#note_for_plugins_published_without_java_gradle_plugin)

## Usage
First, you need to create your template index file, and add `%MIN%` to imported files that has minified versions. 
Then you should add `%MODULES%` to a place in the index file where you would like to add your js files.

Example:

```html
<!-- ... -->
<script src="lib/js/jquery-3.0%MIN%.js"></script> 
<script src="lib/js/someLibThatHasOnlyMinVersion.min.js"></script> 
<!-- ... -->
<script src="js/myFile%MIN%.js"></script>
%MODULES%
<!-- ... -->
```

Then set the configuration of the plugin (`indexConfig`). For each configuration you could set:

+ source - path to template file
+ output - path to output **directory**
+ indexResultName - name of result file (with extension) (default: 'index.html')
+ modulesDir - name with path for directory with your modules, don't set it if you don't want to allow auto searching for modules.
+ outputModulesDir - name with path from `output` for directory, where modules will be. Used only if `modulesDir` was set. (default: 'modules')
+ minify - add `.min` to the libraries or not (default: true)
+ modules - set some specials modules from wherever your want (without extension or `.min`).   
+ modulesMinify - add `.min` to your modules or not. It works only with `modules` (default: true)

```kotlin
indexConfig {
    create("dev") { // just dev {} in groovy dsl
        source = "$projectDir/src/main/webapp/index.html"
        output = "${buildDir}/tmp/weabapp"
        modulesDir = "src/main/webapp/modules"
        outputModulesDir = "js/modules"
        minify = false
    }
    create("prod") {
        source = "src/main/webapp/index.html"
        output = "${buildDir}/tmp/weabapp"
        modules = arrayOf("app", "js/modules")
    }
    // ... other configuration's blocks
}
```

To choose what configuration should be used set property `indexProfile` (for example: `-PindexProfile=dev`). 
If not present, will try to use `dev`. 

If task `proccessResources` is present, the task `resolveIndex` will depend on it. 