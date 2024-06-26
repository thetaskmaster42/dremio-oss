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
package com.dremio.exec.planner.physical;

import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelTrait;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.plan.volcano.RelSubset;
import org.apache.calcite.rel.RelNode;

public abstract class SubsetTransformer<T extends RelNode, E extends Exception> {
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SubsetTransformer.class);

  public abstract RelNode convertChild(T current, RelNode child) throws E;

  private final RelOptRuleCall call;

  public SubsetTransformer(RelOptRuleCall call) {
    this.call = call;
  }

  public RelTraitSet newTraitSet(RelTrait... traits) {
    RelTraitSet set = call.getPlanner().emptyTraitSet();
    for (RelTrait t : traits) {
      set = set.plus(t);
    }
    return set;
  }

  boolean go(T n, RelNode candidateSet) throws E {
    if (!(candidateSet instanceof RelSubset)) {
      return false;
    }

    boolean transform = false;
    for (RelNode rel : ((RelSubset) candidateSet).getRelList()) {
      if (isPhysical(rel)) {
        RelNode newRel =
            RelOptRule.convert(candidateSet, rel.getTraitSet().plus(Prel.PHYSICAL).simplify());
        RelNode out = convertChild(n, newRel);
        if (out != null) {
          call.transformTo(out);
          transform = true;
        }
      }
    }

    return transform;
  }

  private boolean isPhysical(RelNode n) {
    return n.getTraitSet().getTrait(ConventionTraitDef.INSTANCE).equals(Prel.PHYSICAL);
  }
}
