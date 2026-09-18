//package com.melody.web.controller;
//
//import com.melody.web.service.AlarmEmailService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//public class SkyWalkingAlarmController {
//
//    private final AlarmEmailService alarmEmailService;
//
//    @PostMapping("/skywalking/alarm")
//    public String receiveAlarm(@RequestBody List<Map<String, Object>> alarms) {
//        log.warn("收到SkyWalking告警: {}", alarms);
//
//        for (Map<String, Object> alarm : alarms) {
//            String scope = (String) alarm.getOrDefault("scope", "");
//            String name = (String) alarm.getOrDefault("name", "");
//            String alarmMessage = (String) alarm.getOrDefault("alarmMessage", "");
//            Long startTime = (Long) alarm.get("startTime");
//
//            StringBuilder content = new StringBuilder();
//            content.append("告警范围: ").append(scope).append("\n");
//            content.append("告警对象: ").append(name).append("\n");
//            content.append("告警内容: ").append(alarmMessage).append("\n");
//            content.append("触发时间: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//                    .format(new java.util.Date(startTime != null ? startTime : System.currentTimeMillis())));
//
//            log.warn("处理告警: {}", content);
//
//            try {
//                alarmEmailService.sendAlarm("MelodyTicket 系统告警", content.toString());
//            } catch (Exception e) {
//                log.error("发送告警邮件失败", e);
//            }
//        }
//
//        return "ok";
//    }
//}
