/*
 * Copyright (C) 2018 DBC A/S (http://dbc.dk/)
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
 */
package dk.dbc.ee.test;

import dk.dbc.ee.stats.Timed;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Source (source (at) dbc.dk)
 */
@Stateless
@Path("status")
public class EndPoint {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response status() {
        return Response.ok("{\"ok\":true}", MediaType.APPLICATION_JSON_TYPE).build();
    }
}
