package net.taskwolf.server.cli.credential;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.server.cli.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class CredentialConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "credential/credential.json";

  public static CredentialConfiguration createAndLoad() throws Exception {
    var configuration = new CredentialConfiguration(CONFIGURATION_PATH);
    if (!configuration.exists()) {
      return configuration;
    }
    configuration.load();
    return configuration;
  }

  public static CredentialConfiguration createAndStore(
    String token, String refreshToken, String device
  ) throws Exception {
    var configuration = new CredentialConfiguration(CONFIGURATION_PATH, token,
      refreshToken, device);
    if (!configuration.exists()) {
      configuration.create();
    }
    configuration.save();
    return configuration;
  }

  private String token;
  private String refreshToken;
  private String device;

  private CredentialConfiguration(String path) {
    super(path);
  }

  private CredentialConfiguration(
    String path, String token, String refreshToken, String device
  ) {
    super(path);
    this.token = token;
    this.refreshToken = refreshToken;
    this.device = device;
  }

  @Override
  protected JSONObject serialize() {
    var content = new JSONObject();
    content.put("token", token);
    content.put("refreshToken", refreshToken);
    content.put("device", device);
    return content;
  }

  @Override
  protected void deserialize(JSONObject json) {
    token = json.getString("token");
    refreshToken = json.getString("refreshToken");
    device = json.getString("device");
  }
}