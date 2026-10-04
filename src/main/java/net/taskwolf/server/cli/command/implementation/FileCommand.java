package net.taskwolf.server.cli.command.implementation;

import net.taskwolf.server.cli.command.Command;
import net.taskwolf.server.service.credential.CredentialConfiguration;
import net.taskwolf.server.service.file.FileConfiguration;
import net.taskwolf.server.service.request.TaskwolfRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.json.JSONObject;

import java.util.Map;

@Singleton
public final class FileCommand extends Command {
  @Inject
  private FileCommand() {
    super("file", new String[] {"files"}, new String[] {"enable <true / false>"});
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

  private static final String FILE_SETTINGS_URL =
    "https://api.taskwolf.net/v1/device/file/settings/update/";

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
      "fileStorage", enabled, "fileInfo", enabled, "fileDelete", enabled,
      "folderCreate", enabled, "folderDelete", enabled);
    TaskwolfRequest.create(FILE_SETTINGS_URL, "POST", new JSONObject(requestBody))
      .sendAuthorized(credentials.token());
    FileConfiguration.createAndStore(Boolean.parseBoolean(enabled));
    System.out.println("The file settings were successfully updated");
    return true;
  }
}