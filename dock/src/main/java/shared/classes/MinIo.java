package shared.classes;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MinIo {
    private static final Map<String, String> CONFIG;

    static {
        CONFIG = new HashMap<>();
        String minioHost=System.getenv("MINIO_HOST");
        String minioRootUser=System.getenv("MINIO_ROOT_USER");
        String minioRootPassword=System.getenv("MINIO_ROOT_PASSWORD");
        CONFIG.put("MINIO_HOST",minioHost==null?"http://127.0.0.1:9000":minioHost );
        CONFIG.put("MINIO_ROOT_USER", minioRootUser==null?"minioadmin":minioRootUser);
        CONFIG.put("MINIO_ROOT_PASSWORD", minioRootPassword==null?"minioadmin":minioRootPassword);
    }

    private static MinIo instance = null;
    private final MinioClient MINIO_CLIENT;

    private MinIo()  {
      
                     
        MINIO_CLIENT = MinioClient.builder()
                .endpoint(CONFIG.get("MINIO_HOST"))
                .credentials(CONFIG.get("MINIO_ROOT_USER"), CONFIG.get("MINIO_ROOT_PASSWORD"))
                .build();
    }

    public static MinIo getInstance()  {
        if (instance == null) {
            instance = new MinIo();
        }
        return instance;
    }

    public static String uploadObject(String bucketName, String id, JSONObject file)  {
        return uploadObject(bucketName, id, file.getString("data"), file.getString("type"));
    }

    public static String uploadObject(String bucketName, String id, String data, String contentType)  {
        String url = "";
        try {
            MinioClient minioClient = MinIo.getInstance().MINIO_CLIENT;
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()))
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

            byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(data);
            InputStream stream = new ByteArrayInputStream(bytes);
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(id)
                            .stream(stream, -1, 10485760)
                            .contentType(contentType)
                            .build());
            url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(id)
                            .build());
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static boolean deleteObject(String bucketName, String id)  {
        boolean deleted = false;
        try {
            MinioClient minioClient = MinIo.getInstance().MINIO_CLIENT;
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(id).build());
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(id).build());
            deleted = true;
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException | NoSuchAlgorithmException | InvalidKeyException |
                 InvalidResponseException | XmlParserException | InternalException e) {
            e.printStackTrace();
        }
        return deleted;
    }
}
