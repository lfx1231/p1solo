package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {
    //读取application.properties配置文件中的路径url内容
    // FILE_SERVER_URL=http://192.168.200.128/
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER;

    @RequestMapping("/uploadFile")
    public Result uploadFile(@RequestBody MultipartFile file) throws Exception {

        try {
            FastDFSClient dfs = new FastDFSClient("classpath:fastDFS/fdfs_client.conf");
            String path = dfs.uploadFile(file.getBytes(), "jpg", file.getSize());
            return new Result(true,FILE_SERVER+path);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }


    }


}
