package com.naic.productionline.service.minio.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.naic.api.api.ServiceException;
import com.naic.productionline.config.MinioConfig;
import com.naic.productionline.domain.po.DataSetPO;
import com.naic.productionline.domain.po.DataSetTaskPO;
import com.naic.productionline.mapper.DataSetMapper;
import com.naic.productionline.mapper.DataSetTaskMapper;
import com.naic.productionline.service.minio.ResourcesService;
import com.naic.productionline.utils.ZipUpload;
import io.minio.*;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import io.minio.messages.Item;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private final MinioClient minioClient;

    private final MinioConfig minioConfig;

    private final DataSetMapper dataSetMapper;

    private final DataSetTaskMapper dataSetTaskMapper;

    private final ZipUpload zipUpload;
    @Override
    public String putFile(MultipartFile file) throws IOException, ServerException, InsufficientDataException, InternalException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, XmlParserException, ErrorResponseException {

        if(file.isEmpty()) {
            throw new ServiceException("文件不能为空!!!");
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

    @Override
    public String updateFile(MultipartFile file,String markUrl) throws IOException, ServerException, InsufficientDataException, InternalException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, XmlParserException, ErrorResponseException {
        if(file.isEmpty()) {
            throw new ServiceException("文件不能为空!!!");
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
        if(markUrl==null|| "".equals(markUrl)) {
            throw new ServiceException("标注文件路径为空或不存在");
        }
        //存储方法 putObject
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioConfig.getBucket())
                .object(markUrl)
                .stream(inputStream, size, -1)
                .contentType(file.getContentType())
                .build());

        return markUrl;
    }

    @Override
    public String uploadDataset(MultipartFile file, String catalogue) throws IOException, ServerException, InsufficientDataException, InternalException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, XmlParserException, ErrorResponseException {
        if(file.isEmpty()) {
            throw new ServiceException("文件不能为空!!!");
        }

        //文件名
        String filename = file.getOriginalFilename();

        //文件流
        @Cleanup
        InputStream inputStream = file.getInputStream();
        //文件大小
        long size = file.getSize();

        String fileUrl=catalogue+"/"+filename;

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

    @Override
    public List<String> getDatasetFile(String catalogue) throws IOException, ServerException, InsufficientDataException, InternalException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, XmlParserException, ErrorResponseException {
        // 列出存储桶中文件夹内的所有文件
        List<String> filePaths = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(minioConfig.getBucket()).prefix(catalogue).recursive(true).build());

        for (Result<Item> result : results) {
            Item item = result.get();
            // 获取文件的全路径并添加到列表中
            filePaths.add(item.objectName());
        }
        List<String> resultPath = new ArrayList<>();
        for (String filePath:filePaths){
            resultPath.add(minioConfig.getDownloadPath()+filePath);
        }
        return resultPath;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDatasetFile(String catalogue, MultipartFile markFile,Long datasetId) throws IOException, ServerException, InsufficientDataException, InternalException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, XmlParserException, ErrorResponseException {

        if(markFile.isEmpty()) {
            throw new ServiceException("标注文件不能为空!!!");
        }
        //文件名
        String originalFilename = markFile.getOriginalFilename();
        if(!originalFilename.substring(originalFilename.lastIndexOf('.')).equals(".json")){
            throw new ServiceException("标注文件格式错误!!!");
        }

        DataSetPO dataSetPO=dataSetMapper.selectById(datasetId);
        if(dataSetPO.getIsChecked().equals(1)){
            throw new ServiceException("该数据已经校验完成无法修改!!!");
        }

        // 创建RemoveObjectArgs对象
        RemoveObjectArgs args = RemoveObjectArgs.builder()
                .bucket(minioConfig.getBucket())
                .object(catalogue)
                .build();
        // 删除指定的文件
        minioClient.removeObject(args);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void zipDataset(Long datasetId) throws Exception {
        DataSetPO dataset=dataSetMapper.selectById(datasetId);
        if(!dataset.getIsPackage().equals(0)){
            throw new ServiceException("数据集已经打包或者正在打包");
        }
        if(dataset.getIsChecked().equals(0)){
            throw new ServiceException("数据集未校验，无法打包");
        }
        if(!dataset.getIsMarked().equals(2)){
            throw new ServiceException("数据集未标注完成，无法打包");
        }
        List<String> fileList=getDatasetFile(dataset.getFileUrl());
        dataset.setIsPackage(2);
        dataSetMapper.updateById(dataset);
        zipUpload.zipSet(dataset.getFileUrl()+".zip",fileList,dataset);
    }

    @Override
    public String getPath(Long datasetId) {
        DataSetPO dataset=dataSetMapper.selectById(datasetId);
        if(dataset.getIsPackage().equals(0)){
            throw new ServiceException("数据集未打包");
        }
        return minioConfig.getDownloadPath()+"zip/"+dataset.getFileUrl()+".zip";
    }

}
