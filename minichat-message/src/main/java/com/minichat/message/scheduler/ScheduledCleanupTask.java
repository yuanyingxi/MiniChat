package com.minichat.message.scheduler;


import com.minichat.message.service.ClodDataArchiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduledCleanupTask {

    private final ClodDataArchiveService archiveService;

    @Autowired
    ScheduledCleanupTask(ClodDataArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledCleanColdData() {
        String ossUrl = archiveService.cleanColdData();
        if(ossUrl == null){
            log.info("定时任务执行完毕，本月无冷数据需要归档");
        }else{
            log.info("定时任务执行完毕,数据从数据库移动到OSS,访问url为{}", ossUrl);
        }
    }
}
