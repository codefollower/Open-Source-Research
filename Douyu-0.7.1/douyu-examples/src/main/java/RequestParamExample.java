/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/* $Id: RequestParamExample.java 982412 2010-08-04 21:55:19Z markt $
 *
 */

import java.io.PrintWriter;
import java.util.ResourceBundle;

import douyu.examples.util.HTMLFilter;
import douyu.http.HttpResponse;

/**
 * Example servlet showing request headers
 *
 * @author James Duncan Davidson <duncan@eng.sun.com>
 */

@douyu.mvc.Controller
public class RequestParamExample {

	private static final ResourceBundle RB = ResourceBundle.getBundle("LocalStrings");

	public void index(HttpResponse response, PrintWriter out, String firstName, String lastName) {
		response.setContentType("text/html");
		out.println("<html>");
		out.println("<body>");
		out.println("<head>");

		String title = RB.getString("requestparams.title");
		out.println("<title>" + title + "</title>");
		out.println("</head>");
		out.println("<body bgcolor=\"white\">");

		// img stuff not req'd for source code html showing

		// all links relative

		// XXX
		// making these absolute till we work out the
		// addition of a PathInfo issue 

		out.println("<a href=\"../reqparams.html\">");
		out.println("<img src=\"../images/code.gif\" height=24 " + "width=24 align=right border=0 alt=\"view code\"></a>");
		out.println("<a href=\"../index.html\">");
		out.println("<img src=\"../images/return.gif\" height=24 " + "width=24 align=right border=0 alt=\"return\"></a>");

		out.println("<h3>" + title + "</h3>");
		out.println(RB.getString("requestparams.params-in-req") + "<br>");
		if (firstName != null || lastName != null) {
			out.println(RB.getString("requestparams.firstname"));
			out.println(" = " + HTMLFilter.filter(firstName) + "<br>");
			out.println(RB.getString("requestparams.lastname"));
			out.println(" = " + HTMLFilter.filter(lastName));
		} else {
			out.println(RB.getString("requestparams.no-params"));
		}
		out.println("<P>");
		out.print("<form action=\"");
		out.print("RequestParamExample\" ");
		out.println("method=POST>");
		out.println(RB.getString("requestparams.firstname"));
		out.println("<input type=text size=20 name=firstName>");
		out.println("<br>");
		out.println(RB.getString("requestparams.lastname"));
		out.println("<input type=text size=20 name=lastName>");
		out.println("<br>");
		out.println("<input type=submit>");
		out.println("</form>");

		out.println("</body>");
		out.println("</html>");
	}
}
