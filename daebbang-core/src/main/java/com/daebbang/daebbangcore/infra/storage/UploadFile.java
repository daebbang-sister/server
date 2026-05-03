package com.daebbang.daebbangcore.infra.storage;

public record UploadFile(
    byte[] bytes,
    String originalFilename,
    String contentType
) {
}
