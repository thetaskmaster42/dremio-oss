/*
 * Copyright (C) 2017-2019 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dremio.exec.sql;

import com.dremio.BaseTestQuery;
import com.dremio.TestBuilder;
import com.dremio.common.utils.SqlUtils;
import com.google.common.base.Strings;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for view tests. It has utility methods which can be used when writing tests for views
 * on tables in different storage engines such as Hive, HBase etc.
 */
public class TestBaseViewSupport extends BaseTestQuery {
  private static AtomicInteger viewSeqNum = new AtomicInteger(0);

  /**
   * Create view with given parameters.
   *
   * <p>Current default schema "dfs_test"
   *
   * <p>CREATE VIEW tmp.viewName(f1, f2) AS SELECT * FROM cp.\"region.json\"
   *
   * <p>For the above CREATE VIEW query, function parameters are: viewSchema = "tmp" viewName =
   * "viewName" finalSchema = "dfs_test.tmp" viewFields = "(f1, f2)" viewDef = "SELECT * FROM
   * cp.\"region.json\""
   *
   * @param viewSchema Schema name to prefix when referring to the view.
   * @param viewName Name of the view.
   * @param finalSchema Absolute schema path where the view is created. Pameter <i>viewSchema</i>
   *     may refer the schema with respect the current default schema. Combining <i>viewSchema</i>
   *     with default schema gives the final schema.
   * @param viewFields If the created view needs to specify the fields, otherwise null
   * @param viewDef Definition of the view.
   * @throws Exception
   */
  protected void createViewHelper(
      final String viewSchema,
      final String viewName,
      final String finalSchema,
      final String viewFields,
      final String viewDef)
      throws Exception {

    String viewFullName = SqlUtils.quoteIdentifier(viewName);
    if (!Strings.isNullOrEmpty(viewSchema)) {
      viewFullName = viewSchema + "." + viewFullName;
    }

    final String createViewSql =
        String.format(
            "CREATE VIEW %s %s AS %s", viewFullName, viewFields == null ? "" : viewFields, viewDef);

    testBuilder()
        .sqlQuery(createViewSql)
        .unOrdered()
        .baselineColumns("ok", "summary")
        .baselineValues(
            true, String.format("View '%s.%s' created successfully", finalSchema, viewName))
        .go();
  }

  /**
   * Drop view with given parameters.
   *
   * <p>Current schema "dfs_test" DROP VIEW tmp.viewName
   *
   * <p>For the above DROP VIEW query, function parameters values are: viewSchema = "tmp" "viewName"
   * = "viewName" "finalSchema" = "dfs_test.tmp"
   *
   * @param viewSchema
   * @param viewName
   * @param finalSchema
   * @throws Exception
   */
  protected void dropViewHelper(
      final String viewSchema, final String viewName, final String finalSchema) throws Exception {
    String viewFullName = SqlUtils.quoteIdentifier(viewName);
    if (!Strings.isNullOrEmpty(viewSchema)) {
      viewFullName = viewSchema + "." + viewFullName;
    }

    testBuilder()
        .sqlQuery(String.format("DROP VIEW %s", viewFullName))
        .unOrdered()
        .baselineColumns("ok", "summary")
        .baselineValues(
            true, String.format("View [%s.%s] deleted successfully.", finalSchema, viewName))
        .go();
  }

  /**
   * Drop view if exists with given parameters.
   *
   * <p>Current schema "dfs_test" DROP VIEW IF EXISTS tmp.viewName
   *
   * <p>For the above DROP VIEW IF EXISTS query, function parameters values are: viewSchema = "tmp"
   * "viewName" = "viewName" "finalSchema" = "dfs_test.tmp" "ifViewExists" = null
   *
   * @param viewSchema
   * @param viewName
   * @param finalSchema
   * @param ifViewExists Helps to check query result depending from the existing of the view.
   * @throws Exception
   */
  protected void dropViewIfExistsHelper(
      final String viewSchema,
      final String viewName,
      final String finalSchema,
      Boolean ifViewExists)
      throws Exception {
    String viewFullName = SqlUtils.quoteIdentifier(viewName);
    if (!Strings.isNullOrEmpty(viewSchema)) {
      viewFullName = viewSchema + "." + viewFullName;
    }
    if (ifViewExists == null) {
      // ifViewExists == null: we do not know whether the table exists. Just drop it if exists or
      // skip dropping if doesn't exist
      test(String.format("DROP VIEW IF EXISTS %s", viewFullName));
    } else if (ifViewExists) {
      testBuilder()
          .sqlQuery(String.format("DROP VIEW IF EXISTS %s", viewFullName))
          .unOrdered()
          .baselineColumns("ok", "summary")
          .baselineValues(
              true, String.format("View [%s.%s] deleted successfully.", finalSchema, viewName))
          .go();
    } else {
      testBuilder()
          .sqlQuery(String.format("DROP VIEW IF EXISTS %s", viewFullName))
          .unOrdered()
          .baselineColumns("ok", "summary")
          .baselineValues(true, String.format("View [%s.%s] not found.", finalSchema, viewName))
          .go();
    }
  }

  /**
   * Execute the given query and check against the given baseline.
   *
   * @param query
   * @param baselineColumns
   * @param baselineValues
   * @throws Exception
   */
  protected void queryViewHelper(
      final String query, final String[] baselineColumns, final List<Object[]> baselineValues)
      throws Exception {
    TestBuilder testBuilder =
        testBuilder().sqlQuery(query).unOrdered().baselineColumns(baselineColumns);

    for (Object[] values : baselineValues) {
      testBuilder = testBuilder.baselineValues(values);
    }

    testBuilder.go();
  }

  /**
   * Generate a view name by appending the test class with a increasing counter.
   *
   * @return
   */
  protected static String generateViewName() {
    return TestBaseViewSupport.class.getSimpleName() + "_" + viewSeqNum.incrementAndGet();
  }

  /**
   * Tests creating a view with given parameters, query the view and check against the provided
   * baselines and finally drop the view.
   *
   * @param finalSchema Absolute schema path where the view is going to be created.
   * @param viewFields If view has any field list
   * @param viewDef Definition of the view.
   * @param queryOnView Query to run on created view. Refer to test view using "TEST_VIEW_NAME".
   *     Similarly schema using "TEST_SCHEMA".
   * @param expectedBaselineColumns Expected columns from querying the view.
   * @param expectedBaselineValues Expected row values from querying the view.
   * @throws Exception
   */
  protected void testViewHelper(
      final String finalSchema,
      final String viewFields,
      final String viewDef,
      String queryOnView,
      final String[] expectedBaselineColumns,
      final List<Object[]> expectedBaselineValues)
      throws Exception {
    final String viewName = generateViewName();
    String viewFullName = SqlUtils.quoteIdentifier(viewName);

    try {
      createViewHelper(finalSchema, viewName, finalSchema, viewFields, viewDef);

      queryOnView =
          queryOnView.replace("TEST_VIEW_NAME", viewFullName).replace("TEST_SCHEMA", finalSchema);

      queryViewHelper(queryOnView, expectedBaselineColumns, expectedBaselineValues);
    } finally {
      dropViewHelper(finalSchema, viewName, finalSchema);
    }
  }
}
