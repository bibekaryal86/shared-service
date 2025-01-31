package io.github.bibekaryal86.shdsvc;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.bibekaryal86.shdsvc.dtos.Enums;
import io.github.bibekaryal86.shdsvc.dtos.EnvDetailsResponse;
import io.github.bibekaryal86.shdsvc.dtos.HttpResponse;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppEnvProperty {
  private static final Logger logger = LoggerFactory.getLogger(AppEnvProperty.class);

  private static List<EnvDetailsResponse.EnvDetails> ENV_DETAILS_LIST = new ArrayList<>();
  private static Timer timer;
  public static final String ENVSVC_BASE_URL = "ENVSVC_BASE_URL";
  public static final String ENVSVC_USR = "ENVSVC_USR";
  public static final String ENVSVC_PWD = "ENVSVC_PWD";
  public static final long REFRESH_INTERVAL = 5 * 60 * 1000; // every 5 minutes

  private static final String API_URL_BASE = CommonUtilities.getSystemEnvProperty(ENVSVC_BASE_URL);
  private static final String API_AUTH =
      CommonUtilities.getBasicAuth(
          CommonUtilities.getSystemEnvProperty(ENVSVC_USR),
          CommonUtilities.getSystemEnvProperty(ENVSVC_PWD));

  // Refresh properties periodically
  public static void refreshEnvDetailsList(final String appName) {
    logger.info("Starting Routes Timer...");
    timer = new Timer();
    timer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            setEnvDetailsList(appName);
          }
        },
        0,
        REFRESH_INTERVAL);

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  logger.debug("Stopping Routes Timer...");
                  if (timer != null) {
                    timer.cancel();
                    timer.purge();
                    timer = null;
                  }
                }));
  }

  @Deprecated(since = "0.1.2")
  public static List<EnvDetailsResponse.EnvDetails> getEnvDetailsList(final String appName) {
    if (CommonUtilities.isEmpty(ENV_DETAILS_LIST)) {
      setEnvDetailsList(appName);
    }
    return ENV_DETAILS_LIST;
  }

  public static List<EnvDetailsResponse.EnvDetails> getEnvDetailsList(final String appName, final boolean isRefreshNow) {
    if (isRefreshNow || CommonUtilities.isEmpty(ENV_DETAILS_LIST)) {
      setEnvDetailsList(appName);
    }
    return ENV_DETAILS_LIST;
  }

  private static void setEnvDetailsList(final String appName) {
    logger.debug("Retrieving Env Details...");
    final String url = API_URL_BASE + "/api/v1/" + appName;
    HttpResponse<EnvDetailsResponse> response =
        Connector.sendRequest(
            url,
            Enums.HttpMethod.GET,
            new TypeReference<EnvDetailsResponse>() {},
            API_AUTH,
            Collections.emptyMap(),
            null);

    if (response.statusCode() == 200) {
      final EnvDetailsResponse envDetailsResponse = response.responseBody();
      if (CommonUtilities.isEmpty(envDetailsResponse.getErrMsg())) {
        ENV_DETAILS_LIST = envDetailsResponse.getEnvDetails();
      } else {
        logger.error(
            "Failed to Fetch Env Details, Error Response: [{}]", envDetailsResponse.getErrMsg());
      }
    } else {
      logger.error("Failed to Fetch Env Details, Response: [{}]", response.statusCode());
    }
  }
}
