package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageErrorCode implements ErrorCode {

    IMAGE_EMPTY(400, "빈 이미지 파일은 업로드할 수 없습니다."),
    IMAGE_INVALID_TYPE(400, "이미지 파일 형식이 올바르지 않습니다. (jpg, jpeg, png, webp 만 허용)"),
    IMAGE_COUNT_EXCEEDED(400, "이미지는 최대 4장까지 등록 가능합니다."),
    INVALID_KEEP_IMAGE_URL(400, "유지할 수 없는 이미지가 포함되어 있습니다."),
    IMAGE_UPLOAD_FAILED(500, "이미지 업로드에 실패했습니다."),
    IMAGE_DELETE_FAILED(500, "이미지 삭제에 실패했습니다.");

    private final int status;
    private final String message;
}
