package com.blogspot.jvalentino.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * <p>A basic gradle plugin.</p>
 * @author jvalentino2
 */
class LolPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create 'lol', LolExtension
        project.task('clean', type:CleanTask)
        project.task('run', type:RunTask)
    }
}
