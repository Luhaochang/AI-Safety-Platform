package com.naic.productionline.service.minio;

import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * <p>
 * 资源上传服务类
 * </p>*
 * @author wangyunong
 * @data 2023/10/24
 */
public interface ResourcesService {

    /**
     * putFile上传文件到minio中
     *
     * @param file 文件
     * @throws  IOException
     * @throws ServerException
     * @throws InsufficientDataException
     * @throws InternalException
     * @throws InvalidResponseException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws XmlParserException
     * @throws ErrorResponseException
     * @return urlPath
     */
    String putFile(MultipartFile file) throws IOException, ServerException, InsufficientDataException, InternalException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, XmlParserException, ErrorResponseException;

    /**
     * putFile上传文件到minio中
     *
     * @param file 文件
     * @throws  IOException
     * @throws ServerException
     * @throws InsufficientDataException
     * @throws InternalException
     * @throws InvalidResponseException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws XmlParserException
     * @throws ErrorResponseException
     * @return urlPath
     */
    String updateFile(MultipartFile file,String markUrl) throws IOException, ServerException, InsufficientDataException, InternalException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, XmlParserException, ErrorResponseException;

    /**
     * uploadDataset上传文件到minio中
     *
     * @param file 文件
     * @param catalogue 目录
     * @throws  IOException
     * @throws ServerException
     * @throws InsufficientDataException
     * @throws InternalException
     * @throws InvalidResponseException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws XmlParserException
     * @throws ErrorResponseException
     * @return urlPath
     */
    String uploadDataset(MultipartFile file,String catalogue) throws IOException, ServerException, InsufficientDataException, InternalException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, XmlParserException, ErrorResponseException;


    /**
     * uploadData获取文件
     *
     * @param catalogue 目录
     * @throws  IOException
     * @throws ServerException
     * @throws InsufficientDataException
     * @throws InternalException
     * @throws InvalidResponseException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws XmlParserException
     * @throws ErrorResponseException
     * @return urlPath
     */
    List<String> getDatasetFile(String catalogue) throws IOException, ServerException, InsufficientDataException, InternalException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, XmlParserException, ErrorResponseException;

    /**
     * 删除Dataset文件
     *
     * @param markFile 标注文件
     * @param catalogue 所要删除的文件路径
     * @param datasetId 数据集id
     * @throws  IOException
     * @throws ServerException
     * @throws InsufficientDataException
     * @throws InternalException
     * @throws InvalidResponseException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws XmlParserException
     * @throws ErrorResponseException
     */
    void deleteDatasetFile(String catalogue,MultipartFile markFile,Long datasetId) throws IOException, ServerException, InsufficientDataException, InternalException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, XmlParserException, ErrorResponseException;

    /**
     * 压缩dataset文件
     *
     * @param datasetId 数据集id
     * @throws  Exception
     */
    void zipDataset(Long datasetId) throws Exception;

    /**
     * 获取dataset压缩文件url
     *
     * @param datasetId 数据集id
     * @return  压缩包地址
     */
    String getPath(Long datasetId);
}
