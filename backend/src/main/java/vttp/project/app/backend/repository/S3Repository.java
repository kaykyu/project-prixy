package vttp.project.app.backend.repository;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Repository
public class S3Repository {
    
    @Autowired
    private AmazonS3 s3;

    @Value("${s3.bucket.name}")
    private String bucket;

    public String saveImage(MultipartFile imageFile, String name) throws IOException {

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(imageFile.getContentType());
        metadata.setContentLength(imageFile.getSize());

        String key = "project/%s".formatted(name);
        PutObjectRequest req = new PutObjectRequest(bucket, key, imageFile.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        s3.putObject(req);
        return s3.getUrl(bucket, key).toExternalForm();
    }

    public void deleteImage(String id) {

        String key = "project/%s".formatted(id);
        s3.deleteObject(bucket, key);
    }
}
