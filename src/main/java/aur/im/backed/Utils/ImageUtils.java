package aur.im.backed.Utils;

import com.luciad.imageio.webp.WebPWriteParam;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.springframework.http.MediaType;

public class ImageUtils {

	/**
	 * 将原始图像数据转换为WebP格式的字节数组。
	 *
	 * @param originalImageBytes 原始图像的字节数据，不能为null
	 * @param format 原始图像的格式（如"PNG"、"JPEG"、"WEBP"），不区分大小写
	 * @param quality WebP压缩质量，范围0.0到1.0（1.0为最高质量）
	 * @return 转换后的WebP格式图像字节数组
	 * @throws IOException 如果发生以下情况：
	 *                     - 读取图像数据失败
	 *                     - 不支持的图像格式
	 *                     - 找不到WebP图像写入器
	 *                     - 编码过程中发生I/O错误
	 */
	public static byte[] convertToWebP(
		byte[] originalImageBytes,
		String format,
		float quality
	) throws IOException {
		BufferedImage image = readImage(originalImageBytes, format);

		return writeWebP(image, quality);
	}

	/**
	 * 读取图像字节数据并解码为BufferedImage对象。
	 * 对WebP格式使用专用ImageReader，其他格式使用ImageIO默认读取器。
	 *
	 * @param imageBytes 图像原始字节数据
	 * @param format 图像格式标识（如"WEBP"、"PNG"、"JPEG"）
	 * @return 解码后的BufferedImage对象
	 * @throws IOException 如果发生以下情况：
	 *                     - 格式为WEBP但未找到对应的ImageReader
	 *                     - 图像数据解析失败
	 *                     - 输入流读取错误
	 */
	private static BufferedImage readImage(byte[] imageBytes, String format)
		throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);

		if ("webp".equalsIgnoreCase(format)) {
			Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(
				"WEBP"
			);
			if (readers.hasNext()) {
				ImageReader reader = readers.next();
				try (
					ImageInputStream iis = ImageIO.createImageInputStream(bais)
				) {
					reader.setInput(iis);
					return reader.read(0);
				} finally {
					reader.dispose();
				}
			} else {
				throw new IOException("NO_WEBP_FORMAT_IMAGE_READER");
			}
		}

		BufferedImage image = ImageIO.read(bais);
		if (image == null) {
			throw new IOException("FAILED_READ_IMAGE");
		}
		return image;
	}

	/**
	 * 将BufferedImage编码为WebP格式的字节数组。
	 * 注意：当前实现默认使用无损压缩模式（LOSSLESS_COMPRESSION），
	 * 在此模式下quality参数可能被忽略。
	 *
	 * @param image 要编码的BufferedImage对象
	 * @param quality 压缩质量，0.0到1.0之间（仅在有损压缩时生效）
	 * @return WebP格式的字节数组
	 * @throws IOException 如果发生以下情况：
	 *                     - 找不到WebP图像写入器
	 *                     - 图像编码过程中发生I/O错误
	 */
	private static byte[] writeWebP(BufferedImage image, float quality)
		throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(
			"WEBP"
		);
		if (!writers.hasNext()) {
			throw new IOException("IMAGE_WRITER_NOT_FOUNDED");
		}
		ImageWriter writer = writers.next();

		WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
		writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		writeParam.setCompressionType(
			writeParam.getCompressionTypes()[WebPWriteParam.LOSSLESS_COMPRESSION]
		);
		writeParam.setCompressionQuality(quality);

		try (
			MemoryCacheImageOutputStream output =
				new MemoryCacheImageOutputStream(baos)
		) {
			writer.setOutput(output);
			writer.write(null, new IIOImage(image, null, null), writeParam);
		} finally {
			writer.dispose();
		}

		return baos.toByteArray();
	}

	public static MediaType getImageFormat(byte[] originBytes) {
		if (originBytes == null || originBytes.length < 2) {
			return null;
		}
		if (originBytes.length >= 12) {
			String riff = bytesToHex(Arrays.copyOfRange(originBytes, 0, 4));
			String webp = bytesToHex(Arrays.copyOfRange(originBytes, 8, 12));
			if ("52494646".equals(riff) && "57454250".equals(webp)) {
				return MediaType.valueOf("image/webp");
			}
		}
		if (originBytes.length >= 8) {
			String pngHeader = bytesToHex(
				Arrays.copyOfRange(originBytes, 0, 8)
			);
			if ("89504e470d0a1a0a".equalsIgnoreCase(pngHeader)) {
				return MediaType.IMAGE_PNG;
			}
		}
		if (originBytes.length >= 2) {
			String jpgHeader = bytesToHex(
				Arrays.copyOfRange(originBytes, 0, 2)
			);
			if ("ffd8".equalsIgnoreCase(jpgHeader)) {
				return MediaType.IMAGE_JPEG;
			}
		}
		if (originBytes.length >= 6) {
			String gifHeader = bytesToHex(
				Arrays.copyOfRange(originBytes, 0, 6)
			);
			if (
				(gifHeader.startsWith("474946383761") ||
					gifHeader.startsWith("47494638396E"))
			) {
				return MediaType.IMAGE_GIF;
			}
		}
		if (originBytes.length >= 2) {
			String bmpHeader = bytesToHex(
				Arrays.copyOfRange(originBytes, 0, 2)
			);
			if ("424d".equalsIgnoreCase(bmpHeader)) {
				return MediaType.valueOf("image/bmp");
			}
		}
		return null;
	}

	public static String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}
