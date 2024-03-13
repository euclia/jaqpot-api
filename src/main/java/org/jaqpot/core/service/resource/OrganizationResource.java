/*
 *
 * JAQPOT Quattro
 *
 * JAQPOT Quattro and the components shipped with it, in particular:
 * (i)   JaqpotCoreServices
 * (ii)  JaqpotAlgorithmServices
 * (iii) JaqpotDB
 * (iv)  JaqpotDomain
 * (v)   JaqpotEAR
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

//import io.swagger.v3.oas.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import io.swagger.annotations.ApiResponse;
//import io.swagger.annotations.ApiResponses;
//import io.swagger.annotations.Extension;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
//import io.swagger.annotations.ExtensionProperty;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.jaqpot.core.data.OrganizationHandler;
import org.jaqpot.core.data.UserHandler;
import org.jaqpot.core.model.ErrorReport;
import org.jaqpot.core.service.annotations.TokenSecured;
import org.jaqpot.core.service.authentication.AAService;
import org.jaqpot.core.service.authentication.RoleEnum;
import org.jaqpot.core.service.exceptions.JaqpotWebException;
import xyz.euclia.euclia.accounts.client.models.Organization;

/**
 *
 * @author pantelispanka
 */
@Path("/organization")
//@Api(value = "/organization", description = "Organization API", position = 1)
@Produces({"application/json", "text/uri-list"})
@Tag(name = "organization")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class OrganizationResource {

    @EJB
    OrganizationHandler orgHandler;

    @EJB
    UserHandler userHandler;

    @Context
    SecurityContext securityContext;

    @EJB
    AAService aaService;

//    @EJB
//    NotificationHandler notificationHandler;
//
//    @GET
//    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
//    @Operation(summary = "Finds all Organizations",
//            description = "Finds all Organizations on Jaqpot",
//            extensions = {
//                @Extension(properties = {
//            @ExtensionProperty(name = "orn-@type", value = "x-orn:Organization"),})
//                ,
//                @Extension(name = "orn:returns", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:OrganizationList")
//        })},
//            responses = {
//                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced.")
//                ,
//                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Organization.class))), description = "A list of algorithms in the Jaqpot framework")
//                ,
//                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
//            })
//    public Response getOrganizations(
//            @Parameter(name = "start", description = "start", schema = @Schema(implementation = Integer.class, defaultValue = "0")) @QueryParam("start") Integer start,
//            @Parameter(name = "max", description = "max", schema = @Schema(implementation = Integer.class, defaultValue = "10")) @QueryParam("max") Integer max) {
//        return Response
//                .ok(orgHandler.findAll(start != null ? start : 0, max != null ? max : Integer.MAX_VALUE))
//                .header("total", orgHandler.countAll())
//                .build();
//    }
//
//    @GET
//    @Path("/{id}/users")
//    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})

//    @Operation(
//            summary = "Finds all Organization users",
//            description = "Finds all Organization users",
//            extensions = {
//                @Extension(properties = {
//            @ExtensionProperty(name = "orn-@type", value = "x-orn:Organization"),}
//                )
//                ,
//                @Extension(name = "orn:returns", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:OrganizationList")
//        })
//            },
//            responses = {
//                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced.")
//                ,
//                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Organization.class))), description = "A list of algorithms in the Jaqpot framework")
//                ,
//                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
//            })
//    public Response getOrganizationUsers(
//            @PathParam("id") String id) {
//        List<String> fields = new ArrayList();
//        fields.add("userIds");
//        Organization orgUsers = orgHandler.find(id, fields);
//        return Response
//                .ok(orgUsers)
//                .build();
//    }
//
//    @POST
//    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Consumes({MediaType.APPLICATION_JSON})
//    @Operation(
//            summary = "Finds all Organizations",
//            description = "Finds all Organizations on Jaqpot",
//            extensions = {
//                @Extension(properties = {
//            @ExtensionProperty(name = "orn-@type", value = "x-orn:Organization"),}
//                )
//                ,
//                @Extension(name = "orn:returns", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:OrganizationList")
//        })
//            },
//            responses = {
//                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced.")
//                ,
//                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Organization.class))), description = "A list of algorithms in the Jaqpot framework")
//                ,
//                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
//            })
//    public Response createsOrganization(
//            @Parameter(description = "Authorization token") @HeaderParam("Authorization") String api_key,
//            @Parameter(name = "org", schema = @Schema(implementation = Organization.class)) Organization org) throws QuotaExceededException {
//        String userId = securityContext.getUserPrincipal().getName();
//        String[] apiA = api_key.split("\\s+");
//        String apiKey = apiA[1];
//        User user = userHandler.find(userId, apiKey);
//        Organization organizationToBe = OrganizationFactory.buildOrgFromId(org.getId());;
//        try {
//            organizationToBe.setContact(user.getEmail());
//            MetaInfo mf = MetaInfoBuilder.builder()
//                    .addAudiences()
//                    .addCreators(user.get_id())
//                    .addSubjects()
//                    .addDescriptions()
//                    .build();
//            organizationToBe.setMeta(mf);
//            List<String> users = new ArrayList();
//            users.add(userId);
//            organizationToBe.setUserIds(users);
//            orgHandler.create(organizationToBe);
//            user.getOrganizations().add(org.getId());
//        } catch (Exception e) {
//            throw new BadRequestException(e.getMessage());
//        }
//
//        return Response
//                .ok(organizationToBe)
//                .build();
//    }

    @GET
    @Path("/{id}")
    @TokenSecured({RoleEnum.DEFAULT_USER})
    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
    @Operation(
            summary = "Finds Organization",
            description = "Finds Organization on Jaqpot by id",
            extensions = {
                @Extension(properties = {
            @ExtensionProperty(name = "orn-@type", value = "x-orn:Organization"),}
                )
                ,
                @Extension(name = "orn:returns", properties = {
            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Organization")
        })
            },
            responses = {
                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced.")
                ,
                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Organization.class))), description = "A list of algorithms in the Jaqpot framework")
                ,
                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
            })
    public Response getOrganizationById(
            @PathParam("id") String id,
            @Parameter(name = "Authorization", description = "Authorization token", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) throws JaqpotWebException {
        String[] apiA = api_key.split("\\s+");
        String apiKey = apiA[1];
        return Response
                .ok(orgHandler.find(id, apiKey))
                .build();
    }

//    @PUT
//    @Path("/{id}")
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
//    @ApiOperation(
//            value = "Updates Organization",
//            notes = "Updates Organization on Jaqpot by id",
//            extensions = {
//                @Extension(properties = {
//            @ExtensionProperty(name = "orn-@type", value = "x-orn:Organization"),}
//                )
//                ,
//                @Extension(name = "orn:returns", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Organization")
//        })
//            }
//    )
//    @ApiResponses(value = {
//        @ApiResponse(code = 401, response = ErrorReport.class, message = "Wrong, missing or insufficient credentials. Error report is produced.")
//        ,
//            @ApiResponse(code = 200, response = Organization.class, responseContainer = "List", message = "A list of algorithms in the Jaqpot framework")
//        ,
//            @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
//
//    })
//    public Response updateOrganizationById(
//            @PathParam("id") String id,
//            @ApiParam(value = "Clients need to authenticate in order to access this resource")
//            @HeaderParam("Authorization") String api_key,
//            Organization orgForUpdate) throws JaqpotNotAuthorizedException {
//
//        String userId = securityContext.getUserPrincipal().getName();
//
//        String[] apiA = api_key.split("\\s+");
//        String apiKey = apiA[1];
//        if(orgForUpdate.getId().equals("Jaqpot") && aaService.isAdmin(apiKey)){
//            orgHandler.edit(orgForUpdate);
//        }
//
//        List<Notification> notifs = notificationHandler.getInvitationsToOrg(userId, orgForUpdate.getId());
//        if(notifs.size() > 0 || orgForUpdate.getMeta().getCreators().contains(userId)){
//            orgHandler.edit(orgForUpdate);
//        }
//        else{
//            throw new JaqpotNotAuthorizedException("You are not authorized to edit this Organization");
//        }
//
//        return Response
//                .ok(orgHandler.find(orgForUpdate.getId()))
//                .build();
//    }
//
//    @PUT
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
//    /*@ApiOperation(
//     value = "Updates Organization",
//     notes = "Updates Organization on Jaqpot by id",
//     extensions = {
//     @Extension(properties = {
//     @ExtensionProperty(name = "orn-@type", value = "x-orn:Organization"),}
//     )
//     ,
//     @Extension(name = "orn:returns", properties = {
//     @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Organization")
//     })
//     }
//     )
//     @ApiResponses(value = {
//     @ApiResponse(code = 401, response = ErrorReport.class, message = "Wrong, missing or insufficient credentials. Error report is produced.")
//     ,
//     @ApiResponse(code = 200, response = Organization.class, responseContainer = "List", message = "A list of algorithms in the Jaqpot framework")
//     ,
//     @ApiResponse(code = 500, response = ErrorReport.class, message = "Internal server error - this request cannot be served.")
//
//     })*/
//    @Operation(
//            summary = "Updates Organization",
//            description = "Updates Organization on Jaqpot by id",
//            extensions = {
//                @Extension(properties = {
//            @ExtensionProperty(name = "orn-@type", value = "x-orn:Organization"),}
//                )
//                ,
//                @Extension(name = "orn:returns", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Organization")
//        })
//            },
//            responses = {
//                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced.")
//                ,
//                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Organization.class))), description = "A list of algorithms in the Jaqpot framework")
//                ,
//                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
//            })
//    public Response updateOrganization(
//            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access this resource", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
//            @Parameter(name = "orgForUpdate", schema = @Schema(implementation = Organization.class)) Organization orgForUpdate) throws JaqpotNotAuthorizedException {
//
//        String userId = securityContext.getUserPrincipal().getName();
//
//        String[] apiA = api_key.split("\\s+");
//        String apiKey = apiA[1];
//        if (orgForUpdate.getId().equals("Jaqpot") && aaService.isAdmin(apiKey)) {
//            orgHandler.edit(orgForUpdate);
//        }
//
//        List<Notification> notifs = notificationHandler.getInvitationsToOrg(userId, orgForUpdate.getId(), 0, 100);
//        notifs.addAll(notificationHandler.getAffiliationsToOrg(userId, 0, 100));
//        if (notifs.size() > 0 || orgForUpdate.getMeta().getCreators().contains(userId)) {
//            orgHandler.edit(orgForUpdate);
//        } else {
//            throw new JaqpotNotAuthorizedException("You are not authorized to edit this Organization");
//        }
//
//        return Response
//                .ok(orgHandler.find(orgForUpdate.getId()))
//                .build();
//    }
//
//    @DELETE
//    @Path("/{id}")
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
//    @Operation(
//            summary = "Deletes Organization",
//            description = "Deletes Organization on Jaqpot by id. This action can be done only by the creators of an organization",
//            responses = {
//                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced.")
//                ,
//                @ApiResponse(responseCode = "200", description = "Organization deleted")
//                ,
//                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
//            })
//    public Response deleteOrganizationById(
//            @Parameter(description = "Authorization token") @HeaderParam("Authorization") String api_key,
//            @PathParam("id") String id) throws JaqpotNotAuthorizedException {
//
//        String[] apiA = api_key.split("\\s+");
//        String apiKey = apiA[1];
//        User user = userHandler.find(securityContext.getUserPrincipal().getName(), apiKey);
//        Organization organization = orgHandler.find(id);
//        if (!organization.getId().contains("Jaqpot") && organization.getMeta().getCreators().contains(user.get_id())) {
//            List<String> users = organization.getUserIds();
//            users.forEach(userId -> {
//                User userToUpdate = userHandler.find(userId, apiKey);
//                userToUpdate.getOrganizations().remove(organization.getId());
//            });
//            orgHandler.remove(organization);
//        } else {
//            throw new JaqpotNotAuthorizedException("You are not authorized to delete this Organization");
//        }
//        return Response
//                .ok(organization).status(Response.Status.ACCEPTED)
//                .build();
//    }
//
//    @GET
//    @Produces({MediaType.APPLICATION_JSON})
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Path("/search/and/found")
//    @Operation(summary = "Finds Organization from partial given org name",
//            description = "Finds all users queried",
//            responses = {
//                @ApiResponse(responseCode = "200", description = "Orgs found")
//                ,
//                @ApiResponse(responseCode = "401", description = "You are not authorized to access this user")
//                ,
//                @ApiResponse(responseCode = "403", description = "This request is forbidden (e.g., no authentication token is provided)")
//                ,
//                @ApiResponse(responseCode = "404", description = "No user was not found.")
//                ,
//                @ApiResponse(responseCode = "500", description = "Internal server error - this request cannot be served.")
//            })
//    public Response getAllOrgs(
//            @Parameter(name = "orgname", schema = @Schema(implementation = String.class)) @QueryParam("orgname") String orgname,
//            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access this resource", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key) throws JaqpotNotAuthorizedException {
//
//        Map<String, Object> search = new HashMap();
//        if (orgname != null) {
//            search.put("_id", orgname);
//        }
//        List<Organization> users = orgHandler.findAllWithPattern(search);
//
//        return Response.ok(users).build();
//    }
//
//    @PUT
//    @Path("/affiliations")
//    @TokenSecured({RoleEnum.DEFAULT_USER})
//    @Produces({MediaType.APPLICATION_JSON, "text/uri-list"})
//    @Operation(
//            summary = "Updates Organization",
//            description = "Updates Organization on Jaqpot by id",
//            extensions = {
//                @Extension(properties = {
//            @ExtensionProperty(name = "orn-@type", value = "x-orn:Organization"),}
//                )
//                ,
//                @Extension(name = "orn:returns", properties = {
//            @ExtensionProperty(name = "x-orn-@id", value = "x-orn:Organization")
//        })
//            },
//            responses = {
//                @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Wrong, missing or insufficient credentials. Error report is produced.")
//                ,
//                @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Organization.class))), description = "A list of algorithms in the Jaqpot framework")
//                ,
//                @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ErrorReport.class)), description = "Internal server error - this request cannot be served.")
//            })
//    public Response updateOrganizationsAffiliations(
//            @Parameter(name = "Authorization", description = "Clients need to authenticate in order to access this resource", schema = @Schema(implementation = String.class)) @HeaderParam("Authorization") String api_key,
//            @Parameter(name = "orgsForUpdate", array = @ArraySchema(schema = @Schema(implementation = Organization.class))) List<Organization> orgsForUpdate) throws JaqpotNotAuthorizedException {
//
//        String userId = securityContext.getUserPrincipal().getName();
//        String[] apiA = api_key.split("\\s+");
//        String apiKey = apiA[1];
//
//        if (orgsForUpdate.size() != 2) {
//            throw new BadRequestException("Cannot fulfill the reqiest");
//        }
//
//        if (orgsForUpdate.get(0).getMeta().getCreators().contains(userId)
//                || orgsForUpdate.get(1).getMeta().getCreators().contains(userId)) {
//            orgHandler.updateField(orgsForUpdate.get(0).getId(), "affiliations", orgsForUpdate.get(0).getAffiliations());
//            orgsForUpdate.get(0).getUserIds().forEach(useridorg1 -> {
//                Notification notif = NotificationFactory.affiliationBrokenNotification(userId, useridorg1, orgsForUpdate.get(1).getId());
//                try {
//                    notificationHandler.create(notif);
//                } catch (JaqpotDocumentSizeExceededException ex) {
//                    Logger.getLogger(OrganizationResource.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            });
//
//            orgHandler.updateField(orgsForUpdate.get(1).getId(), "affiliations", orgsForUpdate.get(1).getAffiliations());
//            orgsForUpdate.get(1).getUserIds().forEach(useridorg2 -> {
//                Notification notif = NotificationFactory.affiliationBrokenNotification(userId, useridorg2, orgsForUpdate.get(0).getId());
//                try {
//                    notificationHandler.create(notif);
//                } catch (JaqpotDocumentSizeExceededException ex) {
//                    Logger.getLogger(OrganizationResource.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            });
//        } else {
//            throw new JaqpotNotAuthorizedException("You are not authorized to alter the affiliations of these orgs");
//        }
//
//        return Response
//                .accepted()
//                .build();
//    }

}
