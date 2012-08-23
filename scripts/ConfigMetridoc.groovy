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

import grails.build.logging.GrailsConsole

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
//includeTargets << new File("grails.build.logging.GrailsConsole")
includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsArgParsing")

target(main: "Config metridoc-reports plugin") {
    depends(parseArguments)
    def copyFileFromMap = new HashMap<String, String>();
    GrailsConsole grailsConsole = new GrailsConsole()
    mapFile(copyFileFromMap, "${basedir}/grails-app/conf/Config.groovy", "${metridocCorePluginDir}/src/templates/conf/Config.groovy")
    mapFile(copyFileFromMap, "${basedir}/grails-app/conf/DataSource.groovy", "${metridocCorePluginDir}/src/templates/conf/DataSource.groovy")
    mapFile(copyFileFromMap, "${basedir}/grails-app/conf/UrlMappings.groovy", "${metridocCorePluginDir}/src/templates/conf/UrlMappings.groovy")

    copyFileFromMap.each {toFile, fromFile ->
        def splitName = toFile.split("/")
        def name = splitName[splitName.size() - 1]
        if (argsMap['o'] || isFileOverwrite(toFile)) {
            ant.mkdir(dir: toFile.replace("/${name}", ""))
            ant.copy(file: "${fromFile}",
                    tofile: "${toFile}",
                    overwrite: "true")
            grailsConsole.info("copied ${fromFile} to ${toFile}")
        } else {
            grailsConsole.info("Ignored ${name}")
        }
    }

    def rmFileMap = new HashSet<String>();
    rmFileMap.add("${basedir}/grails-app/views/layouts/main.gsp")
    rmFileMap.add("${basedir}/grails-app/views/error.gsp")
    rmFileMap.add("${basedir}/grails-app/views/index.gsp")
    rmFileMap.add("${basedir}/web-app/css/errors.css")
    rmFileMap.add("${basedir}/web-app/css/main.css")
    rmFileMap.add("${basedir}/web-app/css/mobile.css")
    rmFileMap.add("${basedir}/web-app/js/application.js")

    if(argsMap['o']){
        rmFileMap.each{filePath->
            if ((new File(filePath)).exists()){
                ant.delete(file: filePath)
                grailsConsole.info("Deleted ${filePath}")
            }
        }
    }else{
        rmFileMap.each {filePath->
            if (isFileRemove(filePath)){
                ant.delete(file: filePath)
            }else{
                grailsConsole.info("Ignored ${filePath}")
            }

        }
    }
}
/**
 * put toFile and fromFile into a HashMap whose keys are files to copy, and values are files copied from
 * @param fileMap HashMap record all toFiles and fromFiles
 * @param toFile filename to which we copy
 * @param fromFile filename from which we copy
 */
void mapFile(HashMap<String, String> fileMap, String toFile, String fromFile) {
    fileMap.put(toFile, fromFile)
}
/**
 * check if given file already exists, if yes, ask user whether overwrite the existing one.
 * @param filePath path of the file being checked
 * @return true if the file can be overwrote, false if user choose not to overwrite the existing file
 */
isFileOverwrite = {filePath ->
    File file = new File(filePath)
    String ifOverwrite = 'y'
    if (file.exists()) {
        String question = filePath + " already exists, can it be overwritten?"
        String[] validArgs = ['y', 'n']
        GrailsConsole grailsConsole = new GrailsConsole()
        ifOverwrite = grailsConsole.userInput(question, validArgs)
    }
    if ('n'.equals(ifOverwrite)) {
        return false
    }
    return true
}
/**
 * check if given file already exists, if exists, ask user whether remove the existing one.
 * @param filePath path of the file being checked
 * @return true if the file can be overwrote, false if user choose keep the existing one.
 */
isFileRemove = {filePath ->
    File file = new File(filePath)
    String ifFileRemove = 'y'
    if (file.exists()) {
        String question = filePath + " already exists by default, can it be deleted?"
        String[] validArgs = ['y', 'n']
        GrailsConsole grailsConsole = new GrailsConsole()
        ifFileRemove = grailsConsole.userInput(question, validArgs)
        if ('n'.equals(ifFileRemove)) {
            return false
        }
        grailsConsole.info("Deleted ${filePath}")
    }
    return true
}

setDefaultTarget(main)
