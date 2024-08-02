package net.taskwolf.server.cli.command.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.server.cli.command.Command;
import net.taskwolf.server.cli.credential.CredentialConfiguration;
import net.taskwolf.server.cli.device.DeviceConfiguration;
import net.taskwolf.server.cli.request.TaskwolfRequest;
import org.json.JSONObject;

import java.util.Map;

@Singleton
public final class DeleteCommand extends Command {
  @Inject
  private DeleteCommand() {
    super("delete", new String[0], new String[0]);
  }

  @Override
  public boolean execute(String[] arguments) throws Exception {
    var credentials = CredentialConfiguration.createAndLoad();
    if (!credentials.exists()) {
      System.out.println("You are not logged in. For this reason, you cannot " +
        "execute the command.");
      return true;
    }
    var console = System.console();
    System.out.println("Warning: Are you sure you want to delete this device? " +
        "All information and settings from this device will be permanently " +
        "deleted and cannot be restored afterwards. Furthermore, existing " +
        "workflows may be destroyed by the deletion. If you are aware of " +
        "this, you can continue.");
    System.out.println();
    System.out.println("To make sure that you are really the owner of this " +
      "device, we ask you to enter your Taskwolf account password.");
    System.out.println();
    var password = new String(console.readPassword("Password: "));
    deleteDevice(credentials, password);
    return true;
  }

  private static final String DEVICE_DELETE_URL =
    "https://api.taskwolf.net/v1/device/delete/";

  private boolean deleteDevice(
    CredentialConfiguration credentials, String password
  ) throws Exception {
    var requestBody = Map.of("device", credentials.device(), "password", password);
    var response = TaskwolfRequest.create(DEVICE_DELETE_URL, "POST",
      new JSONObject(requestBody)).sendAuthorized(credentials.token());
    var success = new JSONObject(response.body()).getBoolean("success");
    if (!success) {
      System.out.println("An error has occurred: The password you " +
        "entered is incorrect. Please try again");
      return true;
    }
    finishDeletion();
    return true;
  }

  private void finishDeletion() throws Exception {
    CredentialConfiguration.createAndLoad().delete();
    DeviceConfiguration.createAndLoad().delete();
    Runtime.getRuntime().exec("systemctl daemon-reload");
    Runtime.getRuntime().exec("systemctl restart taskwolf.service");
    System.out.println("The deletion process was successful.");
  }
}
