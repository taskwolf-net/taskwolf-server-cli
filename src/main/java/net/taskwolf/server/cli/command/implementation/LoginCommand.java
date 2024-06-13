package net.taskwolf.server.cli.command.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.server.cli.command.Command;

import java.util.Scanner;

@Singleton
public final class LoginCommand extends Command {
  @Inject
  private LoginCommand() {
    super("login", new String[0], new String[0]);
  }

  @Override
  public boolean execute(String[] arguments) {
    var scanner = new Scanner(System.in);
    var console = System.console();
    System.out.println("Taskwolf login");
    System.out.println();
    var email = console.readLine("Email: ");
    var password = new String(console.readPassword("Password: "));
    //TODO: SEND LOGIN REQUEST
    //      SAFE CREDENTIALS
    //      RESTART SERVICE
    return true;
  }
}
