package com.minichat.message.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minichat.message.entity.ChatMessage;
import com.minichat.message.entity.MessageArchive;
import com.minichat.message.mapper.MessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.hadoop.fs.Path;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ClodDataArchiveService {

    private final MessageMapper messageMapper;
    private final OssService ossService;
    private final String archivePath = "cold-data/";
    private final MessageDeleteService messageDeleteService;

    @Autowired
    public ClodDataArchiveService(MessageMapper messageMapper, OssService ossService, MessageDeleteService messageDeleteService) {
        this.messageMapper = messageMapper;
        this.ossService = ossService;
        this.messageDeleteService = messageDeleteService;
    }

    public String cleanColdData(){
        LocalDate yesterday = LocalDate.now().minusDays(1); // 获取昨天时间
        LocalDateTime startTime = LocalDateTime.of(yesterday, LocalTime.MIN);//昨天开始
        LocalDateTime endTime = LocalDateTime.of(yesterday,LocalTime.MAX);//昨天的最后一刻
        log.info("开始归档 数据库中: {} 的数据", yesterday);
        //防止重复删除并上传oss
        long count = messageMapper.selectCount(
                new LambdaQueryWrapper<ChatMessage>()
                        .ge(ChatMessage::getCreateTime, startTime)
                        .le(ChatMessage::getCreateTime, endTime)
        );
        if(count==0){
            log.info("没有 {} 的冷数据需要归档，本次任务结束", yesterday);
            return null; // 或者返回空字符串，调用方判断即可
        }

        File tempFile = null;
        try {
            // 生成Parquet文件用于保存数据库冷数据
            tempFile = File.createTempFile("cold_orders_",".parquet");
            writeOrdersToParquet(tempFile, startTime,endTime);
            log.info("Parquet文件生成成功, 大小: {} bytes", tempFile.length());
            // 上传到 OSS
            // 将FFile转换为MultipartFile
            String fileName = "message_" + yesterday.format((DateTimeFormatter.ISO_DATE)) + ".parquet";
            String objectKey = archivePath + fileName;
            String contentType = "application/octet-stream"; // 通用二进制类型
            String ossUrl;
            try (FileInputStream fis = new FileInputStream(tempFile)) {
            ossUrl = ossService.uploadFile(fis, objectKey, contentType);
                log.info("文件已上传至OSS: {}",ossUrl);
            }
            // 删除数据库中的冷数据基于updateTime, 独立于归档
            LocalDateTime deletionBoundary = LocalDateTime.now().minusMonths(1);
            long deletedCount = messageDeleteService.deleteByUpdateTimeBefore(deletionBoundary);
            log.info("本次删除 {} 条冷数据（updateTime < {}）", deletedCount, deletionBoundary);
            return ossUrl;
            // TODO 还没写完不要忘了
        }catch (Exception e){
            log.error("冷数据归档失败", e);
            throw new RuntimeException("归档失败, 请检查并重试");
        }finally {
        // 清除本地临时文件
            if(tempFile!=null&&tempFile.exists()){
                if(tempFile.delete()){
                    log.debug("临时文件删除成功: {}",tempFile.getAbsolutePath());
                }else {
                    log.warn("临时文件删除失败: {}",tempFile.getAbsolutePath());
                }
            }
        }
    }

    private void writeOrdersToParquet(File parquetFile, LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        // 处理 LocalDateTime：要么把 Message.createTime 改成 String，要么加 @AvroEncode
        Schema avroSchema = ReflectData.get().getSchema(MessageArchive.class);

        try (ParquetWriter<MessageArchive> writer = AvroParquetWriter.<MessageArchive>builder(new Path(parquetFile.getAbsolutePath()))
                .withSchema(avroSchema)
                .withDataModel(ReflectData.get())
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .build()) {

            long pageSize = 1000;
            long currentPage = 1;
            Page<ChatMessage> messagePage;

            do {
                messagePage = messageMapper.selectPage(
                        new Page<>(currentPage, pageSize),
                        new LambdaQueryWrapper<ChatMessage>()
                                .ge(ChatMessage::getCreateTime, startTime)
                                .le(ChatMessage::getCreateTime, endTime)
                );
                List<ChatMessage> messages = messagePage.getRecords();
                if (!messages.isEmpty()) {
                    for (ChatMessage message : messages) {

                        MessageArchive messageArchive = getMessageArchive(message);
                        writer.write(messageArchive);
                    }
                }
                currentPage++;
            } while (messagePage.hasNext());
        }
    }

    // 在 ClodDataArchiveService 中新增方法

    /**
     * 根据时间范围恢复冷数据（自动匹配 OSS 上的归档文件）
     * @param startTime 查询起始时间
     * @param endTime   查询结束时间
     * @return 恢复的总条数 返回 -1表示OSS中也没有
     */
    @Transactional(rollbackFor = Exception.class)
    public int restoreByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("开始恢复时间范围 [{} ~ {}] 的冷数据", startTime, endTime);

        // 1. 计算涉及的日期列表（包含起始和结束日期）
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        List<LocalDate> dateList = startDate.datesUntil(endDate.plusDays(1))
                .collect(Collectors.toList());
        log.info("涉及的恢复日期: {}", dateList);

        int totalRestored = 0;

        for (LocalDate date : dateList) {
            String dateStr = date.format(DateTimeFormatter.ISO_DATE);
            String objectKey = archivePath + "message_" + dateStr + ".parquet";
            log.info("处理日期: {}", dateStr);

            // 2. 检查 OSS 文件是否存在
            if (!ossService.doesObjectExist(objectKey)) {
                log.warn("OSS 中不存在 {} 的归档文件，跳过", dateStr);
                continue;
            }

            File tempFile = null;
            try {
                // 3. 下载文件到临时目录
                tempFile = File.createTempFile("restore_", ".parquet");
                try (InputStream is = ossService.downloadFile(objectKey);
                     FileOutputStream fos = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                }
                log.info("文件下载完成: {}", tempFile.getAbsolutePath());

                // 4. 解析 Parquet 文件
                List<MessageArchive> archives = new ArrayList<>();
                try (ParquetReader<MessageArchive> reader = AvroParquetReader
                        .<MessageArchive>builder(new Path(tempFile.getAbsolutePath()))
                        .withDataModel(ReflectData.get())
                        .build()) {
                    MessageArchive record;
                    while ((record = reader.read()) != null) {
                        archives.add(record);
                    }
                }

                if (archives.isEmpty()) {
                    log.info("{} 的归档文件为空，无数据需要恢复", dateStr);
                    continue;
                }

                // 5. 转换为 ChatMessage 并准备插入
                List<ChatMessage> messages = archives.stream()
                        .map(archive -> {
                            ChatMessage msg = getMessage(archive);
                            // 将 ID 置空，让数据库自动生成新 ID（避免主键冲突）
                            msg.setId(null);
                            // TODO 修改updateTime ?/
                            msg.setUpdateTime(LocalDateTime.now());
                            return msg;
                        })
                        .collect(Collectors.toList());

                // 6. 分批插入（每批 2000 条，避免单次 SQL 过大）
                int batchSize = 2000;
                for (int i = 0; i < messages.size(); i += batchSize) {
                    int endIdx = Math.min(i + batchSize, messages.size());
                    List<ChatMessage> batch = messages.subList(i, endIdx);
                    int inserted = messageMapper.insertBatch(batch);
                    totalRestored += inserted;
                    log.info("日期 {} 插入 {} 条记录，累计恢复 {} 条", dateStr, inserted, totalRestored);
                }

            } catch (Exception e) {
                log.error("恢复日期 {} 的数据失败", dateStr, e);
                // 抛出异常，事务回滚（所有已恢复的数据都会回滚）
                throw new RuntimeException("恢复失败，事务回滚", e);
            } finally {
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }

        log.info("恢复完成，共恢复 {} 条记录", totalRestored);
        return totalRestored;
    }



    private static @NonNull MessageArchive getMessageArchive(ChatMessage message) {
        MessageArchive messageArchive = new MessageArchive();
        messageArchive.setId(message.getId());
        messageArchive.setConversationId(message.getConversationId());
        messageArchive.setChatType(message.getChatType());
        messageArchive.setFromId(message.getFromId());
        messageArchive.setToId(message.getToId());
        messageArchive.setMessageType(message.getMessageType());
        messageArchive.setStatus(message.getStatus());
        messageArchive.setClientSendTime(message.getClientSendTime());
        messageArchive.setContent(message.getContent());
        // 将LocalDataTime转成String
        messageArchive.setUpdateTime(message.getUpdateTime().toString());
        messageArchive.setCreateTime(message.getCreateTime().toString());
        return messageArchive;
    }

    private static @NonNull ChatMessage getMessage(@NonNull MessageArchive archive) {
        ChatMessage message = new ChatMessage();
        message.setId(archive.getId());
        message.setConversationId(archive.getConversationId());
        message.setChatType(archive.getChatType());
        message.setFromId(archive.getFromId());
        message.setToId(archive.getToId());
        message.setMessageType(archive.getMessageType());
        message.setStatus(archive.getStatus());
        message.setClientSendTime(archive.getClientSendTime());
        message.setContent(archive.getContent());
        // 关键：将 String 转回 LocalDateTime
        message.setUpdateTime(LocalDateTime.parse(archive.getUpdateTime()));
        message.setCreateTime(LocalDateTime.parse(archive.getCreateTime()));
        return message;
    }
}
