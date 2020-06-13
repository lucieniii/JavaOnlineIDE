package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    private String randomFileName() {
        String name = "";
        Random rand = new Random();
        int random = rand.nextInt();
        Calendar calCurrent = Calendar.getInstance();
        int intDay = calCurrent.get(Calendar.DATE);
        int intMonth = calCurrent.get(Calendar.MONTH) + 1;
        int intYear = calCurrent.get(Calendar.YEAR);
        String now = String.valueOf(intYear) + "_" + String.valueOf(intMonth) + "_" + String.valueOf(intDay) + "_";
        name = now + String.valueOf(Math.abs(random));
        return name;
    }

    @GetMapping("/run")
    ResponseEntity<Object> run(
            @RequestParam(value = "code", defaultValue = "") String code,
            @RequestParam(value = "language") String language,
            @RequestParam(value = "password", defaultValue = "") String password,
            @RequestParam(value = "consoleInput", defaultValue = "") String consoleInput,
            @RequestParam(value = "option", defaultValue = "") String option,
            @RequestParam(value = "tle", defaultValue = "0") String tle,
            @RequestParam(value = "mle", defaultValue = "0") String mle
    ) {
        System.out.println(tle + ' ' + mle);
        // 检测密码
        if (!password.equals("aikxNo.1")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // 解码code
        try {
            code = new URI(code).getPath();
        } catch (URISyntaxException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // 获取随机文件名
        String filename = randomFileName();

        // 将code写入文件
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            out.write(code);
            out.close();
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 生成命令
        String command;
        switch (language) {
            case "python3.6": {
                command = String.format("python %s", filename);
                break;
            }
            case "g++": {
                command = String.format("g++ %s -o %s.out && %s.out", filename, filename, filename);
                break;
            }
            case "gcc": {
                command = String.format("gcc %s -o %s.out && %s.out", filename, filename, filename);
                break;
            }
            case "java8": {
                filename = "Main";
                command = String.format("javac %s && java %s", filename, filename);
                break;
            }
            case "java12": {
                System.out.println("1");
                command = "2";
                break;
            }

//            case "java13": {
//                command = "";
//                break;
//            }
//            case "javascript": {
//                command = "";
//                break;
//            }
//
            default: {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println(df.format(new Date()));// new Date()为获取当前系统时间


        // 执行命令
        ResponseEntity<Object> ret;
        Runtime runtime = Runtime.getRuntime();
        String[] results = new String[2];
        String result;

        // 创建线程池，用于进行处理
        ExecutorService exec = Executors.newFixedThreadPool(1);

        // 使用Callable 判断任务完成时间
        Callable<String[]> call = new Callable<String[]>() {
            public String[] call() throws Exception {
                // 放入耗时操作代码块
                System.out.println(command);
                Process process = runtime.exec(command);

                // 进行文件读取
                InputStream resultStream = process.getInputStream();
                InputStream errorStream = process.getErrorStream();
                InputStreamReader resultReader = new InputStreamReader(resultStream);
                InputStreamReader errorReader = new InputStreamReader(errorStream);
                BufferedReader resultBR = new BufferedReader(resultReader);
                BufferedReader errorBR = new BufferedReader(errorReader);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = resultBR.readLine()) != null) {
                    sb.append(line);
                    sb.append('\n');
                }
                String result = sb.toString();
                sb.delete(0, sb.length());
                while ((line = errorBR.readLine()) != null) {
                    sb.append(line);
                    sb.append('\n');
                }
                String error = sb.toString();
                System.out.println(result);
                System.out.println(error);

                //耗时代码块结束
                return new String[]{result, error};
            }
        };

        try {
            Future<String[]> future = exec.submit(call);

            // 判断运行时间，若超时则直接杀死进程
            results = future.get(1000 + 1000 * Integer.parseInt(tle), TimeUnit.MILLISECONDS); // 任务处理超时时间设为1 秒

            System.out.println("任务成功返回:");
            ret = new ResponseEntity<>(
                    Map.of("result", results[0], "error", results[1]),
                    HttpStatus.OK
            );
            // Map + 状态码
            // System.out.println(((Map<String, String>)ret.getBody()).get("result"));

        } catch (TimeoutException ex) {
            System.out.println("处理超时");
            System.exit(0);
            ret = new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT);
        } catch (Exception e) {
            System.out.println("处理失败.");
            e.printStackTrace();
            ret = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        exec.shutdown();  // 关闭线程池

        // 删除临时文件
        File file = new File(filename);
        file.delete();
        switch (language) {
            case "python3.6": {
                break;
            }
            case "g++": {
                file = new File(filename + ".out");
                file.delete();
                break;
            }
            default: {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }



        //python2.7
        //python3.6
        //c
        //c++
        //java
        //javascript

        // 头文件限制、控制台输入、多文件、
        // 时间检测、空间检测、文件比对

        return ret;
    }
}
