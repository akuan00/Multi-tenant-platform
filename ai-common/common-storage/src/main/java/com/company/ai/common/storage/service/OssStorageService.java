package com.company.ai.common.storage.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.company.ai.common.storage.config.OssConfig;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssStorageService {

    private final OssConfig ossConfig;
    private OSS ossClient;

    private OSS getClient() {
        if (ossClient == null) {
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );
        }
        return ossClient;
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    public String upload(MultipartFile file, String directory, String appId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectKey = directory + "/" + appId + "/" + UUID.randomUUID() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            getClient().putObject(ossConfig.getBucketName(), objectKey, inputStream, metadata);
        }
        return objectKey;
    }

    public String generatePresignedUrl(String objectKey, int expirationMinutes) {
        long expirationMillis = System.currentTimeMillis() + (long) expirationMinutes * 60 * 1000;
        return getClient().generatePresignedUrl(ossConfig.getBucketName(), objectKey, new java.util.Date(expirationMillis)).toString();
    }

    public void delete(String objectKey) {
        getClient().deleteObject(ossConfig.getBucketName(), objectKey);
    }
}
