package com.minichat.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minichat.message.entity.ChatMessage;
import com.minichat.message.mapper.MessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageDeleteService {
    
    private final MessageMapper messageMapper;

    @Autowired
    public MessageDeleteService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    /**
     * 删除指定时间之前的冷数据(分批删除)
     * @param boundary 时间戳, 删除createTime < boundary 的data
     * @return 删除了多少条数据
     */
    @Transactional(rollbackFor = Exception.class)
    public long deleteByUpdateTimeBefore(LocalDateTime boundary) {
        // 这里只写分批删除逻辑
        log.info("开始删除 updateTime < {} 的数据", boundary);
        return deleteColdDataByUpdateTime(boundary);
    }
    
    private long deleteColdDataByUpdateTime(LocalDateTime boundary) {
        long startTime = System.currentTimeMillis();
        long totalDeleted = 0;
        int batchSize = 1000; // 每批删除 1000 条
        boolean hasMore = true;

        while (hasMore) {
            // 1. 先查询出本批次需要删除的 ID 列表（只查 ID，减少内存）
            Page<ChatMessage> page = messageMapper.selectPage(
                    new Page<>(1, batchSize),
                    new LambdaQueryWrapper<ChatMessage>()
                            .select(ChatMessage::getId)      // 只查 ID 字段，减少数据传输
                            .lt(ChatMessage::getCreateTime, boundary)
                            .orderByAsc(ChatMessage::getId) // 有序删除，避免死锁（可选）
            );

            List<ChatMessage> records = page.getRecords();
            if (records.isEmpty()) {
                break;
            }

            // 2. 提取 ID 列表
            List<Long> ids = records.stream()
                    .map(ChatMessage::getId)
                    .collect(Collectors.toList());

            // 3. 批量删除
            int deletedCount = messageMapper.deleteByIds(ids);
            totalDeleted += deletedCount;
            log.info("本批次删除 {} 条数据，累计删除 {} 条", deletedCount, totalDeleted);

            // 4. 判断是否还有下一页
            hasMore = page.hasNext();

            // 5. 可选：每批删除后休眠一小段时间，减轻数据库压力
            if (hasMore) {
                try {
                    Thread.sleep(50); // 休眠 50ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("休眠被中断", e);
                }
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("冷数据删除完成，共删除 {} 条记录，耗时 {} ms", totalDeleted, elapsed);
        return totalDeleted;
    }
}