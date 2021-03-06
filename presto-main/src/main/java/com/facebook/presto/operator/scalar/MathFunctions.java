/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.operator.scalar;

import com.facebook.presto.annotation.UsedByGeneratedCode;
import com.facebook.presto.metadata.Signature;
import com.facebook.presto.metadata.SqlScalarFunction;
import com.facebook.presto.metadata.SqlScalarFunctionBuilder.SpecializeContext;
import com.facebook.presto.operator.aggregation.TypedSet;
import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.function.Description;
import com.facebook.presto.spi.function.LiteralParameters;
import com.facebook.presto.spi.function.ScalarFunction;
import com.facebook.presto.spi.function.SqlNullable;
import com.facebook.presto.spi.function.SqlType;
import com.facebook.presto.spi.type.StandardTypes;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Doubles;
import io.airlift.slice.Slice;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.facebook.presto.metadata.FunctionKind.SCALAR;
import static com.facebook.presto.metadata.Signature.longVariableExpression;
import static com.facebook.presto.spi.StandardErrorCode.INVALID_FUNCTION_ARGUMENT;
import static com.facebook.presto.spi.StandardErrorCode.NUMERIC_VALUE_OUT_OF_RANGE;
import static com.facebook.presto.spi.type.Decimals.bigIntegerTenToNth;
import static com.facebook.presto.spi.type.Decimals.checkOverflow;
import static com.facebook.presto.spi.type.Decimals.decodeUnscaledValue;
import static com.facebook.presto.spi.type.Decimals.encodeUnscaledValue;
import static com.facebook.presto.spi.type.Decimals.longTenToNth;
import static com.facebook.presto.spi.type.DoubleType.DOUBLE;
import static com.facebook.presto.spi.type.TypeSignature.parseTypeSignature;
import static com.facebook.presto.spi.type.VarcharType.VARCHAR;
import static com.facebook.presto.type.DecimalOperators.modulusScalarFunction;
import static com.facebook.presto.type.DecimalOperators.modulusSignatureBuilder;
import static com.facebook.presto.util.Failures.checkCondition;
import static io.airlift.slice.Slices.utf8Slice;
import static java.lang.Character.MAX_RADIX;
import static java.lang.Character.MIN_RADIX;
import static java.lang.Float.floatToRawIntBits;
import static java.lang.Float.intBitsToFloat;
import static java.lang.String.format;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

public final class MathFunctions
{
    public static final SqlScalarFunction DECIMAL_TRUNCATE_FUNCTION = decimalTruncateNFunction();
    public static final SqlScalarFunction[] DECIMAL_CEILING_FUNCTIONS = {decimalCeilingFunction("ceiling"), decimalCeilingFunction("ceil")};
    public static final SqlScalarFunction DECIMAL_FLOOR_FUNCTION = decimalFloorFunction();
    public static final SqlScalarFunction DECIMAL_MOD_FUNCTION = decimalModFunction();
    public static final SqlScalarFunction[] DECIMAL_ROUND_FUNCTIONS = {decimalRoundFunction(), decimalRoundNFunction()};

    private MathFunctions() {}

    @Description("absolute value")
    @ScalarFunction("abs")
    @SqlType(StandardTypes.TINYINT)
    public static long absTinyint(@SqlType(StandardTypes.TINYINT) long num)
    {
        checkCondition(num != Byte.MIN_VALUE, NUMERIC_VALUE_OUT_OF_RANGE, "Value -128 is out of range for abs(tinyint)");
        return Math.abs(num);
    }

    @Description("absolute value")
    @ScalarFunction("abs")
    @SqlType(StandardTypes.SMALLINT)
    public static long absSmallint(@SqlType(StandardTypes.SMALLINT) long num)
    {
        checkCondition(num != Short.MIN_VALUE, NUMERIC_VALUE_OUT_OF_RANGE, "Value -32768 is out of range for abs(smallint)");
        return Math.abs(num);
    }

    @Description("absolute value")
    @ScalarFunction("abs")
    @SqlType(StandardTypes.INTEGER)
    public static long absInteger(@SqlType(StandardTypes.INTEGER) long num)
    {
        checkCondition(num != Integer.MIN_VALUE, NUMERIC_VALUE_OUT_OF_RANGE, "Value -2147483648 is out of range for abs(integer)");
        return Math.abs(num);
    }

    @Description("absolute value")
    @ScalarFunction
    @SqlType(StandardTypes.BIGINT)
    public static long abs(@SqlType(StandardTypes.BIGINT) long num)
    {
        checkCondition(num != Long.MIN_VALUE, NUMERIC_VALUE_OUT_OF_RANGE, "Value -9223372036854775808 is out of range for abs(bigint)");
        return Math.abs(num);
    }

    @Description("absolute value")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double abs(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.abs(num);
    }

    @ScalarFunction("abs")
    @Description("absolute value")
    public static final class Abs
    {
        private Abs() {}

        @LiteralParameters({"p", "s"})
        @SqlType("decimal(p, s)")
        public static long absShort(@SqlType("decimal(p, s)") long arg)
        {
            return arg > 0 ? arg : -arg;
        }

        @LiteralParameters({"p", "s"})
        @SqlType("decimal(p, s)")
        public static Slice absLong(@SqlType("decimal(p, s)") Slice arg)
        {
            return encodeUnscaledValue(decodeUnscaledValue(arg).abs());
        }
    }

    @Description("absolute value")
    @ScalarFunction("abs")
    @SqlType(StandardTypes.REAL)
    public static long absFloat(@SqlType(StandardTypes.REAL) long num)
    {
        return floatToRawIntBits(Math.abs(intBitsToFloat((int) num)));
    }

    @Description("arc cosine")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double acos(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.acos(num);
    }

    @Description("arc sine")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double asin(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.asin(num);
    }

    @Description("arc tangent")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double atan(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.atan(num);
    }

    @Description("arc tangent of given fraction")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double atan2(@SqlType(StandardTypes.DOUBLE) double num1, @SqlType(StandardTypes.DOUBLE) double num2)
    {
        return Math.atan2(num1, num2);
    }

    @Description("cube root")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double cbrt(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.cbrt(num);
    }

    @Description("round up to nearest integer")
    @ScalarFunction(value = "ceiling", alias = "ceil")
    @SqlType(StandardTypes.TINYINT)
    public static long ceilingTinyint(@SqlType(StandardTypes.TINYINT) long num)
    {
        return num;
    }

    @Description("round up to nearest integer")
    @ScalarFunction(value = "ceiling", alias = "ceil")
    @SqlType(StandardTypes.SMALLINT)
    public static long ceilingSmallint(@SqlType(StandardTypes.SMALLINT) long num)
    {
        return num;
    }

    @Description("round up to nearest integer")
    @ScalarFunction(value = "ceiling", alias = "ceil")
    @SqlType(StandardTypes.INTEGER)
    public static long ceilingInteger(@SqlType(StandardTypes.INTEGER) long num)
    {
        return num;
    }

    @Description("round up to nearest integer")
    @ScalarFunction(alias = "ceil")
    @SqlType(StandardTypes.BIGINT)
    public static long ceiling(@SqlType(StandardTypes.BIGINT) long num)
    {
        return num;
    }

    @Description("round up to nearest integer")
    @ScalarFunction(alias = "ceil")
    @SqlType(StandardTypes.DOUBLE)
    public static double ceiling(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.ceil(num);
    }

    private static SqlScalarFunction decimalCeilingFunction(String name)
    {
        Signature signature = Signature.builder()
                .kind(SCALAR)
                .name(name)
                .longVariableConstraints(longVariableExpression("return_precision", "num_precision - num_scale + min(num_scale, 1)"))
                .argumentTypes(parseTypeSignature("decimal(num_precision,num_scale)", ImmutableSet.of("num_precision", "num_scale")))
                .returnType(parseTypeSignature("decimal(return_precision,0)", ImmutableSet.of("return_precision")))
                .build();
        return SqlScalarFunction.builder(MathFunctions.class)
                .signature(signature)
                .implementation(b -> b
                        .methods("ceilingShortShortDecimal")
                        .withExtraParameters(MathFunctions::decimalTenToScaleAsLongExtraParameters))
                .implementation(b -> b
                        .methods("ceilingLongShortDecimal", "ceilingLongLongDecimal")
                        .withExtraParameters(MathFunctions::decimalTenToScaleAsBigDecimalExtraParameters))
                .build();
    }

    private static List<Object> decimalTenToScaleAsBigDecimalExtraParameters(SpecializeContext context)
    {
        return ImmutableList.of(bigIntegerTenToNth(context.getLiteral("num_scale").intValue()));
    }

    private static List<Object> decimalTenToScaleAsLongExtraParameters(SpecializeContext context)
    {
        return ImmutableList.of(longTenToNth(context.getLiteral("num_scale").intValue()));
    }

    @UsedByGeneratedCode
    public static long ceilingShortShortDecimal(long num, long divisor)
    {
        long increment = (num % divisor) > 0 ? 1 : 0;
        return num / divisor + increment;
    }

    @UsedByGeneratedCode
    public static long ceilingLongShortDecimal(Slice num, BigInteger divisor)
    {
        return ceiling(num, divisor).longValueExact();
    }

    @UsedByGeneratedCode
    public static Slice ceilingLongLongDecimal(Slice num, BigInteger divisor)
    {
        return encodeUnscaledValue(ceiling(num, divisor));
    }

    private static BigInteger ceiling(Slice num, BigInteger divisor)
    {
        BigInteger[] divideAndRemainder = decodeUnscaledValue(num).divideAndRemainder(divisor);
        return divideAndRemainder[0].add(BigInteger.valueOf(divideAndRemainder[1].signum() > 0 ? 1 : 0));
    }

    @Description("round up to nearest integer")
    @ScalarFunction(value = "ceiling", alias = "ceil")
    @SqlType(StandardTypes.REAL)
    public static long ceilingFloat(@SqlType(StandardTypes.REAL) long num)
    {
        return floatToRawIntBits((float) ceiling(intBitsToFloat((int) num)));
    }

    @Description("round to integer by dropping digits after decimal point")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double truncate(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.signum(num) * Math.floor(Math.abs(num));
    }

    @Description("round to integer by dropping digits after decimal point")
    @ScalarFunction
    @SqlType(StandardTypes.REAL)
    public static long truncate(@SqlType(StandardTypes.REAL) long num)
    {
        float numInFloat = intBitsToFloat((int) num);
        return floatToRawIntBits((float) (Math.signum(numInFloat) * Math.floor(Math.abs(numInFloat))));
    }

    @Description("cosine")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double cos(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.cos(num);
    }

    @Description("hyperbolic cosine")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double cosh(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.cosh(num);
    }

    @Description("converts an angle in radians to degrees")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double degrees(@SqlType(StandardTypes.DOUBLE) double radians)
    {
        return Math.toDegrees(radians);
    }

    @Description("Euler's number")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double e()
    {
        return Math.E;
    }

    @Description("Euler's number raised to the given power")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double exp(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.exp(num);
    }

    @Description("round down to nearest integer")
    @ScalarFunction("floor")
    @SqlType(StandardTypes.TINYINT)
    public static long floorTinyint(@SqlType(StandardTypes.TINYINT) long num)
    {
        return num;
    }

    @Description("round down to nearest integer")
    @ScalarFunction("floor")
    @SqlType(StandardTypes.SMALLINT)
    public static long floorSmallint(@SqlType(StandardTypes.SMALLINT) long num)
    {
        return num;
    }

    @Description("round down to nearest integer")
    @ScalarFunction("floor")
    @SqlType(StandardTypes.INTEGER)
    public static long floorInteger(@SqlType(StandardTypes.INTEGER) long num)
    {
        return num;
    }

    @Description("round down to nearest integer")
    @ScalarFunction
    @SqlType(StandardTypes.BIGINT)
    public static long floor(@SqlType(StandardTypes.BIGINT) long num)
    {
        return num;
    }

    @Description("round down to nearest integer")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double floor(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.floor(num);
    }

    private static SqlScalarFunction decimalFloorFunction()
    {
        Signature signature = Signature.builder()
                .kind(SCALAR)
                .name("floor")
                .longVariableConstraints(longVariableExpression("return_precision", "num_precision - num_scale + min(num_scale, 1)"))
                .argumentTypes(parseTypeSignature("decimal(num_precision,num_scale)", ImmutableSet.of("num_precision", "num_scale")))
                .returnType(parseTypeSignature("decimal(return_precision,0)", ImmutableSet.of("return_precision")))
                .build();
        return SqlScalarFunction.builder(MathFunctions.class)
                .signature(signature)
                .implementation(b -> b
                        .methods("floorShortShortDecimal")
                        .withExtraParameters(MathFunctions::decimalTenToScaleAsLongExtraParameters))
                .implementation(b -> b
                        .methods("floorLongShortDecimal", "floorLongLongDecimal")
                        .withExtraParameters(MathFunctions::decimalTenToScaleAsBigDecimalExtraParameters))
                .build();
    }

    @UsedByGeneratedCode
    public static long floorShortShortDecimal(long num, long divisor)
    {
        long increment = (num % divisor) < 0 ? -1 : 0;
        return num / divisor + increment;
    }

    @UsedByGeneratedCode
    public static Slice floorLongLongDecimal(Slice num, BigInteger divisor)
    {
        return encodeUnscaledValue(floor(num, divisor));
    }

    @UsedByGeneratedCode
    public static long floorLongShortDecimal(Slice num, BigInteger divisor)
    {
        return floor(num, divisor).longValueExact();
    }

    private static BigInteger floor(Slice num, BigInteger divisor)
    {
        BigInteger[] divideAndRemainder = decodeUnscaledValue(num).divideAndRemainder(divisor);
        return divideAndRemainder[0].add(BigInteger.valueOf(divideAndRemainder[1].signum() < 0 ? -1 : 0));
    }

    @Description("round down to nearest integer")
    @ScalarFunction("floor")
    @SqlType(StandardTypes.REAL)
    public static long floorFloat(@SqlType(StandardTypes.REAL) long num)
    {
        return floatToRawIntBits((float) floor(intBitsToFloat((int) num)));
    }

    @Description("natural logarithm")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double ln(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.log(num);
    }

    @Description("logarithm to base 2")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double log2(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.log(num) / Math.log(2);
    }

    @Description("logarithm to base 10")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double log10(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.log10(num);
    }

    @Description("logarithm to given base")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double log(@SqlType(StandardTypes.DOUBLE) double num, @SqlType(StandardTypes.DOUBLE) double base)
    {
        return Math.log(num) / Math.log(base);
    }

    @Description("remainder of given quotient")
    @ScalarFunction("mod")
    @SqlType(StandardTypes.TINYINT)
    public static long modTinyint(@SqlType(StandardTypes.TINYINT) long num1, @SqlType(StandardTypes.TINYINT) long num2)
    {
        return num1 % num2;
    }

    @Description("remainder of given quotient")
    @ScalarFunction("mod")
    @SqlType(StandardTypes.SMALLINT)
    public static long modSmallint(@SqlType(StandardTypes.SMALLINT) long num1, @SqlType(StandardTypes.SMALLINT) long num2)
    {
        return num1 % num2;
    }

    @Description("remainder of given quotient")
    @ScalarFunction("mod")
    @SqlType(StandardTypes.INTEGER)
    public static long modInteger(@SqlType(StandardTypes.INTEGER) long num1, @SqlType(StandardTypes.INTEGER) long num2)
    {
        return num1 % num2;
    }

    @Description("remainder of given quotient")
    @ScalarFunction
    @SqlType(StandardTypes.BIGINT)
    public static long mod(@SqlType(StandardTypes.BIGINT) long num1, @SqlType(StandardTypes.BIGINT) long num2)
    {
        return num1 % num2;
    }

    @Description("remainder of given quotient")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double mod(@SqlType(StandardTypes.DOUBLE) double num1, @SqlType(StandardTypes.DOUBLE) double num2)
    {
        return num1 % num2;
    }

    private static SqlScalarFunction decimalModFunction()
    {
        Signature signature = modulusSignatureBuilder()
                .kind(SCALAR)
                .name("mod")
                .build();
        return modulusScalarFunction(signature);
    }

    @Description("remainder of given quotient")
    @ScalarFunction("mod")
    @SqlType(StandardTypes.REAL)
    public static long modFloat(@SqlType(StandardTypes.REAL) long num1, @SqlType(StandardTypes.REAL) long num2)
    {
        return floatToRawIntBits(intBitsToFloat((int) num1) % intBitsToFloat((int) num2));
    }

    @Description("the constant Pi")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double pi()
    {
        return Math.PI;
    }

    @Description("value raised to the power of exponent")
    @ScalarFunction(alias = "pow")
    @SqlType(StandardTypes.DOUBLE)
    public static double power(@SqlType(StandardTypes.DOUBLE) double num, @SqlType(StandardTypes.DOUBLE) double exponent)
    {
        return Math.pow(num, exponent);
    }

    @Description("converts an angle in degrees to radians")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double radians(@SqlType(StandardTypes.DOUBLE) double degrees)
    {
        return Math.toRadians(degrees);
    }

    @Description("a pseudo-random value")
    @ScalarFunction(alias = "rand", deterministic = false)
    @SqlType(StandardTypes.DOUBLE)
    public static double random()
    {
        return ThreadLocalRandom.current().nextDouble();
    }

    @Description("a pseudo-random number between 0 and value (exclusive)")
    @ScalarFunction(value = "random", alias = "rand", deterministic = false)
    @SqlType(StandardTypes.TINYINT)
    public static long randomTinyint(@SqlType(StandardTypes.TINYINT) long value)
    {
        checkCondition(value > 0, INVALID_FUNCTION_ARGUMENT, "bound must be positive");
        return ThreadLocalRandom.current().nextInt((int) value);
    }

    @Description("a pseudo-random number between 0 and value (exclusive)")
    @ScalarFunction(value = "random", alias = "rand", deterministic = false)
    @SqlType(StandardTypes.SMALLINT)
    public static long randomSmallint(@SqlType(StandardTypes.SMALLINT) long value)
    {
        checkCondition(value > 0, INVALID_FUNCTION_ARGUMENT, "bound must be positive");
        return ThreadLocalRandom.current().nextInt((int) value);
    }

    @Description("a pseudo-random number between 0 and value (exclusive)")
    @ScalarFunction(value = "random", alias = "rand", deterministic = false)
    @SqlType(StandardTypes.INTEGER)
    public static long randomInteger(@SqlType(StandardTypes.INTEGER) long value)
    {
        checkCondition(value > 0, INVALID_FUNCTION_ARGUMENT, "bound must be positive");
        return ThreadLocalRandom.current().nextInt((int) value);
    }

    @Description("a pseudo-random number between 0 and value (exclusive)")
    @ScalarFunction(alias = "rand", deterministic = false)
    @SqlType(StandardTypes.BIGINT)
    public static long random(@SqlType(StandardTypes.BIGINT) long value)
    {
        checkCondition(value > 0, INVALID_FUNCTION_ARGUMENT, "bound must be positive");
        return ThreadLocalRandom.current().nextLong(value);
    }

    @Description("round to nearest integer")
    @ScalarFunction("round")
    @SqlType(StandardTypes.TINYINT)
    public static long roundTinyint(@SqlType(StandardTypes.TINYINT) long num)
    {
        return num;
    }

    @Description("round to nearest integer")
    @ScalarFunction("round")
    @SqlType(StandardTypes.SMALLINT)
    public static long roundSmallint(@SqlType(StandardTypes.SMALLINT) long num)
    {
        return num;
    }

    @Description("round to nearest integer")
    @ScalarFunction("round")
    @SqlType(StandardTypes.INTEGER)
    public static long roundInteger(@SqlType(StandardTypes.INTEGER) long num)
    {
        return num;
    }

    @Description("round to nearest integer")
    @ScalarFunction
    @SqlType(StandardTypes.BIGINT)
    public static long round(@SqlType(StandardTypes.BIGINT) long num)
    {
        return round(num, 0);
    }

    @Description("round to nearest integer")
    @ScalarFunction("round")
    @SqlType(StandardTypes.TINYINT)
    public static long roundTinyint(@SqlType(StandardTypes.TINYINT) long num, @SqlType(StandardTypes.BIGINT) long decimals)
    {
        return num;
    }

    @Description("round to nearest integer")
    @ScalarFunction("round")
    @SqlType(StandardTypes.SMALLINT)
    public static long roundSmallint(@SqlType(StandardTypes.SMALLINT) long num, @SqlType(StandardTypes.BIGINT) long decimals)
    {
        return num;
    }

    @Description("round to nearest integer")
    @ScalarFunction("round")
    @SqlType(StandardTypes.INTEGER)
    public static long roundInteger(@SqlType(StandardTypes.INTEGER) long num, @SqlType(StandardTypes.BIGINT) long decimals)
    {
        return num;
    }

    @Description("round to nearest integer")
    @ScalarFunction
    @SqlType(StandardTypes.BIGINT)
    public static long round(@SqlType(StandardTypes.BIGINT) long num, @SqlType(StandardTypes.BIGINT) long decimals)
    {
        return num;
    }

    @Description("round to nearest integer")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double round(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return round(num, 0);
    }

    @Description("round to given number of decimal places")
    @ScalarFunction("round")
    @SqlType(StandardTypes.REAL)
    public static long roundFloat(@SqlType(StandardTypes.REAL) long num)
    {
        return roundFloat(num, 0);
    }

    @Description("round to given number of decimal places")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double round(@SqlType(StandardTypes.DOUBLE) double num, @SqlType(StandardTypes.BIGINT) long decimals)
    {
        if (Double.isNaN(num) || Double.isInfinite(num)) {
            return num;
        }

        double factor = Math.pow(10, decimals);
        if (num < 0) {
            return -(Math.round(-num * factor) / factor);
        }

        return Math.round(num * factor) / factor;
    }

    private static SqlScalarFunction decimalRoundFunction()
    {
        Signature signature = Signature.builder()
                .kind(SCALAR)
                .name("round")
                .longVariableConstraints(
                        longVariableExpression("return_precision", "num_precision - num_scale + min(1, num_scale)"))
                .argumentTypes(parseTypeSignature("decimal(num_precision,num_scale)", ImmutableSet.of("num_precision", "num_scale")))
                .returnType(parseTypeSignature("decimal(return_precision,0)", ImmutableSet.of("return_precision")))
                .build();
        return SqlScalarFunction.builder(MathFunctions.class)
                .signature(signature)
                .description("round to nearest integer")
                .implementation(b -> b
                        .methods("roundShortShortDecimal")
                        .withExtraParameters(MathFunctions::decimalRoundShortExtraParameters))
                .implementation(b -> b
                        .methods("roundLongLongDecimal", "roundLongShortDecimal")
                        .withExtraParameters(MathFunctions::decimalRoundLongExtraParameters))

                .build();
    }

    private static List<Object> decimalRoundShortExtraParameters(SpecializeContext context)
    {
        long scale = context.getLiteral("num_scale");
        long rescaleFactor = longTenToNth((int) scale);
        return ImmutableList.of(rescaleFactor, scale);
    }

    @UsedByGeneratedCode
    public static long roundShortShortDecimal(long num, long rescaleFactor, long inputScale)
    {
        if (num == 0) {
            return 0;
        }
        if (inputScale == 0) {
            return num;
        }
        if (num < 0) {
            return -roundShortShortDecimal(-num, rescaleFactor, inputScale);
        }

        long remainder = num % rescaleFactor;
        long remainderBoundary = rescaleFactor >> 1;
        int roundUp = remainder >= remainderBoundary ? 1 : 0;
        return num / rescaleFactor + roundUp;
    }

    private static List<Object> decimalRoundLongExtraParameters(SpecializeContext context)
    {
        long scale = context.getLiteral("num_scale");
        BigInteger rescaleFactor = bigIntegerTenToNth((int) scale);
        return ImmutableList.of(rescaleFactor, scale);
    }

    @UsedByGeneratedCode
    public static Slice roundLongLongDecimal(Slice numSlice, BigInteger rescaleFactor, long inputScale)
    {
        BigInteger num = decodeUnscaledValue(numSlice);
        if (num.signum() == 0) {
            return encodeUnscaledValue(0);
        }
        if (inputScale == 0) {
            return encodeUnscaledValue(num);
        }
        if (num.signum() < 0) {
            return encodeUnscaledValue(roundLongLongDecimal(num.negate(), rescaleFactor).negate());
        }
        else {
            return encodeUnscaledValue(roundLongLongDecimal(num, rescaleFactor));
        }
    }

    private static BigInteger roundLongLongDecimal(BigInteger num, BigInteger rescaleFactor)
    {
        BigInteger[] divideAndRemainder = num.divideAndRemainder(rescaleFactor);
        BigInteger roundUp = divideAndRemainder[1].compareTo(rescaleFactor.shiftRight(1)) >= 0 ? ONE : ZERO;
        return divideAndRemainder[0].add(roundUp);
    }

    @UsedByGeneratedCode
    public static long roundLongShortDecimal(Slice numSlice, BigInteger rescaleFactor, long inputScale)
    {
        BigInteger num = decodeUnscaledValue(numSlice);
        if (num.signum() == 0) {
            return 0;
        }
        if (num.signum() < 0) {
            return roundLongLongDecimal(num.negate(), rescaleFactor).negate().longValue();
        }
        else {
            return roundLongLongDecimal(num, rescaleFactor).longValue();
        }
    }

    private static SqlScalarFunction decimalRoundNFunction()
    {
        Signature signature = Signature.builder()
                .kind(SCALAR)
                .name("round")
                .longVariableConstraints(
                        //result precision = increment the input precision only if the input number has a decimal point (scale > 0)
                        longVariableExpression("return_precision", "min(38, num_precision + 1)"))
                .argumentTypes(parseTypeSignature("decimal(num_precision,num_scale)", ImmutableSet.of("num_precision", "num_scale")), parseTypeSignature(StandardTypes.BIGINT))
                .returnType(parseTypeSignature("decimal(return_precision,num_scale)", ImmutableSet.of("return_precision", "num_scale")))
                .build();
        return SqlScalarFunction.builder(MathFunctions.class)
                .signature(signature)
                .description("round to given number of decimal places")
                .implementation(b -> b
                        .methods("roundNShortShortDecimal", "roundNLongLongDecimal", "roundNShortLongDecimal")
                        .withExtraParameters(MathFunctions::decimalRoundNExtraParameters))
                .build();
    }

    private static List<Object> decimalRoundNExtraParameters(SpecializeContext context)
    {
        return ImmutableList.of(context.getLiteral("num_precision"), context.getLiteral("num_scale"));
    }

    @UsedByGeneratedCode
    public static long roundNShortShortDecimal(long num, long roundScale, long inputPrecision, long inputScale)
    {
        if (num == 0 || inputPrecision - inputScale + roundScale <= 0) {
            return 0;
        }
        if (roundScale >= inputScale) {
            return num;
        }
        if (num < 0) {
            return -roundNShortShortDecimal(-num, roundScale, inputPrecision, inputScale);
        }

        long rescaleFactor = longTenToNth((int) (inputScale - roundScale));
        long remainder = num % rescaleFactor;
        int roundUp = (remainder >= rescaleFactor >> 1) ? 1 : 0;
        return (num / rescaleFactor + roundUp) * rescaleFactor;
    }

    @UsedByGeneratedCode
    public static Slice roundNLongLongDecimal(Slice num, long roundScale, long inputPrecision, long inputScale)
    {
        BigInteger unscaledVal = decodeUnscaledValue(num);
        if (roundScale >= inputScale) {
            return num;
        }
        return roundNLongDecimal(unscaledVal, roundScale, inputPrecision, inputScale);
    }

    @UsedByGeneratedCode
    public static Slice roundNShortLongDecimal(long num, long roundScale, long inputPrecision, long inputScale)
    {
        if (roundScale >= inputScale) {
            return encodeUnscaledValue(num);
        }
        return roundNLongDecimal(BigInteger.valueOf(num), roundScale, inputPrecision, inputScale);
    }

    private static Slice roundNLongDecimal(BigInteger unscaledValue, long roundScale, long inputPrecision, long inputScale)
    {
        if (unscaledValue.signum() == 0 || inputPrecision - inputScale + roundScale <= 0) {
            return encodeUnscaledValue(0);
        }
        BigInteger rescaleFactor = bigIntegerTenToNth((int) (inputScale - roundScale));
        if (unscaledValue.signum() < 0) {
            return encodeUnscaledValue(roundNLongDecimal(unscaledValue.negate(), rescaleFactor).negate());
        }
        return encodeUnscaledValue(roundNLongDecimal(unscaledValue, rescaleFactor));
    }

    private static BigInteger roundNLongDecimal(BigInteger num, BigInteger rescaleFactor)
    {
        BigInteger[] divideAndRemainder = num.divideAndRemainder(rescaleFactor);
        BigInteger roundUp = divideAndRemainder[1].compareTo(rescaleFactor.shiftRight(1)) >= 0 ? ONE : ZERO;
        BigInteger rounded = divideAndRemainder[0].add(roundUp).multiply(rescaleFactor);
        checkOverflow(rounded);
        return rounded;
    }

    private static SqlScalarFunction decimalTruncateNFunction()
    {
        Signature signature = Signature.builder()
                .kind(SCALAR)
                .name("truncate")
                .argumentTypes(
                        parseTypeSignature("decimal(num_precision,num_scale)", ImmutableSet.of("num_precision", "num_scale")),
                        parseTypeSignature(StandardTypes.BIGINT))
                .returnType(parseTypeSignature("decimal(num_precision,num_scale)", ImmutableSet.of("num_precision", "num_scale")))
                .build();
        return SqlScalarFunction.builder(MathFunctions.class)
                .signature(signature)
                .description("truncate decimal to N places after decimal point")
                .implementation(b -> b
                        .methods("truncateNShortDecimal", "truncateNLongDecimal")
                        .withExtraParameters(MathFunctions::decimalTruncateNExtraParameters))
                .build();
    }

    private static List<Object> decimalTruncateNExtraParameters(SpecializeContext context)
    {
        return ImmutableList.of(context.getLiteral("num_precision"), context.getLiteral("num_scale"));
    }

    @UsedByGeneratedCode
    public static long truncateNShortDecimal(long num, long roundScale, long inputPrecision, long inputScale)
    {
        if (num == 0 || inputPrecision - inputScale + roundScale <= 0) {
            return 0;
        }
        if (roundScale >= inputScale) {
            return num;
        }
        if (num < 0) {
            return -truncateNShortDecimal(-num, roundScale, inputPrecision, inputScale);
        }

        long rescaleFactor = longTenToNth((int) (inputScale - roundScale));
        long remainder = num % rescaleFactor;
        return num - remainder;
    }

    @UsedByGeneratedCode
    public static Slice truncateNLongDecimal(Slice num, long roundScale, long inputPrecision, long inputScale)
    {
        BigInteger unscaledVal = decodeUnscaledValue(num);
        if (unscaledVal.signum() == 0 || inputPrecision - inputScale + roundScale <= 0) {
            return encodeUnscaledValue(0);
        }
        if (roundScale >= inputScale) {
            return num;
        }
        BigInteger rescaleFactor = bigIntegerTenToNth((int) (inputScale - roundScale));
        if (unscaledVal.signum() < 0) {
            return encodeUnscaledValue(truncateNLongDecimal(unscaledVal.negate(), rescaleFactor).negate());
        }
        return encodeUnscaledValue(truncateNLongDecimal(unscaledVal, rescaleFactor));
    }

    @UsedByGeneratedCode
    public static BigInteger truncateNLongDecimal(BigInteger num, BigInteger rescaleFactor)
    {
        BigInteger remainder = num.remainder(rescaleFactor);
        return num.subtract(remainder);
    }

    @Description("round to given number of decimal places")
    @ScalarFunction("round")
    @SqlType(StandardTypes.REAL)
    public static long roundFloat(@SqlType(StandardTypes.REAL) long num, @SqlType(StandardTypes.BIGINT) long decimals)
    {
        float numInFloat = intBitsToFloat((int) num);
        if (Float.isNaN(numInFloat) || Float.isInfinite(numInFloat)) {
            return num;
        }

        double factor = Math.pow(10, decimals);
        if (numInFloat < 0) {
            return floatToRawIntBits((float) -(Math.round(-numInFloat * factor) / factor));
        }

        return floatToRawIntBits((float) (Math.round(numInFloat * factor) / factor));
    }

    @Description("signum")
    @ScalarFunction("sign")
    public static final class Sign
    {
        private Sign() {}

        @LiteralParameters({"p", "s"})
        @SqlType("decimal(1,0)")
        public static long signDecimalShort(@SqlType("decimal(p, s)") long num)
        {
            return (long) Math.signum(num);
        }

        @LiteralParameters({"p", "s"})
        @SqlType("decimal(1,0)")
        public static long signDecimalLong(@SqlType("decimal(p, s)") Slice num)
        {
            return decodeUnscaledValue(num).signum();
        }
    }

    @ScalarFunction
    @SqlType(StandardTypes.BIGINT)
    public static long sign(@SqlType(StandardTypes.BIGINT) long num)
    {
        return (long) Math.signum(num);
    }

    @Description("signum")
    @ScalarFunction("sign")
    @SqlType(StandardTypes.INTEGER)
    public static long signInteger(@SqlType(StandardTypes.INTEGER) long num)
    {
        return (long) Math.signum(num);
    }

    @Description("signum")
    @ScalarFunction("sign")
    @SqlType(StandardTypes.SMALLINT)
    public static long signSmallint(@SqlType(StandardTypes.SMALLINT) long num)
    {
        return (long) Math.signum(num);
    }

    @Description("signum")
    @ScalarFunction("sign")
    @SqlType(StandardTypes.TINYINT)
    public static long signTinyint(@SqlType(StandardTypes.TINYINT) long num)
    {
        return (long) Math.signum(num);
    }

    @Description("signum")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double sign(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.signum(num);
    }

    @Description("signum")
    @ScalarFunction("sign")
    @SqlType(StandardTypes.REAL)
    public static long signFloat(@SqlType(StandardTypes.REAL) long num)
    {
        return floatToRawIntBits((Math.signum(intBitsToFloat((int) num))));
    }

    @Description("sine")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double sin(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.sin(num);
    }

    @Description("square root")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double sqrt(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.sqrt(num);
    }

    @Description("tangent")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double tan(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.tan(num);
    }

    @Description("hyperbolic tangent")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double tanh(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Math.tanh(num);
    }

    @Description("test if value is not-a-number")
    @ScalarFunction("is_nan")
    @SqlType(StandardTypes.BOOLEAN)
    public static boolean isNaN(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Double.isNaN(num);
    }

    @Description("test if value is finite")
    @ScalarFunction
    @SqlType(StandardTypes.BOOLEAN)
    public static boolean isFinite(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Doubles.isFinite(num);
    }

    @Description("test if value is infinite")
    @ScalarFunction
    @SqlType(StandardTypes.BOOLEAN)
    public static boolean isInfinite(@SqlType(StandardTypes.DOUBLE) double num)
    {
        return Double.isInfinite(num);
    }

    @Description("constant representing not-a-number")
    @ScalarFunction("nan")
    @SqlType(StandardTypes.DOUBLE)
    public static double NaN()
    {
        return Double.NaN;
    }

    @Description("Infinity")
    @ScalarFunction
    @SqlType(StandardTypes.DOUBLE)
    public static double infinity()
    {
        return Double.POSITIVE_INFINITY;
    }

    @Description("convert a number to a string in the given base")
    @ScalarFunction
    @SqlType("varchar(64)")
    public static Slice toBase(@SqlType(StandardTypes.BIGINT) long value, @SqlType(StandardTypes.BIGINT) long radix)
    {
        checkRadix(radix);
        return utf8Slice(Long.toString(value, (int) radix));
    }

    @Description("convert a string in the given base to a number")
    @ScalarFunction
    @LiteralParameters("x")
    @SqlType(StandardTypes.BIGINT)
    public static long fromBase(@SqlType("varchar(x)") Slice value, @SqlType(StandardTypes.BIGINT) long radix)
    {
        checkRadix(radix);
        try {
            return Long.parseLong(value.toStringUtf8(), (int) radix);
        }
        catch (NumberFormatException e) {
            throw new PrestoException(INVALID_FUNCTION_ARGUMENT, format("Not a valid base-%d number: %s", radix, value.toStringUtf8()), e);
        }
    }

    private static void checkRadix(long radix)
    {
        checkCondition(radix >= MIN_RADIX && radix <= MAX_RADIX,
                INVALID_FUNCTION_ARGUMENT, "Radix must be between %d and %d", MIN_RADIX, MAX_RADIX);
    }

    @Description("The bucket number of a value given a lower and upper bound and the number of buckets")
    @ScalarFunction("width_bucket")
    @SqlType(StandardTypes.BIGINT)
    public static long widthBucket(@SqlType(StandardTypes.DOUBLE) double operand, @SqlType(StandardTypes.DOUBLE) double bound1, @SqlType(StandardTypes.DOUBLE) double bound2, @SqlType(StandardTypes.BIGINT) long bucketCount)
    {
        checkCondition(bucketCount > 0, INVALID_FUNCTION_ARGUMENT, "bucketCount must be greater than 0");
        checkCondition(!isNaN(operand), INVALID_FUNCTION_ARGUMENT, "operand must not be NaN");
        checkCondition(isFinite(bound1), INVALID_FUNCTION_ARGUMENT, "first bound must be finite");
        checkCondition(isFinite(bound2), INVALID_FUNCTION_ARGUMENT, "second bound must be finite");
        checkCondition(bound1 != bound2, INVALID_FUNCTION_ARGUMENT, "bounds cannot equal each other");

        long result = 0;

        double lower = Math.min(bound1, bound2);
        double upper = Math.max(bound1, bound2);

        if (operand < lower) {
            result = 0;
        }
        else if (operand >= upper) {
            try {
                result = Math.addExact(bucketCount, 1);
            }
            catch (ArithmeticException e) {
                throw new PrestoException(NUMERIC_VALUE_OUT_OF_RANGE, format("Bucket for value %s is out of range", operand));
            }
        }
        else {
            result = (long) ((double) bucketCount * (operand - lower) / (upper - lower) + 1);
        }

        if (bound1 > bound2) {
            result = (bucketCount - result) + 1;
        }

        return result;
    }

    @Description("The bucket number of a value given an array of bins")
    @ScalarFunction("width_bucket")
    @SqlType(StandardTypes.BIGINT)
    public static long widthBucket(@SqlType(StandardTypes.DOUBLE) double operand, @SqlType("array(double)") Block bins)
    {
        int numberOfBins = bins.getPositionCount();

        checkCondition(numberOfBins > 0, INVALID_FUNCTION_ARGUMENT, "Bins cannot be an empty array");
        checkCondition(!isNaN(operand), INVALID_FUNCTION_ARGUMENT, "Operand cannot be NaN");

        int lower = 0;
        int upper = numberOfBins;

        int index;
        double bin;

        while (lower < upper) {
            if (DOUBLE.getDouble(bins, lower) > DOUBLE.getDouble(bins, upper - 1)) {
                throw new PrestoException(INVALID_FUNCTION_ARGUMENT, "Bin values are not sorted in ascending order");
            }

            index = (lower + upper) / 2;
            bin = DOUBLE.getDouble(bins, index);

            checkCondition(isFinite(bin), INVALID_FUNCTION_ARGUMENT, format("Bin value must be finite, got %s", bin));

            if (operand < bin) {
                upper = index;
            }
            else {
                lower = index + 1;
            }
        }

        return lower;
    }

    @Description("cosine similarity between the given sparse vectors")
    @ScalarFunction
    @SqlNullable
    @SqlType(StandardTypes.DOUBLE)
    public static Double cosineSimilarity(@SqlType("map(varchar,double)") Block leftMap, @SqlType("map(varchar,double)") Block rightMap)
    {
        Double normLeftMap = mapL2Norm(leftMap);
        Double normRightMap = mapL2Norm(rightMap);

        if (normLeftMap == null || normRightMap == null) {
            return null;
        }

        double dotProduct = mapDotProduct(leftMap, rightMap);

        return dotProduct / (normLeftMap * normRightMap);
    }

    private static double mapDotProduct(Block leftMap, Block rightMap)
    {
        TypedSet rightMapKeys = new TypedSet(VARCHAR, rightMap.getPositionCount());

        for (int i = 0; i < rightMap.getPositionCount(); i += 2) {
            rightMapKeys.add(rightMap, i);
        }

        double result = 0.0;

        for (int i = 0; i < leftMap.getPositionCount(); i += 2) {
            int position = rightMapKeys.positionOf(leftMap, i);

            if (position != -1) {
                result += DOUBLE.getDouble(leftMap, i + 1) *
                        DOUBLE.getDouble(rightMap, 2 * position + 1);
            }
        }

        return result;
    }

    private static Double mapL2Norm(Block map)
    {
        double norm = 0.0;

        for (int i = 1; i < map.getPositionCount(); i += 2) {
            if (map.isNull(i)) {
                return null;
            }
            norm += DOUBLE.getDouble(map, i) * DOUBLE.getDouble(map, i);
        }

        return Math.sqrt(norm);
    }
}
