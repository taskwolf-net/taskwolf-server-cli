package net.taskwolf.server.cli.command.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.server.cli.command.Command;
import net.taskwolf.server.cli.credential.CredentialConfiguration;

@Singleton
public final class LogoutCommand extends Command {
  @Inject
  private LogoutCommand() {
    super("logout", new String[0], new String[0]);
  }

  @Override
  public boolean execute(String[] arguments) throws Exception {
    var credentials = CredentialConfiguration.createAndLoad();
    if (!credentials.exists()) {
      System.out.println("You are not logged in. Therefore we cannot log you out.");
      return true;
    }
    credentials.delete();
    Runtime.getRuntime().exec("systemctl daemon-reload");
    Runtime.getRuntime().exec("systemctl restart taskwolf.service");
    System.out.println("The logout process was successful.");
    return true;
  }
}