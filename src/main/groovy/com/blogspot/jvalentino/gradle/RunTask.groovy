package com.blogspot.jvalentino.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

/**
 * Runs the lolz
 * @author jvalentino2
 */
@SuppressWarnings(['Println'])
class RunTask extends DefaultTask {

    RunTask instance = this

    @TaskAction
    void perform() {
        Project p = instance.project

        LolExtension ex =
                p.extensions.findByType(LolExtension)

        println "- Output to ${p.buildDir}"
        p.buildDir.mkdirs()

        File inputDir = new File(ex.inputDir)
        println "- Input from ${inputDir.absolutePath}"

        List<LolFile> files = instance.generateFileList(
                p, p.buildDir, inputDir, ex.fileExt)
        
        instance.processFiles(files)
    }

    void processFiles(List<LolFile> files) {
        boolean isWindows = OsUtil.isWindows()

        for (LolFile file : files) {
            List<String> commands = instance.generateCommand(
                    isWindows, file)
            Command result = instance.executeCommand(commands)

            file.outputFile.parentFile.mkdirs()

            String status = 'PASS'
            if (result.error != null) {
                status = 'FAIL'
                file.outputFile.text = result.error
            } else {
                file.outputFile.text = result.output
            }

            println "- [${status}] ${file.inputShortName} " +
                    "-> build/${file.inputShortName}.txt"
            println "   > STDOUT: ${result.output}"
            println "   > STDERR: ${result.error}"
        }
    }

    List<LolFile> generateFileList(Project p, File buildDir, 
        File inputDir, String fileExt) {
        
        List<LolFile> files = []
        FileTree tree = p.fileTree(inputDir) { include "**/*${fileExt}" }

        tree.files.each { File f ->
            String dirDiff = f.absolutePath.replace(
                inputDir.absolutePath, '')

            File outputFile = 
                new File("${buildDir.absolutePath}${dirDiff}.txt")

            LolFile file = new LolFile(
                    inputFile:f,
                    outputFile:outputFile,
                    inputShortName:dirDiff[1..-1])

            files.add(file)
        }

        files
    }

    Command executeCommand(List<String> commands) {
        Command c = new Command()

        ByteArrayOutputStream stdout = new ByteArrayOutputStream()
        ByteArrayOutputStream sterr = new ByteArrayOutputStream()

        try {
            instance.project.exec {
                commandLine commands
                standardOutput = stdout
                errorOutput = sterr
            }

            c.output = stdout.toString().trim()
        } catch (e) {
            c.error = sterr.toString().trim()
        }
        c
    }

    List<String> generateCommand(boolean isWindows, LolFile file) {
        List<String> commands = []

        if (isWindows) {
            commands.add('cmd')
            commands.add('/c')
        }
        commands.add('lci')
        commands.add(file.inputFile.absolutePath)

        commands
    }
}
