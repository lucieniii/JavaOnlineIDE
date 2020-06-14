package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class ApiController {
    /**
     * @return a random file name
     */
    private String randomFileName() {
        String name;
        Random rand = new Random();
        int random = rand.nextInt();
        Calendar calCurrent = Calendar.getInstance();
        int intDay = calCurrent.get(Calendar.DATE);
        int intMonth = calCurrent.get(Calendar.MONTH) + 1;
        int intYear = calCurrent.get(Calendar.YEAR);
        String now = intYear + "_" + intMonth + "_" + intDay + "_";
        name = now + Math.abs(random);
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
        r1 = r1.strip();
        r2 = r2.strip();
        String[] s1 = r1.split("\\s+");
        String[] s2 = r2.split("\\s+");
        if (s1.length != s2.length)
            return "WA";
        else {
            for (int i = 0; i < s1.length; i++) {
                if (!s1[i].equals(s2[i]))
                    return "WA";
            }
        }
        r1 = r1.replace("\t", "");
        r1 = r1.replace(" ", "");
        r1 = r1.replace("\r", "");
        r2 = r2.replace("\t", "");
        r2 = r2.replace(" ", "");
        r2 = r2.replace("\r", "");
        if (r1.equals(r2))
            return "AC";
        else
            return "PE";
    }

    @Value("${spring.profiles.active}")
    private String env;

    private String[] exec(String command, long timeout, String stdin) {
        // 获取运行时
        Runtime runtime = Runtime.getRuntime();

        // 创建线程池，用于进行处理
        ExecutorService executor = Executors.newFixedThreadPool(1);

        // 使用Callable 判断任务完成时间
        Callable<String[]> call = () -> {
            // 放入耗时操作代码块，传入参数
            Process process = runtime.exec(command);
            OutputStream stdinStream = process.getOutputStream();
            PrintWriter printWriter = new PrintWriter(stdinStream, true);
            printWriter.print(stdin);
            printWriter.close();

            // 进行stdout和stderr读取
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

            //耗时代码块结束
            return new String[]{result, error};
        };

        String[] results;
        try {
            Future<String[]> future = executor.submit(call);
            // 判断运行时间，若超时则直接杀死进程
            results = future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            results = new String[]{"", "运行超时"};
        } catch (Exception e) {
            results = new String[]{"", e.getMessage()};
        }
        executor.shutdown();  // 关闭线程池

        return results;
    }

    @PostMapping("/run")
    ResponseEntity<Object> run(
            @RequestParam(value = "code", defaultValue = "") String code,
            @RequestParam(value = "language") String language,
            @RequestParam(value = "password", defaultValue = "") String password,
            @RequestParam(value = "stdin", defaultValue = "") String stdin,
            @RequestParam(value = "tle", defaultValue = "1") String tle,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        // 检测密码
        if (!password.equals("aikxNo.1")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        double timeout;
        try {
            timeout = Double.parseDouble(tle);
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        timeout = Double.max(1, Double.min(timeout, 30));

        // 解码code
        try {
            code = new URI(code).getPath();
            stdin = new URI(stdin).getPath();
        } catch (URISyntaxException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // 获取随机目录名
        String dir = randomFileName();
        String commandDir = env.equals("prod") ? "." : dir;

        // 根据语言生成对应文件、编译命令与执行命令
        String execFile;
        String compileCommand;
        String execCommand;
        switch (language) {
            case "python3": {
                execFile = "main.py";
                compileCommand = "";
                execCommand = String.format("python %s/main.py", commandDir);
                break;
            }
            case "g++": {
                execFile = "main.cpp";
                compileCommand = String.format("g++ %s/main.cpp -o %s/main", commandDir, commandDir);
                execCommand = commandDir + "/main";
                break;
            }
            case "gcc": {
                execFile = "main.c";
                compileCommand = String.format("g++ %s/main.c -o %s/main", commandDir, commandDir);
                execCommand = commandDir + "/main";
                break;
            }
            case "java11": {
                execFile = "Main.java";
                compileCommand = String.format("javac %s/Main.java", commandDir);
                execCommand = env.equals("prod") ? "java Main" : String.format("java -classpath %s Main", dir);
                break;
            }
            case "node": {
                execFile = "main.js";
                compileCommand = "";
                execCommand = String.format("node %s/main.js", commandDir);
                break;
            }
            default: {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        if (env.equals("prod")) {
            compileCommand = String.format(
                    "docker run --rm -v /root/JavaOnlineIDE/%s:/home/runner -w /home/runner onlineide:test %s",
                    dir, compileCommand
            );
            execCommand = String.format(
                    "docker run --rm -v /root/JavaOnlineIDE/%s:/home/runner -w /home/runner onlineide:test %s",
                    dir, execCommand
            );
        }

        // 创建临时文件夹
        File f = new File(dir);
        f.mkdir();

        // 将code和stdin写入文件
        try {
            BufferedWriter codeWriter = new BufferedWriter(new FileWriter(dir + "/" + execFile));
            codeWriter.write(code);
            codeWriter.close();
        } catch (IOException e) {
            // 删除临时文件夹
            File[] subs = f.listFiles();
            if (subs != null) {
                for (File sub : subs) {
                    sub.delete();
                }
                f.delete();
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Map<String, String> ret = new HashMap<>();

        String compileError;
        if (!compileCommand.equals("")) {
            String[] compileResults = exec(compileCommand, 5000, "");
            System.out.println("编译结果");
            System.out.println("stdout:\n" + compileResults[0]);
            System.out.println("stderr:\n" + compileResults[1]);
            compileError = compileResults[1];
        } else {
            compileError = "";
        }

        String result = null;
        if (compileError.equals("")) {
            System.out.println("开始执行");
            String[] execResults = exec(execCommand, Math.round(timeout * 1000), stdin);
            System.out.println("执行结果");
            System.out.println("stdout:\n" + execResults[0]);
            System.out.println("stderr:\n" + execResults[1]);
            ret.put("result", execResults[0]);
            ret.put("error", execResults[1]);
            if (execResults[1].equals("")) {
                result = execResults[0];
            }
        } else {
            ret.put("error", compileError);
        }

        // 删除临时文件夹
        File[] subs = f.listFiles();
        if (subs != null) {
            for (File sub : subs) {
                sub.delete();
            }
            f.delete();
        }

        try {
            String resultOut = new String(file.getBytes());
            if (result != null) {
                String judgeResult = judgeOutput(result, resultOut);
                System.out.println("judge结果：" + judgeResult);
                ret.put("judge", judgeResult);
            }
        } catch (Exception e) {
            System.out.println("judge：未上传比对文件或运行错误");
        }

        System.out.println("--------------------------------------------------");

        return new ResponseEntity<>(ret, HttpStatus.OK);
    }
}
