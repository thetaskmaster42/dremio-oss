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
package com.dremio.exec.store;

import com.dremio.exec.ExecTest;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import org.apache.hadoop.fs.BlockLocation;
import org.junit.Test;

public class TestAffinityCalculator extends ExecTest {
  static final org.slf4j.Logger logger =
      org.slf4j.LoggerFactory.getLogger(TestAffinityCalculator.class);

  String port = "1234";

  public BlockLocation[] buildBlockLocations(String[] hosts, long blockSize) {
    String[] names = new String[hosts.length];

    for (int i = 0; i < hosts.length; i++) {
      hosts[i] = "host" + i;
      names[i] = "host:" + port;
    }

    BlockLocation[] blockLocations = new BlockLocation[3];
    blockLocations[0] =
        new BlockLocation(
            new String[] {names[0], names[1], names[2]},
            new String[] {hosts[0], hosts[1], hosts[2]},
            0,
            blockSize);
    blockLocations[1] =
        new BlockLocation(
            new String[] {names[0], names[2], names[3]},
            new String[] {hosts[0], hosts[2], hosts[3]},
            blockSize,
            blockSize);
    blockLocations[2] =
        new BlockLocation(
            new String[] {names[0], names[1], names[3]},
            new String[] {hosts[0], hosts[1], hosts[3]},
            blockSize * 2,
            blockSize);

    return blockLocations;
  }

  @Test
  public void testBuildRangeMap() {
    BlockLocation[] blocks = buildBlockLocations(new String[4], 256 * 1024 * 1024);
    long tA = System.nanoTime();
    ImmutableRangeMap.Builder<Long, BlockLocation> blockMapBuilder =
        new ImmutableRangeMap.Builder<>();
    for (BlockLocation block : blocks) {
      long start = block.getOffset();
      long end = start + block.getLength();
      Range<Long> range = Range.closedOpen(start, end);
      blockMapBuilder = blockMapBuilder.put(range, block);
    }
    ImmutableRangeMap<Long, BlockLocation> map = blockMapBuilder.build();
    long tB = System.nanoTime();
    System.out.println(String.format("Took %f ms to build range map", (tB - tA) / 1e6));
  }
}
