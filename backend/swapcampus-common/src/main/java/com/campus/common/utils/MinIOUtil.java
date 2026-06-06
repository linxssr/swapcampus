package com.campus.common.utils;

import com.campus.common.config.MinIOConfig;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
public class MinIOUtil {

    private final MinioClient minioClient;

    private final MinIOConfig minIOConfig;

    public MinIOUtil(MinioClient minioClient, MinIOConfig minIOConfig) {
        this.minioClient = minioClient;
        this.minIOConfig = minIOConfig;
    }

    public String upload(MultipartFile file, String directory) {
        try {
            ensureBucketExists();
            String objectName = buildObjectName(file.getOriginalFilename(), directory);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minIOConfig.getBucket())
                    .object(objectName)
                    .contentType(file.getContentType())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());
            // 返回相对路径，由前端代理转发，避免跨域问题
            return "/minio/" + minIOConfig.getBucket() + "/" + objectName;
        } catch (Exception exception) {
            throw new IllegalStateException("文件上传失败", exception);
        }
    }

    private void ensureBucketExists() throws Exception {
        String bucket = minIOConfig.getBucket();
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucket)
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucket)
                    .build());
        }
        // 设置公开读策略，让上传后的图片 URL 可以直接在浏览器访问，不需要鉴权
        String policy = "{"
                + "\"Version\":\"2012-10-17\","
                + "\"Statement\":[{"
                + "\"Effect\":\"Allow\","
                + "\"Principal\":{\"AWS\":[\"*\"]},"
                + "\"Action\":[\"s3:GetObject\"],"
                + "\"Resource\":[\"arn:aws:s3:::" + bucket + "/*\"]"
                + "}]}";
        minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                .bucket(bucket)
                .config(policy)
                .build());
    }

    private String buildObjectName(String originalFilename, String directory) throws IOException {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        return directory + "/" + UUID.randomUUID() + extension;
    }
}
