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
package org.sonar.server.platform;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.picocontainer.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.platform.Server;
import org.sonar.api.platform.ServerFileSystem;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Introspect the filesystem and the classloader to get extension files at startup.
 *
 * @since 2.2
 */
public class DefaultServerFileSystem implements ServerFileSystem, Startable {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServerFileSystem.class);

  private final Server server;
  private final File homeDir, tempDir;

  public DefaultServerFileSystem(Settings settings, Server server) {
    this.server = server;
    this.homeDir = new File(settings.getString("sonar.path.home"));
    this.tempDir = new File(settings.getString("sonar.path.temp"));
  }

  /**
   * for unit tests
   */
  public DefaultServerFileSystem(File homeDir, File tempDir, Server server) {
    this.homeDir = homeDir;
    this.tempDir = tempDir;
    this.server = server;
  }

  @Override
  public void start() {
    LOGGER.info("SonarQube home: " + homeDir.getAbsolutePath());
    try {
      if (getDeployDir() == null) {
        throw new IllegalArgumentException("Web app directory does not exist: " + getDeployDir());
      }
      FileUtils.forceMkdir(getDeployDir());
      for (File subDirectory : getDeployDir().listFiles((FileFilter) FileFilterUtils.directoryFileFilter())) {
        FileUtils.cleanDirectory(subDirectory);
      }

    } catch (IOException e) {
      throw new IllegalStateException("The following directory can not be created: " + getDeployDir().getAbsolutePath(), e);
    }

    File deprecated = getDeprecatedPluginsDir();
    try {
      FileUtils.forceMkdir(deprecated);
      FileUtils.cleanDirectory(deprecated);

    } catch (IOException e) {
      throw new IllegalStateException("The following directory can not be created: " + deprecated.getAbsolutePath(), e);
    }
  }

  @Override
  public void stop() {
    // do nothing
  }

  @Override
  public File getHomeDir() {
    return homeDir;
  }

  @Override
  public File getTempDir() {
    return tempDir;
  }

  public File getDeployDir() {
    return server.getDeployDir();
  }

  public File getDeployedJdbcDriverIndex() {
    return new File(getDeployDir(), "jdbc-driver.txt");
  }

  public File getDeployedPluginsDir() {
    return new File(getDeployDir(), "plugins");
  }

  public File getDownloadedPluginsDir() {
    return new File(getHomeDir(), "extensions/downloads");
  }

  public File getTrashPluginsDir() {
    return new File(getHomeDir(), "extensions/trash");
  }

  public List<File> getCorePlugins() {
    File corePluginsDir = new File(getHomeDir(), "lib/core-plugins");
    return getFiles(corePluginsDir, "jar");
  }

  public List<File> getBundledPlugins() {
    File corePluginsDir = new File(getHomeDir(), "lib/bundled-plugins");
    return getFiles(corePluginsDir, "jar");
  }

  public List<File> getUserPlugins() {
    File pluginsDir = getUserPluginsDir();
    return getFiles(pluginsDir, "jar");
  }

  public File getUserPluginsDir() {
    return new File(getHomeDir(), "extensions/plugins");
  }

  public File getDeprecatedPluginsDir() {
    return new File(getHomeDir(), "extensions/deprecated");
  }

  public File getPluginIndex() {
    return new File(getDeployDir(), "plugins/index.txt");
  }

  /**
   * @deprecated since 4.1
   */
  @Override
  @Deprecated
  public List<File> getExtensions(String dirName, String... suffixes) {
    File dir = new File(getHomeDir(), "extensions/rules/" + dirName);
    if (dir.exists() && dir.isDirectory()) {
      return getFiles(dir, suffixes);
    }
    return Collections.emptyList();
  }

  private List<File> getFiles(File dir, String... fileSuffixes) {
    List<File> files = new ArrayList<File>();
    if (dir != null && dir.exists()) {
      if (fileSuffixes != null && fileSuffixes.length > 0) {
        files.addAll(FileUtils.listFiles(dir, fileSuffixes, false));
      } else {
        files.addAll(FileUtils.listFiles(dir, null, false));
      }
    }
    return files;
  }
}
