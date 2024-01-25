# AutoCacheMover

# 功能描述
- 监听一个目录，自动将目录中文件最后修改时间已经超过指定值的移动到指定目录，从而达到在nas上将固态硬盘里临时存在的文件(如：临时下载到固态硬盘)，移动到磁盘的功能
- 本项目目的是给不支持固态和磁盘混合存储的nas提供一个简单的基于文件夹的缓存管理

当前已实现功能
- 文件未修改指定时间之后，自动移动到指定的文件夹
- 支持多个文件夹一一对应移动


未来可能功能点
- 清理空文件夹
- 对指定的缓存文件夹，监听大小，当缓存所占超过指定阈值(如100g)，之后再触发上述修改时间自动移动动作


# 使用教程
- 必须挂载缓存根文件夹 /cache_root_folder ，然后可以在该跟文件夹下指定多个想要自动移动的文件夹
- /move_target_folder 为移动的目标文件夹，可以挂载多个，也可以一个，可以使用变量 move_target_folders 来指定你想要移动到哪个目录


| 变量                    | 含义                                                                      |
|-----------------------|-------------------------------------------------------------------------|
| cache_folders         | 缓存目录 多个目录用,分割                                                           |
| move_target_folders   | 移动目标目录，多个目录用,分割，和cache_folders 一一对应                                     |
| file_expired_minutes  | 文件缓存时间(文件最后修改时间多少分钟之后触发自动移动)，单位分钟，默认为5分钟，同样支持和前两个变量使用,号分割，实现不同文件夹缓存时间不同 |


举例

|Source (Host)|Destination (Container)|
|-|-|
|C:\Users\Downloads\test\src|/cache_root_folder|
|C:\Users\Downloads\test\target|/move_target_folder|



file_expired_minutes=1,3
cache_root_folder=/cache_root_folder 
cache_folders=/cache_root_folder/download1,/cache_root_folder/download2 
move_target_folders=/move_target_folder/download1,/move_target_folder/download2

表示自动将c盘的\Users\Downloads\test\src 目录下的download1 文件夹下的文件如果修改时间超过1分钟，就自动移动到\Users\Downloads\test\target下的download1文件夹
而对于download2 文件夹是修改时间超过3分钟就移动到download2
