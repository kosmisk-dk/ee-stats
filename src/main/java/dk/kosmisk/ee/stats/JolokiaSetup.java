/*
 * Copyright (C) 2018 Source (source (at) kosmisk.dk)
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
package dk.kosmisk.ee.stats;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jolokia.http.AgentServlet;

/**
 *
 * @author Source (source (at) kosmisk.dk)
 */
@WebListener
public class JolokiaSetup implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        Dynamic jolokiaName = context.addServlet("jolokia-name", new JolokiaName());
        jolokiaName.addMapping("/jolokia/name.txt");

        Dynamic jolokia = context.addServlet("jolokia", AgentServlet.class);
        jolokia.setInitParameter("mimeType", "application/json");
        jolokia.addMapping("/jolokia/*");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

    private static class JolokiaName extends HttpServlet {

        private final byte[] name;

        public JolokiaName() {
            this.name = getAppName().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain");
            try (ServletOutputStream os = resp.getOutputStream()) {
                os.write(name);
            }
        }

        private static String getAppName() {
            Map<String, String> env = System.getenv();
            if (env.containsKey("JOLOKIA_NAME")) {
                return env.get("JOLOKIA_NAME");
            }
            if (env.containsKey("APP_NAME")) {
                return env.get("APP_NAME");
            }
            try {
                return InitialContext.doLookup("java:app/AppName");
            } catch (NamingException ex) {
                return "Unknown";
            }
        }
    }

}
