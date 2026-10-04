package net.taskwolf.server.cli.command.implementation;

import net.taskwolf.server.cli.command.Command;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public final class HelpCommand extends Command {
  @Inject
  private HelpCommand() {
    super("help", new String[] {"info"}, new String[0]);
  }

  @Override
  public boolean execute(String[] arguments) {
    System.out.println();
    System.out.println("A code-free automation tool for your environment");
    System.out.println();
    System.out.println("Commands: ");
    System.out.println("  help - Shows you the list of all available commands");
    System.out.println("  version - Displays the currently installed version " +
      "of the Taskwolf CLI");
    System.out.println("  login - Used to connect the device to your " +
      "Taskwolf account");
    System.out.println("  logout - Logs the device out of Taskwolf");
    System.out.println("  organization - You can use the command to manage the " +
      "availability of the device for your organizations");
    System.out.println("  command - Controls the execution of commands on" +
      " this device");
    System.out.println("  file - Controls the management of files on this device");
    System.out.println("  workspace - You can use this command to control " +
      "the workspaces of the device");
    System.out.println("  delete - This command deletes this device from " +
      "Taskwolf and it is no longer available for automation");
    System.out.println();
    System.out.println("Usage:");
    System.out.println("  taskwolf [COMMAND]");
    System.out.println();
    return true;
  }
}
