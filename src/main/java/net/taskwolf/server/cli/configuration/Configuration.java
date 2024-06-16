package net.taskwolf.server.cli.configuration;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;

@Accessors(fluent = true)
public abstract class Configuration {
  @Getter
  private final String path;

  protected Configuration(String path) {
    this.path = path;
  }

  /**
   * This function fills a configuration file with content
   * @return The JSON object that is to be saved
   */
  protected JSONObject serialize() {
    return new JSONObject();
  }

  /**
   * Is used to write the serialized content to the file
   * @throws Exception
   */
  public void save() throws Exception  {
    FileUtils.writeStringToFile(new File(absolutePath()), serialize().toString(),
      Charset.defaultCharset());
  }

  /**
   * This function builds the actual objects from the content of the
   * configuration file (json), which can be used later on
   * @param json
   */
  protected void deserialize(JSONObject json) {

  }

  /**
   * Used to load the json configuration file
   * @throws Exception
   */
  public void load() throws Exception  {
    deserialize(new JSONObject(FileUtils.readFileToString(
      new File(absolutePath()), Charset.defaultCharset())));
  }

  /**
   * Is used to create an empty configuration file
   */
  public void create() throws Exception {
    var file = new File(absolutePath());
    file.getParentFile().mkdirs();
    file.createNewFile();
  }

  /**
   * Is used to delete a configuration file
   */
  public void delete() throws Exception {
    var file = new File(absolutePath());
    file.delete();
  }

  /**
   * Checks whether configuration file exists
   * @return Is true if file could be found, otherwise false
   */
  public boolean exists() {
    return new File(absolutePath()).exists();
  }

  private String absolutePath() {
    return "/usr/local/taskwolf/" + path;
  }
}
