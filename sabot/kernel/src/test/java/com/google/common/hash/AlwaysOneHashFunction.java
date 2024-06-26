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
package com.google.common.hash;

/** A hash function used for testing rendezvous hashing */
public class AlwaysOneHashFunction extends AbstractNonStreamingHashFunction {
  @Override
  public int bits() {
    return 64;
  }

  @Override
  public HashCode hashBytes(byte[] arg0, int arg1, int arg2) {
    return HashCode.fromLong(1);
  }
}
