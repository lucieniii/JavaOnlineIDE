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
    /**
     * 
     * @return a random file name
     */
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

    /***
     * 
     * @param r1:String 
     * @param r2:String
     * @return result
     * @Author aikx
     * @Date 2020-6-10
     */
    private String judgeOutput(String r1, String r2) {
        String[] s1 = r1.split("\\s+");
        String[] s2 = r2.split("\\s+");
        if(s1.length != s2.length)
            return "WA";
        else
        {
            for(int i = 0; i < s1.length; i++)
            {
                if(s1[i] != s2[i])
                    return "WA";
            }
        }
        r1 = r1.replace("\t", "");
        r1 = r1.replace(" ", "");
        r1 = r1.replace("\r", "");
        r2 = r2.replace("\t", "");
        r2 = r2.replace(" ", "");
        r2 = r2.replace("\r", "");
        if(r1.equals(r2))
            return "AC";
        else
            return "PE";

    }
    
    @GetMapping("/run")
    ResponseEntity<Object> run(
            @RequestParam(value = "code", defaultValue = "") String code,
            @RequestParam(value = "language") String language,
            @RequestParam(value = "password", defaultValue = "") String password,
            @RequestParam(value = "consoleInput", defaultValue = "") String consoleInput,
            @RequestParam(value = "option", defaultValue = "") String option,
            @RequestParam(value = "tle", defaultValue = "1") String tle,
            @RequestParam(value = "mle", defaultValue = "65534") String mle
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

        // 生成命令
        String command;
        switch (language) {
            case "python3.6": {
                filename += ".py";
                command = String.format("python %s", filename);
                break;
            }
            case "g++": {
                filename += ".cpp";
                command = String.format("cmd /c g++ %s -o %s.out && %s.out", filename, filename, filename);
                break;
            }
            case "gcc": {
                filename += ".c";
                command = String.format("cmd /c gcc %s -o %s.out && %s.out", filename, filename, filename);
                break;
            }
            case "java8": {
                filename = "Main.java";
                command = String.format("cmd /c javac %s && java Main", filename);
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

        // 将code写入文件
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            out.write(code);
            out.close();
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println(df.format(new Date()));// new Date()为获取当前系统时间


        // 执行命令
        ResponseEntity<Object> ret;
        Runtime runtime = Runtime.getRuntime();
        String[] results = new String[2];
        String result = null;

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
            // 最大运行30s
            results = future.get(5000 + 1000 * Math.min(Integer.parseInt(tle), 30), TimeUnit.MILLISECONDS); // 任务处理超时时间设为1 秒
            System.out.println("任务成功返回:");
            System.out.println(results[0] + " " + results[1]);
            ret = new ResponseEntity<>(
                    Map.of("result", results[0], "error", results[1]),
                    HttpStatus.OK
            );
            // Map + 状态码
            // System.out.println(((Map<String, String>)ret.getBody()).get("result"));
        } catch (TimeoutException ex) {
            System.out.println("处理超时");
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
            case "gcc":
            case "g++": {
                file = new File(filename + ".out");
                file.delete();
                break;
            }
            case "java8":{
                file = new File("Main.class");
                file.delete();
                break;
            }
            default: {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

//        String filenameOut = "";
//        String resultOut;
//        try{
//            InputStream in = new FileInputStream(filenameOut);
//            InputStreamReader outReader = new InputStreamReader(in);
//            BufferedReader outBR = new BufferedReader(outReader);
//            StringBuilder sb = new StringBuilder();
//            String line;
//            while ((line = outBR.readLine()) != null) {
//                sb.append(line);
//                sb.append('\n');
//            }
//            resultOut = sb.toString();
//
//        }catch (Exception e) {
//            System.out.println(e.getMessage());
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//
//        String judgeResult = judgeOutput(result, resultOut);
//        System.out.println(judgeResult);

        String[] py_ban_list = {"os",
                                "sys",
                                "urllib",
                                ""};
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
