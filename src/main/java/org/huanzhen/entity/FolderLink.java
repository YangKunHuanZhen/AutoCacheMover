package org.huanzhen.entity;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * 一个cache 对应于一个目标目录
 */
@Slf4j
public class FolderLink {

    private String cachePath;
    private String targetPath;

    private long size;

    private Integer fileExpiredMinutes = 5;


    PriorityQueue<File> filesQueue = new PriorityQueue<>(Comparator.comparing(File::lastModified));

    public FolderLink(String cachePath, String targetPath, Integer fileExpiredMinutes) {
        this.cachePath = cachePath;
        this.targetPath = targetPath;
        Assert.notBlank(this.cachePath);
        Assert.notBlank(this.targetPath);
        if (fileExpiredMinutes != null) {
            this.fileExpiredMinutes = fileExpiredMinutes;
        }

        addAllFileToQueues(new File(cachePath));
    }

    private void addAllFileToQueues(File file) {
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                addAllFileToQueues(subFile);
            }
        } else {
            filesQueue.add(file);
        }
    }

    public void moveExpiredFiles() {
        if (filesQueue.isEmpty()) {
            log.debug("cachePath -> [{}] queues is empty", this.cachePath);
            return;
        }

        while (!filesQueue.isEmpty() && isExpired(filesQueue.peek())) {
            File poll = filesQueue.poll();
            if (poll != null && poll.exists()) {
                long lastModified = poll.lastModified();
                String path = poll.getPath();
                String subPath = path.substring(path.indexOf(cachePath) + cachePath.length());
                String newPath = targetPath + subPath;
                FileUtil.move(poll.toPath(), Path.of(newPath), false);
                log.info("file -> [{}] lastModified -> [{}] moved to -> [{}]", path, LocalDateTimeUtil.of(lastModified).toString(), newPath);
            }
        }
    }

    private boolean isExpired(File file) {
        return file != null && file.lastModified() <= System.currentTimeMillis() - fileExpiredMinutes * 60 * 1000;
    }

    public boolean addFile(File file) {
        String path = file.getPath();
        if (path.startsWith(cachePath)) {
            filesQueue.add(file);
            log.info("cachePath -> [{}] add file path -> [{}]", cachePath, path);
            return true;
        }
        return false;
    }

    public boolean delFile(File file) {
        String path = file.getPath();
        if (path.startsWith(cachePath)) {
            filesQueue.remove(file);
            log.info("cachePath -> [{}] remove file path -> [{}]", cachePath, path);
            return true;
        }
        return false;
    }
}
