package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/upload")
public class UploadController {
    /**
     * 读取config目录下的application.properties文件中的FILE_SERVER_URL内容
     * 给下面的变量赋值, 这个是分布式文件系统服务器的IP地址
     */
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER;
    @RequestMapping("/uploadFile")
    public Result uploadFile(MultipartFile file) throws Exception {

        try {
            FastDFSClient fastdfs = new FastDFSClient("classpath:fastDFS/fdfs_client.conf");
            String path = fastdfs.uploadFile(file.getBytes(), file.getOriginalFilename(), file.getSize());
            return new Result(true,FILE_SERVER + path);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }
}
