package com.iind.lox;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
  private LoxClass xlass;
  private final Map<String, Object> fields = new HashMap<>();

  LoxInstance(LoxClass xlass) {
    this.xlass = xlass;
  }

  public Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    LoxFunction method = xlass.findMethod(name.lexeme);
    if (method != null) {
      return method.bind(this);
    }

    throw new RuntimeError(
        name, String.format("Unidentified property %s referenced.", name.lexeme));
  }

  public void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  @Override
  public String toString() {
    return xlass.name + " instance";
  }
}
