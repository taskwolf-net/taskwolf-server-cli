package net.taskwolf.server.cli.command.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.server.cli.command.Command;
import net.taskwolf.server.cli.credential.CredentialConfiguration;
import net.taskwolf.server.cli.device.DeviceConfiguration;
import net.taskwolf.server.cli.request.TaskwolfRequest;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Singleton
public final class LoginCommand extends Command {
  @Inject
  private LoginCommand() {
    super("login", new String[0], new String[0]);
  }

  @Override
  public boolean execute(String[] arguments) throws Exception {
    var console = System.console();
    System.out.println("Taskwolf login");
    System.out.println();
    var email = console.readLine("Email: ");
    var password = new String(console.readPassword("Password: "));
    deviceLogin(verificationLogin(email, password));
    return true;
  }

  private static final String VERIFICATION_LOGIN_URL =
    "https://api.taskwolf.net/v1/verification/login/";

  private Optional<String> verificationLogin(
    String email, String password
  ) throws Exception {
    var requestBody = new JSONObject(Map.of("email", email, "password", password));
    var response = TaskwolfRequest.create(VERIFICATION_LOGIN_URL, "POST", requestBody)
      .sendUnauthorized();
    if (response.statusCode() != 200) {
      return Optional.empty();
    }
    return Optional.of(new JSONObject(response.body()).getString("apiKey"));
  }

  private static final String DEVICE_LOGIN_URL =
    "https://api.taskwolf.net/v1/device/login/";

  private void deviceLogin(Optional<String> apiKey) throws Exception {
    if (apiKey.isEmpty()) {
      System.out.println("Verification failed. Either the email or password " +
        "entered was incorrect.");
      return;
    }
    var localDeviceId = findLocalDeviceId();
    var response = TaskwolfRequest.create(DEVICE_LOGIN_URL, "POST",
      findDeviceInformation(localDeviceId)).sendAuthorized(apiKey.get());
    var decentralizedDeviceId = new JSONObject(response.body()).getString("id");
    finishLogin(apiKey.get(), localDeviceId, decentralizedDeviceId);
  }

  private JSONObject findDeviceInformation(String localDeviceId) throws Exception {
    return new JSONObject(Map.of("device", localDeviceId, "information",
      InetAddress.getLocalHost().getHostName(), "platform", "LINUX"));
  }

  private String findLocalDeviceId() throws Exception {
    var deviceConfiguration = DeviceConfiguration.createAndLoad();
    return deviceConfiguration.deviceId() == null ?
      UUID.randomUUID().toString() : deviceConfiguration.deviceId();
  }

  private void finishLogin(
    String apiKey, String localDeviceId, String decentralizedDeviceId
  ) throws Exception {
    CredentialConfiguration.createAndStore(apiKey, decentralizedDeviceId);
    DeviceConfiguration.createAndStore(localDeviceId);
    Runtime.getRuntime().exec("systemctl daemon-reload");
    Runtime.getRuntime().exec("systemctl restart taskwolf.service");
    System.out.println("The verification process was successful.");
  }
}
