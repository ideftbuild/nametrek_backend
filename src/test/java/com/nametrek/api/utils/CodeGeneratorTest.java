package com.nametrek.api.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class CodeGeneratorTest {

    @Test
    public void testGenerateCode() {
        String code = CodeGenerator.generateCode();
        assertInstanceOf(String.class, code);
        assertEquals(code.length(), 8);
    }
}

