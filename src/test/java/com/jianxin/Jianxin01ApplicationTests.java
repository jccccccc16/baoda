package com.jianxin;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import javax.xml.ws.soap.Addressing;

@SpringBootTest
@RunWith(SpringRunner.class)
class Jianxin01ApplicationTests {

    @Autowired
    private DataSource dataSource;


    private Logger logger = LoggerFactory.getLogger(Jianxin01ApplicationTests.class);

    @Test
    public void test01() {

        logger.info(dataSource.toString());

    }

}
