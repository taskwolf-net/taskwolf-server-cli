package net.taskwolf.server.cli.command.implementation;

import net.taskwolf.server.cli.command.Command;
import net.taskwolf.server.service.command.CommandConfiguration;
import net.taskwolf.server.service.credential.CredentialConfiguration;
import net.taskwolf.server.service.request.TaskwolfRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.json.JSONObject;

import java.util.Map;

@Singleton
public final class CommandCommand extends Command {
  @Inject
  private CommandCommand() {
    super("command", new String[] {"commands"}, new String[] {"enable <true / false>"});
  }

  @Override
  public boolean execute(String[] arguments) throws Exception {
    if (arguments.length == 0) {
      return false;
    }
    if (arguments[0].equalsIgnoreCase("enable")) {
      return processEnableCommand(arguments);
    }
    return false;
  }

  private static final String COMMAND_SETTINGS_URL =
    "https://api.taskwolf.net/v1/device/command/settings/update/";

  private boolean processEnableCommand(String[] arguments) throws Exception {
    if (arguments.length != 2) {
      return false;
    }
    var enabled = arguments[1];
    if (!enabled.equals("true") && !enabled.equals("false")) {
      return false;
    }
    var credentials = CredentialConfiguration.createAndLoad();
    if (!credentials.exists()) {
      System.out.println("You are not logged in. For this reason, you cannot " +
        "execute the command.");
      return true;
    }
    var requestBody = Map.of("device", credentials.device(),
      "commandExecution", enabled);
    TaskwolfRequest.create(COMMAND_SETTINGS_URL, "POST", new JSONObject(requestBody))
      .sendAuthorized(credentials.token());
    CommandConfiguration.createAndStore(Boolean.parseBoolean(enabled));
    System.out.println("The command settings were successfully updated");
    return true;
  }
}