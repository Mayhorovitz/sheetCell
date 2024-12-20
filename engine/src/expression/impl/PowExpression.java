
package expression.impl;


import expression.api.Expression;
import cell.api.CellType;
import cell.api.EffectiveValue;
import cell.impl.EffectiveValueImpl;


public class PowExpression extends BinaryExpression {

    private Expression left;
    private Expression right;

    public PowExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public EffectiveValue eval(EffectiveValue left, EffectiveValue right) {
        // Check for null or unknown values
        if (left == null || right == null || left.getCellType() == CellType.UNKNOWN || right.getCellType() == CellType.UNKNOWN) {
            return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN);
        }
        // Extract numeric values from the operands
        Double leftValue = left.extractValueWithExpectation(Double.class);
        Double rightValue = right.extractValueWithExpectation(Double.class);
        if (leftValue == null || rightValue == null) {
            return new EffectiveValueImpl(CellType.NUMERIC, Double.NaN);
        }
        double result = Math.pow(leftValue, rightValue);

        // Return the result as an EffectiveValue
        return new EffectiveValueImpl(CellType.NUMERIC, result);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}