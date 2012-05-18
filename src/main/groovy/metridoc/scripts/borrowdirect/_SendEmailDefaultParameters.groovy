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
package metridoc.scripts.borrowdirect

import groovy.sql.Sql

borrowDirectReportDistributionTable = "bd_report_distribution"

borrowDirectReportLinkSql = "select * from ${borrowDirectReportDistributionTable} order by institution_id"

getBorrowDirectReportLinks = {Sql sql ->

    def reportLinks = [:]
    sql.eachRow(borrowDirectReportLinkSql as String) {row ->
        def lib = row.library_id
        def addr = row.email_addr

        reportLinks[addr] = lib
    }

    return reportLinks
}

borrowDirectBaseUrl = "http://metridoc.library.upenn.edu/metridoc-penn-borrow/borrowDirect/reports/dataDump"

borrowDirectEmailSubject = "Borrow Direct Transactions Download Link"
borrowDirectEmailHost = "sd.library.upenn.int"
borrowDirectFromAddress = 'pcoll@pobox.upenn.edu'
borrowDirectEmailIntro = "A data file of all Borrow Direct transactions for your institution is available from " +
    "the Penn Libraries' Data Farm at the following URL. <BR><BR> "
borrowDirectEmailInstructions = "<BR><BR>Click on the URL or cut and paste it into your browser to retrieve the file. " +
    " If you're unable to complete the download or see a problem with the data file, please contact " +
    "${borrowDirectFromAddress}. This service is provided by Penn's MetriDoc management information platform. "

borrowDirectConfigFile = "borrowDirectConfig"