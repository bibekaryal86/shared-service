package io.github.bibekaryal86.shdsvc.helpers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

public class OkHttpLoggingTest {

  private static class InMemoryAppender extends AppenderBase<ILoggingEvent> {
    private final List<ILoggingEvent> logs = new ArrayList<>();

    @Override
    protected void append(ILoggingEvent eventObject) {
      logs.add(eventObject);
    }

    public List<ILoggingEvent> getLogs() {
      return logs;
    }

    public void clear() {
      logs.clear();
    }
  }

  private InMemoryAppender appender;

  @BeforeEach
  public void setup() {
    Logger logger =
        (Logger) LoggerFactory.getLogger(io.github.bibekaryal86.shdsvc.helpers.OkHttpLogging.class);
    appender = new InMemoryAppender();
    appender.setContext(logger.getLoggerContext());
    logger.addAppender(appender);
    appender.start();
  }

  @Test
  public void testLoggingInterceptorLogsRequestAndResponse() throws IOException {
    Request request = new Request.Builder().url("https://example.com/test").get().build();

    Response response =
        new Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(ResponseBody.create("response body", MediaType.get("text/plain")))
            .build();

    Interceptor.Chain chain = mock(Interceptor.Chain.class);
    when(chain.request()).thenReturn(request);
    when(chain.proceed(request)).thenReturn(response);

    OkHttpLogging interceptor = new OkHttpLogging();
    Response intercepted = interceptor.intercept(chain);

    assertEquals(200, intercepted.code());

    List<ILoggingEvent> logs = appender.getLogs();
    assertTrue(logs.stream().anyMatch(e -> e.getFormattedMessage().contains("Request: [GET]")));
    assertTrue(logs.stream().anyMatch(e -> e.getFormattedMessage().contains("Response: [200]")));
  }
}
