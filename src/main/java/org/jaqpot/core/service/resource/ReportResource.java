/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it (web applications and beans)
 * are licensed by GPL v3 as specified hereafter. Additional components may ship
 * with some other licence as will be specified therein.
 *
 * Copyright (C) 2014-2015 KinkyDesign (Charalampos Chomenidis, Pantelis Sopasakis)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Source code:
 * The source code of JAQPOT Quattro is available on github at:
 * https://github.com/KinkyDesign/JaqpotQuattro
 * All source files of JAQPOT Quattro that are stored on github are licensed
 * with the aforementioned licence. 
 */
package org.jaqpot.core.service.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatchException;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import io.swagger.annotations.ApiResponse;
//import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
//import io.swagger.jaxrs.PATCH;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.data.ReportHandler;
import org.jaqpot.core.data.serialize.JSONSerializer;
import org.jaqpot.core.model.MetaInfo;
import org.jaqpot.core.model.Report;
import org.jaqpot.core.model.factory.ErrorReportFactory;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.RoleEnum;
import org.jaqpot.core.service.data.ReportService;
import org.jaqpot.core.service.exceptions.JaqpotForbiddenException;

/**
 *
 * @author Charalampos Chomenidis
 * @author Pantelis Sopasakis
 */
@Path("/report")
//@Api(value = "/report", description = "Report API")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "report")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class ReportResource {

    private static final String DEFAULT_PATCH = "[\n"
            + "  {\n"
            + "    \"op\": \"add\",\n"
            + "    \"path\": \"/key\",\n"
            + "    \"value\": \"foo\"\n "
            + "  }\n"
            + "]";

    @Context
    SecurityContext securityContext;

    @EJB
    ReportHandler reportHandler;

    @Inject
    ReportService reportService;

    @Inject
    @Jackson
    JSONSerializer serializer;

    @GET
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Operation(summary = "Retrieves Reports of User")
    public Response getReports(
            //@ApiParam(value = "Authorization token") @HeaderParam("Authorization") String api_key,
            @Parameter(name = "Authorization", description = "Authorization token") @HeaderParam("Authorization") String api_key,
            @Parameter(name = "start", description = "start", schema = @Schema(implementation = Integer.class, defaultValue = "0")) @QueryParam("start") Integer start,
            @Parameter(name = "max", description = "max - the server imposes an upper limit of 500 on this "
                + "parameter.", schema = @Schema(implementation = Integer.class, defaultValue = "10")) @QueryParam("max") Integer max
    ) {
        if (max == null || max > 500) {
            max = 500;
        }
        String userName = securityContext.getUserPrincipal().getName();
        return Response.ok(reportHandler.listMetaOfCreator(userName, start != null ? start : 0, max))
                .header("total", reportHandler.countAllOfCreator(userName))
                .build();

    }

    @GET
    @Path("/{id}")
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Operation(summary = "Retrieves Report by id")
    public Response getReport(
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @Parameter(name = "id", description = "id", schema = @Schema(implementation = String.class)) @PathParam("id") String id) {

        Report report = reportHandler.find(id);
        if (report == null) {
            throw new NotFoundException();
        }
        return Response.ok(reportHandler.find(id)).build();
    }

    @DELETE
    @Path("/{id}")
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Operation(description = "Removes Report by id")
    public Response removeReport(
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @PathParam("id") String id
    ) throws JaqpotForbiddenException {
        Report report = reportHandler.find(id);
        if (report == null) {
            throw new NotFoundException();
        }

        MetaInfo metaInfo = report.getMeta();
        if (metaInfo.getLocked()) {
            throw new JaqpotForbiddenException("You cannot delete a Report that is locked.");
        }

        String userName = securityContext.getUserPrincipal().getName();
        if (!report.getMeta().getCreators().contains(userName)) {
            throw new ForbiddenException("You cannot delete a Report that was not created by you.");
        }
        reportHandler.remove(report);
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/pdf")
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces("application/json; charset=UTF-8")
    @Operation(summary = "Creates PDF from report")
    public Response createPDF(
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class), in = ParameterIn.HEADER) @HeaderParam("Authorization") String api_key,
            @PathParam("id") String id) {
        Report report = reportHandler.find(id);
        if (report == null) {
            throw new NotFoundException();
        }

        StreamingOutput out = (OutputStream output) -> {
            BufferedOutputStream buffer = new BufferedOutputStream(output);
            try {
                reportService.report2PDF(report, output);
            } finally {
                buffer.flush();
            }
        };
        return Response.ok(out)
                .header("Content-Disposition", "attachment; filename=" + "report-" + report.getId() + ".pdf")
                .build();
    }

    //@PATCH
    @PUT
    @Path("/{id}")
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    //@Consumes("application/json-patch+json")
    @Consumes({MediaType.APPLICATION_JSON})
    @Operation(summary = "Modifies a particular Report resource",
            description = "Modifies (applies a patch on) a Report resource of a given ID."
            + "This implementation of PATCH follows the RFC 6902 proposed standard. "
            + "See https://tools.ietf.org/rfc/rfc6902.txt for details.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Report entry was modified successfully."),
                @ApiResponse(responseCode = "404", description = "No such Report - the patch will not be applied"),
                @ApiResponse(responseCode = "401", description = "You are not authorized to access this resource"),
                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)"),
                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
            })
    public Response modifyReport(
            //@ApiParam("Clients need to authenticate in order to create resources on the server") @HeaderParam("Authorization") String api_key,
            //@ApiParam(value = "ID of an existing Report.", required = true) @PathParam("id") String id,
            //@ApiParam(value = "The patch in JSON according to the RFC 6902 specs", required = true, defaultValue = DEFAULT_PATCH) String patch
            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to create resources on the server", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
            @Parameter(name = "id", description = "ID of an existing Report.", schema = @Schema(implementation = String.class)) @PathParam("id") String id,
            @Parameter(name = "patch", description = "The patch in JSON according to the RFC 6902 specs", required = true, schema = @Schema(implementation = String.class, defaultValue = DEFAULT_PATCH)) String patch
    ) throws JsonPatchException, JsonProcessingException {

        Report originalReport = reportHandler.find(id);
        if (originalReport == null) {
            throw new NotFoundException("Report " + id + " not found.");
        }

        Report modifiedReport = serializer.patch(originalReport, patch, Report.class);
        if (modifiedReport == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(ErrorReportFactory.badRequest("Patch cannot be applied because the request is malformed", "Bad patch"))
                    .build();
        }
//        ErrorReport validationError = BibTeXValidator.validate(modifiedAsBib);
//        if (validationError != null) {
//            return Response
//                    .ok(validationError)
//                    .status(Response.Status.BAD_REQUEST)
//                    .build();
//        }
        reportHandler.edit(modifiedReport); // update the entry in the DB

        return Response
                .ok(modifiedReport)
                .build();
    }

}
