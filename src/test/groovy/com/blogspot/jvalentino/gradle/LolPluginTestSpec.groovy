package com.blogspot.jvalentino.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification
import spock.lang.Subject

class LolPluginTestSpec extends Specification {

    Project project
    @Subject
    LolPlugin plugin

    def setup() {
        project = ProjectBuilder.builder().build()
        plugin = new LolPlugin()
    }

    void "test plugin"() {
        when:
        plugin.apply(project)

        then:
        project.tasks.getAt(0).toString() == "task ':clean'"
        project.tasks.getAt(1).toString() == "task ':run'"
    }
}
