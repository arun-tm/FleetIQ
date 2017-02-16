package com.teramatrix.fleetiq.velocimeter.velocimeterlibrary.painter.needle;


import com.teramatrix.fleetiq.velocimeter.velocimeterlibrary.painter.Painter;

/**
 * @author Adrián García Lomas
 */
public interface NeedlePainter extends Painter {

  void setValue(float value);
}
