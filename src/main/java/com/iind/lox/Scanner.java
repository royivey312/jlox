package com.iind.lox;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

  // The Input
  final String source;

  // The Output
  final List<Token> tokens = new ArrayList<>();

  // Scanner Processing State
  private int start;
  private int current;
  private int line = 1;

  public Scanner(String source) {
    this.source = source;
  }

  public List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(new Token(TokenType.EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(':
        addToken(TokenType.LEFT_PAREN);
        break;
      case ')':
        addToken(TokenType.RIGHT_PAREN);
        break;
      case '{':
        addToken(TokenType.LEFT_BRACE);
        break;
      case '}':
        addToken(TokenType.RIGHT_BRACE);
        break;
      case ',':
        addToken(TokenType.COMMA);
        break;
      case '.':
        addToken(TokenType.DOT);
        break;
      case '-':
        addToken(TokenType.MINUS);
        break;
      case '+':
        addToken(TokenType.PLUS);
        break;
      case '*':
        addToken(TokenType.STAR);
        break;
      case ';':
        addToken(TokenType.SEMICOLON);
        break;
      case '?':
        addToken(TokenType.QUESTION_MARK);
        break;
      case ':':
        addToken(TokenType.COLON);
        break;

      // 1 OR 2 character
      case '!':
        addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
        break;
      case '=':
        addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
        break;
      case '>':
        addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
        break;
      case '<':
        addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
        break;

      // Handle Comment or Division
      case '/':
        if (match('/')) {
          singleLineComment();
        } else if (match('*')) {
          multiLineComment();
        } else {
          addToken(TokenType.SLASH);
        }
        break;

      case ' ':
      case '\r':
      case '\t':
        break;

      case '\n':
        line++;
        break;

      case '"':
        string();
        break;

      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected Token found!");
        }
        break;
    }
  }

  // SOURCE PROCESSING METHODS
  private char peek() {
    return isAtEnd() ? '\0' : source.charAt(current);
  }

  private char peekNext() {
    return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);
  }

  private char advance() {
    return source.charAt(current++);
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private boolean match(char m) {
    boolean match = false;

    if (!isAtEnd() && peek() == m) {
      match = true;
      current++;
    }

    return match;
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  // TOKENIZER METHODS
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  private void singleLineComment() {
    while (peek() != '\n' && !isAtEnd()) advance();
  }

  private void multiLineComment() {
    boolean complete = true;
    while (peek() != '*' && peekNext() != '/') {
      if (!isAtEnd()) {
        char c = advance();
        if (c == '\n') line++;
      } else {
        Lox.error(line, "Unterminated multi-line comment");
        complete = false;
        break;
      }
    }
    // Last "*/"
    if (complete) current += 2;
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++;
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated String");
      return;
    }

    advance(); // Last "
    addToken(TokenType.STRING, source.substring(start + 1, current - 1));
  }

  private void number() {
    while (isDigit(peek())) advance();

    if (peek() == '.' && isDigit(peekNext())) {
      advance();
      while (isDigit(peek())) advance();
    }

    addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    String text = source.substring(start, current).toLowerCase();
    TokenType type = TokenType.KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER);

    addToken(type);
  }
}
