package com.xiaoju.basetech.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: copy from com.didichuxing.chefuqa.common.util;
 * @time: 2019/12/24 9:11 PM
 */
public class CmdExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(CmdExecutor.class);

    private static AtomicInteger counter = new AtomicInteger(0);

    private static int maxThread = 64;

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    private static final String WORK_DIR = Paths.get(System.getProperty("user.home")).toString();

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(20, maxThread, 5 * 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1), r -> new Thread(r, "CmdThread-" + counter.getAndIncrement()));

    public static int executeCmd(String[] commands, Long timeout) throws Exception {
        StringBuffer ret = new StringBuffer();
        if (commands == null || commands.length == 0) {
            throw new IllegalArgumentException();
        }
        Process process = null;
        try {
            StringBuilder e = new StringBuilder();
            for (int builder = 0; builder < commands.length; ++builder) {
                e.append(commands[builder]);
                if (builder < commands.length - 1) {
                    e.append(" && ");
                }
            }
            LOG.info("CmdThreadPool:{}", executor);
            if (executor.getPoolSize() >= maxThread) {
                LOG.warn("CmdThreadPoolBusy");
            }

            LOG.info("executeCmd : bash -c " + e.toString());

//            ProcessBuilder var12 = new ProcessBuilder(new String[]{"bash", "-c", e.toString()});
//            ProcessBuilder var12 = new ProcessBuilder(new String[]{e.toString()});
//            var12.redirectErrorStream(true);
            process = executeChain(e.toString());
            CmdExecutor.ReadLine readLine = new CmdExecutor.ReadLine(process.getInputStream(), ret, true);
            Future readLineFuture = executor.submit(readLine);
            long begin = System.currentTimeMillis();
            if (process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
                LOG.info("readLine.stop();");
                readLine.setFlag(false);
                LOG.info("progressBar.stop();");
                LOG.info("executeCmd done !!!!!!");
                LOG.info("worker done !!!!!! times = " + (System.currentTimeMillis() - begin) / 1000L + "s");
                return process.exitValue();
            } else {
                throw new TimeoutException();
            }
        } catch (IOException | InterruptedException var10) {
            LOG.error("executeCmd builder.start(); IOException : ", var10);
            throw var10;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static class ReadLine implements Runnable {
        private static final Logger LOGGER = LoggerFactory.getLogger("commandOutputLogger");
        private final InputStream is;
        // private final StringBuffer sb;
        private volatile boolean flag;

        private ReadLine(InputStream is, StringBuffer sb, boolean flag) {
            this.is = is;
            // this.sb = sb;
            this.flag = flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.is));

            try {
                String line;
                try {
                    while (flag && (line = reader.readLine()) != null) {
                        String e = line.trim();
                        if (e.length() != 0) {
                            LOGGER.info(e);
                            // 这个sb的太大了，另外有啥用
                            //this.sb.append(e + System.getProperty("line.separator"));
                        }
                    }
                } catch (IOException var12) {
                    LOGGER.error("@@@@@@@@@@@@@@ ReadLine Thread, read IOException : ", var12);
                }
            } finally {
                try {
                    reader.close();
                } catch (IOException var11) {
                    LOGGER.error("@@@@@@@@@@@@@@ ReadLine Thread, close IOException : ", var11);
                }

            }

        }
    }


    public static Process executeChain(String commands) throws Exception {
        String workDir = WORK_DIR;

//        List<String> commandList = new ArrayList<>(Arrays.asList(commands.split("&&")));
        List<String> commandList = Arrays.stream(commands.split("&&")).map(String::trim).collect(Collectors.toList());
        if (commandList.get(0).contains("cd ")) {
            workDir = commandList.get(0).split(" ")[1];
            commandList.remove(0);
        }

        // 验证工作目录存在性
        Path workspace = Paths.get(workDir);
        if (!Files.exists(workspace)) {
            throw new FileNotFoundException("工作目录不存在: " + workspace.toAbsolutePath());
        }

        // 构建跨平台命令链
        String commandChain = String.join(" && ", commandList);
        List<String> shellCommand = IS_WINDOWS ?
                Arrays.asList("cmd.exe", "/c", commandChain.replaceAll("cp -rf", "xcopy /E /I /Y").replaceAll("rm -rf", "rmdir /S /Q")) :
                Arrays.asList("bash", "-c", commandChain);

        // 配置进程参数
        ProcessBuilder pb = new ProcessBuilder(shellCommand)
                .directory(workspace.toFile())
                .redirectErrorStream(true);

        // 启动进程
        System.out.printf("[%s] 执行命令链: %s%n",
                new Date(), String.join(" ", shellCommand));

        return pb.start();
    }
}