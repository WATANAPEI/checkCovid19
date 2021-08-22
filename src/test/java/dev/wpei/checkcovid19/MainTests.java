package dev.wpei.checkcovid19;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;

class LoadLocalFileAndSaveS3 {

	public static void testMemoryConsumption() throws IOException {
		printMemoryStat();
		Path sourceFile = Paths.get("target", "100M.dummy");
		Path targetFile = Paths.get("target", "output");
		try(
				InputStream is = Files.newInputStream(sourceFile);
				BufferedInputStream bis = new BufferedInputStream(is);
				OutputStream os = Files.newOutputStream(targetFile)
		) {
			byte[] buffer = new byte[1024*1024];
			int read;
		    while((read = bis.read(buffer, 0, buffer.length)) != -1) {
		    	os.write(buffer, 0, read);
			}
			printMemoryStat();

		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}


	}

	public static String testS3DownloadAndSaveOtherS3() {
		printMemoryStat();
		String targetBucketName = "lambda-artifacts-fs2wafw43";
		String sourceBucketName = "upload-test-ffwek32fsda";
		String sourceFileKey = "100M.dummy";
		ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("default");
		try(S3Client targetS3Client = S3Client.builder()
				.region(Region.US_EAST_2)
				.credentialsProvider(credentialsProvider)
				.build();
			S3Client sourceS3Client = S3Client.builder()
					.region(Region.US_EAST_2)
					.credentialsProvider(credentialsProvider) // In production environment, credential is not the same
					.build();) {
			try {
				HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
						.bucket(sourceBucketName)
						.key(sourceFileKey)
						.build();
				HeadObjectResponse headObjectResponse = targetS3Client.headObject(headObjectRequest);
				long contentLength = headObjectResponse.contentLength();
				System.out.println("content-length: " + contentLength +"[byte]");

				final GetObjectRequest request = GetObjectRequest.builder()
						.bucket(sourceBucketName)
						.key(sourceFileKey)
						.build();
				try (
						final ResponseInputStream<GetObjectResponse> is = sourceS3Client.getObject(request);
						//BufferedInputStream is = new BufferedInputStream(is)
					) {
					PutObjectRequest putObjectRequest = PutObjectRequest.builder()
							.bucket(targetBucketName)
							.key(sourceFileKey)
							.build();

					PutObjectResponse putObjectResponse = targetS3Client.putObject(putObjectRequest, RequestBody.fromInputStream(is, contentLength));
					printMemoryStat();
					return putObjectResponse.eTag();
				}

			} catch (NoSuchKeyException e) {
				throw new RuntimeException(e);
			}

		} catch(Exception e) {
			throw new RuntimeException(e);
		}

	}

	public static void testS3ObjectSize() {
		String bucketName = "lambda-artifacts-fs2wafw43";
		ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("default");
		try(S3Client s3Client = S3Client.builder()
				.region(Region.US_EAST_2)
				.credentialsProvider(credentialsProvider)
				.build();) {
			try {
				HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
						.bucket(bucketName)
						.key("5147161a-417e-41aa-a506-7b3bd328c489")
						.build();
				HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);
				System.out.println("content-length: " + headObjectResponse.contentLength() +"[byte]");

			} catch (NoSuchKeyException e) {
				throw new RuntimeException(e);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

	}



	private static void printMemoryStat() {
		Runtime runtime = Runtime.getRuntime();
		NumberFormat format = NumberFormat.getInstance();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		System.out.println("maxMemory: " + format.format(maxMemory));
		System.out.println("allocatedMemory: " + format.format(allocatedMemory));
		System.out.println("freeMemory: " + format.format(freeMemory));

	}



}

class MainTests {

	@Test
	void loadLocalFileAndSaveS3Test() throws IOException {
		LoadLocalFileAndSaveS3.testMemoryConsumption();
	}
	@Test
	void testS3ObjectSizeTest() {
		LoadLocalFileAndSaveS3.testS3ObjectSize();
	}

	@Test
	public void testS3DownloadAndSaveOtherS3Test() {
		LoadLocalFileAndSaveS3.testS3DownloadAndSaveOtherS3();
	}


}
