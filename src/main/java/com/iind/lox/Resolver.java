package com.iind.lox;

import com.iind.lox.Expr.Assignment;
import com.iind.lox.Expr.Binary;
import com.iind.lox.Expr.Block;
import com.iind.lox.Expr.Call;
import com.iind.lox.Expr.Grouping;
import com.iind.lox.Expr.Literal;
import com.iind.lox.Expr.Logical;
import com.iind.lox.Expr.Ternary;
import com.iind.lox.Expr.Unary;
import com.iind.lox.Expr.Variable;
import com.iind.lox.Stmt.Expression;
import com.iind.lox.Stmt.Function;
import com.iind.lox.Stmt.IfControl;
import com.iind.lox.Stmt.Print;
import com.iind.lox.Stmt.ReturnControl;
import com.iind.lox.Stmt.Var;
import com.iind.lox.Stmt.WhileControl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
  private final Interpreter interpreter;
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
  private FunctionType currentFunction = FunctionType.NONE;

  Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
  }

  private enum FunctionType {
    NONE,
    FUNCTION
  }

  // Statement visit methods
  @Override
  public Void visitBlockStmt(Stmt.Block block) {
    beginScope();
    resolve(block.statements);
    endScope();

    return null;
  }

  @Override
  public Void visitVarStmt(Var var) {
    declare(var.name);
    if (var.initializer != null) {
      resolve(var.initializer);
    }
    define(var.name);

    return null;
  }

  @Override
  public Void visitFunctionStmt(Function fun) {
    declare(fun.name);
    define(fun.name);
    resolveFunction(fun, FunctionType.FUNCTION);

    return null;
  }

  @Override
  public Void visitExpressionStmt(Expression expression) {
    resolve(expression.expr);

    return null;
  }

  @Override
  public Void visitIfControlStmt(IfControl ifControl) {
    resolve(ifControl.cond);
    resolve(ifControl.thenBranch);
    if (ifControl.thenBranch != null) {
      resolve(ifControl.thenBranch);
    }

    return null;
  }

  @Override
  public Void visitWhileControlStmt(WhileControl whileControl) {
    resolve(whileControl.cond);
    resolve(whileControl.body);

    return null;
  }

  @Override
  public Void visitReturnControlStmt(ReturnControl returnControl) {
    if (currentFunction == FunctionType.NONE) {
      Lox.error(returnControl.keyword, "Can't return from top-level code.");
    }
    if (returnControl.res != null) {
      resolve(returnControl.res);
    }

    return null;
  }

  @Override
  public Void visitPrintStmt(Print print) {
    resolve(print.expr);

    return null;
  }

  // Expression visit methods
  @Override
  public Void visitVariableExpr(Variable var) {
    if (!scopes.isEmpty() && scopes.peek().get(var.name.lexeme) == Boolean.FALSE) {
      Lox.error(var.name, "Can't use local variable in its own initializer.");
    }

    resolveLocal(var, var.name);

    return null;
  }

  @Override
  public Void visitAssignmentExpr(Assignment assignment) {
    resolve(assignment.value);
    resolveLocal(assignment, assignment.name);

    return null;
  }

  @Override
  public Void visitBlockExpr(Block block) {
    resolve(block.expr);
    resolve(block.right);

    return null;
  }

  @Override
  public Void visitTernaryExpr(Ternary ternary) {
    resolve(ternary.cond);
    resolve(ternary.exprTrue);
    resolve(ternary.exprFalse);

    return null;
  }

  @Override
  public Void visitBinaryExpr(Binary binary) {
    resolve(binary.left);
    resolve(binary.right);

    return null;
  }

  @Override
  public Void visitGroupingExpr(Grouping grouping) {
    resolve(grouping.expression);

    return null;
  }

  @Override
  public Void visitLiteralExpr(Literal literal) {
    return null;
  }

  @Override
  public Void visitLogicalExpr(Logical logical) {
    resolve(logical.left);
    resolve(logical.right);

    return null;
  }

  @Override
  public Void visitUnaryExpr(Unary unary) {
    resolve(unary.right);

    return null;
  }

  @Override
  public Void visitCallExpr(Call call) {
    resolve(call.callee);
    call.args.stream().forEach(this::resolve);

    return null;
  }

  // Scope stack management
  private void beginScope() {
    scopes.push(new HashMap<>());
  }

  private void endScope() {
    scopes.pop();
  }

  private void declare(Token name) {
    if (scopes.isEmpty()) {
      return;
    }

    Map<String, Boolean> scope = scopes.peek();

    if (scope.containsKey(name.lexeme)) {
      Lox.error(name, "There is already a variable with this name in this scope.");
    }

    scope.put(name.lexeme, false);
  }

  private void define(Token name) {
    if (scopes.isEmpty()) {
      return;
    }

    scopes.peek().put(name.lexeme, true);
  }

  // Resolvers
  void resolve(List<Stmt> statements) {
    for (Stmt stmt : statements) {
      resolve(stmt);
    }
  }

  private void resolve(Stmt stmt) {
    stmt.accept(this);
  }

  private void resolve(Expr expr) {
    expr.accept(this);
  }

  private void resolveLocal(Expr expr, Token name) {
    for (int i = scopes.size() - 1; i >= 0; --i) {
      if (scopes.get(i).containsKey(name.lexeme)) {
        interpreter.resolve(expr, scopes.size() - 1 - i);
        return;
      }
    }
  }

  private void resolveFunction(Function func, FunctionType type) {
    FunctionType enclosingFunc = currentFunction;
    currentFunction = type;
    beginScope();
    for (Token param : func.params) {
      declare(param);
      define(param);
    }
    resolve(func.body);
    endScope();
    currentFunction = enclosingFunc;
  }
}