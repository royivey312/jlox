package com.iind.lox;

import com.iind.lox.Expr.Binary;
import com.iind.lox.Expr.Block;
import com.iind.lox.Expr.Grouping;
import com.iind.lox.Expr.Literal;
import com.iind.lox.Expr.Ternary;
import com.iind.lox.Expr.Unary;
import com.iind.lox.Stmt.Expression;
import com.iind.lox.Stmt.Print;
import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

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

  @Override
  public Object visitBlockExpr(Block block) {
    Object res = evaluate(block.expr);
    evaluate(block.right);
    return res;
  }

  @Override
  public Object visitTernaryExpr(Ternary ternary) {
    Object condResult = evaluate(ternary.cond);
    return isTruthy(condResult) ? evaluate(ternary.exprTrue) : evaluate(ternary.exprFalse);
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
  public Object visitGroupingExpr(Grouping grouping) {
    return evaluate(grouping.expression);
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

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  private Boolean isTruthy(Object obj) {
    Boolean res = true;
    if (obj == null) res = false;
    else if (obj instanceof Boolean) res = (Boolean) obj;
    return res;
  }

  private Boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;
    return a.equals(b);
  }

  private void checkOperand(Token operator, Object operand) {
    if (operand instanceof Double) return;
    throw new RuntimeError(operator, "Operand must be a number");
  }

  private void checkOperands(Token operator, Object leftOp, Object rightOp) {
    if (leftOp instanceof Double && rightOp instanceof Double) return;
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
          operator, "Operands must be either two numbers, two strings, or a string and a number;");
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
    if (obj == null) return "nil";

    if (obj instanceof Double) {
      String text = obj.toString();
      if (text.endsWith(".0")) return text.substring(0, text.length() - 2);
      return text;
    }

    return obj.toString();
  }

  @Override
  public Void visitExpressionStmt(Expression expression) {
    evaluate(expression.expr);
    return null;
  }

  @Override
  public Void visitPrintStmt(Print print) {
    Object value = evaluate(print.expr);
    System.out.println(stringify(value));
    return null;
  }
}
