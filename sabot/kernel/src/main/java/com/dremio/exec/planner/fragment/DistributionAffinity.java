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
package com.dremio.exec.planner.fragment;

/**
 * Describes an operator's endpoint assignment requirements. Ordering is from no assignment
 * requirement to mandatory assignment requirements. Changes/new addition should keep the order of
 * increasing restrictive assignment requirement.
 */
public enum DistributionAffinity {
  /** No affinity to any endpoints. SqlOperatorImpl can run on any endpoint. */
  NONE(SoftAffinityFragmentParallelizer.INSTANCE),

  /**
   * SqlOperatorImpl has soft distribution affinity to one or more endpoints. SqlOperatorImpl
   * performs better when fragments are assigned to the endpoints with affinity, but not a mandatory
   * requirement.
   */
  SOFT(SoftAffinityFragmentParallelizer.INSTANCE),

  /**
   * Hard distribution affinity to one or more endpoints. Fragments having the operator must be
   * scheduled on the nodes with affinity.
   */
  HARD(HardAffinityFragmentParallelizer.INSTANCE);

  private final FragmentParallelizer fragmentParallelizer;

  DistributionAffinity(final FragmentParallelizer fragmentParallelizer) {
    this.fragmentParallelizer = fragmentParallelizer;
  }

  /**
   * @return {@link FragmentParallelizer} implementation.
   */
  public FragmentParallelizer getFragmentParallelizer() {
    return fragmentParallelizer;
  }

  /**
   * Is the current DistributionAffinity less restrictive than the given DistributionAffinity?
   *
   * @param distributionAffinity
   * @return
   */
  public boolean isLessRestrictiveThan(final DistributionAffinity distributionAffinity) {
    return ordinal() < distributionAffinity.ordinal();
  }
}
