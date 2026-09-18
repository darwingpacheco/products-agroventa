package com.agro_venta.products.infrastructure.service;

import com.agro_venta.products.infrastructure.config.S3Properties;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class S3ImageStorageService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public S3ImageStorageService(S3Client s3Client, S3Properties s3Properties) {
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
    }

    public Mono<List<String>> uploadImages(Flux<FilePart> imageParts) {
        return imageParts.collectList()
                .flatMap(this::uploadImages);
    }

    public Mono<List<String>> uploadImages(List<FilePart> imageParts) {
        if (imageParts == null || imageParts.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes enviar al menos una imagen"));
        }

        if (imageParts.size() > 5) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Máximo 5 imágenes por producto"));
        }

        return Flux.fromIterable(imageParts)
                .index()
                .concatMap(entry -> uploadSingleImage(entry.getT1().intValue(), entry.getT2()))
                .collectList();
    }

    private Mono<String> uploadSingleImage(int index, FilePart filePart) {
        MediaType contentType = Optional.ofNullable(filePart.headers().getContentType())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        validateContentType(contentType);

        return DataBufferUtils.join(filePart.content())
                .flatMap(buffer -> Mono.fromCallable(() -> uploadToS3(index, filePart, contentType, buffer))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    private String uploadToS3(int index, FilePart filePart, MediaType contentType, DataBuffer buffer) {
        try {
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);

            if (bytes.length > s3Properties.getMaxFileSize()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cada imagen debe pesar como máximo 5MB");
            }

            String objectKey = buildObjectKey(index, filePart.filename());
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(objectKey)
                    .contentType(contentType.toString())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
            return resolvePublicUrl(objectKey);
        } finally {
            DataBufferUtils.release(buffer);
        }
    }

    private void validateContentType(MediaType contentType) {
        String value = contentType.toString().toLowerCase(Locale.ROOT);
        if (s3Properties.getAllowedContentTypes().stream()
                .map(type -> type.toLowerCase(Locale.ROOT))
                .noneMatch(value::equals)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de imagen no soportado: " + contentType);
        }
    }

    private String buildObjectKey(int index, String originalFilename) {
        String safeName = Optional.ofNullable(originalFilename)
                .filter(name -> !name.isBlank())
                .orElse("image")
                .replaceAll("[^a-zA-Z0-9._-]", "-");

        String datePath = LocalDate.now().toString();
        return String.format("%s/%s/%02d-%s-%s", normalizePrefix(), datePath, index + 1, UUID.randomUUID(), safeName);
    }

    private String normalizePrefix() {
        return Optional.ofNullable(s3Properties.getPrefix())
                .filter(prefix -> !prefix.isBlank())
                .orElse("products")
                .replaceAll("^/+|/+$", "");
    }

    private String resolvePublicUrl(String objectKey) {
        if (s3Properties.getPublicBaseUrl() != null && !s3Properties.getPublicBaseUrl().isBlank()) {
            return String.format("%s/%s", s3Properties.getPublicBaseUrl().replaceAll("/+$", ""), objectKey);
        }

        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3Properties.getBucketName(),
                s3Properties.getRegion(),
                objectKey);
    }
}


