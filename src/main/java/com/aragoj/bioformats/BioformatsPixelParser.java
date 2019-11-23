package com.aragoj.bioformats;

import loci.common.DataTools;

public class BioformatsPixelParser {

  /**
   * This attemps to convert the image to a 24-bit (8 bpp) interleaved RGB image. 24-bit is ideal
   * for *good enough* fidelity while keeping memory consumption "low". Color fidelity is not as
   * important since measuring is the purpose of AragoJ.
   * <p>
   * For now, this also assumes that the nr of color channels is 3 (RGB).
   * <p>
   * The routines for this are based on ImageTools/DataTools classes in Bioformats lib
   */
  static byte[] parseRawData(byte[] rawData, int width, int height, int bytespp,
      boolean isFloat, boolean interleaved, boolean isSigned, boolean isLittleEndian, Double min,
      Double max) {

    PrimitiveType primitiveType = getPrimitiveType(bytespp, isFloat);

    // If min & max are null, it is assumed to be 0 and max value of the pixel type respectively
    double minValue = min != null ? min : getDefaultMinValue(primitiveType, isSigned);
    double maxValue = max != null ? max : getDefaultMaxValue(primitiveType, isSigned);

    System.out.println("MinValue: "
        + minValue
        + " MaxValue: "
        + maxValue
        + " Primitive: "
        + primitiveType
        + " "
        + "BPP: "
        + bytespp);

    byte[] fin = new byte[rawData.length / bytespp];

    if (interleaved) {
      for (int i = 0; i < fin.length; i++) {
        fin[i] =
            convertToSigned24bits(rawData, i * bytespp, minValue, maxValue, primitiveType, bytespp,
                isLittleEndian);
      }
    } else {
      for (int i = 0; i < width * height; i++) {
        fin[i * 3] =
            convertToSigned24bits(rawData, i * bytespp, minValue, maxValue, primitiveType, bytespp,
                isLittleEndian);
        fin[i * 3 + 1] =
            convertToSigned24bits(rawData, width * height + i * bytespp, minValue, maxValue,
                primitiveType, bytespp, isLittleEndian);
        fin[i * 3 + 2] =
            convertToSigned24bits(rawData, width * height * 2 + i * bytespp, minValue, maxValue,
                primitiveType, bytespp, isLittleEndian);
      }
    }
    return fin;
  }

  private static byte convertToSigned24bits(byte[] rawData, int offset, double minValue,
      double maxValue, PrimitiveType primitiveType, int bytespp, boolean isLittleEndian) {
    double val = convertBytesToValue(primitiveType, rawData, offset, bytespp, isLittleEndian);
    double range = maxValue - minValue;
    double mult = 255d / range;
    return (byte) Math.abs(((val - minValue) * mult));
  }

  private static double convertBytesToValue(PrimitiveType primitiveType, byte[] rawData, int offset,
      int bytespp, boolean isLittleEndian) {
    switch (primitiveType) {
      default:
      case BYTE:
      case SHORT:
        return DataTools.bytesToShort(rawData, offset, bytespp, isLittleEndian);
      case INT:
        return DataTools.bytesToInt(rawData, offset, bytespp, isLittleEndian);
      case FLOAT:
        return DataTools.bytesToFloat(rawData, offset, bytespp, isLittleEndian);
      case DOUBLE:
        return DataTools.bytesToDouble(rawData, offset, bytespp, isLittleEndian);
    }
  }

  private static PrimitiveType getPrimitiveType(int bytespp, boolean isFloat) {
    switch (bytespp) {
      case 1:
        return PrimitiveType.BYTE;
      case 2:
        return PrimitiveType.SHORT;
      case 4:
        return isFloat ? PrimitiveType.FLOAT : PrimitiveType.INT;
      case 8:
        return PrimitiveType.DOUBLE;
    }
    return PrimitiveType.BYTE;
  }

  private static Double getDefaultMinValue(PrimitiveType type, boolean isSigned) {
    switch (type) {
      case BYTE:
        return isSigned ? -128d : 0;
      case SHORT:
        return isSigned ? (double) Short.MIN_VALUE : 0;
      case INT:
        return isSigned ? (double) Integer.MIN_VALUE : 0;
      case FLOAT:
        return isSigned ? (double) Float.MIN_VALUE : 0;
      case DOUBLE:
        return isSigned ? Double.MIN_VALUE : 0;
      default:
        return 0.0;
    }
  }

  private static Double getDefaultMaxValue(PrimitiveType type, boolean isSigned) {
    switch (type) {
      case BYTE:
        return isSigned ? 127d : 255d;
      case SHORT:
        return isSigned ? Short.MAX_VALUE : (double) 0xffff;
      case INT:
        return isSigned ? Integer.MAX_VALUE : (double) 0xffffffffL;
      case FLOAT:
        return (double) Float.MAX_VALUE;
      case DOUBLE:
        return Double.MAX_VALUE;
    }
    return 255d;
  }

  private enum PrimitiveType {
    BYTE, // 8 bit
    SHORT, // 16 bit
    INT, // 32 bit
    FLOAT, // 32 bit
    DOUBLE // 64 bit
  }
}
