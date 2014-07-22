/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.rest.auth.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.Servlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RequestDumper extends HttpServlet implements Servlet {
	
	private static final long serialVersionUID = 1L;

	public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();

		out.println("<html><head><title>" + "Request Dumper</title></head>");
		out.println("<body><pre>");
		
		out.println(dump(req));
		out.println("</pre></body></html>");
	}
	
	
    public static String dump(HttpServletRequest request) {
        StringBuffer buf = new StringBuffer("\n\n");

        buf.append("REQUEST:\n--------\n");
        if (request.getUserPrincipal() != null) {
            buf.append("Principal name: [" + request.getUserPrincipal().getName() + "]\n");
        }
        else {
            buf.append("Principal is [null]\n");
        }     
        
        buf.append("AuthType: [" + request.getAuthType() + "]\n");
        buf.append("request URI: [" + request.getRequestURI() + "]\n");
        buf.append("request URL: [" + request.getRequestURL().toString() + "]\n");
        buf.append("isRequestedSessionIdFromCookie: [" + request.isRequestedSessionIdFromCookie() + "]\n");
        buf.append("isRequestedSessionIdFromURL: [" + request.isRequestedSessionIdFromURL() + "]\n");
        buf.append("isRequestedSessionIdValid: [" + request.isRequestedSessionIdValid() + "]\n");
        buf.append("isSecure: [" + request.isSecure() + "]\n");
        buf.append("In authenticated role?: [" + request.isUserInRole("authenticated") + "]\n");
        buf.append("In lightblue-user role?: [" + request.isUserInRole("lightblue-user") + "]\n");
        buf.append("In user-admin role?: [" + request.isUserInRole("user-admin") + "]\n");
        buf.append("In readonly role?: [" + request.isUserInRole("readonly") + "]\n");
        buf.append("In updater role?: [" + request.isUserInRole("updater") + "]\n");
        buf.append("In nonexistant role?: [" + request.isUserInRole("nonexistant") + "]\n");
        
        buf.append("\n\n");
        
        buf.append("BODY: \n------\n");
        
        StringBuffer requestBuffer = new StringBuffer();
        String line = null;
        try {
          BufferedReader reader = request.getReader();
          while ((line = reader.readLine()) != null) {
        	  requestBuffer.append(line.trim());  
          }
        } catch (Exception e) { /*report an error*/ }
                
        buf.append(requestBuffer.toString());
        
        buf.append("\n\n");
                
        buf.append("HEADERS: \n------\n");

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            buf.append("    ");
            buf.append(name);
            buf.append("=");
            buf.append(value);
            buf.append("\n");
        }

        buf.append("COOKIES:\n-------\n");
        Cookie[] cookies = request.getCookies();

        if(cookies != null) {
        	for (int i=0;i<cookies.length;i++) {
                buf.append("Cookie: [" + cookies[i].getName() + "] Value: [" +cookies[i].getValue() + "]\n");
                buf.append("    comment: [" + cookies[i].getComment() + "]\n");
                buf.append("    domain: [" + cookies[i].getDomain() + "]\n");
                buf.append("    maxAge: [" + cookies[i].getMaxAge() + "]\n");
                buf.append("    path: [" + cookies[i].getPath() + "]\n");
                buf.append("    secure?: [" + cookies[i].getSecure() + "]\n");
                buf.append("    version: [" + cookies[i].getVersion() + "]\n");
            }	
        }
        
        return (buf.toString());
    }
}
