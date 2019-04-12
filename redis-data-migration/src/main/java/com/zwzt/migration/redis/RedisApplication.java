package com.zwzt.migration.redis;

import com.zwzt.migration.redis.controller.TestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class RedisApplication implements ApplicationRunner {
	private final static Logger logger = LoggerFactory.getLogger(RedisApplication.class);

	@Autowired
	@Qualifier("redisTemplate_source")
	private RedisTemplate redisTemplate_source;

	@Autowired
	@Qualifier("redisTemplate_desc")
	private RedisTemplate redisTemplate_desc;

	public static void main(String[] args) {
		SpringApplication.run(RedisApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		new Thread(){
			@Override
			public void run() {
				logger.info("重试线程启动!");
				LinkedBlockingQueue queue = TestController.queue();

				while(true){
					String key = null;
					try{
						key = queue.take()+"";
						logger.info("重试key-{}",key);

						byte[] value = redisTemplate_source.dump(key);

						redisTemplate_desc.restore(key, value, 0, TimeUnit.MILLISECONDS);

					} catch(Exception e){
						logger.error("key-{"+key+"}，数据重试迁移失败",e);
					}finally {
						logger.info("key-{},重试成功",key);
					}

//					try {
//						TimeUnit.SECONDS.sleep(1);
//					} catch (InterruptedException e) {
//						logger.error("休眠异常",e);
//					}
				}
			}
		}.start();
	}
}
