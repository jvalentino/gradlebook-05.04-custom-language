package com.blogspot.jvalentino.gradle

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification
import spock.lang.Subject

class CleanTaskTestSpec extends Specification {
    
    @Subject
    CleanTask task
    Project project
    
    def setup() {
        Project p = ProjectBuilder.builder().build()
        task = p.task('clean', type:CleanTask)
        task.instance = Mock(CleanTask)
        project = Mock(ProjectInternal)
    }
    
    void "test perform"() {
        given:
        File buildDir = Mock(File)
        GroovyMock(FileUtils, global:true)
        
        when:
        task.perform()
        
        then:
        _ * task.instance.project >> project
        _ * project.buildDir >> buildDir
        1 * FileUtils.deleteDirectory(buildDir)     
    }

}
