/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.hadoop.fs.swift;

import junit.framework.AssertionFailedError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.swift.exceptions.SwiftNotDirectoryException;
import org.apache.hadoop.fs.swift.snative.SwiftNativeFileSystem;
import org.apache.hadoop.fs.swift.util.SwiftTestUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This is the full filesystem contract test -which requires the
 * Default config set up to point to a filesystem
 */
public class TestSwiftFileSystemContract
  extends NativeSwiftFileSystemContractBaseTest {
  private static final Log LOG =
    LogFactory.getLog(TestSwiftFileSystemContract.class);

  @Override
  protected URI getFilesystemURI() throws URISyntaxException, IOException {
    return SwiftTestUtils.getServiceURI(new Configuration());
  }

  @Override
  protected SwiftNativeFileSystem createSwiftFS() throws IOException {
    SwiftNativeFileSystem swiftNativeFileSystem =
      new SwiftNativeFileSystem();
    return swiftNativeFileSystem;
  }

  @Override
  public void testMkdirsFailsForSubdirectoryOfExistingFile() throws Exception {
    Path testDir = path("/test/hadoop");
    assertFalse(fs.exists(testDir));
    assertTrue(fs.mkdirs(testDir));
    assertTrue(fs.exists(testDir));

    Path filepath = path("/test/hadoop/file");
    SwiftTestUtils.writeTextFile(fs, filepath, "hello, world", false);

    Path testSubDir = new Path(filepath, "subdir");
    SwiftTestUtils.assertPathDoesNotExist(fs, "subdir before mkdir", testSubDir);

    try {
      fs.mkdirs(testSubDir);
      fail("Should throw IOException.");
    } catch (SwiftNotDirectoryException e) {
      
      // expected
      assertEquals(filepath,e.getPath());
      
    }
    //now verify that the subdir path does not exist
    SwiftTestUtils.assertPathDoesNotExist(fs, "subdir after mkdir", testSubDir);

    Path testDeepSubDir = path("/test/hadoop/file/deep/sub/dir");
    try {
      fs.mkdirs(testDeepSubDir);
      fail("Should throw IOException.");
    } catch (SwiftNotDirectoryException e) {
      // expected
    }
    SwiftTestUtils.assertPathDoesNotExist(fs, "testDeepSubDir  after mkdir",
                                          testDeepSubDir);

  }

  @Override
  public void testWriteReadAndDeleteEmptyFile() throws Exception {
    try {
      super.testWriteReadAndDeleteEmptyFile();
    } catch (AssertionFailedError e) {
      SwiftTestUtils.downgrade("empty files get mistaken for directories", e);
    }
  }

  public void testZeroByteFilesAreFiles() throws Exception {
    SwiftTestUtils.unsupported("testZeroByteFilesAreFiles");
/*
    try {
      super.testZeroByteFilesAreFiles();
    } catch (AssertionFailedError e) {
      SwiftTestUtils.downgrade("zero byte files get mistaken for directories", e);
    }
*/
  }
}
