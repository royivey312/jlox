package com.iind.lox;

import com.iind.lox.Stmt.Function;
import java.util.List;

public class LoxFunction implements LoxCallable {
  private final Function decl;
  private final Environment closure;

  LoxFunction(Function decl, Environment closure) {
    this.decl = decl;
    this.closure = closure;
  }

  public int arity() {
    return decl.params.size();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> args) {
    Environment funEnv = new Environment(closure);

    for (int i = 0; i < decl.params.size(); ++i) {
      funEnv.define(decl.params.get(i).lexeme, args.get(i));
    }

    // TODO: Think of another way to return values (one with out Exception mis-use)
    try {
      interpreter.executeBlockStmt(decl.body, funEnv);
    } catch (Return res) {
      return res.value;
    }

    return null;
  }

  @Override
  public String toString() {
    return String.format("<fn %s>", decl.name.lexeme);
  }
}
