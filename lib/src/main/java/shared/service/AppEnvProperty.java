package shared.service;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.service.dtos.Enums;
import shared.service.dtos.EnvDetailsResponse;
import shared.service.dtos.HttpResponse;
import shared.service.helpers.CommonUtilities;

public class AppEnvProperty {
  private static final Logger logger = LoggerFactory.getLogger(AppEnvProperty.class);

  private static List<EnvDetailsResponse.EnvDetails> ENV_DETAILS_LIST;
  private static Timer timer;
  public static final String APP_DETAILS_URL = "APP_DETAILS_URL";
  public static final String ENVSVC_USR = "ENVSVC_USR";
  public static final String ENVSVC_PWD = "ENVSVC_USR";
  public static final long REFRESH_INTERVAL = 5 * 60 * 1000; // every 5 minutes

  private static final String API_URL = CommonUtilities.getSystemEnvProperty(APP_DETAILS_URL);
  private static final String API_AUTH =
      CommonUtilities.getBasicAuth(
          CommonUtilities.getSystemEnvProperty(ENVSVC_USR),
          CommonUtilities.getSystemEnvProperty(ENVSVC_PWD));

  // Refresh properties periodically
  public static void refreshEnvDetailsList() {
    logger.info("Starting Routes Timer...");
    timer = new Timer();
    timer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            setEnvDetailsList();
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

  public static List<EnvDetailsResponse.EnvDetails> getEnvDetailsList() {
    if (CommonUtilities.isEmpty(ENV_DETAILS_LIST)) {
      setEnvDetailsList();
    }
    return ENV_DETAILS_LIST;
  }

  private static void setEnvDetailsList() {
    logger.debug("Retrieving Env Details...");
    HttpResponse<EnvDetailsResponse> response =
        Connector.sendRequest(
            API_URL,
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
