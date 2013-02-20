package metridoc.core



import grails.test.mixin.*
import metridoc.trigger.Trigger
import org.junit.*
import org.quartz.TriggerKey
import org.quartz.core.QuartzScheduler

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(QuartzService)
@Mock(JobSchedule)
class QuartzServiceTests {

    @Test
    void "fixing bug where null pointer exception occurs if an application starts but the jobs don't exist"() {
        new JobSchedule(triggerName: "foobarbazz", triggerType: Trigger.EVERY_10_MINUTES).save(flush: true)
        //this should not fail
        new QuartzService().initializeJobs()
    }

    @Test
    void "illegal argument exception thrown if trigger not found"() {
        def quartzScheduler = new Expando()
        quartzScheduler.getTrigger = { TriggerKey key -> null }
        service.quartzScheduler = quartzScheduler

        doIllegalArgumentCheck { service.triggerJobFromTriggerName("does not exist") }
    }

    @Test
    void "illegal argument exception thrown if trigger is null"() {
        doIllegalArgumentCheck { service.triggerJobFromTrigger(null) }
    }

    @Test
    void "get trigger now throws illegal argument exception if trigger is null"() {
        doIllegalArgumentCheck {service.getTriggerNowTrigger(null)}
    }

    void doIllegalArgumentCheck(Closure closure) {
        try {
            closure.call()
            assert false: "illegal argument should have occurred"
        } catch (IllegalArgumentException e) {
        }
    }
}
