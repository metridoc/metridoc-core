package metridoc.core

class AccessInfoController {

    def accessInfoService

    def index() {
        [
            links: accessInfoService.buildHomeLinks()
        ]
    }

    //TODO: once finished remove this
    def testMenu() {

    }
}