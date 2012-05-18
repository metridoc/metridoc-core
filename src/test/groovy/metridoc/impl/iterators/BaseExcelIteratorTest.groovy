/*
 * Copyright 2010 Trustees of the University of Pennsylvania Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package metridoc.plugins.impl.iterators

import org.junit.Test

/**
 * Created by IntelliJ IDEA.
 * User: tbarker
 * Date: 9/19/11
 * Time: 4:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class BaseExcelIteratorTest {

    @Test
    void testAlphabetConversion() {
        assert 1 == BaseExcelIterator.convertColumnToNumber("A")
        assert 1 == BaseExcelIterator.convertColumnToNumber("a")

        assert 29 == BaseExcelIterator.convertColumnToNumber("ac")
        assert 1 * (26 ** 2) + 1 * (26 ** 1) + 3 == BaseExcelIterator.convertColumnToNumber("aac")
    }

    @Test
    void testColumnWithRowConversion() {
        assert 1 == BaseExcelIterator.convertColumnToNumber("A2")
        assert 1 == BaseExcelIterator.convertColumnToNumber("a7")

        assert 29 == BaseExcelIterator.convertColumnToNumber("ac3")
        assert 1 * (26 ** 2) + 1 * (26 ** 1) + 3 == BaseExcelIterator.convertColumnToNumber("aac300")
    }
}