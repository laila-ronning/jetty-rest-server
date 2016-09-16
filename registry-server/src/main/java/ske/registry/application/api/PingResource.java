package ske.registry.application.api;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import mag.felles.jvm.HotThreads;
import mag.felles.jvm.TimeValue;

@Produces({"application/json"})
@Path("/ping")
@Api(value = "/ping", description = "Oppslagstjenesten - ping")
public class PingResource {

    @GET
    @ApiOperation(value = "Ping pong", response = String.class, httpMethod = "GET")
    @ApiResponses({@ApiResponse(code = 200, message = "Returnerer stringen 'pong'")})
    @Timed
    public Response getLatest() {
        return Response.ok("pong").build();
    }

    @GET
    @Path("/hot_threads")
    @Produces("text/plain")
    @ApiOperation(value = "Henter hot threads informasjon i tekstlig format", response = String.class)
    public String getHotThreads(
            @ApiParam(value = "Antall tråder som skal vises", defaultValue = "3")
            @QueryParam("threads")
            @DefaultValue("3")
            Integer threads,

            @ApiParam(value = "Samplingstid", defaultValue = "500ms")
            @QueryParam("interval")
            @DefaultValue("500ms")
            String interval,

            @ApiParam(value = "Tråtilstand som ønskes samples", defaultValue = "cpu", allowableValues = "cpu, wait, block")
            @QueryParam("type")
            @DefaultValue("cpu")
            String type,

            @ApiParam(value = "Antall samples", defaultValue = "10")
            @QueryParam("snapshots")
            @DefaultValue("10")
            Integer snapshots
    ) throws Exception {
        HotThreads hotThreads = new HotThreads()
                .busiestThreads(threads)
                .type(type)
                .interval(TimeValue.parseTimeValue(interval, TimeValue.timeValueMillis(500)))
                .threadElementsSnapshotCount(snapshots);

        return hotThreads.detect();
    }

}
