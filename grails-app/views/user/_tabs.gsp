<ul class="nav nav-tabs">

    <g:render template="/user/tabLabel"
              model="[controllerName: controllerName,
                      actionName: actionName,
                      linkController: 'manageAccess',
                      linkAction: 'list',
                      linkText: 'Manage Access',
                      icon: 'icon-group']"/>

    <g:render
            template="/user/tabLabel"
            model="[controllerName: controllerName,
                    actionName: actionName,
                    linkController: 'manageConfig',
                    linkAction: 'index',
                    linkText: 'General Settings',
                    icon: 'icon-cog']"/>
    <g:render
            template="/user/tabLabel"
            model="[controllerName: controllerName,
                    actionName: actionName,
                    linkController: 'LdapSettings',
                    linkAction: 'index',
                    linkText: 'LDAP Config',
                    icon: 'icon-sitemap']"/>
    <g:render
            template="/user/tabLabel"
            model="[controllerName: controllerName,
                    actionName: actionName,
                    linkController: 'LdapRole',
                    linkAction: 'index',
                    linkText: 'LDAP Role Mapping',
                    icon: 'icon-table']"/>
</ul>


