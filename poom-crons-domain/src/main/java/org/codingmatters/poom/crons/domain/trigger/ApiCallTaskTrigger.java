package org.codingmatters.poom.crons.domain.trigger;

import com.fasterxml.jackson.core.JsonFactory;
import org.codingmatters.poom.crons.cronned.api.TaskEventTriggeredPostRequest;
import org.codingmatters.poom.crons.cronned.api.TaskEventTriggeredPostResponse;
import org.codingmatters.poom.crons.cronned.client.PoomCronnedClient;
import org.codingmatters.poom.crons.cronned.client.PoomCronnedRequesterClient;
import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.services.logging.CategorizedLogger;
import org.codingmatters.poom.services.support.date.UTC;
import org.codingmatters.rest.api.client.UrlProvider;
import org.codingmatters.rest.api.client.okhttp.HttpClientWrapper;
import org.codingmatters.rest.api.client.okhttp.OkHttpRequesterFactory;

import java.io.IOException;
import java.util.function.Function;

public class ApiCallTaskTrigger implements TaskTrigger {
    static private CategorizedLogger log = CategorizedLogger.getLogger(ApiCallTaskTrigger.class);

    private final Function<TaskSpec, PoomCronnedClient> clientProvider;

    public ApiCallTaskTrigger(Function<TaskSpec, PoomCronnedClient> clientProvider) {
        this.clientProvider = clientProvider;
    }

    public ApiCallTaskTrigger(HttpClientWrapper clientWrapper, JsonFactory jsonFactory) {
        this(spec -> {
            UrlProvider urlProvider = () -> spec.url();
            return new PoomCronnedRequesterClient(new OkHttpRequesterFactory(clientWrapper, urlProvider), jsonFactory, urlProvider);
        });
    }

    @Override
    public TriggerResult trig(TaskSpec spec) {
        PoomCronnedClient client = this.clientProvider.apply(spec);

        try {
            TaskEventTriggeredPostResponse response = client.taskEventTriggered().post(TaskEventTriggeredPostRequest.builder()
                    .poomCronnedAt(UTC.now())
                    .payload(spec.payload())
                    .build());

            if(response.opt().status204().isPresent()) {
                log.debug("triggered {}", spec);
                return new TriggerResult(true);
            } else if (response.opt().status410().isPresent()) {
                log.info("cronned service signaled as gone while triggering {}", spec);
                return new TriggerResult(false, true);
            } else {
                log.error("error while triggering {} : {}", spec, response);
                return new TriggerResult(false, false);
            }
        } catch (IOException e) {
            log.error("failed triggering with task spec : " + spec, e);
            return new TriggerResult(false, false);
        }
    }
}
