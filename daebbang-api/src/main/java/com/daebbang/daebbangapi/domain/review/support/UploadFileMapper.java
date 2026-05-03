package com.daebbang.daebbangapi.domain.review.support;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.ImageErrorCode;
import com.daebbang.daebbangcore.infra.storage.UploadFile;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public final class UploadFileMapper {

    private UploadFileMapper() {
    }

    public static List<UploadFile> toUploadFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();
        return files.stream().map(UploadFileMapper::toUploadFile).toList();
    }

    private static UploadFile toUploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ImageErrorCode.IMAGE_EMPTY);
        }
        try {
            return new UploadFile(file.getBytes(), file.getOriginalFilename(), file.getContentType());
        } catch (IOException e) {
            throw new BusinessException(ImageErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }
}
