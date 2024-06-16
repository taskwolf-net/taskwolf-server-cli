package net.taskwolf.server.cli;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.taskwolf.server.cli.command.Command;
import net.taskwolf.server.cli.command.CommandRegistry;
import net.taskwolf.server.cli.command.implementation.*;

import java.io.File;

public class ServerCLIApplication {
  public static void main(String[] args) throws Exception {
    if (!checkPermission()) {
      return;
    }
    var injector = Guice.createInjector(ServerCLIInjectionModule.create());
    var commandRegistry = injector.getInstance(CommandRegistry.class);
    registerCommands(commandRegistry, injector);
    processCommand(commandRegistry, args);
  }

  private static void registerCommands(
    CommandRegistry registry, Injector injector
  ) {
    registry.register(injector.getInstance(VersionCommand.class));
    registry.register(injector.getInstance(HelpCommand.class));
    registry.register(injector.getInstance(LoginCommand.class));
    registry.register(injector.getInstance(LogoutCommand.class));
    registry.register(injector.getInstance(CommandCommand.class));
    registry.register(injector.getInstance(FileCommand.class));
    registry.register(injector.getInstance(WorkspaceCommand.class));
  }

  private static void processCommand(
    CommandRegistry registry, String[] arguments
  ) throws Exception {
    if (arguments.length == 0) {
      registry.find("help").get().execute(arguments);
      return;
    }
    var commandName = arguments[0];
    registry.find(commandName).ifPresentOrElse(command ->
        executeCommand(command, arguments),
      () -> System.out.println("Command '" + commandName + "' not found"));
  }

  private static void executeCommand(Command command, String[] input) {
    var length = input.length - 1;
    var arguments = new String[length];
    System.arraycopy(input, 1, arguments, 0, length);
    try {
      if (!command.execute(arguments)) {
        printOutSyntax(command);
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private static void printOutSyntax(Command command) {
    var arguments = command.arguments();
    var syntax = new StringBuilder("Syntax: " + command.name() + " ");
    for (var i = 0; i < arguments.length; i++) {
      syntax.append(arguments[i]).append(i != arguments.length - 1 ? " / " : " ");
    }
    System.out.println(syntax);
  }

  private static boolean checkPermission() {
    var file = new File("/usr/local/taskwolf/test");
    if (!file.mkdirs()) {
      System.out.println("Permission denied.");
      return false;
    }
    file.delete();
    return true;
  }
}
