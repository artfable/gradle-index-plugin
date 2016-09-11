# Gradle Resource Plugin
(version: 0.0.1)

## Overview
The plugin that was written on [kotlin](https://kotlinlang.org). 
It give you opportunity to set `index.html` (it could has another name and another extension) file differently 
for all types of builds. For example, you can set different index file for production build and developing build.

## Install
```groovy
buildscript {
    repositories {
        maven {
            url 'http://dl.bintray.com/artfable/gradle-plugins' // I hope that it'll be jcenter() in the future :)
        }
    }
    dependencies {
        classpath "com.github.artfable.gradle:gradle-index-plugin:0.0.1"
    }
}

apply plugin: 'artfable.index'
```

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
+ modulesDir - name with path for directory with your modules, don't set it if you wouldn't to allow auto searching for modules.
+ outputModulesDir - name with path from `output` for directory, where modules will be. Used only if `modulesDir` was set. (default: 'modules')
+ minify - add `.min` to the libraries or not (default: true)
+ modules - set some specials modules from wherever your want (without extension or `.min`).   
+ modulesMinify - add `.min` to your modules or not. It works only with `modules` (default: true)

```groovy
indexConfig {
    dev {
        source = "src/main/webapp/index.html"
        output = "${buildDir}/tmp/weabapp"
        modulesDir = 'src/main/webapp/modules'
        outputModulesDir = 'js/modules'
        minify = false
    }
    prod {
        source = "src/main/webapp/index.html"
        output = "${buildDir}/tmp/weabapp"
        modules = ['app', 'js/modules']
    }
    // ... other configuration's blocks
}
```

To choose what configuration should be used set property `indexProfile` (for example: `-PindexProfile=dev`).
