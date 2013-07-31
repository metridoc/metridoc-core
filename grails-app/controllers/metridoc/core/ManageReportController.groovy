package metridoc.core

import grails.web.RequestParameter

class ManageReportController {

    def manageReportService
    def initAuthService
    def grailsApplication

    static accessControl = {
        role(name: "ROLE_ADMIN")
    }

    def index() {
        def oldSearch = params.searchFilter
        [
                controllerDetails: manageReportService.controllerDetails,
                shiroFilters: grailsApplication.config.security.shiro.filter.filterChainDefinitions,
                searchFilter: oldSearch
        ]
    }

    def show(@RequestParameter('id') String controllerName) {
        if (controllerName == null || !controllerName.trim()) {
            render(status: 404, text: "A controller name is required")
            return
        }
        def controllerDetails = manageReportService.controllerDetails[controllerName]
        if (controllerDetails == null) {
            render(status: 404, text: "Could not find ${controllerName}")
            return
        }

        [controllerDetails: controllerDetails]
    }

    def updateAll() {
        def updateThese = params.controllerNames.tokenize(",")
        for (s in updateThese) {
            manageReportService.updateController(params, flash, s)
        }
        flash.info = "updated security for controller [$updateThese]"
        def oldSearch = params.searchFilter
        redirect(action: "index", params: [searchFilter: oldSearch])
    }

    def update(String controllerName) {
        manageReportService.updateController(params, flash, controllerName)

        redirect(action: "index")
    }
}


