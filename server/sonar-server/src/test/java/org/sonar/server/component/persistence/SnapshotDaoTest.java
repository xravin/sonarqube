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

package org.sonar.server.component.persistence;

import org.apache.commons.lang.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.utils.System2;
import org.sonar.core.component.SnapshotDto;
import org.sonar.core.persistence.AbstractDaoTestCase;
import org.sonar.core.persistence.DbSession;

import java.util.Date;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SnapshotDaoTest extends AbstractDaoTestCase {

  DbSession session;

  SnapshotDao sut;

  System2 system2;

  @Before
  public void createDao() throws Exception {
    session = getMyBatis().openSession(false);
    system2 = mock(System2.class);
    sut = new SnapshotDao(system2);
  }

  @After
  public void tearDown() throws Exception {
    session.close();
  }

  @Test
  public void get_by_key() {
    setupData("shared");

    SnapshotDto result = sut.getNullableByKey(session, 3L);
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(3L);
    assertThat(result.getResourceId()).isEqualTo(3L);
    assertThat(result.getRootProjectId()).isEqualTo(1L);
    assertThat(result.getParentId()).isEqualTo(2L);
    assertThat(result.getRootId()).isEqualTo(1L);
    assertThat(result.getStatus()).isEqualTo("P");
    assertThat(result.getLast()).isTrue();
    assertThat(result.getPurgeStatus()).isEqualTo(1);
    assertThat(result.getDepth()).isEqualTo(1);
    assertThat(result.getScope()).isEqualTo("DIR");
    assertThat(result.getQualifier()).isEqualTo("PAC");
    assertThat(result.getVersion()).isEqualTo("2.1-SNAPSHOT");
    assertThat(result.getPath()).isEqualTo("1.2.");

    assertThat(result.getPeriodMode(1)).isEqualTo("days1");
    assertThat(result.getPeriodModeParameter(1)).isEqualTo("30");
    assertThat(result.getPeriodDate(1)).isEqualTo(org.sonar.api.utils.DateUtils.parseDate("2011-09-24"));
    assertThat(result.getPeriodMode(2)).isEqualTo("days2");
    assertThat(result.getPeriodModeParameter(2)).isEqualTo("31");
    assertThat(result.getPeriodDate(2)).isEqualTo(org.sonar.api.utils.DateUtils.parseDate("2011-09-25"));
    assertThat(result.getPeriodMode(3)).isEqualTo("days3");
    assertThat(result.getPeriodModeParameter(3)).isEqualTo("32");
    assertThat(result.getPeriodDate(3)).isEqualTo(org.sonar.api.utils.DateUtils.parseDate("2011-09-26"));
    assertThat(result.getPeriodMode(4)).isEqualTo("days4");
    assertThat(result.getPeriodModeParameter(4)).isEqualTo("33");
    assertThat(result.getPeriodDate(4)).isEqualTo(org.sonar.api.utils.DateUtils.parseDate("2011-09-27"));
    assertThat(result.getPeriodMode(5)).isEqualTo("days5");
    assertThat(result.getPeriodModeParameter(5)).isEqualTo("34");
    assertThat(result.getPeriodDate(5)).isEqualTo(org.sonar.api.utils.DateUtils.parseDate("2011-09-28"));

    assertThat(result.getCreatedAt()).isEqualTo(org.sonar.api.utils.DateUtils.parseDate("2008-12-02"));
    assertThat(result.getBuildDate()).isEqualTo(org.sonar.api.utils.DateUtils.parseDate("2011-09-29"));

    assertThat(sut.getNullableByKey(session, 999L)).isNull();
  }

  @Test
  public void insert() {
    setupData("empty");

    when(system2.now()).thenReturn(org.sonar.api.utils.DateUtils.parseDate("2014-06-18").getTime());

    SnapshotDto dto = defaultSnapshot();

    sut.insert(session, dto);
    session.commit();

    assertThat(dto.getId()).isNotNull();
    checkTables("insert", "snapshots");
  }

  @Test
  public void lastSnapshot_returns_null_when_no_last_snapshot() {
    setupData("empty");

    SnapshotDto snapshot = sut.getLastSnapshot(session, defaultSnapshot());

    assertThat(snapshot).isNull();
  }

  @Test
  public void lastSnapshot_from_one_resource() {
    setupData("snapshots");

    SnapshotDto snapshot = sut.getLastSnapshot(session, defaultSnapshot().setResourceId(2L));

    assertThat(snapshot).isNotNull();
    assertThat(snapshot.getId()).isEqualTo(4L);
  }

  @Test
  public void lastSnapshot_from_one_resource_without_last_is_null() {
    setupData("snapshots");

    SnapshotDto snapshot = sut.getLastSnapshot(session, defaultSnapshot().setResourceId(5L));

    assertThat(snapshot).isNull();
  }

  @Test
  public void no_last_snapshot_older_than_another_one_in_a_empty_table() {
    setupData("empty");

    SnapshotDto snapshot = sut.getLastSnapshotOlderThan(session, defaultSnapshot());

    assertThat(snapshot).isNull();
  }

  @Test
  public void last_snapshot_older__than_a_reference() {
    setupData("snapshots");

    SnapshotDto referenceSnapshot = defaultSnapshot().setResourceId(1L);
    referenceSnapshot.setCreatedAt(org.sonar.api.utils.DateUtils.parseDate("2008-12-03"));
    SnapshotDto snapshot = sut.getLastSnapshotOlderThan(session, referenceSnapshot);

    assertThat(snapshot).isNotNull();
    assertThat(snapshot.getId()).isEqualTo(1L);
  }

  @Test
  public void last_snapshot_earlier__than_a_reference() {
    setupData("snapshots");

    SnapshotDto referenceSnapshot = defaultSnapshot().setResourceId(1L);
    referenceSnapshot.setCreatedAt(org.sonar.api.utils.DateUtils.parseDate("2008-12-01"));
    SnapshotDto snapshot = sut.getLastSnapshotOlderThan(session, referenceSnapshot);

    assertThat(snapshot).isNull();
  }

  @Test
  public void snapshot_and_child_retrieved() {
    setupData("snapshots");

    List<SnapshotDto> snapshots = sut.findSnapshotAndChildrenOfProjectScope(session, defaultSnapshot().setId(1L));

    assertThat(snapshots).isNotEmpty();
    assertThat(snapshots).onProperty("id").containsOnly(1L, 6L);
  }

  @Test
  public void set_snapshot_and_children_to_false_and_status_processed() {
    setupData("snapshots");
    SnapshotDto snapshot = defaultSnapshot().setId(1L);

    sut.updateSnapshotAndChildrenLastFlagAndStatus(session, snapshot, false, SnapshotDto.STATUS_PROCESSED);
    session.commit();

    List<SnapshotDto> snapshots = sut.findSnapshotAndChildrenOfProjectScope(session, snapshot);
    assertThat(snapshots).hasSize(2);
    assertThat(snapshots).onProperty("id").containsOnly(1L, 6L);
    assertThat(snapshots).onProperty("last").containsOnly(false);
    assertThat(snapshots).onProperty("status").containsOnly(SnapshotDto.STATUS_PROCESSED);
  }

  @Test
  public void set_snapshot_and_children_isLast_flag_to_false() {
    setupData("snapshots");
    SnapshotDto snapshot = defaultSnapshot().setId(1L);

    sut.updateSnapshotAndChildrenLastFlag(session, snapshot, false);
    session.commit();

    List<SnapshotDto> snapshots = sut.findSnapshotAndChildrenOfProjectScope(session, snapshot);
    assertThat(snapshots).hasSize(2);
    assertThat(snapshots).onProperty("id").containsOnly(1L, 6L);
    assertThat(snapshots).onProperty("last").containsOnly(false);
  }

  @Test
  public void find_children_modules() {
    setupData("modules");

    // From root project
    List<SnapshotDto> snapshots = sut.findChildrenModulesFromModule(session, "org.struts:struts");
    assertThat(snapshots).hasSize(2);
    assertThat(snapshots).onProperty("resourceId").containsOnly(2L, 3L);
    assertThat(snapshots).onProperty("parentId").containsOnly(1L, 2L);

    // From module
    snapshots = sut.findChildrenModulesFromModule(session, "org.struts:struts-core");
    assertThat(snapshots).hasSize(1);
    assertThat(snapshots).onProperty("resourceId").containsOnly(3L);
    assertThat(snapshots).onProperty("parentId").containsOnly(2L);

    // From sub module
    snapshots = sut.findChildrenModulesFromModule(session, "org.struts:struts-data");
    assertThat(snapshots).isEmpty();
  }

  @Test
  public void is_last_snapshot_when_no_previous_snapshot() {
    SnapshotDto snapshot = defaultSnapshot();

    boolean isLast = sut.isLast(snapshot, null);

    assertThat(isLast).isTrue();
  }

  @Test
  public void is_last_snapshot_when_previous_snapshot_is_older() {
    Date today = new Date();
    Date yesterday = DateUtils.addDays(today, -1);

    SnapshotDto snapshot = defaultSnapshot().setCreatedAt(today);
    SnapshotDto previousLastSnapshot = defaultSnapshot().setCreatedAt(yesterday);

    boolean isLast = sut.isLast(snapshot, previousLastSnapshot);

    assertThat(isLast).isTrue();
  }

  @Test
  public void is_not_last_snapshot_when_previous_snapshot_is_newer() {
    Date today = new Date();
    Date yesterday = DateUtils.addDays(today, -1);

    SnapshotDto snapshot = defaultSnapshot().setCreatedAt(yesterday);
    SnapshotDto previousLastSnapshot = defaultSnapshot().setCreatedAt(today);

    boolean isLast = sut.isLast(snapshot, previousLastSnapshot);

    assertThat(isLast).isFalse();
  }

  public static SnapshotDto defaultSnapshot() {
    return new SnapshotDto()
      .setResourceId(3L)
      .setRootProjectId(1L)
      .setParentId(2L)
      .setRootId(1L)
      .setStatus("P")
      .setLast(true)
      .setPurgeStatus(1)
      .setDepth(1)
      .setScope("DIR")
      .setQualifier("PAC")
      .setVersion("2.1-SNAPSHOT")
      .setPath("1.2.")
      .setPeriodMode(1, "days1")
      .setPeriodMode(2, "days2")
      .setPeriodMode(3, "days3")
      .setPeriodMode(4, "days4")
      .setPeriodMode(5, "days5")
      .setPeriodParam(1, "30")
      .setPeriodParam(2, "31")
      .setPeriodParam(3, "32")
      .setPeriodParam(4, "33")
      .setPeriodParam(5, "34")
      .setPeriodDate(1, org.sonar.api.utils.DateUtils.parseDate("2011-09-24"))
      .setPeriodDate(2, org.sonar.api.utils.DateUtils.parseDate("2011-09-25"))
      .setPeriodDate(3, org.sonar.api.utils.DateUtils.parseDate("2011-09-26"))
      .setPeriodDate(4, org.sonar.api.utils.DateUtils.parseDate("2011-09-27"))
      .setPeriodDate(5, org.sonar.api.utils.DateUtils.parseDate("2011-09-28"))
      .setBuildDate(org.sonar.api.utils.DateUtils.parseDate("2011-09-29"));
  }
}
