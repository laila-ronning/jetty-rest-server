package ske.registry.application.api;

import static ske.registry.server.RegistryServer.URN_SKATTEETATEN_DRIFT_STS_REST_V2_UTSTED_SAML;
import static ske.registry.server.RegistryServer.URN_SKATTEETATEN_DRIFT_STS_REST_V2_VALIDER_SAML;
import static ske.registry.util.JsonHelper.tilJson;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ske.registry.dto.RegistreringDTO;
import ske.registry.dto.TjenesteDTO;
import ske.registry.dto.admin.KlientInfoDTO;
import ske.registry.dto.admin.PulsDTO;
import ske.registry.dto.admin.TimestampetRegistreringDTO;
import ske.registry.service.RegistryService;
import ske.registry.util.LoggerHjelper;

@Produces({"application/json"})
@Path("/tjeneste")
@Api(value = "/tjeneste", description = "Oppslagstjenesten - tjeneste")
public class TjenesteResource {

    private static final Logger logger = LoggerFactory.getLogger(TjenesteResource.class);
    private final RegistryService registryService;

    @Inject
    public TjenesteResource(RegistryService registryService) {
        this.registryService = registryService;
    }

    @GET
    @Path("/{urn}")
    @Timed
    @ApiOperation(value = "Henter ut aktive tjenester", response = TjenesteDTO.class, responseContainer = "Collection")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Én TjenesteDTO per registrering for gitt URN der tilbyderen har sendt puls innenfor tidsintervallet for aktivitet."),
            @ApiResponse(code = 404, message = "Ingen registrerte tjenester for URN, eller ingen tilbydere av tjenesten som har sendt puls innenfor tidsintervallet for aktivitet. Klienter skal fjerne eventuelle cachede URIer for denne URN."),
            @ApiResponse(code = 503, message = "Ingen registrerte tjenester for URN, men er innenfor grace periode (server nettopp (re)startet). Klienter kan beholde evt. cachede URIer for denne URN.")
    })
    public Response aktiveTjenester(@PathParam("urn") String urn, @Context HttpServletRequest servletRequest) {
        if (logger.isDebugEnabled()) {
            logger.debug(tilJson(
                    "beskrivelse", "Oppslag etter aktive tjeneser",
                    "path", "/tjeneste/{urn}",
                    "host", servletRequest.getRemoteHost(),
                    "urn", urn
            ));
        }

        Collection<TjenesteDTO> aktiveTjenesterMedURN = registryService.aktiveTjenesterMedURN(urn);

        if (aktiveTjenesterMedURN.isEmpty()) {
            if (registryService.isInnenforGracePeriode()) {
                return Response.status(Status.SERVICE_UNAVAILABLE).entity("Server er nettopp startet, venter på registreringer").build();
            } else {
                return Response.status(Status.NOT_FOUND).build();
            }
        } else if (aktiveTjenesterMedURN.size() == 1) {
            return Response.ok(aktiveTjenesterMedURN).build();
        } else {
            // Fjernet prioritering av lokal uri etter ønske fra miljø som ønsker å teste med helt tilfeldig valg av alternativ, se
            // svn history for implementasjon med prioritering
            List<TjenesteDTO> resultat = stokkAlle(aktiveTjenesterMedURN);
            return Response.ok(resultat).build();
        }
    }

    @GET
    @Path("/{urn}/tjenesteattributter")
    @Timed
    @ApiOperation(value = "Henter ut tjenesteattributter")
    @ApiResponses({@ApiResponse(code = 200, message = "")})
    public Response hentTjenesteattributter(@PathParam("urn") String urn, @Context HttpServletRequest servletRequest) {
        if (logger.isDebugEnabled()) {
            logger.debug(tilJson(
                    "beskrivelse", "Uthenting av tjenesteattributter",
                    "path", "/tjeneste/{urn}/tjenesteattributter",
                    "host", servletRequest.getRemoteHost(),
                    "urn", urn
            ));
        }

        Map<String, String> tjenesteattributter = registryService.finnEgendefinertInfoForTjeneste(urn);

        return Response.ok().entity(tjenesteattributter).build();
    }

    @GET
    @Path("/liste")
    @Timed
    @ApiOperation(value = "Henter ut aktive tjenester")
    @ApiResponses({
            @ApiResponse(code = 200, message = ""),
            @ApiResponse(code = 503, message = "Innenfor grace periode")
    })
    public Response aktiveTjenesterForFlereUrn(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        Map<String, Collection<TjenesteDTO>> aktiveTjenesterMedURN = new HashMap<>();
        List<String> urnListe = queryParameters.get("urnListe");

        if (logger.isDebugEnabled()) {
            logger.debug(tilJson(
                    "beskrivelse", "Oppslag etter aktive tjeneser",
                    "path", "/tjeneste/liste",
                    "host", servletRequest.getRemoteHost(),
                    "urner", Joiner.on(", ").join(urnListe)
            ));
        }

        for (String urn : urnListe) {
            Collection<TjenesteDTO> tjenesterForUrn = registryService.aktiveTjenesterMedURN(urn);
            if (tjenesterForUrn.isEmpty()) {
                continue;
            }
            if (tjenesterForUrn.size() > 1) {
                tjenesterForUrn = stokkAlle(tjenesterForUrn);
            }
            aktiveTjenesterMedURN.put(urn, tjenesterForUrn);
        }

        if (urnListe.size() != aktiveTjenesterMedURN.keySet().size() && registryService.isInnenforGracePeriode()) {
            Sets.difference(Sets.newHashSet(urnListe), aktiveTjenesterMedURN.keySet());
            return Response.status(Status.SERVICE_UNAVAILABLE).entity("Server er nettopp startet, venter på registreringer").build();

        }
        return Response.ok(aktiveTjenesterMedURN).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    @Timed
    @ApiOperation(value = "Legg inn tjeneste")
    @ApiResponses({
            @ApiResponse(code = 201, message = ""),
            @ApiResponse(code = 403, message = "Ulovlig registrering")
    })
    public Response leggInnTjeneste(RegistreringDTO registrering, @Context HttpServletRequest servletRequest) {
        registrering.setAdminsideUrl(uriSettFraServer(registrering.getAdminsideUrl(), servletRequest));
        registrering.setHelsesjekkUrl(uriSettFraServer(registrering.getHelsesjekkUrl(), servletRequest));
        registrering.setPingUrl(uriSettFraServer(registrering.getPingUrl(), servletRequest));

        for (TjenesteDTO tjeneste : registrering.getTjenesteliste()) {
            if (erTjenesteUlovligAaRegistrere(tjeneste)) {
                return Response.status(Status.FORBIDDEN).entity("Ikke lov å registrere tjeneste med urn: " + tjeneste.getUrn()).build();
            }
            if (tjeneste.getKlientUri() == null) {
                tjeneste.setKlientUri(uriSettFraServer(tjeneste.getUri(), servletRequest));
            }
        }

        registryService.registrerTjenester(registrering);

        logger.info(tilJson(
                "beskrivelse", "Ny tjeneste",
                "path", "/tjeneste",
                "host", servletRequest.getRemoteHost(),
                "tilbyder", registrering.getTilbyderNavn(),
                "urn", LoggerHjelper.hentUrn(registrering),
                "uri", LoggerHjelper.hentUri(registrering)
        ));

        return Response.created(URI.create(registrering.getTilbyderId().toString())).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/klientinfo")
    @Timed
    @ApiOperation(value = "Oppdater klientinfo", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = ""),
            @ApiResponse(code = 400, message = "")
    })
    public Response oppdaterInfo(KlientInfoDTO klientinfo) {
        try {
            registryService.oppdaterKlientInfo(klientinfo.getTilbyderId(), klientinfo);

            if (logger.isDebugEnabled()) {
                TimestampetRegistreringDTO registrering = registryService.registrering(klientinfo.getTilbyderId());
                logger.debug(tilJson(
                        "beskrivelse", "Oppdatering av klientinfo",
                        "path", "/tjeneste/klientinfo",
                        "tilbyder", LoggerHjelper.hentTilbydernavn(registrering),
                        "tilbyderId", klientinfo.getTilbyderId().toString()
                ));
            }

            return Response.created(URI.create(klientinfo.getTilbyderId().toString())).build();
        } catch (RuntimeException e) {
            return Response.status(Status.BAD_REQUEST).entity(e).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/puls")
    @Timed
    @ApiOperation(value = "Send puls", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = ""),
            @ApiResponse(code = 400, message = "")
    })
    public Response puls(PulsDTO puls) {
        try {
            registryService.oppdaterTimestamp(puls.getTilbyderId());

            if (logger.isDebugEnabled()) {
                TimestampetRegistreringDTO registrering = registryService.registrering(puls.getTilbyderId());
                logger.debug(tilJson(
                        "beskrivelse", "Mottok puls",
                        "path", "/tjeneste/puls",
                        "tilbyder", LoggerHjelper.hentTilbydernavn(registrering),
                        "tilbyderId", puls.getTilbyderId().toString()
                ));
            }

            return Response.created(URI.create(puls.getTilbyderId().toString())).build();
        } catch (RuntimeException e) {
            return Response.status(Status.BAD_REQUEST).entity(e).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/stopp")
    @Timed
    @ApiOperation(value = "Stopp tjenester", response = String.class)
    @ApiResponses({@ApiResponse(code = 201, message = "")})
    public Response stopp(PulsDTO puls) {
        TimestampetRegistreringDTO registrering = registryService.registrering(puls.getTilbyderId());
        logger.info(tilJson(
                "beskrivelse", "Fikk stopp-melding",
                "path", "/tjeneste/stopp",
                "tilbyder", LoggerHjelper.hentTilbydernavn(registrering),
                "tilbyderId", puls.getTilbyderId().toString()
        ));

        registryService.fjernAlleFraTilbyder(puls.getTilbyderId());
        return Response.created(URI.create(puls.getTilbyderId().toString())).build();
    }

    private boolean erTjenesteUlovligAaRegistrere(TjenesteDTO tjeneste) {
        return tjeneste.getUrn().equals(URN_SKATTEETATEN_DRIFT_STS_REST_V2_UTSTED_SAML)
                || tjeneste.getUrn().equals(URN_SKATTEETATEN_DRIFT_STS_REST_V2_VALIDER_SAML);
    }

    private URI uriSettFraServer(URI uri, HttpServletRequest servletRequest) {
        if (uri == null) {
            return null;
        }
        return UriBuilder.fromUri(uri).host(servletRequest.getRemoteHost()).port(uri.getPort()).build();
    }

    private List<TjenesteDTO> stokkAlle(Collection<TjenesteDTO> aktiveTjenesterMedURN) {
        List<TjenesteDTO> resultat = Lists.newArrayList(aktiveTjenesterMedURN);
        Collections.shuffle(resultat);
        return resultat;
    }

}
