package com.blogspot.jvalentino.gradle

import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Deletes the build dir
 * @author jvalentino2
 */
@SuppressWarnings(['Println'])
class CleanTask extends DefaultTask {

    CleanTask instance = this

    @TaskAction
    void perform() {
        println "- Deleting ${instance.project.buildDir}"
        FileUtils.deleteDirectory(instance.project.buildDir)
    }
}
