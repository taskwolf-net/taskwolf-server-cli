package com.dulno.server.cli.whitelist;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.server.cli.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class WhitelistConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "whitelist/whitelist.json";

  public static WhitelistConfiguration createAndLoad() throws Exception {
    var configuration = new WhitelistConfiguration(CONFIGURATION_PATH);
    if (!configuration.exists()) {
      return configuration;
    }
    configuration.load();
    return configuration;
  }

  private boolean enabled;
  private String token;

  private WhitelistConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    enabled = json.getBoolean("enabled");
    token = json.getString("token");
  }
}
