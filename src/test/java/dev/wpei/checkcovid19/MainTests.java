package dev.wpei.checkcovid19;

import dev.wpei.checkcovid19.service.CheckCovid19Service;
import dev.wpei.checkcovid19.service.S3Service;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

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

	public static void testS3DownloadAndSaveLocal() {
		printMemoryStat();
		Path targetFile = Paths.get("target", "s3output");
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
				//TODO
				// User InputStream or Consumer(Stream) to get s3 object

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

}
