/*
 * Copyright (c) 2014 The APN-PROXY Project
 *
 * The APN-PROXY Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.xx_dev.apn.proxy.test;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.test.TestRemoteRuleGen 2014-12-31 18:38 (xmx) Exp $
 */
public class TestRemoteRuleGen {

    @Test
    public void test() throws FileNotFoundException {
        Scanner in = new Scanner(TestRemoteRuleGen.class.getResourceAsStream("/domains.txt"), "UTF-8");

        while (in.hasNextLine()) {

            String line = in.nextLine();
            System.err.println("\t\t\t<original-host>"+line+"</original-host>");

        }
        in.close();
    }

}
