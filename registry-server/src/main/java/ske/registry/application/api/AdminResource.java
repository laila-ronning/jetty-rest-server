package ske.registry.application.api;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.annotations.Api;

import ske.registry.service.RegistryService;

@Produces({"application/json"})
@Path("/")
@Api(value = "/", description = "Oppslagstjenesten - admin")
public class AdminResource {

    private final RegistryService registryService;
    private final MetricRegistry metricRegistry;
    private static final ObjectMapper metricsMapper = new ObjectMapper().registerModule(
            new MetricsModule(TimeUnit.SECONDS, TimeUnit .MILLISECONDS, false));

    @Inject
    public AdminResource(RegistryService registryService, MetricRegistry metricRegistry) {
        this.registryService = registryService;
        this.metricRegistry = metricRegistry;
    }

    @GET
    @Path("admin/registrering")
    @Timed
    public Response registreringer() {
        return Response.ok(registryService.registreringer()).build();
    }

    @GET
    @Path("admin/registrering/aktiv")
    @Timed
    public Response aktiveRegistreringer() {
        return Response.ok(registryService.aktiveRegistreringer()).build();
    }

    @GET
    @Path("admin/registrering/inaktiv")
    @Timed
    public Response inaktiveRegistreringer() {
        return Response.ok(registryService.inaktiveRegistreringer()).build();
    }

    @GET
    @Path("admin/registrering/{tilbyderid}")
    @Timed
    public Response registrering(@PathParam("tilbyderid") String tilbyderid) {
        try {
            return Response.ok(registryService.registrering(UUID.fromString(tilbyderid))).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("admin/tjeneste/aktiv")
    @Timed
    public Response aktiveTjenester() {
        return Response.ok(registryService.aktiveTjenester()).build();
    }

    @GET
    @Path("admin/tjeneste/inaktiv")
    @Timed
    public Response inaktiveTjenester() {
        return Response.ok(registryService.inaktiveTjenester()).build();
    }

    @GET
    @Path("index.html")
    @Produces(MediaType.TEXT_HTML)
    public InputStream indexHtml() {
        return getClass().getClassLoader().getResourceAsStream("index.html");
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Response forsiden() {
        return Response.seeOther(URI.create("index.html")).build();
    }

    @GET
    @Path("admin/metrics")
    public Response metrics() throws JsonProcessingException {
        return Response.ok()
                .entity(metricsMapper.writeValueAsString(metricRegistry))
                .header("Cache-Control", "must-revalidate,no-cache,no-store")
                .build();
    }

}
