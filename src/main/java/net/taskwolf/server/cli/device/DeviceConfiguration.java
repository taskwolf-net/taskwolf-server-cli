package net.taskwolf.server.cli.device;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.server.service.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class DeviceConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "device/device.json";

  public static DeviceConfiguration createAndLoad() throws Exception {
    var configuration = new DeviceConfiguration(CONFIGURATION_PATH);
    if (!configuration.exists()) {
      return configuration;
    }
    configuration.load();
    return configuration;
  }

  public static DeviceConfiguration createAndStore(String deviceId) throws Exception {
    var configuration = new DeviceConfiguration(CONFIGURATION_PATH, deviceId);
    if (!configuration.exists()) {
      configuration.create();
    }
    configuration.save();
    return configuration;
  }

  private String deviceId;

  private DeviceConfiguration(String path) {
    super(path);
  }

  private DeviceConfiguration(String path, String deviceId) {
    super(path);
    this.deviceId = deviceId;
  }

  @Override
  protected JSONObject serialize() {
    var content = new JSONObject();
    content.put("device", deviceId);
    return content;
  }

  @Override
  protected void deserialize(JSONObject json) {
    deviceId = json.getString("device");
  }
}
