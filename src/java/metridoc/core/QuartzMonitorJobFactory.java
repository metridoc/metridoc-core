package metridoc.core;

import grails.plugin.quartz2.GrailsArtefactJob;
import grails.plugin.quartz2.GrailsJobFactory;
import org.hibernate.SessionFactory;
import org.quartz.*;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: tbarker
 * Date: 11/6/12
 * Time: 7:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class QuartzMonitorJobFactory extends GrailsJobFactory {

    static final java.util.Map<String, Map<String, Object>> jobRuns = new HashMap<String, Map<String, Object>>();
    private SessionFactory sessionFactory;
    private ApplicationContext applicationContext;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        //String grailsJobName = bundle.getJobDetail().getName();
        String grailsJobName = bundle.getTrigger().getKey().getName();

        Job job = super.newJob(bundle, scheduler);
        if (job instanceof GrailsArtefactJob) {
            Map<String, Object> map;
            if (jobRuns.containsKey(grailsJobName)) {
                map = jobRuns.get(grailsJobName);
            } else {
                map = new HashMap<String, Object>();
                jobRuns.put(grailsJobName, map);
            }
            job = new QuartzDisplayJob((GrailsArtefactJob) job, map, sessionFactory);
        }
        return job;
    }

//    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
//        //String grailsJobName = bundle.getJobDetail().getName();
//        String grailsJobName = bundle.getTrigger().getKey().getName();
//        Scheduler scheduler = applicationContext.getBean(Scheduler.class);
//        Object job = super.newJob(bundle, scheduler);
//        if (job instanceof GrailsArtefactJob) {
//            Map<String, Object> map;
//            if (jobRuns.containsKey(grailsJobName)) {
//                map = jobRuns.get(grailsJobName);
//            } else {
//                map = new HashMap<String, Object>();
//                jobRuns.put(grailsJobName, map);
//            }
//            job = new QuartzDisplayJob((GrailsArtefactJob) job, map, sessionFactory);
//        }
//        return job;
//    }

//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        super.setApplicationContext(applicationContext);    //To change body of overridden methods use File | Settings | File Templates.
//        this.applicationContext = applicationContext;
//    }

    /**
     * Quartz Job implementation that invokes execute() on the GrailsTaskClassJob instance whilst recording the time
     */
    public class QuartzDisplayJob implements Job {
        GrailsArtefactJob job;
        Map<String, Object> jobDetails;
        private SessionFactory sessionFactory;

        public QuartzDisplayJob(GrailsArtefactJob job, Map<String, Object> jobDetails, SessionFactory sessionFactory) {
            this.job = job;
            this.jobDetails = jobDetails;
            this.sessionFactory = sessionFactory;
        }

        public void execute(final JobExecutionContext context) throws JobExecutionException {
            jobDetails.clear();
            jobDetails.put("lastRun", new Date());
            jobDetails.put("status", "running");
            long start = System.currentTimeMillis();
            try {
                job.execute(context);
                sessionFactory.getCurrentSession().flush();
            } catch (Throwable e) {
                jobDetails.put("error", e.getMessage());
                jobDetails.put("status", "error");
                if (e instanceof JobExecutionException) {
                    throw (JobExecutionException) e;
                }
                throw new JobExecutionException(e.getMessage(), e);
            }
            jobDetails.put("status", "complete");
            jobDetails.put("duration", System.currentTimeMillis() - start);
        }
    }
}
