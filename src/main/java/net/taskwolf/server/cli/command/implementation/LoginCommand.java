package net.taskwolf.server.cli.command.implementation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.server.cli.command.Command;
import net.taskwolf.server.cli.credential.CredentialConfiguration;
import net.taskwolf.server.cli.device.DeviceConfiguration;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Singleton
public final class LoginCommand extends Command {
  private final HttpClient httpClient = HttpClient.newHttpClient();

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
    var requestBuilder = HttpRequest.newBuilder().uri(URI.create(VERIFICATION_LOGIN_URL))
      .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
      .setHeader("Content-Type", "application/json")
      .setHeader("WHITELIST-KEY", "tOrTylordsMeMBiTyLATONTERipLOusiNVOloRyPORKBOScoCk")
      .build();
    var response = httpClient.send(requestBuilder, HttpResponse.BodyHandlers.ofString());
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
    var deviceConfiguration = DeviceConfiguration.createAndLoad();
    var localDeviceId = deviceConfiguration.deviceId() == null ?
      UUID.randomUUID().toString() : deviceConfiguration.deviceId();
    var requestBody = new JSONObject(Map.of("device", localDeviceId, "information",
      InetAddress.getLocalHost().getHostName(), "platform", "LINUX"));
    var requestBuilder = HttpRequest.newBuilder().uri(URI.create(DEVICE_LOGIN_URL))
      .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
      .setHeader("Content-Type", "application/json")
      .setHeader("Authorization", "Bearer " + apiKey.get())
      .setHeader("WHITELIST-KEY", "tOrTylordsMeMBiTyLATONTERipLOusiNVOloRyPORKBOScoCk")
      .build();
    var response = httpClient.send(requestBuilder, HttpResponse.BodyHandlers.ofString());
    var decentralizedDeviceId = new JSONObject(response.body()).getString("id");
    finishLogin(apiKey.get(), localDeviceId, decentralizedDeviceId);
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
