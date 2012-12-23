package metridoc.core

import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.text.StrBuilder

/**
 * contains all emails related to various operations in metridoc such as job failures
 */
class NotificationEmails {
    /**
     * scope of the email... ie what kind of notification is this
     */
    String scope
    String email


    static constraints = {
        email email: true, blank:false, unique: ['scope']
        scope blank: false
    }

    static void storeEmails(String scope, String emails) {
        withNewTransaction {
            def emailsInList = convertEmailsToList(emails)
            emailsInList.each {
                new NotificationEmails(email: it, scope: scope).save()
            }
        }
    }

    static List<String> getEmailsByScope(String scope) {
        List<String> result = []
        def notificationEmails = NotificationEmails.findAllByScope(scope)
        if(notificationEmails) {
            notificationEmails.each {
                result << it.email
            }
        }
        return result
    }

    private static String[] convertEmailsToList(String emails) {
        if (emails) {
            def trimmed = emails.trim()
            if (trimmed) {
                return trimmed.split(/\s+/)
            }
        }

        return [] as String[]
    }

    private static String convertListToString(List<NotificationEmails> emails) {
        def result = new StrBuilder()
        emails.each {
            result.appendln(it.email)
        }

        return result.toString()
    }
}