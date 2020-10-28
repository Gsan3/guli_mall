package com.tjj.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class GulimallThirdPartyApplicationTests {
    @Autowired
    OSSClient ossClient;

    @Test
    void contextLoads() {
    }

        @Test
        public void TestsaveFile() throws FileNotFoundException {
            // download file to local
            InputStream inputStream = new FileInputStream("C:\\Users\\Gsan\\Pictures\\timg.jpg");
            ossClient.putObject("gulimall-gsan", "timg2.jpg", inputStream);

            ossClient.shutdown();

            System.out.println("上传成功...");
        }

}
