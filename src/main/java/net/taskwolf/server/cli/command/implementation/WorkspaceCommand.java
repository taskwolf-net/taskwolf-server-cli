package net.taskwolf.server.cli.command.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.server.cli.command.Command;
import net.taskwolf.server.cli.credential.CredentialConfiguration;
import net.taskwolf.server.cli.request.TaskwolfRequest;
import org.json.JSONObject;

import java.util.Map;
import java.util.Optional;

@Singleton
public final class WorkspaceCommand extends Command {
  @Inject
  private WorkspaceCommand() {
    super("workspace", new String[0], new String[] {"list", "add <path>",
      "remove <path>"});
  }

  @Override
  public boolean execute(String[] arguments) throws Exception {
    if (arguments.length == 0) {
      return false;
    }
    if (arguments[0].equalsIgnoreCase("list")) {
      return listWorkspaces();
    }
    if (arguments[0].equalsIgnoreCase("add")) {
      return addWorkspace(arguments);
    }
    if (arguments[0].equalsIgnoreCase("remove")) {
      return removeWorkspace(arguments);
    }
    return false;
  }

  private static final String WORKSPACE_LIST_URL =
    "https://api.taskwolf.net/v1/device/file/workspaces/find/";

  private boolean listWorkspaces() throws Exception {
    var credentials = CredentialConfiguration.createAndLoad();
    if (!credentials.exists()) {
      System.out.println("For this reason, you cannot execute the command.");
      return true;
    }
    var requestBody = Map.of("device", credentials.device());
    var response = TaskwolfRequest.create(WORKSPACE_LIST_URL, "POST",
      new JSONObject(requestBody)).sendAuthorized(credentials.token());
    var workspaces = new JSONObject(response.body()).getJSONArray("workspaces");
    if (workspaces.isEmpty()) {
      System.out.println("No workspaces have yet been created for the device.");
      return true;
    }
    System.out.println("Workspaces (" + workspaces.length() + ")");
    for (var workspace : workspaces) {
      System.out.println(" - " + ((JSONObject) workspace).getString("path"));
    }
    return true;
  }

  private static final String WORKSPACE_ADD_URL =
    "https://api.taskwolf.net/v1/device/file/workspace/create/";

  private boolean addWorkspace(String[] arguments) throws Exception {
    if (arguments.length != 2) {
      return false;
    }
    var path = arguments[1];
    var credentials = CredentialConfiguration.createAndLoad();
    if (!credentials.exists()) {
      System.out.println("For this reason, you cannot execute the command.");
      return true;
    }
    var requestBody = Map.of("device", credentials.device(), "path", path);
    TaskwolfRequest.create(WORKSPACE_ADD_URL, "POST", new JSONObject(requestBody))
      .sendAuthorized(credentials.token());
    System.out.println("The workspace was successfully created");
    return true;
  }

  private static final String WORKSPACE_REMOVE_URL =
    "https://api.taskwolf.net/v1/device/file/workspace/remove/";

  private boolean removeWorkspace(String[] arguments) throws Exception {
    if (arguments.length != 2) {
      return false;
    }
    var path = arguments[1];
    var credentials = CredentialConfiguration.createAndLoad();
    if (!credentials.exists()) {
      System.out.println("For this reason, you cannot execute the command.");
      return true;
    }
    var target = findTargetWorkspace(credentials, path);
    if (target.isEmpty()) {
      System.out.println("The desired workspace could not be found.");
    } else {
      var deleteRequestBody = Map.of("device", credentials.device(),
        "workspace", target.get());
      TaskwolfRequest.create(WORKSPACE_REMOVE_URL, "POST",
        new JSONObject(deleteRequestBody)).sendAuthorized(credentials.token());
      System.out.println("The selected workspace has been successfully removed.");
    }
    return true;
  }

  private Optional<String> findTargetWorkspace(
    CredentialConfiguration credentials, String path
  ) throws Exception {
    var findRequestBody = Map.of("device", credentials.device());
    var findResponse = TaskwolfRequest.create(WORKSPACE_LIST_URL, "POST",
      new JSONObject(findRequestBody)).sendAuthorized(credentials.token());
    var workspaces = new JSONObject(findResponse.body()).getJSONArray("workspaces");
    return workspaces.toList().stream()
      .map(workspace -> ((Map<String, String>) workspace))
      .filter(workspace -> workspace.get("path").equals(path))
      .map(workspace -> workspace.get("id"))
      .findFirst();
  }
}
