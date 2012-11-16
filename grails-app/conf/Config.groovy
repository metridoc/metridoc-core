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

import org.apache.commons.lang.SystemUtils

import static org.quartz.SimpleScheduleBuilder.simpleSchedule

// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

//for jquery
grails.views.javascript.library="jquery"

def rootLoader = Thread.currentThread().contextClassLoader.rootLoader

def driverDirectory = new File("${SystemUtils.USER_HOME}/.grails/drivers")
if (driverDirectory.exists() && driverDirectory.isDirectory()) {
    if (rootLoader) {
        driverDirectory.eachFile {
            if(it.name.endsWith(".jar")) {
                def url = it.toURI().toURL()
                log.info "adding driver ${url}"
                rootLoader.addURL(url)
            }
        }
    }
}


grails.converters.default.pretty.print = true
metridoc.home = "${userHome}/.metridoc"

grails.dbconsole.enabled = true
grails.dbconsole.urlRoot = '/admin/dbconsole'
grails.config.locations = []

if (new File("${metridoc.home}/MetridocConfig.groovy").exists()) {
    log.info "found MetridocConfig.groovy, will add to configuration"
}
grails.config.locations << "file:${metridoc.home}/MetridocConfig.groovy"


if (System.properties["${appName}.config.location"]) {
    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
}

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [html: ['text/html', 'application/xhtml+xml'],
        xml: ['text/xml', 'application/xml'],
        text: 'text/plain',
        js: 'text/javascript',
        rss: 'application/rss+xml',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        all: '*/*',
        json: ['application/json', 'text/json'],
        form: 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data',
        xlsx: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        xls: "application/vnd.ms-excel"
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// enable query caching by default
grails.hibernate.cache.queries = true

metridoc.app.name = appName

// set per-environment serverURL stem for creating absolute links
environments {
    development {
        grails.logging.jul.usebridge = true
        grails.gsp.reload.enable = true
        grails.resources.processing.enabled = true
        grails.resources.debug = true

    }
    production {
        grails.logging.jul.usebridge = false
        // TODO: grails.serverURL = "http://www.changeme.com"
    }
}

// log4j configuration
log4j = {

    appenders {

        println "INFO: logs will be stored at ${config.metridoc.home}/logs"

        rollingFile name: "file",
                maxBackupIndex: 10,
                maxFileSize: "1MB",
                file: "${config.metridoc.home}/logs/metridoc.log"

        rollingFile name: "stacktrace",
                maxFileSize: "1MB",
                maxBackupIndex: 10,
                file: "${config.metridoc.home}/logs/metridoc-stacktrace.log"

        rollingFile name: "jobLog",
                maxFileSize: "1MB",
                maxBackupIndex: 10,
                file: "${config.metridoc.home}/logs/metridoc-job.log"
    }

    error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
            'org.springframework',
            'org.hibernate',
            'net.sf.ehcache.hibernate',
            'org.apache',
            'grails.util.GrailsUtil',
            'org.grails.plugin.resource',
            'grails.plugin.webxml.WebxmlGrailsPlugin'



    warn 'metridoc.camel',
            'ShiroGrailsPlugin',
            'org.quartz.core',
            'org.codehaus.groovy.grails.scaffolding',
            'metridoc.utils.CamelUtils'


    //logs all job output
    info jobLog: "metridoc.job"

    root {
        info 'stdout', 'file'
    }
}

//change the document parameters if creating a user manual for a plugin
grails.doc.authors = "Thomas Barker, Weizhuo Wu"

grails.doc.subtitle = " "

grails.doc.title = "MetriDoc User Manual"

//sets the layout for all pages
metridoc.style.layout = "main"

/**
 * here is a scheduling example for a workflow named FooWorkflow.  Workflows are currently deprecated, uses grail's
 * job plugin instead which should be installed by default
 */
metridoc {
    scheduling {
        workflows {
            //camel case version of the workflow, fo if a workflow is named FooWorkflow, this would be named foo.
            //If the name of the workflow is FooBarWorkflow, this would be fooBar if you want it to be scheduled.  For
            //now a new schedule can only be created here and the server must restart for it to take affect.
            foo {
                schedule = simpleSchedule().withIntervalInMinutes(5).repeatForever()
            }
        }
    }
}

/**
 * home page configuration to determine how a link is displayed on the home page.  these properties can ne set within
 * the controller as well.
 */
metridoc {
    homePage {
        accessFromConfig = [
            title: "Access From Config",
            description:"Access is determined by config file for home page"
        ]
    }
}