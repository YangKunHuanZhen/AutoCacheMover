package org.huanzhen;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.huanzhen.service.AutoCacheMoverService;

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.PriorityQueue;


public class Application {

    public static void main(String[] args) throws Exception {
        AutoCacheMoverService autoCacheMoverService = new AutoCacheMoverService();
        autoCacheMoverService.start();
    }


}