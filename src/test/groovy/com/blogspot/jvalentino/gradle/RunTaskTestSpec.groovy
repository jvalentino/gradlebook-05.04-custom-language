package com.blogspot.jvalentino.gradle

import java.io.File
import java.util.List

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.internal.plugins.ExtensionContainerInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class RunTaskTestSpec extends Specification {

    @Subject
    RunTask task
    Project project
    ExtensionContainer extensions

    def setup() {
        Project p = ProjectBuilder.builder().build()
        task = p.task('run', type:RunTask)
        task.instance = Mock(RunTask)
        project = Mock(ProjectInternal)
        extensions = Mock(ExtensionContainerInternal)
        FileUtils.deleteDirectory(new File('build/output'))
    }
    
    def cleanup() {
        FileUtils.deleteDirectory(new File('build/output'))
    }

    void "test perform"() {
        given:
        LolExtension ex = new LolExtension()
        File buildDir = Mock(File)
        List<LolFile> files = [new LolFile()]

        when:
        task.perform()

        then:
        _ * task.instance.project >> project
        1 * project.extensions >> extensions
        1 * extensions.findByType(LolExtension) >> ex

        and:
        _ * project.buildDir >> buildDir
        1 * buildDir.mkdirs()

        and:
        1 * task.instance.generateFileList(
                project, buildDir, _, ex.fileExt) >> files

        and:
        task.instance.processFiles(files)
    }

    void "test processFiles"() {
        given:
        LolFile alpha = new LolFile(
                inputFile:new File('build/output/a.lol'),
                outputFile:new File('build/output/a.lol.txt'),
                inputShortName:'build/output/a.lol')
        Command alphaCommand = new Command(output:'blah')
        
        and:
        LolFile bravo = new LolFile(
            inputFile:new File('build/output/b.lol'),
            outputFile:new File('build/output/b.lol.txt'),
            inputShortName:'build/output/b.lol')
        Command bravoCommand = new Command(error:'boo')
    
        
        and:
        List<LolFile> files = [alpha, bravo]
        
        and:
        GroovyMock(OsUtil, global:true)

        when:
        task.processFiles(files)

        then:
        1 * OsUtil.isWindows() >> true
        
        and:
        1 * task.instance.generateCommand(
                true, alpha) >> ['ls']
        1 * task.instance.executeCommand(['ls']) >> alphaCommand
        alpha.outputFile.text == 'blah'
        
        and:
        1 * task.instance.generateCommand(
            true, bravo) >> ['ls']
        1 * task.instance.executeCommand(['ls']) >> bravoCommand
        bravo.outputFile.text == 'boo'
    }
    
    void "Test generateFileList"() {
        given:
        File buildDir = new File('src/test/resources/output')
        File inputDir = new File('src/test/resources/input')
        String fileExt
        
        and:
        ConfigurableFileTree fileTree = 
            Mock(ConfigurableFileTree)
        fileTree.files >> [
            new File('src/test/resources/input/test.lol')
        ]
        
        when:
        List<LolFile> r = task.generateFileList(
            project, buildDir, inputDir, fileExt)
        
        then:
        1 * project.fileTree(_, _) >> fileTree
        
        and:
        LolFile file = r.get(0)
        file.inputFile.absolutePath.endsWith(
            'src/test/resources/input/test.lol')
        file.outputFile.absolutePath.endsWith(
            'src/test/resources/output/test.lol.txt')
        file.inputShortName == 'test.lol'
    }
    
    void "test executeCommand"() {
        given:
        GroovyMock(ByteArrayOutputStream, global:true)
        ByteArrayOutputStream os = Mock(ByteArrayOutputStream)
        ByteArrayOutputStream er = Mock(ByteArrayOutputStream)
        List<String> commands = ['a', 'b']
        
        when:
        Command c = task.executeCommand(commands)
        
        then:
        1 * task.instance.project >> project
        1 * project.exec(_)
        1 * new ByteArrayOutputStream() >> os
        1 * new ByteArrayOutputStream() >> er
        1 * os.toString() >> 'output'
        
        and:
        c.output == 'output'
    }
    
    void "test executeCommand with error"() {
        given:
        GroovyMock(ByteArrayOutputStream, global:true)
        ByteArrayOutputStream os = Mock(ByteArrayOutputStream)
        ByteArrayOutputStream er = Mock(ByteArrayOutputStream)
        List<String> commands = ['a', 'b']
        
        when:
        Command c = task.executeCommand(commands)
        
        then:
        1 * task.instance.project >> project
        1 * project.exec(_) >> { throw new Exception('foo') }
        1 * new ByteArrayOutputStream() >> os
        1 * new ByteArrayOutputStream() >> er
        1 * er.toString() >> 'output'
        
        and:
        c.error == 'output'
    }
    
    @Unroll
    void "test generateCommand where windows=#isWindows"() {
        given:
        File inputFile = Mock(File)
        LolFile file = new LolFile(
            inputFile:inputFile)
        
        when:
        List<String> c = task.generateCommand(isWindows, file)
        
        then:
        _ * inputFile.absolutePath >> '/a/b.lol'
        c.toString() == result
        
        where:
        isWindows   || result
        false       || '[lci, /a/b.lol]'
        true        || '[cmd, /c, lci, /a/b.lol]'
    }
}
