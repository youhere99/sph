package com.zmx.mitm.cap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author zhaomingxing
 * @date 2024/11/21 14:44
 * @描述
 */
@Order(2)
@Slf4j
@Component
public class WeChatStart implements ApplicationRunner {

    @Value("${WeChat.path}")
    private String wechatPath;

    @Value("${appium.server}")
    private String appiumServer;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        boolean isWechatRunning = ProcessHandle.allProcesses()
                .map(ProcessHandle::info)
                .anyMatch(info -> info.command().orElse("").contains("WeChatPlayer.exe"));
        if (isWechatRunning) {
            log.info("微信已登录");
        } else {
            log.info("微信未登录");
            Runtime.getRuntime().exec(wechatPath);
        }
    }
}
