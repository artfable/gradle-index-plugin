package com.artfable.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import java.io.File
import java.util.*

/**
 * Parameter modulesMinify is unacceptable for modulesDir; all modules, that will be found, will be added as is.
 * Add project's property indexProfile - default 'dev'
 *
 * @author artfable
 * 08.09.2016
 */
open class GradleIndexPlugin : Plugin<Project> {

    companion object {
        private const val PROP_NAME: String = "indexProfile"
    }

    override fun apply(project: Project) {
        val config: NamedDomainObjectContainer<ByProfileExtension> = project.container(ByProfileExtension::class.java)
        project.extensions.add("indexConfig", config)

        val task = project.task("resolveIndex")
        task.group = "frontend"
        task.description = "Resolve html file with js dependencies"

        project.gradle.afterProject {
            tasks.findByPath("processResources")?.let { task.dependsOn(it) }
        }

        task.doFirst {
            val properties: ExtraPropertiesExtension = project.extensions.extraProperties
            var profile: String = "dev"
            if (properties.has(PROP_NAME)) {
                profile = properties[PROP_NAME].toString()
            }

            val chosenConfig = config.findByName(profile)
            chosenConfig ?: throw IllegalArgumentException("No config for '$profile' profile")

            logger.info("Using $profile profile")

            val source: File = File(chosenConfig.source ?: throw IllegalArgumentException("Source file is not provided"))
            val outputDir: File = File(chosenConfig.output ?: throw IllegalArgumentException("Output directory is not provided"))

            if (outputDir.isFile) {
                throw IllegalArgumentException("Couldn't use file as output directory!!")
            }

            if (!source.isFile) {
                throw IllegalArgumentException("Couldn't open source")
            }

            outputDir.mkdirs()
            val outputFile: File = File(outputDir.canonicalPath + File.separator + chosenConfig.indexResultName)
            outputFile.delete()
            outputFile.createNewFile()

            var modules: String = ""
            val suffix: String = (if (chosenConfig.modulesMinify) ".min" else "") + ".js"
            chosenConfig.modules.forEach { module ->
                modules += "<script src=\"${module + suffix}\"></script>\n"
            }
            modules += findModules(chosenConfig.modulesDir, chosenConfig.outputModulesDir)

            source.forEachLine { line ->
                outputFile
                    .appendText(line.replace("%MIN%", (if (chosenConfig.minify) ".min" else ""))
                    .replace("%MODULES%", modules) + '\n')
            }
        }
    }

    private fun findModules(modulesDirName: String?, outputModulesDirPath: String): String {
        modulesDirName ?: return ""

        val modulesDir = File(modulesDirName)

        if (!modulesDir.isDirectory) {
            throw IllegalArgumentException("Incorrect directory: '$modulesDirName'")
        }

        return findJsModules(modulesDir, outputModulesDirPath, "/")
    }

    private fun findJsModules(directory: File, outputModulesDirPath: String, parent: String): String {
        var modules: String = ""

        directory.listFiles { file ->
            file.isDirectory
        }?.forEach  { subDir ->
            modules += findJsModules(subDir, outputModulesDirPath, parent + subDir.name + '/')
        }

        directory.listFiles { file ->
            file.isFile && file.name.endsWith(".js")
        }?.forEach { module ->
            modules += "<script src=\"${outputModulesDirPath + parent + module.name}\"></script>\n"
        }

        return modules
    }
}

open class ByProfileExtension(val name: String) {
    var source: String? = null
    var output: String? = null
    var modulesDir: String? = null
    var indexResultName: String = "index.html"
    var outputModulesDir: String = "modules"
    var minify: Boolean = true
    var modulesMinify: Boolean = true
    var modules: Array<String> = arrayOf()

    override fun toString(): String {
        return "ByProfileExtension(name='$name', source=$source, output=$output, minify=$minify, modules=${modules.contentToString()})"
    }
}