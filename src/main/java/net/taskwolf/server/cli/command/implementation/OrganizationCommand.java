package net.taskwolf.server.cli.command.implementation;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.server.cli.command.Command;
import net.taskwolf.server.cli.credential.CredentialConfiguration;
import net.taskwolf.server.cli.request.TaskwolfRequest;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public final class OrganizationCommand extends Command {
  @Inject
  private OrganizationCommand() {
    super("organization", new String[0], new String[] {"list", "add", "remove"});
  }

  @Override
  public boolean execute(String[] arguments) throws Exception {
    if (arguments.length == 0) {
      return false;
    }
    if (arguments[0].equalsIgnoreCase("list")) {
      return listOrganizations();
    }
    if (arguments[0].equalsIgnoreCase("add")) {
      return addOrganization();
    }
    if (arguments[0].equalsIgnoreCase("remove")) {
      return removeOrganization();
    }
    return false;
  }

  private static final String DEVICE_ORGANIZATION_LIST_URL =
    "https://api.taskwolf.net/v1/device/organizations/";

  private boolean listOrganizations() throws Exception {
    var credentials = CredentialConfiguration.createAndLoad();
    if (!credentials.exists()) {
      System.out.println("You are not logged in. For this reason, you cannot " +
        "execute the command.");
      return true;
    }
    var requestBody = Map.of("device", credentials.device());
    var response = TaskwolfRequest.create(DEVICE_ORGANIZATION_LIST_URL, "POST",
      new JSONObject(requestBody)).sendAuthorized(credentials.token());
    var organizations = new JSONObject(response.body()).getJSONArray("organizations");
    if (organizations.isEmpty()) {
      System.out.println("The device has not yet been released for any organization.");
      return true;
    }
    System.out.println("Organizations (" + organizations.length() + ")");
    for (var workspace : organizations) {
      System.out.println(" - " + ((JSONObject) workspace).getString("name"));
    }
    return true;
  }

  private boolean addOrganization() throws Exception {
    var organizations = displayAvailableAddableOrganizations();
    if (organizations.isEmpty()) {
      return true;
    }
    System.out.println();
    System.out.println("Now enter the number of the organization to which you " +
      "would like to make the device available.");
    var organization = System.console().readLine("Organization: ");
    try {
      var organizationIndex = Integer.parseInt(organization);
      if (organizationIndex < 1 || organizationIndex > organizations.size()) {
        throw new Exception();
      }
      addorganization(((HashMap<String, String>)
        organizations.get(organizationIndex - 1)).get("id"));
      System.out.println("The addition was successful. The device is now " +
        "available to the organization.");
    } catch (Exception exception) {
      System.out.println("An error has occurred. Please try again");
    }
    return true;
  }

  private static final String ORGANIZATION_ADD_URL =
    "https://api.taskwolf.net/v1/device/organization/add/";

  private void addorganization(String organizationId) throws Exception {
    var credentials = CredentialConfiguration.createAndLoad();
    var requestBody = Map.of("device", credentials.device(),
      "organization", organizationId);
    TaskwolfRequest.create(ORGANIZATION_ADD_URL, "POST",
      new JSONObject(requestBody)).sendAuthorized(credentials.token());
  }

  private static final String ALL_ORGANIZATION_LIST_URL =
    "https://api.taskwolf.net/v1/organizations/all/";

  private List<Object> displayAvailableAddableOrganizations() throws Exception {
    var credentials = CredentialConfiguration.createAndLoad();
    if (!credentials.exists()) {
      System.out.println("You are not logged in. For this reason, you cannot " +
        "execute the command.");
      return Lists.newArrayList();
    }
    var organizations = findAvailableOrganizations(credentials);
    if (organizations.isEmpty()) {
      System.out.println("There is no organization available to which you can " +
        "still make the device available. Either you have already integrated " +
        "all organizations or you are not yet a member of an organization.");
      return organizations;
    }
    System.out.println("Organizations (" + organizations.size() + ")");
    for (var i = 0; i < organizations.size(); i++) {
      var organizationName = ((HashMap<String, String>) organizations.get(i))
        .get("name");
      System.out.println(" " + (i + 1) + " " + organizationName);
    }
    return organizations;
  }

  private List<Object> findAvailableOrganizations(
    CredentialConfiguration credentials
  ) throws Exception {
    var allOrganizationsResponse = TaskwolfRequest.create(ALL_ORGANIZATION_LIST_URL,
      "GET", new JSONObject("{}")).sendAuthorized(credentials.token());
    var organizations = new JSONObject(allOrganizationsResponse.body())
      .getJSONArray("organizations").toList();
    var deviceOrganizationsResponse = TaskwolfRequest.create(DEVICE_ORGANIZATION_LIST_URL,
        "POST", new JSONObject(Map.of("device", credentials.device())))
      .sendAuthorized(credentials.token());
    var deviceOrganizations = new JSONObject(deviceOrganizationsResponse.body())
      .getJSONArray("organizations").toList();
    organizations = organizations.stream().filter(organization ->
      deviceOrganizations.stream().noneMatch(deviceOrganization ->
        ((HashMap<String, String>) organization).get("id")
          .equals(((HashMap<String, String>) deviceOrganization).get("id"))))
      .collect(Collectors.toList());
    return organizations;
  }

  private boolean removeOrganization() throws Exception {
    var organizations = displayAvailableRemovableOrganizations();
    if (organizations.isEmpty()) {
      return true;
    }
    System.out.println();
    System.out.println("Now enter the number of the organization you would " +
      "like to remove.");
    var organization = System.console().readLine("Organization: ");
    try {
      var organizationIndex = Integer.parseInt(organization);
      if (organizationIndex < 1 || organizationIndex > organizations.size()) {
        throw new Exception();
      }
      removeOrganization(((HashMap<String, String>)
        organizations.get(organizationIndex - 1)).get("id"));
      System.out.println("The removal was successful. The device is no " +
        "longer available to the organization.");
    } catch (Exception exception) {
      System.out.println("An error has occurred. Please try again");
    }
    return true;
  }

  private static final String ORGANIZATION_REMOVE_URL =
    "https://api.taskwolf.net/v1/device/organization/remove/";

  private void removeOrganization(String organizationId) throws Exception {
    var credentials = CredentialConfiguration.createAndLoad();
    var requestBody = Map.of("device", credentials.device(),
      "organization", organizationId);
    TaskwolfRequest.create(ORGANIZATION_REMOVE_URL, "POST",
      new JSONObject(requestBody)).sendAuthorized(credentials.token());
  }

  private List<Object> displayAvailableRemovableOrganizations() throws Exception {
    var credentials = CredentialConfiguration.createAndLoad();
    if (!credentials.exists()) {
      System.out.println("You are not logged in. For this reason, you cannot " +
        "execute the command.");
      return Lists.newArrayList();
    }
    var requestBody = Map.of("device", credentials.device());
    var response = TaskwolfRequest.create(DEVICE_ORGANIZATION_LIST_URL, "POST",
      new JSONObject(requestBody)).sendAuthorized(credentials.token());
    var organizations = new JSONObject(response.body())
      .getJSONArray("organizations").toList();
    if (organizations.isEmpty()) {
      System.out.println("The device has not yet been released for any organization.");
      return Lists.newArrayList();
    }
    System.out.println("Organizations (" + organizations.size() + ")");
    for (var i = 0; i < organizations.size(); i++) {
      var organizationName = ((HashMap<String, String>) organizations.get(i))
        .get("name");
      System.out.println(" " + (i + 1) + " " + organizationName);
    }
    return organizations;
  }
}
