package net.taskwolf.server.cli.command.implementation;

import net.taskwolf.server.cli.command.Command;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public final class VersionCommand extends Command {
  @Inject
  private VersionCommand() {
    super("version", new String[0], new String[0]);
  }

  @Override
  public boolean execute(String[] arguments) {
    System.out.println();
    System.out.println("Taskwolf CLI Version 1.0.0");
    System.out.println();
    return true;
  }
}
