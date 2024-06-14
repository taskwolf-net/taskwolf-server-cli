package net.taskwolf.server.cli.command.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.server.cli.command.Command;

@Singleton
public final class HelpCommand extends Command {
  @Inject
  private HelpCommand() {
    super("help", new String[] {"info", "commands"}, new String[0]);
  }

  @Override
  public boolean execute(String[] arguments) {
    System.out.println();
    System.out.println("A code-free automation tool for your environment");
    System.out.println();
    System.out.println("Commands: ");
    System.out.println("  help");
    System.out.println("  version");
    System.out.println("  login");
    System.out.println("  logout");
    System.out.println("  command");
    System.out.println("  file");
    System.out.println("  workspace");
    System.out.println();
    System.out.println("Usage:");
    System.out.println("  taskwolf [COMMAND]");
    System.out.println();
    return true;
  }
}
