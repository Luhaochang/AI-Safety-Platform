package com.naic.service.minio.impl;

import com.naic.config.minio.MinioConfig;
import com.naic.service.minio.ResourcesService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * <p>
 * 资源上传实现类
 * </p>*
 * @author wangyunong
 * @data 2023/10/26
 */
@Service
@AllArgsConstructor
public class ResourcesServiceImpl implements ResourcesService {

    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioConfig minioConfig;

    @Override
    public String putFile(MultipartFile file) throws IOException, ServerException, InsufficientDataException, InternalException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, XmlParserException, ErrorResponseException {

        if(file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空!!!");
        }

        //文件名
        String originalFilename = file.getOriginalFilename();
        //文件流
        @Cleanup
        InputStream inputStream = file.getInputStream();
        //文件大小
        long size = file.getSize();

        //文件后缀.jpg
        String suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 生成文件唯一性标识
        String fileNewId = UUID.randomUUID().toString();

        String fileUrl= fileNewId + suffixName;

        //存储方法 putObject
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioConfig.getBucket())
                .object(fileUrl)
                .stream(inputStream, size, -1)
                .contentType(file.getContentType())
                .build());

        String urlPath = minioConfig.getDownloadPath() + fileUrl;

        return urlPath;
    }

}
