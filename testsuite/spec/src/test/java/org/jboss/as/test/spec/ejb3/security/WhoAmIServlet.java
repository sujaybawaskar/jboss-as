/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.test.spec.ejb3.security;

import static org.jboss.as.test.spec.ejb3.security.Util.getCLMLoginContext;
import javax.annotation.security.DeclareRoles;
import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

import com.sun.tools.corba.se.idl.toJavaPortable.StringGen;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
@WebServlet(urlPatterns = "/whoAmI", loadOnStartup = 1)
@ServletSecurity(@HttpConstraint(rolesAllowed = { "Users" }))
@DeclareRoles("Users")
public class WhoAmIServlet extends HttpServlet {
    @EJB
    private EntryBean bean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Writer writer = resp.getWriter();
        String method = req.getParameter("method");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if ("whoAmI".equals(method)) {
            LoginContext lc = null;
            try {
                if (username != null && password != null) {
                    lc = getCLMLoginContext(username, password);
                    lc.login();
                }
                try {
                    writer.write(bean.whoAmI());
                } finally {
                    if (lc != null) {
                        lc.logout();
                    }
                }
            } catch (LoginException le) {
                throw new IOException("Unexpected failure", le);
            }

        } else if ("doubleWhoAmI".equals(method)) {
            String[] response = null;
            if (username != null && password != null) {
                try {
                    response = bean.doubleWhoAmI(username, password);
                } catch (Exception e) {
                    writer.write(e.getClass().getName());
                }
            } else {
                try {
                    response = bean.doubleWhoAmI();
                } catch (Exception e) {
                    throw new ServletException("Unexpected failure", e);
                }
            }
            writer.write(response[0] + "," + response[1]);

        } else {
            throw new IllegalArgumentException("Parameter 'method' either missing or invalid method='" + method + "'");
        }

    }
}