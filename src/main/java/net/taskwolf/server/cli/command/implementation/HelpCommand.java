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
    System.out.println("Commands: ");
    System.out.println("  - help");
    return true;
  }
}
