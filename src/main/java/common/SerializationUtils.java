package common;

import java.io.*;

public class SerializationUtils {
	public static byte[] serialize(Object object) {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			try (ObjectOutput objectOutput = new ObjectOutputStream(byteArrayOutputStream)) {
				objectOutput.writeObject(object);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[]{};
	}
	
	public static Object deserialize(byte[] data) {
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data)) {
			return new ObjectInputStream(byteArrayInputStream).readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}