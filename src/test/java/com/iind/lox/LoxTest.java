package com.iind.lox;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoxTest {

  final String TEST_RESOURCE_DIR = System.getProperty("user.dir") + "/src/test/resources/";

  String getFilePath(String filename) {
    return TEST_RESOURCE_DIR + filename;
  }

  private void runScript(String filename) {
    String args[] = {getFilePath(filename)};

    try {
      Lox.main(args);
    } catch (Exception e) {
      fail(e);
    }
  }

  @BeforeAll
  static void beforeAll() {
    Lox.underTest = true;
    Lox.OPTIONS.silentMode = true;
  }

  @BeforeEach
  void beforeEach() {
    Lox.hadRuntimeError = false;
    Lox.hadError = false;
  }

  @Test
  public void shouldAnswerWithTrue() {
    assertTrue(true);
  }

  @Test
  void runFuncClosure() {
    runScript("FuncClosure.lox");
  }

  @Test
  void recursion() {
    runScript("FuncRecursion.lox");
  }

  @Test
  void classCake() {
    runScript("CakeClass.lox");
  }

  @Test
  void subClassDoughnut() {
    runScript("SuperDoughnut.lox");
  }

  @AfterAll
  static void afterAll() {
    System.out.println("\u001B[32m-- Lox Tests Completed --\u001B[0m");
  }
}
