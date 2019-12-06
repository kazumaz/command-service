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
import java.util.concurrent.Callable

@Component
@CommandLine.Command(
        name = "ascii", mixinStandardHelpOptions = true,
        versionProvider = CliCommand::class,
        description = ["output ascii command"])
class AsciiCommand : Callable<Int>, IExitCodeExceptionMapper {
    // --create オプションと --delete オプションはいずれか一方しか指定できないようにする
    @ArgGroup(exclusive = true, multiplicity = "1")
    private val exclusive: Exclusive? = null

    internal class Exclusive {
        @CommandLine.Option(names = ["-s", "--sad"], description = ["output sud expression"])
        var isSad: Boolean = true

        @CommandLine.Option(names = ["-h", "--happy"], description = ["output happy expression"])
        var isHappy: Boolean = true
    }

        override fun call(): Int {
            if (exclusive!!.isSad) {
                println("i am sad (´；Д；`)")
            }
            if (exclusive!!.isHappy) {
                println("i am happy ٩꒰｡•◡•｡꒱۶")
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