package com.atguigu.gmall.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MyUploadUtil {

    public static String uploadImage(MultipartFile multipartFile)  {
        String url = "http://192.168.100.100";
        //获取配置文件的全局路径
        String path = MyUploadUtil.class.getClassLoader().getResource("tracker.conf").getPath();
        //初始化配置文件
        try {
            ClientGlobal.init(path);
            //通过trackerclient获取trackerServer连接
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer connection = trackerClient.getConnection();

            //通过connection让tracker给我们分配一个storage，null为随即分配
            StorageClient storageClient = new StorageClient(connection, null);

            //获取二进制文件后缀名
            byte[] bytes = multipartFile.getBytes();
            String originalFilename = multipartFile.getOriginalFilename();
            int i = originalFilename.lastIndexOf(".");
            String substring = originalFilename.substring(i + 1);

            //上传图片，结果为地址细分后的数组，还需要遍历组合成一个地址
            String[] jpgs = storageClient.upload_file(bytes, substring, null);
            for (String jpg : jpgs) {
                System.out.println(jpg+"---------");
                url += "/"+jpg;
                System.out.println(jpg+"1111111111");
            }

        }  catch (Exception e) {
            e.printStackTrace();
        }



        return  url;
    }
}
