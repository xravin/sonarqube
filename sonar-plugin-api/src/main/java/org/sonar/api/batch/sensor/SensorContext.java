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
package org.sonar.api.batch.sensor;

import org.sonar.api.batch.AnalysisMode;
import org.sonar.api.batch.CpdMapping;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.dependency.NewDependency;
import org.sonar.api.batch.sensor.duplication.NewDuplication;
import org.sonar.api.batch.sensor.highlighting.HighlightingBuilder;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.batch.sensor.measure.NewMeasure;
import org.sonar.api.batch.sensor.symbol.SymbolTableBuilder;
import org.sonar.api.batch.sensor.test.Coverage;
import org.sonar.api.batch.sensor.test.TestCaseCoverage;
import org.sonar.api.batch.sensor.test.TestCaseExecution;
import org.sonar.api.config.Settings;

import java.io.Serializable;

/**
 * See {@link Sensor#execute(SensorContext)}
 * @since 5.1
 */
public interface SensorContext {

  /**
   * Get settings of the current project.
   */
  Settings settings();

  /**
   * Get filesystem of the current project.
   */
  FileSystem fileSystem();

  /**
   * Get list of active rules.
   */
  ActiveRules activeRules();

  /**
   * Get analysis mode.
   */
  AnalysisMode analysisMode();

  // ----------- MEASURES --------------

  /**
   * Fluent builder to create a new {@link Measure}. Don't forget to call {@link NewMeasure#save()} once all parameters are provided.
   */
  <G extends Serializable> NewMeasure<G> newMeasure();

  // ----------- ISSUES --------------

  /**
   * Fluent builder to create a new {@link Issue}. Don't forget to call {@link Issue#save()} once all parameters are provided.
   */
  Issue newIssue();

  // ------------ HIGHLIGHTING ------------

  /**
   * Builder to define highlighting of a file.
   */
  HighlightingBuilder highlightingBuilder(InputFile inputFile);

  // ------------ SYMBOL REFERENCES ------------

  /**
   * Builder to define symbol references in a file.
   */
  SymbolTableBuilder symbolTableBuilder(InputFile inputFile);

  // ------------ DUPLICATIONS ------------

  /**
   * Builder to manually register duplication in a file. This can be used in addition to {@link CpdMapping} extension point.
   * Don't forget to call {@link NewDuplication#save()}.
   */
  NewDuplication newDuplication();

  // ------------ TESTS ------------

  /**
   * Create a new coverage report.
   * Don't forget to call {@link Coverage#save()} once all parameters are provided.
   */
  Coverage newCoverage();

  /**
   * Create a new test case execution report.
   * Don't forget to call {@link TestCaseExecution#save()} once all parameters are provided.
   */
  TestCaseExecution newTestCaseExecution();

  /**
   * Create a new test case coverage report.
   * Don't forget to call {@link TestCaseCoverage#save()} once all parameters are provided.
   */
  TestCaseCoverage newTestCaseCoverage();

  // ------------ DEPENDENCIES ------------

  /**
   * Create a new dependency.
   * Don't forget to call {@link NewDependency#save()} once all parameters are provided.
   */
  NewDependency newDependency();

}
