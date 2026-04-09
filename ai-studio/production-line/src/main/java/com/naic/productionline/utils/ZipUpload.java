package com.naic.productionline.utils;

import com.naic.productionline.config.MinioConfig;
import com.naic.productionline.domain.po.DataSetPO;
import com.naic.productionline.mapper.DataSetMapper;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author wyn
 */
@Slf4j
@Component
@AllArgsConstructor
public class ZipUpload {

    private final MinioClient minioClient;
    // 压缩包所在的文件夹路径
    private static final String PREFIX_NAME = "zip";

    private final MinioConfig minioConfig;

    private final DataSetMapper dataSetMapper;

    @Async
    public void zipSet(String zipFilePath, List<String> fileList, DataSetPO dataset) throws Exception{
        try {
            createZipFile(zipFilePath, fileList);
            //上传压缩包到MinIO
            try (InputStream inputStream = new FileInputStream(zipFilePath)) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(minioConfig.getBucket())
                        .object(PREFIX_NAME + "/" + zipFilePath)
                        .stream(inputStream, inputStream.available(), -1)
                        .contentType("application/zip")
                        .build());
            }
            // 删除本地压缩包文件
            File zipFile = new File(zipFilePath);
            if (zipFile.exists()) {
                zipFile.delete();
            }
            dataset.setIsPackage(1);
            dataSetMapper.updateById(dataset);
        }catch (Exception e){
            dataset.setIsPackage(0);
            dataSetMapper.updateById(dataset);
            e.printStackTrace();
        }
    }

    private void createZipFile(String zipFilePath, List<String> fileList) throws Exception {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            for (String fileName : fileList) {
                String[] fileNameNew=fileName.split(minioConfig.getEndpoint()+"/"+minioConfig.getBucket()+"/");
                GetObjectArgs getObjectArgs=GetObjectArgs.builder()
                        .bucket(minioConfig.getBucket())
                        .object(fileNameNew[fileNameNew.length-1])
                        .build();
                try (InputStream inputStream = minioClient.getObject(getObjectArgs)) {
                    zipOut.putNextEntry(new ZipEntry(fileNameNew[fileNameNew.length-1]));
                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        zipOut.write(buffer, 0, length);
                    }
                    zipOut.closeEntry();
                }
            }
        }
    }

}
