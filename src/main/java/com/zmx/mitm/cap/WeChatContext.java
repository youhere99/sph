package com.zmx.mitm.cap;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author zhaomingxing
 * @date 2024/11/22 17:48
 * @描述
 */
@Slf4j
@Getter
@Setter
@Component
@Scope("singleton")
public class WeChatContext {

    private  String windowHandle;
    private  boolean flag;
    private  String currentNickName;
    private Queue<String> nickNameQueue = new LinkedList<>();;

}
