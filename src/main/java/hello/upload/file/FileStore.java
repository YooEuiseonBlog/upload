package hello.upload.file;

import hello.upload.domain.UploadFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FileStore {

    @Value("${file.dir}")
    private String fileDir;

    public String getFullPath(String fileName) {
        return fileDir + fileName;
    }

    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<UploadFile> storeFileResult = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                storeFileResult.add(storeFile(multipartFile));
            }
        }
        return storeFileResult;
    }

    public List<UploadFile> storeFilesV2(List<MultipartFile> multipartFiles) throws IOException {
        List<UploadFile> storeFileList = multipartFiles.stream()
                .filter(multipartFile -> !multipartFile.isEmpty())
                .map(multipartFile -> storeFileV2(multipartFile))
                .collect(Collectors.toList());
        return storeFileList;
    }

    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFilename);
        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        return new UploadFile(originalFilename, storeFileName);
    }

    public UploadFile storeFileV2(MultipartFile multipartFile) {
        try {
            if (multipartFile.isEmpty()) {
                return null;
            }

            String originalFilename = multipartFile.getOriginalFilename();
            String storeFileName = createStoreFileName(originalFilename);
            multipartFile.transferTo(new File(getFullPath(storeFileName)));

            return new UploadFile(originalFilename, storeFileName);

        } catch (IOException e) {
            log.info("store-FileV2-exception = ", e);
            return null;
        }
    }

    private String createStoreFileName(String originalFilename) {
        //서버에 저장하는 파일명
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }
}
