package org.codingmatters.poom.crons.domain.trigger;

import com.fasterxml.jackson.core.JsonFactory;
import org.codingmatters.poom.crons.cronned.api.PoomCronnedHandlers;
import org.codingmatters.poom.crons.cronned.api.TaskEventTriggeredPostRequest;
import org.codingmatters.poom.crons.cronned.api.TaskEventTriggeredPostResponse;
import org.codingmatters.poom.crons.cronned.api.harness.PoomCronnedProcessor;
import org.codingmatters.poom.crons.cronned.api.taskeventtriggeredpostresponse.Status204;
import org.codingmatters.poom.crons.cronned.api.taskeventtriggeredpostresponse.Status410;
import org.codingmatters.poom.crons.cronned.api.taskeventtriggeredpostresponse.Status500;
import org.codingmatters.poom.crons.cronned.api.types.Error;
import org.codingmatters.poom.crons.cronned.client.PoomCronnedClient;
import org.codingmatters.poom.crons.cronned.client.PoomCronnedRequesterClient;
import org.codingmatters.poom.crons.crontab.api.types.TaskSpec;
import org.codingmatters.poom.crons.crontab.api.types.taskspec.Scheduled;
import org.codingmatters.rest.api.client.okhttp.OkHttpClientWrapper;
import org.codingmatters.rest.api.client.okhttp.OkHttpRequesterFactory;
import org.codingmatters.rest.undertow.CdmHttpUndertowHandler;
import org.codingmatters.rest.undertow.support.UndertowResource;
import org.codingmatters.value.objects.values.ObjectValue;
import org.codingmatters.value.objects.values.PropertyValue;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ApiCallTaskTriggerTest {

    @Rule
    public UndertowResource server = new UndertowResource(new CdmHttpUndertowHandler(new PoomCronnedProcessor("/cronned", new JsonFactory(), new PoomCronnedHandlers.Builder().taskEventTriggeredPostHandler(this::taskEventTriggered).build())));

    private AtomicReference<TaskEventTriggeredPostResponse> nextResponse = new AtomicReference<>();
    private AtomicReference<TaskEventTriggeredPostRequest> lastRequest = new AtomicReference<>();
    private ApiCallTaskTrigger trigger;

    private TaskEventTriggeredPostResponse taskEventTriggered(TaskEventTriggeredPostRequest request) {
        this.lastRequest.set(request);

        if(this.nextResponse.get() == null) throw new AssertionError("must set net response");
        return this.nextResponse.get();
    }

    @Before
    public void setUp() throws Exception {
        this.trigger = new ApiCallTaskTrigger(OkHttpClientWrapper.build(builder -> builder.connectTimeout(2, TimeUnit.SECONDS)), new JsonFactory());
    }

    @Test
    public void givenUrlIsOk__whenCronnedReturnsA204__thenResultIsSuccess() throws Exception {
        this.nextResponse.set(TaskEventTriggeredPostResponse.builder()
                .status204(Status204.builder().build())
                .build());

        assertThat(this.trigger.trig(TaskSpec.builder()
                .url(this.server.baseUrl() + "/cronned")
                .payload(ObjectValue.builder().property("hello", PropertyValue.builder().stringValue("world")).build())
                .build()),
                is(new TriggerResult(true, false))
        );
    }

    @Test
    public void givenUrlIsOk__whenCronnedReturnsA410__thenResultIsFailureAndCronnedIsGone() throws Exception {
        this.nextResponse.set(TaskEventTriggeredPostResponse.builder()
                .status410(Status410.builder().payload(Error.builder().build()).build())
                .build());

        assertThat(this.trigger.trig(TaskSpec.builder()
                .url(this.server.baseUrl() + "/cronned")
                .payload(ObjectValue.builder().property("hello", PropertyValue.builder().stringValue("world")).build())
                .build()),
                is(new TriggerResult(false, true))
        );
    }

    @Test
    public void givenUrlIsOk__whenCronnedReturnsA500__thenResultIsFailureAndCronnedIsNotGone() throws Exception {
        this.nextResponse.set(TaskEventTriggeredPostResponse.builder()
                .status500(Status500.builder().build())
                .build());
        assertThat(this.trigger.trig(TaskSpec.builder()
                .url(this.server.baseUrl() + "/cronned")
                .payload(ObjectValue.builder().property("hello", PropertyValue.builder().stringValue("world")).build())
                .build()),
                is(new TriggerResult(false, false))
        );
    }

    @Test
    public void givenUrlBroken__whenCronnedReturnsA500__thenResultIsFailureAndCronnedIsNotGone() throws Exception {
        this.nextResponse.set(TaskEventTriggeredPostResponse.builder()
                .status500(Status500.builder().build())
                .build());
        assertThat(this.trigger.trig(TaskSpec.builder()
                .url("http://no.where.on.earth.loc")
                .payload(ObjectValue.builder().property("hello", PropertyValue.builder().stringValue("world")).build())
                .build()),
                is(new TriggerResult(false, false))
        );
    }
}