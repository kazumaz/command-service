package com.command.command.service

import com.command.command.service.cli.FileToolsCommand
import org.springframework.boot.ExitCodeExceptionMapper
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import picocli.CommandLine
import picocli.CommandLine.IFactory
import picocli.CommandLine.ExitCode
import picocli.CommandLine.ParameterException
import java.util.concurrent.Callable

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class, KafkaAutoConfiguration::class, WebMvcAutoConfiguration::class])
class Application(
		private val fileToolsCommand: FileToolsCommand,
		private val factory: IFactory
) : ExitCodeExceptionMapper {
	private var exitCode: Int = 0
	fun run(args: Array<String>) {
		val cmd = CliCommand()
		val commandLine = CommandLine(cmd)
				.addSubcommand(fileToolsCommand)
		try {
			val parsed = commandLine.parseArgs(*args)
			if (parsed.subcommand() == null &&
					!parsed.isUsageHelpRequested &&
					!parsed.isVersionHelpRequested
			) {
				commandLine.usage(System.out)
				exitCode = ExitCode.USAGE
				return
			}
		} catch (ignored: ParameterException) {
			// Do nothing
		}
		exitCode = commandLine.setExitCodeExceptionMapper(cmd).execute(*args)
	}

		override fun getExitCode(exception: Throwable?): Int {
			return exitCode

	}

}

@CommandLine.Command(
		name = "filetools", mixinStandardHelpOptions = true,
		versionProvider = CliCommand::class, description = ["create/delete file(s) command"]
)

class CliCommand : Callable<Int>, CommandLine.IExitCodeExceptionMapper, CommandLine.IVersionProvider {
	override fun call(): Int = CommandLine.ExitCode.OK
	override fun getVersion(): Array<String> = arrayOf("1.0.0")
	override fun getExitCode(exception: Throwable?): Int {
		return 1
	}
}

fun main(args: Array<String>) {
	SpringApplication.run(Application::class.java, *args).getBean(Application::class.java).run(args)
}

@org.springframework.context.annotation.Configuration
class Configuration {
}



