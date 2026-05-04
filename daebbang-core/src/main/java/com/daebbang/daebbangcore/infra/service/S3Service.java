package com.daebbang.daebbangcore.infra.service;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.ImageErrorCode;
import com.daebbang.daebbangcore.infra.storage.UploadFile;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int UUID_LENGTH = 16;

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(UploadFile file, String directory) {
        validate(file);

        String extension = extractExtension(file.originalFilename()).toLowerCase();
        String key = buildKey(directory, extension);

        try (InputStream input = new ByteArrayInputStream(file.bytes())) {
            ObjectMetadata metadata = ObjectMetadata.builder()
                .contentType(file.contentType())
                .contentLength((long) file.bytes().length)
                .build();

            S3Resource resource = s3Template.upload(bucket, key, input, metadata);
            return resource.getURL().toString();
        } catch (Exception e) {
            log.error("[S3 업로드 오류] key={}, message={}", key, e.getMessage(), e);
            throw new BusinessException(ImageErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        String key = extractKey(imageUrl);
        try {
            s3Template.deleteObject(bucket, key);
        } catch (Exception e) {
            log.error("[S3 삭제 오류] key={}, message={}", key, e.getMessage(), e);
            throw new BusinessException(ImageErrorCode.IMAGE_DELETE_FAILED);
        }
    }

    public void deleteAll(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        for (String url : imageUrls) {
            try {
                delete(url);
            } catch (BusinessException e) {
                log.warn("[S3 일괄 삭제 일부 실패] url={}", url);
            }
        }
    }

    private void validate(UploadFile file) {
        if (file == null || file.bytes() == null || file.bytes().length == 0) {
            throw new BusinessException(ImageErrorCode.IMAGE_EMPTY);
        }

        String extension = extractExtension(file.originalFilename()).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ImageErrorCode.IMAGE_INVALID_TYPE);
        }
    }

    private String buildKey(String directory, String extension) {
        String date = LocalDate.now().format(DATE_FORMAT);
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, UUID_LENGTH);
        return "%s/%s-%s.%s".formatted(directory, date, uuid, extension);
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BusinessException(ImageErrorCode.IMAGE_INVALID_TYPE);
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    private String extractKey(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            String path = url.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            String bucketPrefix = bucket + "/";
            if (path.startsWith(bucketPrefix)) {
                path = path.substring(bucketPrefix.length());
            }
            return path;
        } catch (MalformedURLException e) {
            log.error("[S3 URL 파싱 오류] url={}", imageUrl, e);
            throw new BusinessException(ImageErrorCode.IMAGE_DELETE_FAILED);
        }
    }
}
