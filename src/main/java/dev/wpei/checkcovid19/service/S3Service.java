package dev.wpei.checkcovid19.service;

import dev.wpei.checkcovid19.model.CovidPatientItem;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;

@Slf4j
public class S3Service {
    private String bucketName;
    private Region region;

    public S3Service(String bucketName, Region region) {
        this.bucketName = bucketName;
        this.region = region;
    }

    public void saveCsvToS3(List<CovidPatientItem> covidPatientItemList, File saveFile) {
        //String bucketName = "lambda-artifacts-fs2wafw43";
        String objectKey = UUID.randomUUID().toString();
        //saveCsvToLocal(itemList, saveFile);
        log.debug("Putting object "+ objectKey +" into bucket "+bucketName);
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("default");
        try(S3Client s3Client = S3Client.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(credentialsProvider)
                .build();) {

            try {
                String result = putS3Object(s3Client, bucketName, objectKey, saveFile);
                log.debug("Tag info: " +result);


            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void saveCsvToS3(List<CovidPatientItem> covidPatientItemList, InputStream is, long contentLength) {
        //String bucketName = "lambda-artifacts-fs2wafw43";
        String objectKey = UUID.randomUUID().toString();
        //saveCsvToLocal(itemList, saveFile);
        log.debug("Putting object "+ objectKey +" into bucket "+bucketName);
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("default");
        try(S3Client s3Client = S3Client.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(credentialsProvider)
                .build();) {

            try {
                String result = putS3Object(s3Client, bucketName, objectKey, is, contentLength);
                log.debug("Tag info: " +result);


            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

    }
    private String putS3Object(S3Client s3, String bucketName, String objectKey, File localFile) throws IOException {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            PutObjectResponse putObjectResponse = s3.putObject(putObjectRequest, RequestBody.fromFile(localFile));
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
}
