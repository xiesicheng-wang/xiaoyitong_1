package com.youlinbang.controller;

import com.youlinbang.common.exception.BusinessException;
import com.youlinbang.common.result.Result;
import io.minio.PutObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传：登录后可用，图片存 MinIO，返回可直接访问的 URL
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final MinioClient minioClient;

    @Value("${youlinbang.minio.endpoint}")
    private String endpoint;

    @Value("${youlinbang.minio.bucket}")
    private String bucket;

    @Value("${youlinbang.upload.max-size-mb}")
    private int maxSizeMb;

    @Value("${youlinbang.upload.allowed-types}")
    private Set<String> allowedTypes;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestPart("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("请选择要上传的文件");
        }
        if (file.getSize() > maxSizeMb * 1024L * 1024L) {
            throw BusinessException.badRequest("文件不能超过 " + maxSizeMb + "MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw BusinessException.badRequest("只支持图片格式: " + allowedTypes);
        }
        String ext = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "bin";
        };
        // 对象命名: products/202610/uuid.jpg —— 按月分目录便于管理
        String objectName = "products/%s/%s.%s".formatted(
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")),
                UUID.randomUUID().toString().replace("-", ""), ext);

        try (var in = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(in, file.getSize(), -1L)
                    .contentType(contentType)
                    .build());
        }
        String url = endpoint + "/" + bucket + "/" + objectName;
        return Result.ok(Map.of("url", url, "object", objectName));
    }
}
