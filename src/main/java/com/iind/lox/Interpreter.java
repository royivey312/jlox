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
import java.util.stream.Collectors;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

  final Environment globals = new Environment();
  private Environment environment = globals;
  private final Map<Expr, Integer> locals = new HashMap<>();

  Interpreter() {
    globals.define(
        "clock",
        new LoxCallable() {
          @Override
          public int arity() {
            return 0;
          }

          @Override
          public Object call(Interpreter interpreter, List<Object> args) {
            return Double.valueOf(System.currentTimeMillis());
          }

          @Override
          public String toString() {
            return "<native fn>";
          }
        });
  }

  void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError e) {
      Lox.runtimeError(e);
    }
  }

  private void execute(Stmt statement) {
    statement.accept(this);
  }

  void resolve(Expr expr, int depth) {
    locals.put(expr, depth);
  }

  void executeBlockStmt(List<Stmt> statements, Environment environment) {
    Environment prev = this.environment;
    try {
      this.environment = environment;

      for (Stmt statement : statements) {
        execute(statement);
      }
    } finally {
      this.environment = prev;
    }
  }

  @Override
  public Object visitBlockExpr(Block block) {
    Object res = evaluate(block.expr);
    evaluate(block.right);
    return res;
  }

  @Override
  public Object visitAssignmentExpr(Assignment assignment) {
    Object value = evaluate(assignment.value);

    Integer distance = locals.get(assignment);
    if (distance != null) {
      environment.assignAt(distance, assignment.name, value);
    } else {
      globals.assign(assignment.name, value);
    }

    return value;
  }

  @Override
  public Object visitTernaryExpr(Ternary ternary) {
    Object condResult = evaluate(ternary.cond);
    return isTruthy(condResult) ? evaluate(ternary.exprTrue) : evaluate(ternary.exprFalse);
  }

  @Override
  public Object visitLogicalExpr(Logical logical) {
    Object lhs = evaluate(logical.left);

    if (logical.operator.type == TokenType.OR) {
      if (isTruthy(lhs)) {
        return lhs;
      }
    } else {
      if (!isTruthy(lhs)) {
        return lhs;
      }
    }

    return evaluate(logical.right);
  }

  @Override
  public Object visitBinaryExpr(Binary binary) {
    Object lhs = evaluate(binary.left);
    Object rhs = evaluate(binary.right);

    Object res = null;
    switch (binary.operator.type) {
      case MINUS:
        checkOperands(binary.operator, lhs, rhs);
        res = (double) lhs - (double) rhs;
        break;
      case PLUS:
        res = addition(binary.operator, lhs, rhs);
        break;
      case STAR:
        checkOperands(binary.operator, lhs, rhs);
        res = (double) lhs * (double) rhs;
        break;
      case SLASH:
        checkOperands(binary.operator, lhs, rhs);
        if (rhs.equals(Double.valueOf(0))) {
          throw new RuntimeError(binary.operator, "Divide by 0 not allowed");
        }
        res = (double) lhs / (double) rhs;
        break;
      case BANG_EQUAL:
        res = !isEqual(lhs, rhs);
        break;
      case EQUAL_EQUAL:
        res = isEqual(lhs, rhs);
        break;
      case GREATER:
        checkOperands(binary.operator, lhs, rhs);
        res = (double) lhs > (double) rhs;
        break;
      case GREATER_EQUAL:
        checkOperands(binary.operator, lhs, rhs);
        res = (double) lhs >= (double) rhs;
        break;
      case LESS:
        checkOperands(binary.operator, lhs, rhs);
        res = (double) lhs < (double) rhs;
        break;
      case LESS_EQUAL:
        checkOperands(binary.operator, lhs, rhs);
        res = (double) lhs <= (double) rhs;
        break;
      default:
        break;
    }

    return res;
  }

  @Override
  public Object visitCallExpr(Call call) {
    Object callee = evaluate(call.callee);

    List<Object> args = call.args.stream().map(arg -> evaluate(arg)).collect(Collectors.toList());

    if (!(callee instanceof LoxCallable)) {
      throw new RuntimeError(call.paren, "Can only call functions and classes.");
    }

    LoxCallable function = (LoxCallable) callee;

    if (function.arity() != args.size()) {
      throw new RuntimeError(
          call.paren,
          String.format("Expected %s arguments but got %s.", function.arity(), args.size()));
    }

    return function.call(this, args);
  }

  @Override
  public Object visitGroupingExpr(Grouping grouping) {
    return evaluate(grouping.expression);
  }

  @Override
  public Object visitVariableExpr(Variable variable) {
    return lookupVariable(variable.name, variable);
  }

  private Object lookupVariable(Token name, Expr expr) {
    Integer distance = locals.get(expr);
    if (distance != null) {
      return environment.getAt(distance, name.lexeme);
    } else {
      return globals.get(name);
    }
  }

  @Override
  public Object visitLiteralExpr(Literal literal) {
    return literal.value;
  }

  @Override
  public Object visitUnaryExpr(Unary unary) {
    Object res = evaluate(unary.right);
    switch (unary.operator.type) {
      case MINUS:
        checkOperand(unary.operator, res);
        res = -(double) res;
        break;
      case BANG:
        res = !isTruthy(res);
        break;
      default:
        res = null;
        break;
    }
    return res;
  }

  @Override
  public Void visitExpressionStmt(Expression expression) {
    evaluate(expression.expr);
    return null;
  }

  @Override
  public Void visitIfControlStmt(IfControl ifControl) {
    if (isTruthy(evaluate(ifControl.cond))) {
      execute(ifControl.thenBranch);
    } else if (ifControl.elseBranch != null) {
      execute(ifControl.elseBranch);
    }

    return null;
  }

  @Override
  public Void visitPrintStmt(Print print) {
    Object value = evaluate(print.expr);
    System.out.println(stringify(value));

    return null;
  }

  @Override
  public Void visitReturnControlStmt(ReturnControl returnControl) {
    Object value = null;

    if (returnControl.res != null) {
      value = evaluate(returnControl.res);
    }

    throw new Return(value);
  }

  @Override
  public Void visitWhileControlStmt(WhileControl whileControl) {
    while (isTruthy(evaluate(whileControl.cond))) {
      execute(whileControl.body);
    }

    return null;
  }

  @Override
  public Void visitVarStmt(Var var) {
    Object value = null;
    if (var.initializer != null) {
      value = evaluate(var.initializer);
    }
    environment.define(var.name.lexeme, value);

    return null;
  }

  @Override
  public Void visitFunctionStmt(Function fun) {
    environment.define(fun.name.lexeme, new LoxFunction(fun, environment));
    return null;
  }

  @Override
  public Void visitBlockStmt(Stmt.Block block) {
    executeBlockStmt(block.statements, new Environment(environment));
    return null;
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  private Boolean isTruthy(Object obj) {
    Boolean res = true;
    if (obj == null) {
      res = false;
    } else if (obj instanceof Boolean) {
      res = (Boolean) obj;
    }
    return res;
  }

  private Boolean isEqual(Object a, Object b) {
    if (a == null && b == null) {
      return true;
    } else if (a == null) {
      return false;
    }
    return a.equals(b);
  }

  private void checkOperand(Token operator, Object operand) {
    if (operand instanceof Double) {
      return;
    }
    throw new RuntimeError(operator, "Operand must be a number");
  }

  private void checkOperands(Token operator, Object leftOp, Object rightOp) {
    if (leftOp instanceof Double && rightOp instanceof Double) {
      return;
    }
    throw new RuntimeError(operator, "Operands must be numbers");
  }

  private Object addition(Token operator, Object lhs, Object rhs) {
    Object res = null;
    if (lhs instanceof Double && rhs instanceof Double) {
      res = (double) lhs + (double) rhs;
    } else if (bothStr(lhs, rhs) || mixedStrDbl(lhs, rhs)) {
      res = stringify(lhs) + stringify(rhs);
    } else {
      throw new RuntimeError(
          operator, "Operands must be either two numbers, two strings, or a string and a number");
    }
    return res;
  }

  private boolean mixedStrDbl(Object lhs, Object rhs) {
    return (lhs instanceof String && rhs instanceof Double)
        || (lhs instanceof Double && rhs instanceof String);
  }

  private boolean bothStr(Object lhs, Object rhs) {
    return lhs instanceof String && lhs instanceof String;
  }

  private String stringify(Object obj) {
    if (obj == null) {
      return "nil";
    }

    if (obj instanceof Double) {
      String text = obj.toString();
      if (text.endsWith(".0")) {
        return text.substring(0, text.length() - 2);
      }
      return text;
    }

    return obj.toString();
  }
}
