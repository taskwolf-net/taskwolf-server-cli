package net.taskwolf.server.cli.request;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.taskwolf.server.cli.credential.CredentialConfiguration;
import net.taskwolf.server.cli.whitelist.WhitelistConfiguration;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public final class TaskwolfRequest {
  private final String url;
  private final String method;
  private final JSONObject body;
  private final HttpClient httpClient = HttpClient.newHttpClient();

  public HttpResponse<String> sendUnauthorized() throws Exception {
    return send(Maps.newHashMap());
  }

  public HttpResponse<String> sendAuthorized(String token) throws Exception {
    return send(Map.of("Authorization", "Bearer " + token));
  }

  private HttpResponse<String> send(Map<String, String> headers) throws Exception {
    var requestBuilder = HttpRequest.newBuilder().uri(URI.create(url))
      .method(method, HttpRequest.BodyPublishers.ofString(body.toString()))
      .setHeader("Content-Type", "application/json");
    for (var header : headers.entrySet()) {
      requestBuilder.setHeader(header.getKey(), header.getValue());
    }
    var whitelistConfiguration = WhitelistConfiguration.createAndLoad();
    if (whitelistConfiguration.enabled()) {
      requestBuilder.setHeader("WHITELIST-KEY", whitelistConfiguration.token());
    }
    var response = httpClient.send(requestBuilder.build(),
      HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() == 417) {
      return refresh(headers);
    }
    return response;
  }

  private static final String REFRESH_URL =
    "https://api.taskwolf.net/v1/verification/refresh/";

  private HttpResponse<String> refresh(Map<String, String> headers) throws Exception {
    var credentials = CredentialConfiguration.createAndLoad();
    if (!credentials.exists()) {
      throw new Exception("Authentication refresh failed.");
    }
    var response = TaskwolfRequest.create(REFRESH_URL, "POST",
        new JSONObject(Map.of("refreshToken", credentials.refreshToken())))
      .sendUnauthorized();
    var responseBody = new JSONObject(response.body());
    if (!responseBody.getBoolean("success")) {
      throw new Exception("Authentication refresh failed.");
    }
    CredentialConfiguration.createAndStore(responseBody.getString("productApiKey"),
      responseBody.getString("refreshToken"), credentials.device());
    return send(headers);
  }
}
