package dev.murmurs.kapper

import com.facebook.ktfmt.format.Formatter
import com.google.common.io.Resources
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

/**
 * This integration test compiles generated code and compares generated code to expected for annotation processor.
 */
class IntegrationTest {
    companion object {
        const val EXPECTED_GENERATED_MAPPER_NAME = "TestMapperImpl"
    }

    @Test
    fun test() {
        val path = Resources.getResource("integration").file
        File(path).listFiles()?.forEach { testCasePath ->
            if (testCasePath.isDirectory) {
                testOneCase(testCasePath)
            }
        }
    }

    private fun testOneCase(path: File) {
        val sourceFiles = getSourceFiles(path)
        if (sourceFiles.isEmpty()) {
            return
        }
        val compilation = KotlinCompilation().apply {
            sources = sourceFiles
            symbolProcessorProviders = listOf(MapperProcessorProvider())
            inheritClassPath = true
        }

        val result = compilation.compile()
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        val generatedMapperImpl = compilation.kspSourcesDir.walkTopDown()
            .filter { it.isFile && it.nameWithoutExtension == EXPECTED_GENERATED_MAPPER_NAME }
            .first().readText()

        val expectedMapperImpl =
            path.listFiles()?.first { it.nameWithoutExtension == EXPECTED_GENERATED_MAPPER_NAME }!!.readText()

        Assertions.assertEquals(Formatter.format(expectedMapperImpl), Formatter.format(generatedMapperImpl))
    }


    /**
     * getSourceFiles function is used to get all source files in the directory
     * @param dir: path to the directory
     * @return list of source files
     */
    private fun getSourceFiles(dir: File): List<SourceFile> {
        val files = dir.listFiles()
        val sourceFiles = mutableListOf<SourceFile>()
        files?.filter { it.nameWithoutExtension != "TestMapperImpl" }
            ?.forEach {
                if (it.isFile) {
                    sourceFiles.add(SourceFile.fromPath(it))
                } else {
                    sourceFiles.addAll(getSourceFiles(it))
                }
            }
        return sourceFiles
    }
}
