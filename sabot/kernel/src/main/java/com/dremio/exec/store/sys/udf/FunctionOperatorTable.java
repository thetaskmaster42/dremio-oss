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
package com.dremio.exec.store.sys.udf;

import java.util.List;

import org.apache.calcite.schema.FunctionParameter;
import org.apache.calcite.sql.SqlFunctionCategory;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlOperatorTable;
import org.apache.calcite.sql.SqlSyntax;
import org.apache.calcite.sql.fun.SqlBaseContextVariable;
import org.apache.calcite.sql.type.SqlReturnTypeInference;
import org.apache.calcite.sql.validate.SqlNameMatcher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class FunctionOperatorTable implements SqlOperatorTable {
  private final List<FunctionParameter> functionParameterList;

  public FunctionOperatorTable(List<FunctionParameter> functionParameterList) {
    this.functionParameterList = functionParameterList;
  }

  @Override public void lookupOperatorOverloads(
      SqlIdentifier opName,
      SqlFunctionCategory category,
      SqlSyntax syntax,
      List<SqlOperator> operatorList,
      SqlNameMatcher nameMatcher) {
    if (category != SqlFunctionCategory.USER_DEFINED_FUNCTION && category != null) {
      return;
    } else if(syntax != SqlSyntax.FUNCTION
      || opName == null
      || opName.names.size() != 1) {
      return;
    }

    String name = Iterables.getOnlyElement(opName.names);
    functionParameterList.stream()
      .filter(param -> nameMatcher.matches(param.getName(), name))
      .findFirst()
      .map(SqlArgumentContextVariable::create)
      .ifPresent(operatorList::add);
  }

  @Override public List<SqlOperator> getOperatorList() {
    return functionParameterList.stream()
      .map(SqlArgumentContextVariable::create)
      .collect(ImmutableList.toImmutableList());
  }
}

class SqlArgumentContextVariable extends SqlBaseContextVariable {
  protected SqlArgumentContextVariable(String name,
    SqlReturnTypeInference returnType) {
    super(name, returnType, SqlFunctionCategory.USER_DEFINED_FUNCTION);
  }

  static SqlArgumentContextVariable create(FunctionParameter functionParameter) {
    return new SqlArgumentContextVariable(
      functionParameter.getName(),
      opBinding -> functionParameter.getType(opBinding.getTypeFactory())
    );
  }
}