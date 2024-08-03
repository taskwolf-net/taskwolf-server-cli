package net.taskwolf.server.cli.command.implementation;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.server.cli.command.Command;
import net.taskwolf.server.cli.credential.CredentialConfiguration;
import net.taskwolf.server.cli.request.TaskwolfRequest;
import org.json.JSONObject;

import java.util.*;
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
    var credentials = CredentialConfiguration.createAndLoad();
    if (!credentials.exists()) {
      System.out.println("You are not logged in. For this reason, you cannot " +
        "execute the command.");
      return true;
    }
    if (arguments[0].equalsIgnoreCase("list")) {
      return listOrganizations(credentials);
    }
    if (arguments[0].equalsIgnoreCase("add")) {
      return addOrganization(credentials);
    }
    if (arguments[0].equalsIgnoreCase("remove")) {
      return removeOrganization(credentials);
    }
    return false;
  }

  private boolean listOrganizations(CredentialConfiguration credentials) throws Exception {
    var users = findDeviceUsers(credentials);
    if (users.isEmpty()) {
      System.out.println("The device has not yet been released for any organization.");
      return true;
    }
    var organizations = users.stream().map(user -> user.get("organizationId"))
      .distinct().collect(Collectors.toList());
    System.out.println("Organizations (" + organizations.size() + ")");
    for (var organization : organizations) {
      var teams = users.stream()
        .filter(user -> user.get("organizationId").equals(organization))
        .collect(Collectors.toList());
      System.out.println(" - " + teams.get(0).get("organizationName"));
      System.out.println("   Teams (" + teams.size() + ")");
      for (var team : teams) {
        System.out.println("    - " + team.get("teamName"));
      }
    }
    return true;
  }

  private boolean addOrganization(CredentialConfiguration credentials) throws Exception {
    var organizations = displayAvailableAddableOrganizations(credentials);
    var organization = entrySelectionProcess(organizations, "Organization",
      "Now enter the number of the organization to which you would like to " +
        "make the device available.");
    if (organization.isEmpty()) {
      return true;
    }
    System.out.println();
    var teams = displayAvailableAddableTeams(credentials, organization.get());
    var team = entrySelectionProcess(teams,  "Team",
      "Now enter the number of the team to which you would like to " +
        "make the device available.");
    if (team.isEmpty()) {
      return true;
    }
    addOrganization(organization.get(), team.get());
    System.out.println("The addition was successful. The device is now " +
      "available to the organization.");
    return true;
  }

  private List<Map<String, String>> displayAvailableAddableOrganizations(
    CredentialConfiguration credentials
  ) throws Exception {
    var organizations = findAllOrganizations(credentials);
    if (organizations.isEmpty()) {
      System.out.println("There is no organization available to which you can " +
        "still make the device available. You are not yet a member of an " +
        "organization.");
      return organizations;
    }
    System.out.println("Organizations (" + organizations.size() + ")");
    for (var i = 0; i < organizations.size(); i++) {
      var organizationName = organizations.get(i).get("name");
      System.out.println(" " + (i + 1) + " " + organizationName);
    }
    return organizations;
  }

  private List<Map<String, String>> displayAvailableAddableTeams(
    CredentialConfiguration credentials, String organization
  ) throws Exception {
    var teams = findAvailableTeams(credentials, organization);
    if (teams.isEmpty()) {
      System.out.println("There is no team available to which you can " +
        "still make the device available. You have already integrated " +
        "all teams of this organization.");
      return teams;
    }
    System.out.println("Teams (" + teams.size() + ")");
    for (var i = 0; i < teams.size(); i++) {
      var teamName = teams.get(i).get("name");
      System.out.println(" " + (i + 1) + " " + teamName);
    }
    return teams;
  }

  private List<Map<String, String>> findAvailableTeams(
    CredentialConfiguration credentials, String organization
  ) throws Exception {
    var deviceUsers = findDeviceUsers(credentials);
    var teams = findOrganizationTeams(credentials, organization);
    teams = teams.stream()
      .filter(team -> deviceUsers.stream().noneMatch(deviceUser ->
        team.get("id").equals(deviceUser.get("teamId"))))
      .collect(Collectors.toList());
    return teams;
  }

  private static final String ORGANIZATION_ADD_URL =
    "https://api.taskwolf.net/v1/device/organization/add/";

  private void addOrganization(String organizationId, String teamId) throws Exception {
    var credentials = CredentialConfiguration.createAndLoad();
    var requestBody = Map.of("device", credentials.device(),
      "organization", organizationId, "team", teamId);
    TaskwolfRequest.create(ORGANIZATION_ADD_URL, "POST",
      new JSONObject(requestBody)).sendAuthorized(credentials.token());
  }

  private boolean removeOrganization(CredentialConfiguration credentials) throws Exception {
    var users = findDeviceUsers(credentials);
    if (users.isEmpty()) {
      System.out.println("The device has not yet been released for any organization.");
      return true;
    }
    var organizations = displayAvailableRemovableOrganizations(users);
    var organization = entrySelectionProcess(organizations, "Organization",
      "Now enter the number of the organization you would like to remove.");
    if (organization.isEmpty()) {
      return true;
    }
    System.out.println();
    var teams = displayAvailableRemovableTeams(users, organization.get());
    var team = entrySelectionProcess(teams, "Team",
      "Now enter the number of the team you would like to remove.");
    if (team.isEmpty()) {
      return true;
    }
    removeOrganization(credentials, organization.get(), team.get());
    System.out.println("The removal was successful. The device is no " +
      "longer available to the organization.");
    return true;
  }

  private List<Map<String, String>> displayAvailableRemovableOrganizations(
    List<Map<String, String>> deviceUsers
  )  {
    var organizations = deviceUsers.stream().map(user ->
      (Map<String, String>) Maps.newHashMap(user)).collect(Collectors.toList());
    for (var organization : organizations) {
      organization.remove("teamId");
      organization.remove("teamName");
      organization.put("id", organization.get("organizationId"));
    }
    organizations = organizations.stream().distinct().collect(Collectors.toList());
    System.out.println("Organizations (" + organizations.size() + ")");
    for (var i = 0; i < organizations.size(); i++) {
      var organizationName = organizations.get(i).get("organizationName");
      System.out.println(" " + (i + 1) + " " + organizationName);
    }
    return organizations;
  }

  private List<Map<String, String>> displayAvailableRemovableTeams(
    List<Map<String, String>> deviceUsers, String organization
  )  {
    var teams = deviceUsers.stream()
      .filter(user -> user.get("organizationId").equals(organization))
      .collect(Collectors.toList());
    System.out.println("Teams (" + teams.size() + ")");
    for (var i = 0; i < teams.size(); i++) {
      var team = teams.get(i);
      team.put("id", team.get("teamId"));
      var teamName = team.get("teamName");
      System.out.println(" " + (i + 1) + " " + teamName);
    }
    return teams;
  }

  private static final String ORGANIZATION_REMOVE_URL =
    "https://api.taskwolf.net/v1/device/organization/remove/";

  private void removeOrganization(
    CredentialConfiguration credentials, String organizationId, String teamId
  ) throws Exception {
    var requestBody = Map.of("device", credentials.device(),
      "organization", organizationId, "team", teamId);
    TaskwolfRequest.create(ORGANIZATION_REMOVE_URL, "POST",
      new JSONObject(requestBody)).sendAuthorized(credentials.token());
  }

  private Optional<String> entrySelectionProcess(
    List<Map<String, String>> entries, String name, String description
  ) {
    if (entries.isEmpty()) {
      return Optional.empty();
    }
    System.out.println();
    System.out.println(description);
    var entry = System.console().readLine(name + ": ");
    try {
      var entryIndex = Integer.parseInt(entry);
      if (entryIndex < 1 || entryIndex > entries.size()) {
        throw new Exception();
      }
      return Optional.of(entries.get(entryIndex - 1).get("id"));
    } catch (Exception exception) {
      System.out.println("An error has occurred. Please try again");
      return Optional.empty();
    }
  }

  private static final String DEVICE_USER_LIST_URL =
    "https://api.taskwolf.net/v1/device/users/";

  private List<Map<String, String>> findDeviceUsers(
    CredentialConfiguration credentials
  ) throws Exception {
    var response = TaskwolfRequest.create(DEVICE_USER_LIST_URL,
        "POST", new JSONObject(Map.of("device", credentials.device())))
      .sendAuthorized(credentials.token());
    return new JSONObject(response.body())
      .getJSONArray("users").toList().stream()
      .map(organization -> (Map<String, String>) organization)
      .collect(Collectors.toList());
  }

  private static final String ALL_ORGANIZATION_LIST_URL =
    "https://api.taskwolf.net/v1/organizations/all/";

  private List<Map<String, String>> findAllOrganizations(
    CredentialConfiguration credentials
  ) throws Exception {
    var response = TaskwolfRequest.create(ALL_ORGANIZATION_LIST_URL,
      "GET", new JSONObject("{}")).sendAuthorized(credentials.token());
    return new JSONObject(response.body())
      .getJSONArray("organizations").toList().stream()
      .map(organization -> (Map<String, String>) organization)
      .collect(Collectors.toList());
  }

  private static final String ORGANIZATION_TEAMS_URL =
    "https://api.taskwolf.net/v1/organization/team/targets/find/";

  private List<Map<String, String>> findOrganizationTeams(
    CredentialConfiguration credentials, String organization
  ) throws Exception {
    var response = TaskwolfRequest.create(ORGANIZATION_TEAMS_URL,
      "POST", new JSONObject(Map.of("organization", organization)))
      .sendAuthorized(credentials.token());
    var teams = new JSONObject(response.body())
      .getJSONArray("targets").toList().stream()
      .map(team -> (Map<String, String>) team)
      .collect(Collectors.toList());
    for (var team : teams) {
      if (team.get("type").equals("GLOBAL")) {
        team.put("id", organization);
      }
    }
    return teams;
  }
}
