package com.iind.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
  private static final Interpreter INTERPRETER = new Interpreter();

  static boolean hadError;
  static boolean hadRuntimeError;

  static final LoxInterpreterOptions OPTIONS = new LoxInterpreterOptions();

  public static void main(String[] args) throws IOException {
    OPTIONS.collectOptions();
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));

    run(new String(bytes, Charset.defaultCharset()));

    if (hadError) System.exit(65);
    if (hadRuntimeError) System.exit(70);
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);

    BufferedReader reader = new BufferedReader(input);

    System.out.println();
    System.out.println("Welcome to the Java Lox Interpreter (Ctrl-D to exit)");
    for (; ; ) {
      System.out.print("jlox $ ");
      String line = reader.readLine();

      if (line == null) break;

      run(line);
      hadError = false;
    }

    System.out.println();
  }

  private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();

    if (hadError) return;

    INTERPRETER.interpret(statements);
  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, "at end", message);
    } else {
      report(token.line, "at '" + token.lexeme + "'", message);
    }
  }

  static void runtimeError(RuntimeError error) {
    System.err.printf("%s%n[line %s]%n", error.getMessage(), error.operator.line);
    hadRuntimeError = true;
  }

  private static void report(int line, String where, String message) {
    System.err.printf("[line %s] Error %s: %s%n", line, where, message);
    hadError = true;
  }

  static class LoxInterpreterOptions {
    boolean scannerDebug = false;
    boolean parserDebug = false;
    boolean interpreterDebug = false;

    public void collectOptions() {
      if (isOn("scannerDebug")) scannerDebug = true;
      if (isOn("parserDebug")) parserDebug = true;
      if (isOn("interpreterDebug")) interpreterDebug = true;
      printOptions();
    }

    private boolean isOn(String prop) {
      return System.getProperty(prop) != null;
    }

    private void printOptions() {
      System.out.println("scannerDebug=" + scannerDebug);
      System.out.println("parserDebug=" + parserDebug);
      System.out.println("interpreterDebug=" + interpreterDebug);
    }
  }
}
