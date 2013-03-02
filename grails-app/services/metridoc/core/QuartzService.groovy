package metridoc.core

import org.apache.commons.lang.SystemUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.quartz.*

import java.util.concurrent.TimeUnit

import static org.springframework.util.Assert.notNull

class QuartzService {

    static final GROOVY_VERSION = "2.0.5"
    static final GROOVY_DISTRIBUTION = "http://dist.groovy.codehaus.org/distributions/groovy-binary-${GROOVY_VERSION}.zip"
    static long NEXT_FIRE_TIME_WHERE_JOB_CONSIDERED_MANUAL = 1000L * 60L * 60L * 24L * 365L * 2L //TWO_YEARS
    def quartzScheduler
    def grailsApplication
    def pluginManager
    def commonService
    def mailService

    static boolean isManual(Trigger trigger) {
        long nextFireTime = trigger.nextFireTime.time
        long timeToNextFire = nextFireTime - new Date().time
        boolean isManual = timeToNextFire > NEXT_FIRE_TIME_WHERE_JOB_CONSIDERED_MANUAL
        return isManual
    }

    def mailJobError(Throwable throwable, JobExecutionContext context) {
        def trigger = context.trigger
        String triggerName = trigger.key.name
        String jobName = trigger.jobKey.name
        String shortErrorMessage = "error occurred running job ${jobName} with trigger ${triggerName} will notify interested users by email"

        def emails = NotificationEmails.findByScope(QuartzController.JOB_FAILURE_SCOPE).collect { it.email }
        def emailIsConfigured = commonService.emailIsConfigured() && emails && !NotificationEmailsService.emailDisabledFromSystemProperty()
        if (emailIsConfigured) {
            emails.each { email ->
                try {
                    log.info "sending email to ${email} about ${jobName} failure"
                    mailService.sendMail {
                        subject shortErrorMessage
                        text ExceptionUtils.getFullStackTrace(throwable)
                        to email
                    }
                } catch (Throwable emailError) {
                    log.error("could not send email to ${email}", emailError)
                }
            }

        } else {
            log.info "could not send email about ${jobName} failure since email is not configured or is disabled"
        }
    }

    def initializeJobs() {
        JobSchedule.list().each {
            def trigger = getTrigger(it.triggerName)
            if (trigger) {
                rescheduleJob(it.triggerName, it.triggerType.toString())
            }
        }
    }

    static List<String> getTriggerSchedules() {
        def result = []
        metridoc.trigger.Trigger.values().each {
            result << it.toString()
        }
        result << "DEFAULT"
        return result
    }

    private static String convertTriggerName(String name) {
        name.replaceAll("_", " ").toLowerCase()
    }

    TriggerKey triggerJobFromJobName(String jobName) {
        return triggerJobFromJobName(jobName, new JobDataMap())
    }

    TriggerKey triggerJobFromJobName(String jobName, JobDataMap dataMap) {
        def jobKey = new JobKey(jobName)
        List<org.quartz.Trigger> triggers = quartzScheduler.getTriggersOfJob(jobKey)
        if (triggers) {
            return triggerJobFromTrigger(triggers[0], dataMap)
        }

        quartzScheduler.triggerJob(jobKey, dataMap)
        return null
    }

    TriggerKey triggerJobFromTriggerName(String triggerName) {
        triggerJobFromTriggerName(triggerName, new JobDataMap())
    }

    TriggerKey triggerJobFromTriggerName(String triggerName, JobDataMap dataMap) {
        def triggerKey = new TriggerKey(triggerName)
        org.quartz.Trigger trigger = quartzScheduler.getTrigger(triggerKey)
        notNull(trigger, "Could not find job ${triggerName}")
        return triggerJobFromTrigger(trigger, dataMap)
    }

    TriggerKey triggerJobFromTrigger(org.quartz.Trigger trigger, JobDataMap dataMap) {
        notNull(trigger, "trigger cannot be null")
        def oldTrigger = trigger
        dataMap.oldTrigger = oldTrigger
        def newTrigger = getTriggerNowTrigger(trigger, dataMap)
        quartzScheduler.rescheduleJob(trigger.key, newTrigger)

        return newTrigger.key
    }

    TriggerKey triggerJobFromTrigger(org.quartz.Trigger trigger) {
        triggerJobFromTrigger(trigger, new JobDataMap())
    }

    org.quartz.Trigger getTriggerNowTrigger(org.quartz.Trigger trigger, JobDataMap dataMap) {
        notNull(trigger, "trigger cannot be null")
        def jobKey = trigger.getJobKey()
        def schedule = SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(1 * 24 * 356 * 4).repeatForever()
        def now = new Date()
        long fiveYears = 1000L * 60L * 60L * 24L * 365L * 5L + now.time
        def end = new Date(fiveYears)
        return TriggerBuilder.newTrigger().forJob(jobKey).startAt(now)
                .endAt(end).withIdentity(trigger.key).withSchedule(schedule).usingJobData(dataMap).build()
    }

    /**
     * gets a {@link JobConfig} if it exists, otherwise returns a new one with the trigger anme
     * @param triggerName
     * @return the job config associated with the trigger name
     */
    JobConfig getJobConfigByTrigger(String triggerName) {
        def jobConfig = JobConfig.findByTriggerName(triggerName)
        if (jobConfig) return jobConfig

        return new JobConfig(triggerName: triggerName)
    }

    org.quartz.Trigger getTriggerNowTrigger(org.quartz.Trigger trigger) {
        getTriggerNowTrigger(trigger, new JobDataMap())
    }

    org.quartz.Trigger getTrigger(String triggerName) {
        def triggerKey = new TriggerKey(triggerName)
        quartzScheduler?.getTrigger(triggerKey)
    }

    /**
     * checks if groovy has been downloaded to run jobs, if not it is downloaded under metridoc home
     */
    void checkForGroovyDistribution() {
        def metridocHome = grailsApplication.mergedConfig.metridoc?.home ?: "${SystemUtils.USER_HOME}/.metridoc"
        def groovyHome = "$metridocHome/groovy"
        def groovyDirectoryPath = "$groovyHome/groovy-$GROOVY_VERSION"
        def groovyDirectory = new File(groovyDirectoryPath)

        if (!groovyDirectory.exists()) {
            assert groovyDirectory.mkdirs(): "Could not create the groovy distribution directory"
            log.info "groovy distribution is not in metridoc home, downloading now, this could take several minutes"
            def groovyFile = new File("${groovyDirectoryPath}.zip")
            groovyFile.delete()
            assert groovyFile.createNewFile(): "Could not create ${groovyFile}"
            groovyFile << new URL(GROOVY_DISTRIBUTION).newInputStream()
            log.info "unzipping the groovy distribution"
            def ant = new AntBuilder()
            ant.unzip(
                    src: groovyFile,
                    dest: groovyDirectory
            )
        }
    }

    void rescheduleJob(String triggerName, String triggerDescription) {
        String jobName = getTrigger(triggerName).jobKey.name
        def plugin = pluginManager.getGrailsPluginForClassName("Quartz2GrailsPlugin").instance
        Closure schedulerJob = plugin.scheduleJob
        schedulerJob.delegate = plugin

        if ("DEFAULT" != triggerDescription) {
            def jobSchedule = getSchedule(triggerName, triggerDescription)
            jobSchedule.triggerType = metridoc.trigger.Trigger.valueOf(triggerDescription)
            jobSchedule.save(flush: true, failOnError: true)
            org.quartz.Trigger newTrigger
            if (triggerDescription == "NEVER") {
                long fiftyYears = TimeUnit.DAYS.toMillis(365 * 50)
                newTrigger = jobSchedule.convertTriggerToQuartzTrigger()
                newTrigger.startTime = new Date(new Date().time + fiftyYears)
            } else {
                newTrigger = jobSchedule.convertTriggerToQuartzTrigger()
                newTrigger.startTime = new Date()
            }

            def key = new TriggerKey(triggerName)
            newTrigger.key = key
            quartzScheduler.rescheduleJob(key, newTrigger)
        }
    }

    JobSchedule getSchedule(String triggerName, String description) {
        def schedule = JobSchedule.findByTriggerName(triggerName)
        if (schedule) return schedule

        return new JobSchedule(triggerName: triggerName, triggerType: metridoc.trigger.Trigger.valueOf(description))
    }

    ConfigObject getConfigByTriggerName(String triggerName) {
        def mergedConfig = grailsApplication.mergedConfig
        return getConfigurationMergedWithAppConfig(mergedConfig, triggerName)
    }

    static void addConfigToBinding(ConfigObject config, Binding binding) {
        if (config) {
            config.each { key, value ->
                binding.setVariable(key, value)
            }
        }
    }

    private static ConfigObject getConfigurationMergedWithAppConfig(ConfigObject applicationConfiguration, String triggerName) {
        def jobConfig = JobConfig.findByTriggerName(triggerName)
        ConfigObject jobConfigConfig = applicationConfiguration
        if (jobConfig) {
            jobConfigConfig = jobConfig.generateConfigObject()
            if (jobConfigConfig) {
                jobConfigConfig.merge(applicationConfiguration)
            }
        }

        return jobConfigConfig
    }
}
