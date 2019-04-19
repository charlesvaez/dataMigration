package com.zwzt.migration.redis.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Controller
@RequestMapping("/test")
public class TestController {
    private final static Logger logger = LoggerFactory.getLogger(TestController.class);
    private static final Long NUMBER = 1000L;
    private static final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();


    @Autowired
    @Qualifier("redisTemplate_source")
    private RedisTemplate redisTemplate_source;

    @Autowired
    @Qualifier("redisTemplate_desc")
    private RedisTemplate redisTemplate_desc;

    //http://localhost:8088/test/scan/zt
    @RequestMapping("/scan/{keyStr}")
    @ResponseBody
    public String scan(@PathVariable String keyStr) {
        if (null == keyStr || "".equals(keyStr.trim())) {
            return "参数变量-key为空!";
        }
        final AtomicLong totalCount = new AtomicLong();
        final AtomicLong failureCount = new AtomicLong();

        handleKeys(keyStr, totalCount, failureCount);

        return "共" + totalCount.get() + "条数据,成功迁移" + (totalCount.get() - failureCount.get()) + "条数据!";
    }

    private void handleKeys(String keyStr, AtomicLong totalCount, AtomicLong failureCount) {
        Cursor<byte[]> cursor = (Cursor<byte[]>) redisTemplate_source.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                ScanOptions options = new ScanOptions.ScanOptionsBuilder().count(NUMBER).match(keyStr + "*").build();

                return redisConnection.scan(options);
            }
        });

        logger.info("position: " + cursor.getPosition());

        if (cursor.getPosition() == 0) {
            while (cursor.hasNext()) {
                String key = null;
                try {
                    key = new String(cursor.next());
                    logger.debug("dump key -> {}", key);
                    byte[] value = redisTemplate_source.dump(key);

                    redisTemplate_desc.restore(key, value, 0, TimeUnit.MILLISECONDS);

                    logger.info("key: {}", key);
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    queue.add(key);
                    logger.error("key-{" + key + "}，数据迁移失败", e);
                } finally {
                    totalCount.incrementAndGet();
                }
            }
        } else {
            handleKeys(keyStr, totalCount, failureCount);
        }
    }

    //http://localhost:8088/test/migration/***
    @RequestMapping("/migration/{keyStr}")
    @ResponseBody
    @Deprecated
    public String migration(@PathVariable String keyStr) {
        if (null == keyStr || "".equals(keyStr.trim())) {
            return "参数变量-key为空!";
        }
        Set<String> keys = redisTemplate_source.keys(keyStr + "*");

//        logger.debug("keys end");
        if (keys != null && keys.size() > 0) {
            int failureCount = 0;
            int total = keys.size();
            for (String key : keys) {
                try {
                    logger.debug("dump key -> {}", key);
                    byte[] value = redisTemplate_source.dump(key);

                    redisTemplate_desc.restore(key, value, 0, TimeUnit.MILLISECONDS);

                    logger.info("key: {}", key);
                } catch (Exception e) {
                    failureCount++;
                    logger.error("key-{" + key + "}，数据迁移失败", e);
                }
            }

            return "共" + total + "条数据,成功迁移" + (total - failureCount) + "条数据!";
        } else {
            return "没有找到相关数据!";
        }

    }


    public static LinkedBlockingQueue queue() {
        return queue;
    }

}
