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
    System.out.println("Taskwolf login");
    System.out.println();
    var email = System.console().readLine("Email: ");
    var password = new String(System.console().readPassword("Password: "));
    var verificationResult = verificationLogin(email, password, "");
    if (verificationResult.isEmpty()) {
      System.out.println("Verification failed. Either the email or password " +
        "entered was incorrect.");
      return true;
    }
    if (verificationResult.get().equals("2FA")) {
      multiFactorAuthorization(email, password);
      return true;
    }
    var responseBody = new JSONObject(verificationResult.get());
    deviceLogin(responseBody.getString("productApiKey"),
      responseBody.getString("refreshToken"));
    return true;
  }

  private void multiFactorAuthorization(
    String email, String password
  ) throws Exception {
    var multiFactorCode = System.console().readLine("Two factor code: ");
    var verificationResult = verificationLogin(email, password, multiFactorCode);
    if (verificationResult.isEmpty()) {
      System.out.println("Verification failed. Either the email or password " +
        "entered was incorrect.");
      return;
    }
    if (verificationResult.get().equals("2FA")) {
      System.out.println("Verification failed. The two factor code entered " +
        "was incorrect. Please try the login again.");
      return;
    }
    var responseBody = new JSONObject(verificationResult.get());
    deviceLogin(responseBody.getString("productApiKey"),
      responseBody.getString("refreshToken"));
  }

  private static final String VERIFICATION_LOGIN_URL =
    "https://api.taskwolf.net/v1/verification/login/";

  private Optional<String> verificationLogin(
    String email, String password, String multiFactorCode
  ) throws Exception {
    var requestBody = new JSONObject(Map.of("email", email, "password", password,
      "multiFactorCode", multiFactorCode.replaceAll(" ", "")));
    var response = TaskwolfRequest.create(VERIFICATION_LOGIN_URL, "POST", requestBody)
      .sendUnauthorized();
    var responseBody = new JSONObject(response.body());
    if (response.statusCode() != 200) {
      return responseBody.getInt("error") == 1002 ? Optional.of("2FA") :
        Optional.empty();
    }
    return Optional.of(response.body());
  }

  private static final String DEVICE_LOGIN_URL =
    "https://api.taskwolf.net/v1/device/login/";

  private void deviceLogin(String apiKey, String refreshToken) throws Exception {
    var localDeviceId = findLocalDeviceId();
    var response = TaskwolfRequest.create(DEVICE_LOGIN_URL, "POST",
      findDeviceInformation(localDeviceId)).sendAuthorized(apiKey);
    var decentralizedDeviceId = new JSONObject(response.body()).getString("id");
    finishLogin(apiKey, refreshToken, localDeviceId, decentralizedDeviceId);
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
    String apiKey, String refreshToken,
    String localDeviceId, String decentralizedDeviceId
  ) throws Exception {
    CredentialConfiguration.createAndStore(apiKey, refreshToken,
      decentralizedDeviceId);
    DeviceConfiguration.createAndStore(localDeviceId);
    Runtime.getRuntime().exec("systemctl daemon-reload");
    Runtime.getRuntime().exec("systemctl restart taskwolf.service");
    System.out.println("The verification process was successful.");
  }
}
