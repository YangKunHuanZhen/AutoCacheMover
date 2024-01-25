package org.huanzhen.service;

import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.huanzhen.entity.FolderLink;

import java.io.File;
import java.util.*;

import static org.huanzhen.constant.Consts.*;
@Slf4j
public class AutoCacheMoverService {


    String[] cacheFolders;
    String[] targetFolders;

    Integer[] fileExpiredMinutes;
    Integer minExpireMinutes = 1;
    String cacheRootPath;
    ArrayList<FolderLink> folderLinks = new ArrayList<>();

    public AutoCacheMoverService() {
        Map<String, String> env = System.getenv();
        log.info("env -> [{}]",env.toString());
        this.cacheRootPath = env.get(CACHE_ROOT_FOLDER);
        Assert.notBlank("cache_root_folder is null");
        String cacheFolders = env.get(CACHE_FOLDERS);
        Assert.notBlank(cacheFolders, "cache_folders is null");
        String targetFolders = env.get(MOVE_TARGET_FOLDERS);
        Assert.notBlank(targetFolders, "move_target_folders is null");


        this.cacheFolders = cacheFolders.split(",");
        this.targetFolders = targetFolders.split(",");
        Assert.equals(this.cacheFolders.length, this.targetFolders.length, "cache_folders and move_target_folders length not same");

        initExpiredMinutes();

        for (String cacheFolder : this.cacheFolders) {
            Assert.isTrue(cacheFolder.contains(cacheRootPath), "cache_folders must be cache_root_folder`s sub folder");
        }

        buildFolderLinks();
    }

    private void initExpiredMinutes() {
        String fileExpiredMinutesS = System.getenv().getOrDefault(FILE_EXPIRED_MINUTES, "5");
        String[] split = fileExpiredMinutesS.split(",");
        this.fileExpiredMinutes = new Integer[split.length];
        for (int i = 0; i < split.length; i++) {
            this.fileExpiredMinutes[i] = Integer.parseInt(split[i]);
        }

        if (fileExpiredMinutes.length < cacheFolders.length) {
            fileExpiredMinutes = Arrays.copyOf(fileExpiredMinutes, cacheFolders.length);
        }

        Optional<Integer> min = Arrays.stream(fileExpiredMinutes)
                .filter(Objects::nonNull)
                .min(Integer::compareTo);
        this.minExpireMinutes = min.orElse(minExpireMinutes);
    }

    private void buildFolderLinks() {
        for (int i = 0; i < cacheFolders.length; i++) {
            folderLinks.add(new FolderLink(cacheFolders[i], targetFolders[i], fileExpiredMinutes[i]));
        }
    }


    public void start() throws Exception {
        FileAlterationObserver fileAlterationObserver = new FileAlterationObserver(new File(cacheRootPath));
        fileAlterationObserver.addListener(new AutoCacheMoverService.FileListener());
        fileAlterationObserver.initialize();
        FileAlterationMonitor monitor = new FileAlterationMonitor(minExpireMinutes * MIN_TO_MILLS);
        monitor.addObserver(fileAlterationObserver);
        monitor.start();

        removeExpiredFileToDisk();
    }

    private void removeExpiredFileToDisk() {
        while (true) {
            try {
                Thread.sleep(minExpireMinutes * MIN_TO_MILLS);
                for (FolderLink folderLink : folderLinks) {
                    folderLink.moveExpiredFiles();
                }
            } catch (Exception e) {
                log.error("moveExpiredFiles error", e);
            }
        }
    }


    private void addFile(File file) {
        for (FolderLink folderLink : folderLinks) {
            folderLink.addFile(file);
        }
    }

    private void delFile(File file) {
        for (FolderLink folderLink : folderLinks) {
            folderLink.delFile(file);
        }
    }

    class FileListener extends FileAlterationListenerAdaptor {
        @Override
        public void onFileCreate(File file) {
            addFile(file);
        }

        @Override
        public void onFileDelete(File file) {
            delFile(file);
        }


    }


}
