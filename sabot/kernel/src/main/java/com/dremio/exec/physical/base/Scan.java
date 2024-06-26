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
package com.dremio.exec.physical.base;

import com.dremio.common.expression.SchemaPath;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public interface Scan extends Leaf {

  /**
   * The set of projected columns.
   *
   * @return The columns to be projected (they are applied to outcome of getSchema() to generate the
   *     projected schema.
   */
  @JsonProperty("columns")
  List<SchemaPath> getColumns();
}
