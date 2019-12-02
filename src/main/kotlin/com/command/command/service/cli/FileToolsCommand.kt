package com.command.command.service.cli


import com.command.command.service.CliCommand
import org.springframework.stereotype.Component
import picocli.CommandLine
import picocli.CommandLine.*

import java.io.File
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Arrays
import java.util.concurrent.Callable

@Component
@CommandLine.Command(
        name = "filetools", mixinStandardHelpOptions = true,
        versionProvider = CliCommand::class,
        description = ["create/delete file(s) command"])
class FileToolsCommand : Callable<Int>, IExitCodeExceptionMapper {
    // --create オプションと --delete オプションはいずれか一方しか指定できないようにする
    @ArgGroup(exclusive = true, multiplicity = "1")
    private val exclusive: Exclusive? = null

    internal class Exclusive {

        @CommandLine.Option(names = ["-c", "--create"], description = ["create file(s)"])
        var isCreate: Boolean = false

        @CommandLine.Option(names = ["-d", "--delete"], description = ["delete file(s)"])
        var isDelete: Boolean = false
    }

    @Parameters(paramLabel = "ファイル", description = ["作成あるいは削除するファイル"])
    private val files: Array<File>? = null

        override fun call(): Int {
            listOf(*this.files!!).forEach { f ->
                try {
                    if (exclusive!!.isCreate) {
                        Files.createFile(Paths.get(f.name))
                        println(f.name + " is created.")
                    } else if (exclusive!!.isDelete) {
                        Files.deleteIfExists(Paths.get(f.name))
                        println(f.name + " is deleted.")
                    }
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
        return ExitCode.OK
    }

    override fun getExitCode(exception: Throwable): Int {
        val cause = exception.cause
        if (cause is FileAlreadyExistsException) {
            // 既に存在するファイルを作成しようとしている
            return 12
        } else if (cause is FileSystemException) {
            // 削除しようとしたファイルが別のプロセスでオープンされている等
            return 13
        }
        return 11
    }

}