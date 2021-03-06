package com.et.terminalserver.common.server;

import com.et.terminalserver.common.bus.BusListener;
import com.et.terminalserver.common.bus.BusManager;
import com.et.terminalserver.common.bus.Command;
import com.et.terminalserver.common.util.SpringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import java.util.concurrent.SynchronousQueue;

/**
 * @Description：
 * @Author：gaop
 * @Version: 1.0
 * @Date: 2017/8/28 9:55
 */
public class MainServerSupport implements Runnable{
    // 日志
    private final static Logger log = LogManager.getLogger(ServerSupport.class);

    public final static int SERVERSTART = 0;

    public final static int SERVERSTOP = -1;

    private final static String SERVERNAME = "mainserver";
    //一次只接收一个指令
    private final SynchronousQueue<Command> mainTreadQueue = new SynchronousQueue<Command>();

    private static boolean activeFlag = false;

    private Server mainServer;
    /**
     * @des 初始化
     */
    Object param;
    public MainServerSupport(Object param){
        this.param=param;
    }
    private boolean init(Object params) {

        log.info("system initializing ...");

        try {
            //次处改为在web.xmlzhong
//			if (System.getProperty("log4j2") != null) {
//
//				PropertyConfigurator.configure(System.getProperty("log4j2"));
//			}

//			if (System.getProperty("spring") != null)
//				SpringUtil.init(System.getProperty("spring"));
//			else
//				//激活spring 使配置文件生效
//			SpringUtil.init(null);
            ApplicationContext context= WebApplicationContextUtils.getWebApplicationContext((ServletContext)params);
           mainServer = (Server) context.getBean("mainserver");
            System.out.println(mainServer);
            if (mainServer.init(params)) {

                log.info("System initializing succeed. ");
            } else {
                log.error("System initializing failed. ");
                return false;
            }

        } catch (Exception e) {
            log.error("System initializing failed. ", e);
            return false;
        }

        return true;
    }



    private boolean start(Object params) {
        //先创建了server的一个通道
        BusManager.createConnect(SERVERNAME);
        //给这个通道增加监听器
        BusManager.registerListener(SERVERNAME, new BusListener() {
            @Override
            public void commandReceived(Command command, String connectName) {
                try {
                    mainTreadQueue.put(command);
                } catch (InterruptedException e) {
                    log.warn("Main server receive command failed ");
                }
            }
        });
        //激活这套监听
        BusManager.startConnect(SERVERNAME);
        boolean result = mainServer.start(params);
        log.info("System start succeed ");
        return result;
    }

    private boolean stop(Object param) {
        BusManager.stopConnect(SERVERNAME);
        mainServer.stop(param);
        SpringUtil.stop();
        log.info("System stop succeed ");
        return true;
    }

    private void holdProcess() {
        while (activeFlag) {
            Command command = null;
            try {
                command = mainTreadQueue.take();
            } catch (InterruptedException e) {
                log.warn("Main server receive command failed ");
            }
            if (command.getCode() == SERVERSTOP) {
                activeFlag = false;
                log.info("Main server receive STOPSERVER command ");
            }
        }
        System.exit(0);
    }

    public void process(Object param) {

    }

    public Server getMainServer() {
        return mainServer;
    }

    public void setMainServer(Server mainServer) {
        this.mainServer = mainServer;
    }

    @Override
    public void run() {
        final Object params=param;
        if (init(params)) {
            if (start(params)) {
                activeFlag = true;
                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    public void run() {
                        stop(params);
                    }
                }));
            }
            ;
        }
        holdProcess();
    }
}
