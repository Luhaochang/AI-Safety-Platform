package com.naic.api.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

public class OssUtils {

    private static OSS ossClient;
    private static final String endpoint = "http://oss-cn-beijing.aliyuncs.com";

    // accessKeyId和accessKeySecret是OSS的访问密钥，您可以在控制台上创建和查看，
    // 创建和查看访问密钥的链接地址是：https://ak-console.aliyun.com/#/。
    // 注意：accessKeyId和accessKeySecret前后都没有空格，从控制台复制时请检查并去除多余的空格。
    private static final String accessKeyId = "YOUR_ACCESS_KEY_ID";
    private static final String accessKeySecret = "YOUR_ACCESS_KEY_SECRET";

    //Bucket用来管理所存储Object的存储空间
    //Bucket命名规范如下：只能包括小写字母，数字和短横线（-），必须以小写字母或者数字开头，长度必须在3-63字节之间。
    private static final String bucketName = "naic";

    /**
     * OSS 初始化
     * @return OSS客户端对象
     */
    public static OSS getClient(){
        if(ossClient == null) {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        }
        return ossClient;
    }

    /**
     * 图片上传
     * @param multipartFile 文件（图片）
     * @return String 图片code
     */
    public static String saveImg(MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        String fileCode = UUID.randomUUID() +fileName.substring(fileName.lastIndexOf('.'));
        try {
            InputStream inputStream=multipartFile.getInputStream();
            getClient().putObject(bucketName, fileCode, inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileCode;
    }

    /**
     * 根据图片code从OSS中获取图片url
     * @param code 图片code
     * @return 图片url
     */
    public static String getImgUrl(String code) {
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
        // 生成以GET方法访问的签名URL，访客可以直接通过浏览器访问相关内容。
        URL url = getClient().generatePresignedUrl(bucketName, code, expiration);
        return url.toString();
    }

    public static void deleteFile(String code){
        getClient().deleteObject(bucketName,code);
    }
}
