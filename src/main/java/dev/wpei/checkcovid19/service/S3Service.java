package dev.wpei.checkcovid19.service;

import dev.wpei.checkcovid19.model.DailyPatient;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Slf4j
public class S3Service {
    private String sinkBucketName;
    private Region region;

    public S3Service(String sinkBucketName, Region region) {
        this.sinkBucketName = sinkBucketName;
        this.region = region;
    }

    public String putFile(Path saveFile) {
        String objectKey = UUID.randomUUID().toString();
        log.debug("Putting object "+ objectKey +" into bucket "+ sinkBucketName);
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("default");
        try(S3Client s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();) {

            try {
                String result = putS3Object(s3Client, sinkBucketName, objectKey, saveFile);
                log.debug("Tag info: " +result);
                return result;

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void putFile(List<DailyPatient> dailyPatientList, InputStream is, long contentLength) {
        String objectKey = UUID.randomUUID().toString();
        log.debug("Putting object "+ objectKey +" into bucket "+ sinkBucketName);
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("default");
        try(S3Client s3Client = S3Client.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(credentialsProvider)
                .build();) {

            try {
                String result = putS3Object(s3Client, sinkBucketName, objectKey, is, contentLength);
                log.debug("Tag info: " +result);


            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

    }
    private String putS3Object(S3Client s3, String bucketName, String objectKey, Path localFilePath) throws IOException {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            PutObjectResponse putObjectResponse = s3.putObject(putObjectRequest, RequestBody.fromFile(localFilePath));
            return putObjectResponse.eTag();
        } catch(S3Exception e) {
            throw new IOException(e);
        }
    }
    private String putS3Object(S3Client s3, String bucketName, String objectKey, InputStream is, long contentLength) throws IOException {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            PutObjectResponse putObjectResponse = s3.putObject(putObjectRequest, RequestBody.fromInputStream(is, contentLength));
            return putObjectResponse.eTag();
        } catch(S3Exception e) {
            throw new IOException(e);
        }
    }

    public long fetchS3FileSize(String objectKey) {
        // create s3 client
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("default");
        try(S3Client s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();) {
            try {
                // create object to check s3 file meta data
                HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                        .bucket(sinkBucketName)
                        .key(objectKey)
                        .build();
                HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);
                long contentLength = headObjectResponse.contentLength();
                System.out.println("content-length: " + contentLength +"[byte]");
                return contentLength;

            } catch (NoSuchKeyException e) {
                throw new RuntimeException(e);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

    }

    public String uploadByStream(String sourceBucketName, String sourceFileKey) {
        if(sinkBucketName == null) {
            throw new IllegalStateException("sink bucket name or source bucket name is not set yet.");
        }
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("default");
        // create s3 clients for target bucket and source bucket
        try(S3Client targetS3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
            S3Client sourceS3Client = S3Client.builder()
                    .region(region)
                    .credentialsProvider(credentialsProvider) // In production environment, credential is not the same
                    .build();) {
            try {
                // check source file size in s3
                long contentLength = fetchS3FileSize(sourceFileKey);

                // make GetObjectRequest to download s3 file
                final GetObjectRequest request = GetObjectRequest.builder()
                        .bucket(sourceBucketName)
                        .key(sourceFileKey)
                        .build();
                try (
                        final ResponseInputStream<GetObjectResponse> is = sourceS3Client.getObject(request);
                        BufferedInputStream bs = new BufferedInputStream(is, 1024*1024)
                ) {
                    // make PutObjectRequest to upload s3 file
                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(sinkBucketName)
                            .key(sourceFileKey)
                            .build();

                    PutObjectResponse putObjectResponse = targetS3Client.putObject(putObjectRequest, RequestBody.fromInputStream(bs, contentLength));
                    return putObjectResponse.eTag();
                }

            } catch (NoSuchKeyException e) {
                throw new RuntimeException(e);
            }

        } catch(Exception e) {
            throw new RuntimeException(e);
        }

    }
}
