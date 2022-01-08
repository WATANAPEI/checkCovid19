package dev.wpei.checkcovid19;

import dev.wpei.checkcovid19.service.S3Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;

class LoadLocalFileAndSaveS3 {

	public static void uploadLocalFile() throws IOException {
		printMemoryStat();
		Path sourceFile = Paths.get("src", "test", "100M.dummy");
		Path targetFile = Paths.get("src", "test", "out.dummy");
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
	void uploadLocalFileTest() throws IOException {
		LoadLocalFileAndSaveS3.uploadLocalFile();
	}
	@Test
	void testS3ObjectSizeTest() {
		String bucketName = "lambda-artifacts-fs2wafw43";
		String key = "5147161a-417e-41aa-a506-7b3bd328c489";
		Region region = Region.US_EAST_2;
		long expect = 1564;
		S3Service s3Service = new S3Service(bucketName, region);
		long actual = s3Service.fetchS3FileSize(key);
		Assertions.assertEquals(expect, actual);
	}

	@Test
	public void testS3DownloadAndSaveOtherS3Test() {
		String sourceBucketName = "lambda-artifacts-fs2wafw43";
		String sinkBucketName = "upload-test-ffwek32fsda";
		String sourceFileKey = "1M.dummy";
		Region region = Region.US_EAST_2;
		S3Service s3Service = new S3Service(sinkBucketName, region);
		String eTag = s3Service.uploadByStream(sourceBucketName, sourceFileKey);
		Assertions.assertNotNull(eTag);
	}


}
