/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.application;

import org.sonar.process.NetworkUtils;
import org.sonar.process.Props;

import java.util.HashMap;
import java.util.Map;

class DefaultSettings {

  public static final String WEB_SERVER_FORCED_JVM_ARGS = "-Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djruby.management.enabled=false " +
    // jruby is slow with java 8: https://jira.codehaus.org/browse/SONAR-6115
    "-Djruby.compile.invokedynamic=false";

  private DefaultSettings() {
    // only static stuff
  }

  static final String CLUSTER_MASTER = "sonar.cluster.master";
  static final String CLUSTER_NAME = "sonar.cluster.name";
  static final String CLUSTER_NODE_NAME = "sonar.node.name";
  static final String SEARCH_PORT = "sonar.search.port";
  static final String SEARCH_JAVA_OPTS = "sonar.search.javaOpts";
  static final String SEARCH_JAVA_ADDITIONAL_OPTS = "sonar.search.javaAdditionalOpts";
  static final String WEB_JAVA_OPTS = "sonar.web.javaOpts";
  static final String WEB_JAVA_ADDITIONAL_OPTS = "sonar.web.javaAdditionalOpts";
  static final String JDBC_URL = "sonar.jdbc.url";
  static final String JDBC_LOGIN = "sonar.jdbc.username";
  static final String JDBC_PASSWORD = "sonar.jdbc.password";

  static void init(Props props) {
    // forced property
    props.set("sonar.search.type", "TRANSPORT");

    // init string properties
    for (Map.Entry<String, String> entry : defaults().entrySet()) {
      props.setDefault(entry.getKey(), entry.getValue());
    }

    // init ports
    for (Map.Entry<String, Integer> entry : defaultPorts().entrySet()) {
      String key = entry.getKey();
      int port = props.valueAsInt(key, -1);
      if (port == -1) {
        // default port
        props.set(key, String.valueOf((int) entry.getValue()));
      } else if (port == 0) {
        // pick one available port
        props.set(key, String.valueOf(NetworkUtils.freePort()));
      }
    }
  }

  private static Map<String, String> defaults() {
    Map<String, String> defaults = new HashMap<String, String>();
    defaults.put(CLUSTER_NAME, "sonarqube");
    defaults.put(SEARCH_JAVA_OPTS, "-Xmx256m -Xms256m -Xss256k -Djava.net.preferIPv4Stack=true " +
      "-XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly " +
      "-XX:+HeapDumpOnOutOfMemoryError");
    defaults.put(SEARCH_JAVA_ADDITIONAL_OPTS, "");
    defaults.put(CLUSTER_NODE_NAME, "sonar-" + System.currentTimeMillis());
    defaults.put(WEB_JAVA_OPTS, "-Xmx768m -XX:MaxPermSize=160m -XX:+HeapDumpOnOutOfMemoryError");
    defaults.put(WEB_JAVA_ADDITIONAL_OPTS, "");
    defaults.put(JDBC_URL, "jdbc:h2:tcp://localhost:9092/sonar");
    defaults.put(JDBC_LOGIN, "sonar");
    defaults.put(JDBC_PASSWORD, "sonar");
    return defaults;
  }

  private static Map<String, Integer> defaultPorts() {
    Map<String, Integer> defaults = new HashMap<String, Integer>();
    defaults.put(SEARCH_PORT, 9001);
    return defaults;
  }
}
