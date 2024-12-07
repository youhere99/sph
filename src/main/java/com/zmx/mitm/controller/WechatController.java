package com.zmx.mitm.controller;

import cn.hutool.core.swing.RobotUtil;
import cn.hutool.http.Header;
import com.alibaba.fastjson.JSONObject;
import com.zmx.mitm.cap.WeChatContext;
import io.appium.java_client.windows.WindowsDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Screen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaomingxing
 * @date 2024/11/14 14:56
 * @描述
 */
@Slf4j
@RequestMapping(value = "/wechat")
@RestController
public class WechatController {

    @Value("${WeChat.path}")
    private String wechatPath;
    @Value("${appium.server}")
    private String appiumServer;

    @Value("${WeChat.uploadLiveUrl}")
    private String uploadUrl;

    @Value("${WeChat.mitmPath}")
    private String mitmPath;

    @Autowired
    private WeChatContext weChatContext;

    @GetMapping(value = "/push")
    public String push(@RequestParam(value = "nickName") String nickName) {
        weChatContext.getNickNameQueue().add(nickName);
        return "success";
    }

    @Scheduled(cron = "*/3 * * * * ?")
    public void task() {
        log.info("需要自动化媒体号 " + JSONObject.toJSONString(weChatContext.getNickNameQueue()));
        if (weChatContext.getCurrentNickName() == null && weChatContext.getNickNameQueue().size() > 0) {
            WindowsDriver windowsDriver = null;
            try {
                DesiredCapabilities capabilities = new DesiredCapabilities();
                capabilities.setCapability("app", wechatPath);
                capabilities.setCapability("platformName", "windows");
                capabilities.setCapability("deviceName", "WindowsPC");
                WindowsDriver driver = new WindowsDriver(new URL(appiumServer), capabilities);
                try {
                    Screen screen = new Screen();
                    String jarPath = new ApplicationHome().getDir().getAbsolutePath();
                    screen.wait(jarPath + "\\wechat-img\\video-toolBar.png", 5);
                    screen.click();
                } catch (Exception e1) {
                    WebElement webElement = driver.findElement(By.xpath("/Window[@Name='微信']//Pane/ToolBar//Pane/Button[@Name='视频号']"));
                    if (webElement != null && webElement.isDisplayed()) {
                        webElement.click();
                    } else {
                        throw new Exception("未找到视频号按扭");
                    }
                }
                driver.quit();
                if (weChatContext.getWindowHandle() != null) {
                    try {
                        windowsDriver = activateWindow();
                    } catch (Exception e2) {
                        windowsDriver = findWindow();
                        windowsDriver = activateWindow();
                    }
                } else {
                    windowsDriver = findWindow();
                    windowsDriver = activateWindow();
                }


                List<WebElement> webElements = windowsDriver.findElements(By.xpath("//Pane[@Name='微信']/Document//Group/Edit[@Name='搜索']"));
                if (webElements != null && webElements.size() > 0) {
                    webElements.get(0).click();
                    Thread.sleep(1000);
                    webElements.get(0).clear();
                    Thread.sleep(1000);
                    RobotUtil.keyPressString(weChatContext.getNickNameQueue().peek());
                    RobotUtil.keyClick(KeyEvent.VK_ENTER);
                    Thread.sleep(5000);
                    WebElement elements3 = windowsDriver.findElement(By.xpath("//Pane[@Name='微信']/Document/Image[1]"));
                    if (elements3 != null) {
                        elements3.click();
                        Thread.sleep(3000);
                        clearList();
                        List<WebElement> elements4 = windowsDriver.findElements(By.xpath("//Pane[@Name='微信']/Document//Text[@Name='直播中']"));
                        if (elements4 != null && elements4.size() > 0) {
                            Thread.sleep(3000);
                            elements4.get(0).click();
                            weChatContext.setCurrentNickName(weChatContext.getNickNameQueue().peek());
                            Thread.sleep(3000);
                            copyLink();
                        } else {
                            log.info("未找到直播中--" + weChatContext.getNickNameQueue().poll());
                        }
                    }
                } else {
                    log.info("未找到搜索框--" + weChatContext.getNickNameQueue().peek());
                    weChatContext.getNickNameQueue().poll();
                    weChatContext.setCurrentNickName(null);
                    weChatContext.setWindowHandle(null);
                }
            } catch (Exception e) {
                log.error("{} 账号自动化异常--" + weChatContext.getNickNameQueue().peek(), e);
                weChatContext.getNickNameQueue().poll();
                weChatContext.setCurrentNickName(null);
                weChatContext.setWindowHandle(null);
            }
        } else {
            log.info("正在自动化账号-- " + weChatContext.getCurrentNickName());
        }
    }

    private void clearList() throws FindFailed, IOException, InterruptedException {
        List<String> commandList = new ArrayList<>();
        commandList.add("cmd");
        commandList.add("/c");
        commandList.add(mitmPath);
        ProcessBuilder pb = new ProcessBuilder(commandList);
        pb.start();
        Thread.sleep(3000);
        Screen screen = new Screen();
        String jarPath = new ApplicationHome().getDir().getAbsolutePath();
        screen.wait(jarPath + "\\wechat-img\\clear-list.png", 3);
        screen.click();
    }

    private void copyLink() throws FindFailed, IOException, UnsupportedFlavorException, InterruptedException {
        Screen screen = new Screen();
        String jarPath = new ApplicationHome().getDir().getAbsolutePath();
        screen.wait(jarPath + "\\wechat-img\\copy-link.jpg", 30);
        screen.click();
        Thread.sleep(2000);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url", (String) clipboard.getData(DataFlavor.stringFlavor));
        jsonObject.put("nickname", weChatContext.getNickNameQueue().poll());
        log.info("上传请求参数 " + jsonObject.toJSONString());
        cn.hutool.http.HttpRequest.post(uploadUrl).header(Header.CONTENT_TYPE.name(), "application/json").header("Key", "qweasd123").body(jsonObject.toJSONString()).executeAsync();
        weChatContext.setCurrentNickName(null);
    }

    private WindowsDriver activateWindow() throws MalformedURLException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("appTopLevelWindow", weChatContext.getWindowHandle());
        return new WindowsDriver(new URL(appiumServer), capabilities);
    }

    private WindowsDriver findWindow() throws FindFailed, IOException, InterruptedException {
        WindowsDriver windowsDriver = null;
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("app", "Root");
        windowsDriver = new WindowsDriver(new URL(appiumServer), capabilities);
        windowsDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        WebElement webElement = windowsDriver.findElement(By.xpath("//Pane[@Name='微信']"));
        String newWindowHandle = Integer.toHexString(Integer.parseInt(webElement.getAttribute("NativeWindowHandle")));
        weChatContext.setWindowHandle(newWindowHandle);
        return windowsDriver;
    }
}
