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

package org.sonar.core.component;

import org.sonar.core.persistence.Dto;

/**
 * Used to check that a project exists. Can return provisionned projects and projects from analysis.
 * Warning, this component should not be retrieve from db using a join on snapshots, otherwise provisionned projects will not be returned anymore.
 */
public class AuthorizedComponentDto extends Dto<String> {

  private Long id;
  private String kee;

  public Long getId() {
    return id;
  }

  public AuthorizedComponentDto setId(Long id) {
    this.id = id;
    return this;
  }

  public String key() {
    return kee;
  }

  public AuthorizedComponentDto setKey(String key) {
    this.kee = key;
    return this;
  }

  @Override
  public String getKey() {
    return kee;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AuthorizedComponentDto that = (AuthorizedComponentDto) o;

    if (!id.equals(that.id)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

}
