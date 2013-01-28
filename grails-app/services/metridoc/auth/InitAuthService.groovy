package metridoc.auth

import metridoc.reports.ShiroRole
import metridoc.reports.ShiroUser
import org.apache.shiro.crypto.hash.Sha256Hash
import grails.util.Holders

/**
 * @auhor Tommy Barker
 *
 * initializes all default roles and users.  The default users are <code>admin</code> and <code>anonymous</code>.
 */
class InitAuthService {

    def grailsApplication
    final static DEFAULT_PASSWORD = "password"
    final static ANONYMOUS = "anonymous"
    final static ADMIN = "admin"
    final static REPORT_USER = "report_user"
    final static ROLE = "ROLE_"
    final static SUPER_USER = "super_user"
    final static REST = "rest"
    final static DEFAULT_ROLES = [ADMIN, SUPER_USER, REST, ANONYMOUS]

    /**
     * The dataSource that the service will build a transaction around
     */
    static dataSource

    static {
        def grailsApplication = Holders.grailsApplication

        if (grailsApplication) {
            if(grailsApplication.mergedConfig.dataSource_admin) {
                dataSource = 'admin'
            }
        }
    }

    /**
     * calls all security initializations
     */
    def init() {
        initDefaultRoles()
        initAdminUser()
        initAnonymousUser()
    }

    def initAdminUser() {
        ShiroUser.withTransaction {

            def adminUser = ShiroUser.find {
                username == "admin"
            }


            if (!adminUser) {

                def preConfiguredPassword = grailsApplication.config.metridoc.admin.password
                def password = preConfiguredPassword ? preConfiguredPassword : DEFAULT_PASSWORD

                if (DEFAULT_PASSWORD == password) {
                    log.warn "Could not find user admin, creating a default one with password '${DEFAULT_PASSWORD}'.  Change this immediatelly"
                }
                adminUser = new ShiroUser(username: 'admin', passwordHash: new Sha256Hash(password).toHex(), emailAddress: "admin@admin.com")

                def adminRole = ShiroRole.find {
                    name == createRoleName(ADMIN)
                }
                adminUser.addToRoles(adminRole)
                adminUser.save()
            } else {
                log.debug "admin user exists, the default admin does not need to be created"
            }
        }
    }

    def initAnonymousUser() {
        ShiroUser.withTransaction {
            def anonymousUser = ShiroUser.find() {
                username == ANONYMOUS
            }

            if (anonymousUser) {
                log.debug "anonymous user found, don't need to create a default one"

            } else {
                anonymousUser = new ShiroUser(
                    username: "anonymous",
                    passwordHash: new Sha256Hash("password").toHex(),
                )

            }

            def hasRoles = anonymousUser.roles

            if (!hasRoles) {
                def anonymousRole = ShiroRole.find {
                    name == createRoleName(ANONYMOUS)
                }
                anonymousUser.addToRoles(anonymousRole)
                anonymousUser.save()
            }
        }
    }

    /**
     * add all default roles if they don't exist, which include ROLE_ANONYMOUS, ROLE_ADMIN, and ROLE_REPORT_USER
     * @return
     */
    def initDefaultRoles() {
        DEFAULT_ROLES.each {shortRoleName ->
            def roleExists = ShiroRole.find {
                name == createRoleName(shortRoleName)
            }

            if (!roleExists) {
                createRole(shortRoleName).save()
            }
        }
    }

    /**
     * creates a role based on short name.  role <code>foo</code> would become <code>ROLE_FOO</code> and would be saved
     * with permission <code>foo</code>
     * @param type
     * @return
     */
    static createRole(String type) {
        def role = new ShiroRole(
            name: createRoleName(type)
        )

        role.addToPermissions(type)
    }

    static createRoleName(String type) {
        ROLE + type.toUpperCase()
    }
}
