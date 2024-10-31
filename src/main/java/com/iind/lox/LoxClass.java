package com.iind.lox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
  final String name;
  final LoxClass superClass;
  final Map<String, LoxFunction> methods;

  LoxClass(String name, LoxClass superClass, Map<String, LoxFunction> methods) {
    this.name = name;
    this.superClass = superClass;
    this.methods = methods;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> args) {
    LoxInstance instance = new LoxInstance(this);

    LoxFunction initializer = findMethod("init");
    if (initializer != null) {
      initializer.bind(instance).call(interpreter, args);
    }
    return instance;
  }

  public LoxFunction findMethod(String name) {
    if (methods.containsKey(name)) {
      return methods.get(name);
    }

    if (superClass != null) {
      return superClass.findMethod(name);
    }

    return null;
  }

  @Override
  public int arity() {
    LoxFunction initializer = findMethod("init");
    if (initializer == null) {
      return 0;
    }
    return initializer.arity();
  }

  @Override
  public String toString() {
    return name;
  }
}
